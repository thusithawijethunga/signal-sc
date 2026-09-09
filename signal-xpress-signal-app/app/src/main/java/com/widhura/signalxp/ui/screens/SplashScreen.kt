package com.widhura.signalxp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.R
import com.widhura.signalxp.ui.theme.AccentAmber
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextSecondary

/**
 * Cold-start / auth-check splash, ported from the vip-android reference:
 * centered app logo + spinner on the app background.
 */
@Composable
fun SplashScreen(isDarkMode: Boolean = true) {
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val body = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val muted = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val gold = if (isDarkMode) AccentAmber else LightTheme.AccentAmber

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.signal_xpress_icon_1786298386233),
                contentDescription = "Signal Xpress",
                modifier = Modifier.height(88.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Signal Xpress",
                color = body,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Official Forex & Gold Signals Feed",
                color = muted,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = gold)
        }
    }
}
