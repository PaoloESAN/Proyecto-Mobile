package com.paoloesan.proyectomobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paoloesan.proyectomobile.presentation.debug.DebugScreen
import com.paoloesan.proyectomobile.presentation.transaction.BankDetailsScreen
import com.paoloesan.proyectomobile.presentation.transaction.OfferDetailScreen
import com.paoloesan.proyectomobile.presentation.transaction.TransactionStatusScreen
import com.paoloesan.proyectomobile.presentation.transaction.UploadVoucherScreen

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
}

//Luego agregalo a la lista
val appDestinations = listOf(
    Destination.Debug,
    Destination.OfferDetail,
    Destination.TransactionStatus,
    Destination.BankDetails,
    Destination.UploadVoucher
)

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "offerDetail/OFF001"
    ) {
        appDestinations.forEach { destination ->
            composable(destination.route) {
                destination.content(navController)
            }
        }
    }
}