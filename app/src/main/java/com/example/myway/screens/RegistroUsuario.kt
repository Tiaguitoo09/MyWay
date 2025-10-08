package com.example.myway.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun RegistroUsuario(
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // 🔹 Lanzador del Intent de Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            // 🔹 Registrar o iniciar sesión con Firebase
            scope.launch {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            // Si el usuario no existía, Firebase lo crea automáticamente
                            navController.navigate("inicio") {
                                popUpTo("registro_usuario") { inclusive = true }
                            }
                        } else {
                            signInTask.exception?.printStackTrace()
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.registro),
            contentDescription = "Fondo de registro",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Flecha volver
        Image(
            painter = painterResource(id = R.drawable.flechaazul),
            contentDescription = "Volver",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var nombre by remember { mutableStateOf("") }
            var apellido by remember { mutableStateOf("") }
            var correo by remember { mutableStateOf("") }
            var contrasena by remember { mutableStateOf("") }
            var verificarContrasena by remember { mutableStateOf("") }
            var dia by remember { mutableStateOf("") }
            var mes by remember { mutableStateOf("") }
            var anio by remember { mutableStateOf("") }
            var genero by remember { mutableStateOf<String?>(null) }

            Spacer(modifier = Modifier.height(40.dp))

            // Campos de texto
            CampoTextoAzul("Nombre", nombre) { nombre = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Apellido", apellido) { apellido = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Correo electrónico", correo) { correo = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Contraseña", contrasena, true) { contrasena = it }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTextoAzul("Verificar contraseña", verificarContrasena, true) { verificarContrasena = it }

            Spacer(modifier = Modifier.height(20.dp))

            // Fecha de nacimiento
            Text(
                text = "Fecha de nacimiento",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CampoFecha("Día", dia) { dia = it }
                CampoFecha("Mes", mes) { mes = it }
                CampoFecha("Año", anio) { anio = it }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Género
            Text(
                text = "Género",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Masculino
                Box(
                    modifier = Modifier
                        .size(90.dp, 55.dp)
                        .background(
                            if (genero == "masculino") Azul4.copy(alpha = 0.4f) else Blanco,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(2.dp, Azul3, shape = RoundedCornerShape(20.dp))
                        .clickable { genero = "masculino" },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icono_masculino),
                        contentDescription = "Masculino",
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Femenino
                Box(
                    modifier = Modifier
                        .size(90.dp, 55.dp)
                        .background(
                            if (genero == "femenino") Color(0xFFFFC0CB).copy(alpha = 0.4f) else Blanco,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(2.dp, Azul3, shape = RoundedCornerShape(20.dp))
                        .clickable { genero = "femenino" },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icono_femenino),
                        contentDescription = "Femenino",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Botón de registro manual
            CustomButton(
                text = "Continuar",
                color = Azul3,
                onClick = {
                    // Aquí luego agregaremos validación y registro manual (email/password)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Botón funcional de Google
            Text(
                text = if (isLoading) "Creando cuenta con Google..." else "Registrarse con Google",
                color = Blanco, // <- cambia Azul1 por Blanco
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
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



// Reutilizable: Campo con fondo blanco, texto azul y esquinas redondeadas
@Composable
fun CampoTextoAzul(
    placeholder: String,
    valor: String,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    androidx.compose.material3.OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = Azul3) },
        singleLine = true,
        textStyle = TextStyle(color = Azul3),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier
            .width(320.dp)
            .height(55.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Azul3,
            unfocusedBorderColor = Azul3,
            cursorColor = Azul3,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

// Campo de fecha pequeño (día, mes, año)
@Composable
fun CampoFecha(
    placeholder: String,
    valor: String,
    onChange: (String) -> Unit
) {
    androidx.compose.material3.OutlinedTextField(
        value = valor,
        onValueChange = {
            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                onChange(it)
            }
        },
        placeholder = { Text(placeholder, color = Azul3, textAlign = TextAlign.Center) },
        singleLine = true,
        textStyle = TextStyle(color = Azul3, textAlign = TextAlign.Center),
        modifier = Modifier
            .width(90.dp)
            .height(55.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Azul3,
            unfocusedBorderColor = Azul3,
            cursorColor = Azul3,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco
        ),
        shape = RoundedCornerShape(20.dp)
    )
}
