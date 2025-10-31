package com.example.myway.screens.modulo2

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

data class PreferenciasViajeData(
    val transportesSeleccionados: Set<String> = setOf("driving", "motorcycle", "walking"),
    val transportePreferido: String = "driving",
    val paradasSugeridas: Set<String> = emptySet(),
    val rutaMasRapida: Boolean = false
)

object PreferenciasManager {
    private const val PREFS_NAME = "preferencias_viaje"
    private const val KEY_TRANSPORTES_SELECCIONADOS = "transportes_seleccionados"
    private const val KEY_TRANSPORTE = "transporte_preferido"
    private const val KEY_PARADAS = "paradas_sugeridas"
    private const val KEY_RUTA_RAPIDA = "ruta_mas_rapida"

    // ✅ GUARDAR EN COLECCIÓN SEPARADA "preferencias_viaje"
    suspend fun guardarPreferenciasFirebase(preferencias: PreferenciasViajeData): Boolean {
        return try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            val userId = auth.currentUser?.uid ?: UsuarioTemporal.correo

            if (userId.isNullOrEmpty()) {
                Log.e("PreferenciasManager", "❌ No hay usuario autenticado")
                return false
            }

            val prefsMap = hashMapOf(
                "userId" to userId,
                "transportesSeleccionados" to preferencias.transportesSeleccionados.toList(),
                "transportePreferido" to preferencias.transportePreferido,
                "paradasSugeridas" to preferencias.paradasSugeridas.toList(),
                "rutaMasRapida" to preferencias.rutaMasRapida,
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )

            // ✅ Guardar en colección "preferencias_viaje" con el userId como ID del documento
            db.collection("preferencias_viaje")
                .document(userId)
                .set(prefsMap)
                .await()

            Log.d("PreferenciasManager", "✅ Preferencias guardadas en Firebase (colección: preferencias_viaje)")
            true
        } catch (e: Exception) {
            Log.e("PreferenciasManager", "❌ Error al guardar en Firebase: ${e.message}")
            false
        }
    }

    // ✅ CARGAR DESDE COLECCIÓN SEPARADA "preferencias_viaje"
    suspend fun cargarPreferenciasFirebase(): PreferenciasViajeData? {
        return try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            val userId = auth.currentUser?.uid ?: UsuarioTemporal.correo

            if (userId.isNullOrEmpty()) {
                Log.e("PreferenciasManager", "❌ No hay usuario autenticado")
                return null
            }

            // ✅ Leer de colección "preferencias_viaje"
            val doc = db.collection("preferencias_viaje")
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                val transportes = (doc.get("transportesSeleccionados") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet() ?: setOf("driving", "motorcycle", "walking")

                val paradas = (doc.get("paradasSugeridas") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet() ?: emptySet()

                Log.d("PreferenciasManager", "✅ Preferencias cargadas desde Firebase (colección: preferencias_viaje)")
                PreferenciasViajeData(
                    transportesSeleccionados = transportes,
                    transportePreferido = doc.getString("transportePreferido") ?: "driving",
                    paradasSugeridas = paradas,
                    rutaMasRapida = doc.getBoolean("rutaMasRapida") ?: false
                )
            } else {
                Log.d("PreferenciasManager", "⚠️ No hay preferencias en Firebase, usando locales")
                null
            }
        } catch (e: Exception) {
            Log.e("PreferenciasManager", "❌ Error al cargar desde Firebase: ${e.message}")
            null
        }
    }

    // Guardar en SharedPreferences (backup local)
    fun guardarPreferenciasLocal(context: Context, preferencias: PreferenciasViajeData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putStringSet(KEY_TRANSPORTES_SELECCIONADOS, preferencias.transportesSeleccionados)
            putString(KEY_TRANSPORTE, preferencias.transportePreferido)
            putStringSet(KEY_PARADAS, preferencias.paradasSugeridas)
            putBoolean(KEY_RUTA_RAPIDA, preferencias.rutaMasRapida)
            apply()
        }
    }

    // Cargar desde SharedPreferences (backup local)
    fun cargarPreferenciasLocal(context: Context): PreferenciasViajeData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PreferenciasViajeData(
            transportesSeleccionados = prefs.getStringSet(KEY_TRANSPORTES_SELECCIONADOS, setOf("driving", "motorcycle", "walking"))
                ?: setOf("driving", "motorcycle", "walking"),
            transportePreferido = prefs.getString(KEY_TRANSPORTE, "driving") ?: "driving",
            paradasSugeridas = prefs.getStringSet(KEY_PARADAS, emptySet()) ?: emptySet(),
            rutaMasRapida = prefs.getBoolean(KEY_RUTA_RAPIDA, false)
        )
    }

    // ✅ NUEVA: Función unificada que intenta Firebase primero, luego local
    suspend fun cargarPreferencias(context: Context): PreferenciasViajeData {
        val firebasePrefs = cargarPreferenciasFirebase()
        return if (firebasePrefs != null) {
            // Si hay datos en Firebase, actualizar local también
            guardarPreferenciasLocal(context, firebasePrefs)
            firebasePrefs
        } else {
            // Si no hay en Firebase, usar local
            cargarPreferenciasLocal(context)
        }
    }
}

@Composable
fun PreferenciasViaje(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var preferencias by remember { mutableStateOf<PreferenciasViajeData?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    var seccionTransporteExpandida by remember { mutableStateOf(true) }
    var seccionParadasExpandida by remember { mutableStateOf(false) }
    var seccionRutaRapidaExpandida by remember { mutableStateOf(false) }

    // Cargar preferencias al inicio
    LaunchedEffect(Unit) {
        preferencias = PreferenciasManager.cargarPreferencias(context)
        cargando = false
    }

    // ⏱️ Guardar automáticamente con delay de 500ms
    LaunchedEffect(preferencias) {
        if (preferencias != null && !cargando) {
            guardando = true
            delay(500) // Espera 500ms antes de guardar
            // Guardar en Firebase
            val exitoso = PreferenciasManager.guardarPreferenciasFirebase(preferencias!!)
            // Guardar backup local
            PreferenciasManager.guardarPreferenciasLocal(context, preferencias!!)
            guardando = false

            // ✅ Mostrar confirmación visual (opcional, descomenta si quieres)
            // if (exitoso) {
            //     Toast.makeText(context, "✓ Guardado en la nube", Toast.LENGTH_SHORT).show()
            // }
        }
    }

    if (cargando) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Verde)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Header con flecha y título
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(3f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.flecha),
                contentDescription = stringResource(id = R.string.volver),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Preferencias de Viaje",
                color = Blanco,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Nunito
            )

            Spacer(modifier = Modifier.weight(1f))

            // Indicador de guardado
            if (guardando) {
                CircularProgressIndicator(
                    color = Verde,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona tus medios de transporte y MyWay los mostrará en tus rutas",
                color = Blanco,
                fontSize = 16.sp,
                fontFamily = Nunito,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            preferencias?.let { prefs ->
                // SECCIÓN 1: Medio de transporte preferido
                SeccionExpandible(
                    titulo = "Medios de transporte",
                    expandida = seccionTransporteExpandida,
                    onToggle = { seccionTransporteExpandida = !seccionTransporteExpandida }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selecciona los transportes que usas (mínimo 1):",
                            color = Blanco.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontFamily = Nunito,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OpcionCheckboxMultiple(
                            icono = R.drawable.ic_car,
                            texto = "Carro",
                            seleccionado = prefs.transportesSeleccionados.contains("driving"),
                            onClick = {
                                val nuevosTransportes = prefs.transportesSeleccionados.toMutableSet()
                                if (nuevosTransportes.contains("driving")) {
                                    if (nuevosTransportes.size > 1) {
                                        nuevosTransportes.remove("driving")
                                        val nuevoPreferido = if (prefs.transportePreferido == "driving") {
                                            nuevosTransportes.first()
                                        } else {
                                            prefs.transportePreferido
                                        }
                                        preferencias = prefs.copy(
                                            transportesSeleccionados = nuevosTransportes,
                                            transportePreferido = nuevoPreferido
                                        )
                                    }
                                } else {
                                    nuevosTransportes.add("driving")
                                    preferencias = prefs.copy(transportesSeleccionados = nuevosTransportes)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OpcionCheckboxMultiple(
                            icono = R.drawable.ic_motorcycle,
                            texto = "Moto",
                            seleccionado = prefs.transportesSeleccionados.contains("motorcycle"),
                            onClick = {
                                val nuevosTransportes = prefs.transportesSeleccionados.toMutableSet()
                                if (nuevosTransportes.contains("motorcycle")) {
                                    if (nuevosTransportes.size > 1) {
                                        nuevosTransportes.remove("motorcycle")
                                        val nuevoPreferido = if (prefs.transportePreferido == "motorcycle") {
                                            nuevosTransportes.first()
                                        } else {
                                            prefs.transportePreferido
                                        }
                                        preferencias = prefs.copy(
                                            transportesSeleccionados = nuevosTransportes,
                                            transportePreferido = nuevoPreferido
                                        )
                                    }
                                } else {
                                    nuevosTransportes.add("motorcycle")
                                    preferencias = prefs.copy(transportesSeleccionados = nuevosTransportes)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OpcionCheckboxMultiple(
                            icono = R.drawable.ic_walk,
                            texto = "Caminando",
                            seleccionado = prefs.transportesSeleccionados.contains("walking"),
                            onClick = {
                                val nuevosTransportes = prefs.transportesSeleccionados.toMutableSet()
                                if (nuevosTransportes.contains("walking")) {
                                    if (nuevosTransportes.size > 1) {
                                        nuevosTransportes.remove("walking")
                                        val nuevoPreferido = if (prefs.transportePreferido == "walking") {
                                            nuevosTransportes.first()
                                        } else {
                                            prefs.transportePreferido
                                        }
                                        preferencias = prefs.copy(
                                            transportesSeleccionados = nuevosTransportes,
                                            transportePreferido = nuevoPreferido
                                        )
                                    }
                                } else {
                                    nuevosTransportes.add("walking")
                                    preferencias = prefs.copy(transportesSeleccionados = nuevosTransportes)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Divider(color = Blanco.copy(alpha = 0.3f), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Transporte preferido (se selecciona por defecto):",
                            color = Blanco.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontFamily = Nunito,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (prefs.transportesSeleccionados.contains("driving")) {
                            OpcionCheckbox(
                                icono = R.drawable.ic_car,
                                texto = "Carro ⭐",
                                seleccionado = prefs.transportePreferido == "driving",
                                onClick = {
                                    preferencias = prefs.copy(transportePreferido = "driving")
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (prefs.transportesSeleccionados.contains("motorcycle")) {
                            OpcionCheckbox(
                                icono = R.drawable.ic_motorcycle,
                                texto = "Moto ⭐",
                                seleccionado = prefs.transportePreferido == "motorcycle",
                                onClick = {
                                    preferencias = prefs.copy(transportePreferido = "motorcycle")
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (prefs.transportesSeleccionados.contains("walking")) {
                            OpcionCheckbox(
                                icono = R.drawable.ic_walk,
                                texto = "Caminando ⭐",
                                seleccionado = prefs.transportePreferido == "walking",
                                onClick = {
                                    preferencias = prefs.copy(transportePreferido = "walking")
                                }
                            )
                        }
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
                            seleccionado = prefs.paradasSugeridas.contains("gasolinera"),
                            onClick = {
                                val nuevasParadas = prefs.paradasSugeridas.toMutableSet()
                                if (nuevasParadas.contains("gasolinera")) {
                                    nuevasParadas.remove("gasolinera")
                                } else {
                                    nuevasParadas.add("gasolinera")
                                }
                                preferencias = prefs.copy(paradasSugeridas = nuevasParadas)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OpcionCheckboxMultiple(
                            icono = android.R.drawable.ic_menu_view,
                            texto = "Restaurante",
                            seleccionado = prefs.paradasSugeridas.contains("restaurante"),
                            onClick = {
                                val nuevasParadas = prefs.paradasSugeridas.toMutableSet()
                                if (nuevasParadas.contains("restaurante")) {
                                    nuevasParadas.remove("restaurante")
                                } else {
                                    nuevasParadas.add("restaurante")
                                }
                                preferencias = prefs.copy(paradasSugeridas = nuevasParadas)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OpcionCheckboxMultiple(
                            icono = android.R.drawable.ic_menu_view,
                            texto = "Tienda",
                            seleccionado = prefs.paradasSugeridas.contains("tienda"),
                            onClick = {
                                val nuevasParadas = prefs.paradasSugeridas.toMutableSet()
                                if (nuevasParadas.contains("tienda")) {
                                    nuevasParadas.remove("tienda")
                                } else {
                                    nuevasParadas.add("tienda")
                                }
                                preferencias = prefs.copy(paradasSugeridas = nuevasParadas)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN 3: Prioridad de Viaje
                SeccionExpandible(
                    titulo = "Prioridad de Viaje",
                    expandida = seccionRutaRapidaExpandida,
                    onToggle = { seccionRutaRapidaExpandida = !seccionRutaRapidaExpandida }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OpcionCheckboxMultiple(
                            icono = android.R.drawable.ic_media_ff,
                            texto = "Ruta más rápida",
                            seleccionado = prefs.rutaMasRapida,
                            onClick = {
                                preferencias = prefs.copy(rutaMasRapida = !prefs.rutaMasRapida)
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
                        containerColor = if (guardando)
                            Amarillo.copy(alpha = 0.2f)
                        else
                            Verde.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (guardando) "⏳" else "✓",
                                color = if (guardando) Amarillo else Verde,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (guardando)
                                    "Sincronizando con la nube..."
                                else
                                    "Preferencias guardadas en la nube",
                                color = Blanco,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Nunito
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Solo verás ${prefs.transportesSeleccionados.size} opción(es) de transporte en tus rutas. Tus preferencias se sincronizan automáticamente.",
                            color = Blanco.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontFamily = Nunito
                        )
                    }
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
            RadioButton(
                selected = seleccionado,
                onClick = { onClick() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Verde,
                    unselectedColor = Blanco.copy(alpha = 0.5f)
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