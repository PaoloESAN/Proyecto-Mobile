package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    navController: NavController,
    viewModel: MatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showBottomSheet by remember { mutableStateOf(false) }

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
                        text = "Coincidencias Automáticas",
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
            ) {
                // Si hay error en la UI, mostrarlo
                if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .background(
                                RikkaTheme.colors.destructive.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                RikkaTheme.colors.destructive.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            variant = TextVariant.P,
                            color = RikkaTheme.colors.destructive
                        )
                    }
                }

                if (uiState.myActiveOffers.isEmpty() && !uiState.isLoading) {
                    // El usuario no tiene ofertas activas para emparejar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No tienes ofertas activas publicadas",
                                variant = TextVariant.H3,
                                color = RikkaTheme.colors.onBackground
                            )
                            Text(
                                text = "Para usar el Matching Automático, primero debes publicar una oferta de compra o venta en el mercado.",
                                variant = TextVariant.P,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Button(
                                onClick = {
                                    navController.navigate("publish_offer")
                                },
                                variant = ButtonVariant.Outline,
                                text = "Publicar Oferta"
                            )
                        }
                    }
                } else {
                    // Sección 1: Botón selector de ofertas activas del usuario (abre Bottom Sheet)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        onClick = { showBottomSheet = true },
                        animation = CardAnimation.Press
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Oferta seleccionada para matching:",
                                    variant = TextVariant.Small,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                uiState.selectedOffer?.let { offer ->
                                    Text(
                                        text = "${offer.tipoOperacion} - ${offer.currency} ${offer.montoTotal} (T.C. ${offer.price})",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                } ?: Text(
                                    text = "Seleccione una oferta...",
                                    variant = TextVariant.P,
                                    color = RikkaTheme.colors.onBackground
                                )
                            }
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(RikkaTheme.colors.muted.copy(alpha = 0.15f))
                    )

                    // Sección 2: Resultados del matching
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = RikkaTheme.colors.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    } else if (uiState.matches.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron ofertas compatibles en el mercado",
                                variant = TextVariant.P,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(uiState.matches, key = { it.ofertaId }) { match ->
                                val isCompra = match.tipoOperacion == "Compra"
                                Card(
                                    onClick = {
                                        navController.navigate("offerDetail/${match.ofertaId}")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    animation = CardAnimation.Press
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = RikkaTheme.colors.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = match.nombreCreador,
                                                    variant = TextVariant.P,
                                                    color = RikkaTheme.colors.onBackground
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = null,
                                                    tint = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = match.tipoOperacion,
                                                    variant = TextVariant.P,
                                                    color = if (isCompra) RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${match.moneda} ${match.montoTotal}",
                                                variant = TextVariant.H3,
                                                color = RikkaTheme.colors.onBackground
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = Icons.Default.AttachMoney,
                                                    contentDescription = null,
                                                    tint = RikkaTheme.colors.onBackground,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "T.C.: ${match.tipoCambio}",
                                                    variant = TextVariant.P,
                                                    color = RikkaTheme.colors.onBackground
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Límites: Min ${match.montoMinimo.toInt()} / Max ${match.montoMaximo.toInt()}",
                                                variant = TextVariant.Small,
                                                color = Color.Gray
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = Icons.Default.Payment,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color.Gray
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = match.banco,
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
                }
            }
        }

        // Modal Bottom Sheet para la selección de ofertas en 2 columnas
        if (showBottomSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = RikkaTheme.colors.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Selecciona una Oferta Activa",
                        variant = TextVariant.Large,
                        color = RikkaTheme.colors.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    val chunkedOffers = uiState.myActiveOffers.chunked(2)
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chunkedOffers) { rowOffers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowOffers.forEach { offer ->
                                    val isSelected = uiState.selectedOffer?.offerId == offer.offerId
                                    val borderColor =
                                        if (isSelected) RikkaTheme.colors.primary else RikkaTheme.colors.muted.copy(
                                            alpha = 0.2f
                                        )
                                    val bgColor =
                                        if (isSelected) RikkaTheme.colors.primary.copy(alpha = 0.05f) else RikkaTheme.colors.background

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                                            .background(bgColor, RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.selectOffer(offer)
                                                showBottomSheet = false
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "${offer.tipoOperacion} - ${offer.currency}",
                                                variant = TextVariant.Small,
                                                color = if (offer.tipoOperacion == "Compra") RikkaTheme.colors.primary else RikkaTheme.colors.destructive
                                            )
                                            Text(
                                                text = "Monto: ${offer.currency} ${offer.montoTotal}",
                                                variant = TextVariant.P,
                                                color = RikkaTheme.colors.onBackground
                                            )
                                            Text(
                                                text = "T.C.: ${offer.price}",
                                                variant = TextVariant.Small,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                                if (rowOffers.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}