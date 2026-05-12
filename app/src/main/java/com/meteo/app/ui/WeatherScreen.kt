package com.meteo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.meteo.app.R
import com.meteo.app.domain.WeatherScreenUi

@Composable
fun WeatherRoute(
    viewModel: WeatherViewModel,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showLocationDialog by remember { mutableStateOf(false) }

    val locationTitle = (state as? WeatherUiState.Success)?.currentLocation?.name
        ?: stringResource(R.string.app_name)

    if (showLocationDialog) {
        LocationSelectionDialog(
            viewModel = viewModel,
            onDismiss = {
                viewModel.clearSearch()
            },
            onCurrentLocationRequest = onRequestLocation
        )
    }

    Scaffold(
        topBar = {
            WeatherTopBar(
                locationTitle = locationTitle,
                viewModel = viewModel,
                onRefresh = onRefresh,
                onCurrentLocationRequest = onRequestLocation
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
                    Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
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
                        Text(stringResource(R.string.retry))
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
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ForecastPanel(data)
        }
        item {
            SectionCard(title = stringResource(R.string.section_next_hours)) {
                HourlyRow(data.hourly12)
            }
        }
        item {
            SectionCard(title = stringResource(R.string.section_5_days)) {
                DailyStrip(data.daily5)
            }
        }
    }
}
