package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.ExchangeRates
import com.example.ui.theme.LocalShadedFieldBorder
import com.example.ui.theme.LocalShadedFieldColor
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ArabicNumberHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class CurrencyCardData(
  val code: String,
  val nameAr: String,
  val symbol: String,
  val shortLabel: String
)

@Composable
fun CurrencyConverterScreen(
  viewModel: InvoiceViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val rates = uiState.exchangeRates

  val shadedBg = LocalShadedFieldColor.current
  val shadedBorder = LocalShadedFieldBorder.current

  var activeCurrency by remember { mutableStateOf("USD") } // "USD", "SAR", "YER"
  var amountInput by remember { mutableStateOf("100") }
  var showRatesDialog by remember { mutableStateOf(false) }

  val currencies = listOf(
    CurrencyCardData(code = "USD", nameAr = "دولار أمريكي", symbol = "USD", shortLabel = "دولار"),
    CurrencyCardData(code = "SAR", nameAr = "ريال سعودي", symbol = "SAR", shortLabel = "ريال سعودي"),
    CurrencyCardData(code = "YER", nameAr = "ريال يمني", symbol = "YER", shortLabel = "ريال يمني")
  )

  // Calculations
  val cleanAmountStr = ArabicNumberHelper.toEngDigits(amountInput).trim()
  val rawAmount = cleanAmountStr.toDoubleOrNull() ?: 0.0

  val usdVal: Double
  val sarVal: Double
  val yerVal: Double

  when (activeCurrency) {
    "USD" -> {
      usdVal = rawAmount
      sarVal = rawAmount * rates.usdToSar
      yerVal = rawAmount * rates.usdToYer
    }
    "SAR" -> {
      sarVal = rawAmount
      usdVal = if (rates.usdToSar > 0) rawAmount / rates.usdToSar else 0.0
      yerVal = if (rates.sarToYer > 0) rawAmount * rates.sarToYer else (usdVal * rates.usdToYer)
    }
    "YER" -> {
      yerVal = rawAmount
      usdVal = if (rates.usdToYer > 0) rawAmount / rates.usdToYer else (rawAmount * rates.yerToUsd)
      sarVal = if (rates.sarToYer > 0) rawAmount / rates.sarToYer else (usdVal * rates.usdToSar)
    }
    else -> {
      usdVal = rawAmount
      sarVal = rawAmount * rates.usdToSar
      yerVal = rawAmount * rates.usdToYer
    }
  }

  fun formatValue(v: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val df = if (v >= 1000.0 || v % 1.0 == 0.0) {
      DecimalFormat("#,##0.##", symbols)
    } else {
      DecimalFormat("#,##0.##", symbols)
    }
    return df.format(v)
  }

  fun getValueFor(code: String): Double = when (code) {
    "USD" -> usdVal
    "SAR" -> sarVal
    "YER" -> yerVal
    else -> 0.0
  }

  val activeCard = currencies.find { it.code == activeCurrency } ?: currencies[0]
  val otherCards = currencies.filter { it.code != activeCurrency }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Scaffold(
      containerColor = Color(0xFFF6F8FA)
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        // --- Top Bar: Exit button on left, Title and Rates button on right ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Exit button (styled pill like in the screenshot)
          CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Surface(
              onClick = onBack,
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
              modifier = Modifier.height(38.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "Exit",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1E293B)
                )
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                  contentDescription = "Exit",
                  tint = Color(0xFF1E293B),
                  modifier = Modifier.size(17.dp)
                )
              }
            }
          }

          // Right side: Edit Rate Button + Title with Currency Icon
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Edit Rates Button ("تغيير السعر وحفظه ✏️")
            Surface(
              onClick = { showRatesDialog = true },
              shape = RoundedCornerShape(20.dp),
              color = Color.White,
              border = BorderStroke(1.dp, Color(0xFFB0BEC5)),
              shadowElevation = 1.dp,
              modifier = Modifier.height(38.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "تعديل",
                  tint = Color(0xFF1E293B),
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "تغيير السعر وحفظه",
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1E293B)
                )
              }
            }

            // Title with Currency Exchange Icon
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "محول العملات",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
              )
              Text(
                text = "💱",
                fontSize = 24.sp
              )
            }
          }
        }

        // Subtitle instructions
        Text(
          text = "اختر العملة أولاً ثم أدخل المبلغ لتظهر النتيجة تلقائياً",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF334155),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // --- 3 Currency Cards ---
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          currencies.forEach { item ->
            val isSelected = activeCurrency == item.code

            Card(
              onClick = {
                if (!isSelected) {
                  activeCurrency = item.code
                }
              },
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 2.dp),
              border = BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                // Left Area (In RTL: Start side is Left in LTR, Right in RTL):
                // In RTL layout, start is on the right and end is on the left.
                // In the screenshot:
                // Right side has: Radio circle + Currency Name (e.g. دولار أمريكي) + Currency code (e.g. USD)
                // Left side has: The Shaded Input Box (with "ادخل المبلغ" & "100") OR Result container (e.g. "375")

                // Let's explicitly put the Value/Input on the Left and the Currency Label + Radio on the Right!
                // Since this is inside RTL:
                // First element in Row goes to Right, second element goes to Left!
                // 1) Currency Label & Radio Button (Right side)
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  // Custom styled Radio button
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFCBD5E1),
                        shape = CircleShape
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    if (isSelected) {
                      Box(
                        modifier = Modifier
                          .size(12.dp)
                          .background(Color(0xFF2E7D32), CircleShape)
                      )
                    }
                  }

                  Column(
                    verticalArrangement = Arrangement.Center
                  ) {
                    Text(
                      text = item.nameAr,
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color(0xFF1B4332) else Color(0xFF1E293B)
                    )
                    Text(
                      text = item.symbol,
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF64748B)
                    )
                  }
                }

                // 2) Value or Shaded Input Box (Left side in RTL)
                if (isSelected) {
                  // Shaded Input Field Container
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = shadedBg,
                    border = BorderStroke(1.5.dp, shadedBorder),
                    modifier = Modifier
                      .widthIn(min = 140.dp, max = 175.dp)
                      .height(68.dp)
                  ) {
                    Column(
                      modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                      verticalArrangement = Arrangement.SpaceBetween,
                      horizontalAlignment = Alignment.Start
                    ) {
                      Text(
                        text = "ادخل المبلغ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF854D0E)
                      )

                      BasicTextField(
                        value = amountInput,
                        onValueChange = { newText ->
                          // Allow digits and single decimal point
                          val clean = ArabicNumberHelper.toEngDigits(newText)
                          if (clean.isEmpty() || clean.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amountInput = clean
                          }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                          fontSize = 24.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF7F1D1D),
                          textAlign = TextAlign.Start
                        ),
                        cursorBrush = SolidColor(Color(0xFF7F1D1D)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                      )
                    }
                  }
                } else {
                  // Converted Result Box (Soft blue/grey background container)
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEBF0F5),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                      .widthIn(min = 140.dp, max = 175.dp)
                      .height(68.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                      contentAlignment = Alignment.CenterStart
                    ) {
                      val formattedResult = formatValue(getValueFor(item.code))
                      Text(
                        text = formattedResult,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- Bottom Action Buttons: [نسخ النتيجة] (Green) and [مسح] (Dark Red) ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Green Copy Result Button
          Button(
            onClick = {
              val formattedInput = if (rawAmount > 0) formatValue(rawAmount) else "0"
              val card1 = otherCards.getOrNull(0)
              val card2 = otherCards.getOrNull(1)

              val summaryShort = buildString {
                append("$formattedInput ${activeCard.shortLabel}")
                if (card1 != null) {
                  append(" = ${formatValue(getValueFor(card1.code))} ${card1.shortLabel}")
                }
                if (card2 != null) {
                  append(" • $formattedInput ${activeCard.shortLabel} = ${formatValue(getValueFor(card2.code))} ${card2.shortLabel}")
                }
              }

              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
              val clip = ClipData.newPlainText("Currency Conversion Result", summaryShort)
              clipboard?.setPrimaryClip(clip)

              viewModel.showToast("✅ تم نسخ نتيجة التحويل إلى الحافظة بنجاح.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5136)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .weight(1f)
              .height(54.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "نسخ النتيجة",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "نسخ",
                tint = Color.White,
                modifier = Modifier.size(19.dp)
              )
            }
          }

          // Dark Red Clear Button
          Button(
            onClick = {
              amountInput = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A1521)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .weight(1f)
              .height(54.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "مسح",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "مسح",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Bottom dynamic conversion summary line ---
        val formattedAmount = if (rawAmount > 0) formatValue(rawAmount) else "0"
        val card1 = otherCards.getOrNull(0)
        val card2 = otherCards.getOrNull(1)

        val summaryText = buildString {
          if (card1 != null && card2 != null) {
            append("$formattedAmount ${activeCard.shortLabel} = ${formatValue(getValueFor(card1.code))} ${card1.shortLabel}")
            append(" • ")
            append("$formattedAmount ${activeCard.shortLabel} = ${formatValue(getValueFor(card2.code))} ${card2.shortLabel}")
          }
        }

        Text(
          text = summaryText,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF1E293B),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // --- Exchange Rates Customizer Dialog ---
    if (showRatesDialog) {
      ExchangeRatesEditorDialog(
        currentRates = rates,
        onDismiss = { showRatesDialog = false },
        onSave = { updatedRates ->
          viewModel.updateExchangeRates(updatedRates)
          showRatesDialog = false
        }
      )
    }
  }
}

@Composable
fun ExchangeRatesEditorDialog(
  currentRates: ExchangeRates,
  onDismiss: () -> Unit,
  onSave: (ExchangeRates) -> Unit
) {
  var usdToYerStr by remember { mutableStateOf(currentRates.usdToYer.toString()) }
  var sarToYerStr by remember { mutableStateOf(currentRates.sarToYer.toString()) }
  var usdToSarStr by remember { mutableStateOf(currentRates.usdToSar.toString()) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(16.dp),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Dialog Title
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("✏️", fontSize = 20.sp)
            Text(
              text = "تعديل أسعار الصرف وحفظها",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1E293B)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
          }
        }

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))

        Text(
          text = "أدخل أسعار الصرف المعتمدة لمتجرك لتطبيقها فورياً على المحول وحفظها بشكل دائم:",
          fontSize = 12.5.sp,
          color = Color(0xFF475569)
        )

        // 1. USD to YER
        OutlinedTextField(
          value = usdToYerStr,
          onValueChange = { usdToYerStr = ArabicNumberHelper.toEngDigits(it) },
          label = { Text("1 دولار أمريكي (USD) = كم ريال يمني (YER)؟") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        // 2. SAR to YER
        OutlinedTextField(
          value = sarToYerStr,
          onValueChange = { sarToYerStr = ArabicNumberHelper.toEngDigits(it) },
          label = { Text("1 ريال سعودي (SAR) = كم ريال يمني (YER)؟") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        // 3. USD to SAR
        OutlinedTextField(
          value = usdToSarStr,
          onValueChange = { usdToSarStr = ArabicNumberHelper.toEngDigits(it) },
          label = { Text("1 دولار أمريكي (USD) = كم ريال سعودي (SAR)؟") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        // Quick Preset Buttons
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("اختيار أسعار سريعة شائعة:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                usdToYerStr = "533"
                sarToYerStr = "142.13"
                usdToSarStr = "3.75"
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Text("سعر صنعاء (533 / 142)", fontSize = 11.sp, textAlign = TextAlign.Center)
            }

            OutlinedButton(
              onClick = {
                usdToYerStr = "1600"
                sarToYerStr = "420"
                usdToSarStr = "3.75"
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
              Text("سعر عدن (1600 / 420)", fontSize = 11.sp, textAlign = TextAlign.Center)
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
          ) {
            Text("إلغاء", fontSize = 14.sp)
          }

          Button(
            onClick = {
              val uToY = usdToYerStr.toDoubleOrNull() ?: 533.0
              val sToY = sarToYerStr.toDoubleOrNull() ?: 142.1333
              val uToS = usdToSarStr.toDoubleOrNull() ?: 3.75

              val yToU = if (uToY > 0) 1.0 / uToY else 0.001876
              val yToS = if (sToY > 0) 1.0 / sToY else 0.007035
              val sToU = if (uToS > 0) 1.0 / uToS else 0.2667

              val newRates = ExchangeRates(
                yerToUsd = yToU,
                usdToYer = uToY,
                yerToSar = yToS,
                sarToYer = sToY,
                usdToSar = uToS,
                sarToUsd = sToU
              )
              onSave(newRates)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5136)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1.5f)
              .height(44.dp)
          ) {
            Text("💾 حفظ السعر وتطبيقه", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}
