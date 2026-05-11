package com.meteo.app.domain

import com.meteo.app.data.api.ForecastResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object WeatherMapper {

    private val localeFr = Locale.FRENCH

    fun describeCode(code: Int?): String {
        if (code == null) return "—"
        return when (code) {
            0 -> "Dégagé"
            1, 2, 3 -> "Partiellement nuageux"
            45, 48 -> "Brouillard"
            51, 53, 55 -> "Bruine"
            56, 57 -> "Bruine verglaçante"
            61, 63, 65 -> "Pluie"
            66, 67 -> "Pluie verglaçante"
            71, 73, 75 -> "Neige"
            77 -> "Grains de neige"
            80, 81, 82 -> "Averses"
            85, 86 -> "Averses de neige"
            95 -> "Orages"
            96, 99 -> "Orages avec grêle"
            else -> "Variable"
        }
    }

    private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val mediumDateFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(localeFr)

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
        date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, localeFr)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeFr) else it.toString() }

    fun buildUi(
        response: ForecastResponse,
        locationLabel: String,
    ): WeatherScreenUi {
        val hourly = response.hourly
        val daily = response.daily
        val current = response.current

        val hourlyTimes = hourly?.time.orEmpty()
        val hourlyTemps = hourly?.temperature.orEmpty()
        val hourlyCodes = hourly?.weatherCode.orEmpty()
        val hourlyPrecip = hourly?.precipitationProbability.orEmpty()

        val dailyDates = daily?.time.orEmpty()
        val dailyMax = daily?.maxC.orEmpty()
        val dailyMin = daily?.minC.orEmpty()
        val dailyCodes = daily?.weatherCode.orEmpty()

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
            HourRowUi(
                timeLabel = timeLabel,
                tempC = hourlyTemps.getOrNull(i)?.roundToInt() ?: 0,
                precipPct = hourlyPrecip.getOrNull(i),
                label = describeCode(hourlyCodes.getOrNull(i)),
            )
        }

        val todayMin = dailyMin.getOrNull(0)?.roundToInt() ?: 0
        val todayMax = dailyMax.getOrNull(0)?.roundToInt() ?: 0
        val todayCode = dailyCodes.getOrNull(0)
        val currentTemp = current?.temperature?.roundToInt()
            ?: hourlyTemps.getOrNull(startIndex)?.roundToInt()

        val today = DayPeriodUi(
            title = "Aujourd'hui",
            minC = todayMin,
            maxC = todayMax,
            label = describeCode(todayCode),
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
                NamedPeriodUi(
                    periodName = target.name,
                    tempC = best.first,
                    label = describeCode(best.second),
                )
            }
        } else {
            emptyList()
        }

        val daily5 = dailyDates.asSequence().take(5).mapIndexed { index, dateStr ->
            val d = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: LocalDate.now()
            val minC = dailyMin.getOrNull(index)?.roundToInt() ?: 0
            val maxC = dailyMax.getOrNull(index)?.roundToInt() ?: 0
            DayForecastUi(
                weekdayLabel = weekdayShort(d),
                dateLabel = d.format(mediumDateFormatter),
                minC = minC,
                maxC = maxC,
                label = describeCode(dailyCodes.getOrNull(index)),
            )
        }.toList()

        return WeatherScreenUi(
            locationLabel = locationLabel,
            overview = WeatherOverviewUi(
                today = today,
                tomorrowSlots = tomorrowSlots,
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
            Triple(idx, kotlin.math.abs(t.hour - preferredHour), t.hour)
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

    private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()
}

data class WeatherScreenUi(
    val locationLabel: String,
    val overview: WeatherOverviewUi,
    val hourly12: List<HourRowUi>,
    val daily5: List<DayForecastUi>,
)

data class WeatherOverviewUi(
    val today: DayPeriodUi,
    val tomorrowSlots: List<NamedPeriodUi>,
)

data class DayPeriodUi(
    val title: String,
    val minC: Int,
    val maxC: Int,
    val label: String,
    val currentTempC: Int?,
)

data class NamedPeriodUi(
    val periodName: String,
    val tempC: Int,
    val label: String,
)

data class HourRowUi(
    val timeLabel: String,
    val tempC: Int,
    val precipPct: Int?,
    val label: String,
)

data class DayForecastUi(
    val weekdayLabel: String,
    val dateLabel: String,
    val minC: Int,
    val maxC: Int,
    val label: String,
)
