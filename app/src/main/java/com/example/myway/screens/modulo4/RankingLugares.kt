package com.example.myway.screens.modulo4

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Amarillo
import com.example.myway.ai.Place
import com.example.myway.ai.AIRepository
import com.example.myway.ai.UserLocation
import kotlinx.coroutines.launch

@Composable
fun RankingLugares(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estado para guardar los lugares top
    var topPlaces by remember { mutableStateOf<List<Place>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔄 Cargar los lugares más valorados al iniciar la pantalla
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val repository = AIRepository(context)
            // Obtener la ubicación actual del usuario (Bogotá por defecto)
            val location = UserLocation(
                latitude = 4.7110,
                longitude = -74.0721
            )
            // Usar getTopPlaces que devuelve objetos Place completos
            val result = repository.getTopPlaces(location, radiusKm = 10.0, limit = 10)
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
                CircularProgressIndicator(
                    color = Blanco,
                    modifier = Modifier.padding(32.dp)
                )
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
                        PlaceCard(
                            rank = index + 1,
                            place = place,
                            onClick = {
                                // ✅ Navegar a detalles del lugar
                                navController.navigate("detalles_lugar/${place.id}/${place.name}")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlaceCard(rank: Int, place: Place, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número de ranking con color amarillo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = Amarillo,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Foto del lugar
            if (place.photoUrl != null) {
                AsyncImage(
                    model = place.photoUrl,
                    contentDescription = place.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_favorite_outline),
                    placeholder = painterResource(id = R.drawable.ic_favorite_outline)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_favorite_outline),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del lugar
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.name,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating con estrella
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐",
                        fontSize = 14.sp
                    )
                    Text(
                        text = " ${place.rating}",
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = place.category.replaceFirstChar { it.uppercase() },
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}