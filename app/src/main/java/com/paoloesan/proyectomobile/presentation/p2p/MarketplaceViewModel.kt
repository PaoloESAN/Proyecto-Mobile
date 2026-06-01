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
    val paymentMethod: String
)

class MarketplaceViewModel : ViewModel() {

    private val _offers = MutableStateFlow(
        listOf(
            P2POffer("1", "Carlos Perez", 150.0, "USD", 3.75, "BCP"),
            P2POffer("2", "Ana Gomez", 500.0, "PEN", 1.0, "Yape"),
            P2POffer("3", "Luis Rodriguez", 2500.0, "PEN", 1.0, "Interbank"),
            P2POffer("4", "Maria Lopez", 300.0, "USD", 3.76, "BCP"),
            P2POffer("5", "Juan Castro", 100.0, "USD", 3.74, "Yape"),
            P2POffer("6", "Sofia Martinez", 1200.0, "PEN", 1.0, "Interbank"),
            P2POffer("7", "Pedro Sanchez", 450.0, "USD", 3.77, "BCP"),
            P2POffer("8", "Lucia Diaz", 80.0, "USD", 3.73, "Yape"),
            P2POffer("9", "Jorge Silva", 3000.0, "PEN", 1.0, "Interbank"),
            P2POffer("10", "Elena Ruiz", 200.0, "USD", 3.75, "BCP")
        )
    )
    val offers: StateFlow<List<P2POffer>> = _offers

    val selectedCurrency = MutableStateFlow("TODOS")

    val filteredOffers: StateFlow<List<P2POffer>> = combine(_offers, selectedCurrency) { offersList, currency ->
        if (currency == "TODOS") {
            offersList
        } else {
            offersList.filter { it.currency.equals(currency, ignoreCase = true) }
        }
    }.stateIn(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCurrency(currency: String) {
        selectedCurrency.value = currency
    }
}
