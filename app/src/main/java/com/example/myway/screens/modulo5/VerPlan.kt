package com.example.myway.screens.modulo5

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun VerPlan(navController: NavController, planId: String) {
    var plan by remember { mutableStateOf<PlanViaje?>(null) }
    var itinerario by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar plan desde Firebase
    LaunchedEffect(planId) {
        cargarPlanPorId(
            planId = planId,
            onSuccess = { planCargado, itinerarioCargado ->
                plan = planCargado
                itinerario = itinerarioCargado
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header con flecha atrás
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Mi Plan de Viaje",
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blanco)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "❌",
                                fontSize = 60.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error: $errorMessage",
                                color = Blanco,
                                fontFamily = Nunito,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                plan != null -> {
                    // Información del plan
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = plan!!.titulo,
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Azul1
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📍 ", fontSize = 18.sp)
                                Text(
                                    text = plan!!.destino,
                                    fontFamily = Nunito,
                                    fontSize = 18.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📅 ", fontSize = 16.sp)
                                Text(
                                    text = "${plan!!.fechaInicio} - ${plan!!.fechaFin}",
                                    fontFamily = Nunito,
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⏱️ ", fontSize = 16.sp)
                                Text(
                                    text = "${plan!!.duracion} ${if (plan!!.duracion == 1) "día" else "días"}",
                                    fontFamily = Nunito,
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Itinerario
                    if (itinerario.isNotEmpty()) {
                        Text(
                            text = "📋 Itinerario",
                            color = Blanco,
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        itinerario.forEachIndexed { index, actividad ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Blanco.copy(alpha = 0.95f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Verde,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )

                                    Column {
                                        Text(
                                            text = "Día ${index + 1}",
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Azul1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = actividad,
                                            fontFamily = Nunito,
                                            fontSize = 15.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Sin itinerario guardado
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Blanco.copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📝",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Este plan aún no tiene itinerario generado",
                                    fontFamily = Nunito,
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función para cargar un plan específico por ID
fun cargarPlanPorId(
    planId: String,
    onSuccess: (PlanViaje, List<String>) -> Unit,
    onError: (String) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("planes_viaje")
        .document(planId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                try {
                    val plan = PlanViaje(
                        id = document.id,
                        titulo = document.getString("titulo") ?: "",
                        destino = document.getString("destino") ?: "",
                        fechaInicio = document.getString("fechaInicio") ?: "",
                        fechaFin = document.getString("fechaFin") ?: "",
                        duracion = document.getLong("duracion")?.toInt() ?: 0,
                        createdAt = document.getLong("createdAt") ?: 0
                    )

                    // Intentar cargar el itinerario (si existe)
                    @Suppress("UNCHECKED_CAST")
                    val itinerario = document.get("itinerario") as? List<String> ?: emptyList()

                    onSuccess(plan, itinerario)
                } catch (e: Exception) {
                    onError("Error al procesar el plan: ${e.message}")
                }
            } else {
                onError("Plan no encontrado")
            }
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Error al cargar el plan")
        }
}