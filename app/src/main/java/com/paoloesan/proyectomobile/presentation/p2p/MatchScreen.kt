package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardAnimation
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.foundation.RikkaTheme

data class MatchedOffer(
    val id: String,
    val username: String,
    val exchangeRate: Double,
    val compatibleAmount: Double,
    val currency: String,
    val paymentMethod: String,
    val type: String,
    val minAmount: Double,
    val maxAmount: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(navController: NavController) {
    val matches = remember {
        listOf(
            MatchedOffer("1", "Carlos Perez", 3.75, 150.0, "USD", "BCP", "Compra", 50.0, 200.0),
            MatchedOffer("2", "Ana Gomez", 3.76, 200.0, "USD", "Yape", "Venta", 100.0, 500.0),
            MatchedOffer(
                "3",
                "Luis Rodriguez",
                3.74,
                500.0,
                "PEN",
                "Interbank",
                "Compra",
                200.0,
                1000.0
            ),
            MatchedOffer("4", "Maria Lopez", 3.77, 300.0, "USD", "BCP", "Venta", 100.0, 400.0),
            MatchedOffer("5", "Juan Castro", 3.75, 100.0, "PEN", "Yape", "Compra", 20.0, 150.0)
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedMatch by remember { mutableStateOf<MatchedOffer?>(null) }
    val focusManager = LocalFocusManager.current

    if (showDialog && selectedMatch != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Iniciar Transaccion",
                    variant = TextVariant.H2,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Text(
                    text = "¿Deseas iniciar una transaccion con ${selectedMatch!!.username}?",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigate("transactionStatus/TX001")
                    },
                    text = "Confirmar"
                )
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
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
                        onClick = { navController.popBackStack() },
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
                        text = "Coincidencias Automaticas",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
            if (matches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron coincidencias disponibles",
                        variant = TextVariant.P,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            focusManager.clearFocus()
                        }
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(matches, key = { it.id }) { match ->
                        val isCompra = match.type == "Compra"
                        val operationColor =
                            if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            animation = CardAnimation.Press
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // User Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = RikkaTheme.colors.primary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Text(
                                            text = match.username,
                                            variant = TextVariant.Large,
                                            color = RikkaTheme.colors.onBackground
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = operationColor.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = match.type,
                                            variant = TextVariant.Small,
                                            color = operationColor
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(RikkaTheme.colors.muted.copy(alpha = 0.2f))
                                )

                                // Financial details and payment method with limits
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Column: T.C. and Payment Method
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            androidx.compose.material3.Icon(
                                                imageVector = Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = RikkaTheme.colors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "T.C.: ${match.exchangeRate}",
                                                variant = TextVariant.Small,
                                                color = Color.Gray
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            androidx.compose.material3.Icon(
                                                imageVector = Icons.Default.Payment,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = match.paymentMethod,
                                                variant = TextVariant.Small,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    // Right Column: Compatible Amount and Min/Max Limits
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${match.currency} ${match.compatibleAmount}",
                                            variant = TextVariant.Large,
                                            color = RikkaTheme.colors.onBackground
                                        )
                                        Text(
                                            text = "Min ${match.minAmount.toInt()} / Max ${match.maxAmount.toInt()}",
                                            variant = TextVariant.Small,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Action Button
                                Button(
                                    onClick = {
                                        selectedMatch = match
                                        showDialog = true
                                    },
                                    size = ButtonSize.Sm,
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Iniciar transaccion"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}