package com.meteo.app.domain

import com.meteo.app.R

data class WeatherData(
    val locationLabel: String,
    val overview: WeatherOverview,
    val hourly12: List<HourRow>,
    val daily5: List<DayForecast>,
)

data class WeatherOverview(
    val today: DayPeriod,
    val tomorrowSlots: List<NamedPeriod>,
    val periodSlots: List<PeriodSlot> = emptyList(),
)

enum class WeatherCondition(val description: String, val iconRes: Int, val bgRes: Int) {
    CLEAR("Dégagé", R.drawable.airy_clear, R.drawable.bg_clear),
    MOSTLY_CLEAR("Essentiellement dégagé", R.drawable.airy_mostly_clear, R.drawable.bg_mostly_clear),
    PARTLY_CLOUDY("Partiellement nuageux", R.drawable.airy_partly_cloudy, R.drawable.bg_partly_cloudy),
    OVERCAST("Couvert", R.drawable.airy_overcast, R.drawable.bg_overcast),
    FOG("Brouillard", R.drawable.airy_fog, R.drawable.bg_fog),
    RIME_FOG("Brouillard givrant", R.drawable.airy_rime_fog, R.drawable.bg_rime_fog),
    LIGHT_DRIZZLE("Bruine légère", R.drawable.airy_light_drizzle, R.drawable.bg_light_drizzle),
    MODERATE_DRIZZLE("Bruine modérée", R.drawable.airy_moderate_drizzle, R.drawable.bg_moderate_drizzle),
    DENSE_DRIZZLE("Bruine dense", R.drawable.airy_dense_drizzle, R.drawable.bg_dense_drizzle),
    LIGHT_FREEZING_DRIZZLE("Bruine verglaçante légère", R.drawable.airy_light_freezing_drizzle, R.drawable.bg_light_freezing_drizzle),
    DENSE_FREEZING_DRIZZLE("Bruine verglaçante dense", R.drawable.airy_dense_freezing_drizzle, R.drawable.bg_dense_freezing_drizzle),
    LIGHT_RAIN("Pluie légère", R.drawable.airy_light_rain, R.drawable.bg_light_rain),
    MODERATE_RAIN("Pluie modérée", R.drawable.airy_moderate_rain, R.drawable.bg_moderate_rain),
    HEAVY_RAIN("Pluie forte", R.drawable.airy_heavy_rain, R.drawable.bg_heavy_rain),
    LIGHT_FREEZING_RAIN("Pluie verglaçante légère", R.drawable.airy_light_freezing_rain, R.drawable.bg_light_freezing_rain),
    HEAVY_FREEZING_RAIN("Pluie verglaçante forte", R.drawable.airy_heavy_freezing_rain, R.drawable.bg_heavy_freezing_rain),
    SLIGHT_SNOWFALL("Chutes de neige légères", R.drawable.airy_slight_snowfall, R.drawable.bg_slight_snowfall),
    MODERATE_SNOWFALL("Chutes de neige modérées", R.drawable.airy_moderate_snowfall, R.drawable.bg_moderate_snowfall),
    HEAVY_SNOWFALL("Chutes de neige fortes", R.drawable.airy_heavy_snowfall, R.drawable.bg_heavy_snowfall),
    SNOW_GRAINS("Grains de neige", R.drawable.airy_snowflake, R.drawable.bg_snow_grains),
    RAIN_SHOWERS_SLIGHT("Averses de pluie légères", R.drawable.airy_light_rain, R.drawable.bg_rain_showers_slight),
    RAIN_SHOWERS_MODERATE("Averses de pluie modérées", R.drawable.airy_moderate_rain, R.drawable.bg_rain_showers_moderate),
    RAIN_SHOWERS_VIOLENT("Averses de pluie violentes", R.drawable.airy_heavy_rain, R.drawable.bg_rain_showers_violent),
    SNOW_SHOWERS_SLIGHT("Averses de neige légères", R.drawable.airy_snowflake, R.drawable.bg_snow_showers_slight),
    SNOW_SHOWERS_HEAVY("Averses de neige fortes", R.drawable.airy_snowflake, R.drawable.bg_snow_showers_heavy),
    THUNDERSTORM("Orage", R.drawable.airy_thunderstorm, R.drawable.bg_thunderstorm),
    THUNDERSTORM_HAIL("Orage avec grêle", R.drawable.airy_thunderstorm_with_hail, R.drawable.bg_thunderstorm_hail),
    VARIABLE("Variable", R.drawable.airy_partly_cloudy, R.drawable.bg_variable),
    UNKNOWN("—", R.drawable.airy_clear, R.drawable.bg_unknown);

    companion object {
        fun fromWMOCode(code: Int?): WeatherCondition {
            return when (code) {
                null -> UNKNOWN
                0 -> CLEAR
                1 -> MOSTLY_CLEAR
                2 -> PARTLY_CLOUDY
                3 -> OVERCAST
                45 -> FOG
                48 -> RIME_FOG
                51 -> LIGHT_DRIZZLE
                53 -> MODERATE_DRIZZLE
                55 -> DENSE_DRIZZLE
                56 -> LIGHT_FREEZING_DRIZZLE
                57 -> DENSE_FREEZING_DRIZZLE
                61 -> LIGHT_RAIN
                63 -> MODERATE_RAIN
                65 -> HEAVY_RAIN
                66 -> LIGHT_FREEZING_RAIN
                67 -> HEAVY_FREEZING_RAIN
                71 -> SLIGHT_SNOWFALL
                73 -> MODERATE_SNOWFALL
                75 -> HEAVY_SNOWFALL
                77 -> SNOW_GRAINS
                80 -> RAIN_SHOWERS_SLIGHT
                81 -> RAIN_SHOWERS_MODERATE
                82 -> RAIN_SHOWERS_VIOLENT
                85 -> SNOW_SHOWERS_SLIGHT
                86 -> SNOW_SHOWERS_HEAVY
                95 -> THUNDERSTORM
                96, 99 -> THUNDERSTORM_HAIL
                else -> VARIABLE
            }
        }
    }
}

enum class DayPeriodType(val label: String, val preferredHour: Int) {
    MORNING("Matin", 9),
    NOON("Midi", 12),
    AFTERNOON("Après-midi", 16),
    EVENING("Soirée", 20)
}

data class PeriodSlot(
    val type: DayPeriodType,
    val tempC: Int?,
    val label: String?,
    val precipPct: Int?,
)

data class DayPeriod(
    val title: String,
    val minC: Int,
    val maxC: Int,
    val label: String,
    val currentTempC: Int?,
)

data class NamedPeriod(
    val periodName: String,
    val tempC: Int,
    val label: String,
)

data class HourRow(
    val timeLabel: String,
    val tempC: Int,
    val precipPct: Int?,
    val windSpeed: Int,
    val label: String,
)

data class DayForecast(
    val weekdayLabel: String,
    val dayOfMonth: Int,
    val minC: Int,
    val maxC: Int,
    val windSpeed: Int,
    val label: String,
)
