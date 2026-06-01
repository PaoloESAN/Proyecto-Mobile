package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

data class LocalOffer(
    val type: String,
    val currency: String,
    val amount: String,
    val rate: String,
    val minAmount: String,
    val maxAmount: String,
    val paymentMethod: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishOfferScreen(navController: NavController) {
    var type by remember { mutableStateOf("Compra") }
    var currency by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("BCP") }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }

    var publishedOffers by remember { mutableStateOf(listOf<LocalOffer>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val paymentMethods = listOf("BCP", "Yape", "Interbank")
    val currencies = listOf("USD", "PEN")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Publicar Oferta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Crea una nueva oferta P2P",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Selector Compra/Venta
            item {
                Column {
                    Text("Tipo de transacción", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Compra", "Venta").forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t) }
                            )
                        }
                    }
                }
            }

            // Selector de Moneda
            item {
                Column {
                    Text("Moneda", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { currencyDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(currency)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        currencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr) },
                                onClick = {
                                    currency = curr
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Input de Monto (Solo acepta números enteros)
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            amount = input
                        }
                    },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Input de Tipo de Cambio (Acepta decimales)
            item {
                OutlinedTextField(
                    value = rate,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            rate = input
                        }
                    },
                    label = { Text("Tipo de cambio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Input de Monto Mínimo (Solo números)
            item {
                OutlinedTextField(
                    value = minAmount,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            minAmount = input
                        }
                    },
                    label = { Text("Monto mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Input de Monto Máximo (Solo números)
            item {
                OutlinedTextField(
                    value = maxAmount,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            maxAmount = input
                        }
                    },
                    label = { Text("Monto máximo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Selector de Método de Pago
            item {
                Column {
                    Text("Método de Pago", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { paymentDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(paymentMethod)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = paymentDropdownExpanded,
                        onDismissRequest = { paymentDropdownExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    paymentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Botón Publicar Oferta
            item {
                Button(
                    onClick = {
                        val amountVal = amount.toDoubleOrNull()
                        val rateVal = rate.toDoubleOrNull()
                        val minVal = minAmount.toDoubleOrNull()
                        val maxVal = maxAmount.toDoubleOrNull()

                        if (amount.isBlank() || rate.isBlank() || minAmount.isBlank() || maxAmount.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Todos los campos son obligatorios")
                            }
                        } else if (amountVal == null || amountVal <= 0) {
                            scope.launch {
                                snackbarHostState.showSnackbar("El monto debe ser mayor a 0")
                            }
                        } else if (rateVal == null || rateVal <= 0) {
                            scope.launch {
                                snackbarHostState.showSnackbar("El tipo de cambio debe ser mayor a 0")
                            }
                        } else if (minVal == null || maxVal == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor ingresa montos límites válidos")
                            }
                        } else if (minVal > maxVal) {
                            scope.launch {
                                snackbarHostState.showSnackbar("El monto mínimo no puede ser mayor al máximo")
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Oferta publicada correctamente")
                                val newOffer = LocalOffer(
                                    type = type,
                                    currency = currency,
                                    amount = amount,
                                    rate = rate,
                                    minAmount = minAmount,
                                    maxAmount = maxAmount,
                                    paymentMethod = paymentMethod
                                )
                                publishedOffers = publishedOffers + newOffer
                                // Limpiar campos
                                amount = ""
                                rate = ""
                                minAmount = ""
                                maxAmount = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Publicar oferta")
                }
            }

            if (publishedOffers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ofertas en el mercado",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                items(publishedOffers) { offer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${offer.type} - ${offer.currency}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${offer.currency} ${offer.amount}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("T.C.: ${offer.rate}", style = MaterialTheme.typography.bodySmall)
                                Text("Método: ${offer.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Límites: Min ${offer.minAmount} / Max ${offer.maxAmount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
