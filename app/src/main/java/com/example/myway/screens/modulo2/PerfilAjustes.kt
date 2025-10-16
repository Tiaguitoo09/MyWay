package com.example.myway.screens.modulo2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
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
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Rojo
import com.example.myway.utils.ImageStorage
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun PerfilAjustes(navController: NavController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(ImageStorage.obtenerImagenUri(context)) }

    // 📷 Abrir galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            UsuarioTemporal.fotoLocalUri = it
            ImageStorage.guardarImagenUri(context, it)
            subirFotoAFirebase(it)
        }
    }

    // 📸 Tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val uri = saveBitmapToCache(context, it)
            imageUri = uri
            UsuarioTemporal.fotoLocalUri = uri
            ImageStorage.guardarImagenUri(context, uri)
            subirFotoAFirebase(uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = "Volver",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )

        // Contenido principal
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
                        if (UsuarioTemporal.fotoUrl == null) {
                            showDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    UsuarioTemporal.fotoUrl != null -> {
                        AsyncImage(
                            model = UsuarioTemporal.fotoUrl,
                            contentDescription = "Foto de perfil (Firebase)",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }

                    imageUri != null -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Foto local",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }

                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.icono_perfil2),
                            contentDescription = "Icono de perfil",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }
                }
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
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("soporte") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            CustomButton(
                text = "Ajustes",
                color = Azul3,
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("ajustes") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            CustomButton(
                text = "Eliminar Cuenta",
                color = Rojo,
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("eliminar_cuenta") }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.cerrar_sesion),
                contentDescription = "Cerrar sesión",
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        navController.navigate("cerrar_sesion")
                    }
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

    // 🪟 Diálogo para elegir fuente de imagen
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = { Text("Selecciona una opción para agregar tu foto") },
            confirmButton = {
                Column {
                    Button(onClick = {
                        galleryLauncher.launch("image/*")
                        showDialog = false
                    }) { Text("Elegir desde galería") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        cameraLauncher.launch(null)
                        showDialog = false
                    }) { Text("Tomar foto con cámara") }
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// 🧩 Guardar el bitmap tomado con cámara
fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "perfil_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

// 🔥 Subir imagen a Firebase Storage y guardar su URL en Firestore
fun subirFotoAFirebase(uri: Uri) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Log.e("Firebase", "❌ No hay usuario autenticado")
        return
    }

    val userId = user.uid
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("fotos_usuarios/$userId.jpg")

    imageRef.putFile(uri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { url ->
                val fotoUrl = url.toString()
                UsuarioTemporal.fotoUrl = fotoUrl // para actualizar en la UI

                val db = FirebaseFirestore.getInstance()
                db.collection("usuarios").document(userId)
                    .update("fotoPerfil", fotoUrl)
                    .addOnSuccessListener {
                        Log.d("Firebase", "✅ URL guardada correctamente")
                    }
                    .addOnFailureListener {
                        Log.e("Firebase", "❌ Error guardando URL en Firestore", it)
                    }
            }
        }
        .addOnFailureListener {
            Log.e("Firebase", "❌ Error subiendo imagen", it)
        }
}
