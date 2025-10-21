package com.example.myway.screens.modulo3

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.myway.screens.CustomTextField
import com.example.myway.ui.theme.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlaneaViaje(navController: NavController) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showPredictions by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Inicializar Places API con BuildConfig
    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    // Buscar predicciones cuando el texto cambia
    LaunchedEffect(searchText) {
        if (searchText.length > 2) {
            delay(500) // Debounce
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

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Text(
                    text = stringResource(R.string.planea_viaje),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Image(
                    painter = painterResource(id = R.drawable.icono_perfil),
                    contentDescription = stringResource(R.string.perfil),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable { navController.navigate("perfil_ajustes") }
                )
            }

            Text(
                text = stringResource(R.string.activar_permisos),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Pregunta
            Text(
                text = stringResource(R.string.a_donde_vamos),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 8.dp)
            )

            // Barra de búsqueda
            CustomTextField(
                placeholder = stringResource(R.string.buscar),
                color = Blanco,
                textColor = Color.Black,
                onTextChange = { searchText = it },
                text = searchText,
                showBorder = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            // Resultados de búsqueda
            if (showPredictions && predictions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(predictions) { prediction ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Navegar al mapa con la ubicación seleccionada
                                        val placeId = prediction.placeId
                                        val placeName = prediction
                                            .getPrimaryText(null)
                                            .toString()
                                        navController.navigate("home/${placeId}/${placeName}")
                                        showPredictions = false
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = prediction
                                        .getPrimaryText(null)
                                        .toString(),
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = prediction
                                        .getSecondaryText(null)
                                        .toString(),
                                    fontFamily = Nunito,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                // Contenido original cuando no hay búsqueda
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de categorías
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomButton(
                                text = stringResource(R.string.guardados),
                                color = Blanco,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("guardados") }
                            )

                            CustomButton(
                                text = stringResource(R.string.alimentos),
                                color = Blanco,
                                modifier = Modifier.weight(1f),
                                onClick = { }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomButton(
                                text = stringResource(R.string.combustible),
                                color = Blanco,
                                modifier = Modifier.weight(1f),
                                onClick = { }
                            )

                            CustomButton(
                                text = stringResource(R.string.supermercados),
                                color = Blanco,
                                modifier = Modifier.weight(1f),
                                onClick = { }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomButton(
                                text = stringResource(R.string.hoteles),
                                color = Blanco,
                                modifier = Modifier.weight(1f),
                                onClick = { }
                            )

                            CustomButton(
                                text = stringResource(R.string.parques),
                                color = Blanco,
                                modifier = Modifier.weight(1f),
                                onClick = { }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección "Recientes"
                    Text(
                        text = stringResource(R.string.recientes),
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    val recientes = listOf(
                        "Calle 9 Bis #19A-60",
                        "Carrera 16 #187-61",
                        "Calle 7c Bis #72A-20",
                        "Calle 60, Teusaquillo"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recientes.forEach { lugar ->
                            CustomButton(
                                text = lugar,
                                color = Blanco,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { searchText = lugar }
                            )
                        }
                    }
                }
            }
        }
    }
}

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
        .addOnSuccessListener { response ->
            onResult(response.autocompletePredictions)
        }
        .addOnFailureListener { exception ->
            exception.printStackTrace()
            onResult(emptyList())
        }
}