package com.example.myway.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myway.R
import com.example.myway.screens.CustomButton
import com.example.myway.ui.theme.Azul4
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Rojo

/**
 * Overlay que advierte sobre conducción
 * Se puede cerrar para acceder a funciones básicas
 */
@Composable
fun SafetyWarningOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .zIndex(999f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Azul4),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono de advertencia
                Icon(
                    painter = painterResource(id = R.drawable.circuloadvertencia),
                    contentDescription = "Advertencia",
                    tint = Rojo,
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "🚗 Conducción Detectada",
                    color = Blanco,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = Nunito,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Por tu seguridad, no busques nuevos destinos mientras conduces.",
                    color = Blanco,
                    fontSize = 16.sp,
                    fontFamily = Nunito,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Blanco.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💡 ¿Tienes un copiloto?",
                            color = Blanco,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Nunito
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ve a Perfil → Ajustes → Modo Copiloto para que otra persona use la app mientras manejas.",
                            color = Blanco.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontFamily = Nunito,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                CustomButton(
                    text = "Entendido",
                    color = Rojo,
                    onClick = onDismiss,
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🛑 No uses el teléfono mientras conduces",
                    color = Blanco.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Nunito,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}