package com.sofiasalud.callscreen.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// COLORS

val purple200 = Color(0xFFBB86FC)
val purple500 = Color(0xFF6200EE)
val purple700 = Color(0xFF3700B3)
val teal200 = Color(0xFF03DAC5)
val transparentGray = Color(0x99C2C2C2)
val solidGray = Color(0xFF2A2A2A)

private val ColorPalette = lightColors(
        primary = purple500,
        primaryVariant = purple700,
        secondary = teal200,
        background = Color.White,
)

// SHAPES

val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp)
)

// TEXT

val typography = Typography(
        body1 = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
        ),
        button = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
        )
)

// THEME

@Composable
fun CallScreenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
            colors = ColorPalette,
            typography = typography,
            shapes = shapes,
            content = content
    )
}
