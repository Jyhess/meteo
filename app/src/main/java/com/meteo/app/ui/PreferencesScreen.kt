package com.meteo.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
) {
    val savedLocations by viewModel.savedLocations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoris") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (savedLocations.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Aucun favori enregistré", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(savedLocations) { loc ->
                    ListItem(
                        headlineContent = { Text(loc.name) },
                        supportingContent = { Text("${loc.latitude}, ${loc.longitude}") },
                        trailingContent = {
                            IconButton(onClick = { viewModel.toggleFavorite(loc) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                            }
                        }
                    )
                }
            }
        }
    }
}
