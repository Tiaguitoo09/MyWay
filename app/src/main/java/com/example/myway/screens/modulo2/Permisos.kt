package com.example.myway.screens.modulo2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Azul4
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Rojo
import com.example.myway.ui.theme.Verde

@Composable
fun Permisos(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estados de permisos
    var ubicacionPermitida by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var camaraPermitida by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Función para verificar permisos
    fun verificarPermisos() {
        ubicacionPermitida = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        camaraPermitida = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Launcher para permisos de ubicación
    val ubicacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        ubicacionPermitida = isGranted
        if (!isGranted) {
            // Si no otorga el permiso, abrir configuración
            abrirConfiguracionApp(context)
        }
    }

    // Launcher para permisos de cámara
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        camaraPermitida = isGranted
        if (!isGranted) {
            abrirConfiguracionApp(context)
        }
    }

    // Verificar permisos al cargar la pantalla
    LaunchedEffect(Unit) {
        verificarPermisos()
    }

    // Verificar permisos cuando la app vuelve del primer plano (ej: después de ir a Settings)
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

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(id = R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
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

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Ícono de permisos
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Blanco),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                    contentDescription = "Permisos",
                    tint = Azul4,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = "Permisos",
                color = Blanco,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Nunito
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Azul3.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Título de la tarjeta
                    Text(
                        text = "Permisos de la App",
                        color = Blanco,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Controla qué permisos has otorgado a MyWay para ofrecerte la mejor experiencia. Puedes activarlos o desactivarlos en cualquier momento.",
                        color = Blanco.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontFamily = Nunito,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // PERMISO: UBICACIÓN
                    PermisoItem(
                        icono = android.R.drawable.ic_menu_mylocation,
                        titulo = "Ubicación",
                        descripcion = "Necesario para calcular rutas y mostrar tu posición en el mapa",
                        isEnabled = ubicacionPermitida,
                        esObligatorio = true,
                        onToggle = {
                            if (ubicacionPermitida) {
                                // Si está activado, abrir configuración para desactivar
                                abrirConfiguracionApp(context)
                            } else {
                                // Si está desactivado, solicitar permiso
                                ubicacionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PERMISO: CÁMARA
                    PermisoItem(
                        icono = android.R.drawable.ic_menu_camera,
                        titulo = "Cámara",
                        descripcion = "Para tomar fotos de perfil y escanear códigos QR",
                        isEnabled = camaraPermitida,
                        esObligatorio = false,
                        onToggle = {
                            if (camaraPermitida) {
                                abrirConfiguracionApp(context)
                            } else {
                                camaraLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nota informativa
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
                                text = "La ubicación es necesaria para el funcionamiento de la aplicación. Sin ella, no podrás usar el mapa ni calcular rutas.",
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

            // Resumen de estado de permisos
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
                                ubicacionPermitida && camaraPermitida -> "MyWay tiene acceso completo a todas las funciones"
                                ubicacionPermitida -> "Funciones básicas de navegación disponibles"
                                else -> "La app necesita ubicación para funcionar"
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
    }
}

@Composable
fun PermisoItem(
    icono: Int,
    titulo: String,
    descripcion: String,
    isEnabled: Boolean,
    esObligatorio: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blanco.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) Verde.copy(alpha = 0.2f) else Rojo.copy(alpha = 0.2f)),
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

            // Texto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Switch
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(1.0f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Verde,
                    checkedTrackColor = Verde.copy(alpha = 0.5f),
                    uncheckedThumbColor = Rojo,
                    uncheckedTrackColor = Rojo.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Abre la configuración de la app para que el usuario cambie permisos manualmente
 */
fun abrirConfiguracionApp(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        // Si falla, intentar abrir la configuración general
        try {
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}