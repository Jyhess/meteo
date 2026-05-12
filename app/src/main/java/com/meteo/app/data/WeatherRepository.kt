package com.meteo.app.data

import com.meteo.app.data.api.MetNorwayService
import com.meteo.app.data.api.OpenMeteoService
import com.meteo.app.data.metnorway.MetNorwayProvider
import com.meteo.app.data.openmeteo.OpenMeteoProvider
import com.meteo.app.domain.WeatherData
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WeatherRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val openMeteoRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val metNorwayRetrofit = Retrofit.Builder()
        .baseUrl("https://api.met.no/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val openMeteoService = openMeteoRetrofit.create(OpenMeteoService::class.java)
    private val metNorwayService = metNorwayRetrofit.create(MetNorwayService::class.java)

    private val providers = listOf(
        OpenMeteoProvider(openMeteoService),
        MetNorwayProvider(metNorwayService)
    ).sortedBy { it.priority }

    suspend fun fetchWeather(latitude: Double, longitude: Double, locationLabel: String): WeatherData {
        var lastException: Exception? = null
        
        for (provider in providers) {
            try {
                return provider.fetchWeather(latitude, longitude, locationLabel)
            } catch (e: Exception) {
                lastException = e
                // Continue to next provider
            }
        }
        
        throw lastException ?: Exception("Aucun fournisseur de météo disponible")
    }

    suspend fun searchCity(query: String) = openMeteoService.search(query).results.orEmpty()
}
