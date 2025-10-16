package com.example.myway.screens.modulo2

import android.net.Uri
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Rojo
import com.example.myway.utils.UsuarioTemporal

@Composable
fun PerfilAjustes(navController: NavController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(UsuarioTemporal.fotoLocalUri) }

    // 📷 Launcher para abrir galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            UsuarioTemporal.fotoLocalUri = it
        }
    }

    // 📸 Launcher para abrir cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val uri = Uri.parse(it.toString()) // (temporal, solo en memoria)
            UsuarioTemporal.fotoLocalUri = uri
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Imagen de perfil
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp)
                    .border(width = 6.dp, color = Azul1, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        if (UsuarioTemporal.fotoUrl == null) {
                            showDialog = true // solo para los que no son de Google
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    UsuarioTemporal.fotoUrl != null -> {
                        AsyncImage(
                            model = UsuarioTemporal.fotoUrl,
                            contentDescription = "Foto de perfil de Google",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }

                    UsuarioTemporal.fotoLocalUri != null -> {
                        AsyncImage(
                            model = UsuarioTemporal.fotoLocalUri,
                            contentDescription = "Foto de perfil local",
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

            // Mostrar Nombre
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
                fontWeight = FontWeight.Normal,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 24.dp)
                    .clickable {
                        navController.navigate("ver_perfil")
                    }
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
                onClick = {
                    navController.navigate("soporte")
                }
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
                onClick = {
                    navController.navigate("ajustes")
                }
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
                onClick = {
                    navController.navigate("eliminar_cuenta")
                }
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
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable {
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
                    }) {
                        Text("Elegir desde galería")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        cameraLauncher.launch(null)
                        showDialog = false
                    }) {
                        Text("Tomar foto con cámara")
                    }
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
