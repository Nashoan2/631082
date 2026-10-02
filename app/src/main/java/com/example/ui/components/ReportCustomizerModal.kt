package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ReportCustomizationConfig
import com.example.ui.screens.ShadedFieldColorCustomizerDialog
import com.example.ui.theme.computeShadedBorderColor
import com.example.ui.theme.getReportFontFamily
import com.example.ui.theme.parseHexColor
import com.example.ui.viewmodel.InvoiceViewModel

private val FONT_OPTIONS = listOf(
  "Cairo" to "خط كايرو (رسمي واحترافي)",
  "Tajawal" to "خط تجوال (عصري ومقروء)",
  "Almarai" to "خط المراعي (أنيق وواضح)",
  "Amiri" to "خط أميري (نسخي كلاسيكي)",
  "sans-serif" to "خط النظام القياسي (افتراضي)"
)

private val PRIMARY_TEXT_COLORS = listOf(
  "#111111" to "أسود داكن",
  "#333333" to "رمادي فحمي",
  "#0D47A1" to "كحلي داكن",
  "#1B5E20" to "أخضر زيتي",
  "#3E2723" to "بني داكن",
  "#263238" to "فولاذي داكن"
)

private val HEADER_COLORS = listOf(
  "#5E258D" to "بنفسجي ملكي",
  "#0070BA" to "أزرق بحري",
  "#1B5E20" to "أخضر احترافي",
  "#C62828" to "أحمر قاني",
  "#006064" to "بترولي داكن",
  "#795548" to "بني كلاسيكي",
  "#D32F2F" to "أحمر نابض",
  "#E65100" to "برتقالي داكن"
)

private val BORDER_COLORS = listOf(
  "#0070BA" to "أزرق كلاسيكي",
  "#5E258D" to "بنفسجي",
  "#B0BEC5" to "رمادي ناعم",
  "#2E7D32" to "أخضر",
  "#C62828" to "أحمر",
  "#78909C" to "رمادي مزرق"
)

private val FONT_SIZE_PRESETS = listOf(
  0.75f to "75% صغير جداً",
  0.85f to "85% صغير",
  1.00f to "100% قياسي",
  1.15f to "115% كبير",
  1.30f to "130% كبير جداً",
  1.50f to "150% ضخم"
)

private fun parseColorSafe(hex: String, fallback: Color = Color(0xFF111111)): Color {
  return try {
    val clean = hex.removePrefix("#")
    val colorInt = clean.toLong(16)
    if (clean.length == 6) {
      Color(colorInt or 0xFF000000)
    } else {
      Color(colorInt)
    }
  } catch (_: Exception) {
    fallback
  }
}

@Composable
fun ReportCustomizerModal(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val config = uiState.reportCustomizationConfig
  var activeTab by remember { mutableStateOf(0) }
  var showResetConfirm by remember { mutableStateOf(false) }
  var showShadedFieldColorDialog by remember { mutableStateOf(false) }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.96f)
          .fillMaxHeight(0.92f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Header Bar
          Surface(
            color = Color(0xFF0070BA),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  "📊 التحكم في إعدادات التقارير والطباعة",
                  fontWeight = FontWeight.Black,
                  fontSize = 17.sp,
                  color = Color.White
                )
                Text(
                  "الفواتير، كشوفات الحساب، سندات القبض والصرف (تطبيق فوري ودائم)",
                  fontSize = 11.5.sp,
                  color = Color(0xFFE3F2FD)
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                IconButton(
                  onClick = { showResetConfirm = true },
                  colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                  Icon(Icons.Default.RestartAlt, contentDescription = "استعادة الافتراضي")
                }
                IconButton(
                  onClick = onDismiss,
                  colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                  Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
              }
            }
          }

          // Tabs
          TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.White,
            contentColor = Color(0xFF0070BA)
          ) {
            Tab(
              selected = activeTab == 0,
              onClick = { activeTab = 0 },
              text = { Text("🔤 الخط والحجم", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
              selected = activeTab == 1,
              onClick = { activeTab = 1 },
              text = { Text("🎨 الألوان", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
              selected = activeTab == 2,
              onClick = { activeTab = 2 },
              text = { Text("👁️ إخفاء/إظهار", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
              selected = activeTab == 3,
              onClick = { activeTab = 3 },
              text = { Text("➕ إضافات مخصصة", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
          }

          // Tab Content
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            when (activeTab) {
              0 -> TabFontAndSize(config, viewModel)
              1 -> TabReportColors(
                config = config,
                viewModel = viewModel,
                uiState = uiState,
                onOpenShadedColorDialog = { showShadedFieldColorDialog = true }
              )
              2 -> TabVisibilityElements(config, viewModel)
              3 -> TabCustomElements(config, viewModel)
            }
          }

          // Bottom Bar
          Surface(
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                border = BorderStroke(1.dp, Color(0xFFDC3545)),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("استعادة الافتراضي", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  viewModel.showToast("✅ تم حفظ وتطبيق كافة إعدادات وتخصيصات التقارير بنجاح.")
                  onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070BA)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ وإغلاق ✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }
    }

    if (showResetConfirm) {
      AlertDialog(
        onDismissRequest = { showResetConfirm = false },
        title = {
          Text("🔄 استعادة الإعدادات الافتراضية للتقارير", fontWeight = FontWeight.Bold, color = Color(0xFF0070BA))
        },
        text = {
          Text(
            "هل ترغب في التراجع عن كافة التعديلات واستعادة الخطوط والألوان والأحجام والعناصر الافتراضية للتقارير وسندات القبض والصرف؟",
            fontSize = 14.sp
          )
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.resetReportCustomizationToDefault()
              showResetConfirm = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
          ) {
            Text("نعم، استعادة الافتراضي", fontWeight = FontWeight.Bold, color = Color.White)
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showResetConfirm = false }) {
            Text("إلغاء")
          }
        }
      )
    }

    if (showShadedFieldColorDialog) {
      ShadedFieldColorCustomizerDialog(
        initialHex = uiState.uiCustomizationConfig.shadedFieldColorHex,
        initialAlpha = uiState.uiCustomizationConfig.shadedFieldAlpha,
        onDismiss = { showShadedFieldColorDialog = false },
        onSave = { hex, alpha ->
          viewModel.updateShadedFieldColor(hex, alpha)
          showShadedFieldColorDialog = false
        },
        onReset = {
          viewModel.resetShadedFieldColor()
          showShadedFieldColorDialog = false
        }
      )
    }
  }
}

@Composable
private fun TabFontAndSize(
  config: ReportCustomizationConfig,
  viewModel: InvoiceViewModel
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Font Scaling Section (تكبير وتصغير حجم الخط)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("🔍 تكبير وتصغير حجم الخط بالتقرير", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0070BA))
              Text("يطبق على كافة الفواتير، السندات، وكشوفات الحساب", fontSize = 11.sp, color = Color.Gray)
            }
            Surface(
              color = Color(0xFFE3F2FD),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                "${(config.fontScale * 100).toInt()}%",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF0070BA),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          // Stepper row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { viewModel.decreaseReportFontScale() },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E258D)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(44.dp)
            ) {
              Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = Color.White)
              Spacer(modifier = Modifier.width(6.dp))
              Text("تصغير الخط -", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
              onClick = { viewModel.increaseReportFontScale() },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070BA)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(44.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = "تكبير", tint = Color.White)
              Spacer(modifier = Modifier.width(6.dp))
              Text("تكبير الخط +", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }

          // Quick Presets
          Text("اختيارات سريعة للحجم:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(FONT_SIZE_PRESETS) { (scale, label) ->
              val isSelected = Math.abs(config.fontScale - scale) < 0.04f
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateReportFontScale(scale) },
                label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = Color(0xFF0070BA),
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }
    }

    // Font Family Selection (نوع الخط)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("🖋️ نوع الخط العربي في التقارير", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0070BA))
          Text("اختر الخط المناسب ليتطابق مع مظهر الفواتير والسندات الرسمية:", fontSize = 11.5.sp, color = Color.Gray)

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FONT_OPTIONS.forEach { (fontKey, fontLabel) ->
              val isSelected = config.fontFamily == fontKey
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { viewModel.updateReportFontFamily(fontKey) },
                color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF8F9FA),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF0070BA) else Color(0xFFE0E0E0))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(fontLabel, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Color(0xFF0070BA) else Color(0xFF333333))
                    Text("نموذج: مؤسسة الحلول - سند قبض وفاتورة مبيعات", fontSize = 11.sp, color = Color(0xFF777777))
                  }
                  if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0070BA), modifier = Modifier.size(20.dp))
                  }
                }
              }
            }
          }
        }
      }
    }

    // Live Preview
    item {
      LiveReportPreviewCard(config)
    }
  }
}

@Composable
private fun TabReportColors(
  config: ReportCustomizationConfig,
  viewModel: InvoiceViewModel,
  uiState: com.example.ui.viewmodel.UiState,
  onOpenShadedColorDialog: () -> Unit
) {
  var activeCustomHexDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Shaded Fields Customizer Card (تخصيص وتغيير لون وتظليل الحقول)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, Color(0xFF6D28D9).copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(Color(0xFF6D28D9), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.WaterDrop,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Column {
                Text(
                  "تغيير ألوان وتظليل الحقول",
                  fontWeight = FontWeight.Black,
                  fontSize = 14.5.sp,
                  color = Color(0xFF1E1B4B)
                )
                Text(
                  "تخصيص لون تظليل حقول الإدخال بالفواتير والتقارير ودرجة شفافيتها",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
            }

            Button(
              onClick = onOpenShadedColorDialog,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("تغيير اللون 🎨", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }

          // Live mini preview of current shaded field
          val currentHex = uiState.uiCustomizationConfig.shadedFieldColorHex
          val currentAlpha = uiState.uiCustomizationConfig.shadedFieldAlpha
          val liveColor = remember(currentHex, currentAlpha) {
            parseHexColor(currentHex, Color(0xFFFFF0F3)).copy(alpha = currentAlpha)
          }
          val liveBorder = remember(liveColor) { computeShadedBorderColor(liveColor) }

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenShadedColorDialog() },
            color = liveColor,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.2.dp, liveBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "نموذج الحقل المظلل في الفواتير والتقارير ✏️",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
              Text(
                "الشفافية: ${(currentAlpha * 100).toInt()}%",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
              )
            }
          }
        }
      }
    }

    // Header & Titles Color
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("👑 لون ترويسة وعناوين التقارير", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0070BA))
          Text("يحدد لون عنوان الفاتورة، السند، واسم المتجر وأشرطة العناوين:", fontSize = 11.5.sp, color = Color.Gray)

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(HEADER_COLORS) { (hex, name) ->
              val isSelected = config.headerColorHex.equals(hex, ignoreCase = true)
              ColorPickerChip(
                colorHex = hex,
                name = name,
                isSelected = isSelected,
                onClick = { viewModel.updateReportHeaderColor(hex) }
              )
            }
          }
        }
      }
    }

    // Primary Text Color
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("📝 لون نصوص وبيانات التقارير الأساسية", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0070BA))
          Text("يحدد لون نصوص الجداول والملاحظات وأسماء العملاء والمبالغ:", fontSize = 11.5.sp, color = Color.Gray)

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PRIMARY_TEXT_COLORS) { (hex, name) ->
              val isSelected = config.primaryTextColorHex.equals(hex, ignoreCase = true)
              ColorPickerChip(
                colorHex = hex,
                name = name,
                isSelected = isSelected,
                onClick = { viewModel.updateReportPrimaryColor(hex) }
              )
            }
          }
        }
      }
    }

    // Table Border Color
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("📐 لون حدود الجداول والبطاقات", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0070BA))
          Text("يحدد لون إطارات الجداول، الفواصل، والبطاقات التعريفية:", fontSize = 11.5.sp, color = Color.Gray)

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(BORDER_COLORS) { (hex, name) ->
              val isSelected = config.tableBorderColorHex.equals(hex, ignoreCase = true)
              ColorPickerChip(
                colorHex = hex,
                name = name,
                isSelected = isSelected,
                onClick = { viewModel.updateReportTableBorderColor(hex) }
              )
            }
          }
        }
      }
    }

    // --- قسم تخصيص ألوان عناصر وبيانات الفاتورة المحددة ---
    item {
      HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.5.dp, modifier = Modifier.padding(vertical = 4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            "🎯 تخصيص ألوان عناصر وبيانات الفاتورة والتقرير",
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = Color(0xFF0070BA)
          )
          Text(
            "تغيير ألوان كل حقل وبيان بدقة وفق رغبتك (اسم العميل، الحساب، العملة، المبالغ، والكميات):",
            fontSize = 11.5.sp,
            color = Color.Gray
          )
        }

        OutlinedButton(
          onClick = {
            viewModel.updateAllAmountsColor(config.invoiceAmountColorHex)
            viewModel.showToast("✅ تم توحيد ألوان مبالغ الفاتورة وسعر الوحدة والقيمة والإجمالي.")
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0070BA)),
          border = BorderStroke(1.dp, Color(0xFF0070BA)),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.height(34.dp)
        ) {
          Text("توحيد ألوان المبالغ ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 1. لون اسم العميل
    item {
      ElementColorSelectorCard(
        title = "👤 لون اسم العميل",
        description = "يحدد لون خط اسم العميل في الفاتورة، السند، وكشف الحساب",
        currentColorHex = config.customerNameColorHex,
        onColorSelected = { viewModel.updateReportCustomerNameColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "customerName" to config.customerNameColorHex
        }
      )
    }

    // 2. لون رقم حساب العميل
    item {
      ElementColorSelectorCard(
        title = "🔢 لون رقم حساب العميل",
        description = "يحدد لون رقم الحساب (يبدأ من 100) في الفواتير وكشوفات الحساب",
        currentColorHex = config.customerAccountColorHex,
        onColorSelected = { viewModel.updateReportCustomerAccountColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "customerAccount" to config.customerAccountColorHex
        }
      )
    }

    // 3. لون رمز العملة
    item {
      ElementColorSelectorCard(
        title = "💱 لون رمز العملة",
        description = "يحدد لون رمز العملة (ريال، $، ر.س) في كافة الجداول والمبالغ",
        currentColorHex = config.currencySymbolColorHex,
        onColorSelected = { viewModel.updateReportCurrencySymbolColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "currencySymbol" to config.currencySymbolColorHex
        }
      )
    }

    // 4. لون المبلغ في الفاتورة
    item {
      ElementColorSelectorCard(
        title = "🧾 لون المبلغ في الفاتورة",
        description = "اللون الأساسي لمبالغ صفوف الفاتورة داخل الجداول",
        currentColorHex = config.invoiceAmountColorHex,
        onColorSelected = { viewModel.updateReportInvoiceAmountColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "invoiceAmount" to config.invoiceAmountColorHex
        }
      )
    }

    // 5. لون سعر الوحدة
    item {
      ElementColorSelectorCard(
        title = "🏷️ لون سعر الوحدة",
        description = "يحدد لون أرقام عمود (سعر الوحدة) في جدول الفاتورة",
        currentColorHex = config.unitPriceColorHex,
        onColorSelected = { viewModel.updateReportUnitPriceColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "unitPrice" to config.unitPriceColorHex
        }
      )
    }

    // 6. لون القيمة الإجمالية
    item {
      ElementColorSelectorCard(
        title = "📊 لون القيمة الإجمالية للسطر",
        description = "يحدد لون أرقام عمود (القيمة الإجمالية) لكل صنف في الفاتورة",
        currentColorHex = config.totalValueColorHex,
        onColorSelected = { viewModel.updateReportTotalValueColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "totalValue" to config.totalValueColorHex
        }
      )
    }

    // 7. لون العدد (الكمية)
    item {
      ElementColorSelectorCard(
        title = "📦 لون العدد (الكمية)",
        description = "يحدد لون أرقام عمود (العدد / الأشهر / الكمية) في جدول الفاتورة",
        currentColorHex = config.quantityColorHex,
        onColorSelected = { viewModel.updateReportQuantityColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "quantity" to config.quantityColorHex
        }
      )
    }

    // 8. لون رقم الصنف
    item {
      ElementColorSelectorCard(
        title = "🔢 لون رقم الصنف",
        description = "يحدد لون التسلسل الرقمي للأصناف (1، 2، 3...) في الجدول",
        currentColorHex = config.itemNumberColorHex,
        onColorSelected = { viewModel.updateReportItemNumberColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "itemNumber" to config.itemNumberColorHex
        }
      )
    }

    // 9. لون المبلغ الإجمالي
    item {
      ElementColorSelectorCard(
        title = "💰 لون المبلغ الإجمالي (Grand Total)",
        description = "يحدد لون رقم المبلغ الإجمالي العام أسفل الفاتورة",
        currentColorHex = config.grandTotalAmountColorHex,
        onColorSelected = { viewModel.updateReportGrandTotalAmountColor(it) },
        onRequestCustomHex = {
          activeCustomHexDialog = "grandTotal" to config.grandTotalAmountColorHex
        }
      )
    }

    // Live Preview
    item {
      LiveReportPreviewCard(config)
    }
  }

  activeCustomHexDialog?.let { (key, initialHex) ->
    val title = when (key) {
      "customerName" -> "لون اسم العميل"
      "customerAccount" -> "لون رقم حساب العميل"
      "currencySymbol" -> "لون رمز العملة"
      "invoiceAmount" -> "لون المبلغ في الفاتورة"
      "unitPrice" -> "لون سعر الوحدة"
      "totalValue" -> "لون القيمة الإجمالية"
      "quantity" -> "لون العدد (الكمية)"
      "itemNumber" -> "لون رقم الصنف"
      "grandTotal" -> "لون المبلغ الإجمالي"
      else -> "لون العنصر"
    }
    CustomHexColorDialog(
      title = title,
      initialHex = initialHex,
      onDismiss = { activeCustomHexDialog = null },
      onSave = { newHex ->
        when (key) {
          "customerName" -> viewModel.updateReportCustomerNameColor(newHex)
          "customerAccount" -> viewModel.updateReportCustomerAccountColor(newHex)
          "currencySymbol" -> viewModel.updateReportCurrencySymbolColor(newHex)
          "invoiceAmount" -> viewModel.updateReportInvoiceAmountColor(newHex)
          "unitPrice" -> viewModel.updateReportUnitPriceColor(newHex)
          "totalValue" -> viewModel.updateReportTotalValueColor(newHex)
          "quantity" -> viewModel.updateReportQuantityColor(newHex)
          "itemNumber" -> viewModel.updateReportItemNumberColor(newHex)
          "grandTotal" -> viewModel.updateReportGrandTotalAmountColor(newHex)
        }
        activeCustomHexDialog = null
      }
    )
  }
}

private val ELEMENT_COLOR_PRESETS = listOf(
  "#111111" to "أسود داكن",
  "#0070BA" to "أزرق بحري",
  "#C62828" to "أحمر قاني",
  "#2E7D32" to "أخضر زمردي",
  "#5E258D" to "بنفسجي ملكي",
  "#E65100" to "برتقالي داكن",
  "#D97706" to "عنبري ذهبي",
  "#0D47A1" to "كحلي داكن",
  "#006064" to "بترولي داكن",
  "#616161" to "رمادي فحمي",
  "#881337" to "ياقوتي نبيذي"
)

@Composable
private fun ElementColorSelectorCard(
  title: String,
  description: String,
  currentColorHex: String,
  onColorSelected: (String) -> Unit,
  onRequestCustomHex: () -> Unit
) {
  val currentColor = parseColorSafe(currentColorHex)
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(title, fontWeight = FontWeight.Black, fontSize = 13.5.sp, color = Color(0xFF1E293B))
          Text(description, fontSize = 11.sp, color = Color(0xFF64748B))
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            color = currentColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, currentColor.copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(currentColor)
                  .border(1.dp, Color.White, CircleShape)
              )
              Text(
                currentColorHex.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = currentColor
              )
            }
          }

          OutlinedButton(
            onClick = onRequestCustomHex,
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("مخصص #", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(ELEMENT_COLOR_PRESETS) { (hex, name) ->
          val isSelected = currentColorHex.equals(hex, ignoreCase = true)
          ColorPickerChip(
            colorHex = hex,
            name = name,
            isSelected = isSelected,
            onClick = { onColorSelected(hex) }
          )
        }
      }
    }
  }
}

@Composable
private fun CustomHexColorDialog(
  title: String,
  initialHex: String,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var hexInput by remember { mutableStateOf(initialHex.removePrefix("#")) }
  val previewColor = remember(hexInput) { parseColorSafe("#$hexInput", Color(0xFF111111)) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier.fillMaxWidth(0.92f)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🎨 $title", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0070BA))
        Text("أدخل كود اللون بتنسيق HEX (مثال: FF5722 أو 0070BA):", fontSize = 12.sp, color = Color.Gray)

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(previewColor)
              .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
          )
          OutlinedTextField(
            value = hexInput,
            onValueChange = { hexInput = it.take(8).filter { ch -> ch.isLetterOrDigit() } },
            prefix = { Text("#", fontWeight = FontWeight.Black) },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("إلغاء")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val formatted = if (hexInput.startsWith("#")) hexInput else "#$hexInput"
              onSave(formatted)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070BA)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("حفظ اللون", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun ColorPickerChip(
  colorHex: String,
  name: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val c = parseColorSafe(colorHex)
  Surface(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF9F9F9),
    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF0070BA) else Color(0xFFE0E0E0))
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(c)
          .border(1.5.dp, Color.White, CircleShape)
      ) {
        if (isSelected) {
          Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp).align(Alignment.Center)
          )
        }
      }
      Text(name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold)
    }
  }
}

@Composable
private fun TabVisibilityElements(
  config: ReportCustomizationConfig,
  viewModel: InvoiceViewModel
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text(
        "👁️ التحكم في إخفاء وإظهار عناصر التقارير والسندات",
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        color = Color(0xFF0070BA)
      )
      Text(
        "قم بتعطيل أو تفعيل أي عنصر ليلائم نموذج العمل الخاص بك (يطبق تلقائياً):",
        fontSize = 11.5.sp,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp)
      )
    }

    val items = listOf(
      Triple("🏢 بيانات وترويسة المتجر (العنوان والهاتف)", config.showStoreInfo, "storeInfo"),
      Triple("🖼️ شعار المتجر (اللوجو)", config.showLogo, "logo"),
      Triple("📅 تاريخ ووقت إصدار التقرير", config.showDateTime, "dateTime"),
      Triple("🏬 اسم الفرع في الترويسة", config.showBranch, "branch"),
      Triple("🔢 رقم حساب العميل في السند والفاتورة", config.showCustomerAccountNumber, "accountNumber"),
      Triple("✍️ تفقيط المبلغ كتابة بالحروف", config.showAmountInWords, "amountInWords"),
      Triple("📦 بوكس بيانات الكرت والاشتراك (في الفاتورة)", config.showCardSubscriptionBox, "subscriptionBox"),
      Triple("💧 العلامة المائية في خلفية التقارير", config.showWatermark, "watermark"),
      Triple("📋 الشروط والأحكام أسفل الفاتورة والتقارير", config.showTermsAndNotes, "termsAndNotes"),
      Triple("✍️ خانات توقيع المشتري وتوقيع البائع", config.showSignatures, "signatures"),
      Triple("🛡️ ختم الاعتماد الرسمي (معتمد APPROVED)", config.showStampSeal, "stampSeal")
    )

    items(items) { (title, isChecked, key) ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isChecked) Color.White else Color(0xFFF5F5F5)),
        border = BorderStroke(1.dp, if (isChecked) Color(0xFFB0BEC5) else Color(0xFFE0E0E0))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            title,
            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (isChecked) Color(0xFF212529) else Color(0xFF757575),
            modifier = Modifier.weight(1f)
          )
          Switch(
            checked = isChecked,
            onCheckedChange = { viewModel.toggleReportElement(key, it) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = Color(0xFF0070BA)
            )
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(6.dp))
      LiveReportPreviewCard(config)
    }
  }
}

@Composable
private fun TabCustomElements(
  config: ReportCustomizationConfig,
  viewModel: InvoiceViewModel
) {
  var watermarkText by remember(config.customWatermarkText) { mutableStateOf(config.customWatermarkText) }
  var headerTitle by remember(config.customHeaderTitle) { mutableStateOf(config.customHeaderTitle) }
  var noticeBadge by remember(config.customNoticeBadge) { mutableStateOf(config.customNoticeBadge) }
  var taxCrNumber by remember(config.taxOrCrNumber) { mutableStateOf(config.taxOrCrNumber) }
  var footerText by remember(config.customFooterText) { mutableStateOf(config.customFooterText) }
  var accountantName by remember(config.accountantSignatureName) { mutableStateOf(config.accountantSignatureName) }
  var managerName by remember(config.managerSignatureName) { mutableStateOf(config.managerSignatureName) }

  fun applyChanges() {
    val updated = config.copy(
      customWatermarkText = watermarkText.trim(),
      customHeaderTitle = headerTitle.trim(),
      customNoticeBadge = noticeBadge.trim(),
      taxOrCrNumber = taxCrNumber.trim(),
      customFooterText = footerText.trim(),
      accountantSignatureName = accountantName.trim(),
      managerSignatureName = managerName.trim()
    )
    viewModel.updateReportCustomizationConfig(updated)
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        "➕ إضافة نصوص وتفاصيل إضافية مخصصة للتقارير",
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        color = Color(0xFF0070BA)
      )
      Text(
        "تضاف هذه البيانات في ترويسة وتذييل وخانات اعتماد الفواتير وكشوفات الحساب وسندات القبض والصرف:",
        fontSize = 11.5.sp,
        color = Color.Gray
      )
    }

    // Custom Watermark Text Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("💧 نص العلامة المائية النصية في خلفية التقارير (Watermark)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
          Text("اكتب النص المراد ظهوره كعلامة مائية في خلفية الفواتير وكشوفات الحساب وسندات القبض والصرف:", fontSize = 11.sp, color = Color.Gray)
          OutlinedTextField(
            value = watermarkText,
            onValueChange = {
              watermarkText = it
              applyChanges()
            },
            placeholder = { Text("مثال: المملكة للإلكترونيات") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      }
    }

    // Custom Header Title
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("📌 عنوان رأس إضافي للتقرير (Custom Header Title)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
          Text("مثال: فرع الرياض الرئيسي - شركة المملكة للاتصالات", fontSize = 11.sp, color = Color.Gray)
          OutlinedTextField(
            value = headerTitle,
            onValueChange = {
              headerTitle = it
              applyChanges()
            },
            placeholder = { Text("أدخل عنوان إضافي في أعلى التقرير (اختياري)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Custom Notice Badge
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("🏷️ شريط تنبيه / بادج مميز في الأعلى (Notice Badge)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
          Text("مثال: نسخة رسمية معتمدة ضريبياً أو كشف حساب مدقق", fontSize = 11.sp, color = Color.Gray)
          OutlinedTextField(
            value = noticeBadge,
            onValueChange = {
              noticeBadge = it
              applyChanges()
            },
            placeholder = { Text("مثال: ★ نسخة إلكترونية معتمدة ★") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Tax or CR Number
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("🏢 الرقم الضريبي / السجل التجاري في الترويسة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
          OutlinedTextField(
            value = taxCrNumber,
            onValueChange = {
              taxCrNumber = it
              applyChanges()
            },
            placeholder = { Text("مثال: 310294857200003") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Signatures Custom Names
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("✍️ أسماء المعتمدين في خانات التوقيع", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
              Text("اسم المحاسب المعتمد:", fontSize = 12.sp, color = Color.Gray)
              Spacer(modifier = Modifier.height(2.dp))
              OutlinedTextField(
                value = accountantName,
                onValueChange = {
                  accountantName = it
                  applyChanges()
                },
                placeholder = { Text("مثال: أ. محمد") },
                modifier = Modifier.fillMaxWidth()
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text("اسم المدير المعتمد:", fontSize = 12.sp, color = Color.Gray)
              Spacer(modifier = Modifier.height(2.dp))
              OutlinedTextField(
                value = managerName,
                onValueChange = {
                  managerName = it
                  applyChanges()
                },
                placeholder = { Text("مثال: المدير العام") },
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }

    // Custom Footer Text
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("📝 نص تذييل مخصص أسفل كل التقارير (Custom Footer Note)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
          Text("مثال: نشكر لكم حسن تعاملكم - البضاعة المباعة ترد وتستبدل خلال 3 أيام", fontSize = 11.sp, color = Color.Gray)
          OutlinedTextField(
            value = footerText,
            onValueChange = {
              footerText = it
              applyChanges()
            },
            placeholder = { Text("أدخل الملاحظة الختامية للتقارير...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    item {
      LiveReportPreviewCard(config)
    }
  }
}

@Composable
private fun LiveReportPreviewCard(config: ReportCustomizationConfig) {
  val hColor = parseColorSafe(config.headerColorHex, Color(0xFF0070BA))
  val tColor = parseColorSafe(config.primaryTextColorHex, Color(0xFF111111))
  val bColor = parseColorSafe(config.tableBorderColorHex, Color(0xFF0070BA))
  val custNameColor = parseColorSafe(config.customerNameColorHex, Color(0xFF0070BA))
  val custAccColor = parseColorSafe(config.customerAccountColorHex, Color(0xFFC62828))
  val currSymColor = parseColorSafe(config.currencySymbolColorHex, tColor)
  val invAmtColor = parseColorSafe(config.invoiceAmountColorHex, tColor)
  val unitPrColor = parseColorSafe(config.unitPriceColorHex, invAmtColor)
  val totalValColor = parseColorSafe(config.totalValueColorHex, invAmtColor)
  val qtyColor = parseColorSafe(config.quantityColorHex, tColor)
  val itemNumColor = parseColorSafe(config.itemNumberColorHex, tColor)
  val grandTotColor = parseColorSafe(config.grandTotalAmountColorHex, Color(0xFF000000))
  val baseSize = (13 * config.fontScale).sp
  val reportFont = getReportFontFamily(config.fontFamily)

  CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = reportFont)
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
      border = BorderStroke(1.5.dp, bColor)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("👁️ معاينة حية للألوان والمظهر في التقارير والفواتير:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = hColor)
          Text("حجم الخط: ${(config.fontScale * 100).toInt()}% | ${config.fontFamily}", fontSize = 10.sp, color = Color.Gray)
        }

        if (config.customNoticeBadge.isNotBlank()) {
          Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFF81C784)),
            modifier = Modifier.align(Alignment.CenterHorizontally)
          ) {
            Text(config.customNoticeBadge, fontSize = (11 * config.fontScale).sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
          }
        }

        if (config.customHeaderTitle.isNotBlank()) {
          Text(config.customHeaderTitle, fontSize = (14 * config.fontScale).sp, fontWeight = FontWeight.Black, color = hColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Text("فاتورة مبيعات / سند قبض / كشف حساب", fontSize = (16 * config.fontScale).sp, fontWeight = FontWeight.Black, color = hColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

        if (config.showStoreInfo) {
          Text("المملكة للإلكترونيات - إب شارع تعز - تلفون: 772707736", fontSize = baseSize, color = tColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        if (config.taxOrCrNumber.isNotBlank()) {
          Text("الرقم الضريبي / السجل: ${config.taxOrCrNumber}", fontSize = (11 * config.fontScale).sp, color = tColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        HorizontalDivider(color = bColor, thickness = 1.dp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("اسم العميل: أحمد علي", fontSize = baseSize, fontWeight = FontWeight.Bold, color = custNameColor)
          if (config.showCustomerAccountNumber) {
            Text("(رقم الحساب: 100)", fontSize = baseSize, color = custAccColor, fontWeight = FontWeight.Black)
          }
        }

        // Preview items table showing all customized element colors
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, bColor, RoundedCornerShape(4.dp))
        ) {
          // Table header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(bColor.copy(alpha = 0.1f))
              .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("القيمة الإجمالية", fontSize = 10.sp, fontWeight = FontWeight.Black, color = hColor, modifier = Modifier.weight(2.2f), textAlign = TextAlign.Center)
            Text("سعر الوحدة", fontSize = 10.sp, fontWeight = FontWeight.Black, color = hColor, modifier = Modifier.weight(2.2f), textAlign = TextAlign.Center)
            Text("العدد", fontSize = 10.sp, fontWeight = FontWeight.Black, color = hColor, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
            Text("التفاصيل", fontSize = 10.sp, fontWeight = FontWeight.Black, color = hColor, modifier = Modifier.weight(3.4f), textAlign = TextAlign.Center)
            Text("الصنف", fontSize = 10.sp, fontWeight = FontWeight.Black, color = hColor, modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center)
          }
          HorizontalDivider(color = bColor, thickness = 1.dp)
          // Row 1
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(modifier = Modifier.weight(2.2f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
              Text("$", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = currSymColor)
              Spacer(modifier = Modifier.width(2.dp))
              Text("1,500.00", fontSize = 11.sp, fontWeight = FontWeight.Black, color = totalValColor)
            }
            Row(modifier = Modifier.weight(2.2f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
              Text("$", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = currSymColor)
              Spacer(modifier = Modifier.width(2.dp))
              Text("1,500.00", fontSize = 11.sp, fontWeight = FontWeight.Black, color = unitPrColor)
            }
            Text("1 أشهر", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = qtyColor, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
            Text("تجديد باقة تميز", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = tColor, modifier = Modifier.weight(3.4f), textAlign = TextAlign.Center)
            Text("1", fontSize = 11.sp, fontWeight = FontWeight.Black, color = itemNumColor, modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center)
          }
          HorizontalDivider(color = bColor, thickness = 1.dp)
          // Grand total row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White)
              .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("الإجمالي:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = currSymColor)
                Spacer(modifier = Modifier.width(2.dp))
                Text("1,500.00", fontSize = 12.sp, fontWeight = FontWeight.Black, color = grandTotColor)
              }
            }
            if (config.showAmountInWords) {
              Text("ألف وخمسمائة دولار أمريكي", fontSize = 10.sp, color = tColor, fontWeight = FontWeight.Bold)
            }
          }
        }

        if (config.showStampSeal) {
          Surface(
            color = Color(0xFFFFEBEE),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Color(0xFFC62828)),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
          ) {
            Text("✓ معتمد رسمياً APPROVED", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFC62828), modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp))
          }
        }

        if (config.showSignatures) {
          Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("توقيع المحاسب: ${config.accountantSignatureName.ifEmpty { "..........." }}", fontSize = (11 * config.fontScale).sp, color = tColor)
            Text("توقيع المستلم: ...........", fontSize = (11 * config.fontScale).sp, color = tColor)
          }
        }

        if (config.customFooterText.isNotBlank()) {
          Text(config.customFooterText, fontSize = (11 * config.fontScale).sp, color = Color(0xFF555555), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        }
      }
    }
  }
}
