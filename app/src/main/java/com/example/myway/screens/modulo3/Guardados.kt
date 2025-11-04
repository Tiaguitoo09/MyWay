package com.example.myway.screens.modulo3

import androidx.compose.foundation.Image
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
fun Guardados(navController: NavController) {
    var searchText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
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

            // Encabezado
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
                    text = stringResource(R.string.guardados),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                // Icono perfil
                Image(
                    painter = painterResource(id = R.drawable.icono_perfil),
                    contentDescription = stringResource(R.string.perfil),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable { navController.navigate("perfil_ajustes") }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))

            // Subtítulo
            Text(
                text = stringResource(R.string.tus_listas),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 8.dp)
            )

            // Barra de búsqueda
            CustomTextField(
                placeholder = stringResource(R.string.buscar),
                color = Blanco,
                textColor = Color.Black,
                fontWeight = FontWeight.Normal,
                onTextChange = { searchText = it },
                text = searchText,
                showBorder = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones con iconos
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // ¿Sin plan?
                CustomButton(
                    text = stringResource(R.string.sin_plan),
                    color = Blanco,
                    textColor = Negro,
                    fontWeight = FontWeight.Normal,
                    icon = painterResource(id = R.drawable.icono_sin_plan),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("sin_plan") }
                )

                // Favoritos
                CustomButton(
                    text = stringResource(R.string.favoritos),
                    color = Blanco,
                    textColor = Negro,
                    fontWeight = FontWeight.Normal,
                    icon = painterResource(id = R.drawable.icono_favoritos),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("favoritos") }
                )

                // Planes de viaje
                CustomButton(
                    text = stringResource(R.string.planes_de_viaje),
                    color = Blanco,
                    textColor = Negro,
                    fontWeight = FontWeight.Normal,
                    icon = painterResource(id = R.drawable.icono_planes),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("planes_de_viaje") }
                )

                // Viajes guardados
                CustomButton(
                    text = stringResource(R.string.viajes_guardados),
                    color = Blanco,
                    textColor = Negro,
                    fontWeight = FontWeight.Normal,
                    icon = painterResource(id = R.drawable.icono_viajes),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("viajes_guardados") }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Ícono grande inferior
            Image(
                painter = painterResource(id = R.drawable.brujula),
                contentDescription = stringResource(R.string.icono_brujula),
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 20.dp)
            )
        }
    }
}