package com.example.myway.screens.modulo2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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


    }
}
