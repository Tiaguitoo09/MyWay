package com.example.myway.utils

import android.net.Uri
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
import com.example.myway.screens.modulo4.Recomiendame
import com.example.myway.screens.modulo4.SinPlan
import com.example.myway.screens.modulo4.TuMood
import com.example.myway.screens.modulo4.RankingLugares
import com.example.myway.screens.modulo5.CrearPlan
import com.example.myway.screens.modulo5.EliminarPlan
import com.example.myway.screens.modulo5.PlanesViaje
import com.example.myway.screens.modulo5.ViajesGuardados
import com.example.myway.screens.modulo5.Itinerario
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.example.myway.screens.modulo5.VerPlan

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


        composable("cargando") { Cargando(navController) }


        composable("home") {
            Home(
                navController = navController,
                placeId = null,
                placeName = null,
                placeType = null
            )
        }

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


        composable("perfil_ajustes") { PerfilAjustes(navController) }
        composable("eliminar_cuenta") { EliminarCuenta(navController) }
        composable("cerrar_sesion") { CerrarSesion(navController) }
        composable("ver_perfil") { VerPerfil(navController) }
        composable("cambio_contraseña") { CambioContrasena(navController) }
        composable("ajustes") { Ajustes(navController) }
        composable("soporte") { Soporte(navController) }
        composable("silenciar_notificaciones") { SilenciarNotificaciones(navController) }


        composable("modo_copiloto") {
            ModoCopiloto(navController = navController)
        }


        composable("permisos") {
            Permisos(navController = navController)
        }


        composable("planea_viaje") {
            PlaneaViaje(navController = navController)
        }


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


        composable("guardados") { Guardados(navController) }
        composable("favoritos") { Favoritos(navController) }


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

        composable("sin_plan") {
            SinPlan(navController = navController)
        }

        composable("recomiendame") {
            Recomiendame(navController = navController)
        }

        composable("tu_mood") {
            TuMood(navController = navController)
        }

        composable("preferencias_viaje") {
            PreferenciasViaje(navController)
        }

        composable("ranking_lugares") {
            RankingLugares(navController = navController)
        }

        composable("planes_de_viaje") {
            PlanesViaje(navController = navController)
        }

        composable("viajes_guardados") {
            ViajesGuardados(navController = navController)
        }

        composable("crear_plan") {
            CrearPlan(navController)
        }

        composable("eliminar_plan") {
            EliminarPlan(navController = navController)
        }



        composable(
            route = "itinerario/{titulo}/{destino}/{fechaInicio}/{fechaFin}",
            arguments = listOf(
                navArgument("titulo") { type = NavType.StringType },
                navArgument("destino") { type = NavType.StringType },
                navArgument("fechaInicio") { type = NavType.StringType },
                navArgument("fechaFin") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val titulo = Uri.decode(backStackEntry.arguments?.getString("titulo") ?: "")
            val destino = Uri.decode(backStackEntry.arguments?.getString("destino") ?: "")
            val fechaInicio = Uri.decode(backStackEntry.arguments?.getString("fechaInicio") ?: "")
            val fechaFin = Uri.decode(backStackEntry.arguments?.getString("fechaFin") ?: "")

            Itinerario(
                navController = navController,
                titulo = titulo,
                destino = destino,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin
            )
        }

        composable(
            route = "ver_plan/{planId}",
            arguments = listOf(
                navArgument("planId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            VerPlan(
                navController = navController,
                planId = planId
            )
        }
    }
}
