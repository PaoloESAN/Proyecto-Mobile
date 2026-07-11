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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.paoloesan.proyectomobile.presentation.navigation.navigateSafe
import zed.rainxch.rikkaui.components.ui.skeleton.Skeleton
import zed.rainxch.rikkaui.components.ui.skeleton.SkeletonAnimation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.paoloesan.proyectomobile.presentation.components.OfflineScreen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
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
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    navController: NavController,
    viewModel: MarketplaceViewModel = viewModel()
) {
    val offers by viewModel.filteredOffers.collectAsState()
    val activeFilters by viewModel.filters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isNetworkError by viewModel.isNetworkError.collectAsState()

    var showFilterBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Temporary local states for filter controls inside bottom sheet
    var tempCurrency by remember(showFilterBottomSheet) { mutableStateOf(activeFilters.currency) }
    var tempType by remember(showFilterBottomSheet) { mutableStateOf(activeFilters.type) }
    var tempAmountStr by remember(showFilterBottomSheet) {
        mutableStateOf(
            activeFilters.amount?.toString() ?: ""
        )
    }
    var tempPaymentMethod by remember(showFilterBottomSheet) { mutableStateOf(activeFilters.paymentMethod) }
    var tempMinRating by remember(showFilterBottomSheet) { mutableStateOf(activeFilters.minRating) }

    val currencies = listOf("TODOS", "USD", "PEN", "MXN", "EUR", "GBP", "JPY")
    val types = listOf("TODOS", "Compra", "Venta")
    val paymentMethods = listOf("TODOS", "BCP", "Yape", "Interbank")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background
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
                Text(
                    text = "Mercado",
                    variant = TextVariant.H1,
                    color = RikkaTheme.colors.onBackground,
                    modifier = Modifier.padding(top = 16.dp)
                )

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
                                        paymentMethod = activeFilters.paymentMethod,
                                        minRating = activeFilters.minRating
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
                                        paymentMethod = activeFilters.paymentMethod,
                                        minRating = activeFilters.minRating
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
                                        paymentMethod = activeFilters.paymentMethod,
                                        minRating = activeFilters.minRating
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
                                        paymentMethod = "TODOS",
                                        minRating = activeFilters.minRating
                                    )
                                }
                            )
                        }
                        if (activeFilters.minRating != null) {
                            ActiveFilterChip(
                                label = "★ mín: ${String.format(java.util.Locale.US, "%.1f", activeFilters.minRating)}",
                                onDismiss = {
                                    viewModel.applyFilters(
                                        currency = activeFilters.currency,
                                        type = activeFilters.type,
                                        amount = activeFilters.amount,
                                        paymentMethod = activeFilters.paymentMethod,
                                        minRating = null
                                    )
                                }
                            )
                        }
                    }
                }

                // Offers List
                if (isNetworkError) {
                    OfflineScreen(onRetry = { viewModel.loadData() })
                } else if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, RikkaTheme.colors.muted.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Skeleton(
                                        modifier = Modifier.width(120.dp).height(20.dp),
                                        animation = SkeletonAnimation.Shimmer
                                    )
                                    Skeleton(
                                        modifier = Modifier.size(24.dp),
                                        shape = RikkaTheme.shapes.full,
                                        animation = SkeletonAnimation.Shimmer
                                    )
                                }
                                Skeleton(
                                    modifier = Modifier.fillMaxWidth(0.7f).height(16.dp),
                                    animation = SkeletonAnimation.Shimmer
                                )
                                Skeleton(
                                    modifier = Modifier.fillMaxWidth(0.5f).height(16.dp),
                                    animation = SkeletonAnimation.Shimmer
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // Matching Automático Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navController.navigateSafe(Destination.Matches.route) },
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
                                        navController.navigateSafe("offerDetail/${offer.id}")
                                    }
                                )
                            }
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

                // Rating Filter
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Calificación mínima del vendedor",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Todos" option
                        val isAllSelected = tempMinRating == null
                        Button(
                            onClick = { tempMinRating = null },
                            variant = if (isAllSelected) ButtonVariant.Default else ButtonVariant.Outline,
                            size = ButtonSize.Sm
                        ) {
                            Text(text = "Todos", variant = TextVariant.P)
                        }
                        listOf(3.0, 4.0, 4.5).forEach { rating ->
                            val isSelected = tempMinRating == rating
                            Button(
                                onClick = { tempMinRating = rating },
                                variant = if (isSelected) ButtonVariant.Default else ButtonVariant.Outline,
                                size = ButtonSize.Sm
                            ) {
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", rating)}★",
                                    variant = TextVariant.P
                                )
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
                            paymentMethod = tempPaymentMethod,
                            minRating = tempMinRating
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
            // Header: Creator name and type (Compra/Venta)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar: foto real o icono genérico como fallback
                    if (offer.avatarUrl != null) {
                        AsyncImage(
                            model = offer.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.dp,
                                    color = RikkaTheme.colors.primary.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = RikkaTheme.colors.primary.copy(alpha = 0.12f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = RikkaTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompra) "Compra" else "Venta",
                        variant = TextVariant.Small,
                        color = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversion Flow: Entregas -> Recibes (Perspectiva del observador)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Entrega",
                        variant = TextVariant.Small,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${
                            String.format(
                                java.util.Locale.US,
                                "%,.2f",
                                offer.montoRecibo
                            )
                        } ${offer.monedaRecibo}",
                        variant = TextVariant.H3,
                        color = RikkaTheme.colors.onBackground
                    )
                }

                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Recibe",
                        variant = TextVariant.Small,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${
                            String.format(
                                java.util.Locale.US,
                                "%,.2f",
                                offer.montoTengo
                            )
                        } ${offer.monedaTengo}",
                        variant = TextVariant.H3,
                        color = RikkaTheme.colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: T.C. a la izquierda + Calificación en estrellas a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "T.C.: ${String.format(java.util.Locale.US, "%.4f", offer.rate)}",
                        variant = TextVariant.Small,
                        color = Color.Gray
                    )
                }

                // Calificación en estrellas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val starFull = offer.calificacion.toInt()
                    val hasHalf = (offer.calificacion - starFull) >= 0.25
                    repeat(5) { i ->
                        val tint = if (i < starFull || (i == starFull && hasHalf))
                            androidx.compose.ui.graphics.Color(0xFFFFBB00)
                        else
                            Color.Gray.copy(alpha = 0.3f)
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", offer.calificacion),
                        variant = TextVariant.Small,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
