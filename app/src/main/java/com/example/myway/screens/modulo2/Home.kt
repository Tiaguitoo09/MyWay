package com.example.myway.screens.modulo2

import android.Manifest
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
import androidx.compose.ui.layout.ContentScale
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
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.Azul4
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Verde
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

@Composable
fun Home(
    navController: NavController,
    placeId: String? = null,
    placeName: String? = null
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

    val defaultLocation = LatLng(4.7110, -74.0721)
    var currentLocation by remember { mutableStateOf(defaultLocation) }
    var destinationLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var destinationName by remember { mutableStateOf<String?>(null) }

    // Variable para saber si hay destino
    val hasDestination = placeId != null && placeId != "null"

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 12f)
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
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // Obtener ubicación actual
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // Obtener el lugar del destino y calcular ruta (SOLO si placeId no es null)
    LaunchedEffect(placeId) {
        if (placeId != null && placeId != "null") {
            val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    place.latLng?.let { latLng ->
                        destinationLocation = latLng
                        destinationName = place.name ?: placeName

                        scope.launch {
                            try {
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
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        } else {
            // Limpiar destino si no hay placeId
            destinationLocation = null
            routePoints = emptyList()
            destinationName = null
        }
    }

    BackHandler(enabled = true) {}

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = currentLocation),
                title = stringResource(id = R.string.tu_ubicacion),
                snippet = stringResource(id = R.string.estas_aqui)
            )

            destinationLocation?.let { destination ->
                Marker(
                    state = MarkerState(position = destination),
                    title = destinationName ?: stringResource(id = R.string.destino),
                    snippet = "Toca para ver opciones de ruta",
                    onClick = {
                        navController.navigate("ruta_opciones/${placeId}/${placeName}")
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

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado superior
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                color = Azul4,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.icono_perfil),
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

            // Botón para centrar en ubicación (ARRIBA del botón principal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp, bottom = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            if (hasLocationPermission) {
                                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                try {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            currentLocation = LatLng(it.latitude, it.longitude)
                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    e.printStackTrace()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                            contentDescription = stringResource(id = R.string.mi_ubicacion),
                            tint = Azul4,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            // Botón principal - Cambia según si hay destino o no
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasDestination) {
                    // Botón "IR" verde cuando hay destino
                    CustomButton(
                        text = "IR",
                        color = Verde,
                        onClick = {
                            navController.navigate("ruta_opciones/${placeId}/${placeName}")
                        },
                        modifier = Modifier
                            .width(330.dp)
                            .height(55.dp)
                            .clip(RoundedCornerShape(15.dp))
                    )
                } else {
                    // Botón "¿A dónde vas?" azul cuando NO hay destino
                    CustomButton(
                        text = stringResource(R.string.a_donde_vas),
                        color = Azul4,
                        onClick = {
                            navController.navigate("planea_viaje")
                        },
                        modifier = Modifier
                            .width(330.dp)
                            .height(55.dp)
                            .clip(RoundedCornerShape(15.dp))
                    )
                }
            }
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

            val polylinePattern = """"points"\s*:\s*"([^"]+)"""".toRegex()
            val match = polylinePattern.find(response)

            match?.groupValues?.get(1)?.let { encodedPolyline ->
                PolyUtil.decode(encodedPolyline)
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}