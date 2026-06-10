package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.presentation.navigation.Destination
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardAnimation
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    navController: NavController,
    viewModel: MarketplaceViewModel = viewModel()
) {
    val offers by viewModel.filteredOffers.collectAsState()
    val activeFilters by viewModel.filters.collectAsState()

    var showFilterBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val focusManager = LocalFocusManager.current

    // Temporary local states for filter controls inside bottom sheet
    var tempCurrency by remember(showFilterBottomSheet) { mutableStateOf(activeFilters.currency) }
    var tempType by remember(showFilterBottomSheet) { mutableStateOf(activeFilters.type) }
    var tempAmountStr by remember(showFilterBottomSheet) {
        mutableStateOf(
            activeFilters.amount?.toString() ?: ""
        )
    }
    var tempPaymentMethod by remember(showFilterBottomSheet) { mutableStateOf(activeFilters.paymentMethod) }

    val currencies = listOf("TODOS", "USD", "PEN")
    val types = listOf("TODOS", "Compra", "Venta")
    val paymentMethods = listOf("TODOS", "BCP", "Yape", "Interbank")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            topBar = {
                // Transparent Top Bar with Centered Title
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
                            contentDescription = "Volver",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }

                    Text(
                        text = "Mercado P2P",
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header of listings & filter action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ofertas Disponibles",
                        variant = TextVariant.H2,
                        color = RikkaTheme.colors.onBackground
                    )

                    Button(
                        onClick = { showFilterBottomSheet = true },
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Sm
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                modifier = Modifier.size(16.dp),
                                tint = RikkaTheme.colors.onBackground
                            )
                            Text(
                                text = "Filtrar",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.onBackground
                            )
                        }
                    }
                }

                // Active Filters Row
                val hasActiveFilters = activeFilters.currency != "TODOS" ||
                        activeFilters.type != "TODOS" ||
                        activeFilters.amount != null ||
                        activeFilters.paymentMethod != "TODOS"

                if (hasActiveFilters) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeFilters.currency != "TODOS") {
                            ActiveFilterChip(
                                label = "Moneda: ${activeFilters.currency}",
                                onDismiss = {
                                    viewModel.applyFilters(
                                        currency = "TODOS",
                                        type = activeFilters.type,
                                        amount = activeFilters.amount,
                                        paymentMethod = activeFilters.paymentMethod
                                    )
                                }
                            )
                        }
                        if (activeFilters.type != "TODOS") {
                            ActiveFilterChip(
                                label = "Operación: ${activeFilters.type}",
                                onDismiss = {
                                    viewModel.applyFilters(
                                        currency = activeFilters.currency,
                                        type = "TODOS",
                                        amount = activeFilters.amount,
                                        paymentMethod = activeFilters.paymentMethod
                                    )
                                }
                            )
                        }
                        if (activeFilters.amount != null) {
                            ActiveFilterChip(
                                label = "Monto: ${activeFilters.amount}",
                                onDismiss = {
                                    viewModel.applyFilters(
                                        currency = activeFilters.currency,
                                        type = activeFilters.type,
                                        amount = null,
                                        paymentMethod = activeFilters.paymentMethod
                                    )
                                }
                            )
                        }
                        if (activeFilters.paymentMethod != "TODOS") {
                            ActiveFilterChip(
                                label = "Pago: ${activeFilters.paymentMethod}",
                                onDismiss = {
                                    viewModel.applyFilters(
                                        currency = activeFilters.currency,
                                        type = activeFilters.type,
                                        amount = activeFilters.amount,
                                        paymentMethod = "TODOS"
                                    )
                                }
                            )
                        }
                    }
                }

                // Offers List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        PartialOffersInfoCard()
                    }

                    item {
                        // Matching Automático Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate(Destination.Matches.route) },
                            animation = CardAnimation.Press
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
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
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = null,
                                            tint = RikkaTheme.colors.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Matching Automático",
                                            variant = TextVariant.P,
                                            color = RikkaTheme.colors.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Encuentra ofertas ideales al instante",
                                            variant = TextVariant.Small,
                                            color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = RikkaTheme.colors.onBackground.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (offers.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No se encontraron ofertas disponibles",
                                    variant = TextVariant.Large,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(offers, key = { it.id }) { offer ->
                            OfferCard(
                                offer = offer,
                                onSelect = {
                                    navController.navigate("offerDetail/${offer.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for search filters
    if (showFilterBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = sheetState,
            containerColor = RikkaTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Filtros de búsqueda",
                    variant = TextVariant.H2,
                    color = RikkaTheme.colors.onBackground
                )

                // Currency Filter Choice
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Moneda",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { curr ->
                            val isSelected = tempCurrency == curr
                            Button(
                                onClick = { tempCurrency = curr },
                                variant = if (isSelected) ButtonVariant.Default else ButtonVariant.Outline,
                                size = ButtonSize.Sm,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = curr, variant = TextVariant.P)
                            }
                        }
                    }
                }

                // Operation Filter Choice
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tipo de operación",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { op ->
                            val isSelected = tempType == op
                            Button(
                                onClick = { tempType = op },
                                variant = if (isSelected) ButtonVariant.Default else ButtonVariant.Outline,
                                size = ButtonSize.Sm,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = op, variant = TextVariant.P)
                            }
                        }
                    }
                }

                // Amount Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Monto",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Input(
                        value = tempAmountStr,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                tempAmountStr = input
                            }
                        },
                        placeholder = "Ingresa el monto a buscar",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Payment Method Filter Choice
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Método de pago",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            val isSelected = tempPaymentMethod == method
                            Button(
                                onClick = { tempPaymentMethod = method },
                                variant = if (isSelected) ButtonVariant.Default else ButtonVariant.Outline,
                                size = ButtonSize.Sm
                            ) {
                                Text(text = method, variant = TextVariant.P)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Apply Action Button
                Button(
                    onClick = {
                        val amountVal = tempAmountStr.toDoubleOrNull()
                        viewModel.applyFilters(
                            currency = tempCurrency,
                            type = tempType,
                            amount = amountVal,
                            paymentMethod = tempPaymentMethod
                        )
                        showFilterBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = "Aplicar filtros"
                )
            }
        }
    }
}

@Composable
fun ActiveFilterChip(
    label: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = RikkaTheme.colors.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                variant = TextVariant.Small,
                color = RikkaTheme.colors.primary
            )
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Limpiar filtro",
                tint = RikkaTheme.colors.primary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onDismiss)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferCard(
    offer: P2POffer,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        animation = CardAnimation.Press
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = RikkaTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = offer.username,
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isCompra = offer.type == "Compra"
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = offer.type,
                        variant = TextVariant.P,
                        color = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${offer.currency} ${offer.amount}",
                    variant = TextVariant.H3,
                    color = RikkaTheme.colors.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = RikkaTheme.colors.onBackground,
                        modifier = Modifier.width(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "T.C.: ${offer.rate}",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Limites: Min ${offer.minLimit} / Max ${offer.maxLimit}",
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.width(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = offer.paymentMethod,
                        variant = TextVariant.Small,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun PartialOffersInfoCard() {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = RikkaTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "¿Cómo funcionan las ofertas?",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )
                }
                androidx.compose.material3.Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Al publicar una oferta, permites compras o ventas parciales según tus límites por transacción. El sistema descuenta el saldo automáticamente y ajusta inteligentemente el límite máximo si supera el monto restante disponible.",
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Example Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = RikkaTheme.colors.muted.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Ejemplo práctico (Oferta de 1000 USD | Min: 100 / Max: 400):",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.primary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Usuario A toma 400 USD -> Quedan 600 USD de saldo (límite máximo sigue en 400 USD).",
                                variant = TextVariant.Small,
                                color = Color.Gray
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Usuario B toma 350 USD -> Quedan 250 USD de saldo.",
                                variant = TextVariant.Small,
                                color = Color.Gray
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Ajuste Inteligente -> El límite máximo se reduce automáticamente a 250 USD.",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.success
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Usuario C toma los 250 USD restantes -> Monto total llega a 0 y la oferta se completa.",
                                variant = TextVariant.Small,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
