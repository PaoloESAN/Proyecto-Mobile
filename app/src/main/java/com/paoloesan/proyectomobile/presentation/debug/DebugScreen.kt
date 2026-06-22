package com.paoloesan.proyectomobile.presentation.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.presentation.navigation.appDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(navController: NavController) {
    var showChatIdDialog by remember { mutableStateOf(false) }
    var inputChatId by remember { mutableStateOf("999") }
    var showBankDetailsDialog by remember { mutableStateOf(false) }
    var inputBankTxId by remember { mutableStateOf("5") }
    var isSeller by remember { mutableStateOf(false) }
    var inputStatus by remember { mutableStateOf("En Proceso") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pantalla de Debug") }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(appDestinations.filter { it.route != "debug" }) { destination ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = {
                        if (destination.route.startsWith("chat/")) {
                            showChatIdDialog = true
                        } else if (destination.route.startsWith("bankDetails/")) {
                            showBankDetailsDialog = true
                        } else {
                            val routeToNavigate = when (destination.route) {
                                "offerDetail/{offerId}" -> "offerDetail/OFF001"
                                "transactionStatus/{transactionId}" -> "transactionStatus/TX001"
                                else -> destination.route
                            }
                            navController.navigate(routeToNavigate)
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = destination.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Ruta: ${destination.route}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (showChatIdDialog) {
        AlertDialog(
            onDismissRequest = { showChatIdDialog = false },
            title = { Text("Ingresar ID de Transacción") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa el ID de la transacción para el chat (usa 999 para modo simulado):")
                    OutlinedTextField(
                        value = inputChatId,
                        onValueChange = { inputChatId = it },
                        placeholder = { Text("ID de transacción") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showChatIdDialog = false
                        val idToUse = inputChatId.toIntOrNull() ?: 999
                        navController.navigate("chat/$idToUse?readOnly=false")
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChatIdDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showBankDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showBankDetailsDialog = false },
            title = { Text("Parámetros para Detalles de Pago") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configura los parámetros para probar la pantalla de Detalles de Pago:")
                    
                    OutlinedTextField(
                        value = inputBankTxId,
                        onValueChange = { inputBankTxId = it },
                        label = { Text("ID de Transacción (ej: 5)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputStatus,
                        onValueChange = { inputStatus = it },
                        label = { Text("Estado de Transacción (ej: En Proceso)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isSeller,
                            onCheckedChange = { isSeller = it }
                        )
                        Text("¿Soy el Vendedor?")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBankDetailsDialog = false
                        val idToUse = inputBankTxId.toIntOrNull() ?: 5
                        val route = "bankDetails/$idToUse?isSeller=$isSeller&status=$inputStatus&amount=150.00&rate=3.80&bank=BCP - 191-99882211-0-45 (PEN)&type=Venta&uploaded=false&currency=USD"
                        navController.navigate(route)
                    }
                ) {
                    Text("Ir a Detalles")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBankDetailsDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
