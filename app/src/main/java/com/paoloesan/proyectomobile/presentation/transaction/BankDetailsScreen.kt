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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(
    onBack: () -> Unit,
    onContinueToVoucher: () -> Unit,
    onChat: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()

    val bankName = "BCP"
    val accountNumber = "191-12345678-0-12"
    val titularName = "Carlos Rodriguez"
    val currency = "Soles (PEN)"
    val transferAmount = "3,850.00 PEN"
    val exchangeRate = "1 USD = 3.85 PEN"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            snackbarHost = {
                ToastHost(hostState = toastState)
            },
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
                        text = "Datos Bancarios",
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
                // Scrollable main form
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // Quick step instructions
                    item {
                        Text(
                            text = "Sigue estos pasos para realizar tu pago de forma segura y rapida.",
                            variant = TextVariant.P,
                            color = Color.Gray
                        )
                    }

                    // Huge outstanding transfer amount Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "MONTO EXACTO A TRANSFERIR",
                                    variant = TextVariant.Small,
                                    color = Color.Gray
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = transferAmount,
                                        variant = TextVariant.H2,
                                        color = RikkaTheme.colors.primary
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(transferAmount.replace(" PEN", "").trim()))
                                            scope.launch {
                                                toastState.show(
                                                    message = "Monto copiado: $transferAmount",
                                                    variant = ToastVariant.Success
                                                )
                                            }
                                        },
                                        variant = ButtonVariant.Ghost,
                                        size = ButtonSize.Icon
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copiar monto",
                                            modifier = Modifier.size(16.dp),
                                            tint = RikkaTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Copyable details bank container Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                CopyableDetailRow(
                                    icon = Icons.Default.AccountBalance,
                                    label = "BANCO",
                                    value = bankName,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.ContentCopy,
                                    label = "NUMERO DE CUENTA",
                                    value = accountNumber,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.Person,
                                    label = "TITULAR DE LA CUENTA",
                                    value = titularName,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.AttachMoney,
                                    label = "MONEDA",
                                    value = currency,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = true
                                )

                                CopyableDetailRow(
                                    icon = Icons.Default.CurrencyExchange,
                                    label = "TIPO DE CAMBIO",
                                    value = exchangeRate,
                                    clipboardManager = clipboardManager,
                                    toastState = toastState,
                                    scope = scope,
                                    showDivider = false
                                )
                            }
                        }
                    }

                    // Important rules warning card
                    item {
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
                                        .size(36.dp)
                                        .background(
                                            color = Color(0xFFFFEBEE),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFC62828)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Importante",
                                        variant = TextVariant.P,
                                        color = Color(0xFFC62828)
                                    )
                                    Text(
                                        text = "Solo se aceptan transferencias desde tu propia cuenta bancaria. Los pagos de terceros seran rechazados.",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Anchored bottom action area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onContinueToVoucher,
                        modifier = Modifier.fillMaxWidth(),
                        text = "He realizado la transferencia"
                    )

                    Button(
                        onClick = onChat,
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Abrir chat con la contraparte"
                    )
                }
            }
        }
    }
}

@Composable
private fun CopyableDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    toastState: zed.rainxch.rikkaui.components.ui.toast.ToastHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
                modifier = Modifier.weight(1f),
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

            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                    scope.launch {
                        toastState.show(
                            message = "Copiado al portapapeles",
                            variant = ToastVariant.Success
                        )
                    }
                },
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Icon
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar $label",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
            )
        }
    }
}
