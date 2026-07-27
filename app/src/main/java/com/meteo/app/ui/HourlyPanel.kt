package com.meteo.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.meteo.app.R
import com.meteo.app.domain.HourRow

@Composable
internal fun HourlyPanel(hours: List<HourRow>?) {
    if (hours.isNullOrEmpty()) {
        Text(stringResource(R.string.no_hourly_data), style = MaterialTheme.typography.bodyMedium)
        return
    }
    HourlyChart(hours = hours)
}
