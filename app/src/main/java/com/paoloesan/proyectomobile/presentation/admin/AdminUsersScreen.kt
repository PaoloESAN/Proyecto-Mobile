package com.paoloesan.proyectomobile.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.PopupAnimation
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.card.CardAnimation
import zed.rainxch.rikkaui.components.ui.dropdown.DropdownMenu
import zed.rainxch.rikkaui.components.ui.dropdown.DropdownMenuItem
import zed.rainxch.rikkaui.components.ui.dropdown.DropdownMenuLabel
import zed.rainxch.rikkaui.components.ui.dropdown.DropdownMenuSeparator
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

private enum class AdminAction(val title: String, val verb: String) {
    ACTIVATE("Activar usuario", "activar"),
    BLOCK("Bloquear usuario", "bloquear")
}

@Composable
fun AdminUsersScreen(
    navController: NavController,
    viewModel: AdminUsersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    var pendingAction by remember { mutableStateOf<Pair<User, AdminAction>?>(null) }
    var pendingBlockDuration by remember { mutableStateOf("") }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    LaunchedEffect(uiState.transientMessage) {
        uiState.transientMessage?.let { message ->
            scope.launch {
                toastState.show(
                    message = message,
                    variant = ToastVariant.Success
                )
            }
            viewModel.consumeMessage()
        }
    }

    // Filter users list based on query and status filter
    val filteredUsers = uiState.usuarios.filter { user ->
        val matchesSearch = user.nombre.contains(searchQuery, ignoreCase = true) ||
                user.correo.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "Todos" || user.estado == selectedFilter
        matchesSearch && matchesFilter
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
                        text = "Administración",
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Input
                Input(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Buscar usuario por nombre o correo...",
                    leadingIcon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf("Todos", "Activo", "Bloqueado")
                    filterOptions.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Button(
                            onClick = { selectedFilter = filter },
                            variant = if (isSelected) ButtonVariant.Default else ButtonVariant.Outline,
                            size = ButtonSize.Sm,
                            text = filter
                        )
                    }
                }

                // Users count summary
                Text(
                    text = "Usuarios encontrados: ${filteredUsers.size}",
                    variant = TextVariant.Small,
                    color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                if (filteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron usuarios",
                            variant = TextVariant.P,
                            color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            UserCard(
                                user = user,
                                onVerDetalle = {
                                    viewModel.setMessage(
                                        "Detalle: ${user.nombre} (${user.correo}) - Bloqueos anteriores: ${user.bloqueosAnteriores}"
                                    )
                                },
                                onActivar = { pendingAction = user to AdminAction.ACTIVATE },
                                onBloquear = { duration ->
                                    pendingBlockDuration = duration
                                    pendingAction = user to AdminAction.BLOCK
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingAction?.let { (user, action) ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            containerColor = RikkaTheme.colors.background,
            title = {
                Text(
                    text = action.title,
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                val durationText =
                    if (action == AdminAction.BLOCK) {
                        if (pendingBlockDuration == "indefinidamente") " indefinidamente" else " por $pendingBlockDuration"
                    } else ""
                Text(
                    text = "¿Estás seguro de ${action.verb} a ${user.nombre}$durationText?",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (action) {
                            AdminAction.ACTIVATE -> viewModel.activateUser(user.id)
                            AdminAction.BLOCK -> viewModel.blockUser(user.id, pendingBlockDuration)
                        }
                        pendingAction = null
                    },
                    size = ButtonSize.Sm,
                    text = "Confirmar"
                )
            },
            dismissButton = {
                Button(
                    onClick = { pendingAction = null },
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Sm,
                    text = "Cancelar"
                )
            }
        )
    }
}

@Composable
private fun UserCard(
    user: User,
    onVerDetalle: () -> Unit,
    onActivar: () -> Unit,
    onBloquear: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        animation = CardAnimation.Press
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar Badge
                val initials =
                    user.nombre.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2)
                        .uppercase()
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = when (user.estado) {
                                "Activo" -> RikkaTheme.colors.primary.copy(alpha = 0.12f)
                                "Bloqueado" -> Color(0xFFFFCDD2)
                                else -> RikkaTheme.colors.muted.copy(alpha = 0.12f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        variant = TextVariant.P,
                        color = when (user.estado) {
                            "Activo" -> RikkaTheme.colors.primary
                            "Bloqueado" -> Color(0xFFB71C1C)
                            else -> RikkaTheme.colors.onBackground
                        },
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.nombre,
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.correo,
                        variant = TextVariant.Small,
                        color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(user.estado)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = RikkaTheme.colors.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Bloqueos: ${user.bloqueosAnteriores}",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            HorizontalDivider(color = RikkaTheme.colors.onBackground.copy(alpha = 0.1f))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View detail button
                Button(
                    onClick = onVerDetalle,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Sm,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = RikkaTheme.colors.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Detalle",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                // Contextual block/activate actions
                if (user.estado != "Activo") {
                    Button(
                        onClick = onActivar,
                        variant = ButtonVariant.Default,
                        size = ButtonSize.Sm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = RikkaTheme.colors.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Activar",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.onPrimary
                            )
                        }
                    }
                } else {
                    // Bloquear Dropdown Menu Trigger
                    var openDropdown by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.weight(1f)) {
                        DropdownMenu(
                            expanded = openDropdown,
                            onDismiss = { openDropdown = false },
                            animation = PopupAnimation.FadeExpand,
                            trigger = {
                                Button(
                                    onClick = { openDropdown = true },
                                    variant = ButtonVariant.Destructive,
                                    size = ButtonSize.Sm,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = RikkaTheme.colors.onDestructiveTinted
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Bloquear",
                                            variant = TextVariant.Small,
                                            color = RikkaTheme.colors.onDestructiveTinted
                                        )
                                    }
                                }
                            }
                        ) {
                            DropdownMenuLabel("Bloquear por:")
                            DropdownMenuItem("1 hora", onClick = {
                                openDropdown = false
                                onBloquear("1 hora")
                            })
                            DropdownMenuItem("24 horas", onClick = {
                                openDropdown = false
                                onBloquear("24 horas")
                            })
                            DropdownMenuItem("7 días", onClick = {
                                openDropdown = false
                                onBloquear("7 días")
                            })
                            DropdownMenuItem("1 mes", onClick = {
                                openDropdown = false
                                onBloquear("1 mes")
                            })
                            DropdownMenuItem("3 meses", onClick = {
                                openDropdown = false
                                onBloquear("3 meses")
                            })
                            DropdownMenuSeparator()
                            DropdownMenuItem("Indefinido", onClick = {
                                openDropdown = false
                                onBloquear("indefinidamente")
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(estado: String) {
    val (bgColor, textColor) = when (estado) {
        "Activo" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Bloqueado" -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
        else -> RikkaTheme.colors.onBackground.copy(alpha = 0.1f) to RikkaTheme.colors.onBackground.copy(
            alpha = 0.6f
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado,
            color = textColor,
            variant = TextVariant.Small,
            modifier = Modifier.padding(bottom = 1.dp)
        )
    }
}
