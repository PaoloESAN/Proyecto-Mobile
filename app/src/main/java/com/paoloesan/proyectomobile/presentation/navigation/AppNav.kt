package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paoloesan.proyectomobile.presentation.debug.DebugScreen

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
    // Para añadir una pantalla nueva, añadelo aqui
    /*
    object Home : Destination(
        route = "home",
        title = "Pantalla de home",
        content = { /* Aquí llamarás a tu HomeScreen() */ }
    )*/

}

//Luego agregalo a la lista
val appDestinations = listOf(
    Destination.Debug,
    //Destination.Home
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