package com.meteo.app.data

import com.meteo.app.data.api.MetNorwayService
import com.meteo.app.domain.MetNorwayMapper
import com.meteo.app.domain.WeatherScreenUi

class MetNorwayProvider(private val api: MetNorwayService) : WeatherProvider {
    override val priority: Int = 2

    override suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherScreenUi {
        val response = api.compactForecast(latitude, longitude)
        return MetNorwayMapper.buildUi(response, locationLabel)
    }
}
