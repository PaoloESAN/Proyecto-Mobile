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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
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
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offerId: Int,
    onStartTransaction: (transactionId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: OfferDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()

    var showConfirmDialog by remember { mutableStateOf(false) }

    // Inicializar el ViewModel con el offerId real
    LaunchedEffect(offerId) {
        viewModel.initialize(offerId)
    }

    // Navegar cuando la transacción se crea exitosamente
    LaunchedEffect(uiState.createdTransactionId) {
        uiState.createdTransactionId?.let { txId ->
            viewModel.consumeCreatedTransaction()
            onStartTransaction(txId)
        }
    }

    // Mostrar errores
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            scope.launch {
                toastState.show(message = msg, variant = ToastVariant.Destructive)
            }
            viewModel.consumeError()
        }
    }

    val offer = uiState.offer
    val rate = offer?.price ?: 3.85
    val minLimit = offer?.montoMinimo ?: 50.0
    val maxLimit = offer?.montoMaximo ?: 200.0
    val currency = offer?.currency ?: "USD"

    val amountDouble = uiState.amountInput.toDoubleOrNull() ?: 0.0
    val isValidAmount = amountDouble >= minLimit && amountDouble <= maxLimit && amountDouble > 0.0

    val accountOptions = uiState.myPaymentMethods.map { pm ->
        SelectOption(
            value = pm.metodoPagoId.toString(),
            label = "${pm.banco} - ${pm.numeroCuenta} (${pm.tipoMoneda})"
        )
    }
    val selectedMethodOption = uiState.selectedMethodId?.toString() ?: accountOptions.firstOrNull()?.value ?: ""

    // Dialog de confirmación
    if (showConfirmDialog && offer != null) {
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Está seguro de que desea iniciar esta transacción?",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                RikkaTheme.colors.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Monto: ${uiState.amountInput} $currency",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.onBackground
                            )
                            Text(
                                text = "Tipo de cambio: 1 USD = $rate PEN",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Vendedor: ${uiState.vendorName}",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.createTransaction()
                    },
                    enabled = !uiState.isCreatingTransaction,
                    text = if (uiState.isCreatingTransaction) "Creando..." else "Confirmar"
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
            snackbarHost = { ToastHost(toastState) },
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
                        text = "Detalle de Oferta #$offerId",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->

            // Pantalla de carga
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RikkaTheme.colors.primary)
                }
                return@Scaffold
            }

            // Error fatal sin oferta
            if (offer == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "No se encontró la oferta #$offerId",
                            variant = TextVariant.P,
                            color = RikkaTheme.colors.onBackground
                        )
                        Button(onClick = onBack, text = "Volver", variant = ButtonVariant.Outline)
                    }
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() }
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
                    Card(modifier = Modifier.fillMaxWidth()) {
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
                                    text = uiState.vendorName,
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
                                        text = String.format("%.1f", uiState.vendorRating),
                                        variant = TextVariant.Small,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                }
                            }
                        }
                    }

                    // Input monto y método de pago
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Label(text = "Monto a recibir ($currency)")
                                Input(
                                    value = uiState.amountInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' }) {
                                            viewModel.onAmountChange(input)
                                        }
                                    },
                                    placeholder = "Ingrese monto...",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (!isValidAmount && uiState.amountInput.isNotEmpty()) {
                                    Text(
                                        text = "El monto debe estar entre ${String.format("%.2f", minLimit)} y ${String.format("%.2f", maxLimit)} $currency",
                                        color = RikkaTheme.colors.destructive,
                                        variant = TextVariant.Small
                                    )
                                }
                            }

                            if (accountOptions.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Label(text = "Recibir en mi cuenta bancaria")
                                    Select(
                                        selectedValue = selectedMethodOption,
                                        onValueChange = { value ->
                                            viewModel.onSelectPaymentMethod(value.toIntOrNull() ?: 0)
                                        },
                                        options = accountOptions,
                                        placeholder = "Seleccione cuenta...",
                                        animation = PopupAnimation.Fade,
                                        maxHeight = 200.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Text(
                                    text = "⚠️ No tienes métodos de pago registrados. Ve a tu perfil para agregar uno.",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.destructive
                                )
                            }
                        }
                    }

                    // Conversión card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Envías", variant = TextVariant.P, color = Color.Gray)
                                val enviasValue = String.format(java.util.Locale.US, "%,.2f", amountDouble * rate)
                                Text(
                                    text = "$enviasValue PEN",
                                    variant = TextVariant.H2,
                                    color = RikkaTheme.colors.onBackground
                                )
                            }

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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Recibes", variant = TextVariant.P, color = Color.Gray)
                                val recibesValue = String.format(java.util.Locale.US, "%,.2f", amountDouble)
                                Text(
                                    text = "$recibesValue $currency",
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

                    // Detalles de la oferta
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailItemRow(
                                icon = Icons.Default.CurrencyExchange,
                                label = "Tipo de operación",
                                value = "${offer.tipoOperacion} de ${offer.currency}"
                            )
                            DetailItemRow(
                                icon = Icons.Default.AccountBalance,
                                label = "Moneda de la oferta",
                                value = offer.currency
                            )
                            DetailItemRow(
                                icon = Icons.Default.Tune,
                                label = "Límites permitidos",
                                value = "Mín ${String.format("%.2f", minLimit)} / Máx ${String.format("%.2f", maxLimit)} $currency"
                            )
                            DetailItemRow(
                                icon = Icons.Default.TrendingUp,
                                label = "Disponible",
                                value = "${String.format("%.2f", offer.montoTotal)} $currency"
                            )
                        }
                    }
                }

                // Botón de acción fijo en el bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = isValidAmount && accountOptions.isNotEmpty() && !uiState.isCreatingTransaction,
                        modifier = Modifier.fillMaxWidth(),
                        text = if (uiState.isCreatingTransaction) "Creando transacción..." else "Iniciar transacción"
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

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, variant = TextVariant.Small, color = Color.Gray)
            Text(text = value, variant = TextVariant.P, color = RikkaTheme.colors.onBackground)
        }
    }
}
