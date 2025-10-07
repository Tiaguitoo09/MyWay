package com.example.myway.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myway.R
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Nunito
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.navigation.NavController


@Composable
public fun InicioPantalla(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = "Fondo de la app",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = "Ícono de brújula",
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MY WAY",
                color = Blanco,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Nunito
            )

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = { navController.navigate("ingreso_usuario") },
                colors = ButtonDefaults.buttonColors(containerColor = Azul3),
            ) {
                Text(
                    text = "Pulsa para comenzar",
                    fontFamily = Nunito,
                    color = Blanco,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

