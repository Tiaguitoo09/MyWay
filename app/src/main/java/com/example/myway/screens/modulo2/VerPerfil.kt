package com.example.myway.screens.modulo2

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.InfoBlock
import com.example.myway.ui.theme.Blanco
import com.example.myway.utils.UsuarioTemporal



@Composable
fun VerPerfil(navController: NavController) {
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
            verticalArrangement = Arrangement.Center
        ) {
            // Imagen de perfil
            Image(
                painter = painterResource(id = R.drawable.icono_perfil2),
                contentDescription = "Icono de perfil",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
            )

            // Bloques de información
            InfoBlock(label = "Nombre", value = UsuarioTemporal.nombre ?: "Usuario")
            InfoBlock(label = "Apellido", value = UsuarioTemporal.apellido ?: "No disponible")
            InfoBlock(label = "Correo", value = UsuarioTemporal.correo ?: "No disponible")
            InfoBlock(label = "Fecha de nacimiento", value = UsuarioTemporal.fechaNacimiento ?: "No registrada")


            // Enlace para cambiar contraseña
            Text(
                text = "Cambiar contraseña",
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
