package com.paoloesan.proyectomobile.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.CalificacionModel
import com.paoloesan.proyectomobile.data.model.ComprobanteModel
import com.paoloesan.proyectomobile.data.model.DisputeModel
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

data class TransactionUiState(
    val isLoading: Boolean = true,
    val transaction: TransactionModel? = null,
    val offer: OfferModel? = null,
    // ¿El usuario actual es el vendedor?
    val isSeller: Boolean = false,
    val currentUserId: Int? = null,
    // Datos de la cuenta de destino (a quien pago)
    val destBankName: String = "",
    val destAccountNumber: String = "",
    val destCCI: String = "",
    val destTitular: String = "",
    val destCurrency: String = "",
    // Estado del intercambio
    val myVoucherUploaded: Boolean = false,
    val peerVoucherUploaded: Boolean = false,
    val myVoucherUrl: String? = null,
    val peerVoucherUrl: String? = null,
    val myConfirmed: Boolean = false,
    val peerConfirmed: Boolean = false,
    val yaCalificado: Boolean = false,
    // Nombre del par (para calificación)
    val peerName: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Estado calculado de la transacción (del servidor)
    val transactionStatus: String = "Pendiente"
)

class TransactionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private var transaccionId: Int = 0

    fun initialize(transactionId: Int) {
        if (transaccionId == transactionId && _uiState.value.transaction != null) return
        transaccionId = transactionId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch { loadAll() }
    }

    fun refresh() {
        viewModelScope.launch { loadAll() }
    }

    private suspend fun loadAll() {
        try {
            val authId = Supabase.client.auth.currentUserAwaitInit()?.id
            if (authId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado") }
                return
            }

            // 1. Usuario actual
            val myPerfil = Supabase.client.postgrest["usuarios"]
                .select { filter { eq("auth_id", authId) } }
                .decodeSingle<UserProfileModel>()
            val myUserId = myPerfil.usuarioId!!

            // 2. Transacción
            val tx = Supabase.client.postgrest["transacciones"]
                .select { filter { eq("transaccion_id", transaccionId) } }
                .decodeSingle<TransactionModel>()

            // 3. Oferta
            val offer = Supabase.client.postgrest["ofertas"]
                .select { filter { eq("oferta_id", tx.offerId) } }
                .decodeSingle<OfferModel>()

            val isSeller = myUserId == tx.usuarioVendedorId

            // 4. Método de pago destino (a quién le pago)
            val destMethodId = if (isSeller) {
                // El vendedor paga al comprador en USD → método del comprador
                tx.metodoPagoCompradorId
            } else {
                // El comprador paga al vendedor en PEN → método del vendedor (en la oferta)
                offer.metodoPagoId
            }

            var bName = ""; var accNum = ""; var cci = ""; var tit = ""; var curr = ""
            if (destMethodId != null) {
                try {
                    val pm = Supabase.client.postgrest["metodos_pago"]
                        .select { filter { eq("metodo_pago_id", destMethodId) } }
                        .decodeSingle<PaymentMethodModel>()
                    bName = pm.banco
                    accNum = pm.numeroCuenta
                    cci = if (pm.banco.uppercase().contains("BCP")) "002-${pm.numeroCuenta}-45" else "003-${pm.numeroCuenta}-12"
                    tit = pm.nombreTitular
                    curr = pm.tipoMoneda
                } catch (_: Exception) {}
            }

            // 5. Comprobantes
            val comprobantes = Supabase.client.postgrest["comprobantes"]
                .select { filter { eq("transaccion_id", transaccionId) } }
                .decodeList<ComprobanteModel>()

            val myVoucher = comprobantes.find { it.usuarioId == myUserId }
            val peerVoucher = comprobantes.find { it.usuarioId != myUserId }

            val myConfirmed = if (isSeller) tx.confirmadoVendedor else tx.confirmadoComprador
            val peerConfirmed = if (isSeller) tx.confirmadoComprador else tx.confirmadoVendedor

            // 6. Nombre del par
            val peerId = if (isSeller) tx.usuarioCompradorId else tx.usuarioVendedorId
            var peerName = "Contraparte"
            try {
                val peer = Supabase.client.postgrest["usuarios"]
                    .select { filter { eq("usuario_id", peerId) } }
                    .decodeSingle<UserProfileModel>()
                peerName = "${peer.nombres} ${peer.apellidos}"
            } catch (_: Exception) {}

            // 7. Calificaciones de la transacción
            var yaCalificado = false
            try {
                val calificaciones = Supabase.client.postgrest["calificaciones"]
                    .select { filter { eq("transaccion_id", transaccionId) } }
                    .decodeList<CalificacionModel>()
                yaCalificado = calificaciones.any { it.usuarioEvaluadorId == myUserId }
            } catch (_: Exception) {}

            _uiState.update {
                it.copy(
                    isLoading = false,
                    transaction = tx,
                    offer = offer,
                    isSeller = isSeller,
                    currentUserId = myUserId,
                    destBankName = bName,
                    destAccountNumber = accNum,
                    destCCI = cci,
                    destTitular = tit,
                    destCurrency = curr,
                    myVoucherUploaded = myVoucher != null,
                    peerVoucherUploaded = peerVoucher != null,
                    myVoucherUrl = myVoucher?.imagenUrl,
                    peerVoucherUrl = peerVoucher?.imagenUrl,
                    myConfirmed = myConfirmed,
                    peerConfirmed = peerConfirmed,
                    yaCalificado = yaCalificado,
                    peerName = peerName,
                    transactionStatus = tx.status
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar transacción: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Confirma que el usuario recibió el pago.
     * Actualiza `confirmado_comprador` o `confirmado_vendedor` en la BD.
     * Si ambos confirman, la transacción pasa a "Finalizado".
     */
    fun confirmarRecepcion() {
        val state = _uiState.value
        if (transaccionId == 0) return

        viewModelScope.launch {
            try {
                val campo = if (state.isSeller) "confirmado_vendedor" else "confirmado_comprador"
                val otherConfirmed = state.peerConfirmed
                
                // 1. Actualizar confirmación de esta parte (Map<String, Boolean>)
                Supabase.client.postgrest["transacciones"].update(
                    mapOf(campo to true)
                ) {
                    filter { eq("transaccion_id", transaccionId) }
                }

                // 2. Si la otra parte ya confirmó, cambiar estado a Finalizado (Map<String, String>)
                if (otherConfirmed) {
                    Supabase.client.postgrest["transacciones"].update(
                        mapOf("estado" to "Finalizado")
                    ) {
                        filter { eq("transaccion_id", transaccionId) }
                    }
                }

                if (otherConfirmed && state.offer != null) {
                    Supabase.client.postgrest["ofertas"].update(
                        mapOf("estado" to "Finalizada")
                    ) {
                        filter { eq("oferta_id", state.offer.offerId ?: 0) }
                    }
                }

                _uiState.update {
                    it.copy(
                        myConfirmed = true,
                        successMessage = "¡Recepción confirmada!"
                    )
                }
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al confirmar: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * Abre una disputa para esta transacción.
     * INSERT en `disputas` + UPDATE `transacciones` estado → "Disputa"
     */
    fun abrirDisputa(motivo: String = "") {
        val state = _uiState.value
        val myUserId = state.currentUserId ?: return

        viewModelScope.launch {
            try {
                val disputa = DisputeModel(
                    transaccionId = transaccionId,
                    usuarioReportadorId = myUserId,
                    estado = "Abierta"
                )
                Supabase.client.postgrest["disputas"].insert(disputa)

                Supabase.client.postgrest["transacciones"].update(
                    mapOf("estado" to "Disputa")
                ) {
                    filter { eq("transaccion_id", transaccionId) }
                }

                _uiState.update {
                    it.copy(
                        transactionStatus = "Disputa",
                        successMessage = "Disputa abierta. Un administrador revisará el caso."
                    )
                }
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al abrir disputa: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * Registra la calificación del usuario par.
     * INSERT en `calificaciones`.
     */
    fun calificar(puntaje: Int, comentario: String) {
        val state = _uiState.value
        val myUserId = state.currentUserId ?: return
        val tx = state.transaction ?: return

        viewModelScope.launch {
            try {
                val evaluadoId = if (state.isSeller) tx.usuarioCompradorId else tx.usuarioVendedorId
                val calificacion = CalificacionModel(
                    transaccionId = transaccionId,
                    usuarioEvaluadorId = myUserId,
                    usuarioEvaluadoId = evaluadoId,
                    puntaje = puntaje,
                    comentario = comentario.ifBlank { null }
                )
                Supabase.client.postgrest["calificaciones"].insert(calificacion)

                // Marcar la transacción como ya calificada
                Supabase.client.postgrest["transacciones"].update(
                    mapOf("ya_calificado" to true)
                ) {
                    filter { eq("transaccion_id", transaccionId) }
                }

                _uiState.update {
                    it.copy(
                        yaCalificado = true,
                        successMessage = "¡Gracias por tu calificación!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al calificar: ${e.localizedMessage}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
