package com.example.myway.screens.modulo2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myway.BuildConfig
import com.example.myway.R
import com.example.myway.components.LocationPermissionBanner
import com.example.myway.components.SafetyWarningOverlay
import com.example.myway.data.repository.RecentPlacesRepository
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*
import com.example.myway.utils.DrivingDetector
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

data class NearbyPlace(
    val placeId: String,
    val name: String,
    val latLng: LatLng
)

@Composable
fun Home(
    navController: NavController,
    placeId: String? = null,
    placeName: String? = null,
    placeType: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recentPlacesRepository = remember { RecentPlacesRepository() }

    val sharedPreferences = remember {
        context.getSharedPreferences("MyWayPrefs", Context.MODE_PRIVATE)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isDriving by DrivingDetector.isDriving.collectAsState()
    var modoCopiloto by remember {
        mutableStateOf(sharedPreferences.getBoolean("modo_copiloto", false))
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        modoCopiloto = sharedPreferences.getBoolean("modo_copiloto", false)
    }

    val canUseApp by remember {
        derivedStateOf {
            !isDriving || modoCopiloto
        }
    }

    var showWarningOverlay by remember { mutableStateOf(false) }

    val defaultLocation = LatLng(4.7110, -74.0721)
    var currentLocation by remember { mutableStateOf(defaultLocation) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var destinationName by remember { mutableStateOf<String?>(null) }
    var nearbyPlaces by remember { mutableStateOf<List<NearbyPlace>>(emptyList()) }

    val hasDestination = placeId != null && placeId != "null"

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 13f)
    }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            updateCurrentLocation(context) { newLocation ->
                currentLocation = newLocation
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                DrivingDetector.updateLocation(
                    android.location.Location("").apply {
                        latitude = newLocation.latitude
                        longitude = newLocation.longitude
                    }
                )
            }
        } else {
            navController.navigate("permisos")
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        DrivingDetector.updateLocation(it)
                    }
                }
            }
            try {
                fusedLocationClient.requestLocationUpdates(request, callback, null)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            updateCurrentLocation(context) { newLocation ->
                currentLocation = newLocation
                cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 15f)
                DrivingDetector.updateLocation(
                    android.location.Location("").apply {
                        latitude = newLocation.latitude
                        longitude = newLocation.longitude
                    }
                )
            }
        }
    }

    LaunchedEffect(placeId, currentLocation) {
        if (hasDestination && destinationLocation != null) {
            scope.launch {
                val route = getDirections(currentLocation, destinationLocation!!)
                routePoints = route
                if (route.isNotEmpty()) {
                    cameraPositionState.position = CameraPosition.Builder()
                        .target(
                            LatLng(
                                (currentLocation.latitude + destinationLocation!!.latitude) / 2,
                                (currentLocation.longitude + destinationLocation!!.longitude) / 2
                            )
                        )
                        .zoom(12f)
                        .build()
                }
            }
        } else if (hasDestination) {
            val request = FetchPlaceRequest.newInstance(
                placeId!!,
                listOf(Place.Field.LAT_LNG, Place.Field.NAME)
            )
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    place.latLng?.let { latLng ->
                        destinationLocation = latLng
                        destinationName = place.name ?: placeName
                        scope.launch {
                            val route = getDirections(currentLocation, latLng)
                            routePoints = route
                            if (route.isNotEmpty()) {
                                cameraPositionState.position = CameraPosition.Builder()
                                    .target(
                                        LatLng(
                                            (currentLocation.latitude + latLng.latitude) / 2,
                                            (currentLocation.longitude + latLng.longitude) / 2
                                        )
                                    )
                                    .zoom(12f)
                                    .build()
                            }
                        }
                    }
                }
                .addOnFailureListener { it.printStackTrace() }
        } else {
            destinationLocation = null
            routePoints = emptyList()
            destinationName = null
        }
    }

    LaunchedEffect(placeType, currentLocation) {
        if (placeType != null && !hasDestination) {
            scope.launch {
                nearbyPlaces = fetchNearbyPlaces(placesClient, currentLocation, placeType)
            }
        } else {
            nearbyPlaces = emptyList()
        }
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    BackHandler(enabled = true) {}

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            Marker(
                state = MarkerState(position = currentLocation),
                title = stringResource(R.string.tu_ubicacion),
                snippet = stringResource(R.string.estas_aqui)
            )

            destinationLocation?.let { destination ->
                Marker(
                    state = MarkerState(position = destination),
                    title = destinationName ?: stringResource(R.string.destino),
                    snippet = "Toca para ver opciones de ruta",
                    onClick = {
                        modoCopiloto = sharedPreferences.getBoolean("modo_copiloto", false)

                        if (!isDriving || modoCopiloto) {
                            scope.launch {
                                recentPlacesRepository.saveRecentPlace(
                                    placeId = placeId ?: "",
                                    placeName = destinationName ?: placeName ?: "Destino",
                                    placeAddress = "",
                                    latitude = destination.latitude,
                                    longitude = destination.longitude
                                )
                            }
                            navController.navigate("ruta_opciones/${placeId}/${placeName}")
                        } else {
                            showWarningOverlay = true
                        }
                        true
                    }
                )
            }

            nearbyPlaces.forEach { place ->
                Marker(
                    state = MarkerState(position = place.latLng),
                    title = place.name,
                    onClick = {
                        modoCopiloto = sharedPreferences.getBoolean("modo_copiloto", false)

                        if (!isDriving || modoCopiloto) {
                            scope.launch {
                                recentPlacesRepository.saveRecentPlace(
                                    placeId = place.placeId,
                                    placeName = place.name,
                                    placeAddress = "",
                                    latitude = place.latLng.latitude,
                                    longitude = place.latLng.longitude
                                )
                            }
                            val encodedName = URLEncoder.encode(place.name, "UTF-8")
                            navController.navigate("ruta_opciones/${place.placeId}/${encodedName}")
                        } else {
                            showWarningOverlay = true
                        }
                        true
                    }
                )
            }

            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = androidx.compose.ui.graphics.Color(0xFF4285F4),
                    width = 10f
                )
            }
        }

        if (showWarningOverlay && isDriving && !modoCopiloto) {
            SafetyWarningOverlay(onDismiss = { showWarningOverlay = false })
        }

        if (!hasLocationPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            ) {
                LocationPermissionBanner(
                    onNavigateToPermissions = {
                        navController.navigate("permisos")
                    }
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(90.dp),
                color = Azul4,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                    Image(
                        painter = painterResource(R.drawable.icono_perfil),
                        contentDescription = stringResource(R.string.perfil),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 16.dp)
                            .size(50.dp)
                            .clickable {
                                navController.navigate("perfil_ajustes")
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth().padding(end = 24.dp, bottom = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    modifier = Modifier.size(56.dp).clickable {
                        if (hasLocationPermission) {
                            updateCurrentLocation(context) { newLoc ->
                                currentLocation = newLoc
                                cameraPositionState.position =
                                    CameraPosition.fromLatLngZoom(currentLocation, 15f)
                            }
                        } else {
                            navController.navigate("permisos")
                        }
                    },
                    shape = CircleShape,
                    color = if (hasLocationPermission) Blanco else Blanco.copy(alpha = 0.5f),
                    shadowElevation = 4.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_mylocation),
                            contentDescription = stringResource(R.string.mi_ubicacion),
                            tint = if (hasLocationPermission) Azul4 else Azul4.copy(alpha = 0.5f),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    hasDestination -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(55.dp).clickable {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                shape = RoundedCornerShape(15.dp),
                                color = Rojo,
                                shadowElevation = 4.dp
                            ) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                                        contentDescription = "Cancelar destino",
                                        tint = Blanco,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            CustomButton(
                                text = "IR",
                                color = Verde,
                                onClick = {
                                    modoCopiloto = sharedPreferences.getBoolean("modo_copiloto", false)

                                    if (!isDriving || modoCopiloto) {
                                        scope.launch {
                                            destinationLocation?.let { dest ->
                                                recentPlacesRepository.saveRecentPlace(
                                                    placeId = placeId ?: "",
                                                    placeName = destinationName ?: placeName ?: "Destino",
                                                    placeAddress = "",
                                                    latitude = dest.latitude,
                                                    longitude = dest.longitude
                                                )
                                            }
                                        }
                                        navController.navigate("ruta_opciones/${placeId}/${placeName}")
                                    } else {
                                        showWarningOverlay = true
                                    }
                                },
                                modifier = Modifier.width(260.dp).height(55.dp)
                                    .clip(RoundedCornerShape(15.dp))
                            )
                        }
                    }

                    placeType != null -> {
                        CustomButton(
                            text = "Volver al mapa principal",
                            color = Azul4,
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            modifier = Modifier.width(330.dp).height(55.dp)
                                .clip(RoundedCornerShape(15.dp))
                        )
                    }

                    else -> {
                        CustomButton(
                            text = stringResource(R.string.a_donde_vas),
                            color = if (!isDriving || modoCopiloto) Azul4 else Azul4.copy(alpha = 0.6f),
                            onClick = {
                                modoCopiloto = sharedPreferences.getBoolean("modo_copiloto", false)

                                if (!isDriving || modoCopiloto) {
                                    navController.navigate("planea_viaje")
                                } else {
                                    showWarningOverlay = true
                                }
                            },
                            modifier = Modifier.width(330.dp).height(55.dp)
                                .clip(RoundedCornerShape(15.dp))
                        )
                    }
                }
            }
        }
    }
}

private fun updateCurrentLocation(context: android.content.Context, onUpdate: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onUpdate(LatLng(location.latitude, location.longitude))
            } else {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    1000L
                ).build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let {
                            onUpdate(LatLng(it.latitude, it.longitude))
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }
                }

                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, callback, null)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

suspend fun fetchNearbyPlaces(
    placesClient: PlacesClient,
    location: LatLng,
    type: String
): List<NearbyPlace> {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=${location.latitude},${location.longitude}" +
                    "&radius=2000&type=$type&key=$apiKey"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")

            (0 until results.length()).mapNotNull { i ->
                val obj = results.getJSONObject(i)
                val placeId = obj.optString("place_id")
                val name = obj.optString("name")
                val geometry = obj.optJSONObject("geometry")
                val loc = geometry?.optJSONObject("location")
                val lat = loc?.optDouble("lat")
                val lng = loc?.optDouble("lng")

                if (placeId.isNotEmpty() && lat != null && lng != null) {
                    NearbyPlace(placeId, name, LatLng(lat, lng))
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun getDirections(origin: LatLng, destination: LatLng): List<LatLng> {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&key=$apiKey"

            val response = URL(url).readText()
            val match = """"points"\s*:\s*"([^"]+)"""".toRegex().find(response)
            match?.groupValues?.get(1)?.let { PolyUtil.decode(it) } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}