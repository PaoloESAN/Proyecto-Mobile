package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.paoloesan.proyectomobile.presentation.p2p.MatchScreen
import com.paoloesan.proyectomobile.presentation.p2p.MyOffersScreen
import com.paoloesan.proyectomobile.presentation.p2p.PublishOfferScreen
import com.paoloesan.proyectomobile.presentation.profile.ProfileScreen
import com.paoloesan.proyectomobile.presentation.profile.SettingsScreen
import com.paoloesan.proyectomobile.presentation.profile.EditProfileScreen
import com.paoloesan.proyectomobile.presentation.transaction.BankDetailsScreen
import com.paoloesan.proyectomobile.presentation.transaction.ChatScreen
import com.paoloesan.proyectomobile.presentation.transaction.ConfirmPaymentScreen
import com.paoloesan.proyectomobile.presentation.transaction.OfferDetailScreen
import com.paoloesan.proyectomobile.presentation.transaction.TransactionStatusScreen
import com.paoloesan.proyectomobile.presentation.verification.IdentityVerificationScreen
import zed.rainxch.rikkaui.components.ui.navigationbar.NavigationBar
import zed.rainxch.rikkaui.components.ui.navigationbar.NavigationBarItem
import zed.rainxch.rikkaui.foundation.RikkaTheme

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
        title = "Mercado",
        icon = Icons.Default.SwapHoriz,
        showInBottomBar = true,
        content = { navController -> MarketplaceScreen(navController) }
    )

    object PublishOffer : Destination(
        route = "publish_offer",
        title = "Crear",
        icon = Icons.Default.Add,
        showInBottomBar = true,
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
        route = "chat/{transactionId}",
        title = "Chat de Transacción",
        content = { navController -> ChatScreen(navController) }
    )

    object MyOffers : Destination(
        route = "my_offers",
        title = "Ofertas",
        icon = Icons.Default.Star,
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
                onStartTransaction = { amount, rate, bank, type ->
                    navController.navigate("transactionStatus/TX001?isSeller=false&amount=$amount&rate=$rate&bank=$bank&type=$type")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    )

    object TransactionStatus : Destination(
        route = "transactionStatus/{transactionId}?isSeller={isSeller}&amount={amount}&rate={rate}&bank={bank}&type={type}&status={status}&uploaded={uploaded}&currency={currency}&isRated={isRated}",
        title = "Estado de Transacción",
        content = { navController ->
            val arguments = navController.currentBackStackEntry?.arguments
            val transactionId = arguments?.getString("transactionId") ?: "TX001"
            val isSeller = arguments?.getString("isSeller")?.toBoolean() ?: false
            val amount = arguments?.getString("amount") ?: "100.00"
            val rate = arguments?.getString("rate") ?: "3.85"
            val bank = arguments?.getString("bank") ?: "BCP - 191-99882211-0-45"
            val type = arguments?.getString("type") ?: "Compra"
            val status = arguments?.getString("status") ?: "Pendiente"
            val uploaded = arguments?.getString("uploaded")?.toBoolean() ?: false
            val currency = arguments?.getString("currency") ?: "USD"
            val isRated = arguments?.getString("isRated")?.toBoolean() ?: false
            TransactionStatusScreen(
                isSeller = isSeller,
                transactionId = transactionId,
                amount = amount,
                rate = rate,
                bank = bank,
                type = type,
                status = status,
                uploaded = uploaded,
                currency = currency,
                isRated = isRated,
                onBack = {
                    navController.popBackStack()
                },
                onViewBankDetails = {
                    navController.navigate("bankDetails/$transactionId?isSeller=$isSeller&amount=$amount&rate=$rate&bank=$bank&type=$type&status=$status&uploaded=$uploaded&currency=$currency")
                },
                onChat = {
                    navController.navigate("chat/$transactionId")
                },
                onConfirmPayment = {
                    navController.navigate("confirm_payment")
                },
                onUploadVoucher = {}
            )
        }
    )

    object BankDetails : Destination(
        route = "bankDetails/{transactionId}?isSeller={isSeller}&amount={amount}&rate={rate}&bank={bank}&type={type}&status={status}&uploaded={uploaded}&currency={currency}",
        title = "Detalles de Pago",
        content = { navController ->
            val arguments = navController.currentBackStackEntry?.arguments
            val transactionId = arguments?.getString("transactionId") ?: "TX001"
            val isSeller = arguments?.getString("isSeller")?.toBoolean() ?: false
            val amount = arguments?.getString("amount") ?: "100.00"
            val rate = arguments?.getString("rate") ?: "3.85"
            val bank = arguments?.getString("bank") ?: "BCP - 191-99882211-0-45"
            val type = arguments?.getString("type") ?: "Compra"
            val status = arguments?.getString("status") ?: "Pendiente"
            val uploaded = arguments?.getString("uploaded")?.toBoolean() ?: false
            val currency = arguments?.getString("currency") ?: "USD"
            BankDetailsScreen(
                isSeller = isSeller,
                transactionId = transactionId,
                amount = amount,
                rate = rate,
                bank = bank,
                type = type,
                status = status,
                uploaded = uploaded,
                currency = currency,
                navController = navController,
                onBack = {
                    navController.popBackStack()
                },
                onChat = {
                    navController.navigate("chat/$transactionId")
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
        title = "Perfil",
        icon = Icons.Default.Person,
        showInBottomBar = true,
        content = { navController -> ProfileScreen(navController) }
    )

    object Settings : Destination(
        route = "settings",
        title = "Configuración",
        content = { navController -> SettingsScreen(navController) }
    )

    object EditProfile : Destination(
        route = "edit_profile",
        title = "Editar Información",
        content = { navController -> EditProfileScreen(navController) }
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
    Destination.Settings,
    Destination.EditProfile,
    Destination.Alerts,
    Destination.OfferDetail,
    Destination.TransactionStatus,
    Destination.BankDetails,
    Destination.ConfirmPayment,
    Destination.Chat,
    Destination.DisputaLista,
    Destination.AdminUsers
)

val bottomBarDestinations = listOf(
    Destination.Marketplace,
    Destination.MyOffers,
    Destination.PublishOffer,
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
            NavigationBar(
                modifier = Modifier.padding(bottom = RikkaTheme.spacing.md)
            ) {
                bottomBarDestinations.forEach { dest ->
                    dest.icon?.let {
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            activeColor = RikkaTheme.colors.primary,
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
                            icon =
                                it,
                            label = dest.title
                        )
                    }
                }
            }
        }
    }
}
