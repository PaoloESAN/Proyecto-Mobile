package com.paoloesan.proyectomobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app.theme.AppTheme
import com.paoloesan.proyectomobile.presentation.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme() {
                AppNav()
            }
        }
    }
}
