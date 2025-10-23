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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Rojo
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

    // 🔄 Función para cargar datos desde Firestore
    fun cargarDatosUsuario() {
        Log.d("PerfilAjustes", "🔄 Iniciando carga de datos...")
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

                        Log.d("PerfilAjustes", "✅ Datos obtenidos:")
                        Log.d("PerfilAjustes", "   - Nombre: $nombre")
                        Log.d("PerfilAjustes", "   - Foto URL: $foto")

                        UsuarioTemporal.nombre = nombre
                        UsuarioTemporal.fotoUrl = foto

                        nombreUsuario = nombre
                        fotoPerfilUrl = foto

                        Log.d("PerfilAjustes", "✅ Estados actualizados correctamente")
                    } else {
                        Log.w("PerfilAjustes", "⚠️ El documento no existe en Firestore")
                    }
                }
                .addOnFailureListener { error ->
                    isLoading = false
                    Log.e("PerfilAjustes", "❌ Error al cargar datos: ${error.message}")
                    error.printStackTrace()
                }
        } ?: run {
            Log.e("PerfilAjustes", "❌ No hay usuario autenticado")
        }
    }

    // 🔄 Cargar datos cuando la pantalla se monta
    LaunchedEffect(Unit) {
        Log.d("PerfilAjustes", "📱 LaunchedEffect - Carga inicial")
        cargarDatosUsuario()
    }

    // 🔄 Recargar cuando regresas de los permisos del sistema
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    Log.d("PerfilAjustes", "🔄 ON_RESUME - Volviste a la app, recargando...")
                    cargarDatosUsuario()
                }
                androidx.lifecycle.Lifecycle.Event.ON_START -> {
                    Log.d("PerfilAjustes", "▶️ ON_START - Pantalla visible")
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Log.d("PerfilAjustes", "🗑️ DisposableEffect - Limpiando observer")
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 📷 Abrir galería
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
                    Log.d("PerfilAjustes", "✅ Foto subida exitosamente: $nuevaUrl")
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.foto_actualizada),
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarDatosUsuario()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_foto, error),
                        Toast.LENGTH_LONG
                    ).show()
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

            subirFotoAFirebase(
                context, uri,
                onSuccess = { nuevaUrl ->
                    Log.d("PerfilAjustes", "✅ Foto subida exitosamente: $nuevaUrl")
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.foto_actualizada),
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarDatosUsuario()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_foto, error),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("PerfilAjustes", "❌ Error: $error")
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = stringResource(id = R.string.volver),
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
                        if (!isUploading) {
                            if (isGoogleUser) {
                                Toast.makeText(
                                    context,
                                    "No puedes cambiar la foto de tu cuenta de Google",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showDialog = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // 🖼️ Mostrar foto de perfil
                when {
                    isUploading || isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = Azul3
                        )
                    }
                    !fotoPerfilUrl.isNullOrEmpty() -> {
                        Log.d("PerfilAjustes", "🖼️ Mostrando AsyncImage con URL: $fotoPerfilUrl")
                        AsyncImage(
                            model = fotoPerfilUrl,
                            contentDescription = stringResource(id = R.string.foto_perfil),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape),
                            error = painterResource(id = R.drawable.icono_perfil2),
                            placeholder = painterResource(id = R.drawable.icono_perfil2),
                            onSuccess = {
                                Log.d("PerfilAjustes", "✅ Imagen cargada exitosamente")
                            },
                            onError = { error ->
                                Log.e("PerfilAjustes", "❌ Error al cargar imagen: ${error.result.throwable.message}")
                            }
                        )
                    }
                    else -> {
                        Log.d("PerfilAjustes", "👤 Mostrando icono por defecto")
                        Image(
                            painter = painterResource(id = R.drawable.icono_perfil2),
                            contentDescription = stringResource(id = R.string.icono_perfil),
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            uploadError?.let { error ->
                Text(
                    text = stringResource(id = R.string.error, error),
                    color = Rojo,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Mostrar nombre
            val displayName = (nombreUsuario ?: userAuth?.displayName)
                ?.trim()
                ?.split(" ")
                ?.firstOrNull()
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() }
                ?: stringResource(id = R.string.usuario)

            Text(
                text = stringResource(id = R.string.hola_usuario, displayName),
                color = Blanco,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.ver_perfil),
                color = Blanco,
                fontSize = 18.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { navController.navigate("ver_perfil") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                text = stringResource(id = R.string.soporte),
                color = Azul3,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("soporte") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            CustomButton(
                text = stringResource(id = R.string.ajustes),
                color = Azul3,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("ajustes") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            CustomButton(
                text = stringResource(id = R.string.eliminar_cuenta),
                color = Rojo,
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(0.85f).height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = { navController.navigate("eliminar_cuenta") }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.cerrar_sesion),
                contentDescription = stringResource(id = R.string.cerrar_sesion),
                modifier = Modifier
                    .size(50.dp)
                    .clickable { navController.navigate("cerrar_sesion") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = stringResource(id = R.string.cerrar_sesion),
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
            title = { Text(stringResource(id = R.string.cambiar_foto_perfil)) },
            text = {
                Column {
                    Text(stringResource(id = R.string.selecciona_opcion))
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(id = R.string.galeria)) }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            cameraLauncher.launch(null)
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(id = R.string.camara)) }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(id = R.string.cancelar)) }
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
        Log.e("Firebase", "No hay usuario autenticado")
        onError(context.getString(R.string.no_usuario_autenticado))
        return
    }

    val userId = user.uid
    Log.d("Firebase", "Usuario: $userId")
    Log.d("Firebase", "URI a subir: $uri")

    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e("Firebase", "No se puede leer el archivo")
            onError(context.getString(R.string.no_leer_archivo))
            return
        }
        val size = inputStream.available()
        inputStream.close()
        Log.d("Firebase", "Tamaño del archivo: ${size / 1024} KB")

        if (size > 10 * 1024 * 1024) {
            onError(context.getString(R.string.archivo_muy_grande))
            return
        }
    } catch (e: Exception) {
        Log.e("Firebase", "Error al leer archivo: ${e.message}")
        onError(context.getString(R.string.error_leer_archivo, e.message))
        return
    }

    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("fotos_usuarios/$userId")

    val uploadTask = imageRef.putFile(uri)

    uploadTask.addOnProgressListener { taskSnapshot ->
        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
        Log.d("Firebase", "Progreso: $progress%")
    }

    uploadTask
        .addOnSuccessListener {
            imageRef.downloadUrl
                .addOnSuccessListener { url ->
                    val fotoUrl = url.toString()
                    UsuarioTemporal.fotoUrl = fotoUrl

                    val db = FirebaseFirestore.getInstance()
                    db.collection("usuarios").document(userId)
                        .update("fotoPerfil", fotoUrl)
                        .addOnSuccessListener {
                            onSuccess(fotoUrl)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error Firestore: ${e.message}")
                            onSuccess(fotoUrl)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error al obtener URL: ${e.message}")
                    onError(context.getString(R.string.error_obtener_url, e.message))
                }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error al subir: ${e.message}")
            onError(context.getString(R.string.error_subir, e.message))
        }
}