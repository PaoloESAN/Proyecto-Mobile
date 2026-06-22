package com.paoloesan.proyectomobile.presentation.disputa

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun DisputaListaScreen(
    navController: NavController,
    viewModel: DisputaViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadDisputas()
    }

    val disputasAMostrar = if (selectedTab == 0) {
        uiState.disputasActivas
    } else {
        uiState.disputasResueltas
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
                        text = "Disputas",
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        variant = if (selectedTab == 0) ButtonVariant.Default else ButtonVariant.Outline,
                        modifier = Modifier.weight(1f),
                        text = "Activas (${uiState.disputasActivas.size})"
                    )

                    Button(
                        onClick = { selectedTab = 1 },
                        variant = if (selectedTab == 1) ButtonVariant.Default else ButtonVariant.Outline,
                        modifier = Modifier.weight(1f),
                        text = "Resueltas (${uiState.disputasResueltas.size})"
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RikkaTheme.colors.primary)
                    }
                } else if (disputasAMostrar.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = RikkaTheme.colors.onBackground.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "No hay disputas en esta sección",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(disputasAMostrar, key = { it.id }) { disputa ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        navController.navigate("detalle_disputa/${disputa.id}")
                                    },
                                animation = CardAnimation.Press
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Header Card Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            androidx.compose.material3.Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = RikkaTheme.colors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = disputa.transaccion,
                                                variant = TextVariant.P,
                                                color = RikkaTheme.colors.onBackground
                                            )
                                        }

                                        Text(
                                            text = disputa.monto,
                                            variant = TextVariant.P,
                                            color = RikkaTheme.colors.primary
                                        )
                                    }

                                    HorizontalDivider(
                                        color = RikkaTheme.colors.onBackground.copy(
                                            alpha = 0.1f
                                        )
                                    )

                                    // Participant details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Comprador",
                                                variant = TextVariant.Small,
                                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(
                                                            RikkaTheme.colors.primary.copy(alpha = 0.1f),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    androidx.compose.material3.Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = RikkaTheme.colors.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Text(
                                                    text = disputa.comprador,
                                                    variant = TextVariant.P,
                                                    color = RikkaTheme.colors.onBackground
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .size(28.dp)
                                                .background(
                                                    color = RikkaTheme.colors.onBackground.copy(
                                                        alpha = 0.05f
                                                    ),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "VS",
                                                variant = TextVariant.Small,
                                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(1.dp)
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "Vendedor",
                                                variant = TextVariant.Small,
                                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = disputa.vendedor,
                                                    variant = TextVariant.P,
                                                    color = RikkaTheme.colors.onBackground
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(
                                                            RikkaTheme.colors.onBackground.copy(
                                                                alpha = 0.15f
                                                            ),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    androidx.compose.material3.Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = RikkaTheme.colors.onBackground.copy(
                                                            alpha = 0.6f
                                                        ),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(
                                        color = RikkaTheme.colors.onBackground.copy(
                                            alpha = 0.1f
                                        )
                                    )

                                    // Bottom Row: Dispute message
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Motivo de la disputa:",
                                            variant = TextVariant.Small,
                                            color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = disputa.mensaje,
                                            variant = TextVariant.P,
                                            color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(estado: String) {
    val (bgColor, textColor) = when (estado) {
        "Activa" -> Color(0xFFE3F2FD) to Color(0xFF1565C0) // Premium light blue background and deep blue text for active dispute
        "Resuelta" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // Light green / dark green
        else -> RikkaTheme.colors.onBackground.copy(alpha = 0.1f) to RikkaTheme.colors.onBackground.copy(
            alpha = 0.6f
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado,
            color = textColor,
            variant = TextVariant.Small,
            modifier = Modifier.padding(bottom = 1.dp)
        )
    }
}
