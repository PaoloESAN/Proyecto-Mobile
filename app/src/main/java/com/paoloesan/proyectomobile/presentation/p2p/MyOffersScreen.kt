package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

data class MyOffer(
    val id: String,
    val type: String,
    val currency: String,
    val amount: Double,
    val rate: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val paymentMethod: String,
    val hasActiveTransaction: Boolean = false
)

data class IncomingRequest(
    val id: String,
    val buyerName: String,
    val type: String, // "Compra" o "Venta"
    val currency: String,
    val amount: Double,
    val rate: Double,
    val paymentMethod: String,
    var status: String // "Pendiente", "Aceptada", "Rechazada"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(navController: NavController) {
    // Pestaña Seleccionada: 0 = Mis solicitudes, 1 = Solicitudes
    var selectedTab by remember { mutableStateOf(0) }

    // Mis Ofertas ("Mis solicitudes")
    var offers by remember {
        mutableStateOf(
            listOf(
                MyOffer("1", "Compra", "USD", 150.0, 3.75, 50.0, 200.0, "BCP"),
                MyOffer("2", "Venta", "PEN", 500.0, 1.0, 10.0, 500.0, "Yape"),
                MyOffer(
                    "3",
                    "Compra",
                    "USD",
                    300.0,
                    3.76,
                    100.0,
                    400.0,
                    "Interbank",
                    hasActiveTransaction = true
                ),
                MyOffer("4", "Venta", "USD", 200.0, 3.74, 50.0, 250.0, "BCP")
            )
        )
    }

    // Ofertas que otros me hacen a mí ("Solicitudes")
    var incomingRequests by remember {
        mutableStateOf(
            listOf(
                IncomingRequest(
                    "1",
                    "Juan Pérez",
                    "Compra",
                    "USD",
                    100.0,
                    3.75,
                    "BCP",
                    "Pendiente"
                ),
                IncomingRequest(
                    "2",
                    "María Gómez",
                    "Venta",
                    "PEN",
                    200.0,
                    1.0,
                    "Yape",
                    "Pendiente"
                ),
                IncomingRequest(
                    "3",
                    "Carlos López",
                    "Compra",
                    "USD",
                    150.0,
                    3.76,
                    "Interbank",
                    "Aceptada"
                )
            )
        )
    }

    // Estados para edición y cancelación (Tab 0)
    var showCancelDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<MyOffer?>(null) }

    var editAmount by remember { mutableStateOf("") }
    var editRate by remember { mutableStateOf("") }
    var editMinLimit by remember { mutableStateOf("") }
    var editMaxLimit by remember { mutableStateOf("") }
    var editPaymentMethod by remember { mutableStateOf("") }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Dialog de cancelar
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
                    text = "¿Está seguro de cancelar esta oferta?",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        offers = offers.filter { it.id != selectedOffer?.id }
                        showCancelDialog = false
                        scope.launch {
                            toastState.show(
                                message = "Oferta cancelada correctamente",
                                variant = ToastVariant.Success
                            )
                        }
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

    // Dialog de editar
    if (showEditDialog && selectedOffer != null) {
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
                    Text(
                        text = "Monto",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = editAmount,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) editAmount = it
                        },
                        placeholder = "Monto",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Tipo de Cambio",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = editRate,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) editRate = it
                        },
                        placeholder = "Tipo de cambio",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Límite Mínimo",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = editMinLimit,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) editMinLimit = it
                        },
                        placeholder = "Límite mínimo",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Límite Máximo",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = editMaxLimit,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) editMaxLimit = it
                        },
                        placeholder = "Límite máximo",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Método de Pago",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = editPaymentMethod,
                        onValueChange = { editPaymentMethod = it },
                        placeholder = "BCP, Yape, etc.",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minVal = editMinLimit.toDoubleOrNull()
                        val maxVal = editMaxLimit.toDoubleOrNull()
                        if (minVal != null && maxVal != null && minVal > maxVal) {
                            scope.launch {
                                toastState.show(
                                    message = "El monto mínimo no puede ser mayor al máximo",
                                    variant = ToastVariant.Destructive
                                )
                            }
                        } else {
                            offers = offers.map {
                                if (it.id == selectedOffer?.id) {
                                    it.copy(
                                        amount = editAmount.toDoubleOrNull() ?: it.amount,
                                        rate = editRate.toDoubleOrNull() ?: it.rate,
                                        minLimit = minVal ?: it.minLimit,
                                        maxLimit = maxVal ?: it.maxLimit,
                                        paymentMethod = editPaymentMethod.ifBlank { it.paymentMethod }
                                    )
                                } else it
                            }
                            showEditDialog = false
                            scope.launch {
                                toastState.show(
                                    message = "Oferta actualizada correctamente",
                                    variant = ToastVariant.Success
                                )
                            }
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
                ToastHost(
                    hostState = toastState
                )
            },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = RikkaIcons.ArrowLeft,
                            contentDescription = "Volver",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }

                    Text(
                        text = "Trades",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                        text = "Solicitudes",
                    )
                }

                TabContent {
                    when (selectedTab) {
                        0 -> {
                            if (offers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            focusManager.clearFocus()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tienes solicitudes activas",
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
                                        ) {
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    items(offers, key = { it.id }) { offer ->
                                        Card(
                                            onClick = {
                                                navController.navigate("transactionStatus/TX001?isSeller=false")
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
                                                        text = "${offer.type} - ${offer.currency}",
                                                        variant = TextVariant.Large,
                                                        color = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                                                    )
                                                    Text(
                                                        text = "${offer.currency} ${offer.amount}",
                                                        variant = TextVariant.Large,
                                                        color = RikkaTheme.colors.onBackground
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = "T.C.: ${offer.rate}  |  Metodo: ${offer.paymentMethod}",
                                                    variant = TextVariant.Small,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = "Limites: Min ${offer.minLimit} / Max ${offer.maxLimit}",
                                                    variant = TextVariant.Small,
                                                    color = Color.Gray
                                                )

                                                if (offer.hasActiveTransaction) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = "Tiene transaccion activa",
                                                        variant = TextVariant.Small,
                                                        color = RikkaTheme.colors.destructive
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            if (incomingRequests.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            focusManager.clearFocus()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No has recibido solicitudes",
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
                                        ) {
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    items(incomingRequests, key = { it.id }) { req ->
                                        Card(
                                            onClick = {
                                                navController.navigate("transactionStatus/TX001?isSeller=true")
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            animation = CardAnimation.Press
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val isCompra = req.type == "Compra"
                                                    Text(
                                                        text = "Solicitud de ${req.buyerName}",
                                                        variant = TextVariant.Large,
                                                        color = RikkaTheme.colors.onBackground
                                                    )
                                                    val badgeBgColor = when (req.status) {
                                                        "Aceptada" -> RikkaTheme.colors.primary.copy(
                                                            alpha = 0.15f
                                                        )

                                                        "Rechazada" -> RikkaTheme.colors.destructive.copy(
                                                            alpha = 0.15f
                                                        )

                                                        else -> RikkaTheme.colors.warning.copy(alpha = 0.15f)
                                                    }
                                                    val badgeTextColor = when (req.status) {
                                                        "Aceptada" -> RikkaTheme.colors.primary
                                                        "Rechazada" -> RikkaTheme.colors.destructive
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
                                                            text = req.status,
                                                            variant = TextVariant.Small,
                                                            color = badgeTextColor
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = "${if (req.type == "Compra") "Quiere comprarte" else "Quiere venderte"} ${req.currency} ${req.amount}",
                                                    variant = TextVariant.P,
                                                    color = RikkaTheme.colors.onBackground.copy(
                                                        alpha = 0.8f
                                                    )
                                                )

                                                Text(
                                                    text = "T.C.: ${req.rate}  |  Metodo: ${req.paymentMethod}",
                                                    variant = TextVariant.Small,
                                                    color = Color.Gray
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