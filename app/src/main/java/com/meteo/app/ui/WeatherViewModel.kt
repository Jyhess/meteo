package com.meteo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meteo.app.data.WeatherRepository
import com.meteo.app.data.api.GeocodingResult
import com.meteo.app.data.local.LocationStore
import com.meteo.app.domain.SavedLocation
import com.meteo.app.domain.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(
        val data: WeatherData,
        val currentLocation: SavedLocation,
        val isOffline: Boolean = false
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationStore: LocationStore,
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    private val _savedLocations = MutableStateFlow<List<SavedLocation>>(emptyList())
    val savedLocations: StateFlow<List<SavedLocation>> = _savedLocations.asStateFlow()

    private val _history = MutableStateFlow<List<SavedLocation>>(emptyList())
    val history: StateFlow<List<SavedLocation>> = _history.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResult>> = _searchResults.asStateFlow()

    init {
        _savedLocations.value = locationStore.getLocations()
        _history.value = locationStore.getHistory()
    }

    fun load(location: SavedLocation, addToHistory: Boolean = false) {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            runCatching {
                repository.fetchWeather(location.latitude, location.longitude, location.name)
            }.fold(
                onSuccess = { weather ->
                    _state.value = WeatherUiState.Success(weather, location, isOffline = false)
                    locationStore.saveLastWeather(location, weather)
                    if (addToHistory) {
                        locationStore.addToHistory(location)
                        _history.value = locationStore.getHistory()
                    }
                },
                onFailure = { e ->
                    val lastKnown = locationStore.getLastWeather(location)
                    if (lastKnown != null) {
                        _state.value = WeatherUiState.Success(lastKnown, location, isOffline = true)
                    } else {
                        _state.value = WeatherUiState.Error(
                            e.message ?: "Erreur inconnue",
                        )
                    }
                }
            )
        }
    }

    fun toggleFavorite(location: SavedLocation) {
        locationStore.toggleFavorite(location)
        _savedLocations.value = locationStore.getLocations()
    }

    fun search(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            runCatching { repository.searchCity(query) }
                .onSuccess { _searchResults.value = it }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val locationStore: LocationStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository, locationStore) as T
        }
        throw IllegalArgumentException("Type de ViewModel inconnu")
    }
}
