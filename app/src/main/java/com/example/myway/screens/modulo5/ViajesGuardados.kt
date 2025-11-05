package com.example.myway.screens.modulo5

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Data class para los planes
data class PlanViaje(
    val id: String = "",
    val titulo: String = "",
    val destino: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val duracion: Int = 0,
    val createdAt: Long = 0
)

@Composable
fun ViajesGuardados(navController: NavController) {
    var planes by remember { mutableStateOf<List<PlanViaje>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar planes desde Firebase
    LaunchedEffect(Unit) {
        cargarPlanesGuardados(
            onSuccess = { planesCargados ->
                planes = planesCargados
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Encabezado superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Flecha atrás
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                // Título principal
                Text(
                    text = stringResource(R.string.viajes_guardados),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                // Icono perfil
                Image(
                    painter = painterResource(id = R.drawable.icono_perfil),
                    contentDescription = stringResource(R.string.perfil),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable { navController.navigate("perfil_ajustes") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtítulo
            Text(
                text = stringResource(R.string.planes_hechos),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Contenido principal
            when {
                isLoading -> {
                    // Mostrar loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blanco)
                    }
                }
                errorMessage != null -> {
                    // Mostrar error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Blanco,
                            fontFamily = Nunito,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                planes.isEmpty() -> {
                    // Sin planes guardados
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🧳",
                                fontSize = 60.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "No tienes viajes guardados aún",
                                color = Blanco.copy(alpha = 0.8f),
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("planes_de_viaje") },
                                colors = ButtonDefaults.buttonColors(containerColor = Verde)
                            ) {
                                Text(
                                    "Crear mi primer plan",
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Mostrar lista de planes
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(planes) { plan ->
                            TarjetaPlanViaje(
                                plan = plan,
                                onClick = {

                                    navController.navigate("ver_plan/${plan.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaPlanViaje(plan: PlanViaje, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título del plan
            Text(
                text = plan.titulo,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Azul1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Destino
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📍 ",
                    fontSize = 16.sp
                )
                Text(
                    text = plan.destino,
                    fontFamily = Nunito,
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fechas
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅 ",
                    fontSize = 14.sp
                )
                Text(
                    text = "${plan.fechaInicio} - ${plan.fechaFin}",
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duración
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⏱️ ",
                    fontSize = 14.sp
                )
                Text(
                    text = "${plan.duracion} ${if (plan.duracion == 1) "día" else "días"}",
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Función para cargar planes desde Firebase
fun cargarPlanesGuardados(
    onSuccess: (List<PlanViaje>) -> Unit,
    onError: (String) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        onError("Usuario no autenticado")
        return
    }

    FirebaseFirestore.getInstance()
        .collection("planes_viaje")
        .whereEqualTo("userId", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { snapshot ->
            val planes = snapshot.documents.mapNotNull { doc ->
                try {
                    PlanViaje(
                        id = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        destino = doc.getString("destino") ?: "",
                        fechaInicio = doc.getString("fechaInicio") ?: "",
                        fechaFin = doc.getString("fechaFin") ?: "",
                        duracion = doc.getLong("duracion")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }
            onSuccess(planes)
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Error al cargar planes")
        }
}