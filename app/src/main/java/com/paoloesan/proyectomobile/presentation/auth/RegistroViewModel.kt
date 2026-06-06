package com.paoloesan.proyectomobile.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmarPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegistroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private val registeredEmails = setOf(
        "admin@test.com",
        "user@test.com",
        "paolo@test.com"
    )

    private val emailRegex = Patterns.EMAIL_ADDRESS

    fun onNombresChange(value: String) {
        _uiState.update { it.copy(nombres = value, errorMessage = null) }
    }

    fun onApellidosChange(value: String) {
        _uiState.update { it.copy(apellidos = value, errorMessage = null) }
    }

    fun onCorreoChange(value: String) {
        _uiState.update { it.copy(correo = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmarPasswordChange(value: String) {
        _uiState.update { it.copy(confirmarPassword = value, errorMessage = null) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun register() {
        val current = _uiState.value

        if (current.nombres.isBlank() ||
            current.apellidos.isBlank() ||
            current.correo.isBlank() ||
            current.password.isBlank() ||
            current.confirmarPassword.isBlank()
        ) {
            _uiState.update {
                it.copy(errorMessage = "Complete todos los campos obligatorios")
            }
            return
        }

        if (!emailRegex.matcher(current.correo).matches() || current.correo.contains(" ")) {
            _uiState.update {
                it.copy(errorMessage = "Ingrese un correo electrónico válido")
            }
            return
        }

        if (current.correo.lowercase() in registeredEmails.map { it.lowercase() }) {
            _uiState.update {
                it.copy(errorMessage = "El correo ya se encuentra registrado")
            }
            return
        }

        if (current.password.length < 8) {
            _uiState.update {
                it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres")
            }
            return
        }

        val hasLetter = current.password.any { it.isLetter() }
        val hasDigit = current.password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            _uiState.update {
                it.copy(errorMessage = "La contraseña debe contener letras y números")
            }
            return
        }

        if (current.password != current.confirmarPassword) {
            _uiState.update {
                it.copy(errorMessage = "Las contraseñas no coinciden")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            }
        }
    }
}
