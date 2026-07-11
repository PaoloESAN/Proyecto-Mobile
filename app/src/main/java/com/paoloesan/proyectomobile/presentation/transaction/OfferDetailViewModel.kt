package com.paoloesan.proyectomobile.presentation.transaction

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

data class OfferDetailUiState(
    val isLoading: Boolean = true,
    val offer: OfferModel? = null,
    val vendorName: String = "",
    val vendorRating: Double = 5.0,
    val vendorPhotoUrl: String? = null,
    val myPaymentMethods: List<PaymentMethodModel> = emptyList(),
    val selectedMethodId: Int? = null,
    val amountInput: String = "",
    val errorMessage: String? = null,
    val isCreatingTransaction: Boolean = false,
    // Cuando la transacción se crea exitosamente, emitimos el ID aquí
    val createdTransactionId: Int? = null,
    val currentUserId: Int? = null,
    val esVerificado: Boolean = true
)

class OfferDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OfferDetailUiState())
    val uiState: StateFlow<OfferDetailUiState> = _uiState.asStateFlow()

    private var offerId: Int = 0

    fun initialize(offerId: Int) {
        if (this.offerId == offerId && _uiState.value.offer != null) return
        this.offerId = offerId

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                if (authId == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado") }
                    return@launch
                }

                // 1. Cargar el perfil del usuario actual
                val myPerfil = Supabase.client.postgrest["usuarios"]
                    .select { filter { eq("auth_id", authId) } }
                    .decodeSingle<UserProfileModel>()

                // 2. Cargar la oferta
                val offer = Supabase.client.postgrest["ofertas"]
                    .select { filter { eq("oferta_id", offerId) } }
                    .decodeSingle<OfferModel>()

                // 3. Cargar nombre, rating y foto del vendedor
                var vendorName = "Vendedor"
                var vendorRating = 5.0
                var vendorPhotoUrl: String? = null
                try {
                    val vendor = Supabase.client.postgrest["usuarios"]
                        .select { filter { eq("usuario_id", offer.usuarioCreadorId) } }
                        .decodeSingle<UserProfileModel>()
                    vendorName = "${vendor.nombres} ${vendor.apellidos}"
                    vendorRating = vendor.calificacion
                    vendorPhotoUrl = vendor.fotoPerfil
                } catch (_: Exception) {}

                // 4. Cargar métodos de pago del usuario actual
                val myMethods = Supabase.client.postgrest["metodos_pago"]
                    .select {
                        filter {
                            eq("usuario_id", myPerfil.usuarioId!!)
                            eq("estado", "Activo")
                        }
                    }
                    .decodeList<PaymentMethodModel>()

                val initialMethodId = myMethods.firstOrNull { it.tipoMoneda.equals(offer.monedaTengo, ignoreCase = true) }?.metodoPagoId

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        offer = offer,
                        vendorName = vendorName,
                        vendorRating = vendorRating,
                        vendorPhotoUrl = vendorPhotoUrl,
                        myPaymentMethods = myMethods,
                        selectedMethodId = initialMethodId,
                        currentUserId = myPerfil.usuarioId,
                        esVerificado = myPerfil.esVerificado
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar oferta: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amountInput = value, errorMessage = null) }
    }

    fun onSelectPaymentMethod(methodId: Int) {
        _uiState.update { it.copy(selectedMethodId = methodId) }
    }

    fun consumeCreatedTransaction() {
        _uiState.update { it.copy(createdTransactionId = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Crea la transacción en Supabase:
     * 1. INSERT en `transacciones` con estado "En Proceso"
     * 2. UPDATE `ofertas` estado → "En Proceso"
     */
    fun createTransaction() {
        val state = _uiState.value
        val offer = state.offer ?: return
        val myUserId = state.currentUserId ?: return
        val selectedMethodId = state.selectedMethodId ?: run {
            _uiState.update { it.copy(errorMessage = "Selecciona un método de pago") }
            return
        }

        if (!state.esVerificado) {
            _uiState.update { it.copy(errorMessage = "Tu cuenta no está verificada. Debes verificar tu identidad para iniciar transacciones.") }
            return
        }

        _uiState.update { it.copy(isCreatingTransaction = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Determinar vendedor/comprador según tipo de operación de la oferta
                val (compradorId, vendedorId) = if (offer.tipoOperacion == "Venta") {
                    myUserId to offer.usuarioCreadorId
                } else {
                    offer.usuarioCreadorId to myUserId
                }

                val nuevaTransaccion = TransactionModel(
                    offerId = offer.offerId!!,
                    usuarioCompradorId = compradorId,
                    usuarioVendedorId = vendedorId,
                    metodoPagoCompradorId = selectedMethodId,
                    amount = offer.montoTengo,
                    tipoCambioAplicado = offer.price,
                    status = "En Proceso"
                )

                val transaccionCreada = Supabase.client.postgrest["transacciones"]
                    .insert(nuevaTransaccion) { select() }
                    .decodeSingle<TransactionModel>()

                // Cambiar estado de la oferta a "En Proceso"
                Supabase.client.postgrest["ofertas"].update(
                    mapOf("estado" to "En Proceso")
                ) {
                    filter { eq("oferta_id", offer.offerId) }
                }

                _uiState.update {
                    it.copy(
                        isCreatingTransaction = false,
                        createdTransactionId = transaccionCreada.transactionId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingTransaction = false,
                        errorMessage = "Error al crear transacción: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}
