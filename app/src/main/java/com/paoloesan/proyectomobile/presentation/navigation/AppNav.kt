package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paoloesan.proyectomobile.presentation.admin.AdminUsersScreen
import com.paoloesan.proyectomobile.presentation.alert.AlertScreen
import com.paoloesan.proyectomobile.presentation.auth.LoginScreen
import com.paoloesan.proyectomobile.presentation.auth.RecoverPasswordScreen
import com.paoloesan.proyectomobile.presentation.auth.RegistroScreen
import com.paoloesan.proyectomobile.presentation.auth.ResetPasswordScreen
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

sealed class Destination(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val showInBottomBar: Boolean = false,
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
        icon = Icons.Default.SwapHoriz,
        showInBottomBar = true,
        content = { navController -> MarketplaceScreen(navController) }
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
        icon = Icons.Default.Receipt,
        showInBottomBar = true,
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
        icon = Icons.Default.History,
        showInBottomBar = true,
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
        icon = Icons.Default.Person,
        showInBottomBar = true,
        content = { navController -> ProfileScreen(navController) }
    )

    object AdminUsers : Destination(
        route = "admin_users",
        title = "Administración de Usuarios",
        content = { navController -> AdminUsersScreen(navController) }
    )
}

val appDestinations = listOf(
    Destination.Debug,
    Destination.Login,
    Destination.Registro,
    Destination.RecoverPassword,
    Destination.ResetPassword,
    Destination.IdentityVerification,
    Destination.Marketplace,
    Destination.MyOffers,
    Destination.PublishOffer,
    Destination.Matches,
    Destination.History,
    Destination.Profile,
    Destination.Alerts,
    Destination.OfferDetail,
    Destination.TransactionStatus,
    Destination.BankDetails,
    Destination.UploadVoucher,
    Destination.ConfirmPayment,
    Destination.Chat,
    Destination.DisputaLista,
    Destination.AdminUsers
)

val bottomBarDestinations = listOf(
    Destination.Marketplace,
    Destination.MyOffers,
    Destination.History,
    Destination.Profile
)

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val disputaViewModel: DisputaViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomBarDestinations.any { it.route == currentRoute }

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Destination.Debug.route,
            modifier = Modifier.weight(1f)
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

        if (showBottomBar) {
            NavigationBar {
                bottomBarDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            if (currentRoute != dest.route) {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            dest.icon?.let {
                                Icon(it, contentDescription = dest.title)
                            }
                        },
                        label = { Text(dest.title) }
                    )
                }
            }
        }
    }
}
