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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import zed.rainxch.rikkaui.components.ui.skeleton.Skeleton
import zed.rainxch.rikkaui.components.ui.skeleton.SkeletonAnimation
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
    EN_PROCESO("En proceso", Color(0xFF1E88E5), Icons.Default.AccessTime, Color(0xFFE3F2FD)),
    PAGADA_CON_VOUCHER("En proceso", Color(0xFF1E88E5), Icons.Default.AccessTime, Color(0xFFE3F2FD)),
    FINALIZADA("Finalizada", Color(0xFF2E7D32), Icons.Default.CheckCircle, Color(0xFFE8F5E9)),
    RECHAZADA("Rechazada", Color(0xFFD32F2F), Icons.Default.Cancel, Color(0xFFFFEBEE)),
    EN_DISPUTA("En disputa", Color(0xFFD32F2F), Icons.Default.Cancel, Color(0xFFFFEBEE))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionStatusScreen(
    transactionId: Int,
    onBack: () -> Unit,
    onViewBankDetails: (transactionId: Int, isSeller: Boolean) -> Unit,
    onChat: (transactionId: Int) -> Unit = {},
    viewModel: TransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()

    var showDisputeDialog by remember { mutableStateOf(false) }
    var ratingSelection by remember { mutableStateOf(0) }
    var commentInput by remember { mutableStateOf("") }

    // Inicializar con el transactionId real
    LaunchedEffect(transactionId) {
        viewModel.initialize(transactionId)
    }

    // Mostrar mensajes de éxito/error
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            scope.launch { toastState.show(message = msg, variant = ToastVariant.Success) }
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            scope.launch { toastState.show(message = msg, variant = ToastVariant.Destructive) }
            viewModel.clearMessages()
        }
    }

    // Mapeo de estado de Supabase → TransactionState UI
    val isSeller = uiState.isSeller
    val currentState = when (uiState.transactionStatus) {
        "Pendiente", "En Proceso", "Pagado" -> TransactionState.EN_PROCESO
        "Finalizado", "Finalizada" -> TransactionState.FINALIZADA
        "Disputa" -> TransactionState.EN_DISPUTA
        "Cancelado" -> TransactionState.RECHAZADA
        else -> TransactionState.EN_PROCESO
    }

    val tx = uiState.transaction
    val offer = uiState.offer

    // Cálculos de montos universales
    val rateDouble = tx?.tipoCambioAplicado ?: offer?.price ?: 3.85
    val destCurrency = uiState.destCurrency.ifEmpty { offer?.monedaRecibo ?: "PEN" }

    var montoEnvio = 0.0
    var monedaEnvio = "PEN"
    var montoRecepcion = 0.0
    var monedaRecepcion = "USD"

    if (offer != null) {
        if (destCurrency.equals(offer.monedaTengo, ignoreCase = true)) {
            montoEnvio = offer.montoTengo
            monedaEnvio = offer.monedaTengo
            montoRecepcion = offer.montoRecibo
            monedaRecepcion = offer.monedaRecibo
        } else {
            montoEnvio = offer.montoRecibo
            monedaEnvio = offer.monedaRecibo
            montoRecepcion = offer.montoTengo
            monedaRecepcion = offer.monedaTengo
        }
    } else {
        val amt = tx?.amount ?: 0.0
        montoEnvio = amt * rateDouble
        monedaEnvio = "PEN"
        montoRecepcion = amt
        monedaRecepcion = "USD"
    }

    val formattedEnvio = String.format(java.util.Locale.US, "%,.2f", montoEnvio)
    val formattedRecepcion = String.format(java.util.Locale.US, "%,.2f", montoRecepcion)
    val txIdStr = transactionId.toString()

    // ── Diálogo de disputa ────────────────────────────────────────────────────
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
                    text = "¿Está seguro de iniciar una disputa? Esto pausará el proceso de intercambio y un administrador intervendrá para verificar los comprobantes de ambas partes.",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisputeDialog = false
                        viewModel.abrirDisputa()
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
                        text = "Transacción #$transactionId",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Button(
                        onClick = { onChat(transactionId) },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Chat",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }
                }
            }
        ) { innerPadding ->

            // Pantalla de carga
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Top Card skeleton
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, RikkaTheme.colors.muted.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Skeleton(
                            modifier = Modifier.fillMaxWidth(0.5f).height(20.dp),
                            animation = SkeletonAnimation.Shimmer
                        )
                        Skeleton(
                            modifier = Modifier.fillMaxWidth().height(16.dp),
                            animation = SkeletonAnimation.Shimmer
                        )
                        Skeleton(
                            modifier = Modifier.fillMaxWidth(0.8f).height(16.dp),
                            animation = SkeletonAnimation.Shimmer
                        )
                    }

                    // Timeline items skeleton
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        repeat(3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Skeleton(
                                    modifier = Modifier.size(32.dp),
                                    shape = RikkaTheme.shapes.full,
                                    animation = SkeletonAnimation.Shimmer
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Skeleton(
                                        modifier = Modifier.fillMaxWidth(0.4f).height(16.dp),
                                        animation = SkeletonAnimation.Shimmer
                                    )
                                    Skeleton(
                                        modifier = Modifier.fillMaxWidth(0.7f).height(12.dp),
                                        animation = SkeletonAnimation.Shimmer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom Button skeleton
                    Skeleton(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        animation = SkeletonAnimation.Shimmer
                    )
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() }
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
                    // 1. Rating Form (solo cuando está finalizada y no calificada)
                    if (currentState == TransactionState.FINALIZADA && !uiState.yaCalificado) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
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
                                        text = "¿Cómo calificarías a ${uiState.peerName}?",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

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

                                    Input(
                                        value = commentInput,
                                        onValueChange = { commentInput = it },
                                        placeholder = "Escribe una reseña (opcional)...",
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        enabled = ratingSelection > 0,
                                        onClick = {
                                            viewModel.calificar(ratingSelection, commentInput)
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
                        Card(modifier = Modifier.fillMaxWidth()) {
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
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "ID DE TRANSACCIÓN", variant = TextVariant.Small, color = Color.Gray)
                                    Text(text = "#$txIdStr", variant = TextVariant.Small, color = RikkaTheme.colors.onBackground)
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
                                    value = "${offer?.tipoOperacion ?: "Compra"} de ${offer?.monedaTengo ?: "USD"}"
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.Person,
                                    label = "Contraparte",
                                    value = uiState.peerName
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.Star,
                                    label = "Tipo de Cambio",
                                    value = "S/ ${String.format("%.2f", rateDouble)}"
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.CheckCircle,
                                    label = if (!isSeller) "Tú Envías (Pago)" else "Tú Envías",
                                    value = "$formattedEnvio $monedaEnvio"
                                )
                                TransactionBriefRow(
                                    icon = Icons.Default.CheckCircle,
                                    label = if (!isSeller) "Tú Recibes" else "Tú Recibes (Cobro)",
                                    value = "$formattedRecepcion $monedaRecepcion"
                                )

                                if (uiState.destBankName.isNotEmpty()) {
                                    TransactionBriefRow(
                                        icon = Icons.Default.MoreVert,
                                        label = "Banco Destino",
                                        value = "${uiState.destBankName} - ${uiState.destAccountNumber}"
                                    )
                                }
                            }
                        }
                    }

                    // 4. Stepper Timeline Card
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
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
                                    val step2Status = TimelineStepStatus.COMPLETED
                                    
                                    val step3Status = when {
                                         currentState == TransactionState.FINALIZADA || 
                                         (uiState.myVoucherUploaded && uiState.peerVoucherUploaded) -> TimelineStepStatus.COMPLETED
                                         
                                         currentState == TransactionState.RECHAZADA || 
                                         currentState == TransactionState.EN_DISPUTA -> TimelineStepStatus.FAILED
                                         
                                         uiState.myVoucherUploaded || uiState.peerVoucherUploaded || currentState == TransactionState.EN_PROCESO -> TimelineStepStatus.ACTIVE
                                         
                                         else -> TimelineStepStatus.PENDING
                                     }
                                     
                                     val step4Status = when {
                                         currentState == TransactionState.FINALIZADA -> TimelineStepStatus.COMPLETED
                                         
                                         (uiState.myVoucherUploaded && uiState.peerVoucherUploaded) -> TimelineStepStatus.ACTIVE
                                         
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
                                        title = "Aceptada — En proceso",
                                        subtitle = "Ambas partes deben realizar sus transferencias",
                                        isLast = false
                                    )

                                    TimelineStep(
                                        status = step3Status,
                                        title = "Pago realizado (Voucher enviado)",
                                        subtitleContent = {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = "Tú: ", variant = TextVariant.Small, color = Color.Gray)
                                                    Text(
                                                        text = if (uiState.myVoucherUploaded) "Comprobante subido ✓" else "Pendiente",
                                                        variant = TextVariant.Small,
                                                        color = if (uiState.myVoucherUploaded) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = "Contraparte: ", variant = TextVariant.Small, color = Color.Gray)
                                                    Text(
                                                        text = if (uiState.peerVoucherUploaded) "Comprobante subido ✓" else "Pendiente",
                                                        variant = TextVariant.Small,
                                                        color = if (uiState.peerVoucherUploaded) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                                    )
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
                                                        Text(text = "Confirmado ✓", variant = TextVariant.Small, color = Color(0xFF4CAF50))
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Contraparte: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(text = "Confirmado ✓", variant = TextVariant.Small, color = Color(0xFF4CAF50))
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Operación completada con éxito.",
                                                        variant = TextVariant.Small,
                                                        color = Color.Gray
                                                    )
                                                }
                                            } else if (step4Status == TimelineStepStatus.ACTIVE) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Tú: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = if (uiState.myConfirmed) "Confirmado ✓" else "Pendiente",
                                                            variant = TextVariant.Small,
                                                            color = if (uiState.myConfirmed) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "Contraparte: ", variant = TextVariant.Small, color = Color.Gray)
                                                        Text(
                                                            text = if (uiState.peerConfirmed) "Confirmado ✓" else "Pendiente",
                                                            variant = TextVariant.Small,
                                                            color = if (uiState.peerConfirmed) Color(0xFF4CAF50) else Color(0xFFD32F2F)
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
                    when (currentState) {
                        TransactionState.EN_PROCESO, TransactionState.PAGADA_CON_VOUCHER -> {
                            // Botón principal: proceder con el pago (ir a BankDetailsScreen)
                            Button(
                                onClick = { onViewBankDetails(transactionId, isSeller) },
                                modifier = Modifier.fillMaxWidth(),
                                text = "Proceder con el pago / Ver comprobantes"
                            )

                            // Botón de chat
                            Button(
                                onClick = { onChat(transactionId) },
                                variant = ButtonVariant.Outline,
                                modifier = Modifier.fillMaxWidth(),
                                text = "Chat"
                            )
                        }

                        TransactionState.ESPERANDO_ACEPTACION -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                text = "Esperando aceptación..."
                            )
                        }

                        TransactionState.RECHAZADA -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                variant = ButtonVariant.Outline,
                                modifier = Modifier.fillMaxWidth(),
                                text = "Transacción cancelada"
                            )
                        }

                        TransactionState.EN_DISPUTA -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                variant = ButtonVariant.Outline,
                                modifier = Modifier.fillMaxWidth(),
                                text = "Transacción en disputa — Esperando resolución"
                            )
                        }

                        TransactionState.FINALIZADA -> {
                            if (uiState.yaCalificado) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    variant = ButtonVariant.Outline,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "✓ Transacción finalizada y calificada"
                                )
                            }
                            // Si no calificó, el form de calificación aparece arriba en el LazyColumn
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

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(text = label, variant = TextVariant.Small, color = Color.Gray)
            Text(text = value, variant = TextVariant.P, color = RikkaTheme.colors.onBackground)
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                Text(text = subtitle, variant = TextVariant.Small, color = Color.Gray)
            }
        }
    }
}
