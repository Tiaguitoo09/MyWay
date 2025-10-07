package com.example.myway.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.OutlinedText


@Composable
fun InicioPantalla(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de la app
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
            // Imagen de brújula
            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = "Ícono de brújula",
                modifier = Modifier.size(240.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto principal
            Text(
                text = "MyWay",
                color = Blanco,
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Nunito
            )

            Spacer(modifier = Modifier.height(30.dp))

            //boton
            Button(
                onClick = { navController.navigate("ingreso_usuario") },
                colors = ButtonDefaults.buttonColors(containerColor = Azul3),
                border = BorderStroke(1.dp, Azul1),
                modifier = Modifier
                    .width(320.dp) // ancho fijo
                    .height(50.dp) // alto fijo
            ){
                //Borde texto
                OutlinedText(
                    text = "Pulsa para comenzar",
                    fontSize = 16.sp,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Normal,
                    textColor = Blanco,   // color del texto principal
                    borderColor = Azul1,  // color del borde
                    borderWidth = 1f      // grosor del borde, ajustable
                )

            }
        }
    }
}
