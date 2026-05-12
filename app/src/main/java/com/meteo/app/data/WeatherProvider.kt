package com.meteo.app.data

import com.meteo.app.domain.WeatherScreenUi

interface WeatherProvider {
    val priority: Int
    suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherScreenUi
}
