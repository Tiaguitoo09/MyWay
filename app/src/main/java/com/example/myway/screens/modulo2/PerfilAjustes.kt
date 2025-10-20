package com.example.myway.screens.modulo2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

@Composable
fun PerfilAjustes(navController: NavController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    var fotoPerfilUrl by remember { mutableStateOf(UsuarioTemporal.fotoUrl) }

    // 📷 Abrir galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("PerfilAjustes", "📸 URI seleccionada: $it")
            isUploading = true
            uploadError = null

            subirFotoAFirebase(context, it,
                onSuccess = { nuevaUrl ->
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(context, "✅ Foto actualizada", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(context, "❌ Error: $error", Toast.LENGTH_LONG).show()
                    Log.e("PerfilAjustes", "❌ Error: $error")
                }
            )
        }
    }

    // 📸 Tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            Log.d("PerfilAjustes", "📸 Foto capturada")
            isUploading = true
            uploadError = null

            val uri = saveBitmapToCache(context, it)

            subirFotoAFirebase(context, uri,
                onSuccess = { nuevaUrl ->
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(context, "✅ Foto actualizada", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(context, "❌ Error: $error", Toast.LENGTH_LONG).show()
                    Log.e("PerfilAjustes", "❌ Error: $error")
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = "Volver",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Imagen de perfil
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp)
                    .border(width = 6.dp, color = Blanco, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        if (!isUploading) showDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!fotoPerfilUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = fotoPerfilUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        error = painterResource(id = R.drawable.icono_perfil2),
                        placeholder = painterResource(id = R.drawable.icono_perfil2),
                        onError = { error ->
                            Log.e("AsyncImage", "❌ Error al cargar: ${error.result.throwable.message}")
                        }
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.icono_perfil2),
                        contentDescription = "Icono de perfil",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                    )
                }

                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        color = Azul3
                    )
                }
            }

            // Mostrar error si existe
            uploadError?.let { error ->
                Text(
                    text = "Error: $error",
                    color = Rojo,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val nombreUsuario = UsuarioTemporal.nombre?.trim()?.lowercase()
                ?.replaceFirstChar { it.uppercase() } ?: "Usuario"

            Text(
                text = "Hola, $nombreUsuario",
                color = Blanco,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ver Perfil",
                color = Blanco,
                fontSize = 18.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { navController.navigate("ver_perfil") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                text = "Soporte",
                color = Azul3,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("soporte") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            CustomButton(
                text = "Ajustes",
                color = Azul3,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("ajustes") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            CustomButton(
                text = "Eliminar Cuenta",
                color = Rojo,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("eliminar_cuenta") }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.cerrar_sesion),
                contentDescription = "Cerrar sesión",
                modifier = Modifier
                    .size(50.dp)
                    .clickable { navController.navigate("cerrar_sesion") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Cerrar Sesión",
                color = Azul1,
                textDecoration = TextDecoration.Underline,
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    navController.navigate("cerrar_sesion")
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = {
                Column {
                    Text("Selecciona una opción")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Galería") }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            cameraLauncher.launch(null)
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cámara") }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "perfil_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

fun subirFotoAFirebase(
    context: Context,
    uri: Uri,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user == null) {
        Log.e("Firebase", "❌ No hay usuario autenticado")
        onError("No hay usuario autenticado")
        return
    }

    val userId = user.uid
    Log.d("Firebase", "👤 Usuario: $userId")
    Log.d("Firebase", "📤 URI a subir: $uri")

    // Verificar que el archivo existe y es legible
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e("Firebase", "❌ No se puede leer el archivo")
            onError("No se puede leer el archivo")
            return
        }
        val size = inputStream.available()
        inputStream.close()
        Log.d("Firebase", "📊 Tamaño del archivo: ${size / 1024} KB")

        if (size > 10 * 1024 * 1024) { // 10MB
            onError("Archivo muy grande (máx 10MB)")
            return
        }
    } catch (e: Exception) {
        Log.e("Firebase", "❌ Error al leer archivo: ${e.message}")
        onError("Error al leer archivo: ${e.message}")
        return
    }

    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("fotos_usuarios/$userId")

    Log.d("Firebase", "📤 Iniciando subida...")

    val uploadTask = imageRef.putFile(uri)

    // Agregar listener de progreso
    uploadTask.addOnProgressListener { taskSnapshot ->
        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
        Log.d("Firebase", "📊 Progreso: $progress%")
    }

    uploadTask
        .addOnSuccessListener {
            Log.d("Firebase", "✅ Imagen subida a Storage")

            imageRef.downloadUrl
                .addOnSuccessListener { url ->
                    val fotoUrl = url.toString()
                    Log.d("Firebase", "✅ URL obtenida: $fotoUrl")

                    UsuarioTemporal.fotoUrl = fotoUrl

                    val db = FirebaseFirestore.getInstance()
                    db.collection("usuarios").document(userId)
                        .update("fotoPerfil", fotoUrl)
                        .addOnSuccessListener {
                            Log.d("Firebase", "✅ Guardado en Firestore")
                            onSuccess(fotoUrl)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "❌ Error Firestore: ${e.message}")
                            onSuccess(fotoUrl) // Aún así mostrar la foto
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "❌ Error al obtener URL: ${e.message}")
                    onError("Error al obtener URL: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "❌ Error al subir: ${e.message}")
            Log.e("Firebase", "❌ Stack trace: ", e)
            onError("Error al subir: ${e.message}")
        }
}