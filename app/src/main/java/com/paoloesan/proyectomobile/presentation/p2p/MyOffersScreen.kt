package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
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
    val status: String = "Activa",
    val hasActiveTransaction: Boolean = false
)

data class IncomingRequest(
    val id: String,
    val offerId: String, // Relacionada a MyOffer.id
    val buyerName: String,
    val type: String, // "Compra" o "Venta"
    val currency: String,
    val amount: Double,
    val rate: Double,
    val paymentMethod: String,
    var status: String // "Pendiente", "Aceptada", "Rechazada"
)

data class SentRequest(
    val id: String,
    val sellerName: String,
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
    // Pestaña Seleccionada: 0 = Mis solicitudes, 1 = Solicitudes recibidas
    var selectedTab by remember { mutableStateOf(0) }

    // Mis Ofertas creadas
    var offers by remember {
        mutableStateOf(
            listOf(
                MyOffer("1", "Compra", "USD", 150.0, 3.75, 50.0, 200.0, "BCP", status = "Activa"),
                MyOffer("2", "Venta", "PEN", 500.0, 1.0, 10.0, 500.0, "Yape", status = "Activa"),
                MyOffer(
                    "3",
                    "Compra",
                    "USD",
                    300.0,
                    3.76,
                    100.0,
                    400.0,
                    "Interbank",
                    status = "En Proceso",
                    hasActiveTransaction = true
                ),
                MyOffer("4", "Venta", "USD", 200.0, 3.74, 50.0, 250.0, "BCP", status = "Finalizada")
            )
        )
    }

    // Ofertas que otros me hacen a mí (agrupadas bajo offerId)
    var incomingRequests by remember {
        mutableStateOf(
            listOf(
                IncomingRequest(
                    "1",
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
                    "3",
                    "Carlos López",
                    "Compra",
                    "USD",
                    150.0,
                    3.76,
                    "Interbank",
                    "Aceptada"
                ),
                IncomingRequest(
                    "4",
                    "1",
                    "Sofía Rojas",
                    "Compra",
                    "USD",
                    50.0,
                    3.75,
                    "BCP",
                    "Pendiente"
                ),
                IncomingRequest(
                    "5",
                    "2",
                    "Pedro Infante",
                    "Venta",
                    "PEN",
                    100.0,
                    1.0,
                    "Yape",
                    "Rechazada"
                )
            )
        )
    }

    // Solicitudes que yo he hecho a otros (Mis Solicitudes)
    var sentRequests by remember {
        mutableStateOf(
            listOf(
                SentRequest(
                    "S1",
                    "Andrés Ganoza",
                    "Compra",
                    "USD",
                    120.0,
                    3.82,
                    "Interbank",
                    "Pendiente"
                ),
                SentRequest(
                    "S2",
                    "Clara Benavides",
                    "Venta",
                    "PEN",
                    400.0,
                    1.00,
                    "Yape",
                    "Aceptada"
                ),
                SentRequest(
                    "S3",
                    "Lucas Torres",
                    "Compra",
                    "USD",
                    80.0,
                    3.80,
                    "BCP",
                    "Rechazada"
                ),
                SentRequest(
                    "S4",
                    "Mariano Beltrán",
                    "Venta",
                    "USD",
                    220.0,
                    3.78,
                    "BCP",
                    "En disputa"
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
                    // TODO: El método de pago debe ser un selector (Dropdown/Select) de los métodos de pago registrados por el usuario
                    Input(
                        value = editPaymentMethod,
                        onValueChange = { editPaymentMethod = it },
                        placeholder = "BCP, Yape, etc.",
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
                        text = "Ofertas",
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
                        text = "Solicitudes recibidas",
                    )
                }

                TabContent {
                    when (selectedTab) {
                        0 -> {
                            if (sentRequests.isEmpty()) {
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
                                        ) {
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    items(sentRequests, key = { it.id }) { req ->
                                        Card(
                                            onClick = {
                                                navController.navigate("transactionStatus/TX001?isSeller=false&amount=${req.amount}&rate=${req.rate}&bank=${req.paymentMethod}&type=${req.type}&status=${req.status}")
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
                                                    Text(
                                                        text = "Solicitud a ${req.sellerName}",
                                                        variant = TextVariant.Large,
                                                        color = RikkaTheme.colors.onBackground
                                                    )
                                                    val badgeBgColor = when (req.status) {
                                                        "Aceptada" -> RikkaTheme.colors.primary.copy(
                                                            alpha = 0.15f
                                                        )

                                                        "Rechazada", "En disputa" -> RikkaTheme.colors.destructive.copy(
                                                            alpha = 0.15f
                                                        )

                                                        else -> RikkaTheme.colors.warning.copy(alpha = 0.15f)
                                                    }
                                                    val badgeTextColor = when (req.status) {
                                                        "Aceptada" -> RikkaTheme.colors.primary
                                                        "Rechazada", "En disputa" -> RikkaTheme.colors.destructive
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
                                                    text = "Quieres ${if (req.type == "Compra") "comprar" else "vender"} ${req.currency} ${req.amount}",
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

                        1 -> {
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
                                        ) {
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    items(offers.filter { it.status != "Finalizada" }, key = { it.id }) { offer ->
                                        Card(
                                            onClick = {
                                                if (offer.status == "En Proceso") {
                                                    val req = incomingRequests.firstOrNull { it.offerId == offer.id }
                                                    if (req != null) {
                                                        navController.navigate("transactionStatus/TX001?isSeller=${offer.type == "Venta"}&amount=${req.amount}&rate=${offer.rate}&bank=${offer.paymentMethod}&type=${offer.type}&status=${req.status}&currency=${offer.currency}")
                                                    }
                                                } else if (offer.status == "Activa") {
                                                    selectedOffer = offer
                                                    editAmount = offer.amount.toString()
                                                    editRate = offer.rate.toString()
                                                    editMinLimit = offer.minLimit.toString()
                                                    editMaxLimit = offer.maxLimit.toString()
                                                    editPaymentMethod = offer.paymentMethod
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
                                                        text = "${offer.type} - ${offer.currency}",
                                                        variant = TextVariant.Large,
                                                        color = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                                                    )

                                                    // Status Badge
                                                    val badgeBgColor = when (offer.status) {
                                                        "Activa" -> RikkaTheme.colors.success.copy(alpha = 0.15f)
                                                        "En Proceso" -> RikkaTheme.colors.warning.copy(alpha = 0.15f)
                                                        else -> RikkaTheme.colors.muted.copy(alpha = 0.15f)
                                                    }
                                                    val badgeTextColor = when (offer.status) {
                                                        "Activa" -> RikkaTheme.colors.success
                                                        "En Proceso" -> RikkaTheme.colors.warning
                                                        else -> Color.Gray
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                color = badgeBgColor,
                                                                shape = RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = offer.status,
                                                            variant = TextVariant.Small,
                                                            color = badgeTextColor
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = "Monto: ${offer.currency} ${offer.amount}",
                                                    variant = TextVariant.Large,
                                                    color = RikkaTheme.colors.onBackground
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
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

                                                if (offer.status == "En Proceso") {
                                                    val req = incomingRequests.firstOrNull { it.offerId == offer.id }
                                                    if (req != null) {
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(1.dp)
                                                                .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = "Solicitud recibida (En Proceso):",
                                                            variant = TextVariant.Small,
                                                            color = Color.Gray
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(RikkaTheme.colors.muted.copy(alpha = 0.05f))
                                                                .border(
                                                                    1.dp,
                                                                    RikkaTheme.colors.muted.copy(alpha = 0.15f),
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .padding(12.dp)
                                                        ) {
                                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = "Comprador: ${req.buyerName}",
                                                                        variant = TextVariant.P,
                                                                        color = RikkaTheme.colors.onBackground,
                                                                        style = androidx.compose.ui.text.TextStyle(
                                                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                                        )
                                                                    )
                                                                    Text(
                                                                        text = "Monto: ${req.currency} ${req.amount}",
                                                                        variant = TextVariant.Small,
                                                                        color = RikkaTheme.colors.onBackground
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(
                                                                    text = "Toca la tarjeta para ver y gestionar la transacción",
                                                                    variant = TextVariant.Small,
                                                                    color = RikkaTheme.colors.primary
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
}