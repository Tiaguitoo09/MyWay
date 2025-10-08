package com.example.myway.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// ------------------- Botón reutilizable -------------------
@Composable
fun CustomButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        border = BorderStroke(1.dp, Azul1),
        modifier = modifier
            .width(320.dp)
            .height(50.dp)
    ) {
        Text(
            text = text,
            fontFamily = Nunito,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Blanco
        )
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
    text: String
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        placeholder = { Text(text = placeholder, color = textColor) },
        singleLine = true,
        textStyle = TextStyle(color = textColor),
        modifier = Modifier
            .width(320.dp)
            .height(55.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Blanco,
            unfocusedTextColor = Blanco,
            cursorColor = Blanco,
            focusedPlaceholderColor = Blanco,
            unfocusedPlaceholderColor = Blanco,
            focusedBorderColor = Azul1,
            unfocusedBorderColor = Azul1,
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
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        color = color,
        fontFamily = fontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight
    )
}
