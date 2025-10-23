package com.example.myway.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myway.ui.theme.*

/**
 * Banner que se muestra cuando no hay permisos de ubicación
 * Redirige a la pantalla de Permisos de la app
 */
@Composable
fun LocationPermissionBanner(
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onNavigateToPermissions() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Rojo.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono de advertencia
            Icon(
                painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                contentDescription = "Alerta permisos",
                tint = Blanco,
                modifier = Modifier.size(40.dp)
            )

            // Texto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "📍 Ubicación desactivada",
                    color = Blanco,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Nunito
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Activa la ubicación para usar el mapa",
                    color = Blanco.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontFamily = Nunito
                )
            }

            // Flecha
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                contentDescription = "Ir a configurar",
                tint = Blanco,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}