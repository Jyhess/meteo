package com.meteo.app.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MetNorwayService {
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun compactForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Header("User-Agent") userAgent: String = "MeteoApp/1.0 (https://github.com/meteo/meteo)"
    ): MetNorwayResponse
}
