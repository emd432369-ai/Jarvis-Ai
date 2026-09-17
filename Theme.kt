package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = SpaceBlack,
  primaryContainer = SurfaceElevated,
  onPrimaryContainer = NeonCyanLight,
  secondary = CyanVariant,
  onSecondary = SpaceBlack,
  secondaryContainer = SurfaceCard,
  onSecondaryContainer = TextSecondary,
  tertiary = NeonGreen,
  onTertiary = SpaceBlack,
  background = SpaceBlack,
  onBackground = TextPrimary,
  surface = DarkNavyBg,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceCard,
  onSurfaceVariant = TextSecondary,
  outline = CyanPrimary.copy(alpha = 0.4f),
  outlineVariant = SurfaceCardBorder,
  error = CriticalRed,
  onError = TextPrimary,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false, // Enforce futuristic JARVIS aesthetic
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = JarvisColorScheme,
    typography = Typography,
    content = content
  )
}

