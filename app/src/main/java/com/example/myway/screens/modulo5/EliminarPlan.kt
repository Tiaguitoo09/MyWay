package com.example.myway.screens.modulo5

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun EliminarPlan(navController: NavController) {
    val context = LocalContext.current
    var planes by remember { mutableStateOf<List<PlanViaje>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var planSeleccionado by remember { mutableStateOf<PlanViaje?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf(false) }

    // Cargar planes desde Firebase
    LaunchedEffect(Unit) {
        cargarPlanesGuardados(
            onSuccess = { planesCargados ->
                planes = planesCargados
                isLoading = false
            },
            onError = { error ->
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
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
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
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

                // Título
                Text(
                    text = stringResource(R.string.eliminar_plan),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje informativo
            Text(
                text = "Selecciona el plan que deseas eliminar:",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Contenido principal
            when {
                isLoading -> {
                    // Loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blanco)
                    }
                }
                planes.isEmpty() -> {
                    // Sin planes
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📭",
                                fontSize = 60.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "No hay planes para eliminar",
                                color = Blanco.copy(alpha = 0.8f),
                                fontFamily = Nunito,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    // Lista de planes
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(planes) { plan ->
                            TarjetaPlanEliminar(
                                plan = plan,
                                onClick = {
                                    planSeleccionado = plan
                                    mostrarDialogo = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogo && planSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) {
                    mostrarDialogo = false
                }
            },
            title = {
                Text(
                    text = "⚠️ Confirmar eliminación",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar el plan \"${planSeleccionado?.titulo}\"?\n\nEsta acción no se puede deshacer.",
                    fontFamily = Nunito,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        eliminando = true
                        eliminarPlan(
                            planId = planSeleccionado!!.id,
                            onSuccess = {
                                // Actualizar lista quitando el plan eliminado
                                planes = planes.filter { it.id != planSeleccionado!!.id }
                                mostrarDialogo = false
                                eliminando = false
                                Toast.makeText(
                                    context,
                                    "Plan eliminado exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { error ->
                                mostrarDialogo = false
                                eliminando = false
                                Toast.makeText(
                                    context,
                                    "Error al eliminar: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rojo),
                    enabled = !eliminando
                ) {
                    Text(
                        text = if (eliminando) "Eliminando..." else "Eliminar",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!eliminando) {
                            mostrarDialogo = false
                        }
                    },
                    enabled = !eliminando
                ) {
                    Text(
                        text = "Cancelar",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        color = Azul1
                    )
                }
            }
        )
    }
}

@Composable
fun TarjetaPlanEliminar(plan: PlanViaje, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Título
                Text(
                    text = plan.titulo,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Azul1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Destino
                Text(
                    text = "📍 ${plan.destino}",
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Fechas
                Text(
                    text = "📅 ${plan.fechaInicio} - ${plan.fechaFin}",
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Icono eliminar
            Icon(
                painter = painterResource(id = R.drawable.icono_restar),
                contentDescription = "Eliminar",
                tint = Rojo,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Función para eliminar un plan de Firebase
fun eliminarPlan(
    planId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("planes_viaje")
        .document(planId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e ->
            onError(e.message ?: "Error desconocido")
        }
}