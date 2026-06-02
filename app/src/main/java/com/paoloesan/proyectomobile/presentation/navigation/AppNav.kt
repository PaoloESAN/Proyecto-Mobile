package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paoloesan.proyectomobile.presentation.debug.DebugScreen
import com.paoloesan.proyectomobile.presentation.p2p.MarketplaceScreen
import com.paoloesan.proyectomobile.presentation.p2p.PublishOfferScreen
import com.paoloesan.proyectomobile.presentation.verification.IdentityVerificationScreen
import com.paoloesan.proyectomobile.presentation.auth.RecoverPasswordScreen
import com.paoloesan.proyectomobile.presentation.auth.ResetPasswordScreen
import com.paoloesan.proyectomobile.presentation.p2p.MatchScreen
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import com.paoloesan.proyectomobile.presentation.p2p.MyOffersScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Transacción") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pantalla de Detalle de Transacción (Demo)",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

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

    object Marketplace : Destination(
        route = "marketplace",
        title = "Mercado P2P",
        content = { navController -> MarketplaceScreen(navController) }
    )

    object TransactionDetail : Destination(
        route = "transaction_detail",
        title = "Detalle de Transacción",
        content = { navController -> TransactionDetailScreen(navController) }
    )

    object PublishOffer : Destination(
        route = "publish_offer",
        title = "Publicar Oferta",
        content = { navController -> PublishOfferScreen(navController) }
    )

    object IdentityVerification : Destination(
        route = "identity_verification",
        title = "Verificación de DNI",
        content = { navController -> IdentityVerificationScreen(navController) }
    )

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
    object MyOffers : Destination(
        route = "my_offers",
        title = "Mis Ofertas",
        content = { navController -> MyOffersScreen(navController) }
    )
    object Matches : Destination(
        route = "matches",
        title = "Coincidencias Automáticas",
        content = { navController -> MatchScreen(navController) }
    )
}

val appDestinations = listOf(
    Destination.Debug,
    Destination.Marketplace,
    Destination.TransactionDetail,
    Destination.PublishOffer,
    Destination.IdentityVerification,
    Destination.RecoverPassword,
    Destination.ResetPassword,
    Destination.MyOffers,
    Destination.Matches,
)
//arreglo del Matches y el nombre de la rama
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