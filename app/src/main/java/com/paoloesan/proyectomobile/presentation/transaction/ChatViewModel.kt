package com.paoloesan.proyectomobile.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.ChatMessageModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
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

data class ChatUiState(
    val messages: List<ChatMessageModel> = emptyList(),
    val currentUserId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var transaccionId: Int = 0
    private var chatChannel: RealtimeChannel? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun initialize(transactionId: Int) {
        transaccionId = transactionId
        viewModelScope.launch {
            try {
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                if (authId == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado") }
                    return@launch
                }

                val perfil = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter { eq("auth_id", authId) }
                    }
                    .decodeSingle<UserProfileModel>()

                _uiState.update { it.copy(currentUserId = perfil.usuarioId) }
                loadHistory()
                subscribeToNewMessages()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al iniciar chat: ${e.localizedMessage}") }
            }
        }
    }

    private suspend fun loadHistory() {
        try {
            val historial = Supabase.client.postgrest["mensajes_chat"]
                .select {
                    filter { eq("transaccion_id", transaccionId) }
                    order(column = "fecha_envio", order = Order.ASCENDING)
                }
                .decodeList<ChatMessageModel>()

            _uiState.update { it.copy(messages = historial, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar mensajes: ${e.localizedMessage}") }
        }
    }

    private fun subscribeToNewMessages() {
        viewModelScope.launch {
            try {
                chatChannel = Supabase.client.channel("chat_transaccion_$transaccionId")

                val flow = chatChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "mensajes_chat"
                }

                chatChannel!!.subscribe()

                flow.collect { action ->
                    action.record?.let { record ->
                        val nuevoMensaje = json.decodeFromJsonElement(ChatMessageModel.serializer(), record)
                        if (nuevoMensaje.transaccionId == transaccionId) {
                            _uiState.update {
                                val exists = it.messages.any { msg ->
                                    msg.mensajeId != null && msg.mensajeId == nuevoMensaje.mensajeId
                                }
                                if (!exists) {
                                    it.copy(messages = it.messages + nuevoMensaje)
                                } else it
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback silencioso: los mensajes se cargan al hacer pull
            }
        }
    }

    fun sendMessage(text: String) {
        val currentUserId = _uiState.value.currentUserId ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val mensaje = ChatMessageModel(
                    transaccionId = transaccionId,
                    remitenteId = currentUserId,
                    contenido = text.trim()
                )
                Supabase.client.postgrest["mensajes_chat"].insert(mensaje)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al enviar mensaje: ${e.localizedMessage}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
