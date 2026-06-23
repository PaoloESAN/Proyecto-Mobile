package com.paoloesan.proyectomobile.presentation.p2p

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.OfferModel
import com.paoloesan.proyectomobile.data.model.PaymentMethodModel
import com.paoloesan.proyectomobile.data.model.TransactionModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyOfferUiItem(
    val offerId: Int,
    val type: String,
    val currency: String,
    val amount: Double,
    val rate: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val paymentMethod: String,
    val status: String,
    val activeTransactionId: Int? = null
)

data class IncomingTransactionItem(
    val transactionId: Int,
    val offerId: Int,
    val buyerName: String,
    val type: String,
    val currency: String,
    val amount: Double,
    val rate: Double,
    val status: String
)

data class SentTransactionItem(
    val transactionId: Int,
    val sellerName: String,
    val type: String,
    val currency: String,
    val amount: Double,
    val rate: Double,
    val status: String
)

data class MyOffersUiState(
    val isLoading: Boolean = false,
    val myOffers: List<MyOfferUiItem> = emptyList(),
    val incomingTransactions: List<IncomingTransactionItem> = emptyList(),
    val sentTransactions: List<SentTransactionItem> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MyOffersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyOffersUiState())
    val uiState: StateFlow<MyOffersUiState> = _uiState.asStateFlow()

    private var currentUserId: Int? = null

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                if (authId == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado") }
                    return@launch
                }

                val perfil = Supabase.client.postgrest["usuarios"]
                    .select { filter { eq("auth_id", authId) } }
                    .decodeSingle<UserProfileModel>()

                currentUserId = perfil.usuarioId
                val userId = perfil.usuarioId ?: throw Exception("Usuario no encontrado")

                val offers = Supabase.client.postgrest["ofertas"]
                    .select {
                        filter {
                            eq("usuario_creador_id", userId)
                            isIn("estado", listOf("Activa", "En Proceso"))
                        }
                    }
                    .decodeList<OfferModel>()

                val metodos = Supabase.client.postgrest["metodos_pago"]
                    .select { filter { eq("usuario_id", userId) } }
                    .decodeList<PaymentMethodModel>()
                val metodoMap = metodos.associateBy { it.metodoPagoId }

                val offerIds = offers.mapNotNull { it.offerId }
                val incomingTx = if (offerIds.isNotEmpty()) {
                    Supabase.client.postgrest["transacciones"]
                        .select {
                            filter {
                                isIn("oferta_id", offerIds)
                                isIn("estado", listOf("Pendiente", "En Proceso", "Pagado", "Disputa"))
                            }
                        }
                        .decodeList<TransactionModel>()
                } else emptyList()

                val sentTx = Supabase.client.postgrest["transacciones"]
                    .select {
                        filter {
                            eq("usuario_comprador_id", userId)
                            isIn("estado", listOf("Pendiente", "En Proceso", "Pagado", "Disputa"))
                        }
                    }
                    .decodeList<TransactionModel>()

                val sentOfferIds = sentTx.map { it.offerId }.distinct()
                val sentOffers = if (sentOfferIds.isNotEmpty()) {
                    Supabase.client.postgrest["ofertas"]
                        .select { filter { isIn("oferta_id", sentOfferIds) } }
                        .decodeList<OfferModel>()
                } else emptyList()
                val sentOfferMap = sentOffers.associateBy { it.offerId }

                val buyerIds = incomingTx.map { it.usuarioCompradorId }
                val sellerIds = sentOffers.map { it.usuarioCreadorId }
                val allNeededIds = (buyerIds + sellerIds).filter { it != userId }.distinct()

                val userNames = if (allNeededIds.isNotEmpty()) {
                    Supabase.client.postgrest["usuarios"]
                        .select { filter { isIn("usuario_id", allNeededIds) } }
                        .decodeList<UserProfileModel>()
                        .associate { (it.usuarioId ?: -1) to "${it.nombres} ${it.apellidos}" }
                } else emptyMap()

                val sentMetodoIds = sentOffers.map { it.metodoPagoId }.distinct()
                val allMetodoIds = (metodoMap.keys + sentMetodoIds).filterNotNull()
                val allMetodos = if (allMetodoIds.isNotEmpty()) {
                    Supabase.client.postgrest["metodos_pago"]
                        .select { filter { isIn("metodo_pago_id", allMetodoIds) } }
                        .decodeList<PaymentMethodModel>()
                } else emptyList()
                val allMetodoMap = allMetodos.associateBy { it.metodoPagoId }

                val myOfferItems = offers.map { offer ->
                    val metodo = allMetodoMap[offer.metodoPagoId]
                    val activeTx = incomingTx.firstOrNull {
                        it.offerId == offer.offerId && it.status == "En Proceso"
                    }
                    MyOfferUiItem(
                        offerId = offer.offerId ?: -1,
                        type = offer.tipoOperacion,
                        currency = offer.currency,
                        amount = offer.montoTotal,
                        rate = offer.price,
                        minLimit = offer.montoMinimo,
                        maxLimit = offer.montoMaximo,
                        paymentMethod = metodo?.banco ?: "",
                        status = offer.estado,
                        activeTransactionId = activeTx?.transactionId
                    )
                }

                val incomingItems = incomingTx.filter { it.status == "Pendiente" }.map { tx ->
                    val offer = offers.firstOrNull { it.offerId == tx.offerId }
                    IncomingTransactionItem(
                        transactionId = tx.transactionId ?: -1,
                        offerId = tx.offerId,
                        buyerName = userNames[tx.usuarioCompradorId] ?: "Usuario ${tx.usuarioCompradorId}",
                        type = offer?.tipoOperacion ?: "",
                        currency = offer?.currency ?: "",
                        amount = tx.amount,
                        rate = tx.tipoCambioAplicado,
                        status = tx.status
                    )
                }

                val sentItems = sentTx.map { tx ->
                    val offer = sentOfferMap[tx.offerId]
                    SentTransactionItem(
                        transactionId = tx.transactionId ?: -1,
                        sellerName = if (offer != null) {
                            userNames[offer.usuarioCreadorId] ?: "Usuario ${offer.usuarioCreadorId}"
                        } else "Desconocido",
                        type = offer?.tipoOperacion ?: "",
                        currency = offer?.currency ?: "",
                        amount = tx.amount,
                        rate = tx.tipoCambioAplicado,
                        status = tx.status
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        myOffers = myOfferItems,
                        incomingTransactions = incomingItems,
                        sentTransactions = sentItems
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al cargar datos: ${e.localizedMessage}")
                }
            }
        }
    }

    fun editOffer(offerId: Int, montoTotal: Double, montoMinimo: Double, montoMaximo: Double) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["ofertas"].update({
                    set("monto_total", montoTotal)
                    set("monto_minimo", montoMinimo)
                    set("monto_maximo", montoMaximo)
                }) {
                    filter { eq("oferta_id", offerId) }
                }
                _uiState.update { it.copy(successMessage = "Oferta actualizada correctamente") }
                loadData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al actualizar oferta: ${e.localizedMessage}")
                }
            }
        }
    }

    fun cancelOffer(offerId: Int) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["ofertas"].update({
                    set("estado", "Cancelada")
                }) {
                    filter { eq("oferta_id", offerId) }
                }
                _uiState.update { it.copy(successMessage = "Oferta cancelada correctamente") }
                loadData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al cancelar oferta: ${e.localizedMessage}")
                }
            }
        }
    }

    fun acceptTransaction(transactionId: Int) {
        viewModelScope.launch {
            try {
                val tx = Supabase.client.postgrest["transacciones"]
                    .select { filter { eq("transaccion_id", transactionId) } }
                    .decodeSingle<TransactionModel>()

                Supabase.client.postgrest["transacciones"].update({
                    set("estado", "En Proceso")
                }) {
                    filter { eq("transaccion_id", transactionId) }
                }

                Supabase.client.postgrest["ofertas"].update({
                    set("estado", "En Proceso")
                }) {
                    filter { eq("oferta_id", tx.offerId) }
                }

                _uiState.update { it.copy(successMessage = "Transacción aceptada correctamente") }
                loadData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al aceptar transacción: ${e.localizedMessage}")
                }
            }
        }
    }

    fun rejectTransaction(transactionId: Int) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["transacciones"].update({
                    set("estado", "Cancelado")
                }) {
                    filter { eq("transaccion_id", transactionId) }
                }
                _uiState.update { it.copy(successMessage = "Transacción rechazada") }
                loadData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al rechazar transacción: ${e.localizedMessage}")
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
