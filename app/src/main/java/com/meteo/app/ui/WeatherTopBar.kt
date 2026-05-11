package com.meteo.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.meteo.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WeatherTopBar(
    locationTitle: String,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onRequestLocation) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_change_location))
            }
        },
        title = {
            Text(
                locationTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_refresh))
            }
            IconButton(onClick = onRequestLocation) {
                Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.cd_my_location))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_actions))
            }
        },
    )
}
