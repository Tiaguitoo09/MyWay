package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito

@Composable
fun CambioExitoso(navController: NavController) {
    // Espera 2.5 segundos y redirige a la pantalla de ingreso
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        navController.navigate("ingresoUsuario") {
            popUpTo("cambioExitoso") { inclusive = true } // Elimina esta pantalla del stack
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de la app
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = "Fondo de la app",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de círculo
            Image(
                painter = painterResource(id = R.drawable.circuloconfirmacion),
                contentDescription = "Círculo Confirmación",
                modifier = Modifier.size(240.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto principal reutilizable
            CustomTitleText(
                text = "Cambio de contraseña exitoso",
                color = Blanco,
                fontSize = 30.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

