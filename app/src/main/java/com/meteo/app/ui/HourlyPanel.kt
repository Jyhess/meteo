package com.meteo.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meteo.app.R
import com.meteo.app.domain.HourRow

@Composable
internal fun HourlyPanel(hours: List<HourRow>) {
    if (hours.isEmpty()) {
        Text(stringResource(R.string.no_hourly_data), style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(hours, key = { it.timeLabel }) { h ->
            Card(
                modifier = Modifier.width(96.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    Modifier
                        .defaultMinSize(minHeight = 132.dp)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        h.timeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        weatherEmoji(h.label),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        "${h.tempC}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        h.precipPct?.let { stringResource(R.string.precipitation_pct, it) } ?: "💧 —",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "💨 ${h.windSpeed} km/h",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
