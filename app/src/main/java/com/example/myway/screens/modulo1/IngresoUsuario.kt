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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTextField
import com.example.myway.ui.theme.Azul3
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.utils.UsuarioTemporal
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun IngresoUsuario(
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    // Google launcher
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
                            navController.navigate("cargando") {
                                popUpTo("ingreso_usuario") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al autenticar", Toast.LENGTH_SHORT).show()
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

        // Contenido con scroll
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())   // 👈 Habilita desplazamiento vertical
                .fillMaxWidth()
                .padding(vertical = 40.dp),              // 👈 Espacio arriba/abajo opcional
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = "Ícono de brújula",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MyWay",
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Nunito,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs
            CustomTextField(
                placeholder = "Correo electrónico",
                color = Azul3,
                textColor = Blanco,
                text = email.value,
                onTextChange = { email.value = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                placeholder = "Contraseña",
                color = Azul3,
                textColor = Blanco,
                isPassword = true,
                text = password.value,
                onTextChange = { password.value = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Texto de olvido contraseña
            Text(
                text = "Olvidé mi contraseña",
                color = Blanco,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    navController.navigate("olvide_contraseña")
                }
            )

            Spacer(modifier = Modifier.height(34.dp))

            // Botones principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón ingresar
                CustomButton(
                    text = "Ingresar",
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        if (email.value.isNotEmpty() && password.value.isNotEmpty()) {
                            val db = FirebaseFirestore.getInstance()

                            db.collection("usuarios")
                                .whereEqualTo("correo", email.value)
                                .whereEqualTo("contrasena", password.value)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        val userDoc = documents.documents[0]
                                        UsuarioTemporal.correo = email.value
                                        UsuarioTemporal.nombre = userDoc.getString("nombre") ?: "Usuario"

                                        Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                        navController.navigate("cargando") {
                                            popUpTo("ingreso_usuario") { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Botón registrarse
                CustomButton(
                    text = "Registrarse",
                    color = Azul3,
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        navController.navigate("registro_usuario")
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google login
            Text(
                text = if (isLoading) "Iniciando sesión..." else "Iniciar sesión con Google",
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    isLoading = true
                    googleSignInClient.signOut()
                    auth.signOut()
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            )
        }
    }
}
