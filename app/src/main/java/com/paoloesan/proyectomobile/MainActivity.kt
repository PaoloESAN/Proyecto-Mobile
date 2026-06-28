package com.paoloesan.proyectomobile

import android.content.Intent
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.lifecycleScope
import com.example.app.theme.AppTheme
import com.paoloesan.proyectomobile.data.AuthEvent
import com.paoloesan.proyectomobile.data.AuthEventChannel
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.data.local.MyFirebaseMessagingService
import com.paoloesan.proyectomobile.presentation.navigation.AppNav
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            obtenerYSincronizarToken()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.initSession(this)
        enableEdgeToEdge()
        
        handleIntent(intent)
        pedirPermisoNotificaciones()

        setContent {
            SessionManager.initTheme(this)
            val themeMode = SessionManager.themeState.value
            val isDark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            AppTheme(isDark = isDark) {
                AppNav()
            }
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                obtenerYSincronizarToken()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            obtenerYSincronizarToken()
        }
    }

    private fun obtenerYSincronizarToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    MyFirebaseMessagingService.registrarTokenEnSupabase(token)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            lifecycleScope.launch {
                try {
                    // Pasar el deep link a Supabase Auth para procesar la sesión
                    Supabase.client.handleDeeplinks(intent)
                    
                    // Si el deep link contiene la redirección de recuperación de contraseña,
                    // enviamos el evento para navegar a la pantalla de restablecimiento.
                    val isRecovery = uri.toString().contains("type=recovery") || uri.host == "recovery"
                    if (isRecovery) {
                        AuthEventChannel.sendEvent(AuthEvent.NavigateToResetPassword)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Manejar extras de la notificación push
        val transaccionId = intent?.getStringExtra("transaccion_id")?.toIntOrNull()
        val type = intent?.getStringExtra("type")
        if (transaccionId != null) {
            if (type == "chat") {
                AuthEventChannel.sendEvent(AuthEvent.NavigateToChat(transaccionId))
            } else {
                AuthEventChannel.sendEvent(AuthEvent.NavigateToTransactionStatus(transaccionId))
            }
        }
    }
}

