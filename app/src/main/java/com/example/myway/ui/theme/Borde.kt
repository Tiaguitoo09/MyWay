package com.example.myway.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

@Composable
fun OutlinedText(
    text: String,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    textColor: Color,
    borderColor: Color,
    borderWidth: Float, // grosor del borde
) {
    Box {
        for (x in -borderWidth.toInt()..borderWidth.toInt()) {
            for (y in -borderWidth.toInt()..borderWidth.toInt()) {
                if (x != 0 || y != 0) {
                    Text(
                        text = text,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        color = borderColor,
                        modifier = Modifier.offset(x.dp, y.dp)
                    )
                }
            }
        }
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}
