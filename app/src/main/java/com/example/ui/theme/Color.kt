package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val BrandPurple = Color(0xFF5E258D)
val BrandPurpleLight = Color(0xFF8B4FC4)
val ReportBlue = Color(0xFF2B5797)
val SuccessGreen = Color(0xFF28A745)
val DangerRed = Color(0xFFD32F2F)
val BgLight = Color(0xFFEEF2F5)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF5E258D)
val PurpleGrey40 = Color(0xFF2B5797)
val Pink40 = Color(0xFF28A745)

val SilverLightGray = Color(0xFFECEFF1) // الرمادي الفاتح (الفضي)
val PinkFieldBg = Color(0xFFFFF0F3) // خلفية الحقول باللون الوردي
val PinkFieldBorder = Color(0xFFFDA4AF) // إطار الحقول باللون الوردي
val PinkFieldFocusedBorder = Color(0xFFFB7185) // إطار الحقول عند التركيز

// تظليل الحقول باللون الوردي الموحد
val MandatoryFieldBg = Color(0xFFFFF0F3)
val MandatoryFieldFocusedBg = Color(0xFFFFE4E8)
val MandatoryFieldBorder = Color(0xFFFDA4AF)
val MandatoryFieldFocusedBorder = Color(0xFFFB7185)
val MandatoryFieldLabelColor = Color(0xFFBE185D)

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFFFFF0F3)): Color {
  return try {
    val clean = hex.removePrefix("#").trim()
    when (clean.length) {
      6 -> {
        val colorInt = clean.toLong(16)
        Color(0xFF000000 or colorInt)
      }
      8 -> {
        val colorInt = clean.toLong(16)
        Color(colorInt)
      }
      else -> defaultColor
    }
  } catch (_: Exception) {
    defaultColor
  }
}

fun computeShadedBorderColor(bgColor: Color): Color {
  val r = (bgColor.red * 0.76f).coerceIn(0f, 1f)
  val g = (bgColor.green * 0.76f).coerceIn(0f, 1f)
  val b = (bgColor.blue * 0.76f).coerceIn(0f, 1f)
  return Color(red = r, green = g, blue = b, alpha = 1.0f)
}

@androidx.compose.runtime.Composable
fun mandatoryTextFieldColors(
  customBgColor: Color = LocalShadedFieldColor.current,
  customBorderColor: Color = LocalShadedFieldBorder.current
) = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
  focusedTextColor = Color(0xFF0F172A),
  unfocusedTextColor = Color(0xFF0F172A),
  disabledTextColor = Color(0xFF1E293B),
  focusedContainerColor = customBgColor,
  unfocusedContainerColor = customBgColor,
  disabledContainerColor = customBgColor,
  focusedBorderColor = customBorderColor,
  unfocusedBorderColor = customBorderColor,
  disabledBorderColor = customBorderColor,
  focusedLabelColor = Color(0xFFBE185D),
  unfocusedLabelColor = Color(0xFF1E293B),
  disabledLabelColor = Color(0xFF334155),
  focusedPlaceholderColor = Color(0xFF94A3B8),
  unfocusedPlaceholderColor = Color(0xFF94A3B8),
  disabledPlaceholderColor = Color(0xFF94A3B8),
  cursorColor = Color(0xFFBE185D)
)

// تظليل الحقول باللون الوردي الموحد
val OptionalYellowFieldBg = Color(0xFFFFF0F3)
val OptionalYellowFieldBorder = Color(0xFFFDA4AF)
val OptionalYellowFieldFocusedBorder = Color(0xFFFB7185)

@androidx.compose.runtime.Composable
fun optionalYellowTextFieldColors(
  customBgColor: Color = LocalShadedFieldColor.current,
  customBorderColor: Color = LocalShadedFieldBorder.current
) = mandatoryTextFieldColors(customBgColor = customBgColor, customBorderColor = customBorderColor)

@androidx.compose.runtime.Composable
fun standardAppTextFieldColors(
  containerColor: Color = LocalShadedFieldColor.current,
  borderColor: Color = LocalShadedFieldBorder.current,
  focusedBorderColor: Color = LocalShadedFieldBorder.current
) = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
  focusedTextColor = Color(0xFF0F172A),
  unfocusedTextColor = Color(0xFF0F172A),
  disabledTextColor = Color(0xFF1E293B),
  focusedContainerColor = containerColor,
  unfocusedContainerColor = containerColor,
  disabledContainerColor = containerColor,
  focusedBorderColor = focusedBorderColor,
  unfocusedBorderColor = borderColor,
  disabledBorderColor = borderColor,
  focusedLabelColor = Color(0xFFBE185D),
  unfocusedLabelColor = Color(0xFF1E293B),
  disabledLabelColor = Color(0xFF334155),
  focusedPlaceholderColor = Color(0xFF94A3B8),
  unfocusedPlaceholderColor = Color(0xFF94A3B8),
  disabledPlaceholderColor = Color(0xFF94A3B8),
  cursorColor = Color(0xFFBE185D)
)

