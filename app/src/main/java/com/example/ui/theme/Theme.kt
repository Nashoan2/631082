package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemePreset

val LocalShadedFieldColor = compositionLocalOf { Color(0xFFFFF0F3) }
val LocalShadedFieldBorder = compositionLocalOf { Color(0xFFFDA4AF) }

data class AppThemeColors(
  val preset: AppThemePreset = AppThemePreset.ROYAL_PURPLE,
  val primary: Color = Color(0xFF5E258D),
  val secondary: Color = Color(0xFF8B5CF6),
  val background: Color = Color(0xFFEEF2F5),
  val card: Color = Color(0xFFFFFFFF),
  val accent: Color = Color(0xFFEAB308),
  val textDark: Color = Color(0xFF1E1B4B),
  val isDark: Boolean = false
)

val LocalAppTheme = compositionLocalOf { AppThemeColors() }

@Composable
fun MyApplicationTheme(
  themePreset: AppThemePreset = AppThemePreset.ROYAL_PURPLE,
  customPrimaryHex: String = "",
  customSecondaryHex: String = "",
  customBgHex: String = "",
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val primary = parseHexColor(customPrimaryHex.ifBlank { themePreset.primaryHex })
  val secondary = parseHexColor(customSecondaryHex.ifBlank { themePreset.secondaryHex })
  val background = parseHexColor(customBgHex.ifBlank { themePreset.bgHex })
  val card = parseHexColor(themePreset.cardHex)
  val accent = parseHexColor(themePreset.accentHex)
  val textDark = parseHexColor(themePreset.textDarkHex)
  val isDark = darkTheme || themePreset.id == "DARK_LUXURY" || themePreset.id == "MIDNIGHT_BLACK"

  val colorScheme = if (isDark) {
    darkColorScheme(
      primary = primary,
      secondary = secondary,
      tertiary = accent,
      background = background,
      surface = card,
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.Black,
      onBackground = Color.White,
      onSurface = Color.White,
      onSurfaceVariant = Color(0xFFE4E4E7),
      outline = Color(0xFF52525B),
      outlineVariant = Color(0xFF27272A)
    )
  } else {
    lightColorScheme(
      primary = primary,
      secondary = secondary,
      tertiary = accent,
      background = background,
      surface = card,
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.White,
      onBackground = textDark,
      onSurface = textDark,
      onSurfaceVariant = Color(0xFF374151),
      outline = Color(0xFF9CA3AF),
      outlineVariant = Color(0xFFE5E7EB)
    )
  }

  val appThemeColors = AppThemeColors(
    preset = themePreset,
    primary = primary,
    secondary = secondary,
    background = background,
    card = card,
    accent = accent,
    textDark = textDark,
    isDark = isDark
  )

  CompositionLocalProvider(LocalAppTheme provides appThemeColors) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
