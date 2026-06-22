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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun initChat(transactionId: Int) {
        this.transactionId = transactionId
        loadMessages()
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
                    // Si es administrador (readOnly = true) y no pertenece a ningún participante directo,
                    // isOwn indicará que pertenece al comprador o vendedor (por ej, vendedor isOwn = true y comprador false).
                    // Pero para consistencia, isOwn = true si el remitente coincide con el usuario logueado.
                    // Si el administrador lo lee, myUserId es el del admin, por lo tanto ningún mensaje coincidirá y todos serán isOwn = false.
                    // Para que se sigan viendo a la izquierda y derecha adecuadamente incluso para el admin,
                    // podemos definir isOwn = msg.remitenteId == transaction.usuarioVendedorId (o comprador) si el usuario actual no participa.
                    val isParticipant = myUserId == transaction.usuarioCompradorId || myUserId == transaction.usuarioVendedorId
                    val isOwn = if (isParticipant) {
                        msg.remitenteId == myUserId
                    } else {
                        // Si es el administrador (no participante), mandamos los mensajes del vendedor a la derecha (isOwn = true) y del comprador a la izquierda
                        msg.remitenteId == transaction.usuarioVendedorId
                    }

                    val senderName = if (msg.remitenteId == transaction.usuarioCompradorId) compradorNombre else vendedorNombre
                    val timeStr = formatFechaEnvio(msg.fechaEnvio)
                    ChatMessageState(
                        id = (msg.mensajeId ?: 0).toString(),
                        text = msg.contenido,
                        isOwn = isOwn,
                        time = timeStr,
                        senderName = senderName
                    )
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

                // Recargar mensajes
                loadMessages()
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
}
