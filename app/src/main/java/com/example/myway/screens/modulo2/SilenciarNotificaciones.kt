package com.example.myway.screens.modulo2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTitleText

@Composable
fun SilenciarNotificaciones(navController: NavController) {
    var notificacionesSilenciadas by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Flecha volver (posición fija arriba a la izquierda) — ahora con zIndex alto
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = "Volver",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .zIndex(3f) // <- garantiza que esté encima de overlays
                .clickable { navController.popBackStack() }
        )

        // Contenido principal con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Título principal
            Text(
                text = "Silenciar Notificaciones",
                color = Blanco,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tarjeta informativa
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Azul3.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Activa esta opción para pausar temporalmente las notificaciones de la app.",
                        color = Blanco,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text("Cuando está activado:", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• No recibirás notificaciones emergentes ni sonidos de alertas.", color = Blanco, fontSize = 16.sp)
                        Text("• Las notificaciones quedarán guardadas en el historial para que las consultes luego.", color = Blanco, fontSize = 16.sp)
                        Text("• Podrás reactivarlas desde los Ajustes cuando quieras.", color = Blanco, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Texto "Recuerda" subrayado
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Azul1,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Recuerda: ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = Azul1,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("al silenciar notificaciones, podrías perder alertas importantes de tráfico o seguridad en tiempo real.")
                            }
                        },
                        fontSize = 17.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Switch ON / OFF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = notificacionesSilenciadas,
                            onCheckedChange = { mostrarDialogo = true },
                            modifier = Modifier.scale(1.3f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Verde,
                                checkedTrackColor = Verde,
                                uncheckedThumbColor = Rojo,
                                uncheckedTrackColor = Rojo
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (notificacionesSilenciadas) "ON" else "OFF",
                            color = Blanco,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Pantalla de confirmación personalizada (overlay)
        if (mostrarDialogo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f) // <- overlay por debajo de la flecha
                    .background(Azul3.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.circuloadvertencia),
                        contentDescription = "Confirmación",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CustomTitleText(
                        text = if (notificacionesSilenciadas)
                            "¿Quieres activar las notificaciones?"
                        else
                            "¿Seguro que quieres\nsilenciar las notificaciones?",
                        color = Blanco,
                        fontSize = 26.sp,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CustomButton(
                            text = "Sí",
                            color = Azul3,
                            modifier = Modifier.width(140.dp),
                            onClick = {
                                notificacionesSilenciadas = !notificacionesSilenciadas
                                mostrarDialogo = false
                            }
                        )
                        CustomButton(
                            text = "No",
                            color = Azul4,
                            modifier = Modifier.width(140.dp),
                            onClick = { mostrarDialogo = false }
                        )
                    }
                }
            }
        }
    }
}
