package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Azul3
import com.example.myway.R
import com.example.myway.ui.theme.Nunito


@Composable
fun IngresoUsuario(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo
        Image(
            painter = painterResource(id = R.drawable.ingreso_usuario),
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

        // Contenido
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo brújula
            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = "Ícono de brújula",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Texto MyWay
            Text(
                text = "MyWay",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Nunito,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input correo
            CustomTextField(
                placeholder = "Correo electrónico",
                color = Azul3,
                isPassword = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input contraseña
            CustomTextField(
                placeholder = "Contraseña",
                color = Azul3,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Texto "Olvidé mi contraseña"
            Text(
                text = "Olvide mi contraseña",
                color = Blanco,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones: Ingresar y Registrarse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButton(
                    text = "Ingresar",
                    color = Azul3,
                    modifier= Modifier.width(140.dp),
                    onClick = {
                        navController.navigate("") // Cambia esto si tienes otra ruta
                    }
                )
                CustomButton(
                    text = "Registrarse",
                    color = Azul3,
                    modifier= Modifier.width(140.dp),
                    onClick = {
                        navController.navigate("registro_usuario") // Asegúrate de que esta ruta exista
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google login
            Text(
                text = "Iniciar sesión con Google",
                color = Blanco,
                fontSize = 14.sp
            )
        }
    }
}
