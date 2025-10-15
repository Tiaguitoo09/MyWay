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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myway.utils.UsuarioTemporal
import kotlinx.coroutines.delay

@Composable
fun Cargando(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val correoUsuario = auth.currentUser?.email

    // 🔹 Cargar datos del usuario desde Firestore
    LaunchedEffect(Unit) {
        if (correoUsuario != null) {
            db.collection("usuarios")
                .whereEqualTo("correo", correoUsuario)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val nombreUsuario = documents.documents[0].getString("nombre")
                        UsuarioTemporal.correo = correoUsuario
                        UsuarioTemporal.nombre = nombreUsuario ?: ""
                    }
                    // Navegar a Home después de cargar los datos
                    navController.navigate("home") {
                        popUpTo("cargando") { inclusive = true }
                    }
                }
                .addOnFailureListener {
                    // Si falla la carga, igual navega al Home
                    navController.navigate("home") {
                        popUpTo("cargando") { inclusive = true }
                    }
                }
        } else {
            // Si no hay usuario autenticado, ir al login
            delay(1500)
            navController.navigate("login_usuario") {
                popUpTo("cargando") { inclusive = true }
            }
        }
    }

    // 🔹 Animación de rotación
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

    // 🔹 Animación de puntos
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

    // 🔹 Interfaz visual
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
