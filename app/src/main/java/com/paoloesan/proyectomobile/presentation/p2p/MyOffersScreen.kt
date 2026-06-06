package com.paoloesan.proyectomobile.presentation.p2p
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(navController: NavController) {

    var offers by remember {
        mutableStateOf(
            listOf(
                MyOffer("1", "Compra", "USD", 150.0, 3.75, 50.0, 200.0, "BCP"),
                MyOffer("2", "Venta", "PEN", 500.0, 1.0, 10.0, 500.0, "Yape"),
                MyOffer("3", "Compra", "USD", 300.0, 3.76, 100.0, 400.0, "Interbank", hasActiveTransaction = true),
                MyOffer("4", "Venta", "USD", 200.0, 3.74, 50.0, 250.0, "BCP")
            )
        )
    }

    var showCancelDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<MyOffer?>(null) }

    // Estados para edición
    var editAmount by remember { mutableStateOf("") }
    var editRate by remember { mutableStateOf("") }
    var editMinLimit by remember { mutableStateOf("") }
    var editMaxLimit by remember { mutableStateOf("") }
    var editPaymentMethod by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog de cancelar
    if (showCancelDialog && selectedOffer != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar Oferta") },
            text = { Text("¿Estás seguro de cancelar esta oferta?") },
            confirmButton = {
                Button(onClick = {
                    offers = offers.filter { it.id != selectedOffer!!.id }
                    showCancelDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Oferta cancelada correctamente")
                    }
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de editar
    if (showEditDialog && selectedOffer != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Oferta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) editAmount = it },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editRate,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) editRate = it },
                        label = { Text("Tipo de cambio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editMinLimit,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) editMinLimit = it },
                        label = { Text("Monto mínimo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editMaxLimit,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) editMaxLimit = it },
                        label = { Text("Monto máximo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPaymentMethod,
                        onValueChange = { editPaymentMethod = it },
                        label = { Text("Método de pago") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val minVal = editMinLimit.toDoubleOrNull()
                    val maxVal = editMaxLimit.toDoubleOrNull()
                    if (minVal != null && maxVal != null && minVal > maxVal) {
                        scope.launch {
                            snackbarHostState.showSnackbar("El monto mínimo no puede ser mayor al máximo")
                        }
                    } else {
                        offers = offers.map {
                            if (it.id == selectedOffer!!.id) {
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
                            snackbarHostState.showSnackbar("Oferta actualizada correctamente")
                        }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Ofertas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("publish_offer") }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Publicar oferta")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (offers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes ofertas activas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(offers, key = { it.id }) { offer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // Tipo y moneda
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${offer.type} - ${offer.currency}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (offer.type == "Compra")
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "${offer.currency} ${offer.amount}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "T.C.: ${offer.rate}  |  Método: ${offer.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Límites: Min ${offer.minLimit} / Max ${offer.maxLimit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (offer.hasActiveTransaction) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠ Tiene transacción activa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Botón Editar
                                OutlinedButton(
                                    onClick = {
                                        selectedOffer = offer
                                        editAmount = offer.amount.toString()
                                        editRate = offer.rate.toString()
                                        editMinLimit = offer.minLimit.toString()
                                        editMaxLimit = offer.maxLimit.toString()
                                        editPaymentMethod = offer.paymentMethod
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }

                                // Botón Cancelar
                                Button(
                                    onClick = {
                                        if (offer.hasActiveTransaction) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("No se puede cancelar: tiene transacción activa")
                                            }
                                        } else {
                                            selectedOffer = offer
                                            showCancelDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}