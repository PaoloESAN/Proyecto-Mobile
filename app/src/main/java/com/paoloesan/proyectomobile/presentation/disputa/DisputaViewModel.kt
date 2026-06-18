package com.paoloesan.proyectomobile.presentation.disputa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.model.DisputeModel
import com.paoloesan.proyectomobile.data.model.TransactionModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisputaUiState(
    val disputasActivas: List<Disputa> = emptyList(),
    val disputasResueltas: List<Disputa> = emptyList(),
    val isLoading: Boolean = false,
    val transientMessage: String? = null
)

class DisputaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DisputaUiState())
    val uiState: StateFlow<DisputaUiState> = _uiState.asStateFlow()

    init {
        loadDisputas()
    }

    fun loadDisputas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val disputes = Supabase.client.postgrest["disputas"]
                    .select {
                        filter {
                            isIn("estado", listOf("Abierta", "Resuelta"))
                        }
                    }
                    .decodeList<DisputeModel>()

                val activas = mutableListOf<Disputa>()
                val resueltas = mutableListOf<Disputa>()

                for (dispute in disputes) {
                    try {
                        val transaction = Supabase.client.postgrest["transacciones"]
                            .select {
                                filter {
                                    eq("transaccion_id", dispute.transaccionId)
                                }
                            }
                            .decodeSingle<TransactionModel>()

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

                        val d = Disputa.fromModels(dispute, transaction, comprador, vendedor)
                        if (dispute.estado == "Abierta") {
                            activas.add(d)
                        } else {
                            resueltas.add(d)
                        }
                    } catch (_: Exception) {
                        // skip disputes with missing relations
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        disputasActivas = activas,
                        disputasResueltas = resueltas
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transientMessage = "Error al cargar disputas: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun resolverDisputa(disputaId: Int, aFavorComprador: Boolean) {
        viewModelScope.launch {
            try {
                val disputa = _uiState.value.disputasActivas.firstOrNull { it.id == disputaId }
                    ?: return@launch

                val resolucion = if (aFavorComprador) "A favor del comprador" else "A favor del vendedor"
                val nuevoEstadoTx = if (aFavorComprador) "Cancelado" else "Finalizado"

                Supabase.client.postgrest["disputas"]
                    .update({
                        set("estado", "Resuelta")
                        set("resolucion", resolucion)
                    }) {
                        filter {
                            eq("disputa_id", disputaId)
                        }
                    }

                Supabase.client.postgrest["transacciones"]
                    .update({
                        set("estado", nuevoEstadoTx)
                    }) {
                        filter {
                            eq("transaccion_id", disputa.transaccionId)
                        }
                    }

                val resuelta = _uiState.value.disputasActivas.firstOrNull { it.id == disputaId }
                if (resuelta != null) {
                    _uiState.update { state ->
                        state.copy(
                            disputasActivas = state.disputasActivas.filter { it.id != disputaId },
                            disputasResueltas = state.disputasResueltas + resuelta.copy(
                                estado = "Resuelta",
                                resolucion = resolucion
                            ),
                            transientMessage = "Disputa resuelta: $resolucion"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(transientMessage = "Error al resolver disputa: ${e.localizedMessage}")
                }
            }
        }
    }

    fun getDisputaById(id: Int): Disputa? {
        val state = _uiState.value
        return state.disputasActivas.find { it.id == id }
            ?: state.disputasResueltas.find { it.id == id }
    }

    fun setMessage(message: String) {
        _uiState.update { it.copy(transientMessage = message) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }
}
