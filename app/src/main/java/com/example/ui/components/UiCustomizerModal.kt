package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppActionButton
import com.example.data.ButtonLayout
import com.example.data.ButtonSize
import com.example.data.FormCustomField
import com.example.ui.viewmodel.InvoiceViewModel

private val PRESET_COLORS = listOf(
  "#007BFF" to "أزرق",
  "#28A745" to "أخضر",
  "#DC3545" to "أحمر",
  "#6F42C1" to "بنفسجي",
  "#17A2B8" to "فيروزي",
  "#FD7E14" to "برتقالي",
  "#5E258D" to "عنابي",
  "#4682B4" to "فولاذي",
  "#495057" to "رمادي داكن",
  "#D39E00" to "ذهبي",
  "#E83E8C" to "وردي",
  "#20C997" to "نعناعي"
)

private val PRESET_ICONS = listOf(
  "➕", "💱", "٪", "🧮", "📄", "🖨️", "📂", "👥", "💰", "💸", "📋", "🚪",
  "⚙️", "💾", "⚡", "⭐", "🛒", "🏷️", "📞", "🌐",
  "📦", "🔍", "📊", "🎯", "📌", "🔔", "💼", "💎"
)

private val ACTION_TYPE_OPTIONS = listOf(
  "PERCENTAGE_CALCULATOR" to "٪ حاسبة النسبة المئوية",
  "FULL_CALCULATOR" to "🧮 اله حاسبه النسبة ",
  "CURRENCY_CONVERTER" to "💱 محول العملات السريع",
  "PAYMENT_VOUCHER" to "💸 سند صرف فوري",
  "RECEIPT_VOUCHER" to "💰 سند قبض فوري",
  "NEW_INVOICE" to "➕ إضافة فاتورة جديدة",
  "CUSTOMERS" to "👥 إدارة العملاء",
  "ALL_CUSTOMERS" to "📋 كشف جميع العملاء",
  "CUSTOMERS_WITH_BALANCE" to "⚖️ العملاء الذين في حساباتهم أرصدة فقط",
  "OPEN_REPORT" to "📄 معاينة الفاتورة",
  "CUSTOMER_DISPLAY" to "📱 وضع العرض للعميل (شاشة واضحة وكبيرة)",
  "PRINT_INVOICE" to "🖨️ طباعة الفاتورة",
  "EXPORT_PDF" to "📄 تصدير الفاتورة PDF",
  "SAVED_INVOICES" to "📂 إدارة الفواتير المحفوظة",
  "ROOM_BACKUP" to "💾 النسخ الاحتياطي المحلي Room",
  "SETTINGS" to "⚙️ إعدادات المتجر",
  "EXIT_APP" to "🚪 خروج من التطبيق نهائياً",
  "REPORT_CONTROL" to "📊 التحكم في التقارير",
  "CUSTOM_PACKAGE" to "⚡ باقة سريعة (سعر ووصف محدد مسبقاً)",
  "CUSTOM_STATEMENT" to "📊 كشف حساب عميل محدد برقم الحساب",
  "CUSTOM_PHONE" to "📞 اتصال سريع برقم هاتف",
  "CUSTOM_URL" to "🌐 فتح رابط موقع خارجي"
)

private fun parseColorSafe(hex: String, fallback: Color = Color(0xFF007BFF)): Color {
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
fun UiCustomizerModal(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val config = uiState.uiCustomizationConfig

  var activeTab by remember { mutableStateOf(0) } // 0: ترتيب وإخفاء الحقول, 1: ترتيب وسحب الأزرار, 2: الحجم والتخطيط
  var buttonToEdit by remember { mutableStateOf<AppActionButton?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }
  var showResetConfirm by remember { mutableStateOf(false) }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.96f)
          .fillMaxHeight(0.94f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Top Header
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF2C3E50))
              .padding(horizontal = 16.dp, vertical = 12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "🎛️ تخصيص وترتيب واجهة التطبيق",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "سحب وإفلات العناصر لإعادة ترتيبها وإخفاء أو إظهار الحقول والأزرار",
                  fontSize = 12.sp,
                  color = Color(0xFFBDC3C7)
                )
              }
              IconButton(
                onClick = onDismiss,
                modifier = Modifier
                  .size(36.dp)
                  .background(Color.White.copy(alpha = 0.2f), CircleShape)
              ) {
                Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }
            }
          }

          // 3 Tabs: 1. ترتيب وإخفاء الحقول | 2. ترتيب وسحب الأزرار | 3. الحجم والتخطيط
          TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.White,
            contentColor = Color(0xFF007BFF)
          ) {
            Tab(
              selected = activeTab == 0,
              onClick = { activeTab = 0 },
              text = {
                Text(
                  "📋 ترتيب وإخفاء الحقول (${config.formFields.size})",
                  fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 13.sp
                )
              }
            )
            Tab(
              selected = activeTab == 1,
              onClick = { activeTab = 1 },
              text = {
                Text(
                  "🔄 ترتيب وسحب الأزرار (${config.buttons.size})",
                  fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 13.sp
                )
              }
            )
            Tab(
              selected = activeTab == 2,
              onClick = { activeTab = 2 },
              text = {
                Text(
                  "📐 الحجم والتخطيط",
                  fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 13.sp
                )
              }
            )
          }

          // Content based on tab
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            when (activeTab) {
              0 -> {
                // TAB 0: ترتيب وسحب وإخفاء حقول الفاتورة
                FormFieldsReorderSection(
                  fields = config.formFields,
                  onReorder = { from, to -> viewModel.reorderFormFields(from, to) },
                  onMoveUp = { viewModel.moveFormFieldUp(it) },
                  onMoveDown = { viewModel.moveFormFieldDown(it) },
                  onToggleVisibility = { viewModel.toggleFormFieldVisibility(it) },
                  onResetFields = { viewModel.resetFormFieldsToDefault() }
                )
              }
              1 -> {
                // TAB 1: ترتيب وسحب الأزرار وإدارتها
                ButtonsReorderSection(
                  buttons = config.buttons,
                  onReorder = { from, to -> viewModel.reorderButtons(from, to) },
                  onMoveUp = { viewModel.moveButtonUp(it) },
                  onMoveDown = { viewModel.moveButtonDown(it) },
                  onToggleVisibility = { viewModel.toggleButtonVisibility(it) },
                  onToggleShortcut = { viewModel.toggleButtonQuickShortcut(it) },
                  onEdit = { buttonToEdit = it },
                  onDelete = { viewModel.deleteCustomButton(it) },
                  onAddNew = { showAddDialog = true }
                )
              }
              else -> {
                // TAB 2: الحجم والتخطيط والشريط السريع
                SizeAndLayoutSection(
                  config = config,
                  onSizeSelected = { viewModel.updateButtonSize(it) },
                  onLayoutSelected = { viewModel.updateButtonLayout(it) },
                  onToggleShortcuts = { viewModel.toggleQuickShortcutsBar(it) }
                )
              }
            }
          }

          // Footer Actions
          Surface(
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
              ) {
                Text(
                  "🔄 استعادة الافتراضي",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              }

              Button(
                onClick = {
                  viewModel.showToast("✅ تم حفظ تفضيلات الواجهة بنجاح!")
                  onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
              ) {
                Text(
                  "💾 حفظ التفضيلات",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }

    // Add New Button Dialog
    if (showAddDialog) {
      ButtonEditDialog(
        initialButton = null,
        onDismiss = { showAddDialog = false },
        onSave = { newBtn ->
          viewModel.addCustomButton(newBtn)
          showAddDialog = false
        }
      )
    }

    // Edit Existing Button Dialog
    buttonToEdit?.let { btn ->
      ButtonEditDialog(
        initialButton = btn,
        onDismiss = { buttonToEdit = null },
        onSave = { updatedBtn ->
          viewModel.updateButton(updatedBtn)
          buttonToEdit = null
        }
      )
    }

    // Reset Confirm Dialog
    if (showResetConfirm) {
      AlertDialog(
        onDismissRequest = { showResetConfirm = false },
        title = { Text("استعادة الترتيب الافتراضي", fontWeight = FontWeight.Bold) },
        text = { Text("هل أنت متأكد من رغبتك في استعادة الترتيب الافتراضي لحقول الفاتورة وأزرار العمليات؟") },
        confirmButton = {
          Button(
            onClick = {
              viewModel.resetUiCustomizationToDefault()
              showResetConfirm = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
          ) {
            Text("نعم، استعادة الافتراضي", color = Color.White)
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showResetConfirm = false }) {
            Text("إلغاء")
          }
        }
      )
    }
  }
}

@Composable
private fun FormFieldsReorderSection(
  fields: List<FormCustomField>,
  onReorder: (Int, Int) -> Unit,
  onMoveUp: (String) -> Unit,
  onMoveDown: (String) -> Unit,
  onToggleVisibility: (String) -> Unit,
  onResetFields: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // List of Fields
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      itemsIndexed(fields, key = { _, f -> f.id }) { index, field ->
        val isFirst = index == 0
        val isLast = index == fields.lastIndex
        var isDragging by remember { mutableStateOf(false) }
        var dragAccumulator by remember { mutableStateOf(0f) }

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDragging) Color(0xFFFFF9E6) else if (field.isVisible) Color.White else Color(0xFFF1F3F5)
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else if (field.isVisible) 2.dp else 0.dp),
          border = androidx.compose.foundation.BorderStroke(
            width = if (isDragging) 2.dp else 1.dp,
            color = if (isDragging) Color(0xFFF1BA63) else if (field.isVisible) Color(0xFFE2E8F0) else Color(0xFFCED4DA)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Drag Handle
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDragging) Color(0xFFFFEEBA) else Color(0xFFF8F9FA))
                .border(1.dp, if (isDragging) Color(0xFFF1BA63) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .pointerInput(field.id, index, fields.size) {
                  detectDragGestures(
                    onDragStart = {
                      isDragging = true
                      dragAccumulator = 0f
                    },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      dragAccumulator += dragAmount.y
                      val threshold = 42.dp.toPx()
                      if (dragAccumulator > threshold) {
                        if (index < fields.size - 1) {
                          onReorder(index, index + 1)
                          dragAccumulator -= threshold
                        }
                      } else if (dragAccumulator < -threshold) {
                        if (index > 0) {
                          onReorder(index, index - 1)
                          dragAccumulator += threshold
                        }
                      }
                    },
                    onDragEnd = {
                      isDragging = false
                      dragAccumulator = 0f
                    },
                    onDragCancel = {
                      isDragging = false
                      dragAccumulator = 0f
                    }
                  )
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "⠿",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDragging) Color(0xFFD39E00) else Color(0xFF6C757D)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Order Number badge
            Box(
              modifier = Modifier
                .size(26.dp)
                .background(if (field.isVisible) Color(0xFF5E258D) else Color(0xFF6C757D), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${index + 1}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Emoji icon & Title
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = field.iconEmoji, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                Text(
                  text = field.title,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (field.isVisible) Color(0xFF212529) else Color(0xFF6C757D)
                )
              }
              if (field.isRequired) {
                Text(
                  text = "🔒 حقل أساسي إلزامي للفاتورة",
                  fontSize = 10.5.sp,
                  color = Color(0xFF856404),
                  fontWeight = FontWeight.Bold
                )
              } else if (!field.isVisible) {
                Text(
                  text = "🚫 مخفي من الواجهة",
                  fontSize = 10.5.sp,
                  color = Color(0xFFDC3545),
                  fontWeight = FontWeight.Bold
                )
              } else {
                Text(
                  text = "👁️ ظاهر في الواجهة",
                  fontSize = 10.5.sp,
                  color = Color(0xFF28A745),
                  fontWeight = FontWeight.Normal
                )
              }
            }

            // Move Up Button
            IconButton(
              onClick = { onMoveUp(field.id) },
              enabled = !isFirst,
              modifier = Modifier.size(32.dp)
            ) {
              Text("⬆️", fontSize = 14.sp)
            }

            // Move Down Button
            IconButton(
              onClick = { onMoveDown(field.id) },
              enabled = !isLast,
              modifier = Modifier.size(32.dp)
            ) {
              Text("⬇️", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Visibility Switch or "Required" Badge
            if (field.isRequired) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFFF3CD),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEEBA)),
                modifier = Modifier.padding(horizontal = 4.dp)
              ) {
                Text(
                  text = "إلزامي",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF856404),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
              }
            } else {
              Switch(
                checked = field.isVisible,
                onCheckedChange = { onToggleVisibility(field.id) },
                modifier = Modifier.height(28.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ButtonsReorderSection(
  buttons: List<AppActionButton>,
  onReorder: (Int, Int) -> Unit,
  onMoveUp: (String) -> Unit,
  onMoveDown: (String) -> Unit,
  onToggleVisibility: (String) -> Unit,
  onToggleShortcut: (String) -> Unit,
  onEdit: (AppActionButton) -> Unit,
  onDelete: (String) -> Unit,
  onAddNew: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Top Info Bar & Add Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "اسحب المقبض (⠿) أو استخدم ⬆️ و ⬇️ للترتيب، و 👁️ للإخفاء/الإظهار:",
        fontSize = 12.sp,
        color = Color(0xFF495057),
        modifier = Modifier.weight(1f)
      )

      Button(
        onClick = onAddNew,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text("➕ إضافة زر جديد", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }
    }

    // List of Buttons
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      itemsIndexed(buttons, key = { _, b -> b.id }) { index, button ->
        val isFirst = index == 0
        val isLast = index == buttons.lastIndex
        val btnColor = parseColorSafe(button.colorHex)
        var isDragging by remember { mutableStateOf(false) }
        var dragAccumulator by remember { mutableStateOf(0f) }

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDragging) Color(0xFFFFF9E6) else if (button.isVisible) Color.White else Color(0xFFF1F3F5)
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else if (button.isVisible) 2.dp else 0.dp),
          border = if (isDragging) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF1BA63))
                   else if (!button.isVisible) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCED4DA))
                   else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Drag Handle
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDragging) Color(0xFFFFEEBA) else Color(0xFFF8F9FA))
                .border(1.dp, if (isDragging) Color(0xFFF1BA63) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .pointerInput(button.id, index, buttons.size) {
                  detectDragGestures(
                    onDragStart = {
                      isDragging = true
                      dragAccumulator = 0f
                    },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      dragAccumulator += dragAmount.y
                      val threshold = 42.dp.toPx()
                      if (dragAccumulator > threshold) {
                        if (index < buttons.size - 1) {
                          onReorder(index, index + 1)
                          dragAccumulator -= threshold
                        }
                      } else if (dragAccumulator < -threshold) {
                        if (index > 0) {
                          onReorder(index, index - 1)
                          dragAccumulator += threshold
                        }
                      }
                    },
                    onDragEnd = {
                      isDragging = false
                      dragAccumulator = 0f
                    },
                    onDragCancel = {
                      isDragging = false
                      dragAccumulator = 0f
                    }
                  )
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "⠿",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDragging) Color(0xFFD39E00) else Color(0xFF6C757D)
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Index Number badge
            Box(
              modifier = Modifier
                .size(24.dp)
                .background(Color(0xFFE9ECEF), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${index + 1}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF495057)
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Color Indicator Circle
            Box(
              modifier = Modifier
                .size(16.dp)
                .background(btnColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Button Icon & Label
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = button.iconEmoji,
                  fontSize = 16.sp,
                  modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                  text = button.label,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (button.isVisible) Color(0xFF212529) else Color(0xFF6C757D)
                )
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (!button.isVisible) {
                  Text(
                    text = "🔒 مخفي",
                    fontSize = 10.5.sp,
                    color = Color(0xFFDC3545),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 6.dp)
                  )
                }
                if (button.isQuickShortcut) {
                  Text(
                    text = "⭐ في شريط الاختصارات",
                    fontSize = 10.5.sp,
                    color = Color(0xFFD39E00),
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Move Up Button
            IconButton(
              onClick = { onMoveUp(button.id) },
              enabled = !isFirst,
              modifier = Modifier.size(30.dp)
            ) {
              Text("⬆️", fontSize = 13.sp)
            }

            // Move Down Button
            IconButton(
              onClick = { onMoveDown(button.id) },
              enabled = !isLast,
              modifier = Modifier.size(30.dp)
            ) {
              Text("⬇️", fontSize = 13.sp)
            }

            // Quick Shortcut Star Toggle
            IconButton(
              onClick = { onToggleShortcut(button.id) },
              modifier = Modifier.size(30.dp)
            ) {
              Text(
                text = if (button.isQuickShortcut) "⭐" else "☆",
                fontSize = 15.sp
              )
            }

            // Visibility Toggle
            IconButton(
              onClick = { onToggleVisibility(button.id) },
              modifier = Modifier.size(30.dp)
            ) {
              Text(
                text = if (button.isVisible) "👁️" else "🚫",
                fontSize = 13.sp
              )
            }

            // Edit Button
            IconButton(
              onClick = { onEdit(button) },
              modifier = Modifier.size(30.dp)
            ) {
              Text("✏️", fontSize = 12.sp)
            }

            // Delete (only for custom added buttons, id starts with CUSTOM_ or USER_)
            if (button.id.startsWith("CUSTOM_") || button.id.startsWith("USER_")) {
              IconButton(
                onClick = { onDelete(button.id) },
                modifier = Modifier.size(30.dp)
              ) {
                Text("🗑️", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SizeAndLayoutSection(
  config: com.example.data.UiCustomizationConfig,
  onSizeSelected: (ButtonSize) -> Unit,
  onLayoutSelected: (ButtonLayout) -> Unit,
  onToggleShortcuts: (Boolean) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "📏 حجم الأزرار (تصغير وتكبير)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF2C3E50)
          )
          Text(
            text = "اختر الحجم المناسب لتوفير مساحة الشاشة أو تكبير الأزرار لسهولة النقر:",
            fontSize = 12.sp,
            color = Color(0xFF6C757D),
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          ButtonSize.values().forEach { size ->
            val isSelected = config.buttonSize == size
            Surface(
              onClick = { onSizeSelected(size) },
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFFE8F4FD) else Color(0xFFF8F9FA),
              border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF007BFF)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(
                    selected = isSelected,
                    onClick = { onSizeSelected(size) }
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = size.label,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 14.sp,
                      color = if (isSelected) Color(0xFF007BFF) else Color(0xFF212529)
                    )
                    Text(
                      text = "الارتفاع: ${size.heightDp}dp - الخط: ${size.fontSizeSp}sp",
                      fontSize = 11.sp,
                      color = Color(0xFF6C757D)
                    )
                  }
                }

                // Small preview button
                Button(
                  onClick = {},
                  enabled = false,
                  shape = RoundedCornerShape(6.dp),
                  colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Color(0xFF007BFF),
                    disabledContentColor = Color.White
                  ),
                  modifier = Modifier
                    .height(size.heightDp.dp)
                    .width(100.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                  Text("معاينة", fontSize = size.fontSizeSp.sp, color = Color.White)
                }
              }
            }
          }
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "📑 نمط تخطيط وتوزيع الأزرار",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF2C3E50)
          )
          Text(
            text = "توزيع الأزرار في عمود واحد أو عمودين متجاورين لتقليل طول الصفحة:",
            fontSize = 12.sp,
            color = Color(0xFF6C757D),
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          ButtonLayout.values().forEach { layout ->
            val isSelected = config.buttonLayout == layout
            Surface(
              onClick = { onLayoutSelected(layout) },
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFFE8F4FD) else Color(0xFFF8F9FA),
              border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF007BFF)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = { onLayoutSelected(layout) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = layout.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isSelected) Color(0xFF007BFF) else Color(0xFF212529)
                  )
                  Text(
                    text = if (layout == ButtonLayout.DOUBLE)
                      "⚡ يقلل التمرير بنسبة 50% ويضع كل زرين بجانب بعضهما"
                    else
                      "أزرار ممتدة بكامل العرض بالترتيب الكلاسيكي",
                    fontSize = 11.5.sp,
                    color = Color(0xFF6C757D)
                  )
                }
              }
            }
          }
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "⭐ شريط الاختصارات السريعة العلوي",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color(0xFF2C3E50)
            )
            Text(
              text = "عرض شريط أفقي سريع أعلى الشاشة للوصول الفوري للأزرار المميزة بنجمة ⭐.",
              fontSize = 12.sp,
              color = Color(0xFF6C757D),
              modifier = Modifier.padding(top = 2.dp)
            )
          }
          Switch(
            checked = config.showQuickShortcutsBar,
            onCheckedChange = onToggleShortcuts
          )
        }
      }
    }
  }
}

@Composable
private fun ButtonEditDialog(
  initialButton: AppActionButton?,
  onDismiss: () -> Unit,
  onSave: (AppActionButton) -> Unit
) {
  val isNew = initialButton == null
  var label by remember { mutableStateOf(initialButton?.label ?: "") }
  var selectedIcon by remember { mutableStateOf(initialButton?.iconEmoji ?: "➕") }
  var selectedColorHex by remember { mutableStateOf(initialButton?.colorHex ?: "#007BFF") }
  var actionType by remember { mutableStateOf(initialButton?.actionType ?: "PAYMENT_VOUCHER") }
  var isQuickShortcut by remember { mutableStateOf(initialButton?.isQuickShortcut ?: false) }
  var customParam by remember { mutableStateOf(initialButton?.customParam ?: "") }
  var customDesc by remember { mutableStateOf(initialButton?.customDesc ?: "") }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .wrapContentHeight(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text(
          text = if (isNew) "➕ إضافة زر جديد / اختصار" else "✏️ تعديل الزر: ${initialButton?.label}",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Label input
        OutlinedTextField(
          value = label,
          onValueChange = { label = it },
          label = { Text("اسم الزر / التسمية") },
          placeholder = { Text("مثال: باقة شهرية أو كشف حساب") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Icon picker
        Text("اختر أيقونة الزر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF495057))
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(PRESET_ICONS) { icon ->
            val isSelected = selectedIcon == icon
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Color(0xFFE8F4FD) else Color(0xFFF1F3F5))
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) Color(0xFF007BFF) else Color(0xFFCED4DA),
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable { selectedIcon = icon },
              contentAlignment = Alignment.Center
            ) {
              Text(icon, fontSize = 18.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Color picker
        Text("اختر لون الزر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF495057))
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(PRESET_COLORS) { (hex, name) ->
            val color = parseColorSafe(hex)
            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                  width = if (isSelected) 3.dp else 1.dp,
                  color = if (isSelected) Color(0xFF2C3E50) else Color.White,
                  shape = CircleShape
                )
                .clickable { selectedColorHex = hex },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Type selection
        Text("وظيفة وإجراء الزر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF495057))
        var actionDropdownExpanded by remember { mutableStateOf(false) }
        val currentActionLabel = ACTION_TYPE_OPTIONS.find { it.first == actionType }?.second ?: actionType

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { actionDropdownExpanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(currentActionLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212529))
              Text("▼", fontSize = 10.sp, color = Color(0xFF6C757D))
            }
          }

          DropdownMenu(
            expanded = actionDropdownExpanded,
            onDismissRequest = { actionDropdownExpanded = false }
          ) {
            ACTION_TYPE_OPTIONS.forEach { (typeKey, typeTitle) ->
              DropdownMenuItem(
                text = { Text(typeTitle, fontSize = 13.sp) },
                onClick = {
                  actionType = typeKey
                  actionDropdownExpanded = false
                }
              )
            }
          }
        }

        // Additional inputs for custom actions
        if (actionType == "CUSTOM_PACKAGE") {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = customParam,
            onValueChange = { customParam = it },
            label = { Text("سعر الباقة") },
            placeholder = { Text("مثال: 5000 أو 10") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = customDesc,
            onValueChange = { customDesc = it },
            label = { Text("وصف الباقة") },
            placeholder = { Text("مثال: باقة VIP 3 أشهر") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )
        } else if (actionType == "CUSTOM_STATEMENT") {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = customParam,
            onValueChange = { customParam = it },
            label = { Text("رقم حساب العميل") },
            placeholder = { Text("مثال: 101") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )
        } else if (actionType == "CUSTOM_PHONE") {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = customParam,
            onValueChange = { customParam = it },
            label = { Text("رقم الهاتف للاتصال") },
            placeholder = { Text("مثال: 777000000") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )
        } else if (actionType == "CUSTOM_URL") {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = customParam,
            onValueChange = { customParam = it },
            label = { Text("رابط الموقع الإلكتروني") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Shortcut Star checkbox
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isQuickShortcut = !isQuickShortcut }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = isQuickShortcut,
            onCheckedChange = { isQuickShortcut = it }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "⭐ إضافة هذا الزر إلى شريط الاختصارات السريعة العلوي",
            fontSize = 12.5.sp,
            color = Color(0xFF2C3E50)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dialog Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("إلغاء")
          }

          Button(
            onClick = {
              if (label.isNotBlank()) {
                val buttonId = initialButton?.id ?: "CUSTOM_${System.currentTimeMillis()}"
                onSave(
                  AppActionButton(
                    id = buttonId,
                    label = label.trim(),
                    iconEmoji = selectedIcon,
                    colorHex = selectedColorHex,
                    actionType = actionType,
                    isVisible = initialButton?.isVisible ?: true,
                    isQuickShortcut = isQuickShortcut,
                    customParam = customParam.trim(),
                    customDesc = customDesc.trim()
                  )
                )
              }
            },
            enabled = label.isNotBlank(),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
          ) {
            Text(if (isNew) "إضافة" else "حفظ التعديل", color = Color.White)
          }
        }
      }
    }
  }
}
