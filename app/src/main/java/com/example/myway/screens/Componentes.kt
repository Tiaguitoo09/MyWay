package com.example.myway.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myway.ui.theme.Azul1
import com.example.myway.ui.theme.Blanco
import com.example.myway.ui.theme.Nunito
import com.example.myway.ui.theme.Azul3
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.example.myway.ui.theme.Azul2

// ------------------- Botón reutilizable -------------------
@Composable
fun CustomButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal, // ← NUEVO parámetro
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(320.dp)
            .height(50.dp)
            .border(1.8.dp, Azul1, RoundedCornerShape(12.dp)) // mismo borde que el TextField
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Texto con borde azul y relleno blanco
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontFamily = Nunito,
                fontWeight = fontWeight,
                fontSize = fontSize,
                color = Azul1,
                style = TextStyle(
                    drawStyle = Stroke(width = 6f)
                )
            )
            Text(
                text = text,
                fontFamily = Nunito,
                fontWeight = fontWeight,
                fontSize = fontSize,
                color = Blanco
            )
        }
    }
}






// ------------------- Campo de texto reutilizable -------------------
@Composable
fun CustomTextField(
    placeholder: String,
    color: Color,
    isPassword: Boolean = false,
    textColor: Color = Color.White,
    onTextChange: (String) -> Unit,
    text: String,
    showBorder: Boolean = true // ← NUEVO parámetro opcional
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        placeholder = {
            if (showBorder) {
                // Placeholder con borde azul
                Box(contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = placeholder,
                        color = Azul1,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            drawStyle = Stroke(width = 6f)
                        )
                    )
                    Text(
                        text = placeholder,
                        color = Blanco,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            } else {
                // Placeholder normal
                Text(
                    text = placeholder,
                    color = textColor,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        },
        singleLine = true,
        textStyle = TextStyle(color = textColor),
        modifier = Modifier
            .width(320.dp)
            .height(55.dp)
            .then(
                if (showBorder)
                    Modifier.border(1.8.dp, Azul1, RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Blanco,
            unfocusedTextColor = Blanco,
            cursorColor = Blanco,
            focusedPlaceholderColor = Blanco,
            unfocusedPlaceholderColor = Blanco,
            focusedBorderColor = if (showBorder) Azul1 else Color.Transparent,
            unfocusedBorderColor = if (showBorder) Azul1 else Color.Transparent,
            focusedContainerColor = color,
            unfocusedContainerColor = color
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}



// ------------------- Texto de título reutilizable -------------------
@Composable
fun CustomTitleText(
    text: String,
    color: Color,
    fontSize: TextUnit = 80.sp,
    fontFamily: FontFamily = Nunito,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        color = color,
        fontFamily = fontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun InfoBlock(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Blanco.copy(alpha = 0.8f), RoundedCornerShape(16.dp)) // ✅
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            color = Azul2,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = value,
            color = Azul1,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp),
            fontWeight = FontWeight.Bold

        )
    }
}


