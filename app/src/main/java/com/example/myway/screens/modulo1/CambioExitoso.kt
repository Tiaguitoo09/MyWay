package com.example.myway.screens.modulo1

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomTitleText
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.utils.UsuarioTemporal
import kotlinx.coroutines.delay

@Composable
fun CambioExitoso(navController: NavController) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3500)

        // Cerrar sesión de Firebase (si la usas)
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        com.example.myway.utils.UsuarioTemporal.correo = ""
        com.example.myway.utils.UsuarioTemporal.nombre = ""
        com.example.myway.utils.UsuarioTemporal.fechaNacimiento = ""
        UsuarioTemporal.apellido = ""
        // Redirigir a la pantalla de ingreso
        navController.navigate("ingreso_usuario") {
            popUpTo(0) { inclusive = true } // Limpia el historial de navegación
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.circuloconfirmacion),
                contentDescription = stringResource(R.string.confirmacion),
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CustomTitleText(
                text = stringResource(R.string.cambio_contrasena_exitoso),
                color = Blanco,
                fontSize = 28.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}



