package com.example.myway.screens.modulo4

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.myway.data.repository.RecentPlacesRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import android.util.Log

// ========== CATEGORÍAS DISPONIBLES ==========
enum class PlaceCategory(
    val displayName: String,
    val emoji: String,
    val categories: List<String>
) {
    RESTAURANTES(
        displayName = "Restaurantes & Cafés",
        emoji = "🍽️",
        categories = listOf("restaurante", "cafe", "bakery", "food")
    ),
    PARQUES(
        displayName = "Parques & Naturaleza",
        emoji = "🌳",
        categories = listOf("parque", "park", "mirador", "campground")
    ),
    CULTURA(
        displayName = "Cultura & Ocio",
        emoji = "🎨",
        categories = listOf(
            "museo", "museum", "art_gallery",
            "cine", "movie_theater",
            "teatro", "performing_arts_theater",
            "atraccion_turistica", "tourist_attraction",
            "entretenimiento", "aquarium", "zoo", "bowling_alley", "amusement_park"
        )
    ),
    SHOPPING(
        displayName = "Shopping",
        emoji = "🛍️",
        categories = listOf(
            "centro_comercial", "shopping_mall", "department_store",
            "zona_comercial", "store",
            "mercado"
        )
    ),
    VIDA_NOCTURNA(
        displayName = "Vida Nocturna",
        emoji = "🎉",
        categories = listOf("bar", "discoteca", "night_club")
    ),
    HOTELES(
        displayName = "Hoteles",
        emoji = "🏨",
        categories = listOf("hotel", "hospedaje", "motel", "lodging")
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RankingLugaresPorCategorias(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf(PlaceCategory.RESTAURANTES) }
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentLocation by remember { mutableStateOf<UserLocation?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ PERMISO DE UBICACIÓN
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // ✅ OBTENER UBICACIÓN ACTUAL
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = UserLocation(it.latitude, it.longitude)
                        Log.d("RankingCategorias", "📍 Ubicación: ${it.latitude}, ${it.longitude}")
                    } ?: run {
                        // Fallback a Bogotá si no hay ubicación
                        currentLocation = UserLocation(4.7110, -74.0721)
                        Log.d("RankingCategorias", "⚠️ Usando ubicación por defecto: Bogotá")
                    }
                }
            } catch (e: SecurityException) {
                Log.e("RankingCategorias", "Error de permisos: ${e.message}")
                currentLocation = UserLocation(4.7110, -74.0721)
            }
        } else {
            // Sin permiso, usar Bogotá
            currentLocation = UserLocation(4.7110, -74.0721)
        }
    }

    // ✅ CARGAR LUGARES CUANDO CAMBIA LA CATEGORÍA O UBICACIÓN
    LaunchedEffect(selectedCategory, currentLocation) {
        currentLocation?.let { location ->
            isLoading = true
            errorMessage = null

            coroutineScope.launch {
                try {
                    val repository = AIRepository(context)

                    // ✅ USAR CACHE OPTIMIZADO
                    places = repository.getTopPlacesByCategoryWithCache(
                        location = location,
                        category = selectedCategory,
                        radiusKm = 15.0,
                        limit = 15
                    )

                    Log.d("RankingCategorias", "📊 Cargados ${places.size} lugares de ${selectedCategory.displayName}")

                } catch (e: Exception) {
                    Log.e("RankingCategorias", "Error: ${e.message}")
                    errorMessage = "Error al cargar lugares: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.tendencias),
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ✅ SOLICITAR PERMISO SI NO ESTÁ OTORGADO
            if (!locationPermission.status.isGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Azul4.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Blanco,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mejora tu experiencia",
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Blanco
                            )
                            Text(
                                text = "Activa la ubicación para ver lugares cerca de ti",
                                fontFamily = Nunito,
                                fontSize = 12.sp,
                                color = Blanco.copy(alpha = 0.8f)
                            )
                        }
                        TextButton(
                            onClick = { locationPermission.launchPermissionRequest() }
                        ) {
                            Text("Activar", color = Amarillo, fontFamily = Nunito)
                        }
                    }
                }
            }

            // Tabs de Categorías
            CategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Lugares
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Blanco)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando ${selectedCategory.displayName.lowercase()}...",
                                color = Blanco,
                                fontFamily = Nunito,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontFamily = Nunito,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                places.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron lugares en esta categoría",
                            color = Blanco,
                            fontFamily = Nunito,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        places.forEachIndexed { index, place ->
                            PlaceRankingCard(
                                rank = index + 1,
                                place = place,
                                navController = navController
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: PlaceCategory,
    onCategorySelected: (PlaceCategory) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PlaceCategory.values().forEach { category ->
            CategoryChip(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: PlaceCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Amarillo else Blanco.copy(alpha = 0.2f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.emoji,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = category.displayName,
                color = if (isSelected) androidx.compose.ui.graphics.Color.Black else Blanco,
                fontFamily = Nunito,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PlaceRankingCard(
    rank: Int,
    place: Place,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val recentPlacesRepository = remember { RecentPlacesRepository() }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Ranking Badge
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (rank) {
                            1 -> androidx.compose.ui.graphics.Color(0xFFFFD700) // Oro
                            2 -> androidx.compose.ui.graphics.Color(0xFFC0C0C0) // Plata
                            3 -> androidx.compose.ui.graphics.Color(0xFFCD7F32) // Bronce
                            else -> Amarillo.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = androidx.compose.ui.graphics.Color.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Imagen del lugar
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
                        .background(androidx.compose.ui.graphics.Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📍",
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del lugar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐ ${String.format("%.1f", place.rating)}",
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.ui.graphics.Color.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = place.category.replaceFirstChar { it.uppercase() },
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = place.address.take(50) + if (place.address.length > 50) "..." else "",
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    color = androidx.compose.ui.graphics.Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Ver Ruta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Amarillo)
                        .clickable {
                            scope.launch {
                                recentPlacesRepository.saveRecentPlace(
                                    placeId = place.id,
                                    placeName = place.name,
                                    placeAddress = place.address,
                                    latitude = place.latitude,
                                    longitude = place.longitude
                                )
                                navController.navigate(
                                    "ruta_opciones/${place.id}/${place.name}"
                                )
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ver ruta",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                }
            }
        }
    }
}