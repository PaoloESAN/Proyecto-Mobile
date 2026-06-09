package com.paoloesan.proyectomobile.presentation.transaction

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionStatusScreen(
    onBack: () -> Unit,
    onViewBankDetails: () -> Unit,
    onChat: () -> Unit = {},
    onConfirmPayment: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

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
                        text = "Estado de Transaccion",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Button(
                        onClick = { /* Menu */ },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mas opciones",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }
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
                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    // Hero Dynamic Status Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFFFF8E1),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = Color(0xFFFFB74D)
                                )

                                Text(
                                    text = "Esperando pago",
                                    variant = TextVariant.Large,
                                    color = Color(0xFFFFB74D)
                                )
                            }
                        }
                    }

                    // Operation Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ID DE TRANSACCION",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "TX-001249",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.CurrencyExchange,
                                    label = "Operacion",
                                    value = "Compra de 1,000.00 USD"
                                )

                                TransactionBriefRow(
                                    icon = Icons.Default.Person,
                                    label = "Contraparte",
                                    value = "Mateo Rojas"
                                )
                            }
                        }
                    }

                    // Stepper Timeline Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Historial del flujo",
                                    variant = TextVariant.Large,
                                    color = RikkaTheme.colors.onBackground
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    TimelineStep(
                                        status = TimelineStepStatus.COMPLETED,
                                        title = "Transaccion creada",
                                        subtitle = "24 May, 14:15",
                                        isLast = false
                                    )

                                    TimelineStep(
                                        status = TimelineStepStatus.COMPLETED,
                                        title = "Pago realizado",
                                        subtitle = "24 May, 14:28",
                                        isLast = false
                                    )

                                    TimelineStep(
                                        status = TimelineStepStatus.ACTIVE,
                                        title = "Verificando transferencia",
                                        subtitle = "El vendedor debe confirmar la recepcion del dinero",
                                        isLast = true
                                    )
                                }
                            }
                        }
                    }
                }

                // Fixed bottom action section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onViewBankDetails,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Ver instrucciones bancarias"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onChat,
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f),
                            text = "Chat"
                        )

                        Button(
                            onClick = onConfirmPayment,
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f),
                            text = "Confirmar recepcion"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionBriefRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = RikkaTheme.colors.primary
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp)
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

enum class TimelineStepStatus {
    COMPLETED,
    ACTIVE,
    PENDING
}

@Composable
private fun TimelineStep(
    status: TimelineStepStatus,
    title: String,
    subtitle: String,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon / Indicator circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        color = when (status) {
                            TimelineStepStatus.COMPLETED -> Color(0xFF4CAF50)
                            TimelineStepStatus.ACTIVE -> Color(0xFFFFB74D)
                            TimelineStepStatus.PENDING -> RikkaTheme.colors.muted.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = when (status) {
                        TimelineStepStatus.COMPLETED -> Icons.Default.CheckCircle
                        TimelineStepStatus.ACTIVE -> Icons.Default.HourglassEmpty
                        TimelineStepStatus.PENDING -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when (status) {
                        TimelineStepStatus.PENDING -> Color.Gray
                        else -> Color.White
                    }
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(
                            color = when (status) {
                                TimelineStepStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                                else -> RikkaTheme.colors.muted.copy(alpha = 0.2f)
                            }
                        )
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                variant = TextVariant.P,
                color = when (status) {
                    TimelineStepStatus.PENDING -> Color.Gray
                    else -> RikkaTheme.colors.onBackground
                }
            )

            Text(
                text = subtitle,
                variant = TextVariant.Small,
                color = Color.Gray
            )
        }
    }
}
