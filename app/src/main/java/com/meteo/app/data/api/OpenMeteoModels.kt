package com.meteo.app.data.api

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("current") val current: CurrentDto?,
    @SerializedName("hourly") val hourly: HourlyDto?,
    @SerializedName("daily") val daily: DailyDto?,
)

data class CurrentDto(
    @SerializedName("time") val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
)

data class HourlyDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m") val temperature: List<Double>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>?,
)

data class DailyDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("temperature_2m_max") val maxC: List<Double>?,
    @SerializedName("temperature_2m_min") val minC: List<Double>?,
)
