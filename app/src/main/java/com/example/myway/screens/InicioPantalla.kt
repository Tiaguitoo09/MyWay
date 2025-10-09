package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
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
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito

@Composable
fun InicioPantalla(navController: NavController) {
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
            // Imagen de brújula
            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = "Ícono de brújula",
                modifier = Modifier.size(240.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto principal reutilizable
            CustomTitleText(
                text = "MyWay",
                color = Blanco,
                fontSize = 80.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center

            )

            Spacer(modifier = Modifier.height(30.dp))

            // Botón reutilizable
            CustomButton(
                text = "Pulsa para comenzar",
                color = Azul3,
                onClick = { navController.navigate("ingreso_usuario") }
            )
        }
    }
}
