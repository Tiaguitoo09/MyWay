package com.example.myway.screens.modulo2

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myway.R

@Composable
fun Home(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo1),
            contentDescription = "Fondo principal",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Imagen Perfil
        Image(
            painter = painterResource(id = R.drawable.icono_perfil),
            contentDescription = "Perfil",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.navigate("perfil_ajustes") }
        )

    }
}
