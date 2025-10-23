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
import com.example.myway.screens.modulo2.*
import com.example.myway.screens.modulo3.*
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun MyWayAppNavigation(
    navController: NavHostController,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    NavHost(navController = navController, startDestination = "inicio") {

        // 🏁 Pantallas de autenticación
        composable("inicio") { InicioPantalla(navController) }

        composable("ingreso_usuario") {
            IngresoUsuario(navController, auth, googleSignInClient)
        }

        composable("registro_usuario") {
            RegistroUsuario(navController, auth, googleSignInClient)
        }

        composable("olvide_contraseña") {
            OlvidoContraseña(navController, auth, googleSignInClient)
        }

        composable("cambio_exitoso") { CambioExitoso(navController) }

        // ⏳ Pantalla de carga
        composable("cargando") { Cargando(navController) }

        // 🗺️ HOME - Mapa principal sin parámetros
        composable("home") {
            Home(
                navController = navController,
                placeId = null,
                placeName = null,
                placeType = null
            )
        }

        // 🗺️ HOME - Mostrar lugares por tipo (restaurantes, hoteles, etc.)
        composable(
            route = "home/{placeType}",
            arguments = listOf(
                navArgument("placeType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val placeType = backStackEntry.arguments?.getString("placeType")
            Home(
                navController = navController,
                placeType = placeType,
                placeId = null,
                placeName = null
            )
        }

        // 🗺️ HOME - Con destino específico
        composable(
            route = "home/{placeId}/{placeName}",
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
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
                placeName = placeName,
                placeType = null
            )
        }

        // 👤 Perfil y configuración
        composable("perfil_ajustes") { PerfilAjustes(navController) }
        composable("eliminar_cuenta") { EliminarCuenta(navController) }
        composable("cerrar_sesion") { CerrarSesion(navController) }
        composable("ver_perfil") { VerPerfil(navController) }
        composable("cambio_contraseña") { CambioContrasena(navController) }
        composable("ajustes") { Ajustes(navController) }
        composable("soporte") { Soporte(navController) }
        composable("silenciar_notificaciones") { SilenciarNotificaciones(navController) }

        // 🚗 MODO COPILOTO - uso seguro mientras conduces
        composable("modo_copiloto") {
            ModoCopiloto(navController = navController)
        }

        // 🔐 PERMISOS - Gestión de permisos
        composable("permisos") {
            Permisos(navController = navController)
        }

        // 🔍 PLANEA VIAJE - Búsqueda de destinos
        composable("planea_viaje") {
            PlaneaViaje(navController = navController)
        }

        // 🚗 RUTA OPCIONES - Seleccionar tipo de transporte
        composable(
            route = "ruta_opciones/{placeId}/{placeName}",
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
                navArgument("placeName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
            val placeName = backStackEntry.arguments?.getString("placeName")
            RutaOpciones(navController, placeId, placeName)
        }

        // 🧭 NAVEGACIÓN ACTIVA - Guía paso a paso
        composable(
            route = "navegacion_activa/{placeId}/{placeName}/{transportMode}",
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
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
            NavegacionActiva(navController, placeId, placeName, transportMode)
        }

        // ⭐ Favoritos y guardados
        composable("guardados") { Guardados(navController) }
        composable("favoritos") { Favoritos(navController) }

        // 📍 Detalles de lugar
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
