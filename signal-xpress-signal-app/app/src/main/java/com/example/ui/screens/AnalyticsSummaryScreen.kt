package com.example.ui.screens

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
import com.example.data.SignalEntity
import com.example.ui.MainViewModel
import com.example.ui.ResultFilter
import com.example.ui.TimeFilter
import com.example.ui.components.EquityGrowthChart
import com.example.ui.components.WinLossDistributionChart
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsSummaryScreen(viewModel: MainViewModel) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
                            color = TextLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF059669))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Analytics",
                                color = TextLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Performance Breakdown",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "START BALANCE",
                        color = TextSecondary,
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
                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅 Filter Summary By Period:",
                            color = TextLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TimeFilterButton("All", timeFilter == TimeFilter.ALL, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.ALL) }
                        TimeFilterButton("Daily", timeFilter == TimeFilter.DAILY, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.DAILY) }
                        TimeFilterButton("Weekly", timeFilter == TimeFilter.WEEKLY, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.WEEKLY) }
                        TimeFilterButton("Monthly", timeFilter == TimeFilter.MONTHLY, modifier = Modifier.weight(1f)) { viewModel.setTimeFilter(TimeFilter.MONTHLY) }
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
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBackground)
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
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
                                    color = if (customDate.isNotBlank()) TextLight else TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text("📅", fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = "Clear",
                            color = Color(0xFFEF4444),
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
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "CURRENT BALANCE",
                    value = String.format("$%,.2f", currentBalance),
                    valueColor = Color(0xFF10B981),
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
                    valueColor = TextLight,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "WIN TRADES",
                    value = winCount.toString(),
                    valueColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "LOSS TRADES",
                    value = lossCount.toString(),
                    valueColor = Color(0xFFEF4444),
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
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "LOSS PIPS",
                    value = lossPips.toString(),
                    valueColor = Color(0xFFEF4444),
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
                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Signal Ledger Database",
                            color = TextLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ResultFilterTab("All", resultFilter == ResultFilter.ALL, textColor = TextLight) { viewModel.setResultFilter(ResultFilter.ALL) }
                            ResultFilterTab("Wins", resultFilter == ResultFilter.WIN, textColor = Color(0xFF10B981)) { viewModel.setResultFilter(ResultFilter.WIN) }
                            ResultFilterTab("Losses", resultFilter == ResultFilter.LOSS, textColor = Color(0xFFEF4444)) { viewModel.setResultFilter(ResultFilter.LOSS) }
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
                        Text("NO", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                        Text("DATE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("PAIR", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("DIR", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("PIPS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text("PROFIT ($)", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
                        Text("RESULT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                    }

                    Divider(color = Color(0xFF1E293B), thickness = 1.dp)

                    if (ledgerSignals.isEmpty()) {
                        Text(
                            text = "No records found.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        Column {
                            ledgerSignals.forEach { sig ->
                                LedgerRow(sig)
                                Divider(color = Color(0xFF1E293B), thickness = 1.dp)
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
    placeholder: String
) {
    Box(contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = TextSecondary, fontSize = 11.sp)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TextLight,
                fontSize = 11.sp
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LedgerRow(sig: SignalEntity) {
    val isWin = sig.result.uppercase() == "WIN"
    val resBg = if (isWin) Color(0xFF064E3B) else Color(0xFF7F1D1D)
    val resText = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444)
    val dirColor = if (sig.type.uppercase() == "BUY") Color(0xFF3B82F6) else Color(0xFFF59E0B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(sig.no.toString(), color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
        Text(sig.date, color = TextSecondary, fontSize = 9.sp, modifier = Modifier.weight(1.5f))
        Text(sig.pair, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
        Text(sig.type, color = dirColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(sig.pips.toString(), color = TextLight, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(
            text = if (sig.profit >= 0) "${sig.profit.toInt()}" else "${sig.profit.toInt()}",
            color = TextLight,
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TimeFilterButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFF0082FB) else DarkBackground
    val border = if (isSelected) Color(0xFF0082FB) else BorderColor

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
            color = if (isSelected) TextLight else TextSecondary,
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
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = if (isSelected) textColor else TextSecondary,
        fontSize = 10.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable { onClick() }
    )
}

