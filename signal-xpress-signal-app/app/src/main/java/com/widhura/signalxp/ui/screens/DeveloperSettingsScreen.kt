package com.widhura.signalxp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.ui.MainViewModel
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextSecondary

@Composable
fun DeveloperSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val isDeveloperMode by viewModel.isDeveloperMode.collectAsState()
    val isScreenshotDisabled by viewModel.isScreenshotDisabled.collectAsState()

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

            // Header with back
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u2190",
                    color = PrimarySky,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
                )
                Column {
                    Text(
                        text = "Developer Settings",
                        color = PrimarySky,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Read-only status (controlled via BuildConfig)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderColor)
            Spacer(modifier = Modifier.height(16.dp))

            // Developer Mode Status (Read-only)
            SettingsStatusRow(
                title = "Developer Mode",
                subtitle = "DEVELOPER_MODE in build.gradle.kts",
                icon = "\uD83D\uDD27",
                isActive = isDeveloperMode
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Screenshot Protection Status (Read-only)
            SettingsStatusRow(
                title = "Screenshot Protection",
                subtitle = "SCREENSHOT_DISABLED in build.gradle.kts",
                icon = "\uD83D\uDCF7",
                isActive = isScreenshotDisabled
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = BorderColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .padding(16.dp)
            ) {
                Text(
                    text = "How to Change",
                    color = TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Edit the buildConfigField values in app/build.gradle.kts:\n\n" +
                            "buildConfigField(\"Boolean\", \"SCREENSHOT_DISABLED\", \"true\")\n" +
                            "buildConfigField(\"Boolean\", \"DEVELOPER_MODE\", \"true\")\n\n" +
                            "Then rebuild the app.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsStatusRow(
    title: String,
    subtitle: String,
    icon: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isActive) "ON" else "OFF",
            color = if (isActive) AccentEmerald else AccentRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
