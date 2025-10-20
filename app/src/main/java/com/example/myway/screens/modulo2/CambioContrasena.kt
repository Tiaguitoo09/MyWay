package com.example.myway.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.myway.ui.theme.*
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CambioContrasena(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    val currentUser = auth.currentUser
    val isGoogleUser = currentUser?.providerData?.any { it.providerId == "google.com" } == true

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Cambiar Contraseña",
                fontSize = 26.sp,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isGoogleUser) {
                // Usuario de Google
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tu cuenta está vinculada con Google.\n" +
                                "No puedes cambiar tu contraseña aquí.\n" +
                                "Hazlo desde la configuración de tu cuenta de Google.",
                        fontSize = 18.sp,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        color = Blanco,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            } else {
                // Usuario con correo/contraseña
                Text(
                    text = "Contraseña actual",
                    fontSize = 16.sp,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    color = Blanco
                )
                CustomTextField(
                    placeholder = "***********",
                    text = actual,
                    onTextChange = { actual = it },
                    color = Blanco,
                    textColor = Azul2,
                    isPassword = true,
                    showBorder = false
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Nueva contraseña",
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
                    color = Blanco
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
                        .height(45.dp),
                    onClick = {
                        val correo = UsuarioTemporal.correo

                        // Validaciones
                        when {
                            actual.isBlank() || nueva.isBlank() || confirmar.isBlank() -> {
                                Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                                return@CustomButton
                            }
                            nueva != confirmar -> {
                                Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                return@CustomButton
                            }
                            nueva.length < 6 -> {
                                Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                return@CustomButton
                            }
                        }

                        // ✅ Cambiar contraseña con FirebaseAuth
                        val user = auth.currentUser
                        if (user != null && correo != null) {
                            val credential = EmailAuthProvider.getCredential(correo, actual)

                            // Verificar contraseña actual
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    // Contraseña actual correcta, actualizar
                                    user.updatePassword(nueva)
                                        .addOnSuccessListener {
                                            // También actualizar en Firestore (opcional)
                                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            db.collection("usuarios").document(user.uid)
                                                .update("contrasena", nueva)

                                            Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                                            navController.navigate("cambio_exitoso") {
                                                popUpTo("cambio_contrasena") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            }
        }
    }
}