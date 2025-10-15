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
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Negro
import com.example.myway.ui.theme.Rojo
import com.example.myway.utils.UsuarioTemporal

@Composable
fun PerfilAjustes(navController: NavController) {

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
            Image(
                painter = painterResource(id = R.drawable.icono_perfil2),
                contentDescription = "Icono de perfil",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp)
            )

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

            // Ver perfil
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

            // Botón Soporte
            CustomButton(
                text = "Soporte",
                color = Azul3,
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(70.dp),
                fontWeight = FontWeight.ExtraBold,
                onClick = {
                    navController.navigate("")
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ✅ Botón Ajustes (con navegación configurada)
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

            // Botón Eliminar cuenta
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

            // Icono cerrar sesión
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
}
