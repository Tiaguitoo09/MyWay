package com.example.myway.screens.modulo4

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.myway.ai.PlaceRecommendation
import com.example.myway.ai.AIRepository
import kotlinx.coroutines.launch

@Composable
fun RankingLugares(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    // Estado para guardar los lugares top
    var topPlaces by remember { mutableStateOf<List<PlaceRecommendation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔄 Cargar los lugares más valorados al iniciar la pantalla
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val repository = AIRepository(context)
            val result = repository.getTopPlacesAI(limit = 10) // IA o datos de Google
            topPlaces = result
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== Encabezado =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Flecha de volver
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(id = R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.ranking_lugares_titulo),
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.no_sabes),
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ===== Contenido =====
            if (isLoading) {
                Text(
                    text = "Cargando lugares...",
                    color = Blanco,
                    fontFamily = Nunito,
                    fontSize = 16.sp
                )
            } else if (topPlaces.isEmpty()) {
                Text(
                    text = "No se encontraron lugares recomendados.",
                    color = Blanco,
                    fontFamily = Nunito,
                    fontSize = 16.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    topPlaces.forEachIndexed { index, place ->
                        PlaceCard(rank = index + 1, place = place)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlaceCard(rank: Int, place: PlaceRecommendation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número de ranking
            Text(
                text = "#$rank",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.width(50.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Información del lugar
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.nombre,
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = place.ciudad,
                    color = Blanco.copy(alpha = 0.8f),
                    fontFamily = Nunito,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "⭐ ${place.calificacion}  •  ${place.categoria}",
                    color = Blanco,
                    fontFamily = Nunito,
                    fontSize = 14.sp
                )

                Text(
                    text = place.razon,
                    color = Blanco.copy(alpha = 0.8f),
                    fontFamily = Nunito,
                    fontSize = 13.sp
                )
            }
        }
    }
}