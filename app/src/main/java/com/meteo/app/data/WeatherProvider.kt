package com.meteo.app.data

import com.meteo.app.domain.WeatherData

interface WeatherProvider {
    val priority: Int
    suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherData
}
