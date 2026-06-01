package com.paoloesan.proyectomobile.presentation.transaction

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * UploadVoucherScreen
 *
 * Pantalla de subida de comprobante de pago
 * Relacionada con US-16: Subida de comprobante de pago
 *
 * Permite al usuario seleccionar una imagen del comprobante de pago,
 * ver una vista previa y enviar el comprobante para actualizar el estado
 * de la transacción a "Pagado".
 * 
 * Validaciones implementadas:
 * - Permitir archivos JPG y PNG
 * - Tamaño máximo de 5MB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVoucherScreen(
    onBack: () -> Unit,
    onVoucherSent: () -> Unit
) {
    // Estados locales
    val voucherSelected = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    val selectedFileName = remember { mutableStateOf("") }
    val context = LocalContext.current
    
    // Lanzador para seleccionar archivo
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Subir voucher",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implementar menú */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Botón Seleccionar imagen
                SelectImageButton(
                    onImageSelected = {
                        filePickerLauncher.launch("image/*")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Título de vista previa
                VoucherPreviewTitle()

                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                // Card de vista previa del voucher
                VoucherPreviewCard(
                    voucherSelected = voucherSelected.value,
                    selectedFileName = selectedFileName.value,
                    errorMessage = errorMessage.value
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Botón Enviar comprobante
                SendVoucherButton(
                    voucherSelected = voucherSelected.value,
                    errorMessage = errorMessage.value,
                    onVoucherSent = onVoucherSent
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * SelectImageButton
 *
 * Componente que muestra el botón para seleccionar una imagen.
 */
@Composable
private fun SelectImageButton(
    onImageSelected: () -> Unit
) {
    OutlinedButton(
        onClick = onImageSelected,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = "Seleccionar imagen",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Seleccionar imagen",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * VoucherPreviewTitle
 *
 * Componente que muestra el título de la vista previa.
 */
@Composable
private fun VoucherPreviewTitle() {
    Text(
        text = "Vista previa del archivo",
        modifier = Modifier.fillMaxWidth(0.9f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * VoucherPreviewCard
 *
 * Componente que muestra la vista previa del comprobante seleccionado.
 */
@Composable
private fun VoucherPreviewCard(
    voucherSelected: Boolean,
    selectedFileName: String = "",
    errorMessage: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(250.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (voucherSelected)
                MaterialTheme.colorScheme.surfaceContainer
            else if (errorMessage.isNotEmpty())
                Color(0xFFFFEBEE)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (voucherSelected || errorMessage.isNotEmpty()) 4.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = if (voucherSelected)
                        MaterialTheme.colorScheme.primary
                    else if (errorMessage.isNotEmpty())
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (errorMessage.isNotEmpty()) {
                // Estado con error
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error en comprobante",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (!voucherSelected) {
                // Estado sin voucher seleccionado
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Comprobante",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No se ha seleccionado ningún comprobante",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Estado con voucher seleccionado
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Comprobante validado",
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Voucher seleccionado correctamente",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = selectedFileName,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Listo para enviar",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * SendVoucherButton
 *
 * Componente que muestra el botón para enviar el comprobante.
 * El botón está deshabilitado si no hay voucher seleccionado.
 */
@Composable
private fun SendVoucherButton(
    voucherSelected: Boolean,
    errorMessage: String = "",
    onVoucherSent: () -> Unit
) {
    Button(
        onClick = {
            if (voucherSelected) {
                onVoucherSent()
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        enabled = voucherSelected,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = "Enviar comprobante",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    // Mostrar mensaje correspondiente
    Spacer(modifier = Modifier.height(12.dp))

    if (errorMessage.isNotEmpty()) {
        Text(
            text = errorMessage,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
    } else if (!voucherSelected) {
        Text(
            text = "Debe adjuntar un comprobante de pago",
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
    }
}

// Constantes de validación
private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024  // 5MB en bytes
private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png")
private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png")

/**
 * Resultado de validación del comprobante
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)

/**
 * Obtiene el nombre del archivo desde una Uri
 */
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

/**
 * Valida el archivo seleccionado verificando:
 * - Formato: solo JPG y PNG
 * - Tamaño: máximo 5MB
 */
private fun validateVoucher(context: Context, uri: Uri): ValidationResult {
    return try {
        // Obtener el tipo MIME
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""

        // Obtener la extensión del archivo
        val fileName = getFileNameFromUri(context, uri)
        val extension = fileName.substringAfterLast(".").lowercase()

        // Validar formato
        if (!ALLOWED_MIME_TYPES.contains(mimeType) && !ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Formato de archivo no permitido. Solo se aceptan JPG y PNG."
            )
        }

        // Obtener tamaño del archivo
        val fileSize = contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        } ?: 0L

        // Validar tamaño
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            val sizeMB = String.format(Locale.US, "%.2f", fileSize / (1024f * 1024f))
            return ValidationResult(
                isValid = false,
                errorMessage = "El archivo es demasiado grande ($sizeMB MB). Tamaño máximo: 5 MB."
            )
        }

        // Si pasa todas las validaciones
        ValidationResult(isValid = true)
    } catch (e: Exception) {
        ValidationResult(
            isValid = false,
            errorMessage = "Error al validar el archivo: ${e.message}"
        )
    }
}
