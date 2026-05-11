package com.meteo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meteo.app.data.WeatherRepository
import com.meteo.app.domain.WeatherScreenUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(val data: WeatherScreenUi) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(
    private val repository: WeatherRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    fun load(latitude: Double, longitude: Double, locationLabel: String) {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            runCatching {
                repository.fetchWeather(latitude, longitude, locationLabel)
            }.fold(
                onSuccess = { _state.value = WeatherUiState.Success(it) },
            ) { e ->
                _state.value = WeatherUiState.Error(
                    e.message ?: "Erreur inconnue",
                )
            }
        }
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository) as T
        }
        throw IllegalArgumentException("Type de ViewModel inconnu")
    }
}
