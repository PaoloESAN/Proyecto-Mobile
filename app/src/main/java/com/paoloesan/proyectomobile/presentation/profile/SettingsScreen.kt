package com.paoloesan.proyectomobile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsSuggest
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.presentation.navigation.Destination
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val currentTheme = SessionManager.themeState.value
    val sheetState = rememberModalBottomSheetState()
    var showThemeSheet by remember { mutableStateOf(false) }

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
                        text = "Configuración",
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
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Menu items Card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsMenuItem(
                            icon = Icons.Default.Person,
                            title = "Editar información personal",
                            onClick = { navController.navigate("edit_profile") }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(RikkaTheme.colors.muted.copy(alpha = 0.2f))
                        )
                        SettingsMenuItem(
                            icon = Icons.Default.LightMode,
                            title = "Tema de la aplicación",
                            subtitle = when (currentTheme) {
                                "light" -> "Claro"
                                "dark" -> "Oscuro"
                                else -> "Sistema"
                            },
                            onClick = { showThemeSheet = true }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(RikkaTheme.colors.muted.copy(alpha = 0.2f))
                        )
                        SettingsMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Cerrar sesión",
                            textColor = RikkaTheme.colors.destructive,
                            onClick = {
                                SessionManager.clearToken(context)
                                navController.navigate(Destination.Login.route) {
                                    popUpTo(Destination.Marketplace.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showThemeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showThemeSheet = false },
                sheetState = sheetState,
                containerColor = RikkaTheme.colors.background
            ) {
                ThemeSelectionSheet(
                    currentTheme = currentTheme,
                    onThemeSelected = { theme ->
                        SessionManager.saveTheme(context, theme)
                    },
                    onDismiss = { showThemeSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelectionSheet(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tema de la aplicación",
            variant = TextVariant.H2,
            color = RikkaTheme.colors.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeChoiceButton(
                label = "Claro",
                icon = Icons.Default.LightMode,
                selected = currentTheme == "light",
                onClick = {
                    onThemeSelected("light")
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
            ThemeChoiceButton(
                label = "Oscuro",
                icon = Icons.Default.DarkMode,
                selected = currentTheme == "dark",
                onClick = {
                    onThemeSelected("dark")
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
            ThemeChoiceButton(
                label = "Sistema",
                icon = Icons.Default.SettingsSuggest,
                selected = currentTheme == "system",
                onClick = {
                    onThemeSelected("system")
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    textColor: Color = RikkaTheme.colors.onBackground,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (textColor == RikkaTheme.colors.destructive) RikkaTheme.colors.destructive else RikkaTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                variant = TextVariant.P,
                color = textColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
            }
        }
        androidx.compose.material3.Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ThemeChoiceButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) RikkaTheme.colors.primary else RikkaTheme.colors.muted.copy(alpha = 0.3f)
    val backgroundAlpha = if (selected) 0.12f else 0.04f
    val backgroundColor = if (selected) RikkaTheme.colors.primary.copy(alpha = backgroundAlpha) else Color.Transparent
    val contentColor = if (selected) RikkaTheme.colors.primary else RikkaTheme.colors.onBackground.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                variant = TextVariant.Small,
                color = contentColor
            )
        }
    }
}
