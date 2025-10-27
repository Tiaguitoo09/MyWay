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
import androidx.compose.ui.res.stringResource
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
    var isLoading by remember { mutableStateOf(false) }

    var fotoPerfilUrl by remember { mutableStateOf(UsuarioTemporal.fotoUrl) }
    var nombreUsuario by remember { mutableStateOf(UsuarioTemporal.nombre) }

    val userAuth = FirebaseAuth.getInstance().currentUser
    val isGoogleUser = userAuth?.providerData?.any { it.providerId == "google.com" } ?: false

    // 🔄 Función para cargar datos
    fun cargarDatosUsuario() {
        Log.d("PerfilAjustes", "🔄 Iniciando carga de datos...")

        // ✅ PRIMERO: Cargar desde SharedPreferences (caché local)
        val sharedPrefs = context.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
        val cachedFoto = sharedPrefs.getString("cached_foto_perfil", null)
        val cachedNombre = sharedPrefs.getString("cached_nombre", null)

        // Mostrar datos en caché inmediatamente
        if (cachedFoto != null) {
            fotoPerfilUrl = cachedFoto
            UsuarioTemporal.fotoUrl = cachedFoto
            Log.d("PerfilAjustes", "📦 Foto cargada desde caché: $cachedFoto")
        }
        if (cachedNombre != null) {
            nombreUsuario = cachedNombre
            UsuarioTemporal.nombre = cachedNombre
            Log.d("PerfilAjustes", "📦 Nombre cargado desde caché: $cachedNombre")
        }

        // ✅ SEGUNDO: Actualizar desde Firestore
        userAuth?.let { u ->
            isLoading = true
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(u.uid)
                .get()
                .addOnSuccessListener { document ->
                    isLoading = false
                    if (document.exists()) {
                        val nombre = document.getString("nombre")
                        val foto = document.getString("fotoPerfil")

                        Log.d("PerfilAjustes", "✅ Datos obtenidos de Firestore:")
                        Log.d("PerfilAjustes", "   - Nombre: $nombre")
                        Log.d("PerfilAjustes", "   - Foto URL: $foto")

                        // Actualizar en memoria
                        UsuarioTemporal.nombre = nombre
                        UsuarioTemporal.fotoUrl = foto
                        nombreUsuario = nombre
                        fotoPerfilUrl = foto

                        // ✅ Guardar en SharedPreferences
                        sharedPrefs.edit().apply {
                            putString("cached_foto_perfil", foto)
                            putString("cached_nombre", nombre)
                            apply()
                        }

                        Log.d("PerfilAjustes", "💾 Datos guardados en caché")
                    } else {
                        Log.w("PerfilAjustes", "⚠️ El documento no existe en Firestore")
                    }
                }
                .addOnFailureListener { error ->
                    isLoading = false
                    Log.e("PerfilAjustes", "❌ Error al cargar datos: ${error.message}")
                    Log.d("PerfilAjustes", "📦 Usando datos en caché por error de red")
                }
        } ?: run {
            Log.e("PerfilAjustes", "❌ No hay usuario autenticado")
        }
    }

    // 🔄 Cargar datos al iniciar
    LaunchedEffect(Unit) {
        Log.d("PerfilAjustes", "📱 LaunchedEffect - Carga inicial")
        cargarDatosUsuario()
    }

    // 🔄 Recargar cuando vuelves de Permisos
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    Log.d("PerfilAjustes", "🔄 ON_RESUME - Recargando...")
                    cargarDatosUsuario()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 📷 Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("PerfilAjustes", "📸 URI seleccionada: $it")
            isUploading = true
            uploadError = null

            subirFotoAFirebase(
                context, it,
                onSuccess = { nuevaUrl ->
                    Log.d("PerfilAjustes", "✅ Foto subida: $nuevaUrl")
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                    cargarDatosUsuario()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    // 📸 Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            Log.d("PerfilAjustes", "📸 Foto capturada")
            isUploading = true
            uploadError = null

            val uri = saveBitmapToCache(context, it)

            subirFotoAFirebase(
                context, uri,
                onSuccess = { nuevaUrl ->
                    Log.d("PerfilAjustes", "✅ Foto subida: $nuevaUrl")
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                    cargarDatosUsuario()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
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

            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp)
                    .border(width = 6.dp, color = Blanco, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        if (!isUploading) {
                            if (isGoogleUser) {
                                Toast.makeText(
                                    context,
                                    "No puedes cambiar la foto de Google",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showDialog = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isUploading || isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = Azul3
                        )
                    }
                    !fotoPerfilUrl.isNullOrEmpty() -> {
                        AsyncImage(
                            model = fotoPerfilUrl,
                            contentDescription = "Foto perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape),
                            error = painterResource(id = R.drawable.icono_perfil2),
                            placeholder = painterResource(id = R.drawable.icono_perfil2)
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.icono_perfil2),
                            contentDescription = "Icono perfil",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            uploadError?.let { error ->
                Text(
                    text = "Error: $error",
                    color = Rojo,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayName = (nombreUsuario ?: userAuth?.displayName)
                ?.trim()
                ?.split(" ")
                ?.firstOrNull()
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() }
                ?: "Usuario"

            Text(
                text = "Hola, $displayName",
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

    if (showDialog && !isGoogleUser) {
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
        onError("No hay usuario autenticado")
        return
    }

    val userId = user.uid

    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            onError("No se puede leer el archivo")
            return
        }
        val size = inputStream.available()
        inputStream.close()

        if (size > 10 * 1024 * 1024) {
            onError("Archivo muy grande")
            return
        }
    } catch (e: Exception) {
        onError("Error al leer archivo: ${e.message}")
        return
    }

    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("fotos_usuarios/$userId")

    imageRef.putFile(uri)
        .addOnSuccessListener {
            imageRef.downloadUrl
                .addOnSuccessListener { url ->
                    val fotoUrl = url.toString()
                    UsuarioTemporal.fotoUrl = fotoUrl

                    // ✅ Guardar en SharedPreferences inmediatamente
                    val sharedPrefs = context.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putString("cached_foto_perfil", fotoUrl).apply()
                    Log.d("Firebase", "💾 Foto guardada en caché: $fotoUrl")

                    val db = FirebaseFirestore.getInstance()
                    db.collection("usuarios").document(userId)
                        .update("fotoPerfil", fotoUrl)
                        .addOnSuccessListener {
                            onSuccess(fotoUrl)
                        }
                        .addOnFailureListener {
                            onSuccess(fotoUrl)
                        }
                }
                .addOnFailureListener { e ->
                    onError("Error al obtener URL: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            onError("Error al subir: ${e.message}")
        }
}