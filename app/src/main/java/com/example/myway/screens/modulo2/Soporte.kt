package com.example.myway.screens.modulo2

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.myway.ui.theme.*

@Composable
fun Soporte(navController: NavController) {

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.soporte),
                color = Blanco,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            // ======= Contactar con Soporte =======
            DesplegableCard(
                titulo = stringResource(id = R.string.contactar_soporte),
                colorFondo = Azul4,
                colorContenido = Azul3,
                contenido = {
                    Text(
                        text = stringResource(id = R.string.texto_contactar_soporte),
                        color = Blanco,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    SoporteButton(
                        icono = R.drawable.telefono,
                        texto = stringResource(id = R.string.telefono_soporte)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SoporteButton(
                        icono = R.drawable.correo,
                        texto = stringResource(id = R.string.correo_soporte)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ======= Enviar sugerencia o reporte =======
            DesplegableCard(
                titulo = stringResource(id = R.string.enviar_sugerencia),
                colorFondo = Azul3,
                colorContenido = Azul2,
                contenido = {
                    Text(
                        text = stringResource(id = R.string.texto_sugerencia),
                        color = Blanco,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    SoporteButton(
                        icono = R.drawable.correo,
                        texto = stringResource(id = R.string.correo_sugerencias)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ======= Políticas y términos =======
            DesplegableCard(
                titulo = stringResource(id = R.string.politicas_terminos),
                colorFondo = Azul2,
                colorContenido = Azul3,
                contenido = {
                    Text(
                        text = stringResource(id = R.string.texto_politicas),
                        color = Blanco,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start
                    )
                }
            )

            Spacer(modifier = Modifier.height(50.dp))
        }

        // Flecha de volver colocada AL FINAL para que quede encima y reciba clicks
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = stringResource(id = R.string.volver),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .zIndex(1f) // fuerza estar en primer plano
                .clickable { navController.popBackStack() }
        )
    }
}

@Composable
fun DesplegableCard(
    titulo: String,
    colorFondo: androidx.compose.ui.graphics.Color,
    colorContenido: androidx.compose.ui.graphics.Color,
    contenido: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = titulo,
                    color = Blanco,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.flecha_arriba else R.drawable.flecha_abajo
                    ),
                    contentDescription = if (expanded)
                        stringResource(id = R.string.flecha_arriba)
                    else
                        stringResource(id = R.string.flecha_abajo),
                    tint = Blanco,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Contenido expandible
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorContenido.copy(alpha = 0.95f))
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    content = contenido
                )
            }
        }
    }
}

@Composable
fun SoporteButton(icono: Int, texto: String) {
    val context = LocalContext.current

    Surface(
        color = Azul4,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable {
                when {
                    texto.contains("@") -> {
                        // Abrir correo
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$texto")
                        }
                        context.startActivity(intent)
                    }
                    texto.contains("(") || texto.contains(")") || texto.contains("345") -> {
                        // Abrir teléfono
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:6013458500")
                        }
                        context.startActivity(intent)
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icono),
                contentDescription = stringResource(id = R.string.icono_contacto),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = texto,
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}