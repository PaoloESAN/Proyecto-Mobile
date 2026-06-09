package com.paoloesan.proyectomobile.presentation.transaction

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVoucherScreen(
    onBack: () -> Unit,
    onVoucherSent: () -> Unit
) {
    val voucherSelected = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    val selectedFileName = remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val validationResult = validateVoucher(context, it)
            if (validationResult.isValid) {
                voucherSelected.value = true
                errorMessage.value = ""
                selectedFileName.value = getFileNameFromUri(context, it)
            } else {
                voucherSelected.value = false
                errorMessage.value = validationResult.errorMessage
                selectedFileName.value = ""
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
                        text = "Subir Voucher",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Button(
                        onClick = { /* TODO: Implementar menu */ },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mas opciones",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Boton Seleccionar imagen
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { filePickerLauncher.launch("image/*") },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Seleccionar imagen",
                                modifier = Modifier.size(20.dp),
                                tint = RikkaTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Seleccionar imagen",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.primary
                            )
                        }
                    }
                }

                // Titulo de vista previa
                item {
                    Text(
                        text = "Vista previa del archivo",
                        variant = TextVariant.Small,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Card de vista previa del voucher
                item {
                    VoucherPreviewCard(
                        voucherSelected = voucherSelected.value,
                        selectedFileName = selectedFileName.value,
                        errorMessage = errorMessage.value
                    )
                }

                // Boton Enviar comprobante
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (voucherSelected.value) {
                                onVoucherSent()
                            }
                        },
                        enabled = voucherSelected.value,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Enviar comprobante"
                    )

                    // Mensajes de error o guia
                    Spacer(modifier = Modifier.height(8.dp))
                    if (errorMessage.value.isNotEmpty()) {
                        Text(
                            text = errorMessage.value,
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.destructive,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else if (!voucherSelected.value) {
                        Text(
                            text = "Debe adjuntar un comprobante de pago",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.destructive,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun VoucherPreviewCard(
    voucherSelected: Boolean,
    selectedFileName: String = "",
    errorMessage: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = when {
                        errorMessage.isNotEmpty() -> RikkaTheme.colors.destructive
                        voucherSelected -> RikkaTheme.colors.primary
                        else -> RikkaTheme.colors.muted.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                errorMessage.isNotEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error en comprobante",
                            modifier = Modifier.size(48.dp),
                            tint = RikkaTheme.colors.destructive
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = errorMessage,
                            variant = TextVariant.P,
                            color = RikkaTheme.colors.destructive,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                !voucherSelected -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Comprobante",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No se ha seleccionado ningun comprobante",
                            variant = TextVariant.P,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Comprobante validado",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Voucher seleccionado correctamente",
                            variant = TextVariant.P,
                            color = Color(0xFF4CAF50),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedFileName,
                            variant = TextVariant.Small,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Listo para enviar",
                            variant = TextVariant.Small,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024
private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png")
private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png")

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    return when {
        uri.scheme == "content" -> {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex("_display_name")
                it.moveToFirst()
                it.getString(nameIndex)
            } ?: "archivo"
        }
        uri.path != null -> {
            uri.path!!.substringAfterLast("/")
        }
        else -> "archivo"
    }
}

private fun validateVoucher(context: Context, uri: Uri): ValidationResult {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""

        val fileName = getFileNameFromUri(context, uri)
        val extension = fileName.substringAfterLast(".").lowercase()

        if (!ALLOWED_MIME_TYPES.contains(mimeType) && !ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Formato de archivo no permitido. Solo se aceptan JPG y PNG."
            )
        }

        val fileSize = contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        } ?: 0L

        if (fileSize > MAX_FILE_SIZE_BYTES) {
            val sizeMB = String.format(Locale.US, "%.2f", fileSize / (1024f * 1024f))
            return ValidationResult(
                isValid = false,
                errorMessage = "El archivo es demasiado grande ($sizeMB MB). Tamano maximo: 5 MB."
            )
        }

        ValidationResult(isValid = true)
    } catch (e: Exception) {
        ValidationResult(
            isValid = false,
            errorMessage = "Error al validar el archivo: ${e.message}"
        )
    }
}
