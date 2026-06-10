package com.paoloesan.proyectomobile.presentation.transaction

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
import android.net.Uri
import android.content.Context
import coil3.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(
    isSeller: Boolean = false,
    transactionId: String = "TX001",
    amount: String = "100.00",
    rate: String = "3.85",
    bank: String = "BCP - 191-99882211-0-45 (PEN)",
    type: String = "Compra",
    status: String = "Pendiente",
    uploaded: Boolean = false,
    currency: String = "USD",
    navController: NavController,
    onBack: () -> Unit,
    onChat: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var buyerVoucherUploaded by remember { mutableStateOf(!isSeller && uploaded || status == "Finalizado" || status == "Finalizada") }
    var sellerVoucherUploaded by remember { mutableStateOf(isSeller && uploaded || status == "Finalizado" || status == "Finalizada") }
    var buyerConfirmedReceipt by remember { mutableStateOf(status == "Finalizado" || status == "Finalizada") }
    var sellerConfirmedReceipt by remember { mutableStateOf(status == "Finalizado" || status == "Finalizada") }
    var currentTransactionStatus by remember { mutableStateOf(status) }

    var showDisputeDialog by remember { mutableStateOf(false) }
    var disputeReason by remember { mutableStateOf("") }
    var showVoucherDialog by remember { mutableStateOf(false) }
    var selectedVoucherTitle by remember { mutableStateOf("") }
    var selectedVoucherAmount by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()
    var showUploadSheet by remember { mutableStateOf(false) }
    var voucherUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val validationResult = validateVoucher(context, it)
            if (validationResult.isValid) {
                voucherUri = it
                errorMessage = ""
                selectedFileName = getFileNameFromUri(context, it)
            } else {
                voucherUri = null
                errorMessage = validationResult.errorMessage
                selectedFileName = ""
            }
        }
    }

    // Simulation LaunchedEffect
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

    // Details of what needs to be transferred:
    val isTransferringPen = !isSeller
    val displayAmount = if (isTransferringPen) "$formattedPen PEN" else "$formattedUsd USD"
    val displayCurrency = if (isTransferringPen) "PEN" else "USD"
    val displayTitular = if (isSeller) "Mateo Rojas (Comprador)" else "Juan Perez (Vendedor)"

    // Parse bank details
    val bankParts = bank.split(" - ")
    val parsedBankName = bankParts.getOrNull(0) ?: "BCP"
    val parsedAccountNumber = bankParts.getOrNull(1)?.replace(Regex("\\(.*\\)"), "")?.trim() ?: "191-99882211-0-45"
    val parsedCCI = if (parsedBankName.contains("BCP")) "002-$parsedAccountNumber-45" else "003-$parsedAccountNumber-12"

    // Dialog: Voucher details
    if (showVoucherDialog) {
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
                        text = "Comprobante de transferencia subido:",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
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
                                text = selectedVoucherTitle,
                                color = Color.Black,
                                variant = TextVariant.P,
                                style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "MONTO: $selectedVoucherAmount",
                                color = Color(0xFF2E7D32),
                                variant = TextVariant.Large
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

    // Dialog: Dispute details
    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = {
                showDisputeDialog = false
                disputeReason = ""
            },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Abrir Disputa",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "¿Está seguro de iniciar una disputa? Esto pausará el proceso de intercambio de divisas y un administrador intervendrá para verificar los comprobantes de pago de ambas partes.",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Motivo de la disputa (Obligatorio)",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                    )
                    Input(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        placeholder = "Describa detalladamente el problema...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisputeDialog = false
                        currentTransactionStatus = "En disputa"
                        disputeReason = ""
                        scope.launch {
                            toastState.show(
                                message = "Disputa iniciada correctamente. Soporte revisará el caso.",
                                variant = ToastVariant.Success
                            )
                            kotlinx.coroutines.delay(1000)
                            navController.navigate("transactionStatus/$transactionId?isSeller=$isSeller&amount=$amount&rate=$rate&bank=$bank&type=$type&status=En disputa&uploaded=${buyerVoucherUploaded || sellerVoucherUploaded}&currency=$currency&isRated=false") {
                                popUpTo("transactionStatus/$transactionId?isSeller=$isSeller&amount=$amount&rate=$rate&bank=$bank&type=$type&status=$status") {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    enabled = disputeReason.isNotBlank(),
                    variant = ButtonVariant.Destructive,
                    text = "Iniciar Disputa"
                )
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDisputeDialog = false
                        disputeReason = ""
                    },
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
                        text = "Detalles de Pago",
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                // Scrollable main content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // Huge outstanding transfer amount Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "MONTO EXACTO A TRANSFERIR",
                                    variant = TextVariant.Small,
                                    color = Color.Gray
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = displayAmount,
                                        variant = TextVariant.H2,
                                        color = RikkaTheme.colors.primary
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(displayAmount.replace(" PEN", "").replace(" USD", "").trim()))
                                            scope.launch {
                                                toastState.show(
                                                    message = "Monto copiado: $displayAmount",
                                                    variant = ToastVariant.Success
                                                )
                                            }
                                        },
                                        variant = ButtonVariant.Ghost,
                                        size = ButtonSize.Icon
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copiar monto",
                                            modifier = Modifier.size(16.dp),
                                            tint = RikkaTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Copyable details bank container Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                CopyableDetailRow(
                                    icon = Icons.Default.AccountBalance,
                                    label = "BANCO",
                                    value = parsedBankName,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.ContentCopy,
                                    label = "NUMERO DE CUENTA",
                                    value = parsedAccountNumber,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.ContentCopy,
                                    label = "CCI",
                                    value = parsedCCI,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.Person,
                                    label = "TITULAR DE LA CUENTA",
                                    value = displayTitular,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.AttachMoney,
                                    label = "MONEDA",
                                    value = displayCurrency,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = false
                                )
                            }
                        }
                    }

                    // Vouchers Card (New/Requested)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Comprobantes de Pago",
                                    variant = TextVariant.Large,
                                    color = RikkaTheme.colors.onBackground
                                )

                                val mineUploaded = if (isSeller) sellerVoucherUploaded else buyerVoucherUploaded
                                val peerUploaded = if (isSeller) buyerVoucherUploaded else sellerVoucherUploaded

                                // Mine Voucher
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Tú Comprobante", variant = TextVariant.P, color = RikkaTheme.colors.onBackground)
                                        Text(
                                            text = if (mineUploaded) "Subido (Listo)" else "Pendiente de subir",
                                            variant = TextVariant.Small,
                                            color = if (mineUploaded) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                        )
                                    }

                                    if (mineUploaded) {
                                        Button(
                                            onClick = {
                                                selectedVoucherTitle = if (isSeller) "Tu Comprobante (USD)" else "Tu Comprobante (PEN)"
                                                selectedVoucherAmount = if (isSeller) "$formattedUsd USD" else "$formattedPen PEN"
                                                showVoucherDialog = true
                                            },
                                            variant = ButtonVariant.Outline,
                                            size = ButtonSize.Sm,
                                            text = "Ver"
                                        )
                                    } else {
                                        Button(
                                            onClick = {
                                                voucherUri = null
                                                selectedFileName = ""
                                                errorMessage = ""
                                                showUploadSheet = true
                                            },
                                            size = ButtonSize.Sm,
                                            text = "Subir"
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                                )

                                // Peer Voucher
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Comprobante de Contraparte", variant = TextVariant.P, color = RikkaTheme.colors.onBackground)
                                        Text(
                                            text = if (peerUploaded) "Subido (Listo)" else "Pendiente de recibir",
                                            variant = TextVariant.Small,
                                            color = if (peerUploaded) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                        )
                                    }

                                    if (peerUploaded) {
                                        Button(
                                            onClick = {
                                                selectedVoucherTitle = if (isSeller) "Comprobante del Comprador (PEN)" else "Comprobante del Vendedor (USD)"
                                                selectedVoucherAmount = if (isSeller) "$formattedPen PEN" else "$formattedUsd USD"
                                                showVoucherDialog = true
                                            },
                                            variant = ButtonVariant.Outline,
                                            size = ButtonSize.Sm,
                                            text = "Ver"
                                        )
                                    } else {
                                        Button(
                                            onClick = { },
                                            enabled = false,
                                            size = ButtonSize.Sm,
                                            variant = ButtonVariant.Outline,
                                            text = "Esperando..."
                                        )
                                    }
                                }
                            }
                        }
                    }


                }

                // Anchored bottom action area
                val mineUploaded = if (isSeller) sellerVoucherUploaded else buyerVoucherUploaded
                val peerUploaded = if (isSeller) buyerVoucherUploaded else sellerVoucherUploaded
                val isReadyToConfirm = mineUploaded && peerUploaded && currentTransactionStatus != "Finalizado"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isReadyToConfirm) {
                        val confirmed = if (isSeller) sellerConfirmedReceipt else buyerConfirmedReceipt
                        Button(
                            onClick = {
                                scope.launch {
                                    if (isSeller) {
                                        sellerConfirmedReceipt = true
                                    } else {
                                        buyerConfirmedReceipt = true
                                    }
                                    toastState.show("Recepción confirmada con éxito", ToastVariant.Success)
                                    kotlinx.coroutines.delay(1000)
                                    currentTransactionStatus = "Finalizado"
                                    navController.navigate("transactionStatus/$transactionId?isSeller=$isSeller&amount=$amount&rate=$rate&bank=$bank&type=$type&status=Finalizado&uploaded=true&isRated=false") {
                                        popUpTo("transactionStatus/$transactionId?isSeller=$isSeller&amount=$amount&rate=$rate&bank=$bank&type=$type&status=$status") {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            enabled = !confirmed,
                            modifier = Modifier.fillMaxWidth(),
                            text = if (confirmed) "Recepción Confirmada" else "Confirmar Recepción"
                        )
                    } else if (currentTransactionStatus == "Finalizado") {
                        Button(
                            onClick = { onBack() },
                            modifier = Modifier.fillMaxWidth(),
                            text = "Volver al Estado de Transacción"
                        )
                    } else {
                        Button(
                            onClick = {
                                if (!mineUploaded) {
                                    voucherUri = null
                                    selectedFileName = ""
                                    errorMessage = ""
                                    showUploadSheet = true
                                }
                            },
                            enabled = !mineUploaded,
                            modifier = Modifier.fillMaxWidth(),
                            text = if (mineUploaded) "Esperando comprobante de contraparte..." else "Subir mi comprobante de pago"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onChat,
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f),
                            text = "Chat"
                        )

                        Button(
                            onClick = { showDisputeDialog = true },
                            variant = ButtonVariant.Destructive,
                            modifier = Modifier.weight(1f),
                            text = "Abrir disputa"
                        )
                    }
                }
            }
        }
    }

    if (showUploadSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUploadSheet = false },
            sheetState = sheetState,
            containerColor = RikkaTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Subir Comprobante de Pago",
                    variant = TextVariant.H2,
                    color = RikkaTheme.colors.onBackground,
                    modifier = Modifier.fillMaxWidth()
                )

                if (voucherUri == null) {
                    Text(
                        text = "Debe adjuntar un comprobante de pago",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.destructive,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Vista previa del archivo",
                    variant = TextVariant.Small,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )

                // Premium Card preview (clickable to pick image)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { filePickerLauncher.launch("image/*") }
                            .border(
                                width = 2.dp,
                                color = when {
                                    errorMessage.isNotEmpty() -> RikkaTheme.colors.destructive
                                    voucherUri != null -> RikkaTheme.colors.primary
                                    else -> RikkaTheme.colors.muted.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            errorMessage.isNotEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error en comprobante",
                                        modifier = Modifier.size(48.dp),
                                        tint = RikkaTheme.colors.destructive
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = errorMessage,
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.destructive,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            voucherUri == null -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Comprobante",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Suba un comprobante de pago",
                                        variant = TextVariant.P,
                                        color = Color.Gray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = voucherUri,
                                        contentDescription = "Comprobante Seleccionado",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(vertical = 6.dp, horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = selectedFileName,
                                                variant = TextVariant.Small,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Listo para enviar (Toca para cambiar)",
                                                variant = TextVariant.Small,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (voucherUri != null) {
                            if (isSeller) {
                                sellerVoucherUploaded = true
                            } else {
                                buyerVoucherUploaded = true
                            }
                            showUploadSheet = false
                            scope.launch {
                                toastState.show("Comprobante enviado con éxito", ToastVariant.Success)
                            }
                        }
                    },
                    enabled = voucherUri != null && errorMessage.isEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    text = "Confirmar Envío"
                )


                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CopyableDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    toastState: zed.rainxch.rikkaui.components.ui.toast.ToastHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = RikkaTheme.colors.primary.copy(alpha = 0.08f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = RikkaTheme.colors.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
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

            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                    scope.launch {
                        toastState.show(
                            message = "Copiado al portapapeles",
                            variant = ToastVariant.Success
                        )
                    }
                },
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Icon
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar $label",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
            )
        }
    }
}

private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024
private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png")
private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png")

private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    return when {
        uri.scheme == "content" -> {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex("_display_name")
                if (nameIndex != -1 && it.moveToFirst()) {
                    it.getString(nameIndex)
                } else {
                    "archivo"
                }
            } ?: "archivo"
        }
        uri.path != null -> {
            uri.path!!.substringAfterLast("/")
        }
        else -> "archivo"
    }
}

private fun validateVoucher(context: Context, uri: Uri): ValidationResult {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""

        val fileName = getFileNameFromUri(context, uri)
        val extension = fileName.substringAfterLast(".").lowercase()

        if (!ALLOWED_MIME_TYPES.contains(mimeType) && !ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Formato de archivo no permitido. Solo se aceptan JPG y PNG."
            )
        }

        val fileSize = contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        } ?: 0L

        if (fileSize > MAX_FILE_SIZE_BYTES) {
            val sizeMB = String.format(java.util.Locale.US, "%.2f", fileSize / (1024f * 1024f))
            return ValidationResult(
                isValid = false,
                errorMessage = "El archivo es demasiado grande ($sizeMB MB). Tamaño máximo: 5 MB."
            )
        }

        ValidationResult(isValid = true)
    } catch (e: Exception) {
        ValidationResult(
            isValid = false,
            errorMessage = "Error al validar el archivo: ${e.message}"
        )
    }
}
