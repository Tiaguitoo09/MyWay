package com.example.myway.screens.modulo5

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito

@Composable
fun ViajesGuardados(navController: NavController) {

    Box(modifier = Modifier.fillMaxSize()) {
        // 🔹 Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // 🔹 Encabezado superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Flecha atrás
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                // Título principal
                Text(
                    text = stringResource(R.string.viajes_guardados),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                // Icono perfil
                Image(
                    painter = painterResource(id = R.drawable.icono_perfil),
                    contentDescription = stringResource(R.string.perfil),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable { navController.navigate("perfil_ajustes") }
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // 🔹 Subtítulo o descripción
            Text(
                text = stringResource(R.string.planes_hechos),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )

            // 🔹 Espacio para el contenido futuro
            Text(
                text = "Aquí aparecerán tus viajes guardados 🧳",
                color = Blanco.copy(alpha = 0.8f),
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            )
        }
    }
}
