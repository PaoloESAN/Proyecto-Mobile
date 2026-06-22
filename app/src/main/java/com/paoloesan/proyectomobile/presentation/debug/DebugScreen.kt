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

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.data.repository.AuthRepository
import com.paoloesan.proyectomobile.presentation.navigation.Destination
import com.paoloesan.proyectomobile.presentation.navigation.appDestinations
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // IDs globales configurables para navegar a las pantallas de transacción/oferta
    var transactionId by remember { mutableStateOf("") }
    var offerId by remember { mutableStateOf("") }

    // Diálogos auxiliares ya existentes
    var showChatIdDialog by remember { mutableStateOf(false) }
    var inputChatId by remember { mutableStateOf("999") }
    var showBankDetailsDialog by remember { mutableStateOf(false) }
    var inputBankTxId by remember { mutableStateOf("5") }
    var isSeller by remember { mutableStateOf(false) }
    var inputStatus by remember { mutableStateOf("En Proceso") }

    // Confirmación de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeveloperMode,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Debug Screen")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Sección de IDs de navegación rápida ──────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔢 IDs de Navegación",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Ingresa IDs reales de Supabase para acceder directamente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Transaction ID row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = transactionId,
                                onValueChange = { transactionId = it },
                                label = { Text("ID Transacción") },
                                placeholder = { Text("ej: 42") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val id = transactionId.trim().ifBlank { "1" }
                                    navController.navigate("transactionStatus/$id")
                                },
                                enabled = transactionId.isNotBlank()
                            ) {
                                Text("Ir")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Offer ID row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = offerId,
                                onValueChange = { offerId = it },
                                label = { Text("ID Oferta") },
                                placeholder = { Text("ej: 7") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val id = offerId.trim().ifBlank { "1" }
                                    navController.navigate("offerDetail/$id")
                                },
                                enabled = offerId.isNotBlank()
                            ) {
                                Text("Ir")
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📋 Todas las Pantallas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Grid de pantallas ─────────────────────────────────────────────────
            val destinations = appDestinations.filter { it.route != "debug" && it.route != "login" }
            items(
                count = (destinations.size + 1) / 2,
                key = { it }
            ) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val first = destinations.getOrNull(rowIndex * 2)
                    val second = destinations.getOrNull(rowIndex * 2 + 1)

                    first?.let { destination ->
                        Card(
                            modifier = Modifier.weight(1f),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            onClick = {
                                when {
                                    destination.route.startsWith("chat/") -> showChatIdDialog = true
                                    destination.route.startsWith("bankDetails/") -> showBankDetailsDialog = true
                                    else -> {
                                        val routeToNavigate = when (destination.route) {
                                            "offerDetail/{offerId}" ->
                                                "offerDetail/${offerId.trim().ifBlank { "1" }}"
                                            "transactionStatus/{transactionId}" ->
                                                "transactionStatus/${transactionId.trim().ifBlank { "1" }}"
                                            else -> destination.route
                                        }
                                        navController.navigate(routeToNavigate)
                                    }
                                }
                            }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = destination.route.take(30),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    second?.let { destination ->
                        Card(
                            modifier = Modifier.weight(1f),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            onClick = {
                                when {
                                    destination.route.startsWith("chat/") -> showChatIdDialog = true
                                    destination.route.startsWith("bankDetails/") -> showBankDetailsDialog = true
                                    else -> {
                                        val routeToNavigate = when (destination.route) {
                                            "offerDetail/{offerId}" ->
                                                "offerDetail/${offerId.trim().ifBlank { "1" }}"
                                            "transactionStatus/{transactionId}" ->
                                                "transactionStatus/${transactionId.trim().ifBlank { "1" }}"
                                            else -> destination.route
                                        }
                                        navController.navigate(routeToNavigate)
                                    }
                                }
                            }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = destination.route.take(30),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } ?: Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    // ── Diálogo de Logout ─────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Deseas cerrar sesión y volver al Login?") },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        try {
                            AuthRepository.logout(context)
                        } catch (_: Exception) {}
                        navController.navigate(Destination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo de Chat ID ────────────────────────────────────────────────────
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
                TextButton(onClick = { showChatIdDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo de BankDetails ────────────────────────────────────────────────
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
                TextButton(onClick = { showBankDetailsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
