package com.example.myway.screens.modulo1

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTextField
import com.example.myway.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore

object UsuarioTemporal {
    var correo: String = ""
}

@Composable
fun NuevaContraseña(navController: NavController, correo: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
        Image(
            painter = painterResource(id = R.drawable.flecha),
            contentDescription = "Volver",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )

        // Contenido principal con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()), // ← habilita el scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Nueva Contraseña",
                fontSize = 26.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ingresa la nueva contraseña",
                fontSize = 16.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                color = Blanco
            )

            CustomTextField(
                placeholder = "Mín. 8 caracteres, 1 mayús, 1 minús, 1 número y 1 símbolo*",
                text = nueva,
                onTextChange = { nueva = it },
                color = Blanco,
                textColor = Azul2,
                isPassword = true,
                showBorder = false

            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Confirma la contraseña",
                fontSize = 16.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                color = Blanco,

            )

            CustomTextField(
                placeholder = "***********",
                text = confirmar,
                onTextChange = { confirmar = it },
                color = Blanco,
                textColor = Azul2,
                isPassword = true,
                showBorder = false
            )

            Spacer(modifier = Modifier.height(30.dp))

            CustomButton(
                text = "Actualizar",
                color = Azul3,
                modifier = Modifier
                    .width(220.dp)
                    .height(55.dp),
                onClick = {
                    // Validaciones
                    when {
                        nueva.isBlank() || confirmar.isBlank() -> {
                            Toast.makeText(context, "Rellena ambos campos", Toast.LENGTH_SHORT).show()
                            return@CustomButton
                        }
                        nueva != confirmar -> {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@CustomButton
                        }
                        !esContrasenaValida(nueva) -> {
                            Toast.makeText(
                                context,
                                "Debe tener 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@CustomButton
                        }
                    }

                    // Actualización en Firestore
                    db.collection("usuarios")
                        .whereEqualTo("correo", correo)
                        .get()
                        .addOnSuccessListener { docs ->
                            if (!docs.isEmpty) {
                                val id = docs.documents[0].id
                                db.collection("usuarios").document(id)
                                    .update("contrasena", nueva)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Contraseña actualizada correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("cambio_exitoso")
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error en la base de datos", Toast.LENGTH_SHORT).show()
                        }
                }
            )
        }
    }
}

// 🔒 Validación de contraseña segura
fun esContrasenaValida(password: String): Boolean {
    val regex = Regex(
        "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{8,}\$"
    )
    return regex.matches(password)
}
