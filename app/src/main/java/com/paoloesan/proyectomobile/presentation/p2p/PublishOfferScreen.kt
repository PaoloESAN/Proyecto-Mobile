package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishOfferScreen(navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var paymentMethod by remember { mutableStateOf("BCP") }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crea una nueva oferta P2P",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Selector de Moneda
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

            // Input de Monto
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monto a cambiar") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Input de Tipo de Cambio
            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Tipo de cambio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de Método de Pago
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    val rateVal = rate.toDoubleOrNull()

                    if (amountVal == null || amountVal <= 0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor ingresa un monto válido mayor a 0")
                        }
                    } else if (rateVal == null || rateVal <= 0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor ingresa un tipo de cambio válido mayor a 0")
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("¡Oferta publicada con éxito! ($currency $amount a un T.C. de $rate vía $paymentMethod)")
                            amount = ""
                            rate = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar Oferta")
            }
        }
    }
}
