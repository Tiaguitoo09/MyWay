package com.example.myway.screens.modulo4

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.myway.ui.theme.*
import com.example.myway.ai.Place
import com.example.myway.ai.AIRepository
import com.example.myway.ai.UserLocation
import kotlinx.coroutines.launch

@Composable
fun RankingLugares(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var topPlaces by remember { mutableStateOf<List<Place>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val repository = AIRepository(context)
            val location = UserLocation(4.7110, -74.0721) // Bogotá por defecto
            topPlaces = repository.getTopPlaces(location, radiusKm = 10.0, limit = 10)
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
            // 🔹 Encabezado
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

            // 🔹 Contenido principal
            when {
                isLoading -> {
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
                }

                topPlaces.isEmpty() -> {
                    Text(
                        text = "No se encontraron lugares recomendados.",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontSize = 16.sp
                    )
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        topPlaces.forEachIndexed { index, place ->
                            PlaceCard(rank = index + 1, place = place, navController = navController)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlaceCard(rank: Int, place: Place, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Número
                Text(
                    text = "#$rank",
                    color = Amarillo,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Imagen
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
                    Image(
                        painter = painterResource(id = R.drawable.ic_favorite_outline),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = androidx.compose.ui.graphics.Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "⭐ ${place.rating}",
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        color = androidx.compose.ui.graphics.Color.Black
                    )

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

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = place.address ?: "Dirección no disponible",
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    color = androidx.compose.ui.graphics.Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = place.phoneNumber ?: "No disponible",
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    color = androidx.compose.ui.graphics.Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Botón directo a mapa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Amarillo.copy(alpha = 0.15f))
                    .clickable {
                        navController.navigate(
                            "ruta_opciones/${place.latitude}/${place.longitude}/${place.name}"
                        )
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Marcar ruta",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Amarillo
                )
            }
        }
    }
}
