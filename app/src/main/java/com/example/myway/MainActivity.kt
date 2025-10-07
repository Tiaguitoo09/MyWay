package com.example.myway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.myway.navigation.MyWayAppNavigation
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
            // Este ID se genera automáticamente al agregar Firebase a tu proyecto
            // y está en el archivo google-services.json
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
    MyWayAppNavigation(
        navController = navController,
        auth = auth,
        googleSignInClient = googleSignInClient
    )
}
