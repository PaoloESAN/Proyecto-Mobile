package com.paoloesan.proyectomobile

import android.content.Intent
import android.os.Bundle
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
import com.paoloesan.proyectomobile.presentation.navigation.AppNav
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.initSession(this)
        enableEdgeToEdge()
        
        handleIntent(intent)

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
    }
}

