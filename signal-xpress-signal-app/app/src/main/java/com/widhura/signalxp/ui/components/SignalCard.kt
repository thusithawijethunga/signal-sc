package com.widhura.signalxp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.data.SignalEntity
import com.widhura.signalxp.ui.theme.AccentAmber
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.ActiveBorder
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.BuyBadgeBg
import com.widhura.signalxp.ui.theme.BuyBadgeText
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.LossBorder
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.ProfitBorder
import com.widhura.signalxp.ui.theme.SecondaryBlue
import com.widhura.signalxp.ui.theme.SellBadgeBg
import com.widhura.signalxp.ui.theme.SellBadgeText
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextPrimary
import com.widhura.signalxp.ui.theme.TextSecondary

@Composable
fun SignalCard(
    signal: SignalEntity,
    onReactionToggle: (String) -> Unit,
    isDarkMode: Boolean = true
) {
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val secondary = if (isDarkMode) SecondaryBlue else LightTheme.SecondaryBlue
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val buyBadgeBg = if (isDarkMode) BuyBadgeBg else LightTheme.BuyBadgeBg
    val buyBadgeText = if (isDarkMode) BuyBadgeText else LightTheme.BuyBadgeText
    val sellBadgeBg = if (isDarkMode) SellBadgeBg else LightTheme.SellBadgeBg
    val sellBadgeText = if (isDarkMode) SellBadgeText else LightTheme.SellBadgeText
    val profitBorder = if (isDarkMode) ProfitBorder else LightTheme.ProfitBorder
    val lossBorder = if (isDarkMode) LossBorder else LightTheme.LossBorder
    val activeBorder = if (isDarkMode) ActiveBorder else LightTheme.ActiveBorder
    val accentAmber = if (isDarkMode) AccentAmber else LightTheme.AccentAmber
    val accentEmerald = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald
    val accentRed = if (isDarkMode) AccentRed else LightTheme.AccentRed

    val leftBorderColor = when (signal.result.uppercase()) {
        "WIN" -> profitBorder
        "LOSS" -> lossBorder
        else -> activeBorder
    }

    val statusBarBg = when (signal.result.uppercase()) {
        "WIN" -> accentEmerald.copy(alpha = 0.3f)
        "LOSS" -> accentRed.copy(alpha = 0.3f)
        else -> secondary
    }

    val statusBarText = when (signal.result.uppercase()) {
        "WIN" -> accentEmerald
        "LOSS" -> accentRed.copy(alpha = 0.8f)
        else -> Color.White
    }

    val hitLevel = signal.hitLevel
    val hitLevelInt = hitLevel.toIntOrNull() ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(leftBorderColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\uD83D\uDEA8 SIGNALXPRESS LIVE",
                        color = accentAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val isBuy = signal.type.uppercase() == "BUY"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBuy) buyBadgeBg else sellBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = signal.type.uppercase(),
                            color = if (isBuy) buyBadgeText else sellBadgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "\uD83D\uDC8E Pair: ${signal.pair}", color = textOnBg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "\uD83D\uDCCA Type: ${signal.type}", color = TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "\uD83D\uDCE5 Entry: ${signal.entry}", color = TextPrimary, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        TpLine(label = "TP1", price = signal.tp1, isHit = hitLevelInt >= 1, isDarkMode = isDarkMode)
                        TpLine(label = "TP2", price = signal.tp2, isHit = hitLevelInt >= 2, isDarkMode = isDarkMode)
                        TpLine(label = "TP3", price = signal.tp3, isHit = hitLevelInt >= 3, isDarkMode = isDarkMode)
                        TpLine(label = "TP4", price = signal.tp4, isHit = hitLevelInt >= 4, isDarkMode = isDarkMode)

                        Spacer(modifier = Modifier.height(8.dp))

                        val isSlHit = hitLevel == "SL"
                        val slColor by animateColorAsState(
                            targetValue = if (isSlHit) accentRed else accentRed.copy(alpha = 0.8f),
                            animationSpec = tween(500), label = "slColor"
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isSlHit) "\uD83D\uDED1 SL HIT!" else "\uD83D\uDED1 SL:",
                                color = slColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isSlHit) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${signal.sl}", color = slColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "\uD83C\uDFAF Target Progress:", color = textSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TargetStatusBadge("TP1", isHit = hitLevelInt >= 1, modifier = Modifier.weight(1f), isDarkMode = isDarkMode)
                        TargetStatusBadge("TP2", isHit = hitLevelInt >= 2, modifier = Modifier.weight(1f), isDarkMode = isDarkMode)
                        TargetStatusBadge("TP3", isHit = hitLevelInt >= 3, modifier = Modifier.weight(1f), isDarkMode = isDarkMode)
                        TargetStatusBadge("TP4", isHit = hitLevelInt >= 4, modifier = Modifier.weight(1f), isDarkMode = isDarkMode)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TargetStatusBadge(label = "BE", isHit = hitLevel == "BE", isWarning = true, modifier = Modifier.weight(1f), isDarkMode = isDarkMode)
                        TargetStatusBadge(label = "SL Hit", isHit = hitLevel == "SL", isDanger = true, modifier = Modifier.weight(1f), isDarkMode = isDarkMode)
                        if (hitLevel == "CLOSE") {
                            TargetStatusBadge(label = "Closed", isHit = true, isGray = true, modifier = Modifier.weight(1f), isDarkMode = isDarkMode)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        ReactionButton("\uD83D\uDC4D", signal.thumbsCount, isSelected = signal.userReactedEmoji == "\uD83D\uDC4D", isDarkMode = isDarkMode) { onReactionToggle("\uD83D\uDC4D") }
                        ReactionButton("\uD83D\uDD25", signal.fireCount, isSelected = signal.userReactedEmoji == "\uD83D\uDD25", isDarkMode = isDarkMode) { onReactionToggle("\uD83D\uDD25") }
                        ReactionButton("\uD83D\uDE80", signal.rocketCount, isSelected = signal.userReactedEmoji == "\uD83D\uDE80", isDarkMode = isDarkMode) { onReactionToggle("\uD83D\uDE80") }
                        ReactionButton("\uD83D\uDC94", signal.brokenHeartCount, isSelected = signal.userReactedEmoji == "\uD83D\uDC94", isDarkMode = isDarkMode) { onReactionToggle("\uD83D\uDC94") }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBarBg)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "STATUS: ${signal.status}", color = statusBarText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentRed.copy(alpha = 0.1f))
                        .border(1.5.dp, accentRed, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "⚠️ වැදගත් අවවාදය", color = accentRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Trading කියන්නේ ඉක්මනින් සල්ලි හොයන ක්‍රමයක් නොවෙයි. ලාභ ලැබෙන වගේම පාඩුත් ලැබිය හැකියි. ඔබට අහිමි වුවහොත් දරාගත හැකි මුදලක් පමණක් ආයෝජනය කරන්න.",
                            color = accentRed.copy(alpha = 0.8f), fontSize = 11.sp, lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = accentRed.copy(alpha = 0.3f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Follow Money Management \uD83D\uDE4F\uD83D\uDE4F\uD83D\uDE4F\n\uD83D\uDD25 GET RISK WIN YOUR LIFE \uD83D\uDD25",
                            color = accentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TpLine(label: String, price: String, isHit: Boolean, isDarkMode: Boolean = true) {
    val accentEmerald = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald

    val bgColor by animateColorAsState(
        targetValue = if (isHit) accentEmerald.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(600), label = "tpBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isHit) accentEmerald else accentEmerald.copy(alpha = 0.8f),
        animationSpec = tween(600), label = "tpText"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isHit) bgColor else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isHit) "\uD83C\uDFAF $label HIT \u2713" else "\uD83C\uDFAF $label:",
            color = textColor.copy(alpha = if (isHit) pulseAlpha else 1f),
            fontSize = 13.sp,
            fontWeight = if (isHit) FontWeight.Bold else FontWeight.Medium
        )
        if (!isHit) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = price, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TargetStatusBadge(
    label: String,
    isHit: Boolean,
    isWarning: Boolean = false,
    isDanger: Boolean = false,
    isGray: Boolean = false,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val accentAmber = if (isDarkMode) AccentAmber else LightTheme.AccentAmber
    val accentEmerald = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald
    val accentRed = if (isDarkMode) AccentRed else LightTheme.AccentRed
    val bdr = if (isDarkMode) BorderColor else LightTheme.BorderColor

    val bgColor = when {
        isHit && isWarning -> accentAmber
        isHit && isDanger -> accentRed
        isHit && isGray -> textSec
        isHit -> accentEmerald
        else -> bg
    }

    val borderColor = when {
        isHit && isWarning -> accentAmber
        isHit && isDanger -> accentRed
        isHit && isGray -> textSec
        isHit -> accentEmerald
        else -> bdr
    }

    val textColor = if (isHit) textOnBg else textSec
    val displayLabel = if (isHit && !isWarning && !isDanger && !isGray) "$label \u2713" else label

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = displayLabel, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun ReactionButton(emoji: String, count: Int, isSelected: Boolean, isDarkMode: Boolean = true, onClick: () -> Unit) {
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val secondary = if (isDarkMode) SecondaryBlue else LightTheme.SecondaryBlue
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val bdr = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) secondary else bg)
            .border(1.dp, if (isSelected) primary else bdr, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = count.toString(), color = if (isSelected) textOnBg else textSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
