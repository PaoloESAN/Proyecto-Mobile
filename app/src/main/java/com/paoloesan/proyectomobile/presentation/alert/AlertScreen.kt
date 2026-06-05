package com.paoloesan.proyectomobile.presentation.alert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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

data class Alert(
    val currency: String,
    val rate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(navController: NavController) {
    var currency by remember { mutableStateOf("USD") }
    var rate by remember { mutableStateOf("") }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var savedAlerts by remember { mutableStateOf(listOf<Alert>()) }
    var rateError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currencies = listOf("USD", "PEN")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Alertas de Tipo de Cambio") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Notificación: El tipo de cambio ha alcanzado el valor configurado"
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Probar Notificación"
                )
            }
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
                    text = "Configura una alerta de tipo de cambio",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
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

            // Input de Tipo de Cambio
            item {
                OutlinedTextField(
                    value = rate,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            rate = input
                            rateError = false
                        }
                    },
                    label = { Text("Tipo de cambio") },
                    isError = rateError,
                    supportingText = if (rateError) {
                        { Text("Ingrese un tipo de cambio válido") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Botón Guardar Alerta
            item {
                Button(
                    onClick = {
                        val rateVal = rate.toDoubleOrNull()
                        if (rate.isBlank() || rateVal == null || rateVal <= 0) {
                            rateError = true
                        } else {
                            rateError = false
                            val newAlert = Alert(currency = currency, rate = rate)
                            savedAlerts = savedAlerts + newAlert
                            rate = ""
                            scope.launch {
                                snackbarHostState.showSnackbar("Alerta registrada correctamente")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar alerta")
                }
            }

            // Lista de Alertas Guardadas
            if (savedAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Alertas configuradas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                items(savedAlerts) { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = alert.currency,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "T.C.: ${alert.rate}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
