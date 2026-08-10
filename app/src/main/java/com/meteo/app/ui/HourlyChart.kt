package com.meteo.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.domain.HourRow
import kotlin.math.max
import kotlin.math.pow

private val TempCurveColor = Color(0xFFFFD54F)
private val PrecipColor = Color(0xFF0D47A1)
private val PrecipBarColor = PrecipColor.copy(alpha = 0.9f)
private val WindCurveColor = Color.White

private const val ChartTopFraction = 0.05f
private const val ChartBottomFraction = 0.95f
private const val ChartHeightFraction = ChartBottomFraction - ChartTopFraction

private const val WindExponent = 0.7f
private const val WindMaxLimit = 60f

private const val PrecipExponent = 0.7f
private const val PrecipMaxLimit = 30f

@Composable
internal fun HourlyChart(hours: List<HourRow>) {
    // On récupère les valeurs pour l'échelle fixe
    val temps = remember(hours) { hours.map { it.tempC.toFloat() } }
    val vMin = temps.minOrNull() ?: 0f
    val vMax = temps.maxOrNull() ?: vMin
    val scaleMin = vMin - 5f
    val scaleMax = vMax + 5f
    val range = max(scaleMax - scaleMin, 1f)

    val windMaxLimit = WindMaxLimit
    val windExponent = WindExponent
    val maxPower = windMaxLimit.pow(windExponent)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.25f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(bottom = 8.dp),
    ) {
        val fullMaxHeight = 168.dp // Hauteur fixée du canvas (112 * 1.5)
        val chartHeightDp = fullMaxHeight * ChartHeightFraction
        val chartBottomDp = fullMaxHeight * ChartBottomFraction

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            HourlyCurvesCanvas(
                hours = hours,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fullMaxHeight)
                    .padding(top = 4.dp),
            )
            HourlyTimeLabels(
                hours = hours,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF003366).copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 2.dp),
            )
        }

        // Échelles fixes au-dessus du graphique
        // Temperature Scale (Left)
        listOf(vMax, (vMin + vMax) / 2f, vMin).forEach { valDeg ->
            val normalized = ((valDeg - scaleMin) / range).coerceIn(0f, 1f)
            val yOffset = chartBottomDp - (chartHeightDp * normalized)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 4.dp, y = yOffset - 11.dp + 4.dp)
                    .background(
                        color = Color(0xFF003366).copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "${valDeg.toInt()}°",
                    color = getTempColor(valDeg),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Joint Wind & Rain Scale (Right)
        listOf(10f to 5f, 20f to 10f, 40f to 20f, 60f to 30f).forEach { (windVal, rainVal) ->
            val normalized = (windVal.pow(windExponent) / maxPower).coerceIn(0f, 1f)
            val yOffset = chartBottomDp - (chartHeightDp * normalized)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = yOffset - 11.dp + 4.dp)
                    .background(
                        color = Color(0xFF003366).copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rainVal.toInt().toString(),
                    color = PrecipColor.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = windVal.toInt().toString(),
                    color = WindCurveColor.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun HourlyTimeLabels(
    hours: List<HourRow>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        hours.forEachIndexed { index, hour ->
            // On affiche les labels toutes les 4 heures pour éviter les chevauchements sur petit écran
            val showLabel = (index % 4 == 0) || (index == hours.lastIndex)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (showLabel) {
                    Text(
                        text = compactHourLabel(hour.timeLabel),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 13.sp,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyCurvesCanvas(
    hours: List<HourRow>,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 2.dp.toPx() }

    val temps = remember(hours) { hours.map { it.tempC.toFloat() } }
    val winds = remember(hours) { hours.map { it.windSpeed.toFloat() } }

    if (hours.isEmpty()) return

    val vMin = temps.minOrNull() ?: 0f
    val vMax = temps.maxOrNull() ?: vMin

    val scaleMin = vMin - 5f
    val scaleMax = vMax + 5f
    val range = max(scaleMax - scaleMin, 1f)

    val normalizedTemps = remember(temps, scaleMin, range) {
        temps.map { ((it - scaleMin) / range).coerceIn(0f, 1f) }
    }

    val windMaxLimit = WindMaxLimit
    val windExponent = WindExponent
    val maxPower = windMaxLimit.pow(windExponent)

    val normalizedWinds = remember(winds, maxPower) {
        winds.map { (it.coerceIn(0f, windMaxLimit).pow(windExponent) / maxPower).coerceIn(0f, 1f) }
    }

    BoxWithConstraints(modifier = modifier) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (hours.isEmpty()) return@Canvas

            val chartTop = size.height * ChartTopFraction
            val chartBottom = size.height * ChartBottomFraction
            val chartHeight = chartBottom - chartTop
            val slotWidth = size.width / hours.size

            fun xAt(index: Int): Float = (slotWidth * index) + (slotWidth / 2f)
            fun yAt(normalized: Float): Float = chartBottom - normalized * chartHeight

            fun buildPoints(values: List<Float>): List<Offset> =
                values.mapIndexed { index, value -> Offset(xAt(index), yAt(value)) }

            drawPrecipBars(
                hours = hours,
                chartLeft = 0f,
                chartRight = size.width,
                chartBottom = chartBottom,
                chartHeight = chartHeight,
            )

            val tempPoints = buildPoints(normalizedTemps)
            val windPoints = buildPoints(normalizedWinds)

            val tempFillPath = Path().apply {
                if (tempPoints.isNotEmpty()) {
                    moveTo(tempPoints.first().x, chartBottom)
                    tempPoints.forEach { lineTo(it.x, it.y) }
                    lineTo(tempPoints.last().x, chartBottom)
                    close()
                }
            }

            val fillBrush = Brush.verticalGradient(
                colors = listOf(getTempColor(scaleMax, 0.3f), getTempColor(scaleMin, 0.3f)),
                startY = chartTop,
                endY = chartBottom
            )
            drawPath(tempFillPath, fillBrush)

            drawSeries(tempPoints, TempCurveColor, strokeWidth)
            drawSeries(windPoints, WindCurveColor, strokeWidth)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPrecipBars(
    hours: List<HourRow>,
    chartLeft: Float,
    chartRight: Float,
    chartBottom: Float,
    chartHeight: Float,
) {
    val slotCount = hours.size
    if (slotCount == 0) return

    val slotWidth = (chartRight - chartLeft) / slotCount
    val barWidth = slotWidth * 0.55f
    val precipMaxPower = PrecipMaxLimit.pow(PrecipExponent)

    hours.forEachIndexed { index, hour ->
        val precip = hour.precipAmount ?: return@forEachIndexed
        if (precip <= 0) return@forEachIndexed

        val scaledFraction = (precip.coerceIn(0f, PrecipMaxLimit).pow(PrecipExponent) / precipMaxPower).coerceIn(0f, 1f)
        // Ajout d'une hauteur minimale de 5 pixels pour rendre les barres visibles dès que precip > 0
        val barHeight = (scaledFraction * chartHeight).coerceAtLeast(5f)
        val centerX = chartLeft + (slotWidth * index) + (slotWidth / 2f)

        drawRoundRect(
            color = PrecipBarColor,
            topLeft = Offset(centerX - (barWidth / 2f), chartBottom - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeries(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
) {
    if (points.size < 2) {
        points.firstOrNull()?.let { drawCircle(color, radius = strokeWidth, center = it) }
        return
    }
    val linePath = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (index in 1 until points.size) {
            lineTo(points[index].x, points[index].y)
        }
    }
    drawPath(linePath, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

private fun compactHourLabel(timeLabel: String): String {
    // On extrait uniquement les chiffres du début du label
    val hourMatch = Regex("""\d+""").find(timeLabel)
    val hourPart = hourMatch?.value ?: "0"
    // On enlève le '0' initial (ex: "01" -> "1") mais on garde "0" si c'est minuit
    val cleanedHour = hourPart.trimStart('0').ifEmpty { "0" }
    return "${cleanedHour}h"
}
