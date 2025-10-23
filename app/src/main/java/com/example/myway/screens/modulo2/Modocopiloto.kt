package com.example.myway.screens.modulo2

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTitleText
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Azul4
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Rojo
import com.example.myway.ui.theme.Verde
import com.example.myway.utils.DrivingDetector

@Composable
fun ModoCopiloto(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
    }

    var modoCopilotoActivado by remember {
        mutableStateOf(sharedPreferences.getBoolean("modo_copiloto", false))
    }
    var mostrarDialogo by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val isDriving by DrivingDetector.isDriving.collectAsState()

    LaunchedEffect(Unit) {
        modoCopilotoActivado = sharedPreferences.getBoolean("modo_copiloto", false)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = stringResource(id = R.string.volver),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .zIndex(3f)
                .clickable { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Modo Copiloto",
                color = Blanco,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Nunito,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
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
                        text = "Permite que tu copiloto use la aplicación mientras conduces, sin restricciones ni bloqueos de seguridad",
                        color = Blanco,
                        fontSize = 17.sp,
                        fontFamily = Nunito,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = modoCopilotoActivado,
                            onCheckedChange = { mostrarDialogo = true },
                            modifier = Modifier.scale(1.5f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Verde,
                                checkedTrackColor = Verde.copy(alpha = 0.5f),
                                uncheckedThumbColor = Rojo,
                                uncheckedTrackColor = Rojo.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = if (modoCopilotoActivado) "ON" else "OFF",
                            color = Blanco,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Nunito
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    if (isDriving && modoCopilotoActivado) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Verde.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✅",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Modo Copiloto activo - La app está desbloqueada",
                                    color = Blanco,
                                    fontSize = 14.sp,
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = "Cuando actives el Modo Copiloto:",
                        color = Blanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        fontFamily = Nunito,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp)
                    ) {
                        BulletPoint("El copiloto puede usar la app por otra persona mientras manejas.")
                        Spacer(modifier = Modifier.height(8.dp))
                        BulletPoint("Se desactivan temporalmente las notificaciones de seguridad que bloquean la pantalla.")
                        Spacer(modifier = Modifier.height(8.dp))
                        BulletPoint("Podrás ingresar destinos o consultar rutas sin interrupciones.")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Azul4.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "⚠️ Recuerda: esta función es para que un copiloto maneje la app por ti. No uses el teléfono mientras conduces.",
                            color = Blanco,
                            fontSize = 15.sp,
                            fontFamily = Nunito,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        if (mostrarDialogo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
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
                        contentDescription = "Advertencia",
                        modifier = Modifier.size(180.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CustomTitleText(
                        text = if (modoCopilotoActivado)
                            "¿Deseas desactivar el Modo Copiloto?"
                        else
                            "¿Seguro que quieres activar el Modo Copiloto?",
                        color = Blanco,
                        fontSize = 26.sp,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!modoCopilotoActivado) {
                        Text(
                            text = "Esta función permite que otra persona use la app por ti mientras conduces. No uses el teléfono mientras manejas.",
                            color = Blanco,
                            fontSize = 16.sp,
                            fontFamily = Nunito,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        Text(
                            text = "Al desactivar el Modo Copiloto, volverán las restricciones de seguridad cuando la app detecte que estás conduciendo.",
                            color = Blanco,
                            fontSize = 16.sp,
                            fontFamily = Nunito,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CustomButton(
                            text = "Sí",
                            color = Verde,
                            modifier = Modifier.width(140.dp),
                            onClick = {
                                // Cambiar estado
                                modoCopilotoActivado = !modoCopilotoActivado

                                // Guardar en SharedPreferences
                                sharedPreferences.edit()
                                    .putBoolean("modo_copiloto", modoCopilotoActivado)
                                    .apply()

                                // ✅ Forzar actualización inmediata del DrivingDetector
                                DrivingDetector.checkCopilotMode()

                                // Cerrar diálogo
                                mostrarDialogo = false
                            }
                        )
                        CustomButton(
                            text = "No",
                            color = Rojo,
                            modifier = Modifier.width(140.dp),
                            onClick = { mostrarDialogo = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = Blanco,
            fontSize = 16.sp,
            fontFamily = Nunito,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            color = Blanco,
            fontSize = 16.sp,
            fontFamily = Nunito,
            lineHeight = 22.sp
        )
    }
}