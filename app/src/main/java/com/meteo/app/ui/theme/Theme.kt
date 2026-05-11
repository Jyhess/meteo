package com.meteo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val scheme = lightColorScheme(
    primary = Color(0xFF0B6EA8),
    onPrimary = Color.White,
    secondary = Color(0xFF607D8B),
    surface = Color(0xFFFFFFFF),
    background = Color(0xFFEFF3F7),
)

@Composable
fun MeteoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}
