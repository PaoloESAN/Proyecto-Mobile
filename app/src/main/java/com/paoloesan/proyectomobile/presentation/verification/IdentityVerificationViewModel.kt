package com.paoloesan.proyectomobile.presentation.verification

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdentityVerificationUiState(
    val selectedFrontImageUri: Uri? = null,
    val selectedBackImageUri: Uri? = null,
    val isUploading: Boolean = false,
    val showSuccess: Boolean = false,
    val errorMessage: String? = null
)

class IdentityVerificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(IdentityVerificationUiState())
    val uiState: StateFlow<IdentityVerificationUiState> = _uiState.asStateFlow()

    fun onFrontImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedFrontImageUri = uri, errorMessage = null) }
    }

    fun onBackImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedBackImageUri = uri, errorMessage = null) }
    }

    fun submitVerification(context: Context) {
        val current = _uiState.value
        if (current.selectedFrontImageUri == null || current.selectedBackImageUri == null) {
            _uiState.update { it.copy(errorMessage = "Debe adjuntar ambas imagenes del documento") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            try {
                val authUser = Supabase.client.auth.currentUserAwaitInit()
                    ?: throw Exception("Sesion no iniciada")

                val profile = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter { eq("auth_id", authUser.id) }
                    }.decodeSingle<UserProfileModel>()

                val userId = profile.usuarioId ?: throw Exception("Usuario no identificado")
                val userEmail = profile.correo

                val frontBytes = context.contentResolver
                    .openInputStream(current.selectedFrontImageUri)
                    ?.use { it.readBytes() }
                    ?: throw Exception("No se pudo leer la imagen frontal")

                val backBytes = context.contentResolver
                    .openInputStream(current.selectedBackImageUri)
                    ?.use { it.readBytes() }
                    ?: throw Exception("No se pudo leer la imagen posterior")

                val bucket = Supabase.client.storage["identities"]
                val frontPath = "frontal_$userId.jpg"
                val backPath = "posterior_$userId.jpg"

                bucket.upload(path = frontPath, data = frontBytes) { upsert = true }
                bucket.upload(path = backPath, data = backBytes) { upsert = true }

                val frontalUrl = bucket.publicUrl(frontPath)
                val posteriorUrl = bucket.publicUrl(backPath)

                Supabase.client.postgrest["usuarios"].update({
                    set("dni_frontal_url", frontalUrl)
                    set("dni_posterior_url", posteriorUrl)
                }) {
                    filter { eq("correo", userEmail) }
                }

                SessionManager.saveVerified(context, false)

                _uiState.update {
                    it.copy(
                        isUploading = false,
                        showSuccess = true,
                        selectedFrontImageUri = null,
                        selectedBackImageUri = null
                    )
                }
            } catch (e: Exception) {
                Log.e("IdentityVerificationVM", "Error al subir documentos", e)
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = "Error al subir documentos: ${e.message}"
                    )
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
