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

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentBearing by remember { mutableFloatStateOf(0f) }
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

                    if (location.accuracy > 50f) {
                        return
                    }

                    if (currentTime - lastUpdateTime < 2000 && isInitialLocationSet) {
                        return
                    }

                    val newLocation = LatLng(location.latitude, location.longitude)

                    lastKnownLocation?.let { lastLoc ->
                        val distance = calculateDistance(lastLoc, newLocation)
                        val timeDiff = (currentTime - lastUpdateTime) / 1000f
                        if (timeDiff > 0) {
                            val speed = distance / timeDiff
                            if (speed > 50f && isInitialLocationSet) {
                                return
                            }
                        }

                        // Calcular bearing manualmente si el GPS no lo proporciona
                        if (distance > 2f) { // Solo actualizar si nos movimos al menos 2 metros
                            val calculatedBearing = calculateBearing(lastLoc, newLocation)
                            currentBearing = calculatedBearing
                        }
                    } ?: run {
                        // Primera vez, usar bearing del GPS si está disponible
                        if (location.bearing != 0f) {
                            currentBearing = location.bearing
                        }
                    }
                    lastKnownLocation = newLocation
                    lastUpdateTime = currentTime
                    currentLocation = newLocation

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
                        scope.launch {
                            try {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                            .target(newLocation)
                                            .zoom(17f)
                                            .bearing(currentBearing) // Usar bearing calculado
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

                    destinationLocation?.let { dest ->
                        val dist = calculateDistance(newLocation, dest)
                        distanceToDestination = formatDistance(dist)
                    }

                    if (allSteps.isNotEmpty() && routePoints.isNotEmpty()) {
                        updateNavigationStep(
                            newLocation,
                            routePoints,
                            allSteps,
                            currentStepIndex
                        ) { newIndex ->
                            if (newIndex != currentStepIndex && newIndex < allSteps.size) {
                                currentStepIndex = newIndex
                                currentStep = allSteps[newIndex]
                                nextStep = if (newIndex + 1 < allSteps.size) {
                                    allSteps[newIndex + 1]
                                } else null
                            }
                        }
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
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val newLoc = LatLng(it.latitude, it.longitude)
                        currentLocation = newLoc
                        currentBearing = it.bearing
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

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            followUserLocation = false
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val newLoc = LatLng(it.latitude, it.longitude)
                        currentLocation = newLoc
                        currentBearing = it.bearing
                        lastKnownLocation = newLoc
                        isInitialLocationSet = true

                        scope.launch {
                            cameraPositionState.move(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(newLoc)
                                        .zoom(17f)
                                        .bearing(currentBearing) // Usar bearing calculado
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

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000L // Actualizar cada 2 segundos
            ).apply {
                setMinUpdateIntervalMillis(1000L) // Mínimo 1 segundo
                setMaxUpdateDelayMillis(3000L)
                setWaitForAccurateLocation(true)
                setMinUpdateDistanceMeters(1f) // Actualizar cada 1 metro
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

                            val routeData = getNavigationRoute(currentLocation!!, dest, mode)
                            routePoints = routeData.first
                            allSteps = routeData.second

                            if (allSteps.isNotEmpty()) {
                                currentStep = allSteps[0]
                                if (allSteps.size > 1) {
                                    nextStep = allSteps[1]
                                }
                            }

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
        if (currentLocation != null) {
            EnhancedNavigationMap(
                currentLocation = currentLocation!!,
                destinationLocation = destinationLocation,
                routePoints = routePoints,
                placeName = placeName,
                cameraPositionState = cameraPositionState,
                currentBearing = currentBearing
            )
        } else {
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
                                                    .bearing(currentBearing)
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
    cameraPositionState: CameraPositionState,
    currentBearing: Float
) {
    val currentMarkerState = rememberMarkerState(position = currentLocation)

    LaunchedEffect(currentLocation) {
        currentMarkerState.position = currentLocation
    }

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
            state = currentMarkerState,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            anchor = Offset(0.5f, 0.5f),
            flat = true,
            rotation = currentBearing
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

fun updateNavigationStep(
    currentLocation: LatLng,
    routePoints: List<LatLng>,
    steps: List<NavigationStep>,
    currentIndex: Int,
    onStepChanged: (Int) -> Unit
) {
    var minDistance = Float.MAX_VALUE
    var closestIndex = 0

    routePoints.forEachIndexed { index, point ->
        val distance = calculateDistance(currentLocation, point)
        if (distance < minDistance) {
            minDistance = distance
            closestIndex = index
        }
    }

    val progressPercentage = closestIndex.toFloat() / routePoints.size
    val stepSize = 1f / steps.size
    val estimatedStepIndex = (progressPercentage / stepSize).toInt().coerceIn(0, steps.size - 1)

    if (estimatedStepIndex > currentIndex) {
        onStepChanged(estimatedStepIndex)
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

fun calculateBearing(from: LatLng, to: LatLng): Float {
    val results = FloatArray(2)
    Location.distanceBetween(
        from.latitude, from.longitude,
        to.latitude, to.longitude,
        results
    )
    // results[1] contiene el bearing inicial
    return if (results.size > 1) results[1] else 0f
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