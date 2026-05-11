package com.meteo.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("forecast_days") forecastDays: Int,
        @Query("timezone") timezone: String,
    ): ForecastResponse
}
