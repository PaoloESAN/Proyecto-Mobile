package com.paoloesan.proyectomobile.presentation.p2p

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.model.OfferModel
import com.paoloesan.proyectomobile.data.model.PaymentMethodModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PublishUiState(
    val isLoading: Boolean = false,
    val paymentMethods: List<PaymentMethodModel> = emptyList(),
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class PublishOfferViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    private var currentUserId: Int? = null

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Obtener auth_id del usuario logueado esperando a que inicialice
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                if (authId == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado") }
                    return@launch
                }

                // 2. Obtener el perfil del usuario para conseguir el usuario_id (Int)
                val perfil = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter {
                            eq("auth_id", authId)
                        }
                    }
                    .decodeSingle<UserProfileModel>()

                currentUserId = perfil.usuarioId

                // 3. Consultar los métodos de pago registrados y activos del usuario
                val metodos = Supabase.client.postgrest["metodos_pago"]
                    .select {
                        filter {
                            eq("usuario_id", perfil.usuarioId!!)
                            eq("estado", "Activo")
                        }
                    }
                    .decodeList<PaymentMethodModel>()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        paymentMethods = metodos
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar métodos de pago: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun publishOffer(
        metodoPagoId: Int,
        tipoOperacion: String,
        monedaTengo: String,
        monedaRecibo: String,
        montoTengo: Double,
        montoRecibo: Double,
        tipoCambio: Double
    ) {
        val userId = currentUserId
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Usuario no cargado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val nuevaOferta = OfferModel(
                    usuarioCreadorId = userId,
                    metodoPagoId = metodoPagoId,
                    tipoOperacion = tipoOperacion,
                    monedaTengo = monedaTengo,
                    monedaRecibo = monedaRecibo,
                    montoTengo = montoTengo,
                    montoRecibo = montoRecibo,
                    price = tipoCambio,
                    estado = "Activa"
                )

                Supabase.client.postgrest["ofertas"].insert(nuevaOferta)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al publicar oferta: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
