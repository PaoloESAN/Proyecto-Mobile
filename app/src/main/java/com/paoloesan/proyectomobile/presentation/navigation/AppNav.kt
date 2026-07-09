package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paoloesan.proyectomobile.presentation.admin.AdminUsersScreen
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

    object Chat : Destination(
        route = "chat/{transactionId}?readOnly={readOnly}",
        title = "Chat de Transacción",
        content = { navController ->
            val arguments = navController.currentBackStackEntry?.arguments
            val transactionId = arguments?.getString("transactionId")?.toIntOrNull() ?: 0
            val readOnly = arguments?.getString("readOnly")?.toBoolean() ?: false
            ChatScreen(navController = navController, transactionId = transactionId, readOnly = readOnly)
        }
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
            val arguments = navController.currentBackStackEntry?.arguments
            val offerId = arguments?.getString("offerId")?.toIntOrNull() ?: 0
            OfferDetailScreen(
                offerId = offerId,
                onStartTransaction = { transactionId ->
                    navController.navigateSafe("transactionStatus/$transactionId") {
                        popUpTo("offerDetail/$offerId") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStackSafe() }
            )
        }
    )

    object TransactionStatus : Destination(
        route = "transactionStatus/{transactionId}",
        title = "Estado de Transacción",
        content = { navController ->
            val arguments = navController.currentBackStackEntry?.arguments
            val transactionId = arguments?.getString("transactionId")?.toIntOrNull() ?: 0
            TransactionStatusScreen(
                transactionId = transactionId,
                onBack = { navController.popBackStackSafe() },
                onViewBankDetails = { txId, isSeller ->
                    navController.navigateSafe("bankDetails/$txId?isSeller=$isSeller")
                },
                onChat = { txId ->
                    navController.navigateSafe("chat/$txId")
                }
            )
        }
    )

    object BankDetails : Destination(
        route = "bankDetails/{transactionId}?isSeller={isSeller}&amount={amount}&rate={rate}&bank={bank}&type={type}&status={status}&uploaded={uploaded}&currency={currency}",
        title = "Detalles de Pago",
        content = { navController ->
            val arguments = navController.currentBackStackEntry?.arguments
            val transactionId = arguments?.getString("transactionId") ?: "0"
            val isSeller = arguments?.getString("isSeller")?.toBoolean() ?: false
            val amount = arguments?.getString("amount") ?: "100.00"
            val rate = arguments?.getString("rate") ?: "3.85"
            val bank = arguments?.getString("bank") ?: ""
            val type = arguments?.getString("type") ?: "Compra"
            val status = arguments?.getString("status") ?: "En Proceso"
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
                onBack = { navController.popBackStackSafe() },
                onChat = { navController.navigateSafe("chat/$transactionId") }
            )
        }
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
    Destination.OfferDetail,
    Destination.TransactionStatus,
    Destination.BankDetails,
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val startDest = androidx.compose.runtime.remember {
        if (com.paoloesan.proyectomobile.data.local.SessionManager.isLoggedIn(context)) {
            Destination.Marketplace.route
        } else {
            Destination.Login.route
        }
    }

    // Escuchar eventos globales de autenticación (ej. Deep Link de recuperación, notificaciones push)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.paoloesan.proyectomobile.data.AuthEventChannel.events.collect { event ->
            when (event) {
                is com.paoloesan.proyectomobile.data.AuthEvent.NavigateToResetPassword -> {
                    navController.navigate(Destination.ResetPassword.route) {
                        launchSingleTop = true
                    }
                }
                is com.paoloesan.proyectomobile.data.AuthEvent.NavigateToChat -> {
                    if (com.paoloesan.proyectomobile.data.local.SessionManager.isLoggedIn(context)) {
                        navController.navigate("chat/${event.transactionId}")
                    }
                }
                is com.paoloesan.proyectomobile.data.AuthEvent.NavigateToTransactionStatus -> {
                    if (com.paoloesan.proyectomobile.data.local.SessionManager.isLoggedIn(context)) {
                        navController.navigate("transactionStatus/${event.transactionId}")
                    }
                }
            }
            com.paoloesan.proyectomobile.data.AuthEventChannel.clearEvents()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.weight(1f),
            enterTransition = {
                val route = targetState.destination.route
                val isBottomDest = bottomBarDestinations.any { it.route == route }
                if (isBottomDest) {
                    fadeIn(animationSpec = tween(220))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                val route = targetState.destination.route
                val isBottomDest = bottomBarDestinations.any { it.route == route }
                if (isBottomDest) {
                    fadeOut(animationSpec = tween(220))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
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
                modifier = Modifier.navigationBarsPadding().padding(bottom = RikkaTheme.spacing.xs)
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
