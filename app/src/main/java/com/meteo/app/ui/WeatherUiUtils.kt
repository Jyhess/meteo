package com.meteo.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.meteo.app.domain.WeatherCondition

@Composable
internal fun WeatherIcon(label: String?, modifier: Modifier = Modifier) {
    val nonNullLabel = label ?: ""
    val condition = WeatherCondition.entries.find { it.description == nonNullLabel } ?: WeatherCondition.UNKNOWN
    Image(
        painter = painterResource(id = condition.iconRes),
        contentDescription = nonNullLabel,
        modifier = modifier.size(48.dp)
    )
}

internal fun getTempColor(temp: Float, alpha: Float = 0.9f): Color {
    return when {
        temp >= 40f -> Color(0xFFFF9800).copy(alpha = alpha) // Orange
        temp <= 0f -> Color(0xFF2196F3).copy(alpha = alpha)  // Blue
        temp > 20f -> lerp(
            Color(0xFFFFD54F).copy(alpha = alpha), // Yellow
            Color(0xFFFF9800).copy(alpha = alpha), // Orange
            (temp - 20f) / 20f
        )
        else -> lerp(
            Color(0xFF2196F3).copy(alpha = alpha), // Blue
            Color(0xFFFFD54F).copy(alpha = alpha), // Yellow
            temp / 20f
        )
    }
}

