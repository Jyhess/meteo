package com.meteo.app.data.api

import com.google.gson.annotations.SerializedName

data class MetNorwayResponse(
    val properties: MetProperties
)

data class MetProperties(
    val timeseries: List<MetTimeSeries>
)

data class MetTimeSeries(
    val time: String,
    val data: MetData
)

data class MetData(
    val instant: MetInstant,
    @SerializedName("next_1_hours") val next1h: MetNextHours?,
    @SerializedName("next_6_hours") val next6h: MetNextHours?,
    @SerializedName("next_12_hours") val next12h: MetNextHours?
)

data class MetInstant(
    val details: MetDetails
)

data class MetNextHours(
    val summary: MetSummary,
    val details: MetDetails?
)

data class MetSummary(
    @SerializedName("symbol_code") val symbolCode: String
)

data class MetDetails(
    @SerializedName("air_temperature") val airTemperature: Double?,
    @SerializedName("wind_speed") val windSpeed: Double?,
    @SerializedName("probability_of_precipitation") val precipitationProbability: Double?,
    @SerializedName("air_temperature_max") val airTemperatureMax: Double?,
    @SerializedName("air_temperature_min") val airTemperatureMin: Double?
)
