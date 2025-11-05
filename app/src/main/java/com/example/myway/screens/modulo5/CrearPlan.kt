package com.example.myway.screens.modulo5

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.myway.BuildConfig
import com.example.myway.R
import com.example.myway.screens.CustomTextField
import com.example.myway.ui.theme.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CrearPlan(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Estados del formulario
    var titulo by remember { mutableStateOf("") }
    var destinos by remember { mutableStateOf(mutableListOf<DestinoItem>()) }

    // Estados de búsqueda
    var searchText by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showPredictions by remember { mutableStateOf(false) }

    // Estados del calendario
    var mesActual by remember { mutableStateOf(Calendar.getInstance()) }
    var fechasSeleccionadas by remember { mutableStateOf(mutableSetOf<String>()) }

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    // Búsqueda de lugares
    LaunchedEffect(searchText) {
        if (searchText.length > 2) {
            delay(500)
            searchPlaces(placesClient, searchText) { results ->
                predictions = results
                showPredictions = results.isNotEmpty()
            }
        } else {
            predictions = emptyList()
            showPredictions = false
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(R.string.volver),
                    tint = Blanco,
                    modifier = Modifier
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Crear plan nuevo",
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card principal del formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Blanco
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Título
                    CustomRoundedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        placeholder = "Título *",
                        backgroundColor = Azul4
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buscador de lugares
                    Text(
                        text = "Lugares *",
                        color = Azul1,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CustomTextField(
                        placeholder = "Buscar lugares...",
                        color = Azul4.copy(alpha = 0.1f),
                        textColor = Color.Black,
                        fontWeight = FontWeight.Normal,
                        onTextChange = { searchText = it },
                        text = searchText,
                        showBorder = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    // Predicciones de búsqueda
                    if (showPredictions && predictions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(predictions) { prediction ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val placeId = prediction.placeId
                                                val placeName = prediction
                                                    .getPrimaryText(null)
                                                    .toString()

                                                // Agregar destino a la lista
                                                if (!destinos.any { it.id == placeId }) {
                                                    destinos.add(
                                                        DestinoItem(
                                                            id = placeId,
                                                            nombre = placeName
                                                        )
                                                    )
                                                }

                                                // Limpiar búsqueda
                                                searchText = ""
                                                showPredictions = false
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = prediction.getPrimaryText(null).toString(),
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        prediction.getSecondaryText(null)?.let { secondary ->
                                            Text(
                                                text = secondary.toString(),
                                                fontFamily = Nunito,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    if (prediction != predictions.last()) {
                                        HorizontalDivider(
                                            color = Color.LightGray,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lista de destinos agregados
                    if (destinos.isNotEmpty()) {
                        Text(
                            text = "Destinos seleccionados:",
                            color = Azul1.copy(alpha = 0.7f),
                            fontFamily = Nunito,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        destinos.forEachIndexed { index, destino ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Azul4.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            color = Azul4,
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = destino.nombre,
                                            color = Azul1,
                                            fontFamily = Nunito,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    IconButton(
                                        onClick = { destinos.removeAt(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ¿Cuándo?
                    Text(
                        text = "¿Cuándo?",
                        color = Azul1,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Calendario
                    CalendarioPersonalizado(
                        mesActual = mesActual,
                        onMesChange = { nuevoMes ->
                            mesActual = nuevoMes
                        },
                        fechasSeleccionadas = fechasSeleccionadas,
                        onFechaClick = { fecha ->
                            if (fechasSeleccionadas.contains(fecha)) {
                                fechasSeleccionadas.remove(fecha)
                            } else {
                                fechasSeleccionadas.add(fecha)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón "Siguiente" dentro del Card
                    Button(
                        onClick = {
                            when {
                                titulo.isBlank() -> {
                                    Toast.makeText(
                                        context,
                                        "Ingresa un título",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                destinos.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        "Agrega al menos un destino",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                fechasSeleccionadas.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        "Selecciona al menos una fecha",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    // TODO: Navegar a pantalla de itinerario
                                    navController.navigate("itinerario_plan")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Verde
                        )
                    ) {
                        Text(
                            text = "Siguiente",
                            color = Blanco,
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Data class para los destinos
data class DestinoItem(
    val id: String,
    val nombre: String
)

@Composable
fun CustomRoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Blanco.copy(alpha = 0.7f),
                fontFamily = Nunito,
                fontSize = 14.sp
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedTextColor = Blanco,
            unfocusedTextColor = Blanco,
            disabledTextColor = Blanco,
            cursorColor = Blanco,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(
            fontFamily = Nunito,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        singleLine = true,
        readOnly = readOnly,
        trailingIcon = trailingIcon
    )
}

@Composable
fun CalendarioPersonalizado(
    mesActual: Calendar,
    onMesChange: (Calendar) -> Unit,
    fechasSeleccionadas: Set<String>,
    onFechaClick: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth()) {
        // Navegación del mes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val nuevaCal = mesActual.clone() as Calendar
                nuevaCal.add(Calendar.MONTH, -1)
                onMesChange(nuevaCal)
            }) {
                Text(
                    text = "←",
                    fontSize = 20.sp,
                    color = Azul1,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${mesActual.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es"))} ${mesActual.get(Calendar.YEAR)}",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Azul1
            )

            IconButton(onClick = {
                val nuevaCal = mesActual.clone() as Calendar
                nuevaCal.add(Calendar.MONTH, 1)
                onMesChange(nuevaCal)
            }) {
                Text(
                    text = "→",
                    fontSize = 20.sp,
                    color = Azul1,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("D", "L", "M", "M", "J", "V", "S").forEach { dia ->
                Text(
                    text = dia,
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de días
        val primerDia = Calendar.getInstance().apply {
            time = mesActual.time
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val diasMes = mesActual.getActualMaximum(Calendar.DAY_OF_MONTH)
        val primerDiaSemana = primerDia.get(Calendar.DAY_OF_WEEK) - 1

        var diaActual = 1
        val filas = 6

        Column {
            for (fila in 0 until filas) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (columna in 0..6) {
                        if (fila == 0 && columna < primerDiaSemana) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else if (diaActual > diasMes) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val fechaCal = Calendar.getInstance().apply {
                                time = mesActual.time
                                set(Calendar.DAY_OF_MONTH, diaActual)
                            }
                            val fechaStr = dateFormat.format(fechaCal.time)
                            val seleccionada = fechasSeleccionadas.contains(fechaStr)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (seleccionada) Azul1 else Color.Transparent
                                    )
                                    .clickable { onFechaClick(fechaStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diaActual.toString(),
                                    fontFamily = Nunito,
                                    fontSize = 13.sp,
                                    color = if (seleccionada) Blanco else Azul1,
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            diaActual++
                        }
                    }
                }
            }
        }
    }
}

// Función de búsqueda de lugares
private fun searchPlaces(
    placesClient: PlacesClient,
    query: String,
    onResult: (List<AutocompletePrediction>) -> Unit
) {
    val token = AutocompleteSessionToken.newInstance()
    val request = FindAutocompletePredictionsRequest.builder()
        .setSessionToken(token)
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response -> onResult(response.autocompletePredictions) }
        .addOnFailureListener { onResult(emptyList()) }
}