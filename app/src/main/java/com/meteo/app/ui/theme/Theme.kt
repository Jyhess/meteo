package com.meteo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val scheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF0277BD),
    surface = Color(0xFFF5F9FC),
    background = Color(0xFFE3F2FD),
)

@Composable
fun MeteoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}
