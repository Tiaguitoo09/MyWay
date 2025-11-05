package com.example.myway.screens.modulo3

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.myway.data.repository.RecentPlacesRepository
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
    val scope = rememberCoroutineScope()
    val recentPlacesRepository = remember { RecentPlacesRepository() }

    var searchText by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showPredictions by remember { mutableStateOf(false) }

    val recentPlaces by recentPlacesRepository.getRecentPlacesFlow()
        .collectAsState(initial = emptyList())

    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

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
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                fontWeight = FontWeight.Normal,
                onTextChange = { searchText = it },
                text = searchText,
                showBorder = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            if (showPredictions && predictions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(predictions) { prediction ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            val placeId = prediction.placeId
                                            val placeName = prediction.getPrimaryText(null).toString()
                                            val placeAddress = prediction.getSecondaryText(null)?.toString() ?: ""

                                            recentPlacesRepository.saveRecentPlace(
                                                placeId = placeId,
                                                placeName = placeName,
                                                placeAddress = placeAddress
                                            )
                                            navController.navigate("home/${placeId}/${placeName}")
                                        }
                                        showPredictions = false
                                        searchText = ""
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = prediction.getPrimaryText(null).toString(),
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = prediction.getSecondaryText(null)?.toString() ?: "",
                                    fontFamily = Nunito,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de categorías (ahora abren Home con placeType)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomButton(
                                alignCenter = false,
                                text = stringResource(R.string.guardados),
                                fontSize = 14.sp,
                                color = Blanco,
                                textColor = Negro,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("guardados") },
                                icon = painterResource(id = R.drawable.icono_guardados)
                            )
                            CustomButton(
                                alignCenter = false,
                                text = stringResource(R.string.alimentos),
                                fontSize = 14.sp,
                                color = Blanco,
                                textColor = Negro,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("home/restaurant") },
                                icon = painterResource(id = R.drawable.icono_alimentos)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomButton(
                                alignCenter = false,
                                text = stringResource(R.string.combustible),
                                fontSize = 14.sp,
                                color = Blanco,
                                textColor = Negro,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("home/gas_station") },
                                icon = painterResource(id = R.drawable.icono_gasolineria)
                            )

                            CustomButton(
                                alignCenter = false,
                                text = stringResource(R.string.supermercados),
                                fontSize = 13.5.sp,
                                color = Blanco,
                                textColor = Negro,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("home/supermarket") },
                                icon = painterResource(id = R.drawable.icono_supermercados)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomButton(
                                alignCenter = false,
                                text = stringResource(R.string.hoteles),
                                fontSize = 14.sp,
                                color = Blanco,
                                textColor = Negro,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("home/lodging") },
                                icon = painterResource(id = R.drawable.icono_hoteles)
                            )

                            CustomButton(
                                alignCenter = false,
                                text = stringResource(R.string.parques),
                                fontSize = 14.sp,
                                color = Blanco,
                                textColor = Negro,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("home/park") },
                                icon = painterResource(id = R.drawable.icono_parques)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Recientes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recientes),
                            color = Blanco,
                            fontFamily = Nunito,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )

                        if (recentPlaces.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.limpiar),
                                color = Blanco.copy(alpha = 0.7f),
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        recentPlacesRepository.clearAllRecents()
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.recientes_eliminados),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }

                    if (recentPlaces.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_lugares_recientes),
                            color = Blanco.copy(alpha = 0.7f),
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentPlaces.forEach { place ->
                                CustomButton(
                                    alignCenter = false,
                                    text = place.name,
                                    color = Blanco,
                                    textColor = Negro,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        scope.launch {
                                            recentPlacesRepository.saveRecentPlace(
                                                placeId = place.id,
                                                placeName = place.name,
                                                placeAddress = place.address,
                                                latitude = place.latitude,
                                                longitude = place.longitude
                                            )
                                            navController.navigate("home/${place.id}/${place.name}")
                                        }
                                    },
                                    icon = painterResource(id = R.drawable.icono_reloj)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

//Función búsqueda
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