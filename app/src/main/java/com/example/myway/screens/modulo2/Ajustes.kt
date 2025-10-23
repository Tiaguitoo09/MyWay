package com.example.myway.screens.modulo2

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/* -------------------------------------------- */
data class OpcionAjuste(
    val tituloResId: Int,
    val ruta: String
)

/* -------------------------------------------- */
class AjustesController(
    private val context: android.content.Context,
    private val navController: NavController
) {
    fun navegarA(ruta: String) {
        if (ruta.isNotEmpty()) {
            navController.navigate(ruta)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.funcionalidad_proximamente),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

/* -------------------------------------------- */
@Composable
fun Ajustes(navController: NavController) {
    val context = LocalContext.current
    val controller = remember { AjustesController(context, navController) }

    // Lista de opciones con rutas actualizadas
    val opciones = listOf(
        OpcionAjuste(R.string.activar_modo_copiloto, "modo_copiloto"),
        OpcionAjuste(R.string.silenciar_notificaciones, "silenciar_notificaciones"),
        OpcionAjuste(R.string.preferencias_viaje, ""),
        OpcionAjuste(R.string.permisos, "permisos")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = stringResource(id = R.string.volver),
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            // Título
            Text(
                text = stringResource(id = R.string.ajustes),
                fontSize = 30.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botones
            opciones.forEach { opcion ->
                CustomButton(
                    text = stringResource(id = opcion.tituloResId),
                    color = Azul3,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(70.dp),
                    fontWeight = FontWeight.ExtraBold,
                    onClick = { controller.navegarA(opcion.ruta) }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

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

        try {
            if (usuarioActual != null) {
                val correo = usuarioActual.email
                val nombreGoogle = usuarioActual.displayName
                val userId = usuarioActual.uid

                Log.d("Cargando", "👤 Usuario autenticado: $userId")

                if (nombreGoogle != null) {
                    //  Login con Google → separar nombre y apellido
                    val partesNombre = nombreGoogle.split(" ")

                    UsuarioTemporal.nombre = partesNombre.firstOrNull() ?: "Usuario"
                    UsuarioTemporal.apellido = if (partesNombre.size > 1)
                        partesNombre.subList(1, partesNombre.size).joinToString(" ")
                    else
                        ""
                    UsuarioTemporal.correo = correo ?: ""

                    // 🔥 CARGA LA FOTO DE GOOGLE O DE FIRESTORE
                    try {
                        val doc = db.collection("usuarios").document(userId).get().await()
                        if (doc.exists()) {
                            val fotoFirestore = doc.getString("fotoPerfil")
                            UsuarioTemporal.fotoUrl = fotoFirestore
                                ?: usuarioActual.photoUrl?.toString()

                            Log.d("Cargando", "✅ Foto de Google/Firestore: ${UsuarioTemporal.fotoUrl}")
                        } else {
                            UsuarioTemporal.fotoUrl = usuarioActual.photoUrl?.toString()
                            Log.d("Cargando", "✅ Foto de Google: ${UsuarioTemporal.fotoUrl}")
                        }
                    } catch (e: Exception) {
                        UsuarioTemporal.fotoUrl = usuarioActual.photoUrl?.toString()
                        Log.e("Cargando", "❌ Error al cargar foto: ${e.message}")
                    }

                } else if (correo != null) {
                    // 🔵 Login con correo Firebase (CORREGIDO CON AWAIT)
                    Log.d("Cargando", "📧 Buscando usuario por correo: $correo")

                    val querySnapshot = db.collection("usuarios")
                        .whereEqualTo("correo", correo)
                        .get()
                        .await() // ESPERA A QUE TERMINE

                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]

                        UsuarioTemporal.nombre = doc.getString("nombre") ?: "Usuario"
                        UsuarioTemporal.apellido = doc.getString("apellido") ?: ""
                        UsuarioTemporal.fechaNacimiento = doc.getString("fechaNacimiento") ?: ""
                        UsuarioTemporal.correo = correo
                        UsuarioTemporal.fotoUrl = doc.getString("fotoPerfil")

                        Log.d("Cargando", "✅ Usuario cargado: ${UsuarioTemporal.nombre}")
                        Log.d("Cargando", "✅ Foto URL: ${UsuarioTemporal.fotoUrl}")
                    } else {
                        Log.e("Cargando", "❌ No se encontró usuario con correo: $correo")
                    }
                }
            } else if (!correoTemporal.isNullOrEmpty()) {
                // 🟣 Login manual (sin FirebaseAuth) - CORREGIDO CON AWAIT
                Log.d("Cargando", "📧 Buscando usuario manual por correo: $correoTemporal")

                val querySnapshot = db.collection("usuarios")
                    .whereEqualTo("correo", correoTemporal)
                    .get()
                    .await() // ✅ ESPERA A QUE TERMINE

                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]

                    UsuarioTemporal.nombre = doc.getString("nombre") ?: "Usuario"
                    UsuarioTemporal.apellido = doc.getString("apellido") ?: ""
                    UsuarioTemporal.fechaNacimiento = doc.getString("fechaNacimiento") ?: ""
                    UsuarioTemporal.fotoUrl = doc.getString("fotoPerfil")

                    Log.d("Cargando", "✅ Usuario manual cargado: ${UsuarioTemporal.nombre}")
                    Log.d("Cargando", "✅ Foto URL: ${UsuarioTemporal.fotoUrl}")
                } else {
                    Log.e("Cargando", "❌ No se encontró usuario con correo: $correoTemporal")
                }
            }
        } catch (e: Exception) {
            Log.e("Cargando", "❌ Error general: ${e.message}")
        }

        // Espera antes de navegar
        delay(2000)

        Log.d("Cargando", "🚀 Navegando a home...")
        Log.d("Cargando", "📸 Foto final en UsuarioTemporal: ${UsuarioTemporal.fotoUrl}")

        navController.navigate("home") {
            popUpTo(0)
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
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.circulobrujula),
                    contentDescription = stringResource(id = R.string.circulo_brujula),
                    modifier = Modifier.size(270.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.palitobrujula),
                    contentDescription = stringResource(id = R.string.palito_brujula),
                    modifier = Modifier
                        .size(180.dp)
                        .offset(y = (-10).dp)
                        .rotate(rotacion)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.cargando) + puntos,
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp
            )
        }
    }
}