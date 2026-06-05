package com.paoloesan.proyectomobile.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TransactionStatusScreen
 * 
 * Pantalla de visualización de estados de transacción
 * Relacionada con US-14: Visualización de estados de transacción
 *
 * Permite al usuario ver el estado actualizado de sus transacciones,
 * incluyendo el progreso detallado y el historial de operación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionStatusScreen(
    onBack: () -> Unit,
    onViewBankDetails: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Estado de Transacción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implementar menú */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Card de estado actual
                TransactionStatusCard()

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Card de historial
                TransactionHistoryCard()

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Botón Ver datos bancarios
                Button(
                    onClick = onViewBankDetails,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Ver datos bancarios",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * TransactionStatusCard
 *
 * Componente que muestra el estado actual de la transacción.
 */
@Composable
private fun TransactionStatusCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Estado actual con ícono
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "En proceso",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFFFFB74D)
                )

                Column {
                    Text(
                        text = "En proceso",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Actualizado el 24 de Mayo, 2024 - 14:30",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Operación
            TransactionDetailRow(
                icon = Icons.Default.CurrencyExchange,
                label = "OPERACIÓN",
                value = "Compra de USD",
                iconTint = MaterialTheme.colorScheme.secondary
            )

            // Usuario vendedor
            TransactionDetailRow(
                icon = Icons.Default.Person,
                label = "VENDIDO POR",
                value = "Mateo Rojas",
                iconTint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * TransactionHistoryCard
 *
 * Componente que muestra el historial de operación en formato de línea de tiempo.
 */
@Composable
private fun TransactionHistoryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título del historial
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Historial",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Historial de operación",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Timeline items
            TimelineItem(
                state = TransactionState.COMPLETED,
                title = "Oferta aceptada",
                date = "24 May, 10:15",
                isLast = false
            )

            TimelineItem(
                state = TransactionState.COMPLETED,
                title = "Pago enviado",
                date = "24 May, 11:30",
                isLast = false
            )

            TimelineItem(
                state = TransactionState.PENDING,
                title = "Verificación pendiente",
                subtitle = "En revisión por el vendedor.",
                date = null,
                isLast = false
            )

            TimelineItem(
                state = TransactionState.INACTIVE,
                title = "Transacción completada",
                date = "Pendiente",
                isLast = true
            )
        }
    }
}

/**
 * TransactionDetailRow
 *
 * Componente reutilizable para mostrar detalles de la transacción.
 */
@Composable
private fun TransactionDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = iconTint.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(6.dp),
            tint = iconTint
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * TimelineItem
 *
 * Componente para mostrar un ítem en la línea de tiempo del historial.
 */
@Composable
private fun TimelineItem(
    state: TransactionState,
    title: String,
    subtitle: String? = null,
    date: String? = null,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Línea de tiempo vertical
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(if (subtitle != null) 80.dp else 60.dp)
                .background(
                    color = if (isLast) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
        )

        // Contenido del item
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Punto de la línea de tiempo y título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Punto indicador
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (state) {
                                TransactionState.COMPLETED -> Color(0xFF4CAF50)
                                TransactionState.PENDING -> Color(0xFFFFB74D)
                                TransactionState.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (state) {
                            TransactionState.COMPLETED -> Icons.Default.CheckCircle
                            TransactionState.PENDING -> Icons.Default.HourglassEmpty
                            TransactionState.INACTIVE -> Icons.Default.Schedule
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = when (state) {
                            TransactionState.COMPLETED -> Color.White
                            TransactionState.PENDING -> Color.White
                            TransactionState.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Subtítulo si existe
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }

            // Fecha si existe
            if (date != null) {
                Text(
                    text = date,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }
    }
}

/**
 * Enum para representar los estados de transacción
 */
enum class TransactionState {
    COMPLETED,
    PENDING,
    INACTIVE
}
