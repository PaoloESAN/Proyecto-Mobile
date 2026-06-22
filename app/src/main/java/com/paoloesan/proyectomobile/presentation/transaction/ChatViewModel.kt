package com.paoloesan.proyectomobile.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.ChatMessageModel
import com.paoloesan.proyectomobile.data.model.TransactionModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class ChatMessageState(
    val id: String,
    val text: String,
    val isOwn: Boolean,
    val time: String,
    val senderName: String
)

data class ChatUiState(
    val messages: List<ChatMessageState> = emptyList(),
    val contraparteName: String = "Cargando...",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentUserId: Int? = null
    private var transactionId: Int = 0
    private var chatChannel: RealtimeChannel? = null
    private val json = Json { ignoreUnknownKeys = true }

    // Guardar información temporal de la transacción y perfiles para mapear mensajes entrantes en tiempo real
    private var cachedTransaction: TransactionModel? = null
    private var cachedCompradorNombre: String = ""
    private var cachedVendedorNombre: String = ""

    fun initChat(transactionId: Int) {
        this.transactionId = transactionId
        loadMessages()
        subscribeToNewMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Obtener auth_id del usuario logueado esperando a que inicialice
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                var myUserId: Int? = null
                if (authId != null) {
                    val perfil = Supabase.client.postgrest["usuarios"]
                        .select {
                            filter {
                                eq("auth_id", authId)
                            }
                        }
                        .decodeList<UserProfileModel>()
                        .firstOrNull()
                    myUserId = perfil?.usuarioId
                    currentUserId = myUserId
                }

                // 2. Obtener transacción
                val transaction = Supabase.client.postgrest["transacciones"]
                    .select {
                        filter {
                            eq("transaccion_id", transactionId)
                        }
                    }
                    .decodeSingle<TransactionModel>()
                cachedTransaction = transaction

                // 3. Obtener perfiles de comprador y vendedor
                val comprador = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter {
                            eq("usuario_id", transaction.usuarioCompradorId)
                        }
                    }
                    .decodeSingle<UserProfileModel>()

                val vendedor = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter {
                            eq("usuario_id", transaction.usuarioVendedorId)
                        }
                    }
                    .decodeSingle<UserProfileModel>()

                val compradorNombre = "${comprador.nombres} ${comprador.apellidos}"
                val vendedorNombre = "${vendedor.nombres} ${vendedor.apellidos}"
                cachedCompradorNombre = compradorNombre
                cachedVendedorNombre = vendedorNombre

                // Definir el nombre de la contraparte para la UI
                val contraparte = if (myUserId == transaction.usuarioCompradorId) {
                    vendedorNombre
                } else {
                    compradorNombre
                }

                // 4. Obtener mensajes de chat
                val dbMessages = Supabase.client.postgrest["mensajes_chat"]
                    .select {
                        filter {
                            eq("transaccion_id", transactionId)
                        }
                    }
                    .decodeList<ChatMessageModel>()
                    .sortedBy { it.fechaEnvio ?: "" }

                val chatStates = dbMessages.map { msg ->
                    mapToState(msg, myUserId, transaction, compradorNombre, vendedorNombre)
                }

                _uiState.update {
                    it.copy(
                        messages = chatStates,
                        contraparteName = contraparte,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar chat: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun mapToState(
        msg: ChatMessageModel,
        myUserId: Int?,
        transaction: TransactionModel,
        compradorNombre: String,
        vendedorNombre: String
    ): ChatMessageState {
        val isParticipant = myUserId == transaction.usuarioCompradorId || myUserId == transaction.usuarioVendedorId
        val isOwn = if (isParticipant) {
            msg.remitenteId == myUserId
        } else {
            // Si es el administrador (no participante), mandamos los mensajes del vendedor a la derecha (isOwn = true) y del comprador a la izquierda
            msg.remitenteId == transaction.usuarioVendedorId
        }

        val senderName = if (msg.remitenteId == transaction.usuarioCompradorId) compradorNombre else vendedorNombre
        val timeStr = formatFechaEnvio(msg.fechaEnvio)
        return ChatMessageState(
            id = (msg.mensajeId ?: 0).toString(),
            text = msg.contenido,
            isOwn = isOwn,
            time = timeStr,
            senderName = senderName
        )
    }

    private fun subscribeToNewMessages() {
        viewModelScope.launch {
            try {
                chatChannel = Supabase.client.channel("chat_transaccion_$transactionId")

                val flow = chatChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "mensajes_chat"
                }

                chatChannel!!.subscribe()

                flow.collect { action ->
                    action.record?.let { record ->
                        val nuevoMensaje = json.decodeFromJsonElement(ChatMessageModel.serializer(), record)
                        if (nuevoMensaje.transaccionId == transactionId) {
                            val tx = cachedTransaction ?: return@let
                            val compNombre = cachedCompradorNombre
                            val vendNombre = cachedVendedorNombre
                            val myId = currentUserId

                            val mapped = mapToState(nuevoMensaje, myId, tx, compNombre, vendNombre)

                            _uiState.update { state ->
                                val exists = state.messages.any { it.id == mapped.id }
                                if (!exists) {
                                    state.copy(messages = state.messages + mapped)
                                } else state
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback silencioso
            }
        }
    }

    fun enviarMensaje(texto: String) {
        val myUserId = currentUserId ?: return
        if (texto.isBlank()) return

        viewModelScope.launch {
            try {
                val nuevoMsg = ChatMessageModel(
                    transaccionId = transactionId,
                    remitenteId = myUserId,
                    contenido = texto.trim()
                )

                Supabase.client.postgrest["mensajes_chat"]
                    .insert(nuevoMsg)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error al enviar mensaje: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun formatFechaEnvio(fechaEnvio: String?): String {
        if (fechaEnvio == null) return ""
        val tIndex = fechaEnvio.indexOf('T')
        if (tIndex != -1 && fechaEnvio.length > tIndex + 6) {
            return fechaEnvio.substring(tIndex + 1, tIndex + 6)
        }
        return fechaEnvio.take(16)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try {
                chatChannel?.unsubscribe()
            } catch (_: Exception) {}
        }
    }
}
