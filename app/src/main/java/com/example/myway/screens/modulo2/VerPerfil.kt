package com.example.myway.screens.modulo2

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.screens.InfoBlock
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Azul3
import com.example.myway.utils.UsuarioTemporal
import android.util.Log

@Composable
fun VerPerfil(navController: NavController) {
    val context = LocalContext.current
    var fotoPerfilUrl by remember { mutableStateOf(UsuarioTemporal.fotoUrl) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo),
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
                .clickable { navController.popBackStack() }
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagen de perfil dinámica
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
                    .border(width = 6.dp, color = Blanco, shape = CircleShape)
                    .clip(CircleShape),
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
                            Log.e("AsyncImage", "❌ Error al cargar: ${error.result.throwable.message}")
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
            }

            // Bloques de información
            InfoBlock(
                label = stringResource(id = R.string.nombre),
                value = UsuarioTemporal.nombre ?: stringResource(id = R.string.usuario)
            )
            InfoBlock(
                label = stringResource(id = R.string.apellido),
                value = UsuarioTemporal.apellido ?: stringResource(id = R.string.no_disponible)
            )
            InfoBlock(
                label = stringResource(id = R.string.correo_label),
                value = UsuarioTemporal.correo ?: stringResource(id = R.string.no_disponible)
            )
            InfoBlock(
                label = stringResource(id = R.string.fecha_nacimiento_label),
                value = UsuarioTemporal.fechaNacimiento ?: stringResource(id = R.string.no_registrada)
            )

            // Enlace para cambiar contraseña
            Text(
                text = stringResource(id = R.string.cambiar_contrasena),
                color = Blanco,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { navController.navigate("cambio_contraseña") }
            )
        }
    }
}