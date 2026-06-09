package com.paoloesan.proyectomobile.presentation.admin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val correo: String,
    val estado: String = "Activo",
    val fechaRegistro: String,
    val bloqueosAnteriores: Int = 0
)

data class AdminUsersUiState(
    val usuarios: List<User> = listOf(
        User(
            nombre = "Juan Pérez",
            correo = "juan.perez@test.com",
            fechaRegistro = "15/01/2024",
            bloqueosAnteriores = 1
        ),
        User(
            nombre = "María López",
            correo = "maria.lopez@test.com",
            fechaRegistro = "20/02/2024",
            bloqueosAnteriores = 0
        ),
        User(
            nombre = "Carlos Ruiz",
            correo = "carlos.ruiz@test.com",
            fechaRegistro = "10/03/2024",
            bloqueosAnteriores = 2
        ),
        User(
            nombre = "Ana Gómez",
            correo = "ana.gomez@test.com",
            fechaRegistro = "05/04/2024",
            bloqueosAnteriores = 0
        ),
        User(
            nombre = "Luis Torres",
            correo = "luis.torres@test.com",
            fechaRegistro = "18/05/2024",
            bloqueosAnteriores = 3
        )
    ),
    val transientMessage: String? = null
)

class AdminUsersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    fun activateUser(userId: String) {
        _uiState.update { state ->
            state.copy(
                usuarios = state.usuarios.map { user ->
                    if (user.id == userId) user.copy(estado = "Activo") else user
                },
                transientMessage = "Usuario activado correctamente"
            )
        }
    }

    fun blockUser(userId: String, duracion: String = "indefinidamente") {
        _uiState.update { state ->
            state.copy(
                usuarios = state.usuarios.map { user ->
                    if (user.id == userId) {
                        user.copy(
                            estado = "Bloqueado",
                            bloqueosAnteriores = user.bloqueosAnteriores + 1
                        )
                    } else {
                        user
                    }
                },
                transientMessage = "Usuario bloqueado por $duracion"
            )
        }
    }

    fun setMessage(message: String) {
        _uiState.update { it.copy(transientMessage = message) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }
}
