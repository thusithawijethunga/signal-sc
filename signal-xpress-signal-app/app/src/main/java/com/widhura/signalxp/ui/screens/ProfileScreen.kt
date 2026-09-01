package com.widhura.signalxp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    isDarkMode: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val textPrimary = if (isDarkMode) LightTheme.TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky

    val userName = com.widhura.signalxp.data.api.ApiClient.getCurrentUserName(context)
    val userEmail = com.widhura.signalxp.data.api.ApiClient.getCurrentUserEmail(context)
    val userInitial = if (userName.isNotBlank()) userName.first().uppercase() else "T"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u2190",
                    color = primary,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = "Profile",
                    color = primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = border)
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial,
                            color = LightTheme.TextLight,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = userName,
                        color = textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userEmail,
                        color = textSec,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Appearance Section
            Text(
                text = "Appearance",
                color = textSec,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isDarkMode) "\uD83C\uDF19" else "\u2600\uFE0F", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Theme",
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isDarkMode) "Dark mode" else "Light mode",
                        color = textSec,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = primary,
                        checkedTrackColor = primary.copy(alpha = 0.3f),
                        uncheckedThumbColor = LightTheme.PrimarySky,
                        uncheckedTrackColor = LightTheme.PrimarySky.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Links
            SettingsMenuItem(title = "About", icon = "\u2139\uFE0F", cardBg = cardBg, textPrimary = textPrimary, textSec = textSec, border = border, onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://signalxpress.com/about"))) })
            Spacer(modifier = Modifier.height(10.dp))
            SettingsMenuItem(title = "Privacy Policy", icon = "\uD83D\uDD12", cardBg = cardBg, textPrimary = textPrimary, textSec = textSec, border = border, onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://signalxpress.com/privacy"))) })
            Spacer(modifier = Modifier.height(10.dp))
            SettingsMenuItem(title = "Terms & Conditions", icon = "\uD83D\uDCDC", cardBg = cardBg, textPrimary = textPrimary, textSec = textSec, border = border, onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://signalxpress.com/terms"))) })

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentRed.copy(alpha = 0.1f))
                    .clickable { onSignOut() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "\uD83D\uDEAA", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign Out",
                    color = AccentRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsMenuItem(
    title: String,
    icon: String,
    cardBg: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSec: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(text = "\u203A", color = textSec, fontSize = 18.sp)
    }
}
