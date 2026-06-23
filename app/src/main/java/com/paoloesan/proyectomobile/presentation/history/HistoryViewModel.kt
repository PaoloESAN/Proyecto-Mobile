package com.paoloesan.proyectomobile.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.CalificacionModel
import com.paoloesan.proyectomobile.data.model.OfferModel
import com.paoloesan.proyectomobile.data.model.TransactionModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiItem(
    val transactionId: Int,
    val fecha: String,
    val tipoOperacion: String,
    val monto: Double,
    val tipoCambio: Double,
    val estado: String,
    val moneda: String,
    val contraparteNombre: String,
    val contraparteId: Int,
    val offerId: Int
)

data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyItems: List<HistoryUiItem> = emptyList(),
    val ratedTransactionIds: Set<Int> = emptySet(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

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

                val txAsBuyer = Supabase.client.postgrest["transacciones"]
                    .select {
                        filter {
                            eq("usuario_comprador_id", userId)
                            eq("estado", "Finalizado")
                        }
                    }
                    .decodeList<TransactionModel>()

                val txAsSeller = Supabase.client.postgrest["transacciones"]
                    .select {
                        filter {
                            eq("usuario_vendedor_id", userId)
                            eq("estado", "Finalizado")
                        }
                    }
                    .decodeList<TransactionModel>()

                val allTx = txAsBuyer + txAsSeller

                if (allTx.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val offerIds = allTx.map { it.offerId }.distinct()
                val offers = if (offerIds.isNotEmpty()) {
                    Supabase.client.postgrest["ofertas"]
                        .select { filter { isIn("oferta_id", offerIds) } }
                        .decodeList<OfferModel>()
                } else emptyList()
                val offerMap = offers.associateBy { it.offerId }

                val counterpartyIds = allTx.map { tx ->
                    if (tx.usuarioCompradorId == userId) tx.usuarioVendedorId else tx.usuarioCompradorId
                }.distinct()

                val counterparties = if (counterpartyIds.isNotEmpty()) {
                    Supabase.client.postgrest["usuarios"]
                        .select { filter { isIn("usuario_id", counterpartyIds) } }
                        .decodeList<UserProfileModel>()
                } else emptyList()
                val counterpartyMap = counterparties.associateBy { it.usuarioId }

                val txIds = allTx.mapNotNull { it.transactionId }
                val calificaciones = if (txIds.isNotEmpty()) {
                    Supabase.client.postgrest["calificaciones"]
                        .select {
                            filter {
                                eq("usuario_evaluador_id", userId)
                                isIn("transaccion_id", txIds)
                            }
                        }
                        .decodeList<CalificacionModel>()
                } else emptyList()

                val ratedIds = calificaciones.mapNotNull { it.transaccionId }.toSet()

                val historyItems = allTx.map { tx ->
                    val offer = offerMap[tx.offerId]
                    val contraparteId = if (tx.usuarioCompradorId == userId) tx.usuarioVendedorId else tx.usuarioCompradorId
                    val contraparte = counterpartyMap[contraparteId]
                    val tipo = if (tx.usuarioCompradorId == userId) "Compra" else "Venta"

                    HistoryUiItem(
                        transactionId = tx.transactionId ?: -1,
                        fecha = tx.createDate?.take(10) ?: "",
                        tipoOperacion = tipo,
                        monto = tx.amount,
                        tipoCambio = tx.tipoCambioAplicado,
                        estado = tx.status,
                        moneda = offer?.currency ?: "",
                        contraparteNombre = contraparte?.let { "${it.nombres} ${it.apellidos}" } ?: "Usuario",
                        contraparteId = contraparteId,
                        offerId = tx.offerId
                    )
                }.sortedByDescending { it.fecha }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        historyItems = historyItems,
                        ratedTransactionIds = ratedIds
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al cargar historial: ${e.localizedMessage}")
                }
            }
        }
    }

    fun submitRating(transactionId: Int, contraparteId: Int, puntaje: Int, comentario: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: throw Exception("No autenticado")

                val calificacion = CalificacionModel(
                    transaccionId = transactionId,
                    usuarioEvaluadorId = userId,
                    usuarioEvaluadoId = contraparteId,
                    puntaje = puntaje,
                    comentario = comentario.ifBlank { null }
                )

                Supabase.client.postgrest["calificaciones"].insert(calificacion)

                _uiState.update {
                    it.copy(
                        successMessage = "Gracias por su calificación!",
                        ratedTransactionIds = it.ratedTransactionIds + transactionId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al calificar: ${e.localizedMessage}")
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
