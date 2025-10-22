package com.example.myway.screens.modulo3

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
import java.io.File
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

    // Estados
    var placeDetails by remember { mutableStateOf<PlaceDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var photoPath by remember { mutableStateOf<String?>(null) }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    // Cargar detalles del lugar
    LaunchedEffect(placeId) {
        placeId?.let { id ->
            // Verificar si hay foto guardada localmente
            val filename = "place_${id}.jpg"
            val file = File(context.filesDir, filename)
            if (file.exists()) {
                photoPath = file.absolutePath
            }

            // Obtener detalles del lugar
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
                    Place.Field.TYPES
                )

                val request = FetchPlaceRequest.newInstance(id, placeFields)
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        val place = response.place

                        // Obtener horarios si están disponibles
                        val hours = place.openingHours?.weekdayText ?: emptyList()

                        placeDetails = PlaceDetails(
                            name = place.name ?: placeName ?: "",
                            address = place.address ?: "No disponible",
                            phone = place.phoneNumber ?: "No disponible",
                            website = place.websiteUri?.toString() ?: "No disponible",
                            rating = place.rating ?: 0.0,
                            totalRatings = place.userRatingsTotal ?: 0,
                            isOpen = place.isOpen,
                            types = place.types?.map { it.name } ?: emptyList(),
                            openingHours = hours
                        )
                        isLoading = false
                    }
                    .addOnFailureListener {
                        isLoading = false
                    }
            } catch (e: Exception) {
                e.printStackTrace()
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
            // Encabezado simple sin caja azul
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
                    text = "Detalles del Lugar",
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
                        if (photoPath != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = File(photoPath!!),
                                    contentDescription = placeDetails?.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre del lugar
                    Text(
                        text = placeDetails?.name ?: placeName ?: "Lugar",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Blanco
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating con corazones (estilo 5 de 5)
                    if (placeDetails?.rating != null && placeDetails!!.rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mostrar corazones basados en rating (de 5)
                            val rating = placeDetails!!.rating
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

                            Text(
                                text = "${"%.1f".format(rating)} (${placeDetails!!.totalRatings} reseñas)",
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                color = Blanco.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección: Descripción
                    Text(
                        text = "Descripción",
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
                                label = "Dirección",
                                value = placeDetails?.address ?: "No disponible"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            DetailRow(
                                icon = Icons.Default.Phone,
                                label = "Teléfono",
                                value = placeDetails?.phone ?: "No disponible"
                            )

                            // Solo mostrar horario/estado si hay información disponible
                            if (placeDetails?.isOpen != null || placeDetails?.openingHours?.isNotEmpty() == true) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                DetailRow(
                                    icon = Icons.Default.DateRange,
                                    label = "Horario",
                                    value = getScheduleText(
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
                        text = "Marcar Ruta",
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

/**
 * Función para obtener el texto del horario de forma inteligente
 */
fun getScheduleText(isOpen: Boolean?, openingHours: List<String>): String {
    return when {
        // Si hay horarios completos, mostrar el de hoy + estado
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
                ?: "No disponible"

            // Traducir el horario al español
            val translatedHours = translateSchedule(todayHours)

            val status = when(isOpen) {
                true -> "🟢 Abierto"
                false -> "🔴 Cerrado"
                null -> ""
            }

            if (status.isNotEmpty()) "$status\n$translatedHours"
            else translatedHours
        }
        // Si solo hay estado abierto/cerrado
        isOpen == true -> "🟢 Abierto ahora"
        isOpen == false -> "🔴 Cerrado ahora"
        // Si no hay nada
        else -> "No disponible"
    }
}

/**
 * Traduce los horarios de inglés a español
 */
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