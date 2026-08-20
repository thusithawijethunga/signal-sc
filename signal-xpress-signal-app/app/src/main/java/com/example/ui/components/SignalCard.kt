package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.SignalEntity
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.ActiveBorder
import com.example.ui.theme.BorderColor
import com.example.ui.theme.BuyBadgeBg
import com.example.ui.theme.BuyBadgeText
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardHeaderBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LossBorder
import com.example.ui.theme.PrimarySky
import com.example.ui.theme.ProfitBorder
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SellBadgeBg
import com.example.ui.theme.SellBadgeText
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SignalCard(
    signal: SignalEntity,
    onReactionToggle: (String) -> Unit
) {
    val leftBorderColor = when (signal.result.uppercase()) {
        "WIN" -> ProfitBorder
        "LOSS" -> LossBorder
        else -> ActiveBorder
    }

    val statusBarBg = when (signal.result.uppercase()) {
        "WIN" -> Color(0xFF065F46)
        "LOSS" -> Color(0xFF991B1B)
        else -> SecondaryBlue
    }

    val statusBarText = when (signal.result.uppercase()) {
        "WIN" -> Color(0xFF34D399)
        "LOSS" -> Color(0xFFFCA5A5)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = BorderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Colored Left Accent Strip
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
                // Header Alert Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚨 SIGNALXPRESS LIVE",
                        color = AccentAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Type Badge (BUY / SELL)
                    val isBuy = signal.type.uppercase() == "BUY"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBuy) BuyBadgeBg else SellBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = signal.type.uppercase(),
                            color = if (isBuy) BuyBadgeText else SellBadgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Signal Body Details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "💎 Pair: ${signal.pair}",
                            color = TextLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📊 Type: ${signal.type}",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📥 Entry: ${signal.entry}",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "🎯 TP1: ${signal.tp1}",
                            color = AccentEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "🎯 TP2: ${signal.tp2}",
                            color = AccentEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "🎯 TP3: ${signal.tp3}",
                            color = AccentEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "🎯 TP4: ${signal.tp4}",
                            color = AccentEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "🛑 SL: ${signal.sl}",
                            color = AccentRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Status Display (Read-Only Badges for Customers)
                val hitLevel = signal.hitLevel
                val hitLevelInt = hitLevel.toIntOrNull() ?: 0

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🎯 Target Progress:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Row 1: TP1, TP2, TP3, TP4 (4 TPs in 1 row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TargetStatusBadge("TP1", isHit = hitLevelInt >= 1, modifier = Modifier.weight(1f))
                        TargetStatusBadge("TP2", isHit = hitLevelInt >= 2, modifier = Modifier.weight(1f))
                        TargetStatusBadge("TP3", isHit = hitLevelInt >= 3, modifier = Modifier.weight(1f))
                        TargetStatusBadge("TP4", isHit = hitLevelInt >= 4, modifier = Modifier.weight(1f))
                    }

                    // Row 2: BE and SL (and Close if applicable) in a second row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TargetStatusBadge(
                            label = "BE",
                            isHit = hitLevel == "BE",
                            isWarning = true,
                            modifier = Modifier.weight(1f)
                        )
                        TargetStatusBadge(
                            label = "SL Hit",
                            isHit = hitLevel == "SL",
                            isDanger = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (hitLevel == "CLOSE") {
                            TargetStatusBadge(
                                label = "Closed",
                                isHit = true,
                                isGray = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reaction Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReactionButton("👍", signal.thumbsCount, isSelected = signal.userReactedEmoji == "👍") { onReactionToggle("👍") }
                        ReactionButton("🔥", signal.fireCount, isSelected = signal.userReactedEmoji == "🔥") { onReactionToggle("🔥") }
                        ReactionButton("🚀", signal.rocketCount, isSelected = signal.userReactedEmoji == "🚀") { onReactionToggle("🚀") }
                        ReactionButton("💔", signal.brokenHeartCount, isSelected = signal.userReactedEmoji == "💔") { onReactionToggle("💔") }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Status Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBarBg)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "STATUS: ${signal.status}",
                        color = statusBarText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Risk Warning Box in Sinhala (Placed at the Very Bottom)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x14EF4444))
                        .border(1.5.dp, AccentRed, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "⚠️ වැදගත් අවවාදය",
                            color = AccentRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Trading කියන්නේ ඉක්මනින් සල්ලි හොයන ක්‍රමයක් නොවෙයි. ලාභ ලැබෙන වගේම පාඩුත් ලැබිය හැකියි. ඔබට අහිමි වුවහොත් දරාගත හැකි මුදලක් පමණක් ආයෝජනය කරන්න. Risk Management පිළිපදින්න, හැඟීම් මත නොව සැලසුමකට අනුව Trade කරන්න.",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0x4DDF4444), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Follow Money Management 🙏🙏🙏\n🔥 GET RISK WIN YOUR LIFE 🔥",
                            color = AccentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
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
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isHit && isWarning -> AccentAmber
        isHit && isDanger -> AccentRed
        isHit && isGray -> Color(0xFF4B5563)
        isHit -> Color(0xFF059669)
        else -> DarkBackground
    }

    val borderColor = when {
        isHit && isWarning -> AccentAmber
        isHit && isDanger -> AccentRed
        isHit && isGray -> Color(0xFF9CA3AF)
        isHit -> AccentEmerald
        else -> BorderColor
    }

    val textColor = if (isHit) TextLight else TextSecondary
    val displayLabel = if (isHit && !isWarning && !isDanger && !isGray) "$label ✓" else label

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayLabel,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun ReactionButton(
    emoji: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) SecondaryBlue else DarkBackground)
            .border(1.dp, if (isSelected) PrimarySky else BorderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                color = if (isSelected) TextLight else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
