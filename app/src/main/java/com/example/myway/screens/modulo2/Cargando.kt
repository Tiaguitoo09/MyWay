package com.example.myway.screens.modulo2

import androidx.activity.compose.BackHandler
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

    BackHandler(enabled = true) {
        // No hacer nada
    }
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val usuarioActual = auth.currentUser
        val correoTemporal = UsuarioTemporal.correo

        if (usuarioActual != null) {
            val correo = usuarioActual.email
            val nombreGoogle = usuarioActual.displayName

            if (nombreGoogle != null) {
                // 🟢 Login con Google → separar nombre y apellido
                val partesNombre = nombreGoogle.split(" ")

                UsuarioTemporal.nombre = partesNombre.firstOrNull() ?: "Usuario"
                UsuarioTemporal.apellido = if (partesNombre.size > 1)
                    partesNombre.subList(1, partesNombre.size).joinToString(" ")
                else
                    ""

                UsuarioTemporal.correo = correo ?: ""
            } else if (correo != null) {
                // 🔵 Login con correo Firebase
                db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .get()
                    .addOnSuccessListener { docs ->
                        if (!docs.isEmpty) {
                            val doc = docs.documents[0]

                            UsuarioTemporal.nombre = doc.getString("nombre") ?: "Usuario"
                            UsuarioTemporal.apellido = doc.getString("apellido") ?: ""
                            UsuarioTemporal.fechaNacimiento = doc.getString("fechaNacimiento") ?: ""
                            UsuarioTemporal.correo = correo
                        }
                    }
            }
        } else if (!correoTemporal.isNullOrEmpty()) {
            // 🟣 Login manual (sin FirebaseAuth)
            db.collection("usuarios")
                .whereEqualTo("correo", correoTemporal)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val doc = docs.documents[0]
                        UsuarioTemporal.nombre = doc.getString("nombre") ?: "Usuario"
                        UsuarioTemporal.apellido = doc.getString("apellido") ?: ""
                        UsuarioTemporal.fechaNacimiento = doc.getString("fechaNacimiento") ?: ""
                    }
                }
        }

        // Espera antes de navegar
        delay(3000)
        navController.navigate("home") {
            popUpTo(0) // 🔹 Limpia toda la pila de navegación
            launchSingleTop = true
        }

    }

    // Animación rotatoria
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

    // Animación puntos "..."
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
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = "Fondo de la app",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
