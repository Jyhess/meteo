package com.meteo.app.data.metnorway

import com.meteo.app.data.WeatherProvider
import com.meteo.app.data.api.MetNorwayService
import com.meteo.app.domain.HourRow
import com.meteo.app.domain.WeatherData
import java.time.LocalDate

class MetNorwayProvider(private val api: MetNorwayService) : WeatherProvider {
    override val priority: Int = 2

    override suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherData {
        val response = api.compactForecast(latitude, longitude)
        return MetNorwayMapper.buildUi(response, locationLabel)
    }

    override suspend fun fetchHourlyForDay(latitude: Double, longitude: Double, date: LocalDate): List<HourRow> {
        // MetNorway returns full forecast in one go, but we follow the on-demand mapping logic
        val response = api.compactForecast(latitude, longitude)
        return MetNorwayMapper.mapHourly(response).filter { it.date == date }
    }
}