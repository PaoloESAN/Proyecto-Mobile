package com.paoloesan.proyectomobile.presentation.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class BankAccount(
    val id: String = UUID.randomUUID().toString(),
    val banco: String,
    val numeroCuenta: String,
    val titular: String,
    val moneda: String
)

data class ProfileUiState(
    val nombres: String = "Freddy",
    val apellidos: String = "Delgado",
    val telefono: String = "987654321",
    val cuentas: List<BankAccount> = listOf(
        BankAccount(
            banco = "BCP",
            numeroCuenta = "1234567890",
            titular = "Freddy Delgado",
            moneda = "PEN"
        ),
        BankAccount(
            banco = "Yape",
            numeroCuenta = "9876543210",
            titular = "Freddy Delgado",
            moneda = "PEN"
        )
    ),
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onNombresChange(value: String) {
        val filtered = value.filter { it.isLetter() || it == ' ' }
        _uiState.update { it.copy(nombres = filtered, errorMessage = null) }
    }

    fun onApellidosChange(value: String) {
        val filtered = value.filter { it.isLetter() || it == ' ' }
        _uiState.update { it.copy(apellidos = filtered, errorMessage = null) }
    }

    fun onTelefonoChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(telefono = filtered, errorMessage = null) }
    }

    fun addCuenta(cuenta: BankAccount) {
        _uiState.update { it.copy(cuentas = it.cuentas + cuenta) }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun saveChanges() {
        val current = _uiState.value

        if (current.nombres.isBlank() || current.apellidos.isBlank() || current.telefono.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Complete todos los campos obligatorios")
            }
            return
        }

        if (current.telefono.length < 9) {
            _uiState.update {
                it.copy(errorMessage = "El teléfono debe tener al menos 9 dígitos")
            }
            return
        }

        _uiState.update {
            it.copy(isSuccess = true, errorMessage = null)
        }
    }
}
