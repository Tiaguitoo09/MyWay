package com.example.myway.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myway.screens.InicioPantalla
import com.example.myway.screens.IngresoUsuario
import com.example.myway.screens.OlvidoContraseña
import com.example.myway.screens.RegistroUsuario
import com.example.myway.screens.NuevaContraseña
import com.example.myway.screens.CambioExitoso
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

        // ✅ Aquí agregamos auth y googleSignInClient también
        composable("registro_usuario") {
            RegistroUsuario(
                navController = navController,
                auth = auth,
                googleSignInClient = googleSignInClient
            )
        }

        composable("olvide_contraseña") {
            OlvidoContraseña(navController = navController, auth=auth)
        }
        composable("nueva_contraseña/{correo}") { backStackEntry ->
            val correo = backStackEntry.arguments?.getString("correo") ?: ""
            NuevaContraseña(navController, correo)
        }
        composable("cambio_exitoso") {
            CambioExitoso(navController = navController)
        }
    }
}
