package com.meteo.app.domain

import com.meteo.app.data.api.MetNorwayResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object MetNorwayMapper {
    private val localeFr = Locale.FRENCH
    private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun buildUi(response: MetNorwayResponse, locationLabel: String): WeatherScreenUi {
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
            HourRowUi(
                timeLabel = t.format(hourMinuteFormatter),
                tempC = it.data.instant.details.airTemperature?.toInt() ?: 0,
                precipPct = it.data.next1h?.details?.precipitationProbability?.toInt(),
                windSpeed = it.data.instant.details.windSpeed?.toInt() ?: 0,
                label = it.data.next1h?.summary?.symbolCode?.replace("_", " ")?.capitalize() ?: "Inconnu"
            )
        }

        // Aggregate daily (Simplified for fallback)
        val daily5 = series.groupBy { it.time.take(10) }.map { (dateStr, dayData) ->
            val date = LocalDate.parse(dateStr)
            val temps = dayData.mapNotNull { it.data.instant.details.airTemperature }
            val winds = dayData.mapNotNull { it.data.instant.details.windSpeed }
            DayForecastUi(
                weekdayLabel = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, localeFr).capitalize(),
                dayOfMonth = date.dayOfMonth,
                minC = temps.minOrNull()?.toInt() ?: 0,
                maxC = temps.maxOrNull()?.toInt() ?: 0,
                windSpeed = winds.maxOrNull()?.toInt() ?: 0,
                label = dayData.firstOrNull { it.data.next6h != null }?.data?.next6h?.summary?.symbolCode?.replace("_", " ")?.capitalize() ?: "Variable"
            )
        }.take(15)

        val firstDay = daily5.first()
        val today = DayPeriodUi(
            title = "Aujourd'hui",
            minC = firstDay.minC,
            maxC = firstDay.maxC,
            label = firstDay.label,
            currentTempC = currentSlot.data.instant.details.airTemperature?.toInt()
        )

        return WeatherScreenUi(
            locationLabel = locationLabel,
            overview = WeatherOverviewUi(today = today, tomorrowSlots = emptyList()), // Simplified
            hourly12 = hourly12,
            daily5 = daily5
        )
    }

    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeFr) else it.toString() }
}
