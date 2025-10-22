package com.example.myway.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myway.screens.CambioContrasena
import com.example.myway.screens.modulo1.CambioExitoso
import com.example.myway.screens.modulo1.IngresoUsuario
import com.example.myway.screens.modulo1.InicioPantalla
import com.example.myway.screens.modulo1.OlvidoContraseña
import com.example.myway.screens.modulo1.RegistroUsuario
import com.example.myway.screens.modulo2.Cargando
import com.example.myway.screens.modulo2.CerrarSesion
import com.example.myway.screens.modulo2.EliminarCuenta
import com.example.myway.screens.modulo2.Home
import com.example.myway.screens.modulo2.PerfilAjustes
import com.example.myway.screens.modulo2.Ajustes
import com.example.myway.screens.modulo2.Soporte
import com.example.myway.screens.modulo2.VerPerfil
import com.example.myway.screens.modulo2.SilenciarNotificaciones
import com.example.myway.screens.modulo3.DetallesLugar
import com.example.myway.screens.modulo3.Favoritos
import com.example.myway.screens.modulo3.PlaneaViaje
import com.example.myway.screens.modulo3.Guardados
import com.example.myway.screens.modulo3.RutaOpciones
import com.example.myway.screens.modulo3.NavegacionActiva
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

        composable("cambio_exitoso") {
            CambioExitoso(navController = navController)
        }

        composable("cargando") {
            Cargando(navController)
        }

        // 🗺️ HOME - Pantalla principal del mapa (sin parámetros)
        composable("home") {
            Home(
                navController = navController,
                placeId = null,
                placeName = null
            )
        }

        // 🗺️ HOME - Con destino seleccionado (con parámetros)
        composable(
            route = "home/{placeId}/{placeName}",
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.StringType
                },
                navArgument("placeName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
            val placeName = backStackEntry.arguments?.getString("placeName")
            Home(
                navController = navController,
                placeId = placeId,
                placeName = placeName
            )
        }

        composable("perfil_ajustes") {
            PerfilAjustes(navController = navController)
        }

        composable("eliminar_cuenta") {
            EliminarCuenta(navController = navController)
        }

        composable("cerrar_sesion") {
            CerrarSesion(navController = navController)
        }

        composable("ver_perfil") {
            VerPerfil(navController = navController)
        }

        composable("cambio_contraseña") {
            CambioContrasena(navController = navController)
        }

        composable("ajustes") {
            Ajustes(navController = navController)
        }

        composable("soporte") {
            Soporte(navController = navController)
        }

        composable("silenciar_notificaciones") {
            SilenciarNotificaciones(navController = navController)
        }

        // 🔍 PLANEA VIAJE - Búsqueda de destinos
        composable("planea_viaje") {
            PlaneaViaje(navController = navController)
        }

        // 🚗 RUTA OPCIONES - Seleccionar tipo de transporte
        composable(
            route = "ruta_opciones/{placeId}/{placeName}",
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.StringType
                },
                navArgument("placeName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
            val placeName = backStackEntry.arguments?.getString("placeName")
            RutaOpciones(
                navController = navController,
                placeId = placeId,
                placeName = placeName
            )
        }

        // 🧭 NAVEGACIÓN ACTIVA - Guía paso a paso
        composable(
            route = "navegacion_activa/{placeId}/{placeName}/{transportMode}",
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.StringType
                },
                navArgument("placeName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("transportMode") {
                    type = NavType.StringType
                    defaultValue = "driving"
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
            val placeName = backStackEntry.arguments?.getString("placeName")
            val transportMode = backStackEntry.arguments?.getString("transportMode")
            NavegacionActiva(
                navController = navController,
                placeId = placeId,
                placeName = placeName,
                transportMode = transportMode
            )
        }

        composable("guardados") {
            Guardados(navController = navController)
        }

        composable("favoritos") {
            Favoritos(navController = navController)
        }

        composable(
            route = "detalles_lugar/{placeId}/{placeName}",
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
                navArgument("placeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            DetallesLugar(
                navController = navController,
                placeId = backStackEntry.arguments?.getString("placeId"),
                placeName = backStackEntry.arguments?.getString("placeName")
            )
        }
    }
}