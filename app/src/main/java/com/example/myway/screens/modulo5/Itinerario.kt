package com.example.myway.screens.modulo5

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Itinerario(
    navController: NavController,
    titulo: String,
    destino: String,
    fechaInicio: String,
    fechaFin: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCreating by remember { mutableStateOf(false) }
    var itinerario by remember { mutableStateOf<List<String>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Itinerario",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tarjeta principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = titulo,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Azul1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📍 $destino", fontFamily = Nunito, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "📅 $fechaInicio - $fechaFin",
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Crear Plan
            if (isCreating) {
                CircularProgressIndicator(color = Blanco)
                Text(
                    "Creando itinerario personalizado...",
                    color = Blanco,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Button(
                    onClick = {
                        isCreating = true
                        scope.launch {
                            // Simula proceso de creación
                            delay(2000)


                            val itinerarioGenerado = generarItinerario(destino)


                            guardarPlanConItinerario(
                                titulo, destino, fechaInicio, fechaFin,
                                itinerarioGenerado,
                                onSuccess = {
                                    itinerario = itinerarioGenerado
                                    isCreating = false
                                    Toast.makeText(context, "¡Plan creado con éxito!", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    isCreating = false
                                    Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde)
                ) {
                    Text("Crear Plan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar itinerario generado
            itinerario?.let { lista ->
                Text(
                    "Itinerario sugerido ✨",
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                lista.forEachIndexed { index, actividad ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Día ${index + 1}: $actividad",
                            modifier = Modifier.padding(16.dp),
                            fontFamily = Nunito,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

//Guarda el plan CON ITINERARIO en Firestore
fun guardarPlanConItinerario(
    titulo: String,
    destino: String,
    fechaInicio: String,
    fechaFin: String,
    itinerario: List<String>, // ✅ Ahora recibe el itinerario
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        onError("Usuario no autenticado")
        return
    }

    val planId = UUID.randomUUID().toString()
    val plan = mapOf(
        "id" to planId,
        "userId" to userId,
        "titulo" to titulo,
        "destino" to destino,
        "fechaInicio" to fechaInicio,
        "fechaFin" to fechaFin,
        "duracion" to calcularDias(fechaInicio, fechaFin),
        "itinerario" to itinerario, // ✅ Guardar el itinerario aquí
        "createdAt" to System.currentTimeMillis()
    )

    FirebaseFirestore.getInstance()
        .collection("planes_viaje")
        .document(planId)
        .set(plan)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onError(it.message ?: "Error desconocido") }
}

// Calcula duración en días
fun calcularDias(inicio: String, fin: String): Int {
    return try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaInicio = format.parse(inicio)
        val fechaFin = format.parse(fin)
        if (fechaInicio != null && fechaFin != null) {
            ((fechaFin.time - fechaInicio.time) / (1000 * 60 * 60 * 24)).toInt() + 1
        } else 1
    } catch (e: Exception) {
        1
    }
}

// Generador simulado de itinerario con IA
fun generarItinerario(destino: String): List<String> {
    return listOf(
        "Explorar los lugares emblemáticos de $destino.",
        "Visitar los museos y probar la gastronomía local.",
        "Tour cultural por los alrededores.",
        "Día libre para actividades al aire libre o compras.",
        "Paseo nocturno y despedida de $destino."
    )
}