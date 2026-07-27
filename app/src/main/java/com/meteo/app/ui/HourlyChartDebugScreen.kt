package com.meteo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.meteo.app.R
import com.meteo.app.domain.HourRow

private enum class HourlyChartScenario(
    val labelRes: Int,
    val hours: List<HourRow>,
) {
    Typical(
        labelRes = R.string.debug_scenario_typical,
        hours = sampleHours(
            temps = listOf(12, 13, 15, 17, 18, 19, 18, 16, 14, 13, 12, 11, 10, 9, 9, 10, 11, 13, 15, 17, 18, 17, 15, 13),
            precips = listOf(5, 10, 15, 20, 25, 30, 35, 40, 30, 20, 10, 5, 0, 0, 5, 10, 15, 20, 25, 30, 25, 20, 15, 10),
            winds = listOf(8, 10, 12, 14, 16, 18, 20, 18, 15, 12, 10, 8, 7, 6, 6, 7, 8, 10, 12, 14, 13, 12, 10, 9),
            labels = List(24) { "Nuageux" },
        ),
    ),
    Rainy(
        labelRes = R.string.debug_scenario_rainy,
        hours = sampleHours(
            temps = listOf(11, 11, 10, 10, 9, 9, 9, 10, 10, 11, 11, 12, 12, 12, 11, 11, 10, 10, 9, 9, 10, 10, 11, 11),
            precips = listOf(40, 55, 70, 85, 90, 95, 90, 80, 65, 50, 35, 25, 20, 15, 30, 45, 60, 75, 80, 70, 60, 50, 40, 30),
            winds = listOf(12, 14, 16, 18, 20, 22, 24, 22, 20, 18, 16, 14, 13, 12, 14, 16, 18, 20, 21, 20, 18, 16, 14, 13),
            labels = List(24) { "Pluie" },
        ),
    ),
    Windy(
        labelRes = R.string.debug_scenario_windy,
        hours = sampleHours(
            temps = listOf(14, 14, 15, 15, 16, 16, 15, 15, 14, 14, 13, 13, 12, 12, 13, 13, 14, 15, 16, 16, 15, 14, 13, 12),
            precips = listOf(0, 5, 10, 5, 0, 0, 5, 10, 5, 0, 0, 0, 0, 5, 10, 5, 0, 0, 0, 0, 0, 0, 0, 0),
            winds = listOf(15, 22, 30, 38, 45, 50, 48, 40, 32, 25, 18, 12, 15, 25, 40, 60, 80, 95, 85, 70, 55, 40, 25, 15),
            labels = List(24) { "Vent" },
        ),
    ),
    Stable(
        labelRes = R.string.debug_scenario_stable,
        hours = sampleHours(
            temps = List(24) { 16 },
            precips = List(24) { 10 },
            winds = List(24) { 12 },
            labels = List(24) { "Dégagé" },
        ),
    ),
    Extreme(
        labelRes = R.string.debug_scenario_extreme,
        hours = sampleHours(
            temps = listOf(2, 8, 14, 22, 26, 28, 27, 24, 18, 12, 6, 0, -2, -5, -3, 0, 4, 10, 16, 20, 18, 14, 10, 5),
            precips = listOf(0, 0, 10, 40, 80, 100, 90, 60, 30, 10, 0, 0, 5, 10, 20, 40, 70, 90, 80, 60, 40, 20, 10, 5),
            winds = listOf(5, 8, 12, 20, 35, 55, 45, 30, 18, 10, 6, 4, 10, 25, 50, 80, 100, 90, 70, 50, 30, 15, 8, 5),
            labels = List(24) { "Variable" },
        ),
    ),
    SparsePrecip(
        labelRes = R.string.debug_scenario_sparse_precip,
        hours = sampleHours(
            temps = listOf(10, 11, 12, 13, 14, 15, 16, 15, 14, 13, 12, 11, 10, 11, 12, 13, 14, 15, 16, 15, 14, 13, 12, 11),
            precips = listOf(-1, -1, 5, -1, -1, 20, -1, -1, 40, -1, -1, 10, -1, -1, 5, -1, -1, 20, -1, -1, 40, -1, -1, 10),
            winds = listOf(6, 7, 8, 9, 10, 11, 12, 11, 10, 9, 8, 7, 6, 7, 8, 9, 10, 11, 12, 11, 10, 9, 8, 7),
            labels = List(24) { "Nuageux" },
        ),
    ),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HourlyChartDebugScreen(onBack: () -> Unit) {
    var scenario by remember { mutableStateOf(HourlyChartScenario.Typical) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.debug_hourly_chart_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.debug_hourly_chart_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HourlyChartScenario.entries.forEach { option ->
                    FilterChip(
                        selected = scenario == option,
                        onClick = { scenario = option },
                        label = { Text(stringResource(option.labelRes)) },
                    )
                }
            }

            SectionCard(title = stringResource(R.string.section_next_hours)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A237E).copy(alpha = 0.55f),
                                    Color(0xFF0D47A1).copy(alpha = 0.45f),
                                ),
                            ),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    HourlyPanel(hours = scenario.hours)
                }
            }
        }
    }
}

private fun sampleHours(
    temps: List<Int>,
    precips: List<Int>,
    winds: List<Int>,
    labels: List<String>,
    startHour: Int = 8,
): List<HourRow> {
    val count = minOf(temps.size, precips.size, winds.size, labels.size, 24)
    return (0 until count).map { index ->
        val hour = (startHour + index) % 24
        HourRow(
            timeLabel = "%02d:00".format(hour),
            tempC = temps[index],
            precipPct = precips[index].takeIf { it >= 0 },
            precipAmount = precips[index].takeIf { it >= 0 }?.toFloat(),
            windSpeed = winds[index],
            label = labels[index],
        )
    }
}
