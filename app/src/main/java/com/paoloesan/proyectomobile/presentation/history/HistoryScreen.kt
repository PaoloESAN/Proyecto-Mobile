package com.paoloesan.proyectomobile.presentation.history

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class HistoryItem(
    val id: Int,
    val fecha: String,
    val tipoOperacion: String,
    val monto: Double,
    val tipoCambio: Double,
    val estado: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {

    val context = LocalContext.current

    val operaciones = remember {
        listOf(
            HistoryItem(1, "05/06/2026", "Compra", 150.0, 3.75, "Finalizado"),
            HistoryItem(2, "04/06/2026", "Venta", 300.0, 3.72, "Pendiente"),
            HistoryItem(3, "03/06/2026", "Compra", 500.0, 3.70, "Finalizado"),
            HistoryItem(4, "02/06/2026", "Venta", 250.0, 3.73, "Finalizado"),
            HistoryItem(5, "01/06/2026", "Compra", 1000.0, 3.71, "Pendiente"),
            HistoryItem(6, "31/05/2026", "Venta", 420.0, 3.74, "Finalizado"),
            HistoryItem(7, "30/05/2026", "Compra", 800.0, 3.69, "Finalizado"),
            HistoryItem(8, "29/05/2026", "Venta", 1500.0, 3.76, "Pendiente"),
            HistoryItem(9, "28/05/2026", "Compra", 350.0, 3.72, "Finalizado"),
            HistoryItem(10, "27/05/2026", "Venta", 620.0, 3.75, "Finalizado")

        )
    }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var operacionSeleccionada by remember { mutableStateOf<Int?>(null) }
    val operacionesCalificadas = remember { mutableStateListOf<Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
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
            items(operaciones, key = { it.id }) { item ->
                HistoryItemCard(
                    item = item,
                    isRated = item.id in operacionesCalificadas,
                    onCalificarClick = {
                        operacionSeleccionada = item.id
                        mostrarDialogo = true
                    }
                )
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
                rating = 0
                comentario = ""
            },
            title = { Text("Calificar operación") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "¿Cómo calificaría su experiencia?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rating $i",
                                    tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        label = { Text("Comentario (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = rating > 0,
                    onClick = {
                        if (operacionSeleccionada != null && rating > 0) {
                            operacionesCalificadas.add(operacionSeleccionada!!)
                            Toast.makeText(
                                context,
                                "¡Gracias por su calificación!",
                                Toast.LENGTH_SHORT
                            ).show()
                            mostrarDialogo = false
                            rating = 0
                            comentario = ""
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    isRated: Boolean,
    onCalificarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (item.tipoOperacion == "Compra")
                        Icons.Default.AddCircleOutline
                    else
                        Icons.Default.RemoveCircleOutline

                    val color = if (item.tipoOperacion == "Compra")
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.tertiary

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.tipoOperacion,
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                }
                Text(
                    text = item.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monto",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "S/${item.monto}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tipo de cambio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.tipoCambio.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(estado = item.estado)

                if (item.estado == "Finalizado") {
                    if (isRated) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calificado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onCalificarClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Calificar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(estado: String) {
    val containerColor = when (estado) {
        "Finalizado" -> MaterialTheme.colorScheme.primaryContainer
        "Pendiente" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (estado) {
        "Finalizado" -> MaterialTheme.colorScheme.onPrimaryContainer
        "Pendiente" -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = estado,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
