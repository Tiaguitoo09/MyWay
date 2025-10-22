package com.example.myway.screens.modulo2

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
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTitleText
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Azul4
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Rojo
import com.example.myway.ui.theme.Verde

@Composable
fun SilenciarNotificaciones(navController: NavController) {
    val context = LocalContext.current
    var notificacionesSilenciadas by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
                .zIndex(3f)
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
                text = stringResource(id = R.string.silenciar_notificaciones_titulo),
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
                        text = stringResource(id = R.string.pausar_notificaciones),
                        color = Blanco,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.cuando_activado),
                            color = Blanco,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.no_recibiras_notificaciones),
                            color = Blanco,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(id = R.string.notificaciones_guardadas),
                            color = Blanco,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(id = R.string.reactivar_ajustes),
                            color = Blanco,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Texto Recuerda
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Azul1,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Companion.Underline
                                )
                            ) {
                                append(context.getString(R.string.recuerda))
                            }
                            withStyle(
                                SpanStyle(
                                    color = Azul1,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Companion.Underline
                                )
                            ) {
                                append(context.getString(R.string.alerta_silenciar))
                            }
                        },
                        fontSize = 17.sp,
                        textAlign = TextAlign.Companion.Start,
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
                            modifier = Modifier.Companion.scale(1.3f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Verde,
                                checkedTrackColor = Verde,
                                uncheckedThumbColor = Rojo,
                                uncheckedTrackColor = Rojo
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (notificacionesSilenciadas)
                                stringResource(id = R.string.on)
                            else
                                stringResource(id = R.string.off),
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
                        contentDescription = stringResource(id = R.string.confirmacion),
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CustomTitleText(
                        text = if (notificacionesSilenciadas)
                            stringResource(id = R.string.activar_notificaciones)
                        else
                            stringResource(id = R.string.seguro_silenciar),
                        color = Blanco,
                        fontSize = 26.sp,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Companion.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CustomButton(
                            text = stringResource(id = R.string.si),
                            color = Azul3,
                            modifier = Modifier.width(140.dp),
                            onClick = {
                                notificacionesSilenciadas = !notificacionesSilenciadas
                                mostrarDialogo = false
                            }
                        )
                        CustomButton(
                            text = stringResource(id = R.string.no),
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