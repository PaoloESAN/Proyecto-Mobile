package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paoloesan.proyectomobile.presentation.debug.DebugScreen
import com.paoloesan.proyectomobile.presentation.verification.IdentityVerificationScreen
import com.paoloesan.proyectomobile.presentation.auth.RecoverPasswordScreen
import com.paoloesan.proyectomobile.presentation.auth.ResetPasswordScreen

sealed class Destination(
    val route: String,
    val title: String,
    val content: @Composable (NavController) -> Unit
) {
    object Debug : Destination(
        route = "debug",
        title = "Pantalla de Debug",
        content = { navController -> DebugScreen(navController) }
    )
    
    object IdentityVerification : Destination(
        route = "identity_verification",
        title = "Verificación de DNI",
        content = { navController -> IdentityVerificationScreen(navController) }
    object RecoverPassword : Destination(
        route = "recover_password",
        title = "Recuperar Contraseña",
        content = { navController -> RecoverPasswordScreen(navController) }
    )

    object ResetPassword : Destination(
        route = "reset_password",
        title = "Restablecer Contraseña",
        content = { navController -> ResetPasswordScreen(navController) }
    )
}

//Luego agregalo a la lista
val appDestinations = listOf(
    Destination.Debug,
    Destination.IdentityVerification,
    Destination.RecoverPassword,
    Destination.ResetPassword,
)

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destination.Debug.route
    ) {
        appDestinations.forEach { destination ->
            composable(destination.route) {
                destination.content(navController)
            }
        }
    }
}