package com.meteo.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.meteo.app.R
import com.meteo.app.domain.SavedLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionDialog(
    viewModel: WeatherViewModel,
    onDismiss: () -> Unit,
    onCurrentLocationRequest: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val savedLocations by viewModel.savedLocations.collectAsState()
    val history by viewModel.history.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cd_change_location)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.search(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search_city)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (query.isEmpty()) {
                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.add_current_location)) },
                                leadingContent = { Icon(Icons.Default.AddLocation, null) },
                                modifier = Modifier.clickable {
                                    onCurrentLocationRequest()
                                    onDismiss()
                                }
                            )
                        }
                        if (savedLocations.isNotEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.saved_locations),
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(savedLocations) { loc ->
                                ListItem(
                                    headlineContent = { Text(loc.name) },
                                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    modifier = Modifier.clickable {
                                        viewModel.load(loc, addToHistory = true)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                        if (history.isNotEmpty()) {
                            item {
                                Text(
                                    "Sélections récentes",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(history) { loc ->
                                ListItem(
                                    headlineContent = { Text(loc.name) },
                                    leadingContent = { Icon(Icons.Default.History, null) },
                                    modifier = Modifier.clickable {
                                        viewModel.load(loc, addToHistory = true)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    } else {
                        if (searchResults.isNotEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.search_results),
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(searchResults) { res ->
                                ListItem(
                                    headlineContent = { Text(res.name) },
                                    supportingContent = { Text("${res.admin1 ?: ""}, ${res.country ?: ""}") },
                                    modifier = Modifier.clickable {
                                        viewModel.load(
                                            SavedLocation(res.name, res.latitude, res.longitude),
                                            addToHistory = true
                                        )
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
