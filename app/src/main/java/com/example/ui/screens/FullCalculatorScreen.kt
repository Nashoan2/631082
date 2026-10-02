package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.InvoiceViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun FullCalculatorScreen(
  viewModel: InvoiceViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  // Find full calculator button config in UI customization
  val calcButton = remember(uiState.uiCustomizationConfig.buttons) {
    uiState.uiCustomizationConfig.buttons.find { it.id == "FULL_CALCULATOR" }
  }
  val isVisibleInMain = calcButton?.isVisible ?: true
  val isPinnedInShortcuts = calcButton?.isQuickShortcut ?: true

  var showOptionsDialog by remember { mutableStateOf(false) }

  // Calculator State - Initial value defaults to 0
  var displayValue by remember { mutableStateOf("0") }
  var expressionText by remember { mutableStateOf("") }
  var storedValue by remember { mutableStateOf<Double?>(null) }
  var pendingOperator by remember { mutableStateOf<String?>(null) }
  var isNewInput by remember { mutableStateOf(true) }

  fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "خطأ"
    return if (value % 1.0 == 0.0 && Math.abs(value) < 1e12) {
      DecimalFormat("#", DecimalFormatSymbols(Locale.US)).format(value)
    } else {
      val df = DecimalFormat("#.####", DecimalFormatSymbols(Locale.US))
      df.format(value)
    }
  }

  fun evaluate(a: Double, op: String, b: Double): Double {
    return when (op) {
      "+" -> a + b
      "-" -> a - b
      "×" -> a * b
      "÷" -> if (b != 0.0) a / b else Double.NaN
      else -> b
    }
  }

  fun onDigit(digit: String) {
    if (isNewInput || displayValue == "0" || displayValue == "خطأ") {
      displayValue = digit
      isNewInput = false
    } else {
      if (displayValue.replace(".", "").length < 12) {
        displayValue += digit
      }
    }
  }

  fun onDot() {
    if (isNewInput || displayValue == "خطأ") {
      displayValue = "0."
      isNewInput = false
    } else if (!displayValue.contains(".")) {
      displayValue += "."
    }
  }

  fun onClear() {
    displayValue = "0"
    expressionText = ""
    storedValue = null
    pendingOperator = null
    isNewInput = true
  }

  fun onBackspace() {
    if (isNewInput || displayValue == "خطأ") {
      displayValue = "0"
      isNewInput = true
      return
    }
    if (displayValue.length > 1) {
      displayValue = displayValue.dropLast(1)
      if (displayValue == "-") displayValue = "0"
    } else {
      displayValue = "0"
      isNewInput = true
    }
  }

  fun onOperator(op: String) {
    val current = displayValue.toDoubleOrNull() ?: 0.0
    if (storedValue != null && pendingOperator != null && !isNewInput) {
      val res = evaluate(storedValue!!, pendingOperator!!, current)
      storedValue = res
      displayValue = formatNumber(res)
    } else {
      storedValue = current
    }
    pendingOperator = op
    expressionText = "${formatNumber(storedValue!!)} $op"
    isNewInput = true
  }

  fun onPercentage() {
    val current = displayValue.toDoubleOrNull() ?: 0.0
    if (storedValue != null && pendingOperator != null) {
      val base = storedValue!!
      val pctRate = current
      val pctAmount = base * (pctRate / 100.0)

      val result = when (pendingOperator) {
        "+" -> base + pctAmount
        "-" -> base - pctAmount
        "×" -> pctAmount
        "÷" -> if (pctRate != 0.0) base / (pctRate / 100.0) else Double.NaN
        else -> current / 100.0
      }

      expressionText = "${formatNumber(base)} $pendingOperator ${formatNumber(pctRate)}%"
      displayValue = formatNumber(result)
      storedValue = result
      pendingOperator = null
      isNewInput = true
    } else {
      val res = current / 100.0
      expressionText = "${formatNumber(current)}%"
      displayValue = formatNumber(res)
      isNewInput = true
    }
  }

  fun onEquals() {
    if (storedValue != null && pendingOperator != null) {
      val current = displayValue.toDoubleOrNull() ?: 0.0
      val res = evaluate(storedValue!!, pendingOperator!!, current)
      expressionText = "${formatNumber(storedValue!!)} $pendingOperator ${formatNumber(current)} ="
      displayValue = formatNumber(res)
      storedValue = null
      pendingOperator = null
      isNewInput = true
    }
  }

  fun copyCurrentResult() {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("حاسبة النسبة", displayValue)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "📋 تم نسخ القيمة: $displayValue", Toast.LENGTH_SHORT).show()
  }

  // Root screen container - Deep dark navy / charcoal background matching the screenshot
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0C1322))
        .systemBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // TOP APP BAR: Options button on Left, prominent "خروج" button on Right
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left side: Settings & Pinning options
          Surface(
            onClick = { showOptionsDialog = true },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF192438),
            border = BorderStroke(1.dp, Color(0xFF263750))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                Icons.Default.Settings,
                contentDescription = "خيارات الواجهة والتثبيت",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "خيارات الزر",
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
              if (isPinnedInShortcuts) {
                Text("⭐", fontSize = 12.sp)
              }
            }
          }

          // Right side: The Burgundy "خروج" Button matching the exact screenshot design
          Surface(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF6B1D24),
            border = BorderStroke(1.dp, Color(0xFF86252E)),
            shadowElevation = 3.dp
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "خروج",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // DISPLAY CARD: Matching dark rounded box with glowing neon cyan digits
        Surface(
          onClick = { copyCurrentResult() },
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF131D2D),
          border = BorderStroke(1.5.dp, Color(0xFF1F2E45)),
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 20.dp, vertical = 14.dp)
          ) {
            // Secondary Expression line
            if (expressionText.isNotEmpty()) {
              Text(
                text = expressionText,
                color = Color(0xFF7D96B2),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                modifier = Modifier
                  .fillMaxWidth()
                  .align(Alignment.TopEnd)
              )
            }

            // Main Display Number - Neon Glowing Cyan (Text: 1250 in screenshot)
            val dynamicFontSize = when {
              displayValue.length > 10 -> 28.sp
              displayValue.length > 7 -> 34.sp
              else -> 42.sp
            }

            Text(
              text = displayValue,
              color = Color(0xFF38E1FF),
              style = TextStyle(
                fontSize = dynamicFontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                shadow = Shadow(
                  color = Color(0x6638E1FF),
                  blurRadius = 16f
                )
              ),
              maxLines = 1,
              modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // KEYPAD: 5 Rows x 4 Columns exactly matching the screenshot
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
        ) {
          val totalWidth = maxWidth
          val spacing = 12.dp
          val numCols = 4
          val keyWidth = (totalWidth - (spacing * (numCols - 1))) / numCols
          val wideZeroWidth = (keyWidth * 2) + spacing

          val keyHeight = 68.dp

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
          ) {
            // ROW 1: [ C ] [ ⌫ ] [ % ] [ ÷ ]
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
              CalcKey(
                text = "C",
                type = KeyType.RED,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onClear() }
              )
              CalcKey(
                icon = {
                  Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "حذف",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                  )
                },
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onBackspace() },
                onLongClick = { onClear() }
              )
              CalcKey(
                text = "%",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onPercentage() }
              )
              CalcKey(
                text = "÷",
                type = KeyType.ORANGE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onOperator("÷") }
              )
            }

            // ROW 2: [ 7 ] [ 8 ] [ 9 ] [ × ]
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
              CalcKey(
                text = "7",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("7") }
              )
              CalcKey(
                text = "8",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("8") }
              )
              CalcKey(
                text = "9",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("9") }
              )
              CalcKey(
                text = "×",
                type = KeyType.ORANGE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onOperator("×") }
              )
            }

            // ROW 3: [ 4 ] [ 5 ] [ 6 ] [ - ]
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
              CalcKey(
                text = "4",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("4") }
              )
              CalcKey(
                text = "5",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("5") }
              )
              CalcKey(
                text = "6",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("6") }
              )
              CalcKey(
                text = "-",
                type = KeyType.ORANGE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onOperator("-") }
              )
            }

            // ROW 4: [ 1 ] [ 2 ] [ 3 ] [ + ]
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
              CalcKey(
                text = "1",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("1") }
              )
              CalcKey(
                text = "2",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("2") }
              )
              CalcKey(
                text = "3",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDigit("3") }
              )
              CalcKey(
                text = "+",
                type = KeyType.ORANGE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onOperator("+") }
              )
            }

            // ROW 5: [ 0 (wide double-column) ] [ . ] [ = ]
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
              CalcKey(
                text = "0",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(wideZeroWidth)
                  .height(keyHeight),
                onClick = { onDigit("0") }
              )
              CalcKey(
                text = ".",
                type = KeyType.SLATE,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onDot() }
              )
              CalcKey(
                text = "=",
                type = KeyType.GREEN,
                modifier = Modifier
                  .width(keyWidth)
                  .height(keyHeight),
                onClick = { onEquals() }
              )
            }
          }
        }
      }
    }
  }

  // DIALOG FOR MANAGING VISIBILITY & PINNING IN SHORTCUTS BAR
  if (showOptionsDialog) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      Dialog(onDismissRequest = { showOptionsDialog = false }) {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
          border = BorderStroke(1.dp, Color(0xFF334155)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text("⚙️", fontSize = 20.sp)
              Text(
                text = "خيارات زر آلة حاسبة النسبة",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            Text(
              text = "يمكنك التحكم في إظهار زر الآلة الحاسبة في الواجهة الرئيسية وتثبيته في شريط الاختصارات السريعة:",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8),
              lineHeight = 18.sp
            )

            HorizontalDivider(color = Color(0xFF334155))

            // Switch 1: Show in Main Screen
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "إظهار في الواجهة الرئيسية",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "يظهر الزر ضمن أزرار الصفحة الرئيسية للتطبيق",
                  fontSize = 11.5.sp,
                  color = Color(0xFF94A3B8)
                )
              }
              Switch(
                checked = isVisibleInMain,
                onCheckedChange = { viewModel.setActionButtonVisibility("FULL_CALCULATOR", it) }
              )
            }

            // Switch 2: Pin to Quick Shortcuts Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text("⭐", fontSize = 13.sp)
                  Text(
                    text = "تثبيت في شريط الاختصارات",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
                Text(
                  text = "يظهر كاختصار مباشر وسريع دائماً في الشريط العلوي",
                  fontSize = 11.5.sp,
                  color = Color(0xFF94A3B8)
                )
              }
              Switch(
                checked = isPinnedInShortcuts,
                onCheckedChange = { viewModel.setActionButtonQuickShortcut("FULL_CALCULATOR", it) }
              )
            }

            // Quick Copy result action
            Button(
              onClick = {
                copyCurrentResult()
                showOptionsDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("نسخ النتيجة الحالية ($displayValue)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { showOptionsDialog = false },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("إغلاق", fontSize = 14.sp, color = Color.White)
            }
          }
        }
      }
    }
  }
}

private enum class KeyType {
  RED,
  SLATE,
  ORANGE,
  GREEN
}

@Composable
private fun CalcKey(
  modifier: Modifier = Modifier,
  text: String? = null,
  icon: (@Composable () -> Unit)? = null,
  type: KeyType,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(18.dp)

  val (gradientBrush, contentColor, fontSize) = when (type) {
    KeyType.RED -> Triple(
      Brush.verticalGradient(listOf(Color(0xFFEA4848), Color(0xFFDF3A3A))),
      Color.White,
      30.sp
    )
    KeyType.SLATE -> Triple(
      Brush.verticalGradient(listOf(Color(0xFF5A6577), Color(0xFF4C5565))),
      Color.White,
      28.sp
    )
    KeyType.ORANGE -> Triple(
      Brush.verticalGradient(listOf(Color(0xFFFAA424), Color(0xFFF29410))),
      Color(0xFF18181B),
      32.sp
    )
    KeyType.GREEN -> Triple(
      Brush.verticalGradient(listOf(Color(0xFF22C570), Color(0xFF19B261))),
      Color.White,
      34.sp
    )
  }

  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = modifier
      .clip(shape)
      .background(brush = gradientBrush)
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(color = Color.White.copy(alpha = 0.3f)),
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    if (icon != null) {
      icon()
    } else if (text != null) {
      Text(
        text = text,
        color = contentColor,
        fontSize = fontSize,
        fontWeight = if (type == KeyType.ORANGE || type == KeyType.RED || type == KeyType.GREEN) FontWeight.Bold else FontWeight.Medium
      )
    }
  }
}
