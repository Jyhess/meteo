package com.meteo.app.data.openmeteo

import com.meteo.app.domain.WeatherData
import com.meteo.app.data.WeatherProvider
import com.meteo.app.data.api.OpenMeteoService

class OpenMeteoProvider(private val api: OpenMeteoService) : WeatherProvider {
    override val priority: Int = 1

    override suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherData {
        val response = api.forecast(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,weather_code",
            hourly = "temperature_2m,weather_code,precipitation_probability,wind_speed_10m",
            daily = "weather_code,temperature_2m_max,temperature_2m_min,wind_speed_10m_max",
            forecastDays = 16,
            timezone = "auto",
        )
        return OpenMeteoMapper.buildUi(response, locationLabel)
    }
}