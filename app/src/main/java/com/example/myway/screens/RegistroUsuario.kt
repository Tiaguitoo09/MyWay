package com.example.myway.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun RegistroUsuario(
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Instancia de Firestore
    val db = FirebaseFirestore.getInstance()

    // Variables de usuario
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verificarContrasena by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var mes by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }

    // Google Sign-In launcher
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

            CampoTextoAzul("Verificar contraseña", verificarContrasena, true) {
                verificarContrasena = it
            }

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

            errorFecha?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, color = Color.Red, fontSize = 14.sp)
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

                Box(
                    modifier = Modifier
                        .size(90.dp, 55.dp)
                        .background(
                            if (genero == "femenino") PurpuraSua.copy(alpha = 0.5f) else Blanco,
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

                Box(
                    modifier = Modifier
                        .size(90.dp, 55.dp)
                        .background(
                            if (genero == "no_binario") Rosado.copy(alpha = 0.5f) else Blanco,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(2.dp, Azul3, shape = RoundedCornerShape(20.dp))
                        .clickable { genero = "no_binario" },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icono_no_binario),
                        contentDescription = "No binario",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Botón continuar
            CustomButton(
                text = "Continuar",
                color = Azul3,
                onClick = {
                    errorFecha = validarFechaNacimiento(dia, mes, anio)
                    if (errorFecha == null) {
                        guardarUsuarioEnFirestore(
                            db,
                            nombre,
                            apellido,
                            correo,
                            contrasena,
                            dia,
                            mes,
                            anio,
                            genero
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign-In
            Text(
                text = if (isLoading) "Creando cuenta con Google..." else "Registrarse con Google",
                color = Blanco,
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

// ---------------------------------------------------------------------
// Funciones de utilidad
// ---------------------------------------------------------------------

fun validarFechaNacimiento(dia: String, mes: String, anio: String): String? {
    if (dia.isBlank() || mes.isBlank() || anio.isBlank()) return "Por favor completa la fecha"

    val d = dia.toIntOrNull() ?: return "Día inválido"
    val m = mes.toIntOrNull() ?: return "Mes inválido"
    val a = anio.toIntOrNull() ?: return "Año inválido"

    val MAX_ANIO = 2025
    if (m !in 1..12) return "Mes inválido"
    if (a !in 1900..MAX_ANIO) return "Año inválido (debe estar entre 1900 y $MAX_ANIO)"

    val diasEnMes = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (esBisiesto(a)) 29 else 28
        else -> 0
    }

    if (d !in 1..diasEnMes) return "El mes $m solo tiene $diasEnMes días"

    return null
}

fun esBisiesto(anio: Int): Boolean {
    return (anio % 4 == 0 && (anio % 100 != 0 || anio % 400 == 0))
}

// ---------------------------------------------------------------------
// 🔹 NUEVA FUNCIÓN PARA GUARDAR USUARIO EN FIRESTORE
// ---------------------------------------------------------------------

fun guardarUsuarioEnFirestore(
    db: FirebaseFirestore,
    nombre: String,
    apellido: String,
    correo: String,
    contrasena: String,
    dia: String,
    mes: String,
    anio: String,
    genero: String?
) {
    val usuario = hashMapOf(
        "nombre" to nombre,
        "apellido" to apellido,
        "correo" to correo,
        "contrasena" to contrasena, // ⚠️ Idealmente NO guardar en texto plano
        "fechaNacimiento" to "$dia/$mes/$anio",
        "genero" to (genero ?: "No especificado"),
        "fechaRegistro" to System.currentTimeMillis()
    )

    db.collection("usuarios")
        .add(usuario)
        .addOnSuccessListener { documentRef ->
            println("✅ Usuario guardado con ID: ${documentRef.id}")
        }
        .addOnFailureListener { e ->
            println("❌ Error al guardar usuario: ${e.message}")
        }
}

// ---------------------------------------------------------------------
// Componentes Componibles
// ---------------------------------------------------------------------

@Composable
fun CampoTextoAzul(
    placeholder: String,
    valor: String,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = Azul3) },
        singleLine = true,
        textStyle = TextStyle(color = Azul3),
        visualTransformation = if (isPassword)
            PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier
            .width(320.dp)
            .height(55.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Azul3,
            unfocusedBorderColor = Azul3,
            cursorColor = Azul3,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun CampoFecha(
    placeholder: String,
    valor: String,
    onChange: (String) -> Unit
) {
    val MAX_ANIO = 2026
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevoValor ->
            if (nuevoValor.all { it.isDigit() }) {
                when (placeholder.lowercase()) {
                    "día" -> {
                        if (nuevoValor.length <= 2) {
                            val num = nuevoValor.toIntOrNull()
                            if (num == null || num in 1..31) onChange(nuevoValor)
                        }
                    }

                    "mes" -> {
                        if (nuevoValor.length <= 2) {
                            val num = nuevoValor.toIntOrNull()
                            if (num == null || num in 1..12) onChange(nuevoValor)
                        }
                    }

                    "año" -> {
                        if (nuevoValor.length <= 4) {
                            val num = nuevoValor.toIntOrNull()
                            if (num == null || nuevoValor.length < 4 || num in 1900..MAX_ANIO) {
                                onChange(nuevoValor)
                            }
                        }
                    }
                }
            }
        },
        placeholder = {
            Text(placeholder, color = Azul3, textAlign = TextAlign.Center)
        },
        singleLine = true,
        textStyle = TextStyle(color = Azul3, textAlign = TextAlign.Center),
        modifier = Modifier
            .width(90.dp)
            .height(55.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Azul3,
            unfocusedBorderColor = Azul3,
            cursorColor = Azul3,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco
        ),
        shape = RoundedCornerShape(20.dp)
    )
}
