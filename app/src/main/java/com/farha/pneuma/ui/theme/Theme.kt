package com.farha.pneuma.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TriageAidColors = darkColorScheme(
    primary = AccentGreen,
    onPrimary = DeepBackground,
    secondary = AccentBlue,
    tertiary = SoftAmber,
    background = DeepBackground,
    surface = CardSurface,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    error = DangerRed,
)

@Composable
fun PneumaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TriageAidColors,
        typography = Typography,
        content = content
    )
}
