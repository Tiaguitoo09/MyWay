package com.example.myway.screens.modulo4

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*

// ====== PASOS PARA USAR SOLO GOOGLE PLACES API ======
// 1. Elimina el archivo PopulatePlaces.kt
// 2. En AIRepository.kt, comenta o elimina las líneas 164-231
//    (la función getPlacesFromFirebaseWithCache)
// 3. En AIRepository.kt línea 154, cambia:
//    val firebasePlaces = getPlacesFromFirebaseWithCache(location, radiusKm)
//    POR:
//    val firebasePlaces = emptyList<Place>()
// 4. Listo! Ahora solo usas Google Places API

@Composable
fun SinPlan(navController: NavController) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(id = R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Text(
                    text = stringResource(R.string.sin_plan),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            // Textos descriptivos
            Text(
                text = stringResource(R.string.no_sabes),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.tus_mejores),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Botón: Recomendación Rápida
            CustomButton(
                alignCenter = false,
                text = stringResource(R.string.recomiendame),
                color = Blanco,
                textColor = Negro,
                fontWeight = FontWeight.Normal,
                icon = painterResource(id = R.drawable.ic_search),
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("recomiendame") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón: Recomendación Personalizada
            CustomButton(
                alignCenter = false,
                text = stringResource(R.string.mood),
                color = Blanco,
                textColor = Negro,
                fontWeight = FontWeight.Normal,
                icon = painterResource(id = R.drawable.ic_mood),
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("tu_mood") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón: Ranking de Lugares
            CustomButton(
                alignCenter = false,
                text = stringResource(R.string.ranking_lugares_top),
                color = Blanco,
                textColor = Negro,
                fontWeight = FontWeight.Normal,
                icon = painterResource(id = R.drawable.ranking),
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("ranking_lugares") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.robot_ia),
                contentDescription = stringResource(R.string.robot_ia),
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 20.dp)
            )
        }
    }
}