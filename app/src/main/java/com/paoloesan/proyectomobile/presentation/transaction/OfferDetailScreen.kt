package com.paoloesan.proyectomobile.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * OfferDetailScreen
 * 
 * Pantalla de detalle de oferta para iniciar una transacción
 * Relacionada con US-13: Inicio de transacción desde mercado
 *
 * Muestra la información completa de una oferta seleccionada y permite
 * al usuario iniciar una transacción o volver atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    onStartTransaction: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detalle de oferta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card con información de la oferta - Mejorada
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 24.dp, bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Usuario ofertante
                    OfferDetailItem(
                        icon = Icons.Default.Person,
                        label = "Usuario ofertante",
                        value = "Juan Perez",
                        iconTint = Color(0xFF6366F1),
                        backgroundColor = Color(0xFF6366F1)
                    )

                    // Divider visual
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )

                    // Tipo de operación
                    OfferDetailItem(
                        icon = Icons.Default.CurrencyExchange,
                        label = "Tipo de operación",
                        value = "Compra de USD",
                        iconTint = Color(0xFF10B981),
                        backgroundColor = Color(0xFF10B981)
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )

                    // Tipo de cambio
                    OfferDetailItem(
                        icon = Icons.Default.TrendingUp,
                        label = "Tipo de cambio",
                        value = "1 USD = 3.85 PEN",
                        iconTint = Color(0xFFF59E0B),
                        backgroundColor = Color(0xFFF59E0B)
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )

                    // Método de pago
                    OfferDetailItem(
                        icon = Icons.Default.AccountBalance,
                        label = "Método de pago",
                        value = "Transferencia BCP",
                        iconTint = Color(0xFF8B5CF6),
                        backgroundColor = Color(0xFF8B5CF6)
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )

                    // Límites de operación
                    OfferDetailItem(
                        icon = Icons.Default.Tune,
                        label = "Límites de operación",
                        value = "Mínimo 100 - Máximo 5000",
                        iconTint = Color(0xFFEF4444),
                        backgroundColor = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Iniciar transacción - Mejorado con gradiente y efectos
            Button(
                onClick = onStartTransaction,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Text(
                    text = "Iniciar transacción",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * OfferDetailItem
 *
 * Componente reutilizable para mostrar un elemento de información
 * con icono, etiqueta y valor. Mejorado con colores vibrantes y mejor diseño.
 */
@Composable
private fun OfferDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    backgroundColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icono con fondo circular
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .background(
                    color = backgroundColor.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .padding(12.dp)
                .size(24.dp),
            tint = backgroundColor
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )

            Text(
                text = value,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
