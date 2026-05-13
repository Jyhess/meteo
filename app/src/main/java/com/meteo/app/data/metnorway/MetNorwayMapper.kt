package com.meteo.app.data.metnorway

import com.meteo.app.data.api.MetNorwayResponse
import com.meteo.app.domain.WeatherCondition
import com.meteo.app.domain.DayForecast
import com.meteo.app.domain.DayPeriod
import com.meteo.app.domain.HourRow
import com.meteo.app.domain.DayPeriodType
import com.meteo.app.domain.PeriodSlot
import com.meteo.app.domain.WeatherOverview
import com.meteo.app.domain.WeatherData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object MetNorwayMapper {
    private val localeFr = Locale.FRENCH
    private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val symbolToCondition = mapOf(
        "clearsky" to WeatherCondition.CLEAR,
        "fair" to WeatherCondition.PARTLY_CLOUDY,
        "partlycloudy" to WeatherCondition.PARTLY_CLOUDY,
        "cloudy" to WeatherCondition.PARTLY_CLOUDY,
        "fog" to WeatherCondition.FOG,
        "lightrain" to WeatherCondition.LIGHT_RAIN,
        "rain" to WeatherCondition.MODERATE_RAIN,
        "heavyrain" to WeatherCondition.HEAVY_RAIN,
        "lightsnow" to WeatherCondition.SLIGHT_SNOWFALL,
        "snow" to WeatherCondition.MODERATE_SNOWFALL,
        "heavysnow" to WeatherCondition.HEAVY_SNOWFALL,
        "thunderstorm" to WeatherCondition.THUNDERSTORM
    )

    private fun String.toDescription(): String {
        val key = this.split("_").first().lowercase()
        return (symbolToCondition[key] ?: WeatherCondition.VARIABLE).description
    }

    fun buildUi(response: MetNorwayResponse, locationLabel: String): WeatherData {
        val series = response.properties.timeseries
        val now = LocalDateTime.now()

        // Current/Today
        val currentSlot = series.find { LocalDateTime.parse(it.time.take(19)) >= now.withMinute(0) }
            ?: series.first()

        val hourly12 = series.filter {
            val t = LocalDateTime.parse(it.time.take(19))
            t >= now.withMinute(0)
        }.take(12).map {
            val t = LocalDateTime.parse(it.time.take(19))
            HourRow(
                timeLabel = t.format(hourMinuteFormatter),
                tempC = it.data.instant.details.airTemperature?.toInt() ?: 0,
                precipPct = it.data.next1h?.details?.precipitationProbability?.toInt(),
                windSpeed = it.data.instant.details.windSpeed?.toInt() ?: 0,
                label = it.data.next1h?.summary?.symbolCode?.toDescription() ?: WeatherCondition.UNKNOWN.description
            )
        }

        // Aggregate daily (Simplified for fallback)
        val daily5 = series.groupBy { it.time.take(10) }.map { (dateStr, dayData) ->
            val date = LocalDate.parse(dateStr)
            val temps = dayData.mapNotNull { it.data.instant.details.airTemperature }
            val winds = dayData.mapNotNull { it.data.instant.details.windSpeed }
            DayForecast(
                weekdayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, localeFr)
                    .capitalize(),
                dayOfMonth = date.dayOfMonth,
                minC = temps.minOrNull()?.toInt() ?: 0,
                maxC = temps.maxOrNull()?.toInt() ?: 0,
                windSpeed = winds.maxOrNull()?.toInt() ?: 0,
                label = dayData.firstOrNull { it.data.next6h != null }?.data?.next6h?.summary?.symbolCode?.toDescription() ?: WeatherCondition.VARIABLE.description
            )
        }.take(15)

        val firstDay = daily5.first()
        val today = DayPeriod(
            title = "Aujourd'hui",
            minC = firstDay.minC,
            maxC = firstDay.maxC,
            label = firstDay.label,
            currentTempC = currentSlot.data.instant.details.airTemperature?.toInt()
        )

        val currentNow = LocalDateTime.now()
        val allTargetTimes = (0..1).flatMap { dayOffset ->
            val date = currentNow.toLocalDate().plusDays(dayOffset.toLong())
            DayPeriodType.entries.map { type ->
                date to type
            }
        }

        val periodSlots = allTargetTimes
            .filter { (date, type) ->
                val targetTime = date.atTime(type.preferredHour, 0)
                targetTime >= currentNow.minusHours(1)
            }
            .take(4)
            .map { (date, type) ->
                val best = series.filter {
                    val t = LocalDateTime.parse(it.time.take(19))
                    t.toLocalDate() == date
                }.minByOrNull {
                    val t = LocalDateTime.parse(it.time.take(19))
                    kotlin.math.abs(t.hour - type.preferredHour)
                }
                if (best != null) {
                    PeriodSlot(
                        type = type,
                        date = date,
                        tempC = best.data.instant.details.airTemperature?.toInt(),
                        label = (best.data.next1h ?: best.data.next6h)?.summary?.symbolCode?.toDescription(),
                        precipPct = (best.data.next1h ?: best.data.next6h)?.details?.precipitationProbability?.toInt(),
                        windSpeed = best.data.instant.details.windSpeed?.toInt()
                    )
                } else {
                    PeriodSlot(type = type, date = date, tempC = null, label = null, precipPct = null, windSpeed = null)
                }
            }

        return WeatherData(
            locationLabel = locationLabel,
            overview = WeatherOverview(
                today = today,
                tomorrowSlots = emptyList(),
                periodSlots = periodSlots
            ), // Simplified
            hourly12 = hourly12,
            daily5 = daily5
        )
    }

    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeFr) else it.toString() }
}