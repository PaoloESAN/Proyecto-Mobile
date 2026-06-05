package com.paoloesan.proyectomobile.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

enum class TransactionStatus {
    PENDING, IN_PROCESS, PAID, FINISHED, DISPUTE, CANCELLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmPaymentScreen(navController: NavController) {

    var transactionStatus by remember { mutableStateOf(TransactionStatus.PAID) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Datos simulados del pagador
    val payerName = "Carlos Rodríguez"
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
            title = { Text("Confirmar recepción") },
            text = { Text("¿Confirmas que recibiste el pago de $amount?") },
            confirmButton = {
                Button(onClick = {
                    transactionStatus = TransactionStatus.FINISHED
                    showConfirmDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Transacción finalizada correctamente")
                    }
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog abrir disputa
    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = { Text("Abrir Disputa") },
            text = { Text("¿Deseas abrir una disputa para esta transacción? Un administrador revisará el caso.") },
            confirmButton = {
                Button(
                    onClick = {
                        transactionStatus = TransactionStatus.DISPUTE
                        showDisputeDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Disputa abierta correctamente")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Abrir disputa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDisputeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Pago") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Estado actual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (transactionStatus) {
                        TransactionStatus.PAID -> MaterialTheme.colorScheme.primaryContainer
                        TransactionStatus.FINISHED -> MaterialTheme.colorScheme.secondaryContainer
                        TransactionStatus.DISPUTE -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (transactionStatus) {
                            TransactionStatus.FINISHED -> Icons.Default.CheckCircle
                            TransactionStatus.DISPUTE -> Icons.Default.Warning
                            else -> Icons.Default.CheckCircle
                        },
                        contentDescription = null
                    )
                    Column {
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
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Actualizado recientemente",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Voucher simulado
            Text("Comprobante de pago", style = MaterialTheme.typography.titleMedium)
            if (hasVoucher) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧾 Voucher adjunto",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No hay comprobante adjunto aún",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Datos del pagador
            Text("Datos del pagador", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nombre:", style = MaterialTheme.typography.bodyMedium)
                        Text(payerName, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Banco:", style = MaterialTheme.typography.bodyMedium)
                        Text(payerBank, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cuenta:", style = MaterialTheme.typography.bodyMedium)
                        Text(payerAccount, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monto:", style = MaterialTheme.typography.bodyMedium)
                        Text(amount, style = MaterialTheme.typography.titleMedium)
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("T.C.:", style = MaterialTheme.typography.bodyMedium)
                        Text(exchangeRate, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones
            if (transactionStatus == TransactionStatus.PAID) {
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar pago")
                }
                OutlinedButton(
                    onClick = { showDisputeDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Abrir disputa")
                }
            } else {
                Text(
                    text = when (transactionStatus) {
                        TransactionStatus.FINISHED -> "✅ Transacción finalizada correctamente"
                        TransactionStatus.DISPUTE -> "⚠ Disputa abierta, un administrador revisará el caso"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (transactionStatus) {
                        TransactionStatus.FINISHED -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}