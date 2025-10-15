package com.example.myway.screens.modulo2

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun Cargando(navController: NavController) {

    // 🔹 Recuperamos información del usuario (Google o correo normal)
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {
            val correo = usuarioActual.email
            val nombreGoogle = usuarioActual.displayName

            if (nombreGoogle != null) {
                // 🟢 Inicio con Google
                UsuarioTemporal.nombre = nombreGoogle
                UsuarioTemporal.correo = correo ?: ""
            } else if (correo != null) {
                // 🔵 Inicio con correo/contraseña (app propia)
                db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .get()
                    .addOnSuccessListener { docs ->
                        if (!docs.isEmpty) {
                            UsuarioTemporal.nombre = docs.documents[0].getString("nombre") ?: "Usuario"
                            UsuarioTemporal.correo = correo
                        }
                    }
            }
        }

        // Espera antes de navegar
        delay(3000)
        navController.navigate("home") {
            popUpTo("cargando") { inclusive = true }
        }
    }

    // 🔁 Animación de rotación
    val infiniteTransition = rememberInfiniteTransition(label = "rotacionPalito")
    val rotacion by infiniteTransition.animateFloat(
        initialValue = -90f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "angulo"
    )

    // 🔁 Animación de puntos "..."
    var puntos by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            puntos = ""
            delay(500)
            puntos = "."
            delay(500)
            puntos = ".."
            delay(500)
            puntos = "..."
            delay(500)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = "Fondo de la app",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {

                Image(
                    painter = painterResource(id = R.drawable.circulobrujula),
                    contentDescription = "Círculo brújula",
                    modifier = Modifier.size(270.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.palitobrujula),
                    contentDescription = "Palito brújula",
                    modifier = Modifier
                        .size(180.dp)
                        .offset(y = (-10).dp)
                        .rotate(rotacion)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Texto con animación de puntos
            Text(
                text = "Cargando$puntos",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp
            )
        }
    }
}
