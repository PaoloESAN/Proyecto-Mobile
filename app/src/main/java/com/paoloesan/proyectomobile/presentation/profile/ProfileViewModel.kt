package com.paoloesan.proyectomobile.presentation.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.data.model.AlertaCambioModel
import com.paoloesan.proyectomobile.data.model.CalificacionModel
import com.paoloesan.proyectomobile.data.model.PaymentMethodModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val usuarioId: Int? = null,
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val isVerified: Boolean = false,
    val calificacion: Double = 5.0,
    val cuentas: List<PaymentMethodModel> = emptyList(),
    val alertas: List<AlertaCambioModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * CONSULTAR: Obtiene el promedio de reputación y las cuentas bancarias activas.
     * Equivale al loadAllData solicitado.
     */
    fun loadProfile(context: Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val authUser = Supabase.client.auth.currentUserAwaitInit()
                if (authUser != null) {
                    // 1. Obtener datos del perfil desde la tabla 'usuarios'
                    val profile = Supabase.client.postgrest["usuarios"]
                        .select {
                            filter {
                                eq("auth_id", authUser.id)
                            }
                        }.decodeSingle<UserProfileModel>()

                    val userId = profile.usuarioId!!

                    // 2. REPUTACIÓN: Obtener promedio de la tabla calificaciones
                    val calificaciones = Supabase.client.postgrest["calificaciones"]
                        .select {
                            filter {
                                eq("usuario_evaluado_id", userId)
                            }
                        }.decodeList<CalificacionModel>()
                    
                    val promedio = if (calificaciones.isNotEmpty()) {
                        calificaciones.map { it.puntaje }.average()
                    } else {
                        profile.calificacion
                    }

                    // 3. CUENTAS BANCARIAS: Listar metodos_pago activos
                    val cuentas = Supabase.client.postgrest["metodos_pago"]
                        .select {
                            filter {
                                eq("usuario_id", userId)
                                eq("estado", "Activo")
                            }
                        }.decodeList<PaymentMethodModel>()

                    // 4. ALERTAS: Consultar alertas de tipo de cambio
                    val alertas = Supabase.client.postgrest["alertas_cambio"]
                        .select {
                            filter {
                                eq("usuario_id", userId)
                            }
                        }.decodeList<AlertaCambioModel>()

                    _uiState.update {
                        it.copy(
                            usuarioId = userId,
                            nombres = profile.nombres,
                            apellidos = profile.apellidos,
                            correo = profile.correo,
                            isVerified = profile.esVerificado,
                            calificacion = promedio,
                            cuentas = cuentas,
                            alertas = alertas,
                            isLoading = false
                        )
                    }

                    // Sincronizar con SessionManager si se proporciona el contexto
                    context?.let {
                        SessionManager.saveProfileInfo(it, profile.nombres, profile.apellidos)
                        SessionManager.saveVerified(it, profile.esVerificado)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión no iniciada") }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile data", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar datos del perfil") }
            }
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

    /**
     * INSERTAR: Tras insertar en metodos_pago, llama a loadProfile (loadAllData) 
     * para que la lista sea reactiva.
     */
    fun addMetodoPago(banco: String, numero: String, titular: String, moneda: String) {
        viewModelScope.launch {
            val userId = _uiState.value.usuarioId ?: return@launch
            try {
                val nuevaCuenta = PaymentMethodModel(
                    usuarioId = userId,
                    banco = banco,
                    numeroCuenta = numero,
                    nombreTitular = titular,
                    tipoMoneda = moneda,
                    estado = "Activo"
                )
                Supabase.client.postgrest["metodos_pago"].insert(nuevaCuenta)
                
                // Refresco automático de la UI
                loadProfile() 
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al insertar cuenta", e)
                _uiState.update { it.copy(errorMessage = "Error de red al registrar cuenta: ${e.message}") }
            }
        }
    }

    /**
     * ELIMINAR: Borra una alerta y recarga la lista automáticamente.
     */
    fun deleteAlerta(alertaId: Int) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["alertas_cambio"].delete {
                    filter {
                        eq("alerta_id", alertaId)
                    }
                }
                // Refresco automático de la UI
                loadProfile()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al eliminar alerta", e)
                _uiState.update { it.copy(errorMessage = "Error de red al eliminar alerta") }
            }
        }
    }

    /**
     * ACTUALIZAR: Guarda cambios del perfil filtrando por correo.
     * Dispara isSuccess para eventos reactivos en la UI.
     */
    fun saveChanges(context: Context) {
        val current = _uiState.value
        if (current.nombres.isBlank() || current.apellidos.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Complete todos los campos obligatorios")
            }
            return
        }

        viewModelScope.launch {
            try {
                val userEmail = current.correo
                
                // Actualizar en Supabase filtrando por correo
                Supabase.client.postgrest["usuarios"].update({
                    set("nombres", current.nombres)
                    set("apellidos", current.apellidos)
                }) {
                    filter {
                        eq("correo", userEmail)
                    }
                }

                // Sincronizar localmente en SessionManager
                SessionManager.saveProfileInfo(context, current.nombres, current.apellidos)

                _uiState.update {
                    it.copy(isSuccess = true, errorMessage = null)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al guardar perfil", e)
                _uiState.update { it.copy(errorMessage = "Error de red al guardar cambios: ${e.message}") }
            }
        }
    }

    fun crearAlerta(moneda: String, tipoCambio: Double) {
        viewModelScope.launch {
            val userId = _uiState.value.usuarioId ?: return@launch
            try {
                val nuevaAlerta = AlertaCambioModel(
                    usuarioId = userId,
                    moneda = moneda,
                    tipoCambioDeseado = tipoCambio
                )
                Supabase.client.postgrest["alertas_cambio"].insert(nuevaAlerta)
                loadProfile()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al crear alerta: ${e.message}") }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
