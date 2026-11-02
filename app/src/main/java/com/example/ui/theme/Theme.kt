package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode

private val LightColorScheme = lightColorScheme(
  primary = BloomRose,
  onPrimary = Color.White,
  primaryContainer = BloomBlush,
  onPrimaryContainer = BloomDeepRose,
  secondary = BloomLavenderDeep,
  onSecondary = Color.White,
  secondaryContainer = BloomLavender,
  onSecondaryContainer = BloomPlum,
  tertiary = BloomCandleGold,
  onTertiary = BloomPlum,
  tertiaryContainer = BloomWarmGold,
  onTertiaryContainer = BloomPlum,
  background = BloomBlush,
  onBackground = BloomPlum,
  surface = BloomLightSurfaceHigh,
  onSurface = BloomPlum,
  surfaceVariant = BloomBlushMid,
  onSurfaceVariant = BloomMuted,
  outline = BloomLightBorder,
  error = BloomDanger,
  onError = Color.White
)

private val StandardDarkColorScheme = darkColorScheme(
  primary = BloomRose,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF2E2233),
  onPrimaryContainer = BloomWarmGold,
  secondary = BloomLavender,
  onSecondary = Color(0xFF1E1722),
  secondaryContainer = Color(0xFF281E2E),
  onSecondaryContainer = BloomLavender,
  tertiary = BloomCandleGold,
  onTertiary = Color(0xFF1E1722),
  tertiaryContainer = Color(0xFF2E2233),
  onTertiaryContainer = BloomWarmGold,
  background = Color(0xFF1A141E),
  onBackground = Color(0xFFF5EDF7),
  surface = Color(0xFF241B29),
  onSurface = Color(0xFFF5EDF7),
  surfaceVariant = Color(0xFF33263B),
  onSurfaceVariant = Color(0xFFC7B3CC),
  outline = Color(0xFF4A3855),
  error = BloomDanger,
  onError = Color.White
)

private val CandleDarkColorScheme = darkColorScheme(
  primary = BloomRose,
  onPrimary = Color.White,
  primaryContainer = BloomCandleDarkCard,
  onPrimaryContainer = BloomWarmGold,
  secondary = BloomLavender,
  onSecondary = BloomCandleDarkBg,
  secondaryContainer = BloomCandleDarkSurface,
  onSecondaryContainer = BloomLavender,
  tertiary = BloomCandleGold,
  onTertiary = BloomCandleDarkBg,
  tertiaryContainer = BloomCandleDarkCard,
  onTertiaryContainer = BloomWarmGold,
  background = BloomCandleDarkBg,
  onBackground = BloomCandleDarkText,
  surface = BloomCandleDarkSurface,
  onSurface = BloomCandleDarkText,
  surfaceVariant = BloomCandleDarkCard,
  onSurfaceVariant = BloomCandleDarkMuted,
  outline = BloomCandleDarkBorder,
  error = BloomDanger,
  onError = Color.White
)

@Composable
fun BloomTheme(
  themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  candleMode: Boolean = false,
  content: @Composable () -> Unit
) {
  val systemDark = isSystemInDarkTheme()
  val isDark = when (themeMode) {
    AppThemeMode.SYSTEM -> systemDark
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
  }

  val colorScheme = when {
    candleMode -> CandleDarkColorScheme
    isDark -> StandardDarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = BloomTypography,
    content = content
  )
}
