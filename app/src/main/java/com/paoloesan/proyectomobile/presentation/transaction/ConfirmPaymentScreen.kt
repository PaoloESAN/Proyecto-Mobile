package com.paoloesan.proyectomobile.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

enum class TransactionStatus {
    PENDING, IN_PROCESS, PAID, FINISHED, DISPUTE, CANCELLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmPaymentScreen(navController: NavController) {
    var transactionStatus by remember { mutableStateOf(TransactionStatus.PAID) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Datos simulados del pagador
    val payerName = "Carlos Rodriguez"
    val payerBank = "BCP"
    val payerAccount = "191-12345678-0-12"
    val amount = "3,850.00 PEN"
    val exchangeRate = "1 USD = 3.85 PEN"
    val hasVoucher = transactionStatus == TransactionStatus.PAID ||
            transactionStatus == TransactionStatus.FINISHED

    // Dialog confirmar pago
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Confirmar recepcion",
                    variant = TextVariant.H2,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Text(
                    text = "¿Confirmas que recibiste el pago de $amount?",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        transactionStatus = TransactionStatus.FINISHED
                        showConfirmDialog = false
                        scope.launch {
                            toastState.show(
                                message = "Transaccion finalizada correctamente",
                                variant = ToastVariant.Success
                            )
                        }
                    },
                    text = "Confirmar"
                )
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false },
                    variant = ButtonVariant.Outline,
                    text = "Cancelar"
                )
            }
        )
    }

    // Dialog abrir disputa
    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Abrir Disputa",
                    variant = TextVariant.H2,
                    color = RikkaTheme.colors.destructive
                )
            },
            text = {
                Text(
                    text = "¿Deseas abrir una disputa para esta transaccion? Un administrador revisara el caso.",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        transactionStatus = TransactionStatus.DISPUTE
                        showDisputeDialog = false
                        scope.launch {
                            toastState.show(
                                message = "Disputa abierta correctamente",
                                variant = ToastVariant.Destructive
                            )
                        }
                    },
                    text = "Abrir disputa"
                )
            },
            dismissButton = {
                Button(
                    onClick = { showDisputeDialog = false },
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
                        onClick = { navController.popBackStack() },
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
                        text = "Confirmar Pago",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Estado actual
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val statusTextColor = when (transactionStatus) {
                            TransactionStatus.FINISHED -> Color(0xFF2E7D32)
                            TransactionStatus.DISPUTE -> Color(0xFFC62828)
                            else -> RikkaTheme.colors.primary
                        }
                        val statusIcon = when (transactionStatus) {
                            TransactionStatus.FINISHED -> Icons.Default.CheckCircle
                            TransactionStatus.DISPUTE -> Icons.Default.Warning
                            else -> Icons.Default.CheckCircle
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = statusTextColor
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Estado: ${
                                        when (transactionStatus) {
                                            TransactionStatus.PENDING -> "Pendiente"
                                            TransactionStatus.IN_PROCESS -> "En proceso"
                                            TransactionStatus.PAID -> "Pagado"
                                            TransactionStatus.FINISHED -> "Finalizado"
                                            TransactionStatus.DISPUTE -> "En disputa"
                                            TransactionStatus.CANCELLED -> "Cancelado"
                                        }
                                    }",
                                    variant = TextVariant.Large,
                                    color = statusTextColor
                                )

                                Text(
                                    text = "Actualizado recientemente",
                                    variant = TextVariant.Small,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Voucher simulado
                    Text(
                        text = "Comprobante de pago",
                        variant = TextVariant.Small,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (hasVoucher) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(
                                        color = RikkaTheme.colors.primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Comprobante",
                                        modifier = Modifier.size(40.dp),
                                        tint = RikkaTheme.colors.primary
                                    )
                                    Text(
                                        text = "Voucher adjunto",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.primary
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(
                                        color = RikkaTheme.colors.muted.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay comprobante adjunto aun",
                                    variant = TextVariant.P,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Datos del pagador
                    Text(
                        text = "Datos del pagador",
                        variant = TextVariant.Small,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            PayerDetailRow(
                                icon = Icons.Default.Person,
                                label = "Nombre",
                                value = payerName,
                                showDivider = true
                            )
                            PayerDetailRow(
                                icon = Icons.Default.AccountBalance,
                                label = "Banco",
                                value = payerBank,
                                showDivider = true
                            )
                            PayerDetailRow(
                                icon = Icons.Default.ContentCopy,
                                label = "Cuenta",
                                value = payerAccount,
                                showDivider = true
                            )
                            PayerDetailRow(
                                icon = Icons.Default.AttachMoney,
                                label = "Monto",
                                value = amount,
                                highlight = true,
                                showDivider = true
                            )
                            PayerDetailRow(
                                icon = Icons.Default.CurrencyExchange,
                                label = "T.C.",
                                value = exchangeRate,
                                showDivider = false
                            )
                        }
                    }
                }

                // Botones o Estado Final
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (transactionStatus == TransactionStatus.PAID) {
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            text = "Confirmar pago"
                        )

                        Button(
                            onClick = { showDisputeDialog = true },
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.fillMaxWidth(),
                            text = "Abrir disputa"
                        )
                    } else {
                        val finalMsg = when (transactionStatus) {
                            TransactionStatus.FINISHED -> "Transaccion finalizada correctamente"
                            TransactionStatus.DISPUTE -> "Disputa abierta, un administrador revisara el caso"
                            else -> ""
                        }
                        val finalColor = when (transactionStatus) {
                            TransactionStatus.FINISHED -> Color(0xFF2E7D32)
                            else -> Color(0xFFC62828)
                        }

                        if (finalMsg.isNotEmpty()) {
                            Text(
                                text = finalMsg,
                                variant = TextVariant.P,
                                color = finalColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayerDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false,
    showDivider: Boolean = true
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
                        color = RikkaTheme.colors.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = RikkaTheme.colors.primary,
                    modifier = Modifier.fillMaxSize()
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
                    variant = if (highlight) TextVariant.Large else TextVariant.P,
                    color = if (highlight) RikkaTheme.colors.primary else RikkaTheme.colors.onBackground
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