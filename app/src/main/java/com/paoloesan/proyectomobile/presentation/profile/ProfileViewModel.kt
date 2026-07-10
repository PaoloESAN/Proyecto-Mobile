package com.paoloesan.proyectomobile.presentation.profile

import android.content.Context
import android.net.Uri
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
import io.github.jan.supabase.storage.storage
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
    val rol: String = "Usuario",
    val isVerified: Boolean = false,
    val calificacion: Double = 5.0,
    val fotoPerfil: String? = null,
    // URI local seleccionada, aún no subida (solo para preview en EditProfile)
    val pendingAvatarUri: Uri? = null,
    val cuentas: List<PaymentMethodModel> = emptyList(),
    val alertas: List<AlertaCambioModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
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
                            rol = profile.rol,
                            isVerified = profile.esVerificado,
                            calificacion = promedio,
                            fotoPerfil = profile.fotoPerfil,
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
     * Guarda la URI seleccionada en el estado para mostrar preview local.
     * NO sube nada a Supabase hasta que el usuario presione "Guardar cambios".
     */
    fun onAvatarSelected(uri: Uri) {
        _uiState.update { it.copy(pendingAvatarUri = uri) }
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

    fun deleteBankAccount(cuentaId: Int) {
        viewModelScope.launch {
            try {
                // Verificar si está vinculada a ofertas activas o en proceso
                val linkedOffers = Supabase.client.postgrest["ofertas"]
                    .select {
                        filter {
                            eq("metodo_pago_id", cuentaId)
                            isIn("estado", listOf("Activa", "En Proceso"))
                        }
                    }.decodeList<com.paoloesan.proyectomobile.data.model.OfferModel>()

                if (linkedOffers.isNotEmpty()) {
                    _uiState.update { it.copy(errorMessage = "No se puede eliminar la cuenta: tiene ofertas activas o en proceso vinculadas.") }
                    return@launch
                }

                // Soft-delete: desactivar cambiando estado a "Inactivo"
                Supabase.client.postgrest["metodos_pago"].update({
                    set("estado", "Inactivo")
                }) {
                    filter {
                        eq("metodo_pago_id", cuentaId)
                    }
                }
                loadProfile()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al eliminar cuenta bancaria", e)
                _uiState.update { it.copy(errorMessage = "Error de red al desactivar cuenta bancaria: ${e.message}") }
            }
        }
    }

    fun editarBankAccount(cuentaId: Int, banco: String, numeroCuenta: String, titular: String, moneda: String) {
        viewModelScope.launch {
            try {
                // Verificar si está vinculada a ofertas activas o en proceso
                val linkedOffers = Supabase.client.postgrest["ofertas"]
                    .select {
                        filter {
                            eq("metodo_pago_id", cuentaId)
                            isIn("estado", listOf("Activa", "En Proceso"))
                        }
                    }.decodeList<com.paoloesan.proyectomobile.data.model.OfferModel>()

                if (linkedOffers.isNotEmpty()) {
                    _uiState.update { it.copy(errorMessage = "No se puede editar la cuenta: tiene ofertas activas o en proceso vinculadas.") }
                    return@launch
                }

                Supabase.client.postgrest["metodos_pago"].update({
                    set("banco", banco)
                    set("numero_cuenta", numeroCuenta)
                    set("nombre_titular", titular)
                    set("tipo_moneda", moneda)
                }) {
                    filter {
                        eq("metodo_pago_id", cuentaId)
                    }
                }
                loadProfile()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al editar cuenta bancaria", e)
                _uiState.update { it.copy(errorMessage = "Error al editar cuenta: ${e.message}") }
            }
        }
    }

    fun editarAlerta(alertaId: Int, moneda: String, tipoCambio: Double) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["alertas_cambio"].update({
                    set("moneda", moneda)
                    set("tipo_cambio_deseado", tipoCambio)
                }) {
                    filter {
                        eq("alerta_id", alertaId)
                    }
                }
                loadProfile()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al editar alerta de cambio", e)
                _uiState.update { it.copy(errorMessage = "Error al editar alerta: ${e.message}") }
            }
        }
    }

    /**
     * ACTUALIZAR: Si hay una imagen pendiente, la sube primero al bucket `avatars`
     * y actualiza `foto_perfil_url`. Luego guarda nombres y apellidos.
     * Al terminar, dispara isSuccess para que la UI navegue de regreso.
     */
    fun saveChanges(context: Context) {
        val current = _uiState.value
        if (current.nombres.isBlank() || current.apellidos.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Complete todos los campos obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val userId = current.usuarioId ?: throw Exception("Usuario no identificado")
                val userEmail = current.correo

                // 1. Subir avatar si hay uno pendiente
                if (current.pendingAvatarUri != null) {
                    val bytes = context.contentResolver
                        .openInputStream(current.pendingAvatarUri)
                        ?.use { it.readBytes() }
                        ?: throw Exception("No se pudo leer la imagen seleccionada")

                    val storagePath = "$userId/avatar.jpg"
                    Supabase.client.storage["avatars"].upload(path = storagePath, data = bytes) {
                        upsert = true
                    }
                    val publicUrl = Supabase.client.storage["avatars"].publicUrl(storagePath)

                    Supabase.client.postgrest["usuarios"].update({
                        set("foto_perfil_url", publicUrl)
                    }) {
                        filter { eq("usuario_id", userId) }
                    }
                }

                // 2. Actualizar nombres y apellidos
                Supabase.client.postgrest["usuarios"].update({
                    set("nombres", current.nombres)
                    set("apellidos", current.apellidos)
                }) {
                    filter { eq("correo", userEmail) }
                }

                // 3. Sincronizar localmente
                SessionManager.saveProfileInfo(context, current.nombres, current.apellidos)

                _uiState.update {
                    it.copy(isSaving = false, isSuccess = true, pendingAvatarUri = null, errorMessage = null)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al guardar perfil", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error al guardar cambios: ${e.message}") }
            }
        }
    }

    fun crearAlerta(moneda: String, tipoCambio: Double) {
        viewModelScope.launch {
            val userId = _uiState.value.usuarioId ?: return@launch
            try {
                val payload = mapOf(
                    "usuario_id" to userId,
                    "moneda" to moneda,
                    "tipo_cambio_deseado" to tipoCambio,
                    "estado" to "Activa"
                )
                Supabase.client.postgrest["alertas_cambio"].insert(payload)
                loadProfile()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al crear alerta", e)
                _uiState.update { it.copy(errorMessage = "Error al crear alerta: ${e.message}") }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
