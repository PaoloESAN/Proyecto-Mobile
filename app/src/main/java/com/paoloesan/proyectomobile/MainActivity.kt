package com.paoloesan.proyectomobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.app.theme.AppTheme
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.presentation.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
}

