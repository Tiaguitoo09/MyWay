package com.example.myway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.myway.ai.AIRepository
import com.example.myway.utils.MyWayAppNavigation
import com.example.myway.ui.theme.MyWayTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : ComponentActivity() {

    // Variables de autenticación
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // 🔹 Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // 🔹 Crear cliente de inicio de sesión de Google
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 🔹 Composición principal
        setContent {
            MyWayTheme {
                App(auth, googleSignInClient)
            }
        }
    }
}

@Composable
fun App(
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 🧹 Limpiar caché expirado al iniciar la app
    LaunchedEffect(Unit) {
        try {
            val repository = AIRepository(context)
            repository.cleanExpiredCache()
        } catch (e: Exception) {
            // Manejo silencioso, no afecta la funcionalidad
        }
    }

    MyWayAppNavigation(
        navController = navController,
        auth = auth,
        googleSignInClient = googleSignInClient
    )
}