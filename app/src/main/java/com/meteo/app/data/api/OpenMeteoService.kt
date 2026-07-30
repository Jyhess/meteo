package com.meteo.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "fr",
        @Query("country") country: String = "FR"
    ): GeocodingResponse

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String? = null,
        @Query("hourly") hourly: String? = null,
        @Query("daily") daily: String? = null,
        @Query("forecast_days") forecastDays: Int? = null,
        @Query("timezone") timezone: String = "auto",
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
    ): ForecastResponse
}
