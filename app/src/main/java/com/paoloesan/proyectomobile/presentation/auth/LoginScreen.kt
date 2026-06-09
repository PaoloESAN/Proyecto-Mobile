package com.paoloesan.proyectomobile.presentation.auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.presentation.navigation.Destination
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.checkbox.Checkbox
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

// Custom inline vector icon for GitHub
val GitHubIcon: ImageVector
    get() = ImageVector.Builder(
        name = "GitHub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF181717)),
            strokeLineWidth = 0f
        ) {
            moveTo(12f, 2f)
            curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
            curveTo(2f, 16.42f, 4.865f, 20.166f, 8.839f, 21.489f)
            curveTo(9.339f, 21.581f, 9.521f, 21.283f, 9.521f, 21.018f)
            curveTo(9.521f, 20.781f, 9.513f, 20.152f, 9.508f, 19.318f)
            curveTo(6.726f, 19.921f, 6.139f, 17.978f, 6.139f, 17.978f)
            curveTo(5.685f, 16.822f, 5.029f, 16.514f, 5.029f, 16.514f)
            curveTo(4.121f, 15.894f, 5.098f, 15.906f, 5.098f, 15.906f)
            curveTo(6.101f, 15.976f, 6.629f, 16.936f, 6.629f, 16.936f)
            curveTo(7.521f, 18.465f, 8.97f, 18.023f, 9.539f, 17.767f)
            curveTo(9.631f, 17.121f, 9.889f, 16.681f, 10.175f, 16.431f)
            curveTo(7.955f, 16.178f, 5.62f, 15.321f, 5.62f, 11.488f)
            curveTo(5.62f, 10.397f, 6.01f, 9.504f, 6.649f, 8.805f)
            curveTo(6.546f, 8.552f, 6.203f, 7.535f, 6.747f, 6.158f)
            curveTo(6.747f, 6.158f, 7.587f, 5.889f, 9.497f, 7.183f)
            curveTo(10.295f, 6.961f, 11.15f, 6.85f, 12f, 6.846f)
            curveTo(12.85f, 6.85f, 13.705f, 6.961f, 14.503f, 7.183f)
            curveTo(16.413f, 5.889f, 17.253f, 6.158f, 17.253f, 6.158f)
            curveTo(17.797f, 7.535f, 17.454f, 8.552f, 17.351f, 8.805f)
            curveTo(17.99f, 9.504f, 18.38f, 10.397f, 18.38f, 11.488f)
            curveTo(18.38f, 15.329f, 16.041f, 16.175f, 13.814f, 16.423f)
            curveTo(14.173f, 16.732f, 14.492f, 17.342f, 14.492f, 18.275f)
            curveTo(14.492f, 19.611f, 14.48f, 20.69f, 14.48f, 21.018f)
            curveTo(14.48f, 21.285f, 14.66f, 21.587f, 15.168f, 21.487f)
            curveTo(19.137f, 20.164f, 22f, 16.418f, 22f, 12f)
            curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
            close()
        }
    }.build()

// Custom inline vector icon for GitLab
val GitLabIcon: ImageVector
    get() = ImageVector.Builder(
        name = "GitLab",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFFE24329)),
            strokeLineWidth = 0f
        ) {
            moveTo(22.65f, 14.39f)
            lineTo(12f, 22.13f)
            lineTo(1.35f, 14.39f)
            curveTo(1.03f, 14.16f, 0.9f, 13.74f, 1.05f, 13.47f)
            lineTo(4.46f, 2.97f)
            curveTo(4.57f, 2.63f, 4.99f, 2.63f, 5.1f, 2.97f)
            lineTo(8.51f, 13.47f)
            lineTo(15.49f, 13.47f)
            lineTo(18.9f, 2.97f)
            curveTo(19.01f, 2.63f, 19.43f, 2.63f, 19.54f, 2.97f)
            lineTo(22.95f, 13.47f)
            curveTo(23.1f, 13.74f, 22.97f, 14.16f, 22.65f, 14.39f)
            close()
        }
    }.build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                toastState.show(
                    message = message,
                    variant = ToastVariant.Destructive
                )
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.consumeSuccess()
            navController.navigate(Destination.Marketplace.route) {
                popUpTo(Destination.Login.route) { inclusive = true }
            }
        }
    }

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)

    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            snackbarHost = {
                ToastHost(
                    hostState = toastState,
                )
            },
            topBar = {
                // Transparent Top Bar with White Icons
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
                        text = "Iniciar Sesión",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Button(
                        onClick = {},
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = RikkaIcons.Settings,
                            contentDescription = "Configuración",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }
                }
            }
        ) { innerPadding ->
            val focusManager = LocalFocusManager.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Welcome Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "¡Hola, Bienvenido!",
                        variant = TextVariant.H1,
                        color = RikkaTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¿Listo para intercambiar?",
                        variant = TextVariant.Large,
                        color = Color.Gray
                    )
                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email Label
                    Text(
                        text = "Correo electrónico",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )

                    Input(
                        value = uiState.correo,
                        onValueChange = viewModel::onCorreoChange,
                        placeholder = "wu@kbro.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = Icons.Default.Email,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Password Label
                    Text(
                        text = "Contraseña",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )

                    // Password Input with eye icon toggle overlay
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Input(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            placeholder = "Por favor escribe tu contraseña",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    enabled = !uiState.isLoading,
                                    onClick = { passwordVisible = !passwordVisible }
                                )
                        )
                    }

                    // Remember Me & Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            enabled = !uiState.isLoading,
                            label = "Recordarme"
                        )

                        Text(
                            text = "Olvide mi contraseña",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.primary,
                            modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                                navController.navigate(Destination.RecoverPassword.route)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Login Button
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = RikkaTheme.colors.primary
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.login(context) },
                            modifier = Modifier.fillMaxWidth(),
                            text = "Iniciar Sesion"
                        )
                    }

                    // Or With Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.LightGray)
                        )
                        Text(
                            text = "O continúe con",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = Color.Gray,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.LightGray)
                        )
                    }

                    // Social Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {},
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = GitHubIcon,
                                    contentDescription = "GitHub",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GitHub",
                                    variant = TextVariant.P,
                                )
                            }
                        }

                        Button(
                            onClick = {},
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = GitLabIcon,
                                    contentDescription = "GitLab",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GitLab",
                                    variant = TextVariant.P,
                                )
                            }
                        }
                    }
                }


                // Sign Up Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿No tienes una cuenta? ",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )
                    Text(
                        text = "Regístrate",
                        variant = TextVariant.Large,
                        color = RikkaTheme.colors.primary,
                        modifier = Modifier.clickable {
                            navController.navigate(Destination.Registro.route)
                        }
                    )
                }
            }
        }
    }
}
