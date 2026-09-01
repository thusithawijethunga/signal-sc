package com.widhura.signalxp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.data.SignalEntity
import com.widhura.signalxp.ui.MainViewModel
import com.widhura.signalxp.ui.ResultFilter
import com.widhura.signalxp.ui.TimeFilter
import com.widhura.signalxp.ui.components.EquityGrowthChart
import com.widhura.signalxp.ui.components.WinLossDistributionChart
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextPrimary
import com.widhura.signalxp.ui.theme.TextSecondary
import com.widhura.signalxp.ui.theme.LightTheme
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsSummaryScreen(viewModel: MainViewModel, isDarkMode: Boolean = true) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            viewModel.setCustomDate(formattedDate)
        },
        year,
        month,
        day
    )

    val filteredSignals by viewModel.filteredSignals.collectAsState()
    val ledgerSignals by viewModel.ledgerSignals.collectAsState()
    val timeFilter by viewModel.selectedTimeFilter.collectAsState()
    val customDate by viewModel.customDate.collectAsState()
    val resultFilter by viewModel.selectedResultFilter.collectAsState()

    val startBalance = 1000.00
    val totalTrades = filteredSignals.size
    val winCount = filteredSignals.count { it.result.uppercase() == "WIN" }
    val lossCount = filteredSignals.count { it.result.uppercase() == "LOSS" }
    val totalProfit = filteredSignals.sumOf { it.profit }
    val currentBalance = startBalance + totalProfit
    val totalPips = filteredSignals.sumOf { it.pips }
    val lossPips = filteredSignals.filter { it.pips < 0 }.sumOf { it.pips }

    val winRateStr = if (totalTrades > 0) {
        String.format("%.1f%%", (winCount.toFloat() / totalTrades) * 100)
    } else "0%"

    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val accentEmerald = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald
    val accentRed = if (isDarkMode) AccentRed else LightTheme.AccentRed

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Summary Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📊 Signal Xpress ",
                            color = textOnBg,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentEmerald)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                                Text(
                                    text = "Analytics",
                                    color = textOnBg,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Performance Breakdown",
                        color = textSec,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "START BALANCE",
                        color = textSec,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("$%,.2f", startBalance),
                        color = Color(0xFFFFB800),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time Filter Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, border, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅 Filter Summary By Period:",
                            color = textOnBg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TimeFilterButton("All", timeFilter == TimeFilter.ALL, isDarkMode = isDarkMode, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.ALL) }
                        TimeFilterButton("Daily", timeFilter == TimeFilter.DAILY, isDarkMode = isDarkMode, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.DAILY) }
                        TimeFilterButton("Weekly", timeFilter == TimeFilter.WEEKLY, isDarkMode = isDarkMode, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.WEEKLY) }
                        TimeFilterButton("Monthly", timeFilter == TimeFilter.MONTHLY, isDarkMode = isDarkMode, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.MONTHLY) }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Select Date:",
                            color = textSec,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
            .background(bg)
                                .border(1.dp, border, RoundedCornerShape(6.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (customDate.isNotBlank()) customDate else "dd/mm/yyyy",
                                    color = if (customDate.isNotBlank()) textOnBg else textSec,
                                    fontSize = 11.sp
                                )
                                Text("📅", fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = "Clear",
                            color = accentRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                viewModel.setCustomDate("")
                                viewModel.setTimeFilter(TimeFilter.ALL)
                            }
                        )
                    }
                }
            }
        }

        // Top Metrics Grid (NET PROFIT & CURRENT BALANCE)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "NET PROFIT",
                    value = String.format("$%.2f", totalProfit),
                    valueColor = Color(0xFF00A3FF),
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "CURRENT BALANCE",
                    value = String.format("$%,.2f", currentBalance),
                    valueColor = Color(0xFF10B981),
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Secondary Metrics Grid (TOTAL, WIN, LOSS TRADES)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "TOTAL TRADES",
                    value = totalTrades.toString(),
                    valueColor = textOnBg,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "WIN TRADES",
                    value = winCount.toString(),
                    valueColor = Color(0xFF10B981),
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "LOSS TRADES",
                    value = lossCount.toString(),
                    valueColor = accentRed,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Pips Gain Grid (NET PIPS GAIN & LOSS PIPS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "NET PIPS GAIN",
                    value = String.format("%s%d", if (totalPips >= 0) "+" else "", totalPips),
                    valueColor = Color(0xFFC084FC),
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "LOSS PIPS",
                    value = lossPips.toString(),
                    valueColor = accentRed,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Charts
        item {
            WinLossDistributionChart(
                wins = winCount,
                losses = lossCount,
                winRateStr = winRateStr
            )
        }

        item {
            EquityGrowthChart(
                signals = filteredSignals,
                startBalance = startBalance
            )
        }

        // Ledger Table Database
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, border, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Signal Ledger Database",
                            color = textOnBg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ResultFilterTab("All", resultFilter == ResultFilter.ALL, textColor = textOnBg, isDarkMode = isDarkMode) { viewModel.setResultFilter(ResultFilter.ALL) }
                            ResultFilterTab("Wins", resultFilter == ResultFilter.WIN, textColor = Color(0xFF10B981), isDarkMode = isDarkMode) { viewModel.setResultFilter(ResultFilter.WIN) }
                            ResultFilterTab("Losses", resultFilter == ResultFilter.LOSS, textColor = accentRed, isDarkMode = isDarkMode) { viewModel.setResultFilter(ResultFilter.LOSS) }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ledger Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("NO", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                        Text("DATE", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("PAIR", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("DIR", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("PIPS", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text("PROFIT ($)", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                        Text("RESULT", color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                    }

                    Divider(color = border, thickness = 1.dp)

                    if (ledgerSignals.isEmpty()) {
                        Text(
                            text = "No records found.",
                            color = textSec,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        Column {
                            ledgerSignals.forEach { sig ->
                                LedgerRow(sig, isDarkMode = isDarkMode)
                                Divider(color = border, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BasicTextFieldHelper(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDarkMode: Boolean = true
) {
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary

    Box(contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = textSec, fontSize = 11.sp)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = textOnBg,
                fontSize = 11.sp
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LedgerRow(sig: SignalEntity, isDarkMode: Boolean = true) {
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val accentEmerald = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald
    val accentRed = if (isDarkMode) AccentRed else LightTheme.AccentRed

    val isWin = sig.result.uppercase() == "WIN"
    val resBg = if (isWin) accentEmerald.copy(alpha = 0.15f) else accentRed.copy(alpha = 0.15f)
    val resText = if (isWin) Color(0xFF10B981) else accentRed
    val dirColor = if (sig.type.uppercase() == "BUY") Color(0xFF3B82F6) else Color(0xFFF59E0B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(sig.no.toString(), color = textOnBg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
        Text(sig.date, color = textSec, fontSize = 9.sp, modifier = Modifier.weight(1.5f))
        Text(sig.pair, color = textOnBg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
        Text(sig.type, color = dirColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(sig.pips.toString(), color = textOnBg, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(
            text = if (sig.profit >= 0) "${sig.profit.toInt()}" else "${sig.profit.toInt()}",
            color = textOnBg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )

        Box(
            modifier = Modifier
                .weight(1.2f)
                .clip(RoundedCornerShape(3.dp))
                .background(resBg)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = sig.result.uppercase(),
                color = resText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    valueColor: Color,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary

    Card(
        modifier = modifier.border(1.dp, border, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = textSec, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TimeFilterButton(
    label: String,
    isSelected: Boolean,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFF0082FB) else if (isDarkMode) DarkBackground else LightTheme.Background
    val border = if (isSelected) Color(0xFF0082FB) else if (isDarkMode) BorderColor else LightTheme.BorderColor
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) textOnBg else textSec,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResultFilterTab(
    label: String,
    isSelected: Boolean,
    textColor: Color,
    isDarkMode: Boolean = true,
    onClick: () -> Unit
) {
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary

    Text(
        text = label,
        color = if (isSelected) textColor else textSec,
        fontSize = 10.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable { onClick() }
    )
}

