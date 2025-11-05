package com.example.myway.screens.modulo1

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.Blanco
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Verificar si hay un usuario autenticado
    LaunchedEffect(Unit) {
        delay(1500) // Esperar 1.5 segundos para mostrar el splash

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // ✅ Usuario ya está logueado → ir directo al Home
            navController.navigate("cargando") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // ❌ No hay usuario → ir a la pantalla de inicio/login
            navController.navigate("inicio") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // Pantalla de splash mientras verifica
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Puedes poner tu logo aquí si tienes uno
            // Image(painter = painterResource(id = R.drawable.logo), ...)

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                color = Blanco,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}