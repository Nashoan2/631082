package com.example.util

import com.example.data.TransactionRecord
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ArabicNumberHelper {

  fun toEngDigits(str: String?): String {
    if (str == null) return ""
    val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val builder = StringBuilder()
    for (ch in str) {
      val idx = arabicChars.indexOf(ch)
      if (idx != -1) {
        builder.append(idx)
      } else {
        builder.append(ch)
      }
    }
    return builder.toString()
  }

  fun formatAmount(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val df = DecimalFormat("#,##0.##", symbols)
    return df.format(value)
  }

  fun formatDateTime(date: Date = Date()): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
    return sdf.format(date)
  }

  fun formatReportDateTime(date: Date = Date()): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
    return sdf.format(date)
  }

  fun formatTo24HourDateTime(dateTimeStr: String): String {
    val clean = toEngDigits(dateTimeStr).trim()
    val parsed = parseDate(clean)
    if (parsed != null) {
      val sdf24 = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
      return sdf24.format(parsed)
    }
    return clean.replace(Regex("(?i)\\s*[AP]M?\\b"), "").replace("مساءً", "").replace("صباحاً", "").replace("م", "").replace("ص", "").trim()
  }

  fun splitTimeAndSymbol(timePart: String): Pair<String, String> {
    val cleaned = timePart.replace("AM", "A").replace("PM", "P").replace("am", "A").replace("pm", "P").trim()
    val parts = cleaned.split(" ").filter { it.isNotBlank() }
    val timeDigits = parts.firstOrNull { it.contains(":") } ?: parts.firstOrNull() ?: ""
    val symbol = parts.firstOrNull { it.equals("A", ignoreCase = true) || it.equals("P", ignoreCase = true) } ?: ""
    return Pair(timeDigits, symbol)
  }

  fun formatDateTimeShortAmPm(raw: String): String {
    return raw.replace("AM", "A").replace("PM", "P").replace("am", "A").replace("pm", "P")
  }

  fun formatDateOnly(date: Date = Date()): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    return sdf.format(date)
  }

  fun parseDate(dateStr: String): Date? {
    val clean = toEngDigits(dateStr).trim()
      .replace(Regex("(?i)\\bA\\b"), "AM")
      .replace(Regex("(?i)\\bP\\b"), "PM")
      .replace("مساءً", "PM")
      .replace("صباحاً", "AM")
      .replace("م", "PM")
      .replace("ص", "AM")
    val formats = listOf(
      "dd/MM/yyyy-HHmm",
      "dd/MM/yyyy-HH:mm",
      "dd/MM/yyyy hh:mm a",
      "dd/MM/yyyy h:mm a",
      "dd/MM/yyyy hh:mm:ss a",
      "dd/MM/yyyy HH:mm:ss",
      "dd/MM/yyyy HH:mm",
      "dd/MM/yyyy",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd HH:mm",
      "yyyy-MM-dd hh:mm a",
      "yyyy-MM-dd h:mm a",
      "yyyy-MM-dd"
    )
    for (fmt in formats) {
      try {
        val sdf = SimpleDateFormat(fmt, Locale.US)
        sdf.isLenient = false
        val d = sdf.parse(clean)
        if (d != null) return d
      } catch (_: Exception) {}
    }
    // Also try splitting first token if date is like "11/09/2026 02:38 PM"
    try {
      val firstToken = clean.split(" ").firstOrNull()
      if (firstToken != null && firstToken.contains("/")) {
        val parts = firstToken.split("/")
        if (parts.size == 3) {
          val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
          val d = sdf.parse(firstToken)
          if (d != null) return d
        }
      }
    } catch (_: Exception) {}
    return null
  }

  fun getCurrencyName(curr: String): String {
    return when (curr) {
      "$", "USD" -> "دولار أمريكي"
      "SAR" -> "ريال سعودي"
      "YER" -> "ريال يمني"
      else -> curr
    }
  }

  fun normalizeCurrencyCode(curr: String?): String {
    if (curr == null) return "$"
    val clean = curr.uppercase().trim()
    return when {
      clean.contains("YER") || clean == "YR" || clean.contains("يمن") -> "YER"
      clean.contains("SAR") || clean == "SR" || clean.contains("سعود") -> "SAR"
      clean.contains("USD") || clean == "$" || clean.contains("دولار") -> "$"
      clean.isEmpty() -> "$"
      else -> clean
    }
  }

  fun convertCurrency(
    amount: Double,
    fromCurrency: String,
    toCurrency: String,
    rates: com.example.data.ExchangeRates = com.example.data.ExchangeRates()
  ): Double {
    val from = normalizeCurrencyCode(fromCurrency)
    val to = normalizeCurrencyCode(toCurrency)
    if (from == to) return amount

    return when ("$from->$to") {
      "$->YER" -> amount * rates.usdToYer
      "YER->$" -> if (rates.usdToYer > 0) amount / rates.usdToYer else (amount * rates.yerToUsd)
      "$->SAR" -> amount * rates.usdToSar
      "SAR->$" -> if (rates.usdToSar > 0) amount / rates.usdToSar else (amount * rates.sarToUsd)
      "YER->SAR" -> if (rates.sarToYer > 0) amount / rates.sarToYer else (amount * rates.yerToSar)
      "SAR->YER" -> amount * rates.sarToYer
      else -> amount
    }
  }

  fun getCurrencySymbol(curr: String): String {
    val clean = curr.uppercase().trim()
    return when {
      clean.contains("YER") || clean == "YR" || clean.contains("يمن") -> "YR"
      clean.contains("SAR") || clean == "SR" || clean.contains("سعود") -> "SR"
      clean.contains("USD") || clean == "$" || clean.contains("دولار") -> "$"
      clean.isEmpty() -> "$"
      else -> curr
    }
  }

  fun numberToArabicWords(value: Double): String {
    val absVal = Math.abs(value)
    if (absVal == 0.0) return "صفر"
    val n = absVal.toLong()

    val units = arrayOf(
      "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة",
      "عشرة", "إحدى عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر",
      "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    )
    val tens = arrayOf("", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون")
    val hundreds = arrayOf(
      "", "مائة", "مائتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"
    )

    fun convertGroup(num: Long): String {
      val str = StringBuilder()
      val h = (num / 100).toInt()
      val rem = (num % 100).toInt()
      if (h in 1..9) {
        str.append(hundreds[h])
      }
      if (rem > 0) {
        if (str.isNotEmpty()) str.append(" و")
        if (rem < 20) {
          str.append(units[rem])
        } else {
          val t = rem / 10
          val u = rem % 10
          if (u > 0) {
            str.append(units[u]).append(" و").append(tens[t])
          } else {
            str.append(tens[t])
          }
        }
      }
      return str.toString()
    }

    val result = StringBuilder()
    val millions = n / 1_000_000
    val remMillions = n % 1_000_000
    val thousands = remMillions / 1000
    val remainder = remMillions % 1000

    if (millions > 0) {
      if (millions == 1L) result.append("مليون")
      else if (millions == 2L) result.append("مليونان")
      else if (millions in 3..10) result.append(convertGroup(millions)).append(" ملايين")
      else result.append(convertGroup(millions)).append(" مليوناً")
    }

    if (thousands > 0) {
      if (result.isNotEmpty()) result.append(" و")
      if (thousands == 1L) result.append("ألف")
      else if (thousands == 2L) result.append("ألفان")
      else if (thousands in 3..10) result.append(convertGroup(thousands)).append(" آلاف")
      else result.append(convertGroup(thousands)).append(" ألفاً")
    }

    if (remainder > 0) {
      if (result.isNotEmpty()) result.append(" و")
      result.append(convertGroup(remainder))
    }

    return if (result.isEmpty()) "صفر" else result.toString()
  }

  fun formatTransactionType(t: TransactionRecord): String {
    val cleanType = t.type.trim()

    // 1. Try to get number from voucherNum
    var num = t.voucherNum?.let { toEngDigits(it).trim() }?.takeIf { it.isNotEmpty() }

    // 2. If not found in voucherNum, extract from note
    if (num.isNullOrEmpty()) {
      val note = toEngDigits(t.note)
      val match = Regex("""(?:رقم\s*\(?|#|\()(\d+)\)?""").find(note)
      if (match != null) {
        num = match.groupValues[1].trim()
      }
    }

    return when {
      cleanType.contains("فاتور") -> {
        if (!num.isNullOrEmpty()) "فاتورة $num" else "فاتورة"
      }
      cleanType.contains("قبض") -> {
        if (!num.isNullOrEmpty()) "قبض $num" else "قبض"
      }
      cleanType.contains("صرف") -> {
        if (!num.isNullOrEmpty()) "صرف $num" else "صرف"
      }
      cleanType.contains("افتتاح") -> {
        "افتتاحي"
      }
      else -> {
        if (!num.isNullOrEmpty()) "$cleanType $num" else cleanType
      }
    }
  }

  fun formatShortTransactionType(t: TransactionRecord): String {
    val cleanType = t.type.trim()

    // 1. Try to get number from voucherNum
    var num = t.voucherNum?.let { toEngDigits(it).trim() }?.takeIf { it.isNotEmpty() }

    // 2. If not found in voucherNum, extract from note
    if (num.isNullOrEmpty()) {
      val note = toEngDigits(t.note)
      val match = Regex("""(?:رقم\s*\(?|#|\()(\d+)\)?""").find(note)
      if (match != null) {
        num = match.groupValues[1].trim()
      }
    }

    return when {
      cleanType.contains("فاتور") -> {
        if (!num.isNullOrEmpty()) "ف $num" else "ف"
      }
      cleanType.contains("قبض") -> {
        if (!num.isNullOrEmpty()) "ق $num" else "ق"
      }
      cleanType.contains("صرف") -> {
        if (!num.isNullOrEmpty()) "ص $num" else "ص"
      }
      cleanType.contains("افتتاح") -> {
        "افتتاحي"
      }
      else -> {
        if (!num.isNullOrEmpty()) "$cleanType $num" else cleanType
      }
    }
  }

  fun cleanStatementNote(note: String): String {
    val trimmed = note.trim()
    if (trimmed.isEmpty()) return ""
    // Remove prefixes like "سند رقم (7) - ", "سند رقم 7 - ", "فاتورة رقم (1) - ", etc.
    val cleaned = trimmed.replace(
      Regex("""^(?:سند\s+رقم\s*(?:\([^)]*\)|\d+)\s*[-–—:]\s*|فاتورة\s+رقم\s*(?:\([^)]*\)|\d+)\s*[-–—:]\s*)"""),
      ""
    ).trim()
    // If original note was just "سند رقم (...)" or "فاتورة رقم (...)" without actual description
    if (cleaned.matches(Regex("""^(?:سند\s+رقم\s*(?:\([^)]*\)|\d+)|فاتورة\s+رقم\s*(?:\([^)]*\)|\d+)|فاتورة)$"""))) {
      return ""
    }
    return cleaned
  }
}
