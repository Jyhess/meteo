package com.meteo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meteo.app.R
import com.meteo.app.domain.DayForecastUi
import com.meteo.app.domain.HourRowUi
import com.meteo.app.domain.NamedPeriodUi
import com.meteo.app.domain.WeatherOverviewUi
import com.meteo.app.domain.WeatherScreenUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherRoute(
    viewModel: WeatherViewModel,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    IconButton(onClick = onRequestLocation) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Ma position")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            is WeatherUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Chargement…", style = MaterialTheme.typography.bodyLarge)
                }
            }
            is WeatherUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(s.message, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("Réessayer")
                    }
                }
            }
            is WeatherUiState.Success -> {
                WeatherContent(
                    modifier = Modifier.padding(padding),
                    data = s.data,
                )
            }
        }
    }
}

@Composable
private fun WeatherContent(
    modifier: Modifier = Modifier,
    data: WeatherScreenUi,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    data.locationLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        item {
            SectionCard(title = stringResource(R.string.summary_title)) {
                OverviewSection(data.overview)
            }
        }
        item {
            SectionCard(title = stringResource(R.string.hourly_title)) {
                HourlyRow(data.hourly12)
            }
        }
        item {
            SectionCard(title = stringResource(R.string.daily_title)) {
                DailyColumn(data.daily5)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun OverviewSection(overview: WeatherOverviewUi) {
    val today = overview.today
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.today),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            buildString {
                today.currentTempC?.let { append("$it °C · ") }
                append("${today.minC}° / ${today.maxC}° · ${today.label}")
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            stringResource(R.string.tomorrow),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
        val slots = overview.tomorrowSlots
        if (slots.isEmpty()) {
            Text("Pas assez de données pour détailler demain.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                slots.forEach { NamedPeriodRow(it) }
            }
        }
    }
}

@Composable
private fun NamedPeriodRow(slot: NamedPeriodUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(slot.periodName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text("${slot.tempC} °C · ${slot.label}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HourlyRow(hours: List<HourRowUi>) {
    if (hours.isEmpty()) {
        Text("Aucune donnée horaire.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(hours, key = { it.timeLabel }) { h ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                ),
            ) {
                Column(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(h.timeLabel, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${h.tempC}°",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        h.precipPct?.let { "$it %" } ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        h.label,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyColumn(days: List<DayForecastUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        days.forEach { d ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "${d.weekdayLabel} · ${d.dateLabel}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(d.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                Text(
                    "${d.minC}° / ${d.maxC}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
