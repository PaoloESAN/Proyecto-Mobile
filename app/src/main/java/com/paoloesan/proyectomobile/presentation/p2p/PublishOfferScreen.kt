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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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

data class LocalOffer(
    val type: String,
    val currency: String,
    val amount: String,
    val rate: String,
    val minAmount: String,
    val maxAmount: String,
    val paymentMethod: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishOfferScreen(navController: NavController) {
    var type by remember { mutableStateOf("Compra") }
    var currency by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("BCP") }

    var publishedOffers by remember { mutableStateOf(listOf<LocalOffer>()) }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val currencyOptions = listOf(
        SelectOption("USD", "USD"),
        SelectOption("PEN", "PEN")
    )

    val paymentOptions = listOf(
        SelectOption("BCP", "BCP"),
        SelectOption("Yape", "Yape"),
        SelectOption("Interbank", "Interbank")
    )

    val isMinMaxError by remember {
        derivedStateOf {
            val min = minAmount.toDoubleOrNull()
            val max = maxAmount.toDoubleOrNull()
            minAmount.isNotBlank() && maxAmount.isNotBlank() &&
                    min != null && max != null && min > max
        }
    }

    val isMinZero by remember {
        derivedStateOf {
            minAmount.isNotBlank() && (minAmount.toDoubleOrNull() ?: -1.0) <= 0
        }
    }

    val isMaxZero by remember {
        derivedStateOf {
            maxAmount.isNotBlank() && (maxAmount.toDoubleOrNull() ?: -1.0) <= 0
        }
    }

    val canPublish by remember {
        derivedStateOf {
            val min = minAmount.toDoubleOrNull()
            val max = maxAmount.toDoubleOrNull()
            minAmount.isNotBlank() && maxAmount.isNotBlank() &&
                    amount.isNotBlank() && rate.isNotBlank() &&
                    (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                    (rate.toDoubleOrNull() ?: 0.0) > 0 &&
                    min != null && min > 0 &&
                    max != null && max > 0 &&
                    !isMinMaxError
        }
    }

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
                        text = "Publicar Oferta",
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
                        text = "Crea una nueva oferta P2P",
                        variant = TextVariant.H2,
                        color = RikkaTheme.colors.primary
                    )
                }

                // Main Form Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Selector Compra/Venta
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Tipo de transaccion")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { type = "Compra" },
                                        variant = if (type == "Compra") ButtonVariant.Default else ButtonVariant.Outline,
                                        modifier = Modifier.weight(1f),
                                        text = "Compra"
                                    )
                                    Button(
                                        onClick = { type = "Venta" },
                                        variant = if (type == "Venta") ButtonVariant.Default else ButtonVariant.Outline,
                                        modifier = Modifier.weight(1f),
                                        text = "Venta"
                                    )
                                }
                            }

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

                            // Monto Input
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Monto")
                                Input(
                                    value = amount,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) {
                                            amount = input
                                        }
                                    },
                                    placeholder = "Ingrese monto",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = Icons.Default.AttachMoney,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
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
                                        }
                                    },
                                    placeholder = "3.75",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = Icons.Default.AttachMoney,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            // Monto Minimo Input
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Monto minimo")
                                Input(
                                    value = minAmount,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) {
                                            minAmount = input
                                        }
                                    },
                                    placeholder = "Minimo",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = Icons.Default.AttachMoney,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                if (isMinMaxError) {
                                    Text(
                                        text = "El monto minimo no puede ser mayor al maximo",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.destructive
                                    )
                                } else if (isMinZero) {
                                    Text(
                                        text = "El monto debe ser mayor a 0",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.destructive
                                    )
                                }
                            }

                            // Monto Maximo Input
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Monto maximo")
                                Input(
                                    value = maxAmount,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) {
                                            maxAmount = input
                                        }
                                    },
                                    placeholder = "Maximo",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = Icons.Default.AttachMoney,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                if (isMaxZero && !isMinMaxError) {
                                    Text(
                                        text = "El monto debe ser mayor a 0",
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.destructive
                                    )
                                }
                            }

                            // Metodo de Pago Select
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Metodo de Pago")
                                Select(
                                    selectedValue = paymentMethod,
                                    onValueChange = { paymentMethod = it },
                                    options = paymentOptions,
                                    placeholder = "Seleccione metodo de pago...",
                                    animation = PopupAnimation.Fade,
                                    maxHeight = 300.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Publish Button
                            Button(
                                enabled = canPublish,
                                onClick = {
                                    scope.launch {
                                        toastState.show(
                                            message = "Oferta publicada correctamente",
                                            variant = ToastVariant.Success
                                        )
                                        val newOffer = LocalOffer(
                                            type = type,
                                            currency = currency,
                                            amount = amount,
                                            rate = rate,
                                            minAmount = minAmount,
                                            maxAmount = maxAmount,
                                            paymentMethod = paymentMethod
                                        )
                                        publishedOffers = publishedOffers + newOffer
                                        // Limpiar campos
                                        amount = ""
                                        rate = ""
                                        minAmount = ""
                                        maxAmount = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                text = "Publicar oferta"
                            )
                        }
                    }
                }

                // Published Offers List
                if (publishedOffers.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ofertas en el mercado",
                            variant = TextVariant.Large,
                            color = RikkaTheme.colors.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(publishedOffers) { offer ->
                        val isCompra = offer.type == "Compra"
                        val operationColor =
                            if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            animation = CardAnimation.Press
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = operationColor.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = offer.type,
                                            variant = TextVariant.Small,
                                            color = operationColor
                                        )
                                    }

                                    Text(
                                        text = "${offer.currency} ${offer.amount}",
                                        variant = TextVariant.Large,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "T.C.: ${offer.rate}",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Metodo: ${offer.paymentMethod}",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(RikkaTheme.colors.muted.copy(alpha = 0.2f))
                                )

                                Text(
                                    text = "Limites: Min ${offer.minAmount} / Max ${offer.maxAmount}",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
