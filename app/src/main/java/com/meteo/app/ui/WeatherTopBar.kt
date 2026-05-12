package com.meteo.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.meteo.app.R
import com.meteo.app.domain.SavedLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WeatherTopBar(
    locationTitle: String,
    viewModel: WeatherViewModel,
    onRefresh: () -> Unit,
    onCurrentLocationRequest: () -> Unit,
) {
    var isSearching by remember { mutableStateOf(value = false) }
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val savedLocations by viewModel.savedLocations.collectAsState()
    val history by viewModel.history.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val isFavorite = (viewModel.state.collectAsState().value as? WeatherUiState.Success)?.let { success ->
        savedLocations.any { it.name == success.currentLocation.name }
    } ?: false

    TopAppBar(
        title = {
            if (!isSearching) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = {
                            isSearching = true
                            query = ""
                        })
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        locationTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.search(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.search_city)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                isSearching = false
                                viewModel.clearSearch()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = isSearching,
                        onDismissRequest = { /* Handle via focus or close button */ },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        properties = PopupProperties(focusable = false)
                    ) {
                        if (query.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_current_location)) },
                                leadingIcon = { Icon(Icons.Default.AddLocation, null) },
                                onClick = {
                                    onCurrentLocationRequest()
                                    isSearching = false
                                }
                            )
                            if (savedLocations.isNotEmpty()) {
                                ListHeader("Favoris")
                                savedLocations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc.name) },
                                        leadingIcon = { Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            viewModel.load(loc, addToHistory = true)
                                            isSearching = false
                                        }
                                    )
                                }
                            }
                            if (history.isNotEmpty()) {
                                ListHeader("Récents")
                                history.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc.name) },
                                        leadingIcon = { Icon(Icons.Default.History, null) },
                                        onClick = {
                                            viewModel.load(loc, addToHistory = true)
                                            isSearching = false
                                        }
                                    )
                                }
                            }
                        } else {
                            searchResults.forEach { res ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(res.name)
                                            Text(
                                                "${res.admin1 ?: ""}, ${res.country ?: ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.load(
                                            SavedLocation(res.name, res.latitude, res.longitude),
                                            addToHistory = true
                                        )
                                        isSearching = false
                                        viewModel.clearSearch()
                                    }
                                )
                            }
                        }
                    }
                }
                LaunchedEffect(isSearching) {
                    if (isSearching) focusRequester.requestFocus()
                }
            }
        },
        actions = {
            if (!isSearching) {
                val state = viewModel.state.collectAsState().value
                if (state is WeatherUiState.Success) {
                    IconButton(onClick = { viewModel.toggleFavorite(state.currentLocation) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_refresh))
                }
            }
        },
    )
}

@Composable
private fun ListHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
