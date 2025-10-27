package com.example.myway.screens.modulo4

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*
import kotlinx.coroutines.launch
import com.example.myway.ai.PopulatePlaces

@Composable
fun SinPlan(navController: NavController) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Estados para el setup
    var showSetup by remember { mutableStateOf(true) } // Cambiar a false cuando ya hayas creado los lugares
    var isLoading by remember { mutableStateOf(false) }
    var isDone by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(id = R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Text(
                    text = stringResource(R.string.sin_plan),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            // ⚙️ CARD DE SETUP (Solo visible si showSetup = true)
            if (showSetup) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Azul4.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚙️ Configuración Inicial",
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Blanco
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Añade 20 lugares emblemáticos de Bogotá para que la IA tenga mejores recomendaciones",
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            color = Blanco.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    color = Blanco,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Creando lugares en Firebase...",
                                    fontFamily = Nunito,
                                    fontSize = 14.sp,
                                    color = Blanco
                                )
                            }

                            isDone -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Verde,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "✅ ¡Listo! 20 lugares creados",
                                    fontFamily = Nunito,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blanco
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { showSetup = false }
                                ) {
                                    Text(
                                        text = "Continuar",
                                        color = Blanco,
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            errorMessage != null -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "❌ Error: $errorMessage",
                                    fontFamily = Nunito,
                                    fontSize = 14.sp,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showSetup = false },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Blanco
                                        )
                                    ) {
                                        Text("Omitir", fontFamily = Nunito)
                                    }
                                    Button(
                                        onClick = {
                                            errorMessage = null
                                            isLoading = true
                                            scope.launch {
                                                try {
                                                    PopulatePlaces.addSamplePlaces()
                                                    isDone = true
                                                } catch (e: Exception) {
                                                    errorMessage = e.message ?: "Error desconocido"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Blanco
                                        )
                                    ) {
                                        Text(
                                            "Reintentar",
                                            color = Azul4,
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            else -> {
                                Button(
                                    onClick = {
                                        isLoading = true
                                        errorMessage = null
                                        scope.launch {
                                            try {
                                                PopulatePlaces.addSamplePlaces()
                                                isDone = true
                                            } catch (e: Exception) {
                                                errorMessage = e.message ?: "Error desconocido"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Blanco
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🚀 Crear Lugares",
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Azul4
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = { showSetup = false }
                                ) {
                                    Text(
                                        text = "Omitir (usar solo Google Places)",
                                        color = Blanco.copy(alpha = 0.7f),
                                        fontFamily = Nunito,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Textos descriptivos
            Text(
                text = stringResource(R.string.no_sabes),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.tus_mejores),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón: Recomendación Rápida
            CustomButton(
                alignCenter = false,
                text = stringResource(R.string.recomiendame),
                color = Blanco,
                textColor = Negro,
                fontWeight = FontWeight.Normal,
                icon = painterResource(id = R.drawable.ic_search),
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("recomiendame") }
            )

            // Botón: Recomendación Personalizada
            CustomButton(
                alignCenter = false,
                text = stringResource(R.string.mood),
                color = Blanco,
                textColor = Negro,
                fontWeight = FontWeight.Normal,
                icon = painterResource(id = R.drawable.ic_mood),
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("tu_mood") }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}