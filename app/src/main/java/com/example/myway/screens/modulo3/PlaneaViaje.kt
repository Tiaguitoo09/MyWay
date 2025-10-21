package com.example.myway.screens.modulo3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.screens.CustomTextField
import com.example.myway.ui.theme.*

@Composable
fun PlaneaViaje(navController: NavController) {
    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido desplazable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Encabezado con flecha, título y perfil
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
                    text = stringResource(R.string.planea_viaje),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                // Icono de perfil
                Image(
                    painter = painterResource(id = R.drawable.icono_perfil),
                    contentDescription = stringResource(R.string.perfil),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable { navController.navigate("perfil_ajustes") }
                )
            }

            // Texto informativo
            Text(
                text = stringResource(R.string.activar_permisos),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Pregunta
            Text(
                text = stringResource(R.string.a_donde_vamos),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 8.dp)
            )

            // Barra de búsqueda
            CustomTextField(
                placeholder = stringResource(R.string.buscar),
                color = Blanco,
                textColor = Color.Black,
                onTextChange = { searchText = it },
                text = searchText,
                showBorder = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fila de botones de categorías
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomButton(
                        text = stringResource(R.string.guardados),
                        color = Blanco,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                navController.navigate("guardados")
                            }
                    ) { /* acción */ }

                    CustomButton(
                        text = stringResource(R.string.alimentos),
                        color = Blanco,
                        modifier = Modifier.weight(1f)
                    ) { /* acción */ }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomButton(
                        text = stringResource(R.string.combustible),
                        color = Blanco,
                        modifier = Modifier.weight(1f)
                    ) { /* acción */ }

                    CustomButton(
                        text = stringResource(R.string.supermercados),
                        color = Blanco,
                        modifier = Modifier.weight(1f)
                    ) { /* acción */ }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomButton(
                        text = stringResource(R.string.hoteles),
                        color = Blanco,
                        modifier = Modifier.weight(1f)
                    ) { /* acción */ }

                    CustomButton(
                        text = stringResource(R.string.parques),
                        color = Blanco,
                        modifier = Modifier.weight(1f)
                    ) { /* acción */ }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección "Recientes"
            Text(
                text = stringResource(R.string.recientes),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            val recientes = listOf(
                "Calle 9 Bis #19A-60",
                "Carrera 16 #187-61",
                "Calle 7c Bis #72A-20",
                "Calle 60, Teusaquillo"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recientes.forEach { lugar ->
                    CustomButton(
                        text = lugar,
                        color = Blanco,
                        modifier = Modifier.fillMaxWidth()
                    ) { /* acción */ }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
