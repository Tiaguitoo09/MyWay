package com.example.myway.screens.modulo4

import android.Manifest
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TuMood(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AIRepository(context) }
    val scrollState = rememberScrollState()

    // Estados
    var currentStep by remember { mutableStateOf(0) } // 0=mood, 1=tipo, 2=presupuesto, 3=duración, 4=resultado
    var selectedMood by remember { mutableStateOf("") }
    var selectedPlanType by remember { mutableStateOf("") }
    var selectedBudget by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var recommendation by remember { mutableStateOf<AIRecommendation?>(null) }
    var currentLocation by remember { mutableStateOf<UserLocation?>(null) }
    var placeDetails by remember { mutableStateOf<RecommendedPlaceDetails?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    // Obtener ubicación
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = UserLocation(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("TuMood", "Error de permisos: ${e.message}")
            }
        }
    }

    // Obtener detalles del lugar recomendado


    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
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
                        .clickable {
                            if (currentStep > 0 && recommendation == null) {
                                currentStep--
                            } else {
                                navController.popBackStack()
                            }
                        }
                )

                Text(
                    text = stringResource(R.string.mood),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Paso 1: Seleccionar Mood
                if (currentStep == 0) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¿Cómo te sientes hoy?",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MoodOption("😊 Feliz", "feliz", selectedMood) { selectedMood = it }
                    MoodOption("😢 Necesito animarme", "triste", selectedMood) { selectedMood = it }
                    MoodOption("🔥 Aventurero", "aventurero", selectedMood) { selectedMood = it }
                    MoodOption("😌 Relajado", "relajado", selectedMood) { selectedMood = it }
                    MoodOption("💕 Romántico", "romantico", selectedMood) { selectedMood = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (selectedMood.isNotEmpty()) {
                        CustomButton(
                            text = "Continuar",
                            color = Blanco,
                            textColor = Negro,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { currentStep = 1 }
                        )
                    }
                }

                // Paso 2: Tipo de Plan
                if (currentStep == 1) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¿Con quién vas?",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MoodOption("🧍 Solo", "solo", selectedPlanType) { selectedPlanType = it }
                    MoodOption("💑 En pareja", "pareja", selectedPlanType) { selectedPlanType = it }
                    MoodOption("👥 Con amigos", "amigos", selectedPlanType) { selectedPlanType = it }
                    MoodOption("👨‍👩‍👧‍👦 En familia", "familia", selectedPlanType) { selectedPlanType = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (selectedPlanType.isNotEmpty()) {
                        CustomButton(
                            text = "Continuar",
                            color = Blanco,
                            textColor = Negro,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { currentStep = 2 }
                        )
                    }
                }

                // Paso 3: Presupuesto
                if (currentStep == 2) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¿Cuál es tu presupuesto?",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MoodOption("💵 Económico", "economico", selectedBudget) { selectedBudget = it }
                    MoodOption("💰 Moderado", "moderado", selectedBudget) { selectedBudget = it }
                    MoodOption("💎 Sin límite", "alto", selectedBudget) { selectedBudget = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (selectedBudget.isNotEmpty()) {
                        CustomButton(
                            text = "Continuar",
                            color = Blanco,
                            textColor = Negro,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { currentStep = 3 }
                        )
                    }
                }

                // Paso 4: Duración
                if (currentStep == 3) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¿Cuánto tiempo tienes?",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MoodOption("⏱️ Poco tiempo (< 2h)", "corto", selectedDuration) { selectedDuration = it }
                    MoodOption("🕐 Tiempo medio (2-4h)", "medio", selectedDuration) { selectedDuration = it }
                    MoodOption("🕓 Todo el día", "largo", selectedDuration) { selectedDuration = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (selectedDuration.isNotEmpty()) {
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
                                            val request = PersonalizedRecommendationRequest(
                                                userLocation = location,
                                                mood = selectedMood,
                                                planType = selectedPlanType,
                                                budget = selectedBudget,
                                                duration = selectedDuration,
                                                currentWeather = getCurrentWeather(),
                                                timeOfDay = getTimeOfDay(),
                                                userId = userId
                                            )

                                            val result = repository.getPersonalizedRecommendation(request)
                                            result.onSuccess {
                                                recommendation = it
                                                currentStep = 4
                                            }.onFailure {
                                                Log.e("TuMood", "Error: ${it.message}")
                                            }
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Mostrar cargando
                if (isLoading) {
                    Spacer(modifier = Modifier.height(64.dp))
                    CircularProgressIndicator(color = Blanco)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buscando el lugar perfecto para ti...",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Mostrar resultado
                if (currentStep == 4 && recommendation != null && !isLoading) {
                    val rec = recommendation!!

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "✨ Tu recomendación personalizada",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card con imagen
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = rec.place.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = 3.dp,
                                            color = Color.White,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
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

                    Spacer(modifier = Modifier.height(16.dp))

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

                    // Puntuación de compatibilidad
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Azul4.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Compatibilidad",
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Blanco
                                )
                                Text(
                                    text = "${rec.score.toInt()}%",
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Blanco
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = (rec.score / 100).toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Razón
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

                    // Detalles
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

                    // Botones
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
                        text = "Nueva Búsqueda",
                        color = Blanco,
                        textColor = Negro,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            currentStep = 0
                            selectedMood = ""
                            selectedPlanType = ""
                            selectedBudget = ""
                            selectedDuration = ""
                            recommendation = null
                            placeDetails = null
                            photoUrl = null
                        }
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun MoodOption(
    text: String,
    value: String,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onSelect(value) },
        colors = CardDefaults.cardColors(
            containerColor = if (selectedValue == value)
                Azul4
            else
                Blanco
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(if (selectedValue == value) 8.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                fontFamily = Nunito,
                fontWeight = if (selectedValue == value) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                color = if (selectedValue == value) Blanco else Negro
            )
        }
    }
}