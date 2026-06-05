package com.paoloesan.proyectomobile.presentation.p2p

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class P2POffer(
    val id: String,
    val username: String,
    val amount: Double,
    val currency: String,
    val rate: Double,
    val paymentMethod: String,
    val type: String, // "Compra" o "Venta"
    val minLimit: Double,
    val maxLimit: Double
)

data class MarketplaceFilters(
    val currency: String = "TODOS",
    val type: String = "TODOS",
    val amount: Double? = null,
    val paymentMethod: String = "TODOS"
)

class MarketplaceViewModel : ViewModel() {

    private val _offers = MutableStateFlow(
        listOf(
            P2POffer("1", "Carlos Perez", 150.0, "USD", 3.75, "BCP", "Compra", 50.0, 200.0),
            P2POffer("2", "Ana Gomez", 500.0, "PEN", 1.0, "Yape", "Venta", 10.0, 500.0),
            P2POffer("3", "Luis Rodriguez", 2500.0, "PEN", 1.0, "Interbank", "Compra", 100.0, 3000.0),
            P2POffer("4", "Maria Lopez", 300.0, "USD", 3.76, "BCP", "Venta", 100.0, 400.0),
            P2POffer("5", "Juan Castro", 100.0, "USD", 3.74, "Yape", "Compra", 20.0, 150.0),
            P2POffer("6", "Sofia Martinez", 1200.0, "PEN", 1.0, "Interbank", "Venta", 200.0, 1500.0),
            P2POffer("7", "Pedro Sanchez", 450.0, "USD", 3.77, "BCP", "Compra", 100.0, 500.0),
            P2POffer("8", "Lucia Diaz", 80.0, "USD", 3.73, "Yape", "Venta", 10.0, 100.0),
            P2POffer("9", "Jorge Silva", 3000.0, "PEN", 1.0, "Interbank", "Compra", 500.0, 5000.0),
            P2POffer("10", "Elena Ruiz", 200.0, "USD", 3.75, "BCP", "Venta", 50.0, 300.0)
        )
    )
    val offers: StateFlow<List<P2POffer>> = _offers

    private val _filters = MutableStateFlow(MarketplaceFilters())
    val filters: StateFlow<MarketplaceFilters> = _filters

    val filteredOffers: StateFlow<List<P2POffer>> = combine(_offers, _filters) { offersList, activeFilters ->
        offersList.filter { offer ->
            val matchesCurrency = activeFilters.currency == "TODOS" || offer.currency.equals(activeFilters.currency, ignoreCase = true)
            val matchesType = activeFilters.type == "TODOS" || offer.type.equals(activeFilters.type, ignoreCase = true)
            val matchesPayment = activeFilters.paymentMethod == "TODOS" || offer.paymentMethod.equals(activeFilters.paymentMethod, ignoreCase = true)
            val matchesAmount = activeFilters.amount == null || (activeFilters.amount >= offer.minLimit && activeFilters.amount <= offer.maxLimit)

            matchesCurrency && matchesType && matchesPayment && matchesAmount
        }
    }.stateIn(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun applyFilters(currency: String, type: String, amount: Double?, paymentMethod: String) {
        _filters.value = MarketplaceFilters(currency, type, amount, paymentMethod)
    }
}
