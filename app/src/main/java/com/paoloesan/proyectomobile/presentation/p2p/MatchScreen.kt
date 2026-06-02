package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class MatchedOffer(
    val id: String,
    val username: String,
    val exchangeRate: Double,
    val compatibleAmount: Double,
    val currency: String,
    val paymentMethod: String,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(navController: NavController) {

    // Lista simulada de coincidencias automáticas
    val matches = remember {
        listOf(
            MatchedOffer("1", "Carlos Perez", 3.75, 150.0, "USD", "BCP", "Compra"),
            MatchedOffer("2", "Ana Gomez", 3.76, 200.0, "USD", "Yape", "Venta"),
            MatchedOffer("3", "Luis Rodriguez", 3.74, 500.0, "PEN", "Interbank", "Compra"),
            MatchedOffer("4", "Maria Lopez", 3.77, 300.0, "USD", "BCP", "Venta"),
            MatchedOffer("5", "Juan Castro", 3.75, 100.0, "PEN", "Yape", "Compra")
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedMatch by remember { mutableStateOf<MatchedOffer?>(null) }

    // Dialogo de confirmación al iniciar transacción
    if (showDialog && selectedMatch != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Iniciar Transacción") },
            text = { Text("¿Deseas iniciar una transacción con ${selectedMatch!!.username}?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    navController.navigate("transaction_detail")
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coincidencias Automáticas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (matches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron coincidencias disponibles",
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
                items(matches, key = { it.id }) { match ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // Usuario y tipo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = match.username,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Text(
                                    text = match.type,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (match.type == "Compra")
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Tipo de cambio y monto
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "T.C.: ${match.exchangeRate}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "${match.currency} ${match.compatibleAmount}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Método de pago
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = match.paymentMethod,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botón iniciar transacción
                            Button(
                                onClick = {
                                    selectedMatch = match
                                    showDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Iniciar transacción")
                            }
                        }
                    }
                }
            }
        }
    }
}