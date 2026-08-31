package com.widhura.signalxp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.data.NewsEntity
import com.widhura.signalxp.ui.theme.AccentAmber
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextPrimary
import com.widhura.signalxp.ui.theme.TextSecondary

@Composable
fun NewsCard(news: NewsEntity) {
    val impactBorderColor = when (news.impact.uppercase()) {
        "HIGH" -> AccentRed
        "MEDIUM" -> AccentAmber
        else -> PrimarySky
    }

    val impactBadgeBg = when (news.impact.uppercase()) {
        "HIGH" -> Color(0xFF450A0A)
        "MEDIUM" -> Color(0xFF451A03)
        else -> Color(0xFF172554)
    }

    val impactBadgeText = when (news.impact.uppercase()) {
        "HIGH" -> Color(0xFFFCA5A5)
        "MEDIUM" -> Color(0xFFFCD34D)
        else -> Color(0xFF93C5FD)
    }

    val folderIcon = when (news.impact.uppercase()) {
        "HIGH" -> "📁🔴" // Red Folder
        "MEDIUM" -> "📁🟠" // Orange Folder
        else -> "📁🟡" // Yellow Folder
    }

    val currencyFlag = when (news.currency.uppercase()) {
        "USD" -> "🇺🇸"
        "EUR" -> "🇪🇺"
        "GBP" -> "🇬🇧"
        "JPY" -> "🇯🇵"
        "CAD" -> "🇨🇦"
        "AUD" -> "🇦🇺"
        "CHF" -> "🇨🇭"
        "NZD" -> "🇳🇿"
        else -> "🌐"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Impact Border Left
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(140.dp)
                    .background(impactBorderColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header: Time, Impact Badge & Folder Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = folderIcon,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "⏰ ${news.time}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(impactBadgeBg)
                            .border(1.dp, impactBorderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${news.impact.uppercase()} IMPACT",
                            color = impactBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Currency Tag & Event Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkBackground)
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$currencyFlag ${news.currency}",
                            color = PrimarySky,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = news.title,
                        color = TextLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (news.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = news.description,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Forex Factory Metrics Box: Actual | Forecast | Previous
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground)
                        .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Actual Metric
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ACTUAL", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (news.actual.isNotBlank()) news.actual else "-",
                                color = if (news.actual != "-" && news.actual.isNotBlank()) com.widhura.signalxp.ui.theme.AccentEmerald else TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(BorderColor))

                        // Forecast Metric
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "FORECAST", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (news.forecast.isNotBlank()) news.forecast else "-",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(BorderColor))

                        // Previous Metric
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "PREVIOUS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (news.previous.isNotBlank()) news.previous else "-",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
