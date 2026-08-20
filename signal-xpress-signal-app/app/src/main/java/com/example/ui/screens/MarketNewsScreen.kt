package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.NewsCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimarySky
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSecondary

@Composable
fun MarketNewsScreen(
    viewModel: MainViewModel
) {
    val newsList by viewModel.filteredNews.collectAsState()
    val selectedCurrency by viewModel.newsCurrencyFilter.collectAsState()
    val selectedImpact by viewModel.newsImpactFilter.collectAsState()
    val isSyncing by viewModel.isSyncingNews.collectAsState()

    val currencies = listOf("ALL", "USD", "EUR", "GBP", "JPY", "AUD", "CAD")
    val impacts = listOf("ALL", "HIGH", "MEDIUM", "LOW")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Forex Factory Calendar Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅 FOREX FACTORY",
                            color = TextLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E3A8A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CALENDAR",
                                color = PrimarySky,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "🇱🇰 Sri Lanka Standard Time (SLST / GMT+5:30)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Sync Live Feed Button
                IconButton(
                    onClick = { viewModel.syncForexFactoryNews() },
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimarySky,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Forex Factory Calendar",
                            tint = PrimarySky
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Currency Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currencies) { curr ->
                    val isSelected = selectedCurrency.equals(curr, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) PrimarySky else CardBackground)
                            .border(1.dp, if (isSelected) PrimarySky else BorderColor, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setNewsCurrencyFilter(curr) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (curr == "ALL") "All Currencies" else curr,
                            color = if (isSelected) TextLight else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Impact Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(impacts) { imp ->
                    val isSelected = selectedImpact.equals(imp, ignoreCase = true)
                    val chipLabel = when (imp) {
                        "HIGH" -> "🔴 High Impact"
                        "MEDIUM" -> "🟠 Medium Impact"
                        "LOW" -> "🟡 Low Impact"
                        else -> "All Impact Levels"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) CardBackground else DarkBackground)
                            .border(1.dp, if (isSelected) PrimarySky else BorderColor, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setNewsImpactFilter(imp) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = chipLabel,
                            color = if (isSelected) TextLight else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderColor)
            Spacer(modifier = Modifier.height(8.dp))

            // News Feed List
            if (newsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Forex Factory events found for selected filters.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardBackground)
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setNewsCurrencyFilter("ALL")
                                    viewModel.setNewsImpactFilter("ALL")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Reset Filters", color = PrimarySky, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(newsList, key = { it.id }) { news ->
                        NewsCard(news = news)
                    }
                }
            }
        }
    }
}
