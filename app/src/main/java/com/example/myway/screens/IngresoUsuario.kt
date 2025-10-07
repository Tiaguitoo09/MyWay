package com.example.myway.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun IngresoUsuario(
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // 🔹 Lanzador para el Intent de Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            // 🔹 Autenticamos con Firebase
            scope.launch {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            // Login correcto
                            navController.navigate("inicio") {
                                popUpTo("ingreso_usuario") { inclusive = true }
                            }
                        } else {
                            signInTask.exception?.printStackTrace()
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

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

        // Contenido principal
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

            // Inputs (no funcionales todavía)
            CustomTextField(
                placeholder = "Correo electrónico",
                color = Azul3,
                isPassword = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                placeholder = "Contraseña",
                color = Azul3,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Olvidé mi contraseña",
                color = Blanco,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de ingresar y registrarse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomButton(
                    text = "Ingresar",
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        // Aquí más adelante puedes hacer login manual con correo/contraseña
                    }
                )
                CustomButton(
                    text = "Registrarse",
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        navController.navigate("registro_usuario")
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Botón funcional de Google
            Text(
                text = if (isLoading) "Iniciando sesión..." else "Iniciar sesión con Google",
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    isLoading = true
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            )
        }
    }
}
