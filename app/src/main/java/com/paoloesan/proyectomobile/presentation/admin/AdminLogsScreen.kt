package com.paoloesan.proyectomobile.presentation.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.data.model.TransactionLogModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

// ── Configuración de tipos de evento ─────────────────────────────────────────

private data class EventConfig(
    val label: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val onBadgeColor: Color,
    val dotColor: Color
)

private val EVENT_CONFIGS = mapOf(
    "transaccion_iniciada" to EventConfig(
        "INICIO DE TRANSACCIÓN",
        Icons.Default.SwapHoriz,
        Color(0xFF1565C0), Color.White, Color(0xFF42A5F5)
    ),
    "estado_cambiado" to EventConfig(
        "ESTADO CAMBIADO",
        Icons.Default.Refresh,
        Color(0xFF6A1B9A), Color.White, Color(0xFFCE93D8)
    ),
    "comprobante_subido" to EventConfig(
        "COMPROBANTE SUBIDO",
        Icons.Default.Description,
        Color(0xFF00695C), Color.White, Color(0xFF4DB6AC)
    ),
    "confirmacion_usuario" to EventConfig(
        "CONFIRMACIÓN",
        Icons.Default.CheckCircle,
        Color(0xFF2E7D32), Color.White, Color(0xFF81C784)
    ),
    "disputa_abierta" to EventConfig(
        "DISPUTA ABIERTA",
        Icons.Default.Warning,
        Color(0xFFE65100), Color.White, Color(0xFFFFCC80)
    ),
    "disputa_resuelta" to EventConfig(
        "DISPUTA RESUELTA",
        Icons.Default.CheckCircle,
        Color(0xFF37474F), Color.White, Color(0xFF90A4AE)
    ),
    "calificacion_registrada" to EventConfig(
        "CALIFICACIÓN",
        Icons.Default.Star,
        Color(0xFFF57F17), Color.White, Color(0xFFFFD54F)
    ),
    "verificacion_ia" to EventConfig(
        "VERIFICACIÓN IA",
        Icons.Default.CheckCircle,
        Color(0xFF00838F), Color.White, Color(0xFF80DEEA)
    )
)

private val DEFAULT_CONFIG = EventConfig(
    "EVENTO",
    Icons.Default.Description,
    Color(0xFF37474F), Color.White, Color(0xFF90A4AE)
)

// Todos los tipos de evento disponibles para el filtro (compartido con ViewModel)
private val ALL_EVENT_TYPES = ALL_LOG_EVENT_TYPES.toList()

// ── Pantalla principal ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLogsScreen(
    navController: NavController,
    viewModel: AdminLogsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            scope.launch {
                toastState.show(message = msg, variant = ToastVariant.Destructive)
            }
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
            snackbarHost = { ToastHost(toastState) },
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RikkaTheme.colors.background)
                ) {
                    // ── TopBar ────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { navController.popBackStack() },
                            variant = ButtonVariant.Ghost,
                            size = ButtonSize.Icon
                        ) {
                            Icon(
                                imageVector = RikkaIcons.ArrowLeft,
                                contentDescription = "Regresar",
                                tint = RikkaTheme.colors.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auditoría de Procesos",
                                variant = TextVariant.Large,
                                color = RikkaTheme.colors.onBackground
                            )
                            Text(
                                text = "Visualiza cada paso del flujo transaccional",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.5f)
                            )
                        }
                        Button(
                            onClick = { viewModel.loadLogs() },
                            variant = ButtonVariant.Ghost,
                            size = ButtonSize.Icon
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                tint = RikkaTheme.colors.onBackground
                            )
                        }
                    }

                    HorizontalDivider(color = RikkaTheme.colors.onBackground.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Buscador + Botón de filtro ────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Input(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = "Buscar por ID, usuario o descripción...",
                            leadingIcon = Icons.Default.Search,
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Botón de filtro con badge de cantidad seleccionada
                        val filterCount = uiState.selectedFilters.size
                        Box {
                            Button(
                                onClick = { showFilterSheet = true },
                                variant = if (filterCount > 0) ButtonVariant.Default else ButtonVariant.Outline,
                                size = ButtonSize.Icon
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtrar eventos",
                                    tint = if (filterCount > 0) RikkaTheme.colors.onPrimary
                                    else RikkaTheme.colors.onBackground
                                )
                            }
                            // Badge contador
                            if (filterCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFFE53935), CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filterCount.toString(),
                                        variant = TextVariant.Small,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = RikkaTheme.colors.onBackground.copy(alpha = 0.06f))
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    uiState.isLoading && uiState.logs.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RikkaTheme.colors.primary)
                        }
                    }

                    uiState.filteredLogs.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = RikkaTheme.colors.onBackground.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Sin logs registrados",
                                    variant = TextVariant.P,
                                    color = RikkaTheme.colors.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 32.dp
                            )
                        ) {
                            item {
                                Text(
                                    text = "${uiState.filteredLogs.size} eventos encontrados",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.onBackground.copy(alpha = 0.45f),
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                            }

                            items(
                                items = uiState.filteredLogs,
                                key = { it.logId ?: it.hashCode().toLong() }
                            ) { log ->
                                LogTimelineItem(log = log)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Bottom Sheet de filtros ───────────────────────────────────────────────
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState,
            containerColor = RikkaTheme.colors.background
        ) {
            FilterBottomSheet(
                selectedFilters = uiState.selectedFilters,
                onToggle = { viewModel.toggleFilter(it) },
                onClear = { viewModel.clearFilters() },
                onApply = {
                    scope.launch { filterSheetState.hide() }
                        .invokeOnCompletion { showFilterSheet = false }
                }
            )
        }
    }
}

// ── Bottom Sheet de selección de filtros ──────────────────────────────────────

@Composable
private fun FilterBottomSheet(
    selectedFilters: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tipos de Evento",
                variant = TextVariant.H2,
                color = RikkaTheme.colors.onBackground
            )
            if (selectedFilters.isNotEmpty()) {
                Button(
                    onClick = onClear,
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Sm,
                    text = "Restablecer"
                )
            }
        }

        Text(
            text = "Selecciona los eventos que deseas ver",
            variant = TextVariant.Small,
            color = RikkaTheme.colors.onBackground.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = RikkaTheme.colors.onBackground.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(8.dp))

        // Checkboxes de tipos de evento
        ALL_EVENT_TYPES.forEach { eventType ->
            val config = EVENT_CONFIGS[eventType] ?: DEFAULT_CONFIG
            val isChecked = eventType in selectedFilters

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(eventType) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícono con color del evento
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = config.dotColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = config.icon,
                        contentDescription = null,
                        tint = config.dotColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Label del evento
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.label,
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                }

                // Checkbox
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onToggle(eventType) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = RikkaTheme.colors.primary,
                        uncheckedColor = RikkaTheme.colors.onBackground.copy(alpha = 0.4f),
                        checkmarkColor = RikkaTheme.colors.onPrimary
                    )
                )
            }

            HorizontalDivider(color = RikkaTheme.colors.onBackground.copy(alpha = 0.05f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón Aplicar
        Button(
            onClick = onApply,
            variant = ButtonVariant.Default,
            size = ButtonSize.Default,
            modifier = Modifier.fillMaxWidth(),
            text = if (selectedFilters.isEmpty()) "Ver todos los eventos"
            else "Aplicar (${selectedFilters.size} seleccionados)"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Item de la línea de tiempo ────────────────────────────────────────────────

@Composable
private fun LogTimelineItem(log: TransactionLogModel) {
    val config = EVENT_CONFIGS[log.tipoEvento] ?: DEFAULT_CONFIG
    var expanded by remember { mutableStateOf(false) }
    val hasExtra =
        !log.datosExtra.isNullOrBlank() && log.datosExtra != "{}" && log.datosExtra != "null"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // ── Línea de tiempo: dot + línea vertical ─────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = config.dotColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    tint = config.dotColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── Contenido del log ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(config.badgeColor)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = config.label,
                            variant = TextVariant.Small,
                            color = config.onBadgeColor
                        )
                    }
                    Text(
                        text = formatLogDate(log.fechaEvento),
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.5f)
                    )
                }

                log.transaccionId?.let { txId ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RikkaTheme.colors.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Transacción",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "#$txId",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.descripcion,
                variant = TextVariant.P,
                color = RikkaTheme.colors.onBackground
            )

            if (hasExtra) {
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    ExtraDataGrid(datosExtra = log.datosExtra ?: "")
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (expanded) "Ocultar datos" else "Ver datos",
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.primary
                    )
                    Icon(
                        imageVector = if (expanded) RikkaIcons.ChevronUp else RikkaIcons.ChevronDown,
                        contentDescription = null,
                        tint = RikkaTheme.colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = RikkaTheme.colors.onBackground.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ── Grid de datos extra ───────────────────────────────────────────────────────

@Composable
private fun ExtraDataGrid(datosExtra: String) {
    val pairs = remember(datosExtra) { parseJsonFlat(datosExtra) }
    if (pairs.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RikkaTheme.colors.onBackground.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val chunked = pairs.chunked(3)
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (key, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = key.uppercase().replace("_", " "),
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.onBackground.copy(alpha = 0.45f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = value,
                            variant = TextVariant.P,
                            color = RikkaTheme.colors.onBackground
                        )
                    }
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun parseJsonFlat(json: String): List<Pair<String, String>> {
    return try {
        val obj = JSONObject(json)
        obj.keys().asSequence().map { key ->
            val raw = obj.opt(key)
            val value = when {
                raw == null || raw == JSONObject.NULL -> "—"
                else -> raw.toString()
            }
            key to value
        }.toList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun formatLogDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val instant = java.time.Instant.parse(isoDate)
        val local = instant.atZone(java.time.ZoneId.systemDefault())
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm:ss a")
        local.format(formatter)
    } catch (e: Exception) {
        isoDate.take(16).replace("T", " ")
    }
}
