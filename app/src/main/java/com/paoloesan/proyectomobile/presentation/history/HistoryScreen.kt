package com.paoloesan.proyectomobile.presentation.history

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardAnimation
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val viewModel: HistoryViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var operacionSeleccionada by remember { mutableStateOf<HistoryUiItem?>(null) }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            toastState.show(message = it, variant = ToastVariant.Success)
            viewModel.consumeSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            toastState.show(message = it, variant = ToastVariant.Destructive)
            viewModel.consumeError()
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
                        text = "Historial",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RikkaTheme.colors.primary)
                }
            } else if (uiState.historyItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { focusManager.clearFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes transacciones finalizadas",
                        variant = TextVariant.Large,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { focusManager.clearFocus() }
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.historyItems, key = { it.transactionId }) { item ->
                        HistoryItemCard(
                            item = item,
                            isRated = item.transactionId in uiState.ratedTransactionIds,
                            onCalificarClick = {
                                operacionSeleccionada = item
                                rating = 0
                                comentario = ""
                                mostrarDialogo = true
                            },
                            onCardClick = {
                                navController.navigate("transactionStatus/${item.transactionId}")
                            }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogo && operacionSeleccionada != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
                rating = 0
                comentario = ""
            },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = "Calificar operaci\u00f3n",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "\u00bfC\u00f3mo calificar\u00eda su experiencia?",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { rating = i }) {
                                androidx.compose.material3.Icon(
                                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rating $i",
                                    tint = if (i <= rating) RikkaTheme.colors.primary else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Input(
                        value = comentario,
                        onValueChange = { comentario = it },
                        placeholder = "Comentario (Opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = rating > 0,
                    onClick = {
                        operacionSeleccionada?.let { item ->
                            viewModel.submitRating(
                                transactionId = item.transactionId,
                                contraparteId = item.contraparteId,
                                puntaje = rating,
                                comentario = comentario
                            )
                        }
                        mostrarDialogo = false
                        rating = 0
                        comentario = ""
                    },
                    text = "Enviar"
                )
            },
            dismissButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        rating = 0
                        comentario = ""
                    },
                    variant = ButtonVariant.Outline,
                    text = "Cancelar"
                )
            }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryUiItem,
    isRated: Boolean,
    onCalificarClick: () -> Unit,
    onCardClick: () -> Unit = {}
) {
    Card(
        onClick = onCardClick,
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
                    val isCompra = item.tipoOperacion == "Compra"
                    val icon = if (isCompra)
                        Icons.Default.AddCircleOutline
                    else
                        Icons.Default.RemoveCircleOutline

                    val color = if (isCompra)
                        RikkaTheme.colors.primary
                    else
                        RikkaTheme.colors.destructive

                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.tipoOperacion,
                        variant = TextVariant.Large,
                        color = color
                    )
                }
                Text(
                    text = item.fecha,
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monto",
                        variant = TextVariant.Small,
                        color = Color.Gray
                    )
                    Text(
                        text = "${item.moneda} ${String.format("%.2f", item.monto)}",
                        variant = TextVariant.H2,
                        color = RikkaTheme.colors.onBackground
                    )
                }

                if (isRated) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = RikkaTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Calificado",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.primary
                        )
                    }
                } else {
                    Button(
                        onClick = onCalificarClick,
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Sm,
                        text = "Calificar"
                    )
                }
            }
        }
    }
}
