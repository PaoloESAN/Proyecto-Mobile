package com.paoloesan.proyectomobile.presentation.p2p

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.model.MatchParams
import com.paoloesan.proyectomobile.data.model.OfertaMatch
import com.paoloesan.proyectomobile.data.model.OfferModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferWithBank(
    @SerialName("oferta_id") val ofertaId: Int,
    @SerialName("metodos_pago") val metodoPago: MetodoPagoBanco? = null
)

@Serializable
data class MetodoPagoBanco(
    val banco: String
)

data class MatchUiState(
    val isLoading: Boolean = false,
    val myActiveOffers: List<OfferModel> = emptyList(),
    val selectedOffer: OfferModel? = null,
    val matches: List<OfertaMatch> = emptyList(),
    val errorMessage: String? = null
)

class MatchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private var currentUserId: Int? = null

    init {
        loadMyActiveOffers()
    }

    fun loadMyActiveOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                if (authId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Usuario no autenticado"
                        )
                    }
                    return@launch
                }

                // 1. Obtener perfil del usuario
                val perfil = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter {
                            eq("auth_id", authId)
                        }
                    }
                    .decodeSingle<UserProfileModel>()

                currentUserId = perfil.usuarioId

                // 2. Obtener ofertas activas
                val miOfertas = Supabase.client.postgrest["ofertas"]
                    .select {
                        filter {
                            eq("usuario_creador_id", perfil.usuarioId!!)
                            eq("estado", "Activa")
                        }
                    }
                    .decodeList<OfferModel>()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        myActiveOffers = miOfertas,
                        selectedOffer = miOfertas.firstOrNull()
                    )
                }

                // Buscar matches para la primera oferta si existe
                miOfertas.firstOrNull()?.let {
                    buscarMatches(it)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar ofertas: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectOffer(offer: OfferModel) {
        _uiState.update { it.copy(selectedOffer = offer) }
        buscarMatches(offer)
    }

    private fun buscarMatches(offer: OfferModel) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val params = MatchParams(
                    usuarioId = userId,
                    monedaTengo = offer.monedaTengo,
                    monedaRecibo = offer.monedaRecibo,
                    montoTengo = offer.montoTengo,
                    montoRecibo = offer.montoRecibo,
                    tipoOperacion = offer.tipoOperacion
                )

                val result = Supabase.client.postgrest.rpc(
                    function = "obtener_matches",
                    parameters = params
                ).decodeList<OfertaMatch>()

                if (result.isNotEmpty()) {
                    val matchedOfferIds = result.map { it.ofertaId }
                    val creadorIds = result.map { it.usuarioCreadorId }.distinct()

                    // 1. Consultar nombres de creadores
                    val usuarios = Supabase.client.postgrest["usuarios"]
                        .select {
                            filter {
                                isIn("usuario_id", creadorIds)
                            }
                        }
                        .decodeList<UserProfileModel>()

                    // 2. Consultar bancos de métodos de pago asociados a las ofertas
                    val offerBanks = Supabase.client.postgrest["ofertas"]
                        .select(columns = Columns.raw("oferta_id, metodos_pago(banco)")) {
                            filter {
                                isIn("oferta_id", matchedOfferIds)
                            }
                        }
                        .decodeList<OfferWithBank>()

                    // 3. Asignar los valores a los matches
                    result.forEach { match ->
                        val u = usuarios.firstOrNull { it.usuarioId == match.usuarioCreadorId }
                        if (u != null) {
                            match.nombreCreador = "${u.nombres} ${u.apellidos}"
                        }

                        val ob = offerBanks.firstOrNull { it.ofertaId == match.ofertaId }
                        if (ob?.metodoPago != null) {
                            match.banco = ob.metodoPago.banco
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        matches = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al buscar coincidencias: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}
