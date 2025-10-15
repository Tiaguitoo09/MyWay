package com.example.myway.screens.modulo1

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.myway.ui.theme.Azul2
import com.example.myway.utils.UsuarioTemporal
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Negro
import com.example.myway.ui.theme.Nunito
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun OlvidoContraseña(
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    var fraseSeguridad by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Launcher para Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            scope.launch {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            Toast.makeText(context, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                            navController.navigate("inicio") {
                                popUpTo("olvide_contraseña") { inclusive = true }
                            }
                        } else {
                            signInTask.exception?.printStackTrace()
                            Toast.makeText(context, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al autenticar con Google", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

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

        // 👇 Aquí se agregó el scroll vertical
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()), // ← habilita scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Olvidé mi contraseña",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ingrese su frase de seguridad",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Blanco
                )

                // Campo de frase
                CustomTextField(
                    placeholder = "Frase de seguridad",
                    text = fraseSeguridad,
                    onTextChange = { fraseSeguridad = it },
                    color = Blanco,
                    textColor = Azul2,
                    isPassword = false,
                    showBorder = false
                )


                // Botón enviar
                CustomButton(
                    text = "Enviar",
                    color = Azul3,
                    modifier = Modifier
                        .width(220.dp)
                        .height(55.dp),
                    onClick = {
                        if (fraseSeguridad.isNotEmpty()) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("usuarios")
                                .whereEqualTo("fraseSeguridad", fraseSeguridad)
                                .get()
                                .addOnSuccessListener { documentos ->
                                    if (!documentos.isEmpty) {
                                        val usuario = documentos.documents.first()
                                        val correo = usuario.getString("correo") ?: ""

                                        if (correo.isNotEmpty()) {
                                            UsuarioTemporal.correo = correo
                                            Toast.makeText(
                                                context,
                                                "Frase válida. Redirigiendo...",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("nueva_contraseña/${correo}")
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Correo no encontrado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Frase incorrecta o no registrada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error al buscar la frase",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                context,
                                "Ingresa tu frase de seguridad",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Imagen Google con inicio de sesión
                Image(
                    painter = painterResource(id = R.drawable.google_image),
                    contentDescription = "Google",
                    modifier = Modifier
                        .size(90.dp)
                        .clickable(enabled = !isLoading) {
                            isLoading = true
                            googleSignInClient.signOut()
                            auth.signOut()
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ¿Tienes una cuenta?
                Text(
                    text = "¿Tienes una cuenta?",
                    color = Blanco,
                    fontSize = 14.sp,
                    fontFamily = Nunito
                )

                // Botón Iniciar sesión
                CustomButton(
                    text = "Iniciar Sesión",
                    color = Azul3,
                    modifier = Modifier
                        .width(220.dp)
                        .height(55.dp),
                    onClick = {
                        navController.navigate("ingreso_usuario")
                    }
                )
            }
        }
    }
}
