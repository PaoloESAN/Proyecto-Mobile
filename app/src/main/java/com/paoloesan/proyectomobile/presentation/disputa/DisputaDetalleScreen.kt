package com.paoloesan.proyectomobile.presentation.disputa

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardAnimation
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@Composable
fun DisputaDetalleScreen(
    navController: NavController,
    viewModel: DisputaViewModel,
    disputaId: Int
) {
    val disputa = viewModel.getDisputaById(disputaId)
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()

    var showZoomDialog by remember { mutableStateOf(false) }
    var pendingResolveForBuyer by remember { mutableStateOf<Boolean?>(null) }

    if (disputa == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RikkaTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Disputa no encontrada",
                variant = TextVariant.Large,
                color = RikkaTheme.colors.onBackground
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            snackbarHost = { ToastHost(hostState = toastState) },
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
                        text = "Detalle de Disputa",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Section: Transaction Data
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        animation = CardAnimation.None
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = RikkaTheme.colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = disputa.transaccion,
                                        variant = TextVariant.Large,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                }

                                val (badgeBgColor, badgeTextColor) = if (disputa.estado == "Activa") {
                                    Color(0xFFE3F2FD) to Color(0xFF1565C0)
                                } else {
                                    Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = disputa.estado,
                                        color = badgeTextColor,
                                        variant = TextVariant.Small,
                                        modifier = Modifier.padding(bottom = 1.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = RikkaTheme.colors.muted.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Monto",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "S/ 500.00",
                                        variant = TextVariant.H1,
                                        color = RikkaTheme.colors.primary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Tipo de Operación",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Compra de USD",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground,
                                    )
                                }
                            }

                            HorizontalDivider(color = RikkaTheme.colors.onBackground.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Comprador",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = disputa.comprador,
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Vendedor",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = disputa.vendedor,
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground,
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Voucher Sent
                item {
                    Text(
                        text = "Comprobante Adjunto",
                        variant = TextVariant.Large,
                        color = RikkaTheme.colors.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        animation = CardAnimation.Press
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                                    .clickable { showZoomDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.voucher_demo),
                                    contentDescription = "Voucher en disputa",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .padding(8.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Zoom",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showZoomDialog = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Presiona para ampliar comprobante",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.primary,
                                )
                            }
                        }
                    }
                }

                // Section: Chat History
                item {
                    Text(
                        text = "Historial de Mensajes",
                        variant = TextVariant.Large,
                        color = RikkaTheme.colors.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ChatBubble(
                            sender = disputa.comprador,
                            message = "Hola, ¿ya realizaste la transferencia?",
                            time = "10:00",
                            isBuyer = true
                        )

                        ChatBubble(
                            sender = disputa.vendedor,
                            message = "Si, acabo de transferir el monto.",
                            time = "10:02",
                            isBuyer = false
                        )

                        ChatBubble(
                            sender = disputa.comprador,
                            message = "Perfecto, dejame verificarlo.",
                            time = "10:03",
                            isBuyer = true
                        )

                        ChatBubble(
                            sender = disputa.vendedor,
                            message = "Te envie el comprobante por aqui.",
                            time = "10:05",
                            isBuyer = false
                        )
                    }
                }

                // Section: Arbitration Actions
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Warning Alert Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = RikkaTheme.colors.destructive.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = RikkaTheme.colors.destructive,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Arbitraje Irreversible",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive,
                            )
                            Text(
                                text = "Asegúrese de validar correctamente las pruebas.",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            pendingResolveForBuyer = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        text = "Darle la razón al Comprador"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            pendingResolveForBuyer = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Outline,
                        text = "Darle la razón al Vendedor"
                    )
                }
            }
        }
    }

    // Zoom Dialog for Voucher
    if (showZoomDialog) {
        AlertDialog(
            onDismissRequest = { showZoomDialog = false },
            containerColor = RikkaTheme.colors.background,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comprobante de Pago",
                        variant = TextVariant.Large,
                        color = RikkaTheme.colors.onBackground
                    )

                    Button(
                        onClick = { showZoomDialog = false },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.voucher_demo),
                        contentDescription = "Comprobante ampliado",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Confirmation Dialog for Arbitration
    pendingResolveForBuyer?.let { resolveForBuyer ->
        val targetName = if (resolveForBuyer) disputa.comprador else disputa.vendedor
        val roleName = if (resolveForBuyer) "comprador" else "vendedor"
        AlertDialog(
            onDismissRequest = { pendingResolveForBuyer = null },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Confirmar Resolución",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de darle la razón al $roleName ($targetName)? Los fondos serán liberados de forma inmediata y esta acción es irreversible.",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolverDisputa(disputaId)
                        scope.launch {
                            toastState.show(
                                message = "Fondos liberados a favor de $targetName",
                                variant = ToastVariant.Success
                            )
                            delay(1500)
                            navController.popBackStack()
                        }
                        pendingResolveForBuyer = null
                    },
                    size = ButtonSize.Sm,
                    text = "Confirmar"
                )
            },
            dismissButton = {
                Button(
                    onClick = { pendingResolveForBuyer = null },
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Sm,
                    text = "Cancelar"
                )
            }
        )
    }
}

@Composable
private fun ChatBubble(
    sender: String,
    message: String,
    time: String,
    isBuyer: Boolean
) {
    val bubbleShape = if (isBuyer) {
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    }

    val bubbleBgColor = if (isBuyer) {
        Color(0xFFF1F3F4) // Light gray
    } else {
        Color(0xFF0F9D58) // Vibrant green
    }

    val textColor = if (isBuyer) RikkaTheme.colors.onBackground else Color.White
    val alignment = if (isBuyer) Alignment.Start else Alignment.End
    val initials = if (sender.isNotEmpty()) {
        sender.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
    } else {
        "U"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (isBuyer) {
                // Buyer Avatar (Gray text, no background)
                Box(
                    modifier = Modifier
                        .size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f),
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                horizontalAlignment = if (isBuyer) Alignment.Start else Alignment.End
            ) {
                Text(
                    text = sender,
                    variant = TextVariant.Small,
                    color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(bubbleBgColor)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message,
                        variant = TextVariant.P,
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    variant = TextVariant.Small,
                    color = RikkaTheme.colors.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (!isBuyer) {
                // Seller Avatar (Green text with green background)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(RikkaTheme.colors.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.primary,
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
