package com.paoloesan.proyectomobile.presentation.transaction

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

enum class TransactionState(
    val title: String,
    val color: Color,
    val icon: ImageVector,
    val bgColor: Color
) {
    ESPERANDO_ACEPTACION("Esperando aceptación", Color(0xFFFFB74D), Icons.Default.HourglassEmpty, Color(0xFFFFF8E1)),
    EN_PROCESO("En proceso (Pendiente de pago)", Color(0xFF1E88E5), Icons.Default.AccessTime, Color(0xFFE3F2FD)),
    PAGADA_CON_VOUCHER("Pago enviado (Voucher subido)", Color(0xFF4CAF50), Icons.Default.CheckCircle, Color(0xFFE8F5E9)),
    FINALIZADA("Finalizada", Color(0xFF2E7D32), Icons.Default.CheckCircle, Color(0xFFE8F5E9)),
    RECHAZADA("Rechazada", Color(0xFFD32F2F), Icons.Default.Cancel, Color(0xFFFFEBEE)),
    EN_DISPUTA("En disputa", Color(0xFFD32F2F), Icons.Default.Cancel, Color(0xFFFFEBEE))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionStatusScreen(
    isSeller: Boolean = false,
    transactionId: String = "TX001",
    amount: String = "100.00",
    rate: String = "3.85",
    bank: String = "BCP - 191-99882211-0-45 (PEN)",
    type: String = "Compra",
    status: String = "Pendiente",
    uploaded: Boolean = false,
    currency: String = "USD",
    isRated: Boolean = false,
    onBack: () -> Unit,
    onViewBankDetails: () -> Unit,
    onChat: () -> Unit = {},
    onConfirmPayment: () -> Unit = {},
    onUploadVoucher: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()

    val initialState = when (status) {
        "Aceptada", "Aceptado" -> TransactionState.EN_PROCESO
        "Rechazada", "Rechazado" -> TransactionState.RECHAZADA
        "Finalizada", "Finalizado" -> TransactionState.FINALIZADA
        "En disputa", "Disputa" -> TransactionState.EN_DISPUTA
        else -> TransactionState.ESPERANDO_ACEPTACION
    }
    var currentState by remember { mutableStateOf(initialState) }
    var showVoucherDialog by remember { mutableStateOf(false) }
    var showBankDetailsDialog by remember { mutableStateOf(false) }
    var showBuyerBankDetailsDialog by remember { mutableStateOf(false) }

    var buyerVoucherUploaded by remember { mutableStateOf(!isSeller && uploaded || initialState == TransactionState.FINALIZADA) }
    var sellerVoucherUploaded by remember { mutableStateOf(isSeller && uploaded || initialState == TransactionState.FINALIZADA) }
    var buyerConfirmedReceipt by remember { mutableStateOf(initialState == TransactionState.FINALIZADA) }
    var sellerConfirmedReceipt by remember { mutableStateOf(initialState == TransactionState.FINALIZADA) }
    var showDisputeDialog by remember { mutableStateOf(false) }
    var isTransactionRated by remember { mutableStateOf(isRated) }

    androidx.compose.runtime.LaunchedEffect(buyerVoucherUploaded, sellerVoucherUploaded) {
        if (buyerVoucherUploaded && !sellerVoucherUploaded && !isSeller) {
            kotlinx.coroutines.delay(2000)
            sellerVoucherUploaded = true
            toastState.show("El vendedor ha subido su comprobante", ToastVariant.Success)
        } else if (sellerVoucherUploaded && !buyerVoucherUploaded && isSeller) {
            kotlinx.coroutines.delay(2000)
            buyerVoucherUploaded = true
            toastState.show("El comprador ha subido su comprobante", ToastVariant.Success)
        }
        if (buyerVoucherUploaded && sellerVoucherUploaded && currentState == TransactionState.EN_PROCESO) {
            currentState = TransactionState.PAGADA_CON_VOUCHER
        }
    }

    val currencyClean = currency.uppercase()
    val amountDouble = amount.toDoubleOrNull() ?: 100.0
    val rateDouble = rate.toDoubleOrNull() ?: 3.85

    val (usdAmount, penAmount) = if (currencyClean == "PEN") {
        (amountDouble / rateDouble) to amountDouble
    } else {
        amountDouble to (amountDouble * rateDouble)
    }

    val formattedPen = String.format(java.util.Locale.US, "%,.2f", penAmount)
    val formattedUsd = String.format(java.util.Locale.US, "%,.2f", usdAmount)

    // Dialog: Bank details for buyer (Seller's bank details)
    if (showBankDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showBankDetailsDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Datos de Cuenta del Vendedor",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Por favor realiza la transferencia a la siguiente cuenta del vendedor para completar el intercambio:",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RikkaTheme.colors.muted.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Banco: BCP", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Text(text = "Cuenta: 191-99882211-0-45", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Text(text = "CCI: 002-191-199882211045-56", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Text(text = "Titular: Juan Perez", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Text(text = "Moneda: PEN", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Monto a depositar: $formattedPen PEN",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.primary,
                                style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBankDetailsDialog = false },
                    text = "Entendido"
                )
            }
        )
    }

    // Dialog: Bank details for seller (Buyer's bank details)
    if (showBuyerBankDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showBuyerBankDetailsDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Datos de Cuenta del Comprador",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Por favor realiza la transferencia a la siguiente cuenta elegida por el comprador:",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RikkaTheme.colors.muted.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Detalles de Cuenta: $bank", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Text(text = "Titular: Mateo Rojas", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Text(text = "Moneda: USD", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Monto a depositar: $formattedUsd USD",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.primary,
                                style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBuyerBankDetailsDialog = false },
                    text = "Entendido"
                )
            }
        )
    }

    // Dialog: Voucher preview for seller or buyer
    if (showVoucherDialog) {
        val voucherTitle = if (isSeller) "Comprobante del Comprador (PEN)" else "Comprobante del Vendedor (USD)"
        val voucherAmount = if (isSeller) "$formattedPen PEN" else "$formattedUsd USD"
        val transactionCode = if (isSeller) "BCP-9081249-X" else "INT-5542123-Z"

        AlertDialog(
            onDismissRequest = { showVoucherDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Verificación de Depósito",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Se ha subido el siguiente comprobante de transferencia:",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = voucherTitle,
                                color = Color.Black,
                                variant = TextVariant.P,
                                style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "MONTO: $voucherAmount",
                                color = Color(0xFF2E7D32),
                                variant = TextVariant.Large
                            )
                            Text(
                                text = "Transacción: $transactionCode",
                                color = Color.DarkGray,
                                variant = TextVariant.Small
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVoucherDialog = false },
                    text = "Cerrar"
                )
            }
        )
    }

    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Abrir Disputa",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Text(
                    text = "¿Está seguro de iniciar una disputa? Esto pausará el proceso de intercambio de divisas y un administrador intervendrá para verificar los comprobantes de pago de ambas partes.",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisputeDialog = false
                        scope.launch {
                            toastState.show(
                                message = "Disputa iniciada correctamente. Soporte revisará el caso.",
                                variant = ToastVariant.Success
                            )
                        }
                    },
                    variant = ButtonVariant.Destructive,
                    text = "Iniciar Disputa"
                )
            },
            dismissButton = {
                Button(
                    onClick = { showDisputeDialog = false },
                    variant = ButtonVariant.Outline,
                    text = "Volver"
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
            snackbarHost = { ToastHost(toastState) },
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
                        onClick = onBack,
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = RikkaIcons.ArrowLeft,
                            contentDescription = "Regresar",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }

                    Text(
                        text = "Estado de Transacción",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Button(
                        onClick = { /* Menu */ },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mas opciones",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    // 1. Rating Form (only when transaction is finished and not yet rated)
                    if (currentState == TransactionState.FINALIZADA && !isTransactionRated) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "¡Intercambio Completado con Éxito!",
                                        variant = TextVariant.Large,
                                        color = RikkaTheme.colors.primary
                                    )
                                    Text(
                                        text = "¿Cómo calificarías a ${if (isSeller) "Mateo Rojas (Comprador)" else "Juan Perez (Vendedor)"}?",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    
                                    var ratingSelection by remember { mutableStateOf(0) }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 1..5) {
                                            IconButton(onClick = { ratingSelection = i }) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = if (i <= ratingSelection) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = "Rating $i",
                                                    tint = if (i <= ratingSelection) RikkaTheme.colors.primary else Color.Gray,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    var commentInput by remember { mutableStateOf("") }
                                    Input(
                                        value = commentInput,
                                        onValueChange = { commentInput = it },
                                        placeholder = "Escribe una reseña (opcional)...",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Button(
                                        enabled = ratingSelection > 0,
                                        onClick = {
                                            scope.launch {
                                                toastState.show(
                                                    message = "¡Gracias por calificar la transacción!",
                                                    variant = ToastVariant.Success
                                                )
                                                isTransactionRated = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "Enviar Calificación"
                                    )
                                }
                            }
                        }
                    }

                    // 2. Hero Dynamic Status Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = currentState.bgColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = currentState.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = currentState.color
                                )

                                Text(
                                    text = currentState.title,
                                    variant = TextVariant.Large,
                                    color = currentState.color
                                )
                            }
                        }
                    }

                    // 3. Operation Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ID DE TRANSACCION",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = transactionId,
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.CurrencyExchange,
                                    label = "Operación",
                                    value = if (type == "Compra") "Compra de USD" else "Venta de USD"
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.Person,
                                    label = "Contraparte",
                                    value = if (isSeller) "Mateo Rojas (Comprador)" else "Juan Perez (Vendedor)"
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.Star,
                                    label = "Tipo de Cambio",
                                    value = "S/ $rate"
                                )

                                if (type == "Compra") {
                                    TransactionBriefRow(
                                        icon = Icons.Default.CheckCircle,
                                        label = "Tú Enviaste (Pago)",
                                        value = "$formattedPen PEN"
                                    )
                                    TransactionBriefRow(
                                        icon = Icons.Default.CheckCircle,
                                        label = "Tú Recibiste",
                                        value = "$formattedUsd USD"
                                    )
                                } else {
                                    TransactionBriefRow(
                                        icon = Icons.Default.CheckCircle,
                                        label = "Tú Enviaste",
                                        value = "$formattedUsd USD"
                                    )
                                    TransactionBriefRow(
                                        icon = Icons.Default.CheckCircle,
                                        label = "Tú Recibiste (Cobro)",
                                        value = "$formattedPen PEN"
                                    )
                                }

                                TransactionBriefRow(
                                    icon = Icons.Default.MoreVert,
                                    label = "Banco Destino",
                                    value = bank
                                )
                            }
                        }
                    }

                    // 4. Stepper Timeline Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Historial del flujo",
                                    variant = TextVariant.Large,
                                    color = RikkaTheme.colors.onBackground
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    val step1Status = TimelineStepStatus.COMPLETED
                                    val step2Status = when (currentState) {
                                        TransactionState.ESPERANDO_ACEPTACION -> TimelineStepStatus.ACTIVE
                                        TransactionState.RECHAZADA -> TimelineStepStatus.FAILED
                                        else -> TimelineStepStatus.COMPLETED
                                    }
                                    val step3Status = when (currentState) {
                                        TransactionState.ESPERANDO_ACEPTACION, TransactionState.RECHAZADA -> TimelineStepStatus.PENDING
                                        TransactionState.EN_PROCESO -> TimelineStepStatus.ACTIVE
                                        else -> TimelineStepStatus.COMPLETED
                                    }
                                    val step4Status = when (currentState) {
                                        TransactionState.FINALIZADA -> TimelineStepStatus.COMPLETED
                                        TransactionState.PAGADA_CON_VOUCHER -> TimelineStepStatus.ACTIVE
                                        else -> TimelineStepStatus.PENDING
                                    }

                                    TimelineStep(
                                        status = step1Status,
                                        title = "Transacción creada",
                                        subtitle = "Monto e instrucciones fijados",
                                        isLast = false
                                    )

                                    TimelineStep(
                                        status = step2Status,
                                        title = if (currentState == TransactionState.RECHAZADA) "Rechazada por el vendedor" else "Aceptada por el vendedor",
                                        subtitle = when (currentState) {
                                            TransactionState.ESPERANDO_ACEPTACION -> "Esperando confirmación del vendedor"
                                            TransactionState.RECHAZADA -> "La operación fue rechazada"
                                            else -> "Operación aceptada"
                                        },
                                        isLast = false
                                    )

                                    TimelineStep(
                                        status = step3Status,
                                        title = "Pago realizado (Voucher enviado)",
                                        subtitleContent = {
                                            if (currentState == TransactionState.ESPERANDO_ACEPTACION || currentState == TransactionState.RECHAZADA) {
                                                Text(
                                                    text = "Pendiente de aceptación de la transacción",
                                                    variant = TextVariant.Small,
                                                    color = Color.Gray
                                                )
                                            } else {
                                                val mine = if (isSeller) sellerVoucherUploaded else buyerVoucherUploaded
                                                val peer = if (isSeller) buyerVoucherUploaded else sellerVoucherUploaded
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Tú: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = if (mine) "Listo (Comprobante subido)" else "Pendiente",
                                                            variant = TextVariant.Small,
                                                            color = if (mine) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Contraparte: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = if (peer) "Listo (Comprobante subido)" else "Pendiente",
                                                            variant = TextVariant.Small,
                                                            color = if (peer) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        isLast = false
                                    )

                                    TimelineStep(
                                        status = step4Status,
                                        title = "Pago verificado por ambas partes",
                                        subtitleContent = {
                                            if (currentState == TransactionState.FINALIZADA) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Tú: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = "Confirmado",
                                                            variant = TextVariant.Small,
                                                            color = Color(0xFF4CAF50)
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Contraparte: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = "Confirmado",
                                                            variant = TextVariant.Small,
                                                            color = Color(0xFF4CAF50)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Fondos liberados. Operación completada con éxito.",
                                                        variant = TextVariant.Small,
                                                        color = Color.Gray
                                                    )
                                                }
                                            } else if (currentState == TransactionState.PAGADA_CON_VOUCHER) {
                                                val mineConf = if (isSeller) sellerConfirmedReceipt else buyerConfirmedReceipt
                                                val peerConf = if (isSeller) buyerConfirmedReceipt else sellerConfirmedReceipt
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Tú: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = if (mineConf) "Confirmado" else "Pendiente",
                                                            variant = TextVariant.Small,
                                                            color = if (mineConf) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Contraparte: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = if (peerConf) "Confirmado" else "Pendiente",
                                                            variant = TextVariant.Small,
                                                            color = if (peerConf) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    text = "Pendiente de verificación del depósito.",
                                                    variant = TextVariant.Small,
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        isLast = true
                                    )
                                }
                            }
                        }
                    }
                }

                // Fixed bottom action section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isSeller) {
                        // Seller View
                        when (currentState) {
                            TransactionState.ESPERANDO_ACEPTACION -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                toastState.show(
                                                    message = "Solicitud aceptada correctamente",
                                                    variant = ToastVariant.Success
                                                )
                                                currentState = TransactionState.EN_PROCESO
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        text = "Aceptar"
                                    )

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                toastState.show(
                                                    message = "Solicitud rechazada correctamente",
                                                    variant = ToastVariant.Destructive
                                                )
                                                currentState = TransactionState.RECHAZADA
                                            }
                                        },
                                        variant = ButtonVariant.Destructive,
                                        modifier = Modifier.weight(1f),
                                        text = "Rechazar"
                                    )
                                }
                            }
                            TransactionState.RECHAZADA -> {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    variant = ButtonVariant.Outline,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Esta transacción ha sido rechazada"
                                )
                            }
                            TransactionState.EN_DISPUTA -> {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    variant = ButtonVariant.Outline,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Esta transacción se encuentra en disputa"
                                )
                            }
                            TransactionState.EN_PROCESO, TransactionState.PAGADA_CON_VOUCHER -> {
                                Button(
                                    onClick = onViewBankDetails,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Proceder con el pago"
                                )
                            }
                            TransactionState.FINALIZADA -> {
                                // No action buttons needed, rating card is shown
                            }
                        }
                    } else {
                        // Buyer View
                        when (currentState) {
                            TransactionState.ESPERANDO_ACEPTACION -> {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Esperando aceptación de la transacción..."
                                )
                            }
                            TransactionState.RECHAZADA -> {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    variant = ButtonVariant.Outline,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Esta transacción ha sido rechazada"
                                )
                            }
                            TransactionState.EN_DISPUTA -> {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    variant = ButtonVariant.Outline,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Esta transacción se encuentra en disputa"
                                )
                            }
                            TransactionState.EN_PROCESO, TransactionState.PAGADA_CON_VOUCHER -> {
                                Button(
                                    onClick = onViewBankDetails,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Proceder con el pago"
                                )
                            }
                            TransactionState.FINALIZADA -> {
                                // No actions needed, rating card is shown
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionBriefRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = RikkaTheme.colors.primary
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                variant = TextVariant.Small,
                color = Color.Gray
            )
            Text(
                text = value,
                variant = TextVariant.P,
                color = RikkaTheme.colors.onBackground
            )
        }
    }
}

enum class TimelineStepStatus {
    COMPLETED,
    ACTIVE,
    PENDING,
    FAILED
}

@Composable
private fun TimelineStep(
    status: TimelineStepStatus,
    title: String,
    subtitle: String = "",
    subtitleContent: @Composable (() -> Unit)? = null,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon / Indicator circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        color = when (status) {
                            TimelineStepStatus.COMPLETED -> Color(0xFF4CAF50)
                            TimelineStepStatus.ACTIVE -> Color(0xFFFFB74D)
                            TimelineStepStatus.PENDING -> RikkaTheme.colors.muted.copy(alpha = 0.2f)
                            TimelineStepStatus.FAILED -> Color(0xFFD32F2F)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = when (status) {
                        TimelineStepStatus.COMPLETED -> Icons.Default.CheckCircle
                        TimelineStepStatus.ACTIVE -> Icons.Default.HourglassEmpty
                        TimelineStepStatus.PENDING -> Icons.Default.RadioButtonUnchecked
                        TimelineStepStatus.FAILED -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when (status) {
                        TimelineStepStatus.PENDING -> Color.Gray
                        else -> Color.White
                    }
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(
                            color = when (status) {
                                TimelineStepStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                                else -> RikkaTheme.colors.muted.copy(alpha = 0.2f)
                            }
                        )
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                variant = TextVariant.P,
                color = when (status) {
                    TimelineStepStatus.PENDING -> Color.Gray
                    else -> RikkaTheme.colors.onBackground
                }
            )

            if (subtitleContent != null) {
                subtitleContent()
            } else if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
            }
        }
    }
}
