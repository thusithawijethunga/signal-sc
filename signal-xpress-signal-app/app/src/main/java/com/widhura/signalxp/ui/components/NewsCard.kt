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
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextPrimary
import com.widhura.signalxp.ui.theme.TextSecondary

@Composable
fun NewsCard(news: NewsEntity, isDarkMode: Boolean = true) {
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val accentAmber = if (isDarkMode) AccentAmber else LightTheme.AccentAmber
    val accentRed = if (isDarkMode) AccentRed else LightTheme.AccentRed
    val accentEmerald = if (isDarkMode) com.widhura.signalxp.ui.theme.AccentEmerald else LightTheme.AccentEmerald

    val impactBorderColor = when (news.impact.uppercase()) {
        "HIGH" -> accentRed
        "MEDIUM" -> accentAmber
        else -> primary
    }

    val impactBadgeBg = when (news.impact.uppercase()) {
        "HIGH" -> accentRed.copy(alpha = 0.15f)
        "MEDIUM" -> accentAmber.copy(alpha = 0.15f)
        else -> primary.copy(alpha = 0.15f)
    }

    val impactBadgeText = when (news.impact.uppercase()) {
        "HIGH" -> accentRed.copy(alpha = 0.8f)
        "MEDIUM" -> accentAmber
        else -> primary
    }

    val folderIcon = when (news.impact.uppercase()) {
        "HIGH" -> "\uD83D\uDCC1\uD83D\uDD34"
        "MEDIUM" -> "\uD83D\uDCC1\uD83E\uDD0C"
        else -> "\uD83D\uDCC1\uD83D\uDFE1"
    }

    val currencyFlag = when (news.currency.uppercase()) {
        "USD" -> "\uD83C\uDDFA\uD83C\uDDF8"
        "EUR" -> "\uD83C\uDDEA\uD83C\uDDFA"
        "GBP" -> "\uD83C\uDDEC\uD83C\uDDE7"
        "JPY" -> "\uD83C\uDDEF\uD83C\uDDF5"
        "CAD" -> "\uD83C\uDDE8\uD83C\uDDE6"
        "AUD" -> "\uD83C\uDDE6\uD83C\uDDFA"
        "CHF" -> "\uD83C\uDDE8\uD83C\uDDED"
        "NZD" -> "\uD83C\uDDF3\uD83C\uDDFF"
        else -> "\uD83C\uDF10"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, border, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
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
                            text = if (news.time.isNotBlank()) "\u23F0 ${news.time}" else "",
                            color = textSec,
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
                            text = "${news.impact.uppercase()}",
                            color = impactBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$currencyFlag ${news.currency}",
                            color = primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = news.title,
                        color = textOnBg,
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ACTUAL", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (news.actual.isNotBlank()) news.actual else "-",
                                color = if (news.actual != "-" && news.actual.isNotBlank()) accentEmerald else textOnBg,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(border))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "FORECAST", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (news.forecast.isNotBlank()) news.forecast else "-",
                                color = textOnBg,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(border))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "PREVIOUS", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (news.previous.isNotBlank()) news.previous else "-",
                                color = textSec,
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
