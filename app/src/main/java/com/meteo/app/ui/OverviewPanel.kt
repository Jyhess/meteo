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
    val today = data.overview.today
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF003366).copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.forecast_title),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    data.overview.periodSlots.forEachIndexed { index, slot ->
                        // Si le créneau actuel est le Soir et le suivant est le Matin, 
                        // ou si la date change entre deux créneaux, on affiche un séparateur de nuit.
                        if (index > 0) {
                            val prev = data.overview.periodSlots[index - 1]
                            if (prev.date != slot.date) {
                                NightSeparator()
                            }
                        }

                        PeriodCell(
                            title = slot.displayTitle,
                            tempC = slot.tempC,
                            label = slot.label,
                            precipPct = slot.precipPct
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    today.label.let { WeatherIcon(it, Modifier.size(24.dp)) }
                    Text(
                        stringResource(
                            R.string.today_summary,
                            stringResource(R.string.today),
                            today.minC,
                            today.maxC,
                            "", // On vide l'emoji car on utilise l'icone
                            today.label,
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
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
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        if (label != null) {
            WeatherIcon(label)
        } else {
            Text("?", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            if (tempC != null) "${tempC}°" else "?°",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (precipPct != null) stringResource(R.string.precipitation_pct, precipPct) else "💧 ?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
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
