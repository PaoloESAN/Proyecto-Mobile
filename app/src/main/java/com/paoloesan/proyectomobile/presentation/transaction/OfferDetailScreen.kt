package com.paoloesan.proyectomobile.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.paoloesan.proyectomobile.presentation.profile.BankAccount
import zed.rainxch.rikkaui.components.ui.PopupAnimation
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.label.Label
import zed.rainxch.rikkaui.components.ui.select.Select
import zed.rainxch.rikkaui.components.ui.select.SelectOption
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    onStartTransaction: (amount: String, rate: String, bank: String, type: String) -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var amountInput by remember { mutableStateOf("100") }
    
    val bankAccounts = listOf(
        BankAccount(banco = "BCP", numeroCuenta = "1234567890", titular = "Freddy Delgado", moneda = "PEN"),
        BankAccount(banco = "Yape", numeroCuenta = "9876543210", titular = "Freddy Delgado", moneda = "PEN"),
        BankAccount(banco = "Interbank", numeroCuenta = "5432109876", titular = "Freddy Delgado", moneda = "USD")
    )
    
    val accountOptions = bankAccounts.map {
        SelectOption(value = "${it.banco} - ${it.numeroCuenta} (${it.moneda})", label = "${it.banco} - ${it.numeroCuenta} (${it.moneda})")
    }

    var selectedAccount by remember { mutableStateOf(accountOptions.first().value) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val minLimit = 50.0
    val maxLimit = 200.0
    val rate = 3.85
    val isValidAmount = amountDouble >= minLimit && amountDouble <= maxLimit

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Confirmar Transacción",
                    variant = TextVariant.H2,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Text(
                    text = "¿Está seguro de que desea iniciar esta transacción por $amountInput USD?",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onStartTransaction(amountInput, rate.toString(), selectedAccount, "Compra")
                    },
                    text = "Confirmar"
                )
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false },
                    variant = ButtonVariant.Outline,
                    text = "Cancelar"
                )
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBack,
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = RikkaIcons.ArrowLeft,
                            contentDescription = "Regresar",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }

                    Text(
                        text = "Detalle de Oferta",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                // Scrollable main content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Advertiser Reputation Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = RikkaTheme.colors.primary.copy(alpha = 0.12f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = RikkaTheme.colors.primary
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Juan Perez",
                                    variant = TextVariant.Large,
                                    color = RikkaTheme.colors.onBackground
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Estrellas",
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "4.5",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                    Text(
                                        text = "• 120 reseñas",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Input & Bank account details card
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Monto a recibir (USD)")
                                Input(
                                    value = amountInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' }) {
                                            amountInput = input
                                        }
                                    },
                                    placeholder = "Ingrese monto...",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (!isValidAmount && amountInput.isNotEmpty()) {
                                    Text(
                                        text = "El monto debe estar entre $minLimit y $maxLimit USD",
                                        color = RikkaTheme.colors.destructive,
                                        variant = TextVariant.Small
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Recibir en mi cuenta bancaria")
                                Select(
                                    selectedValue = selectedAccount,
                                    onValueChange = { selectedAccount = it },
                                    options = accountOptions,
                                    placeholder = "Seleccione cuenta...",
                                    animation = PopupAnimation.Fade,
                                    maxHeight = 200.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Conversion Card (Premium exchange rates stacked presentation)
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Row Envías
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Envias",
                                    variant = TextVariant.P,
                                    color = Color.Gray
                                )
                                val enviasValue = String.format(java.util.Locale.US, "%,.2f", amountDouble * rate)
                                Text(
                                    text = "$enviasValue PEN",
                                    variant = TextVariant.H2,
                                    color = RikkaTheme.colors.onBackground
                                )
                            }

                            // Stepper indicator arrow/line divider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                                )
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = RikkaTheme.colors.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                                )
                            }

                            // Row Recibes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recibes",
                                    variant = TextVariant.P,
                                    color = Color.Gray
                                )
                                val recibesValue = String.format(java.util.Locale.US, "%,.2f", amountDouble)
                                Text(
                                    text = "$recibesValue USD",
                                    variant = TextVariant.H2,
                                    color = RikkaTheme.colors.primary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tipo de cambio pactado",
                                    variant = TextVariant.Small,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "1 USD = $rate PEN",
                                    variant = TextVariant.P,
                                    color = RikkaTheme.colors.onBackground
                                )
                            }
                        }
                    }

                    // Details Grid (Card containing a list of operation details)
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailItemRow(
                                icon = Icons.Default.CurrencyExchange,
                                label = "Tipo de operacion",
                                value = "Compra de USD"
                            )
                            DetailItemRow(
                                icon = Icons.Default.AccountBalance,
                                label = "Metodo de pago",
                                value = "Transferencia BCP"
                            )
                            DetailItemRow(
                                icon = Icons.Default.Tune,
                                label = "Limites permitidos",
                                value = "Minimo $minLimit / Maximo $maxLimit"
                            )
                        }
                    }
                }

                // Anchored Action Button at the bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            showConfirmDialog = true
                        },
                        enabled = isValidAmount,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Iniciar transaccion"
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = RikkaTheme.colors.primary.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = RikkaTheme.colors.primary
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                variant = TextVariant.Small,
                color = Color.Gray
            )
            Text(
                text = value,
                variant = TextVariant.P,
                color = RikkaTheme.colors.onBackground
            )
        }
    }
}
