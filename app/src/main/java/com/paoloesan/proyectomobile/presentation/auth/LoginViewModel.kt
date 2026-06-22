package com.paoloesan.proyectomobile.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(value: String) {
        _uiState.update { it.copy(correo = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun login(context: Context, rememberMe: Boolean) {
        val current = _uiState.value

        if (current.correo.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Complete los campos requeridos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                AuthRepository.login(context, current.correo, current.password, rememberMe)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                val message = e.message ?: ""
                val errorMsg = when {
                    message.contains("Invalid login credentials", ignoreCase = true) || 
                    message.contains("User not found", ignoreCase = true) -> {
                        "Correo o contraseña incorrectos"
                    }
                    message.contains("Email not confirmed", ignoreCase = true) -> {
                        "Por favor, confirme su correo electrónico antes de iniciar sesión."
                    }
                    message.isNotBlank() -> {
                        if (message.contains("URL:", ignoreCase = true)) {
                            // Limpiar excepción HTTP detallada de Supabase
                            val firstLine = message.lineSequence().firstOrNull()?.trim() ?: ""
                            if (firstLine.isNotBlank() && firstLine.length < 80) {
                                firstLine
                            } else {
                                "Error de conexión con el servidor. Intente de nuevo."
                            }
                        } else {
                            message
                        }
                    }
                    else -> {
                        "Error de conexión con el servidor"
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }
}
