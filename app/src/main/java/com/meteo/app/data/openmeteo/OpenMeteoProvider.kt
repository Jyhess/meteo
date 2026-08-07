package com.meteo.app.data.openmeteo

import com.meteo.app.domain.HourRow
import com.meteo.app.domain.WeatherData
import com.meteo.app.data.WeatherProvider
import com.meteo.app.data.api.OpenMeteoService
import java.time.LocalDate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenMeteoProvider(private val api: OpenMeteoService) : WeatherProvider {
    override val priority: Int = 1

    override suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherData = withContext(Dispatchers.Default) {
        // Appal 1: Données actuelles et prévisions journalières (16 jours)
        val dailyResponse = api.forecast(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,weather_code",
            daily = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum,wind_speed_10m_max",
            forecastDays = 16,
            timezone = "auto",
        )

        // Appel 2: Détails horaires restreints (seulement pour les prochaines 24h)
        val hourlyResponse = api.forecast(
            latitude = latitude,
            longitude = longitude,
            hourly = "temperature_2m,weather_code,precipitation_probability,precipitation,wind_speed_10m",
            forecastDays = 2,
            timezone = "auto"
        )

        // Fusion des données : on injecte l'hourly de l'appel 2 dans la réponse globale
        val mergedResponse = dailyResponse.copy(hourly = hourlyResponse.hourly)
        
        OpenMeteoMapper.buildUi(mergedResponse, locationLabel)
    }

    override suspend fun fetchHourlyForDay(latitude: Double, longitude: Double, date: LocalDate): List<HourRow> = withContext(Dispatchers.Default) {
        val dateStr = date.toString()
        val response = api.forecast(
            latitude = latitude,
            longitude = longitude,
            hourly = "temperature_2m,weather_code,precipitation_probability,precipitation,wind_speed_10m",
            startDate = dateStr,
            endDate = dateStr,
            timezone = "auto"
        )
        OpenMeteoMapper.mapHourly(response)
    }
}
