package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*

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
            painter = painterResource(id = R.drawable.flechaazul),
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
            // Variables de los campos
            var nombre by remember { mutableStateOf("") }
            var apellido by remember { mutableStateOf("") }
            var correo by remember { mutableStateOf("") }
            var contrasena by remember { mutableStateOf("") }
            var verificarContrasena by remember { mutableStateOf("") }
            var dia by remember { mutableStateOf("") }
            var mes by remember { mutableStateOf("") }
            var anio by remember { mutableStateOf("") }
            var genero by remember { mutableStateOf<String?>(null) }

            Spacer(modifier = Modifier.height(40.dp))

            // Campos de texto
            CampoTextoAzul("Nombre", nombre) { nombre = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Apellido", apellido) { apellido = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Correo electrónico", correo) { correo = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Contraseña", contrasena, true) { contrasena = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Verificar contraseña", verificarContrasena, true) { verificarContrasena = it }

            Spacer(modifier = Modifier.height(20.dp))

            // --- FECHA DE NACIMIENTO ---
            Text(
                text = "Fecha de nacimiento",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CampoFecha("Día", dia) { dia = it }
                CampoFecha("Mes", mes) { mes = it }
                CampoFecha("Año", anio) { anio = it }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- GÉNERO ---
            Text(
                text = "Género",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Masculino
                Box(
                    modifier = Modifier
                        .size(90.dp, 55.dp)
                        .background(
                            if (genero == "masculino") Azul4.copy(alpha = 0.4f) else Blanco,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(2.dp, Azul3, shape = RoundedCornerShape(20.dp))
                        .clickable { genero = "masculino" },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icono_masculino),
                        contentDescription = "Masculino",
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Femenino
                Box(
                    modifier = Modifier
                        .size(90.dp, 55.dp)
                        .background(
                            if (genero == "femenino") Color(0xFFFFC0CB).copy(alpha = 0.4f) else Blanco,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(2.dp, Azul3, shape = RoundedCornerShape(20.dp))
                        .clickable { genero = "femenino" },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icono_femenino),
                        contentDescription = "Femenino",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Botón de registro
            CustomButton(
                text = "Continuar",
                color = Azul3,
                onClick = {
                    // Aquí luego agregaremos la lógica de validación o Firebase
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto subrayado para iniciar sesión con Google
            Text(
                text = "Iniciar sesión con Google",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    // Aquí luego agregaremos la lógica de autenticación con Google
                }
            )
        }
    }
}

// Reutilizable: Campo con fondo blanco, texto azul y esquinas redondeadas
@Composable
fun CampoTextoAzul(
    placeholder: String,
    valor: String,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    androidx.compose.material3.OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = Azul3) },
        singleLine = true,
        textStyle = TextStyle(color = Azul3),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier
            .width(320.dp)
            .height(55.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Azul3,
            unfocusedBorderColor = Azul3,
            cursorColor = Azul3,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

// Campo de fecha pequeño (día, mes, año)
@Composable
fun CampoFecha(
    placeholder: String,
    valor: String,
    onChange: (String) -> Unit
) {
    androidx.compose.material3.OutlinedTextField(
        value = valor,
        onValueChange = {
            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                onChange(it)
            }
        },
        placeholder = { Text(placeholder, color = Azul3, textAlign = TextAlign.Center) },
        singleLine = true,
        textStyle = TextStyle(color = Azul3, textAlign = TextAlign.Center),
        modifier = Modifier
            .width(90.dp)
            .height(55.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Azul3,
            unfocusedBorderColor = Azul3,
            cursorColor = Azul3,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco
        ),
        shape = RoundedCornerShape(20.dp)
    )
}
