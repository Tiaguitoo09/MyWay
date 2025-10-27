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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.screens.InfoBlock
import com.example.myway.ui.theme.Blanco
import com.example.myway.utils.UsuarioTemporal
import android.util.Log

@Composable
fun VerPerfil(navController: NavController) {
    val context = LocalContext.current
    var fotoPerfilUrl by remember { mutableStateOf(UsuarioTemporal.fotoUrl) }

    // ✅ Cargar desde SharedPreferences al iniciar
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("MyWayPrefs", android.content.Context.MODE_PRIVATE)
        val cachedFoto = sharedPrefs.getString("cached_foto_perfil", null)

        if (cachedFoto != null) {
            fotoPerfilUrl = cachedFoto
            UsuarioTemporal.fotoUrl = cachedFoto
            Log.d("VerPerfil", "📦 Foto cargada desde caché: $cachedFoto")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                        contentDescription = "Foto perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        error = painterResource(id = R.drawable.icono_perfil2),
                        placeholder = painterResource(id = R.drawable.icono_perfil2),
                        onError = { error ->
                            Log.e("AsyncImage", "Error al cargar: ${error.result.throwable.message}")
                        }
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.icono_perfil2),
                        contentDescription = "Icono perfil",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                    )
                }
            }

            InfoBlock(
                label = "Nombre",
                value = UsuarioTemporal.nombre ?: "Usuario"
            )
            InfoBlock(
                label = "Apellido",
                value = UsuarioTemporal.apellido ?: "No disponible"
            )
            InfoBlock(
                label = "Correo",
                value = UsuarioTemporal.correo ?: "No disponible"
            )
            InfoBlock(
                label = "Fecha de Nacimiento",
                value = UsuarioTemporal.fechaNacimiento ?: "No registrada"
            )

            Text(
                text = "Cambiar Contraseña",
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