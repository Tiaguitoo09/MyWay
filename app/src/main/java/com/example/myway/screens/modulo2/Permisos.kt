package com.example.myway.screens.modulo2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*

@Composable
fun Permisos(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    var mostrarDialogoUbicacionSistema by remember { mutableStateOf(false) }
    var ubicacionPermitida by remember { mutableStateOf(false) }
    var camaraPermitida by remember { mutableStateOf(false) }

    fun abrirConfiguracionUbicacion() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    fun verificarPermisos() {
        val tienePermisoUbicacion = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val ubicacionHabilitada = isLocationEnabled()

        // Solo está activo si AMBOS están habilitados
        ubicacionPermitida = tienePermisoUbicacion && ubicacionHabilitada

        camaraPermitida = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Launcher para ubicación
    val ubicacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        ubicacionPermitida = isGranted
    }

    // Launcher para cámara
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        camaraPermitida = isGranted
    }

    LaunchedEffect(Unit) {
        verificarPermisos()
    }

    // Verificar permisos cuando la app vuelve al frente
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                verificarPermisos()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Verificar permisos constantemente cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            verificarPermisos()
        }
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

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                    contentDescription = "Permisos",
                    tint = Blanco,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Permisos",
                color = Blanco,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Nunito
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Azul3.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Permisos de la App",
                        color = Blanco,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Controla qué permisos has otorgado a MyWay para ofrecerte la mejor experiencia.",
                        color = Blanco.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontFamily = Nunito,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // PERMISO: UBICACIÓN
                    PermisoItemMejorado(
                        icono = android.R.drawable.ic_menu_mylocation,
                        titulo = "Ubicación",
                        descripcion = "Necesario para calcular rutas y mostrar tu posición",
                        isEnabled = ubicacionPermitida,
                        esObligatorio = true,
                        onActivar = {
                            val tienePermiso = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            val ubicacionSistema = isLocationEnabled()

                            when {
                                // Caso 1: No tiene permiso de app
                                !tienePermiso -> {
                                    ubicacionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                                // Caso 2: Tiene permiso pero ubicación del sistema OFF
                                !ubicacionSistema -> {
                                    mostrarDialogoUbicacionSistema = true
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PERMISO: CÁMARA
                    PermisoItemMejorado(
                        icono = android.R.drawable.ic_menu_camera,
                        titulo = "Cámara",
                        descripcion = "Para tomar fotos de perfil y escanear códigos QR",
                        isEnabled = camaraPermitida,
                        esObligatorio = false,
                        onActivar = {
                            camaraLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Azul1.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "ℹ️ Algunos permisos son esenciales",
                                color = Blanco,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Nunito
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "La ubicación es necesaria para el funcionamiento de la aplicación.",
                                color = Blanco.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontFamily = Nunito,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ubicacionPermitida && camaraPermitida)
                        Verde.copy(alpha = 0.2f)
                    else if (ubicacionPermitida)
                        Azul4.copy(alpha = 0.3f)
                    else
                        Rojo.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            ubicacionPermitida && camaraPermitida -> "✅"
                            ubicacionPermitida -> "⚠️"
                            else -> "❌"
                        },
                        fontSize = 32.sp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                ubicacionPermitida && camaraPermitida -> "Todos los permisos otorgados"
                                ubicacionPermitida -> "Permisos básicos activos"
                                else -> "Permisos esenciales faltantes"
                            },
                            color = Blanco,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Nunito
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when {
                                ubicacionPermitida && camaraPermitida -> "MyWay tiene acceso completo"
                                ubicacionPermitida -> "Funciones básicas disponibles"
                                else -> "La app necesita ubicación"
                            },
                            color = Blanco.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontFamily = Nunito,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Diálogo para activar ubicación del sistema
        if (mostrarDialogoUbicacionSistema) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoUbicacionSistema = false },
                containerColor = Azul3,
                title = {
                    Text(
                        text = "📍 Ubicación desactivada",
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "La ubicación de tu dispositivo está desactivada.\n\n" +
                                    "Para usar MyWay, necesitas activar la ubicación desde el panel rápido " +
                                    "o ir a la configuración de ubicación.",
                            color = Blanco,
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoUbicacionSistema = false
                            abrirConfiguracionUbicacion()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Verde)
                    ) {
                        Text("Activar ubicación", color = Blanco)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoUbicacionSistema = false }) {
                        Text("Ahora no", color = Blanco.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}

@Composable
fun PermisoItemMejorado(
    icono: Int,
    titulo: String,
    descripcion: String,
    isEnabled: Boolean,
    esObligatorio: Boolean,
    onActivar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled)
                Verde.copy(alpha = 0.15f)
            else
                Rojo.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isEnabled) {
                    onActivar()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = titulo,
                    tint = if (isEnabled) Verde else Rojo,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titulo,
                        color = Blanco,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito
                    )
                    if (esObligatorio) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•",
                            color = Rojo,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = descripcion,
                    color = Blanco.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontFamily = Nunito,
                    lineHeight = 18.sp
                )

                if (!isEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para activar",
                        color = if (esObligatorio) Rojo else Azul1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Activado",
                        color = Verde,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Switch visual personalizado
            Box(
                modifier = Modifier
                    .width(51.dp)
                    .height(31.dp)
                    .clip(RoundedCornerShape(15.5.dp))
                    .clickable(enabled = !isEnabled) {
                        onActivar()
                    },
                contentAlignment = if (isEnabled) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isEnabled) Verde.copy(alpha = 0.5f) else Rojo.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(15.5.dp)
                ) {}

                Surface(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(27.dp),
                    shape = CircleShape,
                    color = if (isEnabled) Verde else Rojo,
                    shadowElevation = 2.dp
                ) {}
            }
        }
    }
}