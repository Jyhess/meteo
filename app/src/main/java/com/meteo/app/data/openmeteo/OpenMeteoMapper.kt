package com.meteo.app.data.openmeteo

import com.meteo.app.data.api.ForecastResponse
import com.meteo.app.domain.WeatherData
import com.meteo.app.domain.DayForecast
import com.meteo.app.domain.HourRow
import com.meteo.app.domain.DayPeriod
import com.meteo.app.domain.DayPeriodType
import com.meteo.app.domain.NamedPeriod
import com.meteo.app.domain.PeriodSlot
import com.meteo.app.domain.WeatherOverview
import com.meteo.app.domain.WeatherCondition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

object OpenMeteoMapper {

    private val localeFr = Locale.FRENCH

    private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private fun parseLocalDateTime(iso: String): LocalDateTime {
        val base = iso.takeWhile { (it != '[') && (it != '+') && (it != 'Z') }
        return try {
            LocalDateTime.parse(base)
        } catch (_: Exception) {
            if (base.count { it == ':' } == 1) {
                LocalDateTime.parse("$base:00")
            } else {
                LocalDateTime.parse(base.take(19))
            }
        }
    }

    private fun weekdayShort(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, localeFr)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeFr) else it.toString() }

    fun buildUi(
        response: ForecastResponse,
        locationLabel: String,
    ): WeatherData {
        val hourly = response.hourly
        val daily = response.daily
        val current = response.current

        val hourlyTimes = hourly?.time.orEmpty()
        val hourlyTemps = hourly?.temperature.orEmpty()
        val hourlyCodes = hourly?.weatherCode.orEmpty()
        val hourlyPrecip = hourly?.precipitationProbability.orEmpty()
        val hourlyWind = hourly?.windSpeed.orEmpty()

        val dailyDates = daily?.time.orEmpty()
        val dailyMax = daily?.maxC.orEmpty()
        val dailyMin = daily?.minC.orEmpty()
        val dailyCodes = daily?.weatherCode.orEmpty()
        val dailyWind = daily?.windSpeedMax.orEmpty()

        val now = LocalDateTime.now()
        val slotStart = now.withMinute(0).withSecond(0).withNano(0)
        val startIndex = hourlyTimes.indexOfFirst { t ->
            runCatching { parseLocalDateTime(t) }.getOrNull()?.let { it >= slotStart } == true
        }.takeIf { it >= 0 } ?: 0

        val hourly12 = (startIndex until minOf(startIndex + 12, hourlyTimes.size)).map { i ->
            val t = hourlyTimes[i]
            val timeLabel = runCatching {
                parseLocalDateTime(t).format(hourMinuteFormatter)
            }.getOrDefault("—")
            HourRow(
                timeLabel = timeLabel,
                tempC = hourlyTemps.getOrNull(i)?.roundToInt() ?: 0,
                precipPct = hourlyPrecip.getOrNull(i),
                windSpeed = hourlyWind.getOrNull(i)?.roundToInt() ?: 0,
                label = WeatherCondition.fromWMOCode(hourlyCodes.getOrNull(i)).description,
            )
        }

        val todayMin = dailyMin.getOrNull(0)?.roundToInt() ?: 0
        val todayMax = dailyMax.getOrNull(0)?.roundToInt() ?: 0
        val todayCode = dailyCodes.getOrNull(0)
        val currentTemp = current?.temperature?.roundToInt()
            ?: hourlyTemps.getOrNull(startIndex)?.roundToInt()

        val today = DayPeriod(
            title = "Aujourd'hui",
            minC = todayMin,
            maxC = todayMax,
            label = WeatherCondition.fromWMOCode(todayCode).description,
            currentTempC = currentTemp,
        )

        val tomorrowDateStr = dailyDates.getOrNull(1)
        val tomorrowLocalDate = tomorrowDateStr?.let { LocalDate.parse(it) }

        val targets = listOf(
            NamedTarget("Matin", 9),
            NamedTarget("Midi", 12),
            NamedTarget("Après-midi", 16),
            NamedTarget("Soirée", 20),
        )

        val tomorrowSlots = if (tomorrowLocalDate != null) {
            targets.map { target ->
                val best = findBestHourlyForDay(
                    date = tomorrowLocalDate,
                    preferredHour = target.preferredHour,
                    hourlyTimes = hourlyTimes,
                    hourlyTemps = hourlyTemps,
                    hourlyCodes = hourlyCodes,
                )
                NamedPeriod(
                    periodName = target.name,
                    tempC = best.first,
                    label = WeatherCondition.fromWMOCode(best.second).description,
                )
            }
        } else {
            emptyList()
        }

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
                val candidates = hourlyTimes.mapIndexedNotNull { idx, iso ->
                    val t = runCatching { parseLocalDateTime(iso) }.getOrNull() ?: return@mapIndexedNotNull null
                    if (t.toLocalDate() != date) return@mapIndexedNotNull null
                    Triple(idx, abs(t.hour - type.preferredHour), t.hour)
                }
                val best = candidates.minByOrNull { it.second }
                if (best != null) {
                    val i = best.first
                    PeriodSlot(
                        type = type,
                        date = date,
                        tempC = hourlyTemps.getOrNull(i)?.roundToInt(),
                        label = WeatherCondition.fromWMOCode(hourlyCodes.getOrNull(i)).description,
                        precipPct = hourlyPrecip.getOrNull(i),
                        windSpeed = hourlyWind.getOrNull(i)?.roundToInt()
                    )
                } else {
                    PeriodSlot(type = type, date = date, tempC = null, label = null, precipPct = null, windSpeed = null)
                }
            }

        val daily5 = dailyDates.asSequence().take(15).mapIndexed { index, dateStr ->
            val d = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: LocalDate.now()
            val minC = dailyMin.getOrNull(index)?.roundToInt() ?: 0
            val maxC = dailyMax.getOrNull(index)?.roundToInt() ?: 0
            val windSpeed = dailyWind.getOrNull(index)?.roundToInt() ?: 0
            DayForecast(
                weekdayLabel = weekdayShort(d),
                dayOfMonth = d.dayOfMonth,
                minC = minC,
                maxC = maxC,
                windSpeed = windSpeed,
                label = WeatherCondition.fromWMOCode(dailyCodes.getOrNull(index)).description,
            )
        }.toList()

        return WeatherData(
            locationLabel = locationLabel,
            overview = WeatherOverview(
                today = today,
                tomorrowSlots = tomorrowSlots,
                periodSlots = periodSlots,
            ),
            hourly12 = hourly12,
            daily5 = daily5,
        )
    }

    private data class NamedTarget(val name: String, val preferredHour: Int)

    private fun findBestHourlyForDay(
        date: LocalDate,
        preferredHour: Int,
        hourlyTimes: List<String>,
        hourlyTemps: List<Double>,
        hourlyCodes: List<Int>,
    ): Pair<Int, Int?> {
        val candidates = hourlyTimes.mapIndexedNotNull { idx, iso ->
            val t = runCatching { parseLocalDateTime(iso) }.getOrNull() ?: return@mapIndexedNotNull null
            if (t.toLocalDate() != date) return@mapIndexedNotNull null
            Triple(idx, abs(t.hour - preferredHour), t.hour)
        }
        if (candidates.isEmpty()) {
            return 0 to null
        }
        val best = candidates.minBy { it.second }
        val i = best.first
        val temp = hourlyTemps.getOrNull(i)?.roundToInt() ?: 0
        val code = hourlyCodes.getOrNull(i)
        return temp to code
    }

    private fun Double.roundToInt(): Int = round(this).toInt()
}
