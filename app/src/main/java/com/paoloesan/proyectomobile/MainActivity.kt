package com.paoloesan.proyectomobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paoloesan.proyectomobile.presentation.navigation.AppNav
import com.paoloesan.proyectomobile.ui.theme.ProyectoMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoMobileTheme {
                AppNav()
            }
        }
    }
}
