package com.example.myway.screens.modulo1

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTextField
import com.example.myway.ui.theme.Azul2
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OlvidoContraseña(
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    var correo by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = stringResource(R.string.volver),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Blanco,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ingresa tu correo y te enviaremos un enlace para recuperar tu contraseña",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Blanco,
                    textAlign = TextAlign.Center
                )

                // Campo de correo
                CustomTextField(
                    placeholder = "Correo electrónico",
                    text = correo,
                    onTextChange = { correo = it },
                    color = Blanco,
                    textColor = Azul2,
                    isPassword = false,
                    showBorder = false
                )

                // Botón enviar
                CustomButton(
                    text = "Enviar enlace",
                    color = Azul3,
                    modifier = Modifier
                        .width(220.dp)
                        .height(55.dp),
                    onClick = {
                        if (correo.isNotEmpty()) {

                            auth.sendPasswordResetEmail(correo)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Correo enviado. Revisa tu bandeja de entrada",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                context,
                                "Ingresa tu correo electrónico",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ¿Tienes una cuenta?
                Text(
                    text = "¿Ya tienes cuenta?",
                    color = Blanco,
                    fontSize = 14.sp,
                    fontFamily = Nunito
                )

                // Botón Iniciar sesión
                CustomButton(
                    text = "Iniciar sesión",
                    color = Azul3,
                    modifier = Modifier
                        .width(220.dp)
                        .height(55.dp),
                    onClick = {
                        navController.navigate("ingreso_usuario")
                    }
                )
            }
        }
    }
}