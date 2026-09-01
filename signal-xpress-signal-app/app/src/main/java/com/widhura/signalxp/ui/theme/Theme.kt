package com.widhura.signalxp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
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

private val LightColorScheme = lightColorScheme(
    primary = LightTheme.PrimarySky,
    onPrimary = LightTheme.TextLight,
    secondary = LightTheme.SecondaryBlue,
    onSecondary = LightTheme.TextLight,
    tertiary = LightTheme.AccentEmerald,
    background = LightTheme.Background,
    onBackground = LightTheme.OnBackground,
    surface = LightTheme.CardBackground,
    onSurface = LightTheme.OnSurface,
    surfaceVariant = LightTheme.CardHeaderBackground,
    outline = LightTheme.BorderColor
)

@Composable
fun SignalXpressTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
