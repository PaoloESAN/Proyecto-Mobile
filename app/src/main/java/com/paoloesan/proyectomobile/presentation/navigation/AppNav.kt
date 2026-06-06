package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paoloesan.proyectomobile.presentation.alert.AlertScreen
import com.paoloesan.proyectomobile.presentation.auth.LoginScreen
import com.paoloesan.proyectomobile.presentation.auth.RecoverPasswordScreen
import com.paoloesan.proyectomobile.presentation.auth.RegistroScreen
import com.paoloesan.proyectomobile.presentation.auth.ResetPasswordScreen
import com.paoloesan.proyectomobile.presentation.dashboard.DashboardScreen
import com.paoloesan.proyectomobile.presentation.debug.DebugScreen
import com.paoloesan.proyectomobile.presentation.disputa.DisputaDetalleScreen
import com.paoloesan.proyectomobile.presentation.disputa.DisputaListaScreen
import com.paoloesan.proyectomobile.presentation.disputa.DisputaViewModel
import com.paoloesan.proyectomobile.presentation.history.HistoryScreen
import com.paoloesan.proyectomobile.presentation.p2p.MarketplaceScreen
import com.paoloesan.proyectomobile.presentation.profile.ProfileScreen
import com.paoloesan.proyectomobile.presentation.p2p.MatchScreen
import com.paoloesan.proyectomobile.presentation.p2p.MyOffersScreen
import com.paoloesan.proyectomobile.presentation.p2p.PublishOfferScreen
import com.paoloesan.proyectomobile.presentation.transaction.BankDetailsScreen
import com.paoloesan.proyectomobile.presentation.transaction.ChatScreen
import com.paoloesan.proyectomobile.presentation.transaction.ConfirmPaymentScreen
import com.paoloesan.proyectomobile.presentation.transaction.OfferDetailScreen
import com.paoloesan.proyectomobile.presentation.transaction.TransactionStatusScreen
import com.paoloesan.proyectomobile.presentation.transaction.UploadVoucherScreen
import com.paoloesan.proyectomobile.presentation.verification.IdentityVerificationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Transacción") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
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

    object Registro : Destination(
        route = "registro",
        title = "Registro de Usuario",
        content = { navController -> RegistroScreen(navController) }
    )

    object Login : Destination(
        route = "login",
        title = "Iniciar Sesión",
        content = { navController -> LoginScreen(navController) }
    )

    object Dashboard : Destination(
        route = "dashboard",
        title = "Dashboard Principal",
        content = { navController -> DashboardScreen(navController) }
    )

    object ResetPassword : Destination(
        route = "reset_password",
        title = "Restablecer Contraseña",
        content = { navController -> ResetPasswordScreen(navController) }
    )
    
    object ConfirmPayment : Destination(
        route = "confirm_payment",
        title = "Confirmar Pago",
        content = { navController -> ConfirmPaymentScreen(navController) }
    )
    
    object Chat : Destination(
        route = "chat",
        title = "Chat de Transacción",
        content = { navController -> ChatScreen(navController) }
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

    object History : Destination(
        route = "history",
        title = "Historial",
        content = { navController -> HistoryScreen(navController) }
    )

    object DisputaLista : Destination(
        route = "disputas",
        title = "Disputas",
        content = { navController ->
            Text("Error: Use AppNav parameter")
        }
    )

    object DisputaDetalle : Destination(
        route = "detalle_disputa/{disputaId}",
        title = "Detalle de Disputa",
        content = { navController ->
            Text("Error: Use AppNav parameter")
        }
    )

    object OfferDetail : Destination(
        route = "offerDetail/{offerId}",
        title = "Detalle de Oferta",
        content = { navController ->
            OfferDetailScreen(
                onStartTransaction = {
                    navController.navigate("transactionStatus/TX001")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    )

    object TransactionStatus : Destination(
        route = "transactionStatus/{transactionId}",
        title = "Estado de Transacción",
        content = { navController ->
            TransactionStatusScreen(
                onBack = {
                    navController.popBackStack()
                },
                onViewBankDetails = {
                    navController.navigate("bankDetails/TX001")
                }
            )
        }
    )

    object BankDetails : Destination(
        route = "bankDetails/{transactionId}",
        title = "Datos Bancarios",
        content = { navController ->
            BankDetailsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onContinueToVoucher = {
                    navController.navigate("uploadVoucher/TX001")
                }
            )
        }
    )

    object UploadVoucher : Destination(
        route = "uploadVoucher/{transactionId}",
        title = "Subir Voucher",
        content = { navController ->
            UploadVoucherScreen(
                onBack = {
                    navController.popBackStack()
                },
                onVoucherSent = {
                    navController.navigate("transactionStatus/TX001") {
                        popUpTo("transactionStatus/TX001") {
                            inclusive = true
                        }
                    }
                }
            )
        }
    )

    object Alerts : Destination(
        route = "alerts",
        title = "Alertas de Tipo de Cambio",
        content = { navController -> AlertScreen(navController) }
    )

    object Profile : Destination(
        route = "profile",
        title = "Mi Perfil",
        content = { navController -> ProfileScreen(navController) }
    )
}

val appDestinations = listOf(
    Destination.Debug,
    Destination.Alerts,
    Destination.Marketplace,
    Destination.TransactionDetail,
    Destination.PublishOffer,
    Destination.IdentityVerification,
    Destination.RecoverPassword,
    Destination.Registro,
    Destination.Login,
    Destination.Dashboard,
    Destination.ResetPassword,
    Destination.ConfirmPayment,
    Destination.Chat,
    Destination.MyOffers,
    Destination.Matches,
    Destination.History,
    Destination.Profile,
    Destination.DisputaLista,
    Destination.OfferDetail,
    Destination.TransactionStatus,
    Destination.BankDetails,
    Destination.UploadVoucher
)

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val disputaViewModel: DisputaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Destination.Debug.route
    ) {
        appDestinations.forEach { destination ->
            composable(destination.route) {
                if (destination == Destination.DisputaLista) {
                    DisputaListaScreen(navController, disputaViewModel)
                } else {
                    destination.content(navController)
                }
            }
        }

        composable(Destination.DisputaDetalle.route) { backStackEntry ->
            val disputaId = backStackEntry.arguments?.getString("disputaId")?.toIntOrNull()
            if (disputaId != null) {
                DisputaDetalleScreen(navController, disputaViewModel, disputaId)
            }
        }
    }
}
