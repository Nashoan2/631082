package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val CairoFontFamily = FontFamily(
  Font(R.font.cairo, FontWeight.Normal)
)

val TajawalFontFamily = FontFamily(
  Font(R.font.tajawal, FontWeight.Normal)
)

val AlmaraiFontFamily = FontFamily(
  Font(R.font.almarai, FontWeight.Normal)
)

val AmiriFontFamily = FontFamily(
  Font(R.font.amiri, FontWeight.Normal)
)

fun getReportFontFamily(fontFamilyName: String): FontFamily {
  return when (fontFamilyName.trim().lowercase()) {
    "cairo" -> CairoFontFamily
    "tajawal" -> TajawalFontFamily
    "almarai" -> AlmaraiFontFamily
    "amiri" -> AmiriFontFamily
    "sans-serif" -> FontFamily.SansSerif
    "serif" -> FontFamily.Serif
    else -> CairoFontFamily
  }
}

// Set of Material typography styles to start with
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
  )
