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
import com.example.myway.ui.theme.Nunito

@Composable
fun RegistroUsuario(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.registro),
            contentDescription = "Fondo de registro",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha de volver
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
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            CustomTitleText(
                text = "Registro",
                color = Blanco,
                fontSize = 50.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Variables de los campos
            var nombre by remember { mutableStateOf("") }
            var apellido by remember { mutableStateOf("") }
            var correo by remember { mutableStateOf("") }
            var contrasena by remember { mutableStateOf("") }
            var verificarContrasena by remember { mutableStateOf("") }

            // Campos de texto reutilizables
            CustomTextField(
                placeholder = "Nombre",
                color = Blanco,
            )
            Spacer(modifier = Modifier.height(10.dp))

            CustomTextField(
                placeholder = "Apellido",
                color = Blanco,
            )
            Spacer(modifier = Modifier.height(10.dp))

            CustomTextField(
                placeholder = "Correo electrónico",
                color = Blanco,
            )
            Spacer(modifier = Modifier.height(10.dp))

            CustomTextField(
                placeholder = "Contraseña",
                color = Blanco,
                isPassword = true,
            )
            Spacer(modifier = Modifier.height(10.dp))

            CustomTextField(
                placeholder = "Verificar contraseña",
                color = Blanco,
                isPassword = true,
            )
            Spacer(modifier = Modifier.height(25.dp))

            // Botón de registro
            CustomButton(
                text = "Registrarse",
                color = Azul3,
                onClick = {
                    // Aquí luego agregaremos la lógica de validación o Firebase
                }
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Texto para volver al login
            Text(
                text = "¿Ya tienes cuenta? Inicia sesión",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.clickable {
                    navController.navigate("ingreso_usuario")
                }
            )
        }
    }
}
