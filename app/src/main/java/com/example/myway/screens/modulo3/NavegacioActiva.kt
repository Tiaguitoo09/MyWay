package com.example.myway.screens.modulo3

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myway.BuildConfig
import com.example.myway.R
import com.example.myway.ui.theme.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import org.json.JSONObject
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class NavigationStep(
    val instruction: String,
    val distance: String,
    val duration: String,
    val maneuver: String?
)

@Composable
fun NavegacionActiva(
    navController: NavController,
    placeId: String?,
    placeName: String?,
    transportMode: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var currentLocation by remember { mutableStateOf(LatLng(4.7110, -74.0721)) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var currentStep by remember { mutableStateOf<NavigationStep?>(null) }
    var nextStep by remember { mutableStateOf<NavigationStep?>(null) }
    var distanceToDestination by remember { mutableStateOf("Calculando...") }
    var timeToDestination by remember { mutableStateOf("") }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var allSteps by remember { mutableStateOf<List<NavigationStep>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 17f)
    }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    currentLocation = LatLng(location.latitude, location.longitude)
                    cameraPositionState.position = CameraPosition.Builder()
                        .target(currentLocation)
                        .zoom(17f)
                        .bearing(location.bearing)
                        .tilt(45f)
                        .build()

                    // Actualizar distancia
                    destinationLocation?.let { dest ->
                        val distance = calculateDistance(currentLocation, dest)
                        distanceToDestination = formatDistance(distance)
                    }

                    // Verificar si llegamos al siguiente paso
                    if (allSteps.isNotEmpty() && currentStepIndex < allSteps.size - 1) {
                        // Lógica para avanzar al siguiente paso
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    // Iniciar seguimiento de ubicación
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000
            ).build()

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Obtener ruta y pasos de navegación
    LaunchedEffect(placeId) {
        placeId?.let { id ->
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(id, placeFields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    destinationLocation = response.place.latLng

                    scope.launch {
                        destinationLocation?.let { dest ->
                            val mode = when (transportMode) {
                                "walking" -> "walking"
                                "driving" -> "driving"
                                "motorcycle" -> "driving"
                                else -> "driving"
                            }

                            val routeData = getNavigationRoute(currentLocation, dest, mode)
                            routePoints = routeData.first
                            allSteps = routeData.second

                            if (allSteps.isNotEmpty()) {
                                currentStep = allSteps[0]
                                if (allSteps.size > 1) {
                                    nextStep = allSteps[1]
                                }
                            }
                        }
                    }
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa en modo navegación
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {
            // Marcador de ubicación actual (personalizado)
            Marker(
                state = MarkerState(position = currentLocation),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )

            // Marcador de destino
            destinationLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = placeName
                )
            }

            // Ruta
            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = androidx.compose.ui.graphics.Color(0xFF4285F4),
                    width = 12f
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Panel superior con instrucciones
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Azul4,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Cerrar",
                            tint = Blanco,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    fusedLocationClient.removeLocationUpdates(locationCallback)
                                    navController.popBackStack()
                                }
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = distanceToDestination,
                                color = Blanco,
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                            Text(
                                text = timeToDestination,
                                color = Blanco,
                                fontFamily = Nunito,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instrucción actual
                    currentStep?.let { step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = getManeuverIcon(step.maneuver)),
                                contentDescription = step.maneuver,
                                tint = Blanco,
                                modifier = Modifier.size(48.dp)
                            )

                            Column {
                                Text(
                                    text = step.instruction,
                                    color = Blanco,
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = step.distance,
                                    color = Blanco,
                                    fontFamily = Nunito,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Siguiente paso
                    nextStep?.let { step ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Blanco.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = getManeuverIcon(step.maneuver)),
                                contentDescription = step.maneuver,
                                tint = Blanco.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = "Luego: ${step.instruction}",
                                color = Blanco.copy(alpha = 0.8f),
                                fontFamily = Nunito,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Botones de control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón de volumen/silencio
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Blanco,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_lock_silent_mode_off),
                            contentDescription = "Volumen",
                            tint = Azul4
                        )
                    }
                }

                // Botón de lista de pasos
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Blanco,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                            contentDescription = "Lista de pasos",
                            tint = Azul4
                        )
                    }
                }
            }
        }
    }
}

suspend fun getNavigationRoute(
    origin: LatLng,
    destination: LatLng,
    mode: String
): Pair<List<LatLng>, List<NavigationStep>> {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=$mode" +
                    "&key=$apiKey"

            val response = URL(url).readText()
            val json = JSONObject(response)

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext Pair(emptyList(), emptyList())

            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            val leg = legs.getJSONObject(0)

            // Extraer polyline
            val polylineStr = route.getJSONObject("overview_polyline").getString("points")
            val points = PolyUtil.decode(polylineStr)

            // Extraer pasos
            val steps = leg.getJSONArray("steps")
            val navigationSteps = mutableListOf<NavigationStep>()

            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                val instruction = step.getString("html_instructions")
                    .replace("<[^>]*>".toRegex(), "") // Quitar HTML
                val distance = step.getJSONObject("distance").getString("text")
                val duration = step.getJSONObject("duration").getString("text")
                val maneuver = step.optString("maneuver", "")

                navigationSteps.add(
                    NavigationStep(
                        instruction = instruction,
                        distance = distance,
                        duration = duration,
                        maneuver = maneuver
                    )
                )
            }

            Pair(points, navigationSteps)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), emptyList())
        }
    }
}

fun calculateDistance(from: LatLng, to: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        from.latitude, from.longitude,
        to.latitude, to.longitude,
        results
    )
    return results[0]
}

fun formatDistance(distance: Float): String {
    return when {
        distance < 1000 -> "${distance.toInt()} m"
        else -> String.format("%.1f km", distance / 1000)
    }
}

fun getManeuverIcon(maneuver: String?): Int {
    return when (maneuver) {
        "turn-left" -> android.R.drawable.ic_media_rew
        "turn-right" -> android.R.drawable.ic_media_ff
        "turn-sharp-left" -> android.R.drawable.ic_media_rew
        "turn-sharp-right" -> android.R.drawable.ic_media_ff
        "uturn-left", "uturn-right" -> android.R.drawable.ic_menu_revert
        "straight" -> android.R.drawable.ic_menu_upload
        "ramp-left", "ramp-right" -> android.R.drawable.ic_menu_directions
        "fork-left", "fork-right" -> android.R.drawable.ic_menu_directions
        "roundabout-left", "roundabout-right" -> android.R.drawable.ic_menu_rotate
        else -> android.R.drawable.ic_menu_upload
    }
}