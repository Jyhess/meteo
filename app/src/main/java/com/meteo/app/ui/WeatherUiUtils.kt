package com.meteo.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.meteo.app.domain.WeatherCondition

@Composable
internal fun WeatherIcon(label: String, modifier: Modifier = Modifier) {
    val condition = WeatherCondition.entries.find { it.description == label } ?: WeatherCondition.UNKNOWN
    Image(
        painter = painterResource(id = condition.iconRes),
        contentDescription = label,
        modifier = modifier.size(48.dp)
    )
}

