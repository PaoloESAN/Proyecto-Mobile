package com.paoloesan.proyectomobile.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class User(
    val id: String,
    val nombre: String,
    val correo: String,
    val estado: String = "Activo",
    val fechaRegistro: String,
    val bloqueosAnteriores: Int = 0
)

data class AdminUsersUiState(
    val usuarios: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val transientMessage: String? = null
)

class AdminUsersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val usuarios = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter {
                            neq("rol", "Administrador")
                        }
                    }
                    .decodeList<UserProfileModel>()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        usuarios = usuarios.map { perfil ->
                            User(
                                id = perfil.usuarioId.toString(),
                                nombre = "${perfil.nombres} ${perfil.apellidos}",
                                correo = perfil.correo,
                                estado = perfil.estado,
                                fechaRegistro = perfil.fechaRegistro ?: "Sin fecha",
                                bloqueosAnteriores = perfil.bloqueosAnteriores
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transientMessage = "Error al cargar usuarios: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun activateUser(userId: String) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["usuarios"]
                    .update({
                        set("estado", "Activo")
                    }) {
                        filter {
                            eq("usuario_id", userId.toInt())
                        }
                    }

                _uiState.update { state ->
                    state.copy(
                        usuarios = state.usuarios.map { user ->
                            if (user.id == userId) user.copy(estado = "Activo") else user
                        },
                        transientMessage = "Usuario activado correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(transientMessage = "Error al activar usuario: ${e.localizedMessage}")
                }
            }
        }
    }

    fun blockUser(userId: String, duracion: String = "indefinidamente") {
        viewModelScope.launch {
            try {
                val user = _uiState.value.usuarios.firstOrNull { it.id == userId }
                val bloqueosAnteriores = user?.bloqueosAnteriores ?: 0

                val now = java.time.Instant.now()
                val bloqueadoHastaInstant = when (duracion) {
                    "1 hora" -> now.plus(1, java.time.temporal.ChronoUnit.HOURS)
                    "24 horas" -> now.plus(24, java.time.temporal.ChronoUnit.HOURS)
                    "7 días" -> now.plus(7, java.time.temporal.ChronoUnit.DAYS)
                    "1 mes" -> now.plus(30, java.time.temporal.ChronoUnit.DAYS)
                    "3 meses" -> now.plus(90, java.time.temporal.ChronoUnit.DAYS)
                    else -> null
                }
                val bloqueadoHastaString = bloqueadoHastaInstant?.toString()

                Supabase.client.postgrest["usuarios"]
                    .update({
                        set("estado", "Bloqueado")
                        set("bloqueos_anteriores", bloqueosAnteriores + 1)
                        set("bloqueado_hasta", bloqueadoHastaString)
                    }) {
                        filter {
                            eq("usuario_id", userId.toInt())
                        }
                    }

                _uiState.update { state ->
                    state.copy(
                        usuarios = state.usuarios.map { u ->
                            if (u.id == userId) {
                                u.copy(
                                    estado = "Bloqueado",
                                    bloqueosAnteriores = u.bloqueosAnteriores + 1
                                )
                            } else {
                                u
                            }
                        },
                        transientMessage = "Usuario bloqueado por $duracion"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(transientMessage = "Error al bloquear usuario: ${e.localizedMessage}")
                }
            }
        }
    }

    fun setMessage(message: String) {
        _uiState.update { it.copy(transientMessage = message) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }
}
