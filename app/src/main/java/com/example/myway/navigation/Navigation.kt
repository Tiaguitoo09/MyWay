package com.example.myway.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myway.screens.*
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun MyWayAppNavigation(
    navController: NavHostController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") {
            InicioPantalla(navController)
        }

        composable("ingreso_usuario") {
            IngresoUsuario(
                navController = navController,
                auth = auth,
                googleSignInClient = googleSignInClient
            )
        }

        composable("registro_usuario") {
            RegistroUsuario(
                navController = navController,
                auth = auth,
                googleSignInClient = googleSignInClient
            )
        }

        composable("olvide_contraseña") {
            OlvidoContraseña(
                navController = navController,
                auth = auth,
                googleSignInClient = googleSignInClient
            )
        }

        composable("nueva_contraseña/{correo}") { backStackEntry ->
            val correo = backStackEntry.arguments?.getString("correo") ?: ""
            NuevaContraseña(navController, correo)
        }

        composable("cambio_exitoso") {
            CambioExitoso(navController = navController)
        }


        composable("cargando") {
            Cargando(navController)
        }


        composable("home") {
            Home(navController)
        }
    }
}
