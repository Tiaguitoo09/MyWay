package com.example.myway.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.google.firebase.auth.FirebaseAuth
import com.example.myway.temporalCode.CodigoTemporal

@Composable
fun OlvidoContraseña(
    navController: NavController,
    auth: FirebaseAuth
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

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

        // Columna principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Olvide mi contraseña",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(40.dp))


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Ingrese su correo electrónico",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Blanco
                )

                // Campo de correo
                CustomTextField(
                    placeholder = "Correo electrónico",
                    text = email,
                    onTextChange = { email = it },
                    color = Blanco,
                    textColor = Negro,
                    isPassword = false
                )

                // Botón enviar
                CustomButton(
                    text = "Enviar",
                    color = Azul3,
                    modifier = Modifier
                        .width(200.dp)
                        .height(45.dp),
                    onClick = {
                        if (email.isNotEmpty()) {
                            // 1. Generar código aleatorio
                            val code = (1000..9999).random().toString()

                            // 2. Guardar código y correo temporalmente
                            CodigoTemporal.codigo = code
                            CodigoTemporal.correo = email

                            // 3. Simular el envío (esto en producción se hace con un servicio de email real)
                            Toast.makeText(context, "Código enviado: $code", Toast.LENGTH_LONG).show()

                            // 4. Navegar a pantalla de verificación
                            navController.navigate("verificacion_contraseña")
                        } else {
                            Toast.makeText(context, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
                        }
                    }

                )

                // Imagen Google
                Image(
                    painter = painterResource(id = R.drawable.google_image),
                    contentDescription = "Google",
                    modifier = Modifier.size(90.dp)
                )

                // ¿Tienes una cuenta?
                Text(
                    text = "¿Tienes una cuenta?",
                    color = Blanco,
                    fontSize = 14.sp,
                    fontFamily = Nunito
                )

                // Botón Iniciar sesión
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
}






