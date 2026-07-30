package com.meteo.app.data

import com.meteo.app.domain.HourRow
import com.meteo.app.domain.WeatherData
import java.time.LocalDate

interface WeatherProvider {
    val priority: Int
    suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherData
    suspend fun fetchHourlyForDay(latitude: Double, longitude: Double, date: LocalDate): List<HourRow>
}
