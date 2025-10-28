package com.example.myway.screens.modulo2

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*

data class PreferenciasViajeData(
    val transportePreferido: String = "driving", // walking, driving, motorcycle, transit
    val paradasSugeridas: Set<String> = emptySet(), // gasolinera, restaurante, tienda, baño
    val prioridadViaje: String = "rapida" // rapida, economica, tranquila
)

object PreferenciasManager {
    private const val PREFS_NAME = "preferencias_viaje"
    private const val KEY_TRANSPORTE = "transporte_preferido"
    private const val KEY_PARADAS = "paradas_sugeridas"
    private const val KEY_PRIORIDAD = "prioridad_viaje"

    fun guardarPreferencias(context: Context, preferencias: PreferenciasViajeData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_TRANSPORTE, preferencias.transportePreferido)
            putStringSet(KEY_PARADAS, preferencias.paradasSugeridas)
            putString(KEY_PRIORIDAD, preferencias.prioridadViaje)
            apply()
        }
    }

    fun cargarPreferencias(context: Context): PreferenciasViajeData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PreferenciasViajeData(
            transportePreferido = prefs.getString(KEY_TRANSPORTE, "driving") ?: "driving",
            paradasSugeridas = prefs.getStringSet(KEY_PARADAS, emptySet()) ?: emptySet(),
            prioridadViaje = prefs.getString(KEY_PRIORIDAD, "rapida") ?: "rapida"
        )
    }
}

@Composable
fun PreferenciasViaje(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var preferencias by remember {
        mutableStateOf(PreferenciasManager.cargarPreferencias(context))
    }

    var seccionTransporteExpandida by remember { mutableStateOf(true) }
    var seccionParadasExpandida by remember { mutableStateOf(false) }
    var seccionPrioridadExpandida by remember { mutableStateOf(false) }

    // Guardar automáticamente cuando cambian las preferencias
    LaunchedEffect(preferencias) {
        PreferenciasManager.guardarPreferencias(context, preferencias)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = stringResource(id = R.string.volver),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .zIndex(3f)
                .clickable { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_compass),
                contentDescription = "Preferencias",
                tint = Blanco,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Preferencias de Viaje",
                color = Blanco,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Nunito
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Configura tus preferencias para que MyWay personalice tus rutas",
                color = Blanco.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontFamily = Nunito,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // SECCIÓN 1: Medio de transporte preferido
            SeccionExpandible(
                titulo = "Medio de transporte preferido",
                expandida = seccionTransporteExpandida,
                onToggle = { seccionTransporteExpandida = !seccionTransporteExpandida }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OpcionCheckbox(
                        icono = R.drawable.ic_walk,
                        texto = "Bicicleta",
                        seleccionado = preferencias.transportePreferido == "bicycling",
                        onClick = {
                            preferencias = preferencias.copy(transportePreferido = "bicycling")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OpcionCheckbox(
                        icono = R.drawable.ic_car,
                        texto = "Carro",
                        seleccionado = preferencias.transportePreferido == "driving",
                        onClick = {
                            preferencias = preferencias.copy(transportePreferido = "driving")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OpcionCheckbox(
                        icono = R.drawable.ic_motorcycle,
                        texto = "Moto",
                        seleccionado = preferencias.transportePreferido == "motorcycle",
                        onClick = {
                            preferencias = preferencias.copy(transportePreferido = "motorcycle")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OpcionCheckbox(
                        icono = android.R.drawable.ic_dialog_map,
                        texto = "Transporte público",
                        seleccionado = preferencias.transportePreferido == "transit",
                        onClick = {
                            preferencias = preferencias.copy(transportePreferido = "transit")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECCIÓN 2: Paradas Sugeridas
            SeccionExpandible(
                titulo = "Paradas Sugeridas en el Camino",
                expandida = seccionParadasExpandida,
                onToggle = { seccionParadasExpandida = !seccionParadasExpandida }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OpcionCheckboxMultiple(
                        icono = android.R.drawable.ic_menu_view,
                        texto = "Gasolinera",
                        seleccionado = preferencias.paradasSugeridas.contains("gasolinera"),
                        onClick = {
                            val nuevasParadas = preferencias.paradasSugeridas.toMutableSet()
                            if (nuevasParadas.contains("gasolinera")) {
                                nuevasParadas.remove("gasolinera")
                            } else {
                                nuevasParadas.add("gasolinera")
                            }
                            preferencias = preferencias.copy(paradasSugeridas = nuevasParadas)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OpcionCheckboxMultiple(
                        icono = android.R.drawable.ic_menu_view,
                        texto = "Restaurante",
                        seleccionado = preferencias.paradasSugeridas.contains("restaurante"),
                        onClick = {
                            val nuevasParadas = preferencias.paradasSugeridas.toMutableSet()
                            if (nuevasParadas.contains("restaurante")) {
                                nuevasParadas.remove("restaurante")
                            } else {
                                nuevasParadas.add("restaurante")
                            }
                            preferencias = preferencias.copy(paradasSugeridas = nuevasParadas)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OpcionCheckboxMultiple(
                        icono = android.R.drawable.ic_menu_view,
                        texto = "Tienda",
                        seleccionado = preferencias.paradasSugeridas.contains("tienda"),
                        onClick = {
                            val nuevasParadas = preferencias.paradasSugeridas.toMutableSet()
                            if (nuevasParadas.contains("tienda")) {
                                nuevasParadas.remove("tienda")
                            } else {
                                nuevasParadas.add("tienda")
                            }
                            preferencias = preferencias.copy(paradasSugeridas = nuevasParadas)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OpcionCheckboxMultiple(
                        icono = android.R.drawable.ic_menu_view,
                        texto = "Baño",
                        seleccionado = preferencias.paradasSugeridas.contains("baño"),
                        onClick = {
                            val nuevasParadas = preferencias.paradasSugeridas.toMutableSet()
                            if (nuevasParadas.contains("baño")) {
                                nuevasParadas.remove("baño")
                            } else {
                                nuevasParadas.add("baño")
                            }
                            preferencias = preferencias.copy(paradasSugeridas = nuevasParadas)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECCIÓN 3: Prioridad de Viaje
            SeccionExpandible(
                titulo = "Prioridad de Viaje",
                expandida = seccionPrioridadExpandida,
                onToggle = { seccionPrioridadExpandida = !seccionPrioridadExpandida }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OpcionCheckbox(
                        icono = android.R.drawable.ic_media_ff,
                        texto = "Ruta más rápida",
                        seleccionado = preferencias.prioridadViaje == "rapida",
                        onClick = {
                            preferencias = preferencias.copy(prioridadViaje = "rapida")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OpcionCheckbox(
                        icono = android.R.drawable.star_on,
                        texto = "Ruta con tranquilidad / segura",
                        seleccionado = preferencias.prioridadViaje == "tranquila",
                        onClick = {
                            preferencias = preferencias.copy(prioridadViaje = "tranquila")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Resumen de preferencias
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Verde.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✓",
                            color = Verde,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Preferencias guardadas",
                            color = Blanco,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Nunito
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tus preferencias se aplicarán automáticamente en todas tus rutas",
                        color = Blanco.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontFamily = Nunito
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SeccionExpandible(
    titulo: String,
    expandida: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Azul3.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    color = Blanco,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Nunito,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                    contentDescription = if (expandida) "Contraer" else "Expandir",
                    tint = Blanco,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expandida) 180f else 0f)
                )
            }

            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                content()
            }
        }
    }
}

@Composable
fun OpcionCheckbox(
    icono: Int,
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                Verde.copy(alpha = 0.2f)
            else
                Azul1.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = seleccionado,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Verde,
                    uncheckedColor = Blanco.copy(alpha = 0.5f),
                    checkmarkColor = Blanco
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                painter = painterResource(id = icono),
                contentDescription = texto,
                tint = if (seleccionado) Verde else Blanco.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = texto,
                color = Blanco,
                fontSize = 15.sp,
                fontFamily = Nunito,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun OpcionCheckboxMultiple(
    icono: Int,
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                Azul4.copy(alpha = 0.3f)
            else
                Azul1.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = seleccionado,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Azul4,
                    uncheckedColor = Blanco.copy(alpha = 0.5f),
                    checkmarkColor = Blanco
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                painter = painterResource(id = icono),
                contentDescription = texto,
                tint = if (seleccionado) Azul4 else Blanco.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = texto,
                color = Blanco,
                fontSize = 15.sp,
                fontFamily = Nunito,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}