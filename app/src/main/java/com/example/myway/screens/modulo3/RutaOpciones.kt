package com.example.myway.screens.modulo3

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.BuildConfig
import com.example.myway.R
import com.example.myway.data.FavoritesRepository
import com.example.myway.screens.CustomButton
import com.example.myway.screens.modulo2.PreferenciasManager
import com.example.myway.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class RouteInfo(
    val distance: String,
    val duration: String,
    val points: List<LatLng>
)

@Composable
fun RutaOpciones(
    navController: NavController,
    placeId: String?,
    placeName: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val preferencias = remember { PreferenciasManager.cargarPreferencias(context) }
    val repository = remember { FavoritesRepository(context) }

    var isFavorite by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf(LatLng(4.7110, -74.0721)) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }

    var selectedMode by remember {
        mutableStateOf(
            if (preferencias.transportesSeleccionados.contains(preferencias.transportePreferido)) {
                preferencias.transportePreferido
            } else {
                preferencias.transportesSeleccionados.firstOrNull() ?: "driving"
            }
        )
    }
    var isLoading by remember { mutableStateOf(true) }

    var walkingRoute by remember { mutableStateOf<RouteInfo?>(null) }
    var drivingRoute by remember { mutableStateOf<RouteInfo?>(null) }
    var motorcycleRoute by remember { mutableStateOf<RouteInfo?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 12f)
    }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    LaunchedEffect(placeId) {
        if (!placeId.isNullOrEmpty() && placeId != "null") {
            isFavorite = repository.isFavorite(placeId)
        }
    }

    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // ✅ LÓGICA UNIFICADA: Firebase o Google Places
        if (!placeId.isNullOrEmpty() && placeId != "null") {
            // Verificar si es un lugar de Firebase
            if (!placeId.startsWith("ChIJ") && !placeId.startsWith("Ei")) {
                Log.d("RutaOpciones", "📦 Lugar de Firebase detectado: $placeId")

                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val doc = firestore.collection("lugares")
                        .document(placeId)
                        .get()
                        .await()

                    if (doc.exists()) {
                        val lat = doc.getDouble("latitude")
                        val lng = doc.getDouble("longitude")

                        if (lat != null && lng != null) {
                            destinationLocation = LatLng(lat, lng)
                            Log.d("RutaOpciones", "✅ Coordenadas obtenidas: $lat, $lng")

                            scope.launch {
                                if (preferencias.transportesSeleccionados.contains("walking")) {
                                    walkingRoute = getRouteInfo(
                                        currentLocation,
                                        destinationLocation!!,
                                        "walking",
                                        preferencias.rutaMasRapida
                                    )
                                }
                                if (preferencias.transportesSeleccionados.contains("driving")) {
                                    drivingRoute = getRouteInfo(
                                        currentLocation,
                                        destinationLocation!!,
                                        "driving",
                                        preferencias.rutaMasRapida
                                    )
                                }
                                if (preferencias.transportesSeleccionados.contains("motorcycle")) {
                                    motorcycleRoute = getRouteInfo(
                                        currentLocation,
                                        destinationLocation!!,
                                        "driving",
                                        preferencias.rutaMasRapida
                                    )
                                }
                                isLoading = false
                            }
                        } else {
                            Log.e("RutaOpciones", "❌ No se encontraron coordenadas")
                            isLoading = false
                        }
                    } else {
                        Log.e("RutaOpciones", "❌ Documento no existe")
                        isLoading = false
                    }
                } catch (e: Exception) {
                    Log.e("RutaOpciones", "❌ Error: ${e.message}")
                    isLoading = false
                }
            } else {
                // Es un lugar de Google Places
                Log.d("RutaOpciones", "🌍 Lugar de Google Places detectado: $placeId")

                val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
                val request = FetchPlaceRequest.newInstance(placeId, placeFields)
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        destinationLocation = response.place.latLng
                        scope.launch {
                            destinationLocation?.let { dest ->
                                if (preferencias.transportesSeleccionados.contains("walking")) {
                                    walkingRoute = getRouteInfo(
                                        currentLocation,
                                        dest,
                                        "walking",
                                        preferencias.rutaMasRapida
                                    )
                                }
                                if (preferencias.transportesSeleccionados.contains("driving")) {
                                    drivingRoute = getRouteInfo(
                                        currentLocation,
                                        dest,
                                        "driving",
                                        preferencias.rutaMasRapida
                                    )
                                }
                                if (preferencias.transportesSeleccionados.contains("motorcycle")) {
                                    motorcycleRoute = getRouteInfo(
                                        currentLocation,
                                        dest,
                                        "driving",
                                        preferencias.rutaMasRapida
                                    )
                                }
                                isLoading = false
                            }
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        isLoading = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_cargar_lugar),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            isLoading = false
            Toast.makeText(
                context,
                context.getString(R.string.no_info_lugar),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ... (resto del código de UI se mantiene igual - GoogleMap, Column, Surface, etc.)
    // Copio solo lo relevante:

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            Marker(state = MarkerState(position = currentLocation))
            destinationLocation?.let {
                Marker(state = MarkerState(position = it))
            }

            val currentRoute = when (selectedMode) {
                "walking" -> walkingRoute
                "driving" -> drivingRoute
                "motorcycle" -> motorcycleRoute
                else -> drivingRoute
            }

            currentRoute?.points?.let { points ->
                if (points.isNotEmpty()) {
                    Polyline(
                        points = points,
                        color = when (selectedMode) {
                            "walking" -> Color(0xFF34A853)
                            "driving" -> Color(0xFF4285F4)
                            "motorcycle" -> Color(0xFFEA4335)
                            else -> Color(0xFF4285F4)
                        },
                        width = 10f
                    )
                }
            }
        }

        // ... resto del UI (Column, Surface, TransportOption, etc.)
    }
}

// ... (TransportOption y otras funciones composables se mantienen)

suspend fun getRouteInfo(
    origin: LatLng,
    destination: LatLng,
    mode: String,
    useFastestRoute: Boolean = false
): RouteInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY

            val trafficParams = if (useFastestRoute) {
                "&departure_time=now&traffic_model=best_guess"
            } else {
                ""
            }

            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=$mode" +
                    trafficParams +
                    "&key=$apiKey"

            val response = URL(url).readText()

            val distancePattern = """"distance"[^}]*"text"\s*:\s*"([^"]+)"""".toRegex()
            val distance = distancePattern.find(response)?.groupValues?.get(1) ?: "N/A"

            val durationPattern = """"duration"[^}]*"text"\s*:\s*"([^"]+)"""".toRegex()
            val duration = durationPattern.find(response)?.groupValues?.get(1) ?: "N/A"

            val polylinePattern = """"points"\s*:\s*"([^"]+)"""".toRegex()
            val match = polylinePattern.find(response)
            val points = match?.groupValues?.get(1)?.let { PolyUtil.decode(it) } ?: emptyList()

            RouteInfo(distance, duration, points)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}