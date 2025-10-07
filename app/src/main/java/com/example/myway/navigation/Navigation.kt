package com.example.myway.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myway.screens.InicioPantalla
import com.example.myway.screens.IngresoUsuario
import com.example.myway.screens.RegistroUsuario

@Composable
fun MyWayAppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") { InicioPantalla(navController) }
        composable("ingreso_usuario") { IngresoUsuario(navController = navController) }
        composable("registro_usuario") { RegistroUsuario(navController = navController) }
    }
}
