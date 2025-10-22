package com.example.myway.screens.modulo2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTitleText
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Azul4
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Rojo
import com.example.myway.ui.theme.Verde
import com.example.myway.utils.UsuarioTemporal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

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


