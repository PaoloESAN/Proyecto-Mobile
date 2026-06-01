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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * BankDetailsScreen
 * 
 * Pantalla de visualización de instrucciones de pago y detalles bancarios
 * Relacionada con US-15: Visualización de instrucciones de pago
 * 
 * Permite al usuario pagador visualizar los datos bancarios de la contraparte
 * antes de realizar la transferencia y subir el comprobante de pago.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(
    onBack: () -> Unit,
    onContinueToVoucher: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Datos bancarios",
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

                // Texto de instrucción
                PaymentInstructionText()

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Card de datos bancarios
                BankDetailsCard()

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Botón Continuar
                Button(
                    onClick = onContinueToVoucher,
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
                        text = "Continuar",
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
 * PaymentInstructionText
 * 
 * Componente que muestra las instrucciones de pago al usuario.
 */
@Composable
private fun PaymentInstructionText() {
    Text(
        text = "Por favor, transfiere el monto exacto a la siguiente cuenta para completar tu operación.",
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 16.dp),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
    )
}

/**
 * BankDetailsCard
 * 
 * Componente que muestra todos los datos bancarios de la contraparte.
 */
@Composable
private fun BankDetailsCard() {
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
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Nombre del banco
            BankDetailRow(
                icon = Icons.Default.AccountBalance,
                label = "BANCO",
                value = "BCP",
                iconTint = MaterialTheme.colorScheme.primary,
                showDivider = true
            )

            // Número de cuenta
            BankDetailRow(
                icon = Icons.Default.ContentCopy,
                label = "NÚMERO DE CUENTA",
                value = "191-12345678-0-12",
                iconTint = MaterialTheme.colorScheme.secondary,
                showDivider = true
            )

            // Nombre del titular
            BankDetailRow(
                icon = Icons.Default.Person,
                label = "TITULAR",
                value = "Carlos Rodríguez",
                iconTint = MaterialTheme.colorScheme.tertiary,
                showDivider = true
            )

            // Tipo de moneda
            BankDetailRow(
                icon = Icons.Default.AttachMoney,
                label = "TIPO DE MONEDA",
                value = "Soles (PEN)",
                iconTint = MaterialTheme.colorScheme.primary,
                showDivider = true
            )

            // Monto a transferir (destacado)
            BankDetailRow(
                icon = Icons.Default.AttachMoney,
                label = "MONTO A TRANSFERIR",
                value = "3,850.00 PEN",
                iconTint = Color(0xFF4CAF50),
                highlight = true,
                showDivider = true
            )

            // Tipo de cambio
            BankDetailRow(
                icon = Icons.Default.CurrencyExchange,
                label = "TIPO DE CAMBIO",
                value = "1 USD = 3.85 PEN",
                iconTint = MaterialTheme.colorScheme.secondary,
                showDivider = false
            )
        }
    }
}

/**
 * BankDetailRow
 * 
 * Componente reutilizable para mostrar un detalle bancario con ícono, etiqueta y valor.
 */
@Composable
private fun BankDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: androidx.compose.ui.graphics.Color,
    highlight: Boolean = false,
    showDivider: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícono
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

            // Contenido de texto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = value,
                    fontSize = if (highlight) 16.sp else 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold
                )
            }
        }

        // Divisor si es necesario
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
    }
}
