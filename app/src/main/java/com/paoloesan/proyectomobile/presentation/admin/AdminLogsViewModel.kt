package com.paoloesan.proyectomobile.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.model.TransactionLogModel
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

val ALL_LOG_EVENT_TYPES = setOf(
    "transaccion_iniciada",
    "estado_cambiado",
    "comprobante_subido",
    "confirmacion_usuario",
    "disputa_abierta",
    "disputa_resuelta",
    "calificacion_registrada",
    "verificacion_ia",
    "oferta_creada",
    "oferta_editada",
    "oferta_cancelada",
    "usuario_bloqueado",
    "usuario_desbloqueado"
)

data class AdminLogsUiState(
    val logs: List<TransactionLogModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilters: Set<String> = ALL_LOG_EVENT_TYPES, // todos seleccionados por defecto
    val isConnected: Boolean = false
) {
    val filteredLogs: List<TransactionLogModel>
        get() {
            val query = searchQuery.trim()
            return logs.filter { log ->
                val matchesFilter = log.tipoEvento in selectedFilters
                val matchesSearch = if (query.isEmpty()) true else {
                    log.transaccionId?.toString()?.contains(query, ignoreCase = true) == true ||
                    log.descripcion.contains(query, ignoreCase = true) ||
                    log.usuarioId?.toString()?.contains(query, ignoreCase = true) == true ||
                    log.tipoEvento.contains(query, ignoreCase = true) ||
                    (log.datosExtra?.toString()?.contains(query, ignoreCase = true) == true)
                }
                matchesFilter && matchesSearch
            }
        }
}

class AdminLogsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLogsUiState())
    val uiState: StateFlow<AdminLogsUiState> = _uiState.asStateFlow()

    private var logsChannel: RealtimeChannel? = null
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadLogs()
        subscribeToNewLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val logs = Supabase.client.postgrest["transaction_logs"]
                    .select {
                        order("fecha_evento", Order.DESCENDING)
                        limit(300)
                    }
                    .decodeList<TransactionLogModel>()

                _uiState.update { it.copy(isLoading = false, logs = logs) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar logs: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun subscribeToNewLogs() {
        viewModelScope.launch {
            try {
                logsChannel = Supabase.client.channel("admin_logs_channel")

                val flow = logsChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "transaction_logs"
                }

                logsChannel!!.subscribe()
                _uiState.update { it.copy(isConnected = true) }

                flow.collect { action ->
                    val nuevoLog = json.decodeFromJsonElement(
                        TransactionLogModel.serializer(),
                        action.record
                    )
                    // Insertar al principio de la lista (más reciente primero)
                    _uiState.update { state ->
                        val alreadyExists = state.logs.any { it.logId == nuevoLog.logId }
                        if (!alreadyExists) {
                            state.copy(logs = listOf(nuevoLog) + state.logs)
                        } else state
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isConnected = false) }
                android.util.Log.e("AdminLogsViewModel", "Error en realtime: ${e.localizedMessage}", e)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFilter(filter: String) {
        _uiState.update { state ->
            val current = state.selectedFilters
            val updated = if (filter in current) current - filter else current + filter
            state.copy(selectedFilters = updated)
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(selectedFilters = ALL_LOG_EVENT_TYPES) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try {
                logsChannel?.unsubscribe()
            } catch (_: Exception) { }
        }
    }
}
