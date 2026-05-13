package com.meteo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.meteo.app.domain.WeatherData

@Composable
internal fun OverviewPanel(data: WeatherData) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(data.overview.periodSlots) { index, slot ->
            val hasDayBreak = index > 0 && data.overview.periodSlots[index - 1].date != slot.date

            if (hasDayBreak) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NightSeparator()
                    // Même valeur que l'espacement du LazyRow pour centrer le séparateur.
                    Spacer(modifier = Modifier.width(10.dp))
                    PeriodCell(
                        title = slot.displayTitle,
                        tempC = slot.tempC,
                        label = slot.label,
                        precipPct = slot.precipPct,
                        windSpeed = slot.windSpeed
                    )
                }
            } else {
                PeriodCell(
                    title = slot.displayTitle,
                    tempC = slot.tempC,
                    label = slot.label,
                    precipPct = slot.precipPct,
                    windSpeed = slot.windSpeed
                )
            }
        }
    }
}

@Composable
private fun PeriodCell(
    title: String,
    tempC: Int?,
    label: String?,
    precipPct: Int?,
    windSpeed: Int?,
) {
    Card(
        modifier = Modifier.width(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            if (label != null) {
                WeatherIcon(label, modifier = Modifier.size(32.dp))
            } else {
                Text("?", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                if (tempC != null) "${tempC}°" else "?°",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (precipPct != null) stringResource(R.string.precipitation_pct, precipPct) else "💧 ?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                if (windSpeed != null) "💨 $windSpeed" else "💨 ?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun NightSeparator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(
                    color = Color(0xFF1A237E).copy(alpha = 0.4f), // Bleu très foncé nuit
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}
