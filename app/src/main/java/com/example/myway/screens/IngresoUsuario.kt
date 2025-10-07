package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Azul3
import com.example.myway.R

@Composable
fun IngresoUsuario(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo de pantalla
        Image(
            painter = painterResource(id = R.drawable.ingreso_usuario),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver arriba a la izquierda
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = "Volver",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clickable {
                    navController.popBackStack()
                }
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo de la app
            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = "Ícono de brújula",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Texto MyWay
            Text(
                text = "MyWay",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Nunito,
                color = Blanco // Tu color personalizado
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo Correo electrónico
            CustomTextField(placeholder = "Correo electrónico", color=Blanco)

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            CustomTextField(placeholder = "Contraseña", isPassword = true, color=Azul3)

            Spacer(modifier = Modifier.height(12.dp))

            // Olvidé mi contraseña
            Text(
                text = "Olvide mi contraseña",
                color = Blanco,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de Ingresar y Registrarse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButton(text = "Ingresar", color=Azul3)
                CustomButton(text = "Registrarse", color=Azul3)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Iniciar sesión con Google
            Text(
                text = "Iniciar sesión con Google",
                color = Blanco,
                fontSize = 14.sp
            )
        }
    }
}
