package com.paoloesan.proyectomobile.presentation.profile

import android.content.Context
import com.paoloesan.proyectomobile.data.local.SessionManager
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
    val isVerified: Boolean = false,
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

    fun loadProfile(context: Context) {
        val isVerified = SessionManager.isVerified(context)
        val nombres = SessionManager.getNombres(context)
        val apellidos = SessionManager.getApellidos(context)
        _uiState.update {
            it.copy(
                isVerified = isVerified,
                nombres = nombres,
                apellidos = apellidos
            )
        }
    }

    fun onNombresChange(value: String) {
        val filtered = value.filter { it.isLetter() || it == ' ' }
        _uiState.update { it.copy(nombres = filtered, errorMessage = null) }
    }

    fun onApellidosChange(value: String) {
        val filtered = value.filter { it.isLetter() || it == ' ' }
        _uiState.update { it.copy(apellidos = filtered, errorMessage = null) }
    }

    fun addCuenta(cuenta: BankAccount) {
        _uiState.update { it.copy(cuentas = it.cuentas + cuenta) }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun saveChanges(context: Context) {
        val current = _uiState.value
        if (current.nombres.isBlank() || current.apellidos.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Complete todos los campos obligatorios")
            }
            return
        }

        SessionManager.saveProfileInfo(context, current.nombres, current.apellidos)

        _uiState.update {
            it.copy(isSuccess = true, errorMessage = null)
        }
    }
}
