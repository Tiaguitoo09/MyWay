package com.example.myway.screens.modulo1

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomTitleText
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import kotlinx.coroutines.delay

@Composable
fun CambioExitoso(navController: NavController) {
    // Espera unos segundos y redirige a la pantalla de ingreso
    LaunchedEffect(Unit) {
        delay(3500)
        navController.navigate("ingreso_usuario") {
            popUpTo("cambio_exitoso") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Fondo de la app
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = "Fondo de la app",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de círculo con check
            Image(
                painter = painterResource(id = R.drawable.circuloconfirmacion),
                contentDescription = "Confirmación",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texto principal
            CustomTitleText(
                text = "Cambio de contraseña\nexitoso",
                color = Blanco,
                fontSize = 28.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )


            Spacer(modifier = Modifier.height(24.dp))



        }
    }
}


