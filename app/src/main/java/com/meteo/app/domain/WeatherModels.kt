package com.meteo.app.domain

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

enum class WeatherCondition(val description: String) {
    CLEAR("Dégagé"),
    PARTLY_CLOUDY("Partiellement nuageux"),
    FOG("Brouillard"),
    DRIZZLE("Bruine"),
    FREEZING_DRIZZLE("Bruine verglaçante"),
    RAIN("Pluie"),
    FREEZING_RAIN("Pluie verglaçante"),
    SNOW("Neige"),
    SNOW_GRAINS("Grains de neige"),
    SHOWERS("Averses"),
    SNOW_SHOWERS("Averses de neige"),
    THUNDERSTORM("Orages"),
    THUNDERSTORM_HAIL("Orages avec grêle"),
    VARIABLE("Variable"),
    UNKNOWN("—");

    companion object {
        fun fromWMOCode(code: Int?): WeatherCondition {
            return when (code) {
                null -> UNKNOWN
                0 -> CLEAR
                1, 2, 3 -> PARTLY_CLOUDY
                45, 48 -> FOG
                51, 53, 55 -> DRIZZLE
                56, 57 -> FREEZING_DRIZZLE
                61, 63, 65 -> RAIN
                66, 67 -> FREEZING_RAIN
                71, 73, 75 -> SNOW
                77 -> SNOW_GRAINS
                80, 81, 82 -> SHOWERS
                85, 86 -> SNOW_SHOWERS
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
