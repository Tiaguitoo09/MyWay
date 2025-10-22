package com.example.myway.screens.modulo3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.BuildConfig
import com.example.myway.R
import com.example.myway.screens.CustomButton
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

    var currentLocation by remember { mutableStateOf(LatLng(4.7110, -74.0721)) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedMode by remember { mutableStateOf("driving") }
    var isLoading by remember { mutableStateOf(true) }

    // Información de rutas
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

    // Obtener ubicación actual y destino
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

        // Obtener destino
        placeId?.let { id ->
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(id, placeFields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    destinationLocation = response.place.latLng

                    // Calcular todas las rutas
                    scope.launch {
                        destinationLocation?.let { dest ->
                            walkingRoute = getRouteInfo(currentLocation, dest, "walking")
                            drivingRoute = getRouteInfo(currentLocation, dest, "driving")
                            motorcycleRoute = getRouteInfo(currentLocation, dest, "driving") // Google no tiene modo moto, usamos driving
                            isLoading = false
                        }
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa de fondo
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

            // Mostrar ruta seleccionada
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
                        color = androidx.compose.ui.graphics.Color(0xFF4285F4),
                        width = 10f
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Encabezado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Azul4,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.flecha),
                            contentDescription = "Volver",
                            tint = Blanco,
                            modifier = Modifier
                                .size(35.dp)
                                .clickable { navController.popBackStack() }
                        )

                        Text(
                            text = "Opciones de ruta",
                            color = Blanco,
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.size(35.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = placeName ?: "Destino",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontSize = 16.sp
                    )
                }
            }

            // Opciones de transporte
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Blanco,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Selecciona tu medio de transporte",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Azul4
                    )

// ⭐ AGREGAR ESTO AQUÍ ⭐
                    Spacer(modifier = Modifier.height(12.dp))

                    var isFavorite by remember { mutableStateOf(false) } // TODO: Verificar si ya está guardado

                    Button(
                        onClick = {

                            isFavorite = !isFavorite
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFavorite) Azul4 else Azul4.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isFavorite) R.drawable.ic_favorite_filled
                                    else R.drawable.ic_favorite_outline
                                ),
                                contentDescription = "Favorito",
                                tint = if (isFavorite) Blanco else Azul4,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFavorite) "Guardado en favoritos" else "Guardar en favoritos",
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isFavorite) Blanco else Azul4
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Azul4)
                        }
                    } else {
                        // Opción Caminando
                        TransportOption(
                            icon = R.drawable.ic_walk,
                            title = "Caminando",
                            duration = walkingRoute?.duration ?: "N/A",
                            distance = walkingRoute?.distance ?: "N/A",
                            isSelected = selectedMode == "walking",
                            onClick = { selectedMode = "walking" }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Opción Carro
                        TransportOption(
                            icon = R.drawable.ic_car,
                            title = "En carro",
                            duration = drivingRoute?.duration ?: "N/A",
                            distance = drivingRoute?.distance ?: "N/A",
                            isSelected = selectedMode == "driving",
                            onClick = { selectedMode = "driving" }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Opción Moto
                        TransportOption(
                            icon = R.drawable.ic_motorcycle,
                            title = "En moto",
                            duration = motorcycleRoute?.duration ?: "N/A",
                            distance = motorcycleRoute?.distance ?: "N/A",
                            isSelected = selectedMode == "motorcycle",
                            onClick = { selectedMode = "motorcycle" }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón Iniciar navegación
                        CustomButton(
                            text = "Iniciar navegación",
                            color = Azul4,
                            onClick = {
                                val route = when (selectedMode) {
                                    "walking" -> walkingRoute
                                    "driving" -> drivingRoute
                                    "motorcycle" -> motorcycleRoute
                                    else -> drivingRoute
                                }

                                // Navegar a pantalla de navegación activa
                                navController.navigate(
                                    "navegacion_activa/${placeId}/${placeName}/${selectedMode}"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransportOption(
    icon: Int,
    title: String,
    duration: String,
    distance: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Azul4.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = if (isSelected) Azul4 else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = title,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) Azul4 else Color.Black
                    )
                    Text(
                        text = "$duration • $distance",
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            if (isSelected) {
                Icon(
                    painter = painterResource(id = android.R.drawable.radiobutton_on_background),
                    contentDescription = "Seleccionado",
                    tint = Azul4,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

suspend fun getRouteInfo(origin: LatLng, destination: LatLng, mode: String): RouteInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=$mode" +
                    "&key=$apiKey"

            val response = URL(url).readText()

            // Extraer distancia
            val distancePattern = """"distance"[^}]*"text"\s*:\s*"([^"]+)"""".toRegex()
            val distance = distancePattern.find(response)?.groupValues?.get(1) ?: "N/A"

            // Extraer duración
            val durationPattern = """"duration"[^}]*"text"\s*:\s*"([^"]+)"""".toRegex()
            val duration = durationPattern.find(response)?.groupValues?.get(1) ?: "N/A"

            // Extraer polyline
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