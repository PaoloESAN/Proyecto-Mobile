package com.paoloesan.proyectomobile.presentation.disputa

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DisputaViewModel : ViewModel() {

    private val _disputas = MutableStateFlow(disputasFalsas)
    val disputas: StateFlow<List<Disputa>> = _disputas.asStateFlow()

    fun resolverDisputa(id: Int) {
        _disputas.value = _disputas.value.filter { it.id != id }
    }

    fun getDisputaById(id: Int): Disputa? {
        return _disputas.value.find { it.id == id }
    }
}
