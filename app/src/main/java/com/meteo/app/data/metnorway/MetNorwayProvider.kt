package com.meteo.app.data.metnorway

import com.meteo.app.data.WeatherProvider
import com.meteo.app.data.api.MetNorwayService
import com.meteo.app.domain.WeatherData

class MetNorwayProvider(private val api: MetNorwayService) : WeatherProvider {
    override val priority: Int = 2

    override suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherData {
        val response = api.compactForecast(latitude, longitude)
        return MetNorwayMapper.buildUi(response, locationLabel)
    }
}