package com.example.myway.screens.modulo4

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.BuildConfig
import com.example.myway.R
import com.example.myway.ai.*
import com.example.myway.screens.CustomButton
import com.example.myway.services.WeatherAPIService
import com.example.myway.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Recomiendame(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AIRepository(context) }
    val scrollState = rememberScrollState()

    // Estados
    var isLoading by remember { mutableStateOf(false) }
    var recommendation by remember { mutableStateOf<AIRecommendation?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentLocation by remember { mutableStateOf<UserLocation?>(null) }
    var placeDetails by remember { mutableStateOf<RecommendedPlaceDetails?>(null) }
    var photoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) } // Cambio: Bitmap en vez de URL

    // Permisos de ubicación
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    // Obtener ubicación actual
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = UserLocation(it.latitude, it.longitude)
                        Log.d("Recomiendame", "Ubicación obtenida: ${it.latitude}, ${it.longitude}")
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Recomiendame", "Error de permisos: ${e.message}")
            }
        }
    }

    // Obtener detalles del lugar recomendado
// Reemplaza el LaunchedEffect(recommendation) completo (línea ~63-98) con esto:

    LaunchedEffect(recommendation) {
        recommendation?.let { rec ->
            Log.d("Recomiendame", "═══════════════════════")
            Log.d("Recomiendame", "Lugar: ${rec.place.name}")
            Log.d("Recomiendame", "ID: ${rec.place.id}")
            Log.d("Recomiendame", "PhotoURL: '${rec.place.photoUrl}'")
            Log.d("Recomiendame", "Categoría: ${rec.place.category}")
            Log.d("Recomiendame", "═══════════════════════")

            photoBitmap = null
            placeDetails = null

            try {

                if (rec.place.id.startsWith("ChIJ") || rec.place.id.startsWith("Ei")) {
                    Log.d("Recomiendame", "Lugar de Google Places: ${rec.place.id}")

                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.PHONE_NUMBER,
                        Place.Field.RATING,
                        Place.Field.USER_RATINGS_TOTAL,
                        Place.Field.OPENING_HOURS,
                        Place.Field.PHOTO_METADATAS
                    )

                    val request = FetchPlaceRequest.newInstance(rec.place.id, placeFields)
                    placesClient.fetchPlace(request)
                        .addOnSuccessListener { response ->
                            val place = response.place
                            placeDetails = RecommendedPlaceDetails(
                                name = place.name ?: rec.place.name,
                                address = place.address ?: rec.place.address,
                                phone = place.phoneNumber ?: "No disponible",
                                rating = place.rating ?: rec.place.rating,
                                totalRatings = place.userRatingsTotal ?: 0,
                                isOpen = place.isOpen,
                                openingHours = place.openingHours?.weekdayText ?: emptyList()
                            )

                            // Obtener foto de Google
                            place.photoMetadatas?.firstOrNull()?.let { photoMetadata ->
                                val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                    .setMaxWidth(800)
                                    .setMaxHeight(600)
                                    .build()

                                placesClient.fetchPhoto(photoRequest)
                                    .addOnSuccessListener { photoResponse ->
                                        photoBitmap = photoResponse.bitmap
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Recomiendame", "Error: ${exception.message}")
                        }
                } else {
                    // ✅ Es un lugar de Firebase - usar datos básicos
                    Log.d("Recomiendame", "Lugar de Firebase: ${rec.place.name}")
                    placeDetails = RecommendedPlaceDetails(
                        name = rec.place.name,
                        address = rec.place.address,
                        phone = "No disponible",
                        rating = rec.place.rating,
                        totalRatings = 0,
                        isOpen = null,
                        openingHours = emptyList()
                    )
                    // La foto se cargará de rec.place.photoUrl directamente
                }
            } catch (e: Exception) {
                Log.e("Recomiendame", "Error: ${e.message}")
                placeDetails = RecommendedPlaceDetails(
                    name = rec.place.name,
                    address = rec.place.address,
                    phone = "No disponible",
                    rating = rec.place.rating,
                    totalRatings = 0,
                    isOpen = null,
                    openingHours = emptyList()
                )
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
                .verticalScroll(scrollState)
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    text = stringResource(R.string.recomiendame),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mostrar recomendación o botón para obtener
                if (recommendation == null && !isLoading) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Recomendación Inteligente",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Basado en tu ubicación, clima y hora actual",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    CustomButton(
                        text = if (!locationPermission.status.isGranted)
                            "Permitir Ubicación"
                        else
                            "Obtener Recomendación",
                        color = Blanco,
                        textColor = Negro,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (!locationPermission.status.isGranted) {
                                locationPermission.launchPermissionRequest()
                            } else {
                                currentLocation?.let { location ->
                                    isLoading = true
                                    scope.launch {
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                                        // ✅ Pasar location y context
                                        val weather = getCurrentWeather(location, context)
                                        val timeOfDay = getTimeOfDay()

                                        val request = QuickRecommendationRequest(
                                            userLocation = location,
                                            currentWeather = weather,
                                            timeOfDay = getTimeOfDay(),
                                            userId = userId
                                        )

                                        val result = repository.getQuickRecommendation(request)
                                        result.onSuccess {
                                            recommendation = it
                                            Log.d("Recomiendame", "Recomendación: ${it.place.name}")
                                        }.onFailure {
                                            errorMessage = it.message
                                            Log.e("Recomiendame", "Error: ${it.message}")
                                        }
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Mostrar cargando
                if (isLoading) {
                    Spacer(modifier = Modifier.height(64.dp))
                    CircularProgressIndicator(color = Blanco)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analizando opciones...",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontSize = 16.sp
                    )
                }

                // Mostrar resultado
                recommendation?.let { rec ->
                    Spacer(modifier = Modifier.height(16.dp))

                    // Card con la imagen
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                photoBitmap != null -> {
                                    // ✅ Foto de Google Places (Bitmap)
                                    AsyncImage(
                                        model = photoBitmap,
                                        contentDescription = rec.place.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(3.dp, Color.White, RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop,
                                        onLoading = { Log.d("Recomiendame", "Cargando bitmap") },
                                        onSuccess = { Log.d("Recomiendame", "Bitmap cargado") },
                                        onError = { Log.e("Recomiendame", "Error bitmap") }
                                    )
                                }
                                !rec.place.photoUrl.isNullOrEmpty() -> {

                                    AsyncImage(
                                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                                            .data(rec.place.photoUrl)
                                            .crossfade(true)
                                            .allowHardware(false)
                                            .build(),
                                        contentDescription = rec.place.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(3.dp, Color.White, RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop,
                                        onLoading = {
                                            Log.d("Recomiendame", "Cargando: ${rec.place.photoUrl}")
                                        },
                                        onSuccess = {
                                            Log.d("Recomiendame", "Imagen cargada: ${rec.place.photoUrl}")
                                        },
                                        onError = { error ->
                                            Log.e("Recomiendame", "Error: ${error.result.throwable.message}")
                                            Log.e("Recomiendame", "URL: ${rec.place.photoUrl}")
                                        }
                                    )
                                }
                                else -> {

                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp),
                                            tint = Azul4.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre del lugar
                    Text(
                        text = placeDetails?.name ?: rec.place.name,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Blanco
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating
                    if (placeDetails?.rating != null && placeDetails!!.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val rating = placeDetails!!.rating
                            val fullHearts = rating.roundToInt().coerceIn(0, 5)

                            repeat(5) { index ->
                                Icon(
                                    painter = painterResource(
                                        id = if (index < fullHearts)
                                            R.drawable.ic_favorite_filled
                                        else
                                            R.drawable.ic_favorite_outline
                                    ),
                                    contentDescription = null,
                                    tint = if (index < fullHearts)
                                        Color(0xFFE91E63)
                                    else
                                        Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (index < 4) Spacer(modifier = Modifier.width(4.dp))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${"%.1f".format(rating)} (${placeDetails!!.totalRatings} reseñas)",
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                color = Blanco.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Razón de la recomendación
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Azul4.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💡 ¿Por qué este lugar?",
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Blanco
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = rec.reason,
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                color = Blanco
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detalles del lugar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            RecommendationDetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "Dirección",
                                value = placeDetails?.address ?: rec.place.address
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            RecommendationDetailRow(
                                icon = Icons.Default.DirectionsCar,
                                label = "Distancia",
                                value = "${"%.1f".format(rec.distance)} km • ${rec.estimatedDuration}"
                            )

                            if (placeDetails?.phone != null && placeDetails!!.phone != "No disponible") {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                RecommendationDetailRow(
                                    icon = Icons.Default.Phone,
                                    label = "Teléfono",
                                    value = placeDetails!!.phone
                                )
                            }

                            if (placeDetails?.isOpen != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                RecommendationDetailRow(
                                    icon = Icons.Default.Schedule,
                                    label = "Estado",
                                    value = if (placeDetails!!.isOpen == true)
                                        "Abierto ahora"
                                    else
                                        "Cerrado ahora"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de acción
                    CustomButton(
                        text = "Ver Ruta",
                        color = Azul3,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            navController.navigate(
                                "ruta_opciones/${rec.place.id}/${rec.place.name}"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CustomButton(
                        text = "Otra Recomendación",
                        color = Blanco,
                        textColor = Negro,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            recommendation = null
                            placeDetails = null
                            photoBitmap = null
                        }
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun RecommendationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Azul4,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

data class RecommendedPlaceDetails(
    val name: String,
    val address: String,
    val phone: String,
    val rating: Double,
    val totalRatings: Int,
    val isOpen: Boolean?,
    val openingHours: List<String>
)

// Helper functions
private suspend fun getCurrentWeather(location: UserLocation, context: Context): String {
    return WeatherAPIService.getCurrentWeather(location, context)
}

private fun getTimeOfDay(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 6..11 -> "mañana"
        in 12..18 -> "tarde"
        else -> "noche"
    }
}