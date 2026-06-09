package com.paoloesan.proyectomobile.presentation.alert

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.PopupAnimation
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardAnimation
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.label.Label
import zed.rainxch.rikkaui.components.ui.select.Select
import zed.rainxch.rikkaui.components.ui.select.SelectOption
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

data class Alert(
    val currency: String,
    val rate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(navController: NavController) {
    var currency by remember { mutableStateOf("USD") }
    var rate by remember { mutableStateOf("") }
    var savedAlerts by remember { mutableStateOf(listOf<Alert>()) }
    var rateError by remember { mutableStateOf(false) }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val currencyOptions = listOf(
        SelectOption("USD", "USD"),
        SelectOption("PEN", "PEN")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            snackbarHost = {
                ToastHost(
                    hostState = toastState
                )
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
                        text = "Alertas de Tipo de Cambio",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            },
            floatingActionButton = {
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        scope.launch {
                            toastState.show(
                                message = "Notificacion: El tipo de cambio ha alcanzado el valor configurado",
                                variant = ToastVariant.Success
                            )
                        }
                    },
                    containerColor = RikkaTheme.colors.primary,
                    contentColor = Color.White
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Probar Notificacion",
                        tint = Color.White
                    )
                }
            }
        ) { innerPadding ->
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
                item {
                    Text(
                        text = "Configura una alerta de tipo de cambio",
                        variant = TextVariant.H2,
                        color = RikkaTheme.colors.primary
                    )
                }

                // Configuration Form Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Moneda Select
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Moneda")
                                Select(
                                    selectedValue = currency,
                                    onValueChange = { currency = it },
                                    options = currencyOptions,
                                    placeholder = "Seleccione moneda...",
                                    animation = PopupAnimation.Fade,
                                    maxHeight = 300.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Tipo de Cambio Input
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Tipo de cambio")
                                Input(
                                    value = rate,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' }) {
                                            rate = input
                                            rateError = false
                                        }
                                    },
                                    placeholder = "3.75",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = Icons.Default.AttachMoney,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                if (rateError) {
                                    Text(
                                        text = "Ingrese un tipo de cambio valido",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.destructive
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Guardar Alerta Button
                            Button(
                                onClick = {
                                    val rateVal = rate.toDoubleOrNull()
                                    if (rate.isBlank() || rateVal == null || rateVal <= 0) {
                                        rateError = true
                                    } else {
                                        rateError = false
                                        val newAlert = Alert(currency = currency, rate = rate)
                                        savedAlerts = savedAlerts + newAlert
                                        rate = ""
                                        scope.launch {
                                            toastState.show(
                                                message = "Alerta registrada correctamente",
                                                variant = ToastVariant.Success
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                text = "Guardar alerta"
                            )
                        }
                    }
                }

                // Configured Alerts List
                if (savedAlerts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Alertas configuradas",
                            variant = TextVariant.Large,
                            color = RikkaTheme.colors.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(savedAlerts) { alert ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            animation = CardAnimation.Press
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = RikkaTheme.colors.primary.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = RikkaTheme.colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Text(
                                        text = alert.currency,
                                        variant = TextVariant.Large,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                }

                                Text(
                                    text = "T.C.: ${alert.rate}",
                                    variant = TextVariant.Large,
                                    color = RikkaTheme.colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
