package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommunityPostEntity
import com.example.ui.theme.BuyBadgeBg
import com.example.ui.theme.BuyBadgeText
import com.example.ui.theme.SellBadgeBg
import com.example.ui.theme.SellBadgeText
import java.text.DecimalFormat

data class CardThemeStyle(
    val backgroundBrush: Brush,
    val borderGradient: Brush,
    val accentColor: Color,
    val glowColor: Color
)

@Composable
fun ProfitCardWidget(
    post: CommunityPostEntity,
    modifier: Modifier = Modifier,
    showCopyButton: Boolean = true
) {
    val context = LocalContext.current
    val isProfit = post.profitAmount >= 0

    val themeStyle = when (post.cardTheme) {
        "GOLD_LUXURY" -> CardThemeStyle(
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF231B05),
                    Color(0xFF161205),
                    Color(0xFF0F0C03)
                )
            ),
            borderGradient = Brush.linearGradient(
                listOf(
                    Color(0xFFFDE68A),
                    Color(0xFFF59E0B),
                    Color(0xFF78350F),
                    Color(0xFFF59E0B)
                )
            ),
            accentColor = Color(0xFFFBBF24),
            glowColor = Color(0x33F59E0B)
        )
        "CYBER_SKY" -> CardThemeStyle(
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF051B2C),
                    Color(0xFF071421),
                    Color(0xFF030A12)
                )
            ),
            borderGradient = Brush.linearGradient(
                listOf(
                    Color(0xFF7DD3FC),
                    Color(0xFF0284C7),
                    Color(0xFF0369A1),
                    Color(0xFF38BDF8)
                )
            ),
            accentColor = Color(0xFF38BDF8),
            glowColor = Color(0x330284C7)
        )
        "DEEP_VIOLET" -> CardThemeStyle(
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E1035),
                    Color(0xFF130924),
                    Color(0xFF0A0414)
                )
            ),
            borderGradient = Brush.linearGradient(
                listOf(
                    Color(0xFFC084FC),
                    Color(0xFF9333EA),
                    Color(0xFF581C87),
                    Color(0xFFA855F7)
                )
            ),
            accentColor = Color(0xFFC084FC),
            glowColor = Color(0x339333EA)
        )
        else -> CardThemeStyle( // EMERALD_NEON
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF042018),
                    Color(0xFF031610),
                    Color(0xFF020D0A)
                )
            ),
            borderGradient = Brush.linearGradient(
                listOf(
                    Color(0xFF6EE7B7),
                    Color(0xFF10B981),
                    Color(0xFF047857),
                    Color(0xFF34D399)
                )
            ),
            accentColor = Color(0xFF10B981),
            glowColor = Color(0x3310B981)
        )
    }

    val df = DecimalFormat("#,##0.00")
    val profitSign = if (isProfit) "+" else "-"
    val formattedProfit = "$profitSign$${df.format(Math.abs(post.profitAmount))}"
    val roiSign = if (post.roiPercentage >= 0) "+" else ""
    val formattedRoi = "$roiSign${String.format("%.1f", post.roiPercentage)}%"
    val pipsSign = if (post.pipsGain >= 0) "+" else ""
    val formattedPips = "$pipsSign${post.pipsGain} PIPS"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = themeStyle.accentColor)
            .border(width = 1.5.dp, brush = themeStyle.borderGradient, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeStyle.backgroundBrush)
                .padding(16.dp)
        ) {
            Column {
                // Header Row: Pair, Buy/Sell, Broker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pair Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            Text(
                                text = post.pair,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Buy/Sell Badge
                        val isBuy = post.tradeType.uppercase() == "BUY"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBuy) BuyBadgeBg else SellBadgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isBuy) BuyBadgeText else SellBadgeText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${post.tradeType} ${post.lotSize}L",
                                    color = if (isBuy) BuyBadgeText else SellBadgeText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Broker & Verified Watermark
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22000000),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x33FFFFFF))
                        ) {
                            Text(
                                text = post.brokerName,
                                color = Color(0xFFD1D5DB),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        if (showCopyButton) {
                            IconButton(
                                onClick = {
                                    val clip = "${post.authorName}'s Profit Card:\nPair: ${post.pair} (${post.tradeType})\nProfit: $formattedProfit ($formattedRoi / $formattedPips)\nBroker: ${post.brokerName}\nGenerated via Signal Xpress"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Profit Card", clip))
                                    Toast.makeText(context, "Profit Card details copied! 📋", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Card",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Profit & ROI Showcase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "TOTAL PROFIT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeStyle.accentColor.copy(alpha = 0.8f),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedProfit,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isProfit) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // ROI & Pips Pill
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isProfit) Color(0x3310B981) else Color(0x33EF4444),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isProfit) Color(0x8810B981) else Color(0x88EF4444)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedRoi,
                                    color = if (isProfit) Color(0xFF34D399) else Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = " • $formattedPips",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Execution Data Matrix (Entry -> Exit)
                if (post.entryPrice.isNotBlank() || post.exitPrice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (post.entryPrice.isNotBlank()) {
                                Column {
                                    Text(
                                        text = "ENTRY",
                                        fontSize = 9.sp,
                                        color = Color(0xFF9CA3AF),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = post.entryPrice,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (post.exitPrice.isNotBlank()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "CLOSE / TP",
                                        fontSize = 9.sp,
                                        color = Color(0xFF9CA3AF),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = post.exitPrice,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "STATUS",
                                    fontSize = 9.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isProfit) "WINNER 🎯" else "CLOSED 🛑",
                                    fontSize = 12.sp,
                                    color = if (isProfit) Color(0xFF4ADE80) else Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer: Verified Trader badge & Signal Xpress Watermark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = themeStyle.accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified Trade Execution",
                            fontSize = 10.sp,
                            color = Color(0xFFD1D5DB),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "⚡ Signal Xpress",
                        fontSize = 10.sp,
                        color = themeStyle.accentColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
