package com.paoloesan.proyectomobile.presentation.verification

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityVerificationScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedFrontImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBackImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val validateUri: (Uri) -> Boolean = { uri ->
        val mimeType = context.contentResolver.getType(uri)
        val isTypeValid = mimeType == "image/jpeg" || mimeType == "image/png" || mimeType == "image/jpg"
        
        var isSizeValid = false
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        val size = cursor.getLong(sizeIndex)
                        isSizeValid = size <= 5 * 1024 * 1024 // 5MB limit
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isTypeValid && isSizeValid
    }

    val pickFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            if (validateUri(uri)) {
                selectedFrontImageUri = uri
            } else {
                scope.launch {
                    toastState.show(
                        message = "Formato de archivo o tamano no permitido",
                        variant = ToastVariant.Destructive
                    )
                }
            }
        }
    }

    val pickBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            if (validateUri(uri)) {
                selectedBackImageUri = uri
            } else {
                scope.launch {
                    toastState.show(
                        message = "Formato de archivo o tamano no permitido",
                        variant = ToastVariant.Destructive
                    )
                }
            }
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
                        text = "Verificacion de DNI",
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
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    Text(
                        text = "Sube la verificacion de tu identidad",
                        variant = TextVariant.H2,
                        color = RikkaTheme.colors.primary
                    )
                }

                // DNI Frontal Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "DNI frontal",
                                variant = TextVariant.Large,
                                color = RikkaTheme.colors.onBackground
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RikkaTheme.colors.primary.copy(alpha = 0.04f))
                                    .border(
                                        width = 1.dp,
                                        color = RikkaTheme.colors.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedFrontImageUri != null) {
                                    AsyncImage(
                                        model = selectedFrontImageUri,
                                        contentDescription = "DNI Frontal Seleccionado",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = RikkaTheme.colors.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Sin imagen frontal",
                                            variant = TextVariant.Small,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    pickFrontLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !isUploading,
                                variant = ButtonVariant.Outline,
                                modifier = Modifier.fillMaxWidth(),
                                text = "Seleccionar imagen"
                            )
                        }
                    }
                }

                // DNI Posterior Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "DNI posterior",
                                variant = TextVariant.Large,
                                color = RikkaTheme.colors.onBackground
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RikkaTheme.colors.primary.copy(alpha = 0.04f))
                                    .border(
                                        width = 1.dp,
                                        color = RikkaTheme.colors.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedBackImageUri != null) {
                                    AsyncImage(
                                        model = selectedBackImageUri,
                                        contentDescription = "DNI Posterior Seleccionado",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = RikkaTheme.colors.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Sin imagen posterior",
                                            variant = TextVariant.Small,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    pickBackLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !isUploading,
                                variant = ButtonVariant.Outline,
                                modifier = Modifier.fillMaxWidth(),
                                text = "Seleccionar imagen"
                            )
                        }
                    }
                }

                // Upload Progress Indicators
                if (isUploading) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(color = RikkaTheme.colors.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Subiendo documentos...",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.onBackground
                            )
                        }
                    }
                }

                // Submit Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (selectedFrontImageUri == null || selectedBackImageUri == null) {
                                scope.launch {
                                    toastState.show(
                                        message = "Debe adjuntar ambas imagenes del documento",
                                        variant = ToastVariant.Destructive
                                    )
                                }
                            } else {
                                scope.launch {
                                    isUploading = true
                                    delay(2000) // Simulación de carga
                                    isUploading = false
                                    selectedFrontImageUri = null
                                    selectedBackImageUri = null
                                    toastState.show(
                                        message = "Verificacion enviada correctamente",
                                        variant = ToastVariant.Success
                                    )
                                }
                            }
                        },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enviar verificacion",
                                variant = TextVariant.P,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
