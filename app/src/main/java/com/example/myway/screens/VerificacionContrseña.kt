package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*

@Composable
fun VerificacionCodigo(navController: NavController) {
    var code1 by remember { mutableStateOf("") }
    var code2 by remember { mutableStateOf("") }
    var code3 by remember { mutableStateOf("") }
    var code4 by remember { mutableStateOf("") }

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
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            Text(
                text = "Verificación",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ingrese el Código de Verificación",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Cajas del código
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                CodeBox(value = code1, onValueChange = { if (it.length <= 1) code1 = it })
                CodeBox(value = code2, onValueChange = { if (it.length <= 1) code2 = it })
                CodeBox(value = code3, onValueChange = { if (it.length <= 1) code3 = it })
                CodeBox(value = code4, onValueChange = { if (it.length <= 1) code4 = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Si no recibiste un código, reenvíalo",
                color = Blanco,
                fontSize = 14.sp,
                fontFamily = Nunito,
                textDecoration = TextDecoration.Underline
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButton(
                text = "Enviar",
                color = Azul3,
                modifier = Modifier
                    .width(200.dp)
                    .height(45.dp),
                onClick = {
                    val code = code1 + code2 + code3 + code4
                    // lógica de verificación de código
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Línea e imagen Google
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .padding(end = 8.dp),
                    onDraw = {
                        drawLine(
                            color = Blanco,
                            start = androidx.compose.ui.geometry.Offset.Zero,
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f)
                        )
                    }
                )

                Image(
                    painter = painterResource(id = R.drawable.google_image),
                    contentDescription = "Google",
                    modifier = Modifier
                        .size(90.dp)
                )

                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .padding(start = 8.dp),
                    onDraw = {
                        drawLine(
                            color = Blanco,
                            start = androidx.compose.ui.geometry.Offset.Zero,
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier
                    .width(200.dp)
                    .height(45.dp),
                onClick = {
                    navController.navigate("ingreso_usuario")
                }
            )
        }
    }
}

@Composable
fun CodeBox(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(60.dp)
            .height(60.dp),
        textStyle = TextStyle(fontSize = 22.sp, color = Negro),
        singleLine = true,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Negro,
            unfocusedTextColor = Negro,
            focusedBorderColor = Azul3,
            unfocusedBorderColor = Azul3,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco,
            cursorColor = Azul3
        )
    )
}
