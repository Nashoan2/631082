package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.InvoiceViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class PercentageCalcMode(val title: String) {
  SIMPLE("بسيطة"),
  INCREASE("زيادة"),
  DECREASE("نقص"),
  COMPLETION("إكمال")
}

@Composable
fun PercentageCalculatorScreen(
  viewModel: InvoiceViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  // Find percentage calculator button config in UI customization
  val calcButton = remember(uiState.uiCustomizationConfig.buttons) {
    uiState.uiCustomizationConfig.buttons.find { it.id == "PERCENTAGE_CALCULATOR" }
  }
  val isVisibleInMain = calcButton?.isVisible ?: true
  val isPinnedInShortcuts = calcButton?.isQuickShortcut ?: true

  var showOptionsDialog by remember { mutableStateOf(false) }

  // Calculation State - Matching initial values from screenshot (250 and 10)
  var activeMode by remember { mutableStateOf(PercentageCalcMode.SIMPLE) }
  var startValueInput by remember { mutableStateOf("250") }
  var percentageInput by remember { mutableStateOf("10") }

  val startVal = startValueInput.toDoubleOrNull() ?: 0.0
  val pctVal = percentageInput.toDoubleOrNull() ?: 0.0

  fun formatNum(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    return if (value % 1.0 == 0.0 && Math.abs(value) < 1e12) {
      DecimalFormat("#", DecimalFormatSymbols(Locale.US)).format(value)
    } else {
      val df = DecimalFormat("#.####", DecimalFormatSymbols(Locale.US))
      df.format(value)
    }
  }

  // Calculations based on active mode
  val percentAmount = startVal * (pctVal / 100.0)
  val (resultValue, detailLabel, detailColor, isUpArrow) = when (activeMode) {
    PercentageCalcMode.SIMPLE -> {
      val finalRes = startVal + percentAmount
      val diffText = "زيادة : ${formatNum(percentAmount)}"
      Quadruple(finalRes, diffText, Color(0xFF1E5136), true)
    }
    PercentageCalcMode.INCREASE -> {
      val finalRes = startVal + percentAmount
      val diffText = "زيادة : ${formatNum(percentAmount)}"
      Quadruple(finalRes, diffText, Color(0xFF1E5136), true)
    }
    PercentageCalcMode.DECREASE -> {
      val finalRes = startVal - percentAmount
      val diffText = "نقص : ${formatNum(percentAmount)}"
      Quadruple(finalRes, diffText, Color(0xFFDC2626), false)
    }
    PercentageCalcMode.COMPLETION -> {
      val remaining = (100.0 - pctVal).coerceAtLeast(0.0)
      val diffText = "المتبقي لإكمال 100% : ${formatNum(remaining)}%"
      val compAmount = startVal * (remaining / 100.0)
      Quadruple(compAmount, diffText, Color(0xFF0284C7), true)
    }
  }

  fun copyResultToClipboard() {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("النتيجة", formatNum(resultValue))
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "📋 تم نسخ النتيجة: ${formatNum(resultValue)}", Toast.LENGTH_SHORT).show()
  }

  fun copyFullSummary() {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val summary = "حاسبة النسبة المئوية (${activeMode.title}):\nقيمة البداية: $startValueInput\nالنسبة: $percentageInput%\nالنتيجة: ${formatNum(resultValue)}\n$detailLabel"
    val clip = ClipData.newPlainText("تفاصيل النسبة", summary)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "📋 تم نسخ ملخص العملية بالكامل", Toast.LENGTH_SHORT).show()
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Scaffold(
      containerColor = Color(0xFFF7F5EF),
      modifier = Modifier.fillMaxSize()
    ) { paddingVals ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingVals)
          .verticalScroll(rememberScrollState())
      ) {
        // --- 1. TOP CURVED GREEN HEADER (Exact replica of screenshot 1789743174629.png) ---
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
              Brush.verticalGradient(
                listOf(
                  Color(0xFF276342),
                  Color(0xFF1B4E34),
                  Color(0xFF133C26)
                )
              )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Right button: Settings ⚙ (Translucent rounded square with border)
            Surface(
              onClick = { showOptionsDialog = true },
              shape = RoundedCornerShape(14.dp),
              color = Color(0x33FFFFFF),
              border = BorderStroke(1.dp, Color(0x3BFFFFFF)),
              modifier = Modifier.size(46.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Default.Settings,
                  contentDescription = "إعدادات وتثبيت الزر",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            // Center Title: "حاسبة النسبة المئوية"
            Text(
              text = "حاسبة النسبة المئوية",
              color = Color.White,
              fontSize = 21.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )

            // Left button: Close ✕ (Translucent rounded square with border)
            Surface(
              onClick = onBack,
              shape = RoundedCornerShape(14.dp),
              color = Color(0x33FFFFFF),
              border = BorderStroke(1.dp, Color(0x3BFFFFFF)),
              modifier = Modifier.size(46.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Default.Close,
                  contentDescription = "إغلاق",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. SEGMENTED MODE TABS (بسيطة | زيادة | نقص | إكمال) ---
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFDED9CD),
            border = BorderStroke(1.dp, Color(0xFFD0CABE)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Order matching the screenshot: بسيطة, زيادة, نقص, إكمال
              val modes = listOf(
                PercentageCalcMode.SIMPLE,
                PercentageCalcMode.INCREASE,
                PercentageCalcMode.DECREASE,
                PercentageCalcMode.COMPLETION
              )

              modes.forEach { mode ->
                val isSelected = activeMode == mode
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                      if (isSelected) {
                        Modifier
                          .shadow(3.dp, RoundedCornerShape(14.dp))
                          .background(Color.White)
                      } else {
                        Modifier.background(Color.Transparent)
                      }
                    )
                    .clickable { activeMode = mode }
                    .padding(vertical = 10.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = mode.title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF556070)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // --- 3. INPUT FIELDS SECTION ---
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
          verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
          // ROW 1: قيمة البداية
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Label on the right
            Text(
              text = "قيمة البداية",
              fontSize = 17.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1E293B)
            )

            // Input Box on the left
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color.White,
              border = BorderStroke(1.5.dp, Color(0xFFD8D4C8)),
              shadowElevation = 2.dp,
              modifier = Modifier
                .width(170.dp)
                .height(52.dp)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                BasicTextField(
                  value = startValueInput,
                  onValueChange = { startValueInput = it },
                  textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Start
                  ),
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  cursorBrush = SolidColor(Color(0xFF1B4E34)),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }

          // ROW 2: النسبة المئوية
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Label on the right
            Text(
              text = "النسبة المئوية",
              fontSize = 17.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1E293B)
            )

            // Input container with "%" box on the left
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color.White,
              border = BorderStroke(1.5.dp, Color(0xFFD8D4C8)),
              shadowElevation = 2.dp,
              modifier = Modifier
                .width(170.dp)
                .height(52.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // The "%" square box
                Box(
                  modifier = Modifier
                    .width(42.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFECEFF3))
                    .padding(horizontal = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                  )
                }

                // The input field for the percentage value
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                  contentAlignment = Alignment.CenterStart
                ) {
                  BasicTextField(
                    value = percentageInput,
                    onValueChange = { percentageInput = it },
                    textStyle = TextStyle(
                      fontSize = 22.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF1E293B),
                      textAlign = TextAlign.Start
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(Color(0xFF1B4E34)),
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
            }
          }

          // Quick Percentage Preset Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf("5", "10", "15", "20", "25", "50").forEach { pct ->
              val isSelected = percentageInput == pct
              Surface(
                onClick = { percentageInput = pct },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF1B4E34) else Color(0xFFE9E5DC),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF133C26) else Color(0xFFD8D4C8)),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$pct%",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF334155)
                  )
                }
              }
            }

            // Clear Button
            Surface(
              onClick = {
                startValueInput = "0"
                percentageInput = "0"
              },
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text("C", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- 4. THE RESULT CARD (Matching exact screenshot 1789743174629.png) ---
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
        ) {
          Surface(
            onClick = { copyResultToClipboard() },
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFFBF9F4),
            border = BorderStroke(1.5.dp, Color(0xFFE8E4D8)),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
            ) {
              // Top Right Label: "النتيجة"
              Text(
                text = "النتيجة",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                modifier = Modifier.align(Alignment.TopEnd)
              )

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 22.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                // Big Result Value (e.g. 275)
                Text(
                  text = formatNum(resultValue),
                  fontSize = 50.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2C3E50),
                  textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Detail row with arrow (e.g. "↗ زيادة : 25")
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = if (isUpArrow) "↗" else "↘",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = detailColor
                  )
                  Text(
                    text = detailLabel,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = detailColor
                  )
                }
              }

              // Sparkle symbol in the bottom right corner
              Text(
                text = "✦",
                fontSize = 22.sp,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.align(Alignment.BottomEnd)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. ACTION BUTTONS (Copy Result / Copy Full Summary) ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = { copyResultToClipboard() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4E34)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("نسخ النتيجة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { copyFullSummary() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1B4E34)),
            border = BorderStroke(1.5.dp, Color(0xFF1B4E34)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("نسخ التفاصيل", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // DIALOG FOR MANAGING VISIBILITY & PINNING IN SHORTCUTS BAR
  if (showOptionsDialog) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      Dialog(onDismissRequest = { showOptionsDialog = false }) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
              Text("⚙️", fontSize = 22.sp)
              Text(
                text = "خيارات حاسبة النسبة المئوية",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
            }

            Text(
              text = "يمكنك التحكم في إظهار زر حاسبة النسبة في الواجهة الرئيسية وتثبيته في شريط الاختصارات السريعة:",
              fontSize = 13.sp,
              color = Color(0xFF64748B),
              lineHeight = 18.sp
            )

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Switch 1: Show in Main Screen
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "إظهار في الواجهة الرئيسية",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A)
                )
                Text(
                  text = "يظهر الزر ضمن أزرار الصفحة الرئيسية للتطبيق",
                  fontSize = 11.5.sp,
                  color = Color(0xFF64748B)
                )
              }
              Switch(
                checked = isVisibleInMain,
                onCheckedChange = { viewModel.setActionButtonVisibility("PERCENTAGE_CALCULATOR", it) }
              )
            }

            // Switch 2: Pin to Quick Shortcuts Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
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
                    color = Color(0xFF0F172A)
                  )
                }
                Text(
                  text = "يظهر كاختصار مباشر ودائم في الشريط العلوي",
                  fontSize = 11.5.sp,
                  color = Color(0xFF64748B)
                )
              }
              Switch(
                checked = isPinnedInShortcuts,
                onCheckedChange = { viewModel.setActionButtonQuickShortcut("PERCENTAGE_CALCULATOR", it) }
              )
            }

            Button(
              onClick = { showOptionsDialog = false },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4E34)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("تم", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
