package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Negro
import com.example.myway.ui.theme.Nunito

@Composable
fun OlvidoContraseña(navController: NavController) {
    var email by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
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
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Olvide mi contraseña",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = "Ingrese su correo electrónico",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo correo
            CustomTextField(
                placeholder = "Correo electrónico",
                text = email,
                onTextChange = {
                    val it = ""
                    email = it
                },
                color = Blanco,        // Fondo blanco
                textColor = Negro,
                isPassword = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Enviar
            CustomButton(
                text = "Enviar",
                color = Azul3,
                onClick = {
                    // Aquí puedes llamar lógica de recuperación
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Línea con icono de Google (simulada)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(horizontal = 40.dp),
                        onDraw = {
                            drawLine(
                                color = Blanco,
                                start = androidx.compose.ui.geometry.Offset.Zero,
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f)
                            )
                        }
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.google_image),
                    contentDescription = "Google",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(horizontal = 8.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(horizontal = 40.dp),
                        onDraw = {
                            drawLine(
                                color = Blanco,
                                start = androidx.compose.ui.geometry.Offset.Zero,
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¿Tienes una cuenta?",
                color = Blanco,
                fontSize = 14.sp,
                fontFamily = Nunito
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomButton(
                text = "Iniciar Sesión",
                color = Azul3,
                onClick = {
                    navController.navigate("ingreso_usuario")
                }
            )
        }
    }
}
