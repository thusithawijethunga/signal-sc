package com.widhura.signalxp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.data.SignalEntity
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextSecondary

@Composable
fun WinLossDistributionChart(
    wins: Int,
    losses: Int,
    winRateStr: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Title on Left and Legend on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Win vs Loss Distribution",
                    color = TextLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem(color = AccentEmerald, label = "Wins")
                    Spacer(modifier = Modifier.width(12.dp))
                    LegendItem(color = AccentRed, label = "Losses")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart with Center Percentage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val total = (wins + losses).coerceAtLeast(1)
                    val winSweep = (wins.toFloat() / total) * 360f
                    val lossSweep = (losses.toFloat() / total) * 360f

                    val strokeWidth = 32.dp.toPx()

                    // Draw Wins Arc
                    drawArc(
                        color = AccentEmerald,
                        startAngle = -90f,
                        sweepAngle = if (total == 0) 180f else winSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )

                    // Draw Losses Arc
                    drawArc(
                        color = AccentRed,
                        startAngle = -90f + winSweep,
                        sweepAngle = if (total == 0) 180f else lossSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }

                // Central Win Rate Text (Large Bold White)
                Text(
                    text = winRateStr,
                    color = TextLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EquityGrowthChart(
    signals: List<SignalEntity>,
    startBalance: Double = 1000.00
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Account Equity Growth Curve",
                color = TextLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Calculate equity points
            val points = mutableListOf<Double>()
            var runningBal = startBalance
            points.add(runningBal)

            signals.forEach {
                runningBal += it.profit
                points.add(runningBal)
            }

            val minVal = 1000.0
            val maxVal = 1180.0
            val yStep = 20.0
            val yLabels = listOf(1180, 1160, 1140, 1120, 1100, 1080, 1060, 1040, 1020, 1000)

            val xLabels = mutableListOf("Start")
            signals.forEachIndexed { idx, _ ->
                xLabels.add("T${idx + 1}")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    yLabels.forEach { label ->
                        Text(
                            text = String.format("%,d", label),
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Chart Area (Graph + X-Axis Labels)
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height

                            // Draw Horizontal Grid Lines matching Y-labels
                            val gridLines = yLabels.size - 1
                            for (i in 0..gridLines) {
                                val y = height * (i.toFloat() / gridLines)
                                drawLine(
                                    color = Color(0xFF1E293B),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            if (points.size > 1) {
                                val stepX = width / (points.size - 1)
                                val linePath = Path()
                                val fillPath = Path()

                                val range = (maxVal - minVal).coerceAtLeast(1.0)

                                points.forEachIndexed { index, valPoint ->
                                    val x = index * stepX
                                    val normalizedY = ((valPoint - minVal) / range).toFloat()
                                    val y = height - (normalizedY * height)

                                    if (index == 0) {
                                        linePath.moveTo(x, y)
                                        fillPath.moveTo(x, height)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        linePath.lineTo(x, y)
                                        fillPath.lineTo(x, y)
                                    }
                                }

                                fillPath.lineTo(width, height)
                                fillPath.close()

                                // Gradient fill under equity curve
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x4010B981),
                                            Color(0x0010B981)
                                        )
                                    )
                                )

                                // Draw Curve Line
                                drawPath(
                                    path = linePath,
                                    color = AccentEmerald,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Draw Data Point Circles
                                points.forEachIndexed { index, valPoint ->
                                    val x = index * stepX
                                    val normalizedY = ((valPoint - minVal) / range).toFloat()
                                    val y = height - (normalizedY * height)

                                    // Point circle with border
                                    drawCircle(
                                        color = AccentEmerald,
                                        radius = 4.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                    drawCircle(
                                        color = CardBackground,
                                        radius = 2.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }
                    }

                    // X-Axis Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        xLabels.forEach { label ->
                            Text(
                                text = label,
                                color = TextSecondary,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp, 8.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

