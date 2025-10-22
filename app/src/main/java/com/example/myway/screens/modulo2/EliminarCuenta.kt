package com.example.myway.screens.modulo2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTitleText
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Azul4
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Rojo
import com.example.myway.ui.theme.Verde
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

@Composable
fun EliminarCuenta(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de la app
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen advertencia
            Image(
                painter = painterResource(id = R.drawable.circuloadvertencia),
                contentDescription = stringResource(id = R.string.confirmacion),
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texto principal
            CustomTitleText(
                text = stringResource(id = R.string.seguro_eliminar_cuenta),
                color = Blanco,
                fontSize = 28.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                //Botón Sí
                CustomButton(
                    text = stringResource(id = R.string.si),
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        val usuario = auth.currentUser
                        val correoUsuario = UsuarioTemporal.correo

                        if (usuario != null && correoUsuario != null) {
                            // Eliminar documento de Firestore
                            db.collection("usuarios")
                                .whereEqualTo("correo", correoUsuario)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (doc in documents) {
                                        db.collection("usuarios").document(doc.id).delete()
                                    }
                                    // Luego eliminar de Authentication
                                    usuario.delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.cuenta_eliminada),
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Limpia los datos temporales
                                            UsuarioTemporal.correo = ""
                                            UsuarioTemporal.nombre = ""

                                            // Navegar al ingreso
                                            navController.navigate("inicio") {
                                                popUpTo("perfil_ajustes") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.error_eliminar_cuenta, e.message),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.error_eliminar_usuario, e.message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.no_info_usuario),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

                // Botón No
                CustomButton(
                    text = stringResource(id = R.string.no),
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        navController.navigate("perfil_ajustes")
                    }
                )
            }
        }
    }
}

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

            subirFotoAFirebase(
                context, it,
                onSuccess = { nuevaUrl ->
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.foto_actualizada),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_foto, error),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("PerfilAjustes", "Error: $error")
                }
            )
        }
    }

    // 📸 Tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            Log.d("PerfilAjustes", "Foto capturada")
            isUploading = true
            uploadError = null

            val uri = saveBitmapToCache(context, it)

            subirFotoAFirebase(
                context, uri,
                onSuccess = { nuevaUrl ->
                    fotoPerfilUrl = nuevaUrl
                    isUploading = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.foto_actualizada),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { error ->
                    isUploading = false
                    uploadError = error
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_foto, error),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("PerfilAjustes", "Error: $error")
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
                        if (!isUploading) showDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!fotoPerfilUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = fotoPerfilUrl,
                        contentDescription = stringResource(id = R.string.foto_perfil),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        error = painterResource(id = R.drawable.icono_perfil2),
                        placeholder = painterResource(id = R.drawable.icono_perfil2),
                        onError = { error ->
                            Log.e(
                                "AsyncImage",
                                "Error al cargar: ${error.result.throwable.message}"
                            )
                        }
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.icono_perfil2),
                        contentDescription = stringResource(id = R.string.icono_perfil),
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
                    text = stringResource(id = R.string.error, error),
                    color = Rojo,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val nombreUsuario = UsuarioTemporal.nombre?.trim()?.lowercase()
                ?.replaceFirstChar { it.uppercase() }
                ?: stringResource(id = R.string.usuario)

            Text(
                text = stringResource(id = R.string.hola_usuario, nombreUsuario),
                color = Blanco,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.ver_perfil),
                color = Blanco,
                fontSize = 18.sp,
                textDecoration = TextDecoration.Companion.Underline,
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
                textDecoration = TextDecoration.Companion.Underline,
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

    // Verificar que el archivo existe y es legible
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

        if (size > 10 * 1024 * 1024) { // 10MB
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

    Log.d("Firebase", "Iniciando subida...")

    val uploadTask = imageRef.putFile(uri)

    // Agregar listener de progreso
    uploadTask.addOnProgressListener { taskSnapshot ->
        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
        Log.d("Firebase", "Progreso: $progress%")
    }

    uploadTask
        .addOnSuccessListener {
            Log.d("Firebase", "Imagen subida a Storage")

            imageRef.downloadUrl
                .addOnSuccessListener { url ->
                    val fotoUrl = url.toString()
                    Log.d("Firebase", "URL obtenida: $fotoUrl")

                    UsuarioTemporal.fotoUrl = fotoUrl

                    val db = FirebaseFirestore.getInstance()
                    db.collection("usuarios").document(userId)
                        .update("fotoPerfil", fotoUrl)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Guardado en Firestore")
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
            Log.e("Firebase", "Stack trace: ", e)
            onError(context.getString(R.string.error_subir, e.message))
        }
}

@Composable
fun SilenciarNotificaciones(navController: NavController) {
    val context = LocalContext.current
    var notificacionesSilenciadas by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = stringResource(id = R.string.volver),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .zIndex(3f)
                .clickable { navController.popBackStack() }
        )

        // Contenido principal con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Título principal
            Text(
                text = stringResource(id = R.string.silenciar_notificaciones_titulo),
                color = Blanco,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tarjeta informativa
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Azul3.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = stringResource(id = R.string.pausar_notificaciones),
                        color = Blanco,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.cuando_activado),
                            color = Blanco,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.no_recibiras_notificaciones),
                            color = Blanco,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(id = R.string.notificaciones_guardadas),
                            color = Blanco,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(id = R.string.reactivar_ajustes),
                            color = Blanco,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Texto Recuerda
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Azul1,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Companion.Underline
                                )
                            ) {
                                append(context.getString(R.string.recuerda))
                            }
                            withStyle(
                                SpanStyle(
                                    color = Azul1,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Companion.Underline
                                )
                            ) {
                                append(context.getString(R.string.alerta_silenciar))
                            }
                        },
                        fontSize = 17.sp,
                        textAlign = TextAlign.Companion.Start,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Switch ON / OFF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = notificacionesSilenciadas,
                            onCheckedChange = { mostrarDialogo = true },
                            modifier = Modifier.Companion.scale(1.3f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Verde,
                                checkedTrackColor = Verde,
                                uncheckedThumbColor = Rojo,
                                uncheckedTrackColor = Rojo
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (notificacionesSilenciadas)
                                stringResource(id = R.string.on)
                            else
                                stringResource(id = R.string.off),
                            color = Blanco,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Pantalla de confirmación personalizada (overlay)
        if (mostrarDialogo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .background(Azul3.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.circuloadvertencia),
                        contentDescription = stringResource(id = R.string.confirmacion),
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CustomTitleText(
                        text = if (notificacionesSilenciadas)
                            stringResource(id = R.string.activar_notificaciones)
                        else
                            stringResource(id = R.string.seguro_silenciar),
                        color = Blanco,
                        fontSize = 26.sp,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Companion.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CustomButton(
                            text = stringResource(id = R.string.si),
                            color = Azul3,
                            modifier = Modifier.width(140.dp),
                            onClick = {
                                notificacionesSilenciadas = !notificacionesSilenciadas
                                mostrarDialogo = false
                            }
                        )
                        CustomButton(
                            text = stringResource(id = R.string.no),
                            color = Azul4,
                            modifier = Modifier.width(140.dp),
                            onClick = { mostrarDialogo = false }
                        )
                    }
                }
            }
        }
    }
}