package com.widhura.signalxp.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.data.SignalEntity
import com.widhura.signalxp.ui.MainViewModel
import com.widhura.signalxp.ui.components.SignalCard
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextSecondary

@Composable
fun SignalsFeedScreen(
    viewModel: MainViewModel,
    isDarkMode: Boolean = true,
    onTitleTap: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    val signals by viewModel.filteredSignals.collectAsState()
    val selectedPair by viewModel.selectedPairFilter.collectAsState()
    var tapCount by remember { mutableIntStateOf(0) }

    val pairs = listOf("ALL", "XAU/USD", "EUR/USD", "GBP/JPY")

    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val accentEmerald = if (isDarkMode) com.widhura.signalxp.ui.theme.AccentEmerald else LightTheme.AccentEmerald

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Header
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SIGNAL XPRESS",
                        color = primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.then(
                            if (onTitleTap != null) {
                                Modifier.clickable {
                                    tapCount++
                                    if (tapCount >= 5) {
                                        tapCount = 0
                                        onTitleTap()
                                    }
                                }
                            } else Modifier
                        )
                    )
                    Text(
                        text = "Official Forex & Gold Signals Feed",
                        color = textSec,
                        fontSize = 12.sp
                    )
                }
                if (onProfileClick != null) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val userLetter = remember {
                        val name = com.widhura.signalxp.data.api.ApiClient.getCurrentUserName(context)
                        if (name.isNotBlank()) name.first().uppercase() else "T"
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(primary)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userLetter,
                            color = bg,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = border)
            Spacer(modifier = Modifier.height(12.dp))

            // Subheader Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📡 Live Market Signals",
                    color = textOnBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "● Live Feed",
                    color = accentEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pair Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(pairs) { pair ->
                    val isSelected = selectedPair.equals(pair, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) primary else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (isSelected) primary else border, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setPairFilter(pair) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = pair,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Signals Feed List
            if (signals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No signals available for selected pair.",
                        color = textSec,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(signals, key = { it.id }) { signal ->
                        SignalCard(
                            signal = signal,
                            onReactionToggle = { emoji -> viewModel.toggleReaction(signal, emoji) }
                        )
                    }
                }
            }
        }
    }
}
