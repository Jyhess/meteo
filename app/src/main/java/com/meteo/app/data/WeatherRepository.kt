package com.meteo.app.data

import com.meteo.app.data.api.OpenMeteoService
import com.meteo.app.domain.WeatherMapper
import com.meteo.app.domain.WeatherScreenUi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WeatherRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(OpenMeteoService::class.java)

    suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherScreenUi {
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

    suspend fun searchCity(query: String) = api.search(query).results.orEmpty()

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"
    }
}
