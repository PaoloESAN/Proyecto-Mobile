package com.paoloesan.proyectomobile.presentation.p2p

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.OfferModel
import com.paoloesan.proyectomobile.data.model.PaymentMethodModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class P2POffer(
    val id: String,
    val username: String,
    val avatarUrl: String? = null,
    val type: String, // "Compra" o "Venta"
    val monedaTengo: String,
    val monedaRecibo: String,
    val montoTengo: Double,
    val montoRecibo: Double,
    val rate: Double,
    val paymentMethod: String,
    val calificacion: Double = 5.0
)

data class MarketplaceFilters(
    val currency: String = "TODOS",
    val type: String = "TODOS",
    val amount: Double? = null,
    val paymentMethod: String = "TODOS",
    val minRating: Double? = null
)

class MarketplaceViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isNetworkError = MutableStateFlow(false)
    val isNetworkError: StateFlow<Boolean> = _isNetworkError.asStateFlow()

    private val _offers = MutableStateFlow<List<P2POffer>>(emptyList())
    val offers: StateFlow<List<P2POffer>> = _offers

    private val _filters = MutableStateFlow(MarketplaceFilters())
    val filters: StateFlow<MarketplaceFilters> = _filters

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _isNetworkError.value = false
            try {
                // 1. Obtener el perfil del usuario actual (si está logueado) para no mostrar sus propias ofertas
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                val currentUserId = if (authId != null) {
                    try {
                        val perfil = Supabase.client.postgrest["usuarios"]
                            .select { filter { eq("auth_id", authId) } }
                            .decodeSingle<UserProfileModel>()
                        perfil.usuarioId
                    } catch (e: Exception) {
                        null
                    }
                } else null

                // 2. Consultar las ofertas en estado "Activa" en Supabase
                val activeOffers = Supabase.client.postgrest["ofertas"]
                    .select {
                        filter {
                            eq("estado", "Activa")
                            if (currentUserId != null) {
                                neq("usuario_creador_id", currentUserId)
                            }
                        }
                    }
                    .decodeList<OfferModel>()
                    .sortedByDescending { it.offerId ?: 0 }

                // 3. Obtener nombres de creadores y nombres de banco de métodos de pago
                val creatorIds = activeOffers.map { it.usuarioCreadorId }.distinct()
                val paymentMethodIds = activeOffers.map { it.metodoPagoId }.distinct()

                // Mapear a (nombre, avatarUrl) por usuario_id
                data class UserInfo(val name: String, val avatarUrl: String?, val calificacion: Double)
                val userProfiles = if (creatorIds.isNotEmpty()) {
                    Supabase.client.postgrest["usuarios"]
                        .select { filter { isIn("usuario_id", creatorIds) } }
                        .decodeList<UserProfileModel>()
                        .associate { (it.usuarioId ?: -1) to UserInfo("${it.nombres} ${it.apellidos}", it.fotoPerfil, it.calificacion) }
                } else emptyMap()

                val paymentMethods = if (paymentMethodIds.isNotEmpty()) {
                    Supabase.client.postgrest["metodos_pago"]
                        .select { filter { isIn("metodo_pago_id", paymentMethodIds) } }
                        .decodeList<PaymentMethodModel>()
                        .associateBy { it.metodoPagoId }
                } else emptyMap()

                // 4. Mapear a la lista de ofertas UI
                val p2pOffers = activeOffers.map { offer ->
                    val creatorInfo = userProfiles[offer.usuarioCreadorId]
                    val creatorName = creatorInfo?.name ?: "Usuario ${offer.usuarioCreadorId}"
                    val paymentMethodLabel = paymentMethods[offer.metodoPagoId]?.banco ?: "Cuenta"
                    P2POffer(
                        id = offer.offerId?.toString() ?: "",
                        username = creatorName,
                        avatarUrl = creatorInfo?.avatarUrl,
                        type = offer.tipoOperacion,
                        monedaTengo = offer.monedaTengo,
                        monedaRecibo = offer.monedaRecibo,
                        montoTengo = offer.montoTengo,
                        montoRecibo = offer.montoRecibo,
                        rate = offer.price,
                        paymentMethod = paymentMethodLabel,
                        calificacion = creatorInfo?.calificacion ?: 5.0
                    )
                }

                _offers.value = p2pOffers
            } catch (e: Exception) {
                _offers.value = emptyList()
                _isNetworkError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    val filteredOffers: StateFlow<List<P2POffer>> = combine(_offers, _filters) { offersList, activeFilters ->
        offersList.filter { offer ->
            val matchesCurrency = activeFilters.currency == "TODOS" ||
                    offer.monedaTengo.equals(activeFilters.currency, ignoreCase = true) ||
                    offer.monedaRecibo.equals(activeFilters.currency, ignoreCase = true)

            val matchesType = activeFilters.type == "TODOS" ||
                    offer.type.equals(activeFilters.type, ignoreCase = true)

            val matchesPayment = activeFilters.paymentMethod == "TODOS" ||
                    offer.paymentMethod.contains(activeFilters.paymentMethod, ignoreCase = true)

            val matchesAmount = activeFilters.amount == null ||
                    offer.montoTengo >= activeFilters.amount

            val matchesRating = activeFilters.minRating == null ||
                    offer.calificacion >= activeFilters.minRating

            matchesCurrency && matchesType && matchesPayment && matchesAmount && matchesRating
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun applyFilters(currency: String, type: String, amount: Double?, paymentMethod: String, minRating: Double?) {
        _filters.value = MarketplaceFilters(currency, type, amount, paymentMethod, minRating)
    }
}
