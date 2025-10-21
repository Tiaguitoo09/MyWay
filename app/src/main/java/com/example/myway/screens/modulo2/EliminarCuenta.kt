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
import androidx.compose.ui.res.stringResource
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
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EliminarCuenta(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de la app
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = stringResource(id = R.string.fondo_app),
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
                contentDescription = stringResource(id = R.string.confirmacion),
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texto principal
            CustomTitleText(
                text = stringResource(id = R.string.seguro_eliminar_cuenta),
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
                //Botón Sí
                CustomButton(
                    text = stringResource(id = R.string.si),
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        val usuario = auth.currentUser
                        val correoUsuario = UsuarioTemporal.correo

                        if (usuario != null && correoUsuario != null) {
                            // Eliminar documento de Firestore
                            db.collection("usuarios")
                                .whereEqualTo("correo", correoUsuario)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (doc in documents) {
                                        db.collection("usuarios").document(doc.id).delete()
                                    }
                                    // Luego eliminar de Authentication
                                    usuario.delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.cuenta_eliminada),
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Limpia los datos temporales
                                            UsuarioTemporal.correo = ""
                                            UsuarioTemporal.nombre = ""

                                            // Navegar al ingreso
                                            navController.navigate("inicio") {
                                                popUpTo("perfil_ajustes") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.error_eliminar_cuenta, e.message),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.error_eliminar_usuario, e.message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.no_info_usuario),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

                // Botón No
                CustomButton(
                    text = stringResource(id = R.string.no),
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