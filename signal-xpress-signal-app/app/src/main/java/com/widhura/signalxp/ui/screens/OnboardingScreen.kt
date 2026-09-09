package com.widhura.signalxp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.R
import com.widhura.signalxp.ui.theme.AccentAmber
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.SecondaryBlue
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val details: List<String>,
)

private fun onboardingPages(
    gold: Color,
    green: Color,
    blue: Color,
    purple: Color,
): List<OnboardingPage> = listOf(
    OnboardingPage(
        icon = Icons.Default.CandlestickChart,
        iconBg = gold.copy(alpha = 0.12f),
        iconTint = gold,
        title = "Real-Time Trading Signals",
        subtitle = "Stay ahead of the market",
        description = "SignalXpress delivers live trading signals directly to your device with millisecond precision.",
        details = listOf(
            "Receive BUY/SELL signals the moment they are generated",
            "Signals include entry price, stop loss, and take profit levels",
            "Track your trading performance in real-time",
        ),
    ),
    OnboardingPage(
        icon = Icons.Default.Sync,
        iconBg = green.copy(alpha = 0.12f),
        iconTint = green,
        title = "Background Data Sync",
        subtitle = "Never miss a signal",
        description = "To deliver real-time signals even when the app is in the background, SignalXpress uses a foreground service to maintain a persistent connection.",
        details = listOf(
            "A visible notification shows when the service is active",
            "Maintains a live connection to sync trading data continuously",
            "You can disable this service anytime in Settings",
            "Required for receiving time-critical trading alerts",
        ),
    ),
    OnboardingPage(
        icon = Icons.Default.Notifications,
        iconBg = blue.copy(alpha = 0.12f),
        iconTint = blue,
        title = "Instant Notifications",
        subtitle = "Alerts that matter",
        description = "Get instant push notifications for trading signals, market updates, and important announcements.",
        details = listOf(
            "High-priority alerts for new trading signals",
            "Background notifications even when app is closed",
            "Customizable notification preferences in Settings",
        ),
    ),
    OnboardingPage(
        icon = Icons.Default.Security,
        iconBg = purple.copy(alpha = 0.12f),
        iconTint = purple,
        title = "Your Privacy & Control",
        subtitle = "You are in control",
        description = "SignalXpress respects your privacy and gives you full control over data collection and service usage.",
        details = listOf(
            "Foreground service notification is always visible when active",
            "Disable background sync anytime in Profile > Settings",
            "Your data is encrypted and never shared with third parties",
            "Clear all local data with one tap in Settings",
        ),
    ),
)

@Composable
fun OnboardingScreen(
    isDarkMode: Boolean = true,
    onComplete: () -> Unit,
) {
    val gold = if (isDarkMode) AccentAmber else LightTheme.AccentAmber
    val green = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald
    val blue = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val purple = if (isDarkMode) SecondaryBlue else LightTheme.SecondaryBlue
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val body = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val muted = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val pillUnselected = if (isDarkMode) BorderColor else LightTheme.BorderColor

    val pages = onboardingPages(gold, green, blue, purple)
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                body = body,
                gold = gold,
                muted = muted,
                checkTint = green,
            )
        }

        // ── Bottom controls ──────────────────────────────
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        Modifier
                            .size(if (selected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) gold else pillUnselected,
                            ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Page counter
            Text(
                "${pagerState.currentPage + 1} / ${pages.size}",
                color = muted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )

            Spacer(Modifier.height(16.dp))

            // Action button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = gold),
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    Text(
                        "CONTINUE",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF412D00),
                    )
                } else {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF412D00), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "GET STARTED",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF412D00),
                    )
                }
            }

            if (pagerState.currentPage < pages.size - 1) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onComplete) {
                    Text(
                        "Skip",
                        color = muted,
                        fontSize = 13.sp,
                    )
                }
            }
        }

        // ── Logo at top ──────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.signal_xpress_icon_1786298386233),
                contentDescription = "Signal Xpress",
                modifier = Modifier.height(28.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Signal Xpress",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = body,
                letterSpacing = (-0.5).sp,
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    body: Color,
    gold: Color,
    muted: Color,
    checkTint: Color,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon
        Box(
            Modifier
                .size(80.dp)
                .background(page.iconBg, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                page.icon,
                null,
                tint = page.iconTint,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        // Title
        Text(
            page.title,
            color = body,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp,
        )

        Spacer(Modifier.height(8.dp))

        // Subtitle
        Text(
            page.subtitle,
            color = gold,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(20.dp))

        // Description
        Text(
            page.description,
            color = muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(24.dp))

        // Detail list
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            page.details.forEach { detail ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .background(checkTint.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = checkTint,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    Text(
                        detail,
                        color = body.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
