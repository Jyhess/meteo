package com.meteo.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.domain.DayForecast
import kotlin.math.max
import kotlin.math.pow

private val WindBarColor = Color.White.copy(alpha = 0.9f)
private val PrecipCurveColor = Color(0xFF0D47A1)

@Composable
internal fun DailyChart(days: List<DayForecast>) {
    if (days.isEmpty()) return
    val displayDays = days.take(7)

    val minTemps = displayDays.map { it.minC.toFloat() }
    val maxTemps = displayDays.map { it.maxC.toFloat() }
    val precipPcts = displayDays.map { it.precipPct?.toFloat() ?: 0f }

    val overallMinTemp = minTemps.minOrNull() ?: 0f
    val overallMaxTemp = maxTemps.maxOrNull() ?: 20f
    val tempScaleMin = overallMinTemp - 3f
    val tempScaleMax = overallMaxTemp + 3f
    val tempRange = max(tempScaleMax - tempScaleMin, 1f)

    val windMaxLimit = 60f
    val windExponent = 0.7f
    val windMaxPower = windMaxLimit.pow(windExponent)

    val chartPaddingHorizontal = 32.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        // Legend at the Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(Color(0xFFFFD54F), "Temp")
            Spacer(Modifier.width(16.dp))
            LegendItem(Color.White, "Vent")
            Spacer(Modifier.width(16.dp))
            LegendItem(PrecipCurveColor, "Pluie %")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = chartPaddingHorizontal)) {
                val chartHeight = size.height
                val chartWidth = size.width
                val slotWidth = chartWidth / displayDays.size

                displayDays.forEachIndexed { index, day ->
                    val xCenter = slotWidth * index + slotWidth / 2f
                    
                    // 1. Temp Bar (Gradient) - Centered
                    val yMaxTemp = chartHeight * (1f - (day.maxC - tempScaleMin) / tempRange)
                    val yMinTemp = chartHeight * (1f - (day.minC - tempScaleMin) / tempRange)
                    val barWidth = 10.dp.toPx()

                    val colorMax = getTempColor(day.maxC.toFloat())
                    val colorMin = getTempColor(day.minC.toFloat())
                    
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(colorMax, colorMin),
                            startY = yMaxTemp,
                            endY = yMinTemp
                        ),
                        topLeft = Offset(xCenter - barWidth / 2f, yMaxTemp),
                        size = Size(barWidth, (yMinTemp - yMaxTemp).coerceAtLeast(2.dp.toPx())),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )

                    // 2. Wind Bar (White) - Sightly to the right of temp bar
                    val windNormMax = (day.maxWindSpeed.toFloat().coerceIn(0f, windMaxLimit).pow(windExponent) / windMaxPower)
                    val windNormMin = (day.minWindSpeed.toFloat().coerceIn(0f, windMaxLimit).pow(windExponent) / windMaxPower)
                    val yMaxWind = chartHeight * (1f - windNormMax)
                    val yMinWind = chartHeight * (1f - windNormMin)
                    
                    val windX = xCenter + barWidth / 2f + 4.dp.toPx()
                    drawLine(
                        color = WindBarColor,
                        start = Offset(windX, yMaxWind),
                        end = Offset(windX, yMinWind),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 3. Precip Curve (Blue)
                if (precipPcts.any { it > 0 }) {
                    val precipPoints = precipPcts.mapIndexed { index, pct ->
                        val x = slotWidth * index + slotWidth / 2f
                        val y = chartHeight * (1f - (pct / 100f))
                        Offset(x, y)
                    }
                    
                    if (precipPoints.size >= 2) {
                        val path = Path().apply {
                            moveTo(precipPoints.first().x, precipPoints.first().y)
                            for (i in 1 until precipPoints.size) {
                                lineTo(precipPoints[i].x, precipPoints[i].y)
                            }
                        }

                        // Fill under the path
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(precipPoints.last().x, chartHeight)
                            lineTo(precipPoints.first().x, chartHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(PrecipCurveColor.copy(alpha = 0.3f), Color.Transparent),
                                startY = precipPoints.map { it.y }.minOrNull() ?: 0f,
                                endY = chartHeight
                            )
                        )

                        drawPath(
                            path = path,
                            color = PrecipCurveColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // SCALES (inside the Box to align correctly with the Canvas)
            val chartHeightDp = 140.dp

            // Temperature Scale (Left)
            listOf(overallMaxTemp, (overallMinTemp + overallMaxTemp) / 2f, overallMinTemp).forEach { valDeg ->
                val normalized = ((valDeg - tempScaleMin) / tempRange).coerceIn(0f, 1f)
                val yOffset = chartHeightDp * (1f - normalized)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 4.dp, y = yOffset)
                        .background(
                            color = Color(0xFF003366).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "${valDeg.toInt()}°",
                        color = getTempColor(valDeg),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Wind Scale (Right)
            listOf(10f, 20f, 40f, 60f).forEach { windVal ->
                val normalized = (windVal.pow(windExponent) / windMaxPower).coerceIn(0f, 1f)
                val yOffset = chartHeightDp * (1f - normalized)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = yOffset)
                        .background(
                            color = Color(0xFF003366).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = windVal.toInt().toString(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Weather Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = chartPaddingHorizontal)
        ) {
            displayDays.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    WeatherIcon(day.label, modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Days Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = chartPaddingHorizontal)
                .background(
                    color = Color(0xFF003366).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(vertical = 2.dp)
        ) {
            displayDays.forEach { day ->
                Text(
                    text = day.weekdayLabel.take(3),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
    }
}
