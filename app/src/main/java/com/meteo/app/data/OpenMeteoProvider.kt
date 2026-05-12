package com.meteo.app.data

import com.meteo.app.data.api.OpenMeteoService
import com.meteo.app.domain.WeatherMapper
import com.meteo.app.domain.WeatherScreenUi

class OpenMeteoProvider(private val api: OpenMeteoService) : WeatherProvider {
    override val priority: Int = 1

    override suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherScreenUi {
        val response = api.forecast(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,weather_code",
            hourly = "temperature_2m,weather_code,precipitation_probability,wind_speed_10m",
            daily = "weather_code,temperature_2m_max,temperature_2m_min,wind_speed_10m_max",
            forecastDays = 16,
            timezone = "auto",
        )
        return WeatherMapper.buildUi(response, locationLabel)
    }
}
