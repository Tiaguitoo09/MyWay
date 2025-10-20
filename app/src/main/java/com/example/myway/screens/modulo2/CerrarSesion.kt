package com.example.myway.screens.modulo2

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTitleText
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.utils.ImageStorage
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CerrarSesion(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = "Fondo de la app",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen advertencia
            Image(
                painter = painterResource(id = R.drawable.circuloadvertencia),
                contentDescription = "Advertencia",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texto principal
            CustomTitleText(
                text = "¿Seguro/a que quieres\ncerrar sesión?",
                color = Blanco,
                fontSize = 28.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón Sí
                CustomButton(
                    text = "Sí",
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        auth.signOut() // Cierra la sesión de Firebase

                        // ✅ Limpia TODOS los datos temporales (esto faltaba)
                        UsuarioTemporal.correo = null
                        UsuarioTemporal.nombre = null
                        UsuarioTemporal.fechaNacimiento = null
                        UsuarioTemporal.apellido = null
                        UsuarioTemporal.fotoUrl = null
                        UsuarioTemporal.fotoLocalUri = null

                        // ✅ Limpia la imagen guardada en SharedPreferences
                        ImageStorage.eliminarImagen(context)

                        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT)
                            .show()

                        // Navegar al inicio de la app
                        navController.navigate("ingreso_usuario") {
                            popUpTo("perfil_ajustes") { inclusive = true }
                        }
                    }
                )

                // Botón "No"
                CustomButton(
                    text = "No",
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        navController.navigate("perfil_ajustes")
                    }
                )
            }
        }
    }
}
