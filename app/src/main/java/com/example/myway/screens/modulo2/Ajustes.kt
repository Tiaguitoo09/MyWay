package com.example.myway.screens.modulo2

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*

/* -------------------------------------------- */
data class OpcionAjuste(
    val titulo: String,
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
            Toast.makeText(context, "Funcionalidad próximamente", Toast.LENGTH_SHORT).show()
        }
    }
}

/* -------------------------------------------- */
@Composable
fun Ajustes(navController: NavController) {
    val context = LocalContext.current
    val controller = remember { AjustesController(context, navController) }

    // Lista de opciones con ruta en "Silenciar Notificaciones"
    val opciones = listOf(
        OpcionAjuste("Activar Modo Copiloto", ""),
        OpcionAjuste("Silenciar Notificaciones", "silenciar_notificaciones"),
        OpcionAjuste("Preferencias de Viaje", ""),
        OpcionAjuste("Permisos", "")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = "Volver",
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
                text = "Ajustes",
                fontSize = 30.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botones
            opciones.forEach { opcion ->
                CustomButton(
                    text = opcion.titulo,
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
