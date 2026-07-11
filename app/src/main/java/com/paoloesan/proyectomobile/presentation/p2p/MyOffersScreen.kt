package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import com.paoloesan.proyectomobile.presentation.navigation.navigateSafe
import com.paoloesan.proyectomobile.presentation.components.OfflineScreen
import zed.rainxch.rikkaui.components.ui.skeleton.Skeleton
import zed.rainxch.rikkaui.components.ui.skeleton.SkeletonAnimation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardAnimation
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.tabs.Tab
import zed.rainxch.rikkaui.components.ui.tabs.TabAnimation
import zed.rainxch.rikkaui.components.ui.tabs.TabContent
import zed.rainxch.rikkaui.components.ui.tabs.TabList
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(navController: NavController) {
    val viewModel: MyOffersViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    var showCancelDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<MyOfferUiItem?>(null) }

    var editAmount by remember { mutableStateOf("") }
    val editCalculatedAmount by remember(editAmount, selectedOffer) {
        derivedStateOf {
            val amt = editAmount.toDoubleOrNull() ?: 0.0
            val offer = selectedOffer ?: return@derivedStateOf 0.0
            if (offer.type == "Compra") {
                if (offer.rate > 0.0) amt / offer.rate else 0.0
            } else {
                amt * offer.rate
            }
        }
    }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            toastState.show(message = it, variant = ToastVariant.Success)
            viewModel.consumeSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            toastState.show(message = it, variant = ToastVariant.Destructive)
            viewModel.consumeError()
        }
    }

    if (showCancelDialog && selectedOffer != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Cancelar Oferta",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Text(
                    text = "\u00bfEst\u00e1 seguro de cancelar esta oferta?",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedOffer?.let { viewModel.cancelOffer(it.offerId) }
                        showCancelDialog = false
                    },
                    text = "Confirmar"
                )
            },
            dismissButton = {
                Button(
                    onClick = { showCancelDialog = false },
                    variant = ButtonVariant.Outline,
                    text = "Cancelar"
                )
            }
        )
    }

    if (showEditDialog && selectedOffer != null) {
        val currentOffer = selectedOffer!!
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Editar Oferta",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    val inputLabel = if (currentOffer.type == "Compra") {
                        "Cantidad a Cambiar (${currentOffer.monedaRecibo})"
                    } else {
                        "Cantidad a Cambiar (${currentOffer.monedaTengo})"
                    }
                    Text(
                        text = inputLabel,
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = editAmount,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) editAmount = it
                        },
                        placeholder = "Cantidad",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Tipo de Cambio",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = String.format(java.util.Locale.US, "%.4f", currentOffer.rate),
                        onValueChange = {},
                        enabled = false,
                        placeholder = "Tipo de cambio",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    val estimatedLabel = if (currentOffer.type == "Compra") {
                        "Entregas (Estimado en ${currentOffer.monedaTengo})"
                    } else {
                        "Recibes (Estimado en ${currentOffer.monedaRecibo})"
                    }
                    Text(
                        text = estimatedLabel,
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = String.format(
                            java.util.Locale.US,
                            "%,.2f",
                            editCalculatedAmount
                        ),
                        onValueChange = {},
                        enabled = false,
                        placeholder = if (currentOffer.type == "Compra") "Entregas" else "Recibes",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showEditDialog = false
                            showCancelDialog = true
                        },
                        variant = ButtonVariant.Destructive,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Eliminar Oferta"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cantVal = editAmount.toDoubleOrNull() ?: 0.0
                        if (cantVal <= 0.0) {
                            scope.launch {
                                toastState.show(
                                    message = "La cantidad debe ser mayor a 0",
                                    variant = ToastVariant.Destructive
                                )
                            }
                        } else {
                            viewModel.editOffer(
                                offerId = currentOffer.offerId,
                                cantidad = cantVal,
                                monedaTengo = currentOffer.monedaTengo,
                                monedaRecibo = currentOffer.monedaRecibo,
                                type = currentOffer.type
                            )
                            showEditDialog = false
                        }
                    },

                    text = "Guardar"
                )
            },
            dismissButton = {
                Button(
                    onClick = { showEditDialog = false },
                    variant = ButtonVariant.Outline,
                    text = "Cancelar"
                )
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            snackbarHost = {
                ToastHost(hostState = toastState)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Ofertas",
                    color = RikkaTheme.colors.onBackground,
                    variant = TextVariant.H1,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                TabList(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Tab(
                        modifier = Modifier.weight(1f),
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = "Mis solicitudes",
                        animation = TabAnimation.Spring,
                    )
                    Tab(
                        modifier = Modifier.weight(1f),
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = "Mis ofertas",
                    )
                }

                if (uiState.isNetworkError) {
                    OfflineScreen(onRetry = { viewModel.loadData() })
                } else if (uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, RikkaTheme.colors.muted.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Skeleton(
                                        modifier = Modifier.width(150.dp).height(20.dp),
                                        animation = SkeletonAnimation.Shimmer
                                    )
                                    Skeleton(
                                        modifier = Modifier.size(24.dp),
                                        shape = RikkaTheme.shapes.full,
                                        animation = SkeletonAnimation.Shimmer
                                    )
                                }
                                Skeleton(
                                    modifier = Modifier.fillMaxWidth(0.6f).height(16.dp),
                                    animation = SkeletonAnimation.Shimmer
                                )
                                Skeleton(
                                    modifier = Modifier.fillMaxWidth(0.4f).height(16.dp),
                                    animation = SkeletonAnimation.Shimmer
                                )
                            }
                        }
                    }
                } else {
                    TabContent {
                        when (selectedTab) {
                            0 -> {
                                if (uiState.sentTransactions.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { focusManager.clearFocus() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No tienes solicitudes enviadas activas",
                                            variant = TextVariant.Large,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { focusManager.clearFocus() }
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(vertical = 12.dp)
                                    ) {
                                        items(
                                            uiState.sentTransactions,
                                            key = { it.transactionId }) { tx ->
                                            Card(
                                                onClick = {
                                                    navController.navigateSafe("transactionStatus/${tx.transactionId}")
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                animation = CardAnimation.Press
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = if (tx.isMyOffer) "Solicitud de ${tx.sellerName}" else "Solicitud a ${tx.sellerName}",
                                                            variant = TextVariant.Large,
                                                            color = RikkaTheme.colors.onBackground
                                                        )
                                                        val badgeBgColor = when (tx.status) {
                                                            "Aceptada", "Pagado" -> RikkaTheme.colors.primary.copy(
                                                                alpha = 0.15f
                                                            )

                                                            "Rechazada", "Cancelado", "Disputa" -> RikkaTheme.colors.destructive.copy(
                                                                alpha = 0.15f
                                                            )

                                                            else -> RikkaTheme.colors.warning.copy(
                                                                alpha = 0.15f
                                                            )
                                                        }
                                                        val badgeTextColor = when (tx.status) {
                                                            "Aceptada", "Pagado" -> RikkaTheme.colors.primary
                                                            "Rechazada", "Cancelado", "Disputa" -> RikkaTheme.colors.destructive
                                                            else -> RikkaTheme.colors.warning
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    color = badgeBgColor,
                                                                    shape = RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(
                                                                    horizontal = 8.dp,
                                                                    vertical = 4.dp
                                                                )
                                                        ) {
                                                            Text(
                                                                text = tx.status,
                                                                variant = TextVariant.Small,
                                                                color = badgeTextColor
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(
                                                        text = "Quieres ${if (tx.type == "Compra") "comprar" else "vender"} ${tx.currency} ${
                                                            "%.2f".format(
                                                                tx.amount
                                                            )
                                                        }",
                                                        variant = TextVariant.P,
                                                        color = RikkaTheme.colors.onBackground.copy(
                                                            alpha = 0.8f
                                                        )
                                                    )

                                                    Text(
                                                        text = "T.C.: ${"%.2f".format(tx.rate)}",
                                                        variant = TextVariant.Small,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                if (uiState.myOffers.isEmpty() && uiState.incomingTransactions.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { focusManager.clearFocus() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No has publicado ofertas",
                                            variant = TextVariant.Large,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { focusManager.clearFocus() }
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(vertical = 12.dp)
                                    ) {
                                        items(uiState.myOffers, key = { it.offerId }) { offer ->
                                            Card(
                                                onClick = {
                                                    if (offer.status == "En Proceso") {
                                                        offer.activeTransactionId?.let { txId ->
                                                            navController.navigateSafe("transactionStatus/$txId")
                                                        }
                                                    } else if (offer.status == "Activa") {
                                                        selectedOffer = offer
                                                        editAmount = if (offer.type == "Compra") {
                                                            offer.montoRecibo.toString()
                                                        } else {
                                                            offer.montoTengo.toString()
                                                        }
                                                        showEditDialog = true
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                animation = CardAnimation.Press
                                            ) {
                                                Column {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        val isCompra = offer.type == "Compra"
                                                        Text(
                                                            text = "${offer.type} - ${offer.monedaTengo}/${offer.monedaRecibo}",
                                                            variant = TextVariant.Large,
                                                            color = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                                                        )

                                                        val displayStatus = offer.activeTransactionStatus ?: offer.status
                                                        val badgeBgColor = when (displayStatus) {
                                                            "Activa" -> RikkaTheme.colors.success.copy(
                                                                alpha = 0.15f
                                                            )
                                                            "En Proceso", "Pendiente" -> RikkaTheme.colors.warning.copy(
                                                                alpha = 0.15f
                                                            )
                                                            "Pagado" -> RikkaTheme.colors.primary.copy(
                                                                alpha = 0.15f
                                                            )
                                                            "Disputa" -> RikkaTheme.colors.destructive.copy(
                                                                alpha = 0.15f
                                                            )
                                                            "Finalizado" -> RikkaTheme.colors.success.copy(
                                                                alpha = 0.15f
                                                            )
                                                            else -> RikkaTheme.colors.muted.copy(
                                                                alpha = 0.15f
                                                            )
                                                        }
                                                        val badgeTextColor = when (displayStatus) {
                                                            "Activa", "Finalizado" -> RikkaTheme.colors.success
                                                            "En Proceso", "Pendiente" -> RikkaTheme.colors.warning
                                                            "Pagado" -> RikkaTheme.colors.primary
                                                            "Disputa" -> RikkaTheme.colors.destructive
                                                            else -> Color.Gray
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    color = badgeBgColor,
                                                                    shape = RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(
                                                                    horizontal = 8.dp,
                                                                    vertical = 4.dp
                                                                )
                                                        ) {
                                                            Text(
                                                                text = displayStatus,
                                                                variant = TextVariant.Small,
                                                                color = badgeTextColor
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Text(
                                                        text = "Tengo: ${
                                                            String.format(
                                                                java.util.Locale.US,
                                                                "%,.2f",
                                                                offer.montoTengo
                                                            )
                                                        } ${offer.monedaTengo}",
                                                        variant = TextVariant.Large,
                                                        color = RikkaTheme.colors.onBackground
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Recibo: ${
                                                            String.format(
                                                                java.util.Locale.US,
                                                                "%,.2f",
                                                                offer.montoRecibo
                                                            )
                                                        } ${offer.monedaRecibo}",
                                                        variant = TextVariant.Large,
                                                        color = RikkaTheme.colors.primary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "T.C.: ${
                                                            String.format(
                                                                java.util.Locale.US,
                                                                "%.4f",
                                                                offer.rate
                                                            )
                                                        }  |  Método: ${offer.paymentMethod}",
                                                        variant = TextVariant.Small,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }

                                        if (uiState.incomingTransactions.isNotEmpty()) {
                                            item {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Solicitudes Pendientes",
                                                    variant = TextVariant.Large,
                                                    color = RikkaTheme.colors.onBackground,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }

                                            items(
                                                uiState.incomingTransactions,
                                                key = { it.transactionId }) { req ->
                                                Card(
                                                    onClick = {},
                                                    modifier = Modifier.fillMaxWidth(),
                                                    animation = CardAnimation.Press
                                                ) {
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                     ) {
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth(),
                                                             horizontalArrangement = Arrangement.SpaceBetween,
                                                             verticalAlignment = Alignment.CenterVertically
                                                         ) {
                                                             Text(
                                                                 text = "Solicitud de ${req.buyerName}",
                                                                 variant = TextVariant.P,
                                                                color = RikkaTheme.colors.onBackground,
                                                                style = androidx.compose.ui.text.TextStyle(
                                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                                )
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(
                                                                        color = RikkaTheme.colors.warning.copy(
                                                                            alpha = 0.15f
                                                                        ),
                                                                        shape = RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(
                                                                        horizontal = 8.dp,
                                                                        vertical = 4.dp
                                                                    )
                                                            ) {
                                                                Text(
                                                                    text = "Pendiente",
                                                                    variant = TextVariant.Small,
                                                                    color = RikkaTheme.colors.warning
                                                                )
                                                            }
                                                        }

                                                        Text(
                                                            text = "${req.type} ${req.currency} ${
                                                                "%.2f".format(
                                                                    req.amount
                                                                )
                                                            } a T.C. ${"%.2f".format(req.rate)}",
                                                            variant = TextVariant.P,
                                                            color = RikkaTheme.colors.onBackground.copy(
                                                                alpha = 0.8f
                                                            )
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            )
                                                        ) {
                                                            Button(
                                                                onClick = {
                                                                    viewModel.acceptTransaction(
                                                                        req.transactionId
                                                                    )
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                text = "Aceptar"
                                                            )
                                                            Button(
                                                                onClick = {
                                                                    viewModel.rejectTransaction(
                                                                        req.transactionId
                                                                    )
                                                                },
                                                                variant = ButtonVariant.Destructive,
                                                                modifier = Modifier.weight(1f),
                                                                text = "Rechazar"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
