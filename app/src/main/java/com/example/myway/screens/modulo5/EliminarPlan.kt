package com.example.myway.screens.modulo5

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Rojo

@Composable
fun EliminarPlan(navController: NavController) {

    Box(modifier = Modifier.fillMaxSize()) {
        // 🔹 Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 🔹 Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Flecha atrás
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                // Título
                Text(
                    text = stringResource(R.string.eliminar_plan),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // 🔹 Mensaje informativo
            Text(
                text = "Selecciona el plan que deseas eliminar o confirma tu acción.",
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 🔹 Botón eliminar
            CustomButton(
                alignCenter = false,
                text = stringResource(R.string.eliminar),
                color = Rojo,
                textColor = Blanco,
                fontWeight = FontWeight.Bold,
                // icon = painterResource(id = R.drawable.ic_search),
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("eliminar_plan2") }
            )
        }
    }
}
