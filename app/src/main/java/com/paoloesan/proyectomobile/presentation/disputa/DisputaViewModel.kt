package com.paoloesan.proyectomobile.presentation.disputa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.model.DisputeModel
import com.paoloesan.proyectomobile.data.model.TransactionModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import com.paoloesan.proyectomobile.data.model.OfferModel
import com.paoloesan.proyectomobile.data.model.ComprobanteModel
import com.paoloesan.proyectomobile.data.model.ChatMessageModel
import com.paoloesan.proyectomobile.data.model.PaymentMethodModel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisputaDetalleState(
    val dispute: DisputeModel,
    val transaction: TransactionModel,
    val offer: OfferModel,
    val comprador: UserProfileModel,
    val vendedor: UserProfileModel,
    val metodoPagoComprador: PaymentMethodModel? = null,
    val metodoPagoVendedor: PaymentMethodModel? = null,
    val comprobantes: List<ComprobanteModel> = emptyList(),
    val mensajesChat: List<ChatMessageModel> = emptyList()
)

data class DisputaUiState(
    val disputasActivas: List<Disputa> = emptyList(),
    val disputasResueltas: List<Disputa> = emptyList(),
    val isLoading: Boolean = false,
    val transientMessage: String? = null
)

class DisputaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DisputaUiState())
    val uiState: StateFlow<DisputaUiState> = _uiState.asStateFlow()

    private val _detalleState = MutableStateFlow<DisputaDetalleState?>(null)
    val detalleState: StateFlow<DisputaDetalleState?> = _detalleState.asStateFlow()

    init {
        loadDisputas()
    }

    fun loadDisputaDetalle(disputaId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dispute = Supabase.client.postgrest["disputas"]
                    .select {
                        filter {
                            eq("disputa_id", disputaId)
                        }
                    }
                    .decodeSingle<DisputeModel>()

                val transaction = Supabase.client.postgrest["transacciones"]
                    .select {
                        filter {
                            eq("transaccion_id", dispute.transaccionId)
                        }
                    }
                    .decodeSingle<TransactionModel>()

                val offer = Supabase.client.postgrest["ofertas"]
                    .select {
                        filter {
                            eq("oferta_id", transaction.offerId)
                        }
                    }
                    .decodeSingle<OfferModel>()

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

                val metodoPagoComprador = transaction.metodoPagoCompradorId?.let { id ->
                    try {
                        Supabase.client.postgrest["metodos_pago"]
                            .select {
                                filter {
                                    eq("metodo_pago_id", id)
                                }
                            }
                            .decodeList<PaymentMethodModel>()
                            .firstOrNull()
                    } catch (e: Exception) {
                        null
                    }
                }

                val metodoPagoVendedor = try {
                    Supabase.client.postgrest["metodos_pago"]
                        .select {
                            filter {
                                eq("metodo_pago_id", offer.metodoPagoId)
                            }
                        }
                    .decodeList<PaymentMethodModel>()
                    .firstOrNull()
                } catch (e: Exception) {
                    null
                }

                val comprobantes = Supabase.client.postgrest["comprobantes"]
                    .select {
                        filter {
                            eq("transaccion_id", transaction.transactionId!!)
                        }
                    }
                    .decodeList<ComprobanteModel>()

                val mensajes = Supabase.client.postgrest["mensajes_chat"]
                    .select {
                        filter {
                            eq("transaccion_id", transaction.transactionId!!)
                        }
                    }
                    .decodeList<ChatMessageModel>()
                    .sortedBy { it.fechaEnvio ?: "" }

                _detalleState.value = DisputaDetalleState(
                    dispute = dispute,
                    transaction = transaction,
                    offer = offer,
                    comprador = comprador,
                    vendedor = vendedor,
                    metodoPagoComprador = metodoPagoComprador,
                    metodoPagoVendedor = metodoPagoVendedor,
                    comprobantes = comprobantes,
                    mensajesChat = mensajes
                )
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transientMessage = "Error al cargar detalle: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearDetalleState() {
        _detalleState.value = null
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

                // 1. Obtener la transacción para conocer el id de la oferta asociada
                val transaction = Supabase.client.postgrest["transacciones"]
                    .select {
                        filter {
                            eq("transaccion_id", disputa.transaccionId)
                        }
                    }
                    .decodeSingle<TransactionModel>()

                // 2. Actualizar la disputa
                Supabase.client.postgrest["disputas"]
                    .update({
                        set("estado", "Resuelta")
                        set("resolucion", resolucion)
                    }) {
                        filter {
                            eq("disputa_id", disputaId)
                        }
                    }

                // 3. Actualizar la transacción
                Supabase.client.postgrest["transacciones"]
                    .update({
                        set("estado", nuevoEstadoTx)
                    }) {
                        filter {
                            eq("transaccion_id", disputa.transaccionId)
                        }
                    }

                // 4. Si es a favor del comprador, reactivar la oferta original
                if (aFavorComprador) {
                    Supabase.client.postgrest["ofertas"]
                        .update({
                            set("estado", "Activa")
                        }) {
                            filter {
                                eq("oferta_id", transaction.offerId)
                            }
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
