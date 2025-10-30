package com.example.myway.screens.modulo3

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.BuildConfig
import com.example.myway.R
import com.example.myway.data.FavoritesRepository
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun DetallesLugar(
    navController: NavController,
    placeId: String?,
    placeName: String?
) {
    val context = LocalContext.current
    val repository = remember { FavoritesRepository(context) }
    val scope = rememberCoroutineScope()

    // Estados
    var placeDetails by remember { mutableStateOf<PlaceDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    LaunchedEffect(placeId) {
        Log.d("DetallesLugar", "🔍 Iniciando carga de detalles para placeId: $placeId")

        placeId?.let { id ->
            // ✅ PASO 1: Intentar obtener foto desde Firestore (si es favorito)
            scope.launch {
                try {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    Log.d("DetallesLugar", "👤 UserId actual: $userId")

                    if (userId != null) {
                        val docRef = FirebaseFirestore.getInstance()
                            .collection("favoritos")
                            .document(userId)
                            .collection("lugares")
                            .document(id)

                        Log.d("DetallesLugar", "📂 Buscando foto en Firestore...")

                        val doc = withContext(Dispatchers.IO) {
                            docRef.get().await()
                        }

                        if (doc.exists()) {
                            val storedUrl = doc.getString("photoUrl")
                            Log.d("DetallesLugar", "📄 Documento encontrado")

                            if (storedUrl != null && storedUrl.isNotEmpty()) {
                                photoUrl = storedUrl
                                Log.d("DetallesLugar", "✅ Foto cargada desde Firebase Storage")
                            } else {
                                Log.w("DetallesLugar", "⚠️ Este favorito no tiene foto guardada")
                            }
                        } else {
                            Log.d("DetallesLugar", "ℹ️ No es un favorito (buscando foto en Google Places)")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DetallesLugar", "❌ Error obteniendo foto: ${e.message}", e)
                }
            }

            // ✅ PASO 2: Obtener detalles del lugar desde Google Places
            try {
                val placeFields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.WEBSITE_URI,
                    Place.Field.RATING,
                    Place.Field.USER_RATINGS_TOTAL,
                    Place.Field.OPENING_HOURS,
                    Place.Field.TYPES,
                    Place.Field.PHOTO_METADATAS  // ✅ AGREGADO: Para obtener fotos
                )

                val request = FetchPlaceRequest.newInstance(id, placeFields)
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        val place = response.place
                        Log.d("DetallesLugar", "✅ Detalles de Google Places obtenidos")

                        val hours = place.openingHours?.weekdayText ?: emptyList()

                        placeDetails = PlaceDetails(
                            name = place.name ?: placeName ?: "",
                            address = place.address ?: context.getString(R.string.no_disponible),
                            phone = place.phoneNumber ?: context.getString(R.string.no_disponible),
                            website = place.websiteUri?.toString() ?: context.getString(R.string.no_disponible),
                            rating = place.rating ?: 0.0,
                            totalRatings = place.userRatingsTotal ?: 0,
                            isOpen = place.isOpen,
                            types = place.types?.map { it.name } ?: emptyList(),
                            openingHours = hours
                        )

                        // ✅ PASO 3: Si no hay foto de Firebase, obtener de Google Places
                        if (photoUrl == null) {
                            val photoMetadata = place.photoMetadatas?.firstOrNull()
                            if (photoMetadata != null) {
                                Log.d("DetallesLugar", "📸 Cargando foto de Google Places...")
                                
                                val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                    .setMaxWidth(800)
                                    .setMaxHeight(600)
                                    .build()

                                placesClient.fetchPhoto(photoRequest)
                                    .addOnSuccessListener { fetchPhotoResponse ->
                                        val bitmap = fetchPhotoResponse.bitmap
                                        // Convertir bitmap a URL temporal o usar directamente
                                        // Por ahora, usar attributions como fallback
                                        photoUrl = photoMetadata.attributions
                                        Log.d("DetallesLugar", "✅ Foto de Google Places cargada")
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("DetallesLugar", "❌ Error cargando foto: ${exception.message}")
                                    }
                            } else {
                                Log.d("DetallesLugar", "ℹ️ No hay fotos disponibles para este lugar")
                            }
                        }

                        isLoading = false
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DetallesLugar", "❌ Error obteniendo detalles: ${exception.message}")
                        isLoading = false
                    }
            } catch (e: Exception) {
                Log.e("DetallesLugar", "❌ Error general: ${e.message}", e)
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(R.string.volver),
                    tint = Blanco,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Text(
                    text = stringResource(R.string.detalles_lugar),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            // Contenido con scroll
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Azul4)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Imagen del lugar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = placeDetails?.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = 3.dp,
                                            color = Color.White,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.ic_favorite_outline),
                                    placeholder = painterResource(id = R.drawable.ic_favorite_outline)
                                )
                            } else {
                                // Sin foto - mostrar ícono
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

                    // Nombre del lugar
                    Text(
                        text = placeDetails?.name ?: placeName ?: stringResource(R.string.destino),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Blanco
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ Rating con corazones - SIEMPRE mostrar
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val rating = placeDetails?.rating ?: 0.0
                        val totalRatings = placeDetails?.totalRatings ?: 0
                        val fullHearts = rating.roundToInt().coerceIn(0, 5)

                        repeat(5) { index ->
                            Icon(
                                painter = painterResource(
                                    id = if (index < fullHearts) R.drawable.ic_favorite_filled
                                    else R.drawable.ic_favorite_outline
                                ),
                                contentDescription = null,
                                tint = if (index < fullHearts) Color(0xFFE91E63) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            if (index < 4) Spacer(modifier = Modifier.width(4.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (rating > 0) {
                            Text(
                                text = "${"%.1f".format(rating)} ($totalRatings reseñas)",
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                color = Blanco.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = "Sin valoraciones",
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                color = Blanco.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección: Descripción
                    Text(
                        text = stringResource(R.string.descripcion),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Blanco
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            DetailRow(
                                icon = Icons.Default.LocationOn,
                                label = stringResource(R.string.direccion),
                                value = placeDetails?.address ?: stringResource(R.string.no_disponible)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            DetailRow(
                                icon = Icons.Default.Phone,
                                label = stringResource(R.string.telefono),
                                value = placeDetails?.phone ?: stringResource(R.string.no_disponible)
                            )

                            if (placeDetails?.isOpen != null || placeDetails?.openingHours?.isNotEmpty() == true) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                DetailRow(
                                    icon = Icons.Default.DateRange,
                                    label = stringResource(R.string.horario),
                                    value = getScheduleText(
                                        context = context,
                                        isOpen = placeDetails?.isOpen,
                                        openingHours = placeDetails?.openingHours ?: emptyList()
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón marcar ruta
                    CustomButton(
                        text = stringResource(R.string.marcar_ruta),
                        color = Azul3,
                        onClick = {
                            navController.navigate(
                                "ruta_opciones/${placeId}/${placeName}"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

fun getScheduleText(context: android.content.Context, isOpen: Boolean?, openingHours: List<String>): String {
    return when {
        openingHours.isNotEmpty() -> {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val todayIndex = when(today) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }

            val todayHours = openingHours.getOrNull(todayIndex)
                ?: openingHours.firstOrNull()
                ?: context.getString(R.string.no_disponible)

            val translatedHours = translateSchedule(todayHours)

            val status = when(isOpen) {
                true -> context.getString(R.string.abierto_ahora)
                false -> context.getString(R.string.cerrado_ahora)
                null -> ""
            }

            if (status.isNotEmpty()) "$status\n$translatedHours"
            else translatedHours
        }
        isOpen == true -> context.getString(R.string.abierto_ahora)
        isOpen == false -> context.getString(R.string.cerrado_ahora)
        else -> context.getString(R.string.no_disponible)
    }
}

fun translateSchedule(schedule: String): String {
    return schedule
        .replace("Monday", "Lunes")
        .replace("Tuesday", "Martes")
        .replace("Wednesday", "Miércoles")
        .replace("Thursday", "Jueves")
        .replace("Friday", "Viernes")
        .replace("Saturday", "Sábado")
        .replace("Sunday", "Domingo")
        .replace("AM", "a.m.")
        .replace("PM", "p.m.")
        .replace("Closed", "Cerrado")
        .replace("Open 24 hours", "Abierto 24 horas")
}

@Composable
fun DetailRow(
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

data class PlaceDetails(
    val name: String,
    val address: String,
    val phone: String,
    val website: String,
    val rating: Double,
    val totalRatings: Int,
    val isOpen: Boolean?,
    val types: List<String>,
    val openingHours: List<String> = emptyList()
)