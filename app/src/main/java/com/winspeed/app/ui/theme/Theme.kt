package com.winspeed.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(fontSize = 80.sp),
    displayMedium = TextStyle(fontSize = 64.sp),
    displaySmall = TextStyle(fontSize = 48.sp),
    headlineLarge = TextStyle(fontSize = 40.sp),
    headlineMedium = TextStyle(fontSize = 36.sp),
    headlineSmall = TextStyle(fontSize = 32.sp),
    titleLarge = TextStyle(fontSize = 32.sp),
    titleMedium = TextStyle(fontSize = 28.sp),
    titleSmall = TextStyle(fontSize = 24.sp),
    bodyLarge = TextStyle(fontSize = 24.sp),
    bodyMedium = TextStyle(fontSize = 20.sp),
    bodySmall = TextStyle(fontSize = 16.sp),
    labelLarge = TextStyle(fontSize = 24.sp),
    labelMedium = TextStyle(fontSize = 18.sp),
    labelSmall = TextStyle(fontSize = 14.sp)
)

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

@Composable
fun WinspeedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}