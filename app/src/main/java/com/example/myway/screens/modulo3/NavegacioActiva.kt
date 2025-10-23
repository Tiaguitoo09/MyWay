package com.example.myway.screens.modulo3

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.google.android.gms.maps.CameraUpdateFactory
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

    // IMPORTANTE: No usar ubicación por defecto, esperar la ubicación real
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var currentStep by remember { mutableStateOf<NavigationStep?>(null) }
    var nextStep by remember { mutableStateOf<NavigationStep?>(null) }
    var distanceToDestination by remember { mutableStateOf("Obteniendo ubicación...") }
    var timeToDestination by remember { mutableStateOf("") }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var allSteps by remember { mutableStateOf<List<NavigationStep>>(emptyList()) }

    var lastUpdateTime by remember { mutableLongStateOf(0L) }
    var lastKnownLocation by remember { mutableStateOf<LatLng?>(null) }
    var followUserLocation by remember { mutableStateOf(true) }
    var isInitialLocationSet by remember { mutableStateOf(false) }

    // Cámara con posición inicial temporal (se actualizará con ubicación real)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.7110, -74.0721), 15f)
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
                    val currentTime = System.currentTimeMillis()

                    // Filtro de precisión
                    if (location.accuracy > 50f) {
                        return
                    }

                    // Filtro de tiempo (no actualizar muy frecuentemente)
                    if (currentTime - lastUpdateTime < 2000 && isInitialLocationSet) {
                        return
                    }

                    val newLocation = LatLng(location.latitude, location.longitude)

                    // Filtro de velocidad (evitar saltos)
                    lastKnownLocation?.let { lastLoc ->
                        val distance = calculateDistance(lastLoc, newLocation)
                        val timeDiff = (currentTime - lastUpdateTime) / 1000f
                        if (timeDiff > 0) {
                            val speed = distance / timeDiff
                            if (speed > 50f && isInitialLocationSet) {
                                return
                            }
                        }
                    }

                    lastKnownLocation = newLocation
                    lastUpdateTime = currentTime
                    currentLocation = newLocation

                    // Primera vez: centrar inmediatamente
                    if (!isInitialLocationSet) {
                        isInitialLocationSet = true
                        scope.launch {
                            try {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                            .target(newLocation)
                                            .zoom(17f)
                                            .bearing(location.bearing)
                                            .tilt(45f)
                                            .build()
                                    )
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else if (followUserLocation) {
                        // Actualizaciones subsecuentes: animar
                        scope.launch {
                            try {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                            .target(newLocation)
                                            .zoom(17f)
                                            .bearing(location.bearing)
                                            .tilt(45f)
                                            .build()
                                    ),
                                    durationMs = 1500
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Actualizar distancia al destino
                    destinationLocation?.let { dest ->
                        val dist = calculateDistance(newLocation, dest)
                        distanceToDestination = formatDistance(dist)
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            // Obtener ubicación inmediatamente después de otorgar permiso
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val newLoc = LatLng(it.latitude, it.longitude)
                        currentLocation = newLoc
                        isInitialLocationSet = true
                        scope.launch {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(newLoc, 17f)
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // Detectar cuando el usuario mueve el mapa manualmente
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            followUserLocation = false
        }
    }

    // Obtener ubicación inicial INMEDIATAMENTE
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            // Primero: intentar obtener última ubicación conocida
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val newLoc = LatLng(it.latitude, it.longitude)
                        currentLocation = newLoc
                        lastKnownLocation = newLoc
                        isInitialLocationSet = true

                        scope.launch {
                            cameraPositionState.move(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(newLoc)
                                        .zoom(17f)
                                        .bearing(it.bearing)
                                        .tilt(45f)
                                        .build()
                                )
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            // Segundo: configurar actualizaciones continuas
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000L
            ).apply {
                setMinUpdateIntervalMillis(2000L)
                setMaxUpdateDelayMillis(5000L)
                setWaitForAccurateLocation(true)
                setMinUpdateDistanceMeters(3f)
            }.build()

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

    // Obtener destino y calcular ruta SOLO cuando tengamos ubicación actual
    LaunchedEffect(placeId, currentLocation) {
        if (placeId != null && currentLocation != null) {
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val dest = response.place.latLng
                    if (dest != null) {
                        destinationLocation = dest

                        scope.launch {
                            val mode = when (transportMode) {
                                "walking" -> "walking"
                                "driving" -> "driving"
                                "motorcycle" -> "driving"
                                else -> "driving"
                            }

                            // Usar la ubicación actual real, NO una por defecto
                            val routeData = getNavigationRoute(currentLocation!!, dest, mode)
                            routePoints = routeData.first
                            allSteps = routeData.second

                            if (allSteps.isNotEmpty()) {
                                currentStep = allSteps[0]
                                if (allSteps.size > 1) {
                                    nextStep = allSteps[1]
                                }
                            }

                            // Calcular distancia inicial
                            val dist = calculateDistance(currentLocation!!, dest)
                            distanceToDestination = formatDistance(dist)
                        }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    distanceToDestination = "Error al obtener destino"
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Solo mostrar mapa cuando tengamos ubicación
        if (currentLocation != null) {
            EnhancedNavigationMap(
                currentLocation = currentLocation!!,
                destinationLocation = destinationLocation,
                routePoints = routePoints,
                placeName = placeName,
                cameraPositionState = cameraPositionState
            )
        } else {
            // Pantalla de carga mientras se obtiene ubicación
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Azul4)
                    Text(
                        text = "Obteniendo tu ubicación...",
                        color = Azul4,
                        fontFamily = Nunito,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Panel superior COMPACTO
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Azul4,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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
                                .size(28.dp)
                                .clickable {
                                    fusedLocationClient.removeLocationUpdates(locationCallback)
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = distanceToDestination,
                                color = Blanco,
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            if (timeToDestination.isNotEmpty()) {
                                Text(
                                    text = timeToDestination,
                                    color = Blanco,
                                    fontFamily = Nunito,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Instrucción actual
                    currentStep?.let { step ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = getManeuverIcon(step.maneuver)),
                                contentDescription = step.maneuver,
                                tint = Blanco,
                                modifier = Modifier.size(32.dp)
                            )

                            Column {
                                Text(
                                    text = step.instruction,
                                    color = Blanco,
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 2
                                )
                                Text(
                                    text = step.distance,
                                    color = Blanco,
                                    fontFamily = Nunito,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Siguiente paso
                    nextStep?.let { step ->
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(
                            color = Blanco.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = getManeuverIcon(step.maneuver)),
                                contentDescription = step.maneuver,
                                tint = Blanco.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )

                            Text(
                                text = "Luego: ${step.instruction}",
                                color = Blanco.copy(alpha = 0.7f),
                                fontFamily = Nunito,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Botón para centrar ubicación
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            followUserLocation = true
                            currentLocation?.let { loc ->
                                scope.launch {
                                    try {
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newCameraPosition(
                                                CameraPosition.Builder()
                                                    .target(loc)
                                                    .zoom(17f)
                                                    .bearing(0f)
                                                    .tilt(45f)
                                                    .build()
                                            ),
                                            durationMs = 1000
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                    shape = CircleShape,
                    color = Blanco,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                            contentDescription = "Centrar en mi ubicación",
                            tint = if (followUserLocation) Azul4 else Azul4.copy(alpha = 0.6f),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedNavigationMap(
    currentLocation: LatLng,
    destinationLocation: LatLng?,
    routePoints: List<LatLng>,
    placeName: String?,
    cameraPositionState: CameraPositionState
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    ) {
        if (routePoints.isNotEmpty()) {
            Polyline(
                points = routePoints,
                color = androidx.compose.ui.graphics.Color.White,
                width = 18f,
                geodesic = true,
                zIndex = -1f
            )

            Polyline(
                points = routePoints,
                color = androidx.compose.ui.graphics.Color(0xFF4285F4),
                width = 14f,
                geodesic = true
            )
        }

        Circle(
            center = currentLocation,
            radius = 25.0,
            fillColor = androidx.compose.ui.graphics.Color(0x304285F4),
            strokeColor = androidx.compose.ui.graphics.Color(0x804285F4),
            strokeWidth = 3f
        )

        Marker(
            state = MarkerState(position = currentLocation),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            anchor = Offset(0.5f, 0.5f),
            flat = true
        )

        destinationLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = placeName ?: "Destino",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
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
                    "&language=es" +
                    "&key=$apiKey"

            val response = URL(url).readText()
            val json = JSONObject(response)

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext Pair(emptyList(), emptyList())

            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            val leg = legs.getJSONObject(0)

            val polylineStr = route.getJSONObject("overview_polyline").getString("points")
            val points = PolyUtil.decode(polylineStr)

            val steps = leg.getJSONArray("steps")
            val navigationSteps = mutableListOf<NavigationStep>()

            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                var instruction = step.getString("html_instructions")
                    .replace("<[^>]*>".toRegex(), "")
                    .replace("&nbsp;", " ")
                    .trim()

                instruction = translateInstruction(instruction)

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

fun translateInstruction(instruction: String): String {
    return instruction
        .replace("Turn left", "Gira a la izquierda")
        .replace("Turn right", "Gira a la derecha")
        .replace("turn left", "gira a la izquierda")
        .replace("turn right", "gira a la derecha")
        .replace("Keep left", "Mantente a la izquierda")
        .replace("Keep right", "Mantente a la derecha")
        .replace("Continue", "Continúa")
        .replace("Head", "Dirígete")
        .replace("toward", "hacia")
        .replace("onto", "en")
        .replace("Merge", "Incorpórate")
        .replace("Take the ramp", "Toma la rampa")
        .replace("Exit", "Sal")
        .replace("roundabout", "rotonda")
        .replace("at the", "en la")
        .replace("slight left", "ligeramente a la izquierda")
        .replace("slight right", "ligeramente a la derecha")
        .replace("sharp left", "cerrada a la izquierda")
        .replace("sharp right", "cerrada a la derecha")
        .replace("U-turn", "vuelta en U")
        .replace("destination", "destino")
        .replace("on the left", "a la izquierda")
        .replace("on the right", "a la derecha")
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