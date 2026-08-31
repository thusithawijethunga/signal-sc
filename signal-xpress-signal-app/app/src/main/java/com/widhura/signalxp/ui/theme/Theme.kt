package com.widhura.signalxp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SignalXpressColorScheme = darkColorScheme(
  primary = PrimarySky,
  onPrimary = TextLight,
  secondary = SecondaryBlue,
  onSecondary = TextLight,
  tertiary = AccentEmerald,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = CardBackground,
  onSurface = TextPrimary,
  surfaceVariant = CardHeaderBackground,
  outline = BorderColor
)

@Composable
fun SignalXpressTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = SignalXpressColorScheme,
    typography = Typography,
    content = content
  )
}

