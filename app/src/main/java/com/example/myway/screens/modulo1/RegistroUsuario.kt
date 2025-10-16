package com.example.myway.screens.modulo1

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.myway.utils.UsuarioTemporal

@Composable
fun RegistroUsuario(
    navController: NavController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verificarContrasena by remember { mutableStateOf("") }
    var fraseSeguridad by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var mes by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }

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
        Image(
            painter = painterResource(id = R.drawable.registro),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.flechaazul),
            contentDescription = stringResource(R.string.volver),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            CampoTextoAzul(stringResource(R.string.campo_nombre), nombre) { nombre = it }
            CampoTextoAzul(stringResource(R.string.campo_apellido), apellido) { apellido = it }
            CampoTextoAzul(stringResource(R.string.campo_correo), correo) { correo = it }
            CampoTextoAzul(stringResource(R.string.campo_contrasena), contrasena, true) { contrasena = it }
            CampoTextoAzul(stringResource(R.string.campo_verificar_contrasena), verificarContrasena, true) { verificarContrasena = it }
            CampoTextoAzul(stringResource(R.string.campo_frase_seguridad), fraseSeguridad) { fraseSeguridad = it }

            Text(
                text = stringResource(R.string.fecha_nacimiento),
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
                CampoFecha(stringResource(R.string.dia), dia) { dia = it }
                CampoFecha(stringResource(R.string.mes), mes) { mes = it }
                CampoFecha(stringResource(R.string.anio), anio) { anio = it }
            }

            errorFecha?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.genero),
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
                        contentDescription = stringResource(R.string.masculino),
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
                        contentDescription = stringResource(R.string.femenino),
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
                        contentDescription = stringResource(R.string.no_binario),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            CustomButton(
                text = "Continuar",
                color = Azul3,
                onClick = {
                    if (contrasena != verificarContrasena) {
                        Toast.makeText(context,
                            context.getString(R.string.toast_contrasenas_no_coinciden), Toast.LENGTH_SHORT).show()
                        return@CustomButton
                    }

                    if (nombre.isBlank() || apellido.isBlank() || correo.isBlank() ||
                        contrasena.isBlank() || verificarContrasena.isBlank() ||
                        fraseSeguridad.isBlank() || dia.isBlank() || mes.isBlank() || anio.isBlank() ||
                        genero.isNullOrBlank()
                    ) {
                        Toast.makeText(context,context.getString(R.string.toast_campos_incompletos), Toast.LENGTH_SHORT).show()
                        return@CustomButton
                    }

                    errorFecha = validarFechaNacimiento(context,dia, mes, anio)
                    if (errorFecha == null) {
                        val fechaNacimiento = "$dia/$mes/$anio"
                        guardarUsuarioEnFirestore(
                            auth = auth,
                            navController = navController,
                            nombre = nombre,
                            apellido = apellido,
                            correo = correo,
                            contrasena = contrasena,
                            fechaNacimiento = fechaNacimiento,
                            genero = genero,
                            fraseSeguridad = fraseSeguridad,
                            context = context
                        )
                    } else {
                        Toast.makeText(context, errorFecha, Toast.LENGTH_SHORT).show()
                    }

                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isLoading)
                        stringResource(R.string.google_creando_cuenta)
                    else
                        stringResource(R.string.google_registrarse),
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
fun guardarUsuarioEnFirestore(
    auth: FirebaseAuth,
    navController: NavController,
    nombre: String,
    apellido: String,
    correo: String,
    contrasena: String,
    fechaNacimiento: String,
    genero: String?,
    fraseSeguridad: String,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("usuarios")
        .whereEqualTo("correo", correo)
        .get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_correo_repetido),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val userId = auth.currentUser?.uid ?: db.collection("usuarios").document().id
                val usuarioData = hashMapOf(
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "correo" to correo,
                    "contrasena" to contrasena,
                    "fechaNacimiento" to fechaNacimiento,
                    "genero" to genero,
                    "fraseSeguridad" to fraseSeguridad,
                    "fechaRegistro" to System.currentTimeMillis()
                )

                db.collection("usuarios").document(userId)
                    .set(usuarioData)
                    .addOnSuccessListener {
                        UsuarioTemporal.correo = correo
                        UsuarioTemporal.nombre = nombre

                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_cuenta_creada),
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate("cargando") {
                            popUpTo("registro_usuario") { inclusive = true }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_error_guardar_usuario, e.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }

            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(
                context,
                context.getString(R.string.error_verificar_correo, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
}

// ---------------------------------------------------------------------
fun validarFechaNacimiento(context: Context, dia: String, mes: String, anio: String): String? {
    if (dia.isBlank() || mes.isBlank() || anio.isBlank())
        return context.getString(R.string.error_fecha_incompleta)

    val d = dia.toIntOrNull() ?: return context.getString(R.string.error_dia_invalido)
    val m = mes.toIntOrNull() ?: return context.getString(R.string.error_mes_invalido)
    val a = anio.toIntOrNull() ?: return context.getString(R.string.error_anio_invalido, 2025)

    val MAX_ANIO = 2025
    if (m !in 1..12) return context.getString(R.string.error_mes_invalido)
    if (a !in 1900..MAX_ANIO)
        return context.getString(R.string.error_anio_invalido, MAX_ANIO)

    val diasEnMes = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (esBisiesto(a)) 29 else 28
        else -> 0
    }

    if (d !in 1..diasEnMes)
        return context.getString(R.string.error_dias_mes, m, diasEnMes)

    return null
}


fun esBisiesto(anio: Int): Boolean {
    return (anio % 4 == 0 && (anio % 100 != 0 || anio % 400 == 0))
}

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
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
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
fun CampoFecha(placeholder: String, valor: String, onChange: (String) -> Unit) {
    val MAX_ANIO = 2026
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevoValor ->
            if (nuevoValor.all { it.isDigit() }) {
                when (placeholder.lowercase()) {
                    "día" -> if (nuevoValor.length <= 2) {
                        val num = nuevoValor.toIntOrNull()
                        if (num == null || num in 1..31) onChange(nuevoValor)
                    }

                    "mes" -> if (nuevoValor.length <= 2) {
                        val num = nuevoValor.toIntOrNull()
                        if (num == null || num in 1..12) onChange(nuevoValor)
                    }

                    "año" -> if (nuevoValor.length <= 4) {
                        val num = nuevoValor.toIntOrNull()
                        if (num == null || nuevoValor.length < 4 || num in 1900..MAX_ANIO) {
                            onChange(nuevoValor)
                        }
                    }
                }
            }
        },
        placeholder = {
            Text(
                placeholder,
                color = Azul3,
                textAlign = TextAlign.Center
            )
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
