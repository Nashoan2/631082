package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.data.CustomerReminder
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ArabicNumberHelper
import com.example.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TabCustomerReminders(viewModel: InvoiceViewModel, onDismiss: () -> Unit = {}) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var reminderToEdit by remember { mutableStateOf<CustomerReminder?>(null) }
  var reminderToDelete by remember { mutableStateOf<CustomerReminder?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var filterMode by remember { mutableStateOf(0) } // 0: All, 1: Due/Overdue, 2: Upcoming, 3: Completed

  val now = System.currentTimeMillis()
  val reminders = uiState.customerReminders

  val overdueCount = remember(reminders) {
    reminders.count { !it.isCompleted && it.dueTimestamp <= now }
  }
  val upcomingCount = remember(reminders) {
    reminders.count { !it.isCompleted && it.dueTimestamp > now }
  }
  val completedCount = remember(reminders) {
    reminders.count { it.isCompleted }
  }

  val filteredReminders = remember(reminders, filterMode, searchQuery) {
    var list = when (filterMode) {
      1 -> reminders.filter { !it.isCompleted && it.dueTimestamp <= now }
      2 -> reminders.filter { !it.isCompleted && it.dueTimestamp > now }
      3 -> reminders.filter { it.isCompleted }
      else -> reminders
    }

    if (searchQuery.isNotBlank()) {
      val q = searchQuery.trim().lowercase()
      list = list.filter { r ->
        r.customerName.lowercase().contains(q) ||
        r.customerAccount.contains(q) ||
        r.customerPhone.contains(q) ||
        r.title.lowercase().contains(q) ||
        r.note.lowercase().contains(q)
      }
    }

    // Sort: Due/overdue first, then upcoming by due timestamp ascending, completed last
    list.sortedWith(
      compareBy<CustomerReminder> { it.isCompleted }
        .thenBy { if (!it.isCompleted && it.dueTimestamp <= now) 0 else 1 }
        .thenBy { it.dueTimestamp }
    )
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. بطاقة عنوان التنبيهات وإحصائيات المواعيد
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(1.2.dp, Color(0xFFFFD54F)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFD97706),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              "تنبيهات ومواعيد العملاء",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp,
              color = Color(0xFF78350F)
            )
            Text(
              "تذكير بمواعيد السداد والأقساط ومتابعة الحسابات",
              fontSize = 11.5.sp,
              color = Color(0xFFB45309),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Surface(
          color = if (overdueCount > 0) Color(0xFFDC2626) else Color(0xFFD97706),
          shape = RoundedCornerShape(14.dp),
          shadowElevation = 1.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = if (overdueCount > 0) "$overdueCount مستحق" else "${reminders.size} تنبيه",
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 12.5.sp
            )
          }
        }
      }
    }

    // 2. كروت إحصائيات سريعة
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text("🔴 مستحقة الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
          Text(
            "$overdueCount",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFB71C1C)
          )
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        border = BorderStroke(1.dp, Color(0xFFFFF59D)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text("🟡 قادمة قريباً", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
          Text(
            "$upcomingCount",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFE65100)
          )
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text("🟢 مكتملة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
          Text(
            "$completedCount",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1B5E20)
          )
        }
      }
    }

    // 3. زر إضافة تنبيه جديد لعميل
    Button(
      onClick = { showAddDialog = true },
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          "إضافة تنبيه أو موعد جديد لعميل ⏰",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 14.5.sp
        )
      }
    }

    // 4. شرائح التصفية السريعة (الكل | المستحقة | القادمة | المكتملة)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      val filterOptions = listOf(
        0 to "الكل (${reminders.size})",
        1 to "المستحقة ($overdueCount)",
        2 to "القادمة ($upcomingCount)",
        3 to "المكتملة ($completedCount)"
      )
      filterOptions.forEach { (mode, label) ->
        val isSelected = filterMode == mode
        Surface(
          onClick = { filterMode = mode },
          color = if (isSelected) Color(0xFFD97706) else Color(0xFFFFFBEB),
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, if (isSelected) Color(0xFFB45309) else Color(0xFFFDE68A)),
          modifier = Modifier.weight(1f).height(34.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              label,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
              color = if (isSelected) Color.White else Color(0xFF92400E)
            )
          }
        }
      }
    }

    // 5. حقل البحث في التنبيهات
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("بحث بالاسم، رقم الحساب، أو موضوع التنبيه...", fontSize = 12.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFB45309)) },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(18.dp))
          }
        }
      },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFD97706),
        unfocusedBorderColor = Color(0xFFE5E7EB),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
      ),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
    )

    // 6. قائمة كروت التنبيهات
    if (filteredReminders.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("🔔", fontSize = 36.sp)
          Text(
            if (searchQuery.isNotEmpty()) "لا توجد تنبيهات مطابقة لبحثك" else "لا توجد تنبيهات مسجلة حالياً",
            fontWeight = FontWeight.Bold,
            fontSize = 14.5.sp,
            color = Color(0xFF6B7280)
          )
          Text(
            "يمكنك الضغط على زر (إضافة تنبيه جديد) بالأعلى لتحديد مهلة أو موعد سداد لأي عميل.",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      filteredReminders.forEach { reminder ->
        ReminderCard(
          reminder = reminder,
          storeName = uiState.storeConfig.storeNameAr.ifBlank { "المتجر" },
          onToggleComplete = { viewModel.toggleCustomerReminderCompleted(reminder.id) },
          onEdit = { reminderToEdit = reminder },
          onDelete = { reminderToDelete = reminder },
          onSendWhatsApp = {
            NotificationHelper.sendWhatsAppReminder(
              context,
              reminder,
              uiState.storeConfig.storeNameAr.ifBlank { "المتجر" }
            )
          }
        )
      }
    }
  }

  // Dialog to Add or Edit Reminder
  if (showAddDialog || reminderToEdit != null) {
    CustomerReminderFormDialog(
      customers = uiState.customers,
      initialReminder = reminderToEdit,
      onDismiss = {
        showAddDialog = false
        reminderToEdit = null
      },
      onSave = { savedReminder ->
        if (reminderToEdit != null) {
          viewModel.updateCustomerReminder(savedReminder)
        } else {
          viewModel.addCustomerReminder(savedReminder)
        }
        showAddDialog = false
        reminderToEdit = null
      }
    )
  }

  // Dialog to confirm delete
  reminderToDelete?.let { rem ->
    AlertDialog(
      onDismissRequest = { reminderToDelete = null },
      title = { Text("حذف التنبيه", fontWeight = FontWeight.Bold) },
      text = { Text("هل أنت متأكد من رغبتك في حذف تنبيه (${rem.title}) الخاص بالعميل (${rem.customerName})؟") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteCustomerReminder(rem.id)
            reminderToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("تأكيد الحذف")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { reminderToDelete = null }) {
          Text("إلغاء")
        }
      }
    )
  }
}

@Composable
fun ReminderCard(
  reminder: CustomerReminder,
  storeName: String,
  onToggleComplete: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onSendWhatsApp: () -> Unit
) {
  val now = System.currentTimeMillis()
  val isOverdue = !reminder.isCompleted && reminder.dueTimestamp <= now
  val isDueSoon = !reminder.isCompleted && !isOverdue && (reminder.dueTimestamp - now) <= 2 * 24 * 60 * 60 * 1000L

  val cardBg = when {
    reminder.isCompleted -> Color(0xFFF9FAFB)
    isOverdue -> Color(0xFFFFF1F2)
    isDueSoon -> Color(0xFFFFFBEB)
    else -> Color.White
  }

  val borderColor = when {
    reminder.isCompleted -> Color(0xFFE5E7EB)
    isOverdue -> Color(0xFFFECDD3)
    isDueSoon -> Color(0xFFFDE68A)
    else -> Color(0xFFE5E7EB)
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(1.2.dp, borderColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // السطر الأول: اسم العميل والشارة
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f)
        ) {
          Surface(
            shape = CircleShape,
            color = if (reminder.isCompleted) Color(0xFFE5E7EB) else if (isOverdue) Color(0xFFFFE4E6) else Color(0xFFFEF3C7),
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (reminder.isCompleted) Icons.Default.Check else Icons.Default.Person,
                contentDescription = null,
                tint = if (reminder.isCompleted) Color(0xFF10B981) else if (isOverdue) Color(0xFFE11D48) else Color(0xFFD97706),
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = reminder.customerName,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp,
              color = Color(0xFF1F2937),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (reminder.customerAccount.isNotBlank()) {
              Text(
                text = "حساب: ${reminder.customerAccount}",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
              )
            }
          }
        }

        // شارة الحالة
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when {
            reminder.isCompleted -> Color(0xFFD1FAE5)
            isOverdue -> Color(0xFFFFE4E6)
            isDueSoon -> Color(0xFFFEF3C7)
            else -> Color(0xFFE0F2FE)
          }
        ) {
          Text(
            text = when {
              reminder.isCompleted -> "✔️ تم السداد / منجز"
              isOverdue -> "🔴 مستحق اليوم / متأخر"
              isDueSoon -> "🟡 موعد قريب (${reminder.remainingDays} يوم)"
              else -> "🟢 متبقي ${reminder.remainingDays} يوم"
            },
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = when {
              reminder.isCompleted -> Color(0xFF047857)
              isOverdue -> Color(0xFFBE123C)
              isDueSoon -> Color(0xFFB45309)
              else -> Color(0xFF0369A1)
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      HorizontalDivider(color = borderColor, thickness = 0.8.dp)

      // السطر الثاني: موضوع التنبيه والمبلغ
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = reminder.title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = Color(0xFF111827)
          )
          if (reminder.note.isNotBlank()) {
            Text(
              text = reminder.note,
              fontSize = 11.5.sp,
              color = Color(0xFF4B5563)
            )
          }
        }

        if (reminder.amountDue > 0) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF3E8FF)
          ) {
            Text(
              text = "${ArabicNumberHelper.formatAmount(reminder.amountDue)} $",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF7E22CE),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      // السطر الثالث: تاريخ الاستحقاق والهاتف
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(15.dp))
          Text(
            text = "تاريخ الاستحقاق: ${reminder.dueDate}",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOverdue) Color(0xFFBE123C) else Color(0xFF4B5563)
          )
        }

        if (reminder.customerPhone.isNotBlank()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            Text(
              text = reminder.customerPhone,
              fontSize = 11.5.sp,
              color = Color(0xFF4B5563)
            )
          }
        }
      }

      // السطر الرابع: أزرار العمليات (واتساب - إنجاز - تعديل - حذف)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // زر إرسال واتساب / رسالة تذكير
        if (reminder.customerPhone.isNotBlank()) {
          Button(
            onClick = onSendWhatsApp,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            modifier = Modifier.weight(1.3f).height(36.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text("💬", fontSize = 12.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text("إرسال تذكير", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }

        // زر تم السداد / غير مكتمل
        Button(
          onClick = onToggleComplete,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (reminder.isCompleted) Color(0xFF6B7280) else Color(0xFF059669)
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
          modifier = Modifier.weight(1.1f).height(36.dp)
        ) {
          Text(
            if (reminder.isCompleted) "إعادة فتح" else "تم السداد ✔️",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        // زر تمديد / تعديل
        OutlinedButton(
          onClick = onEdit,
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
          modifier = Modifier.weight(0.9f).height(36.dp),
          border = BorderStroke(1.dp, Color(0xFFD97706))
        ) {
          Text("تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
        }

        // زر حذف
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
fun CustomerReminderFormDialog(
  customers: List<Customer>,
  initialReminder: CustomerReminder?,
  onDismiss: () -> Unit,
  onSave: (CustomerReminder) -> Unit
) {
  val context = LocalContext.current
  var selectedCustomer by remember {
    mutableStateOf(customers.find { it.accountNumber == initialReminder?.customerAccount } ?: customers.firstOrNull())
  }
  var customerSearchQuery by remember { mutableStateOf("") }
  var isCustomerDropdownExpanded by remember { mutableStateOf(false) }

  var title by remember { mutableStateOf(initialReminder?.title ?: "ميعاد تسديد قسط") }
  var amountStr by remember {
    mutableStateOf(
      if (initialReminder != null && initialReminder.amountDue > 0) initialReminder.amountDue.toString()
      else if (selectedCustomer != null && selectedCustomer!!.balance > 0) selectedCustomer!!.balance.toString()
      else ""
    )
  }
  var note by remember { mutableStateOf(initialReminder?.note ?: "") }
  var selectedPreset by remember { mutableStateOf(initialReminder?.periodPreset ?: "أسبوع") }

  // Calculation of Due Date based on Preset
  var dueCalendar by remember {
    mutableStateOf(
      Calendar.getInstance().apply {
        if (initialReminder != null && initialReminder.dueTimestamp > 0) {
          timeInMillis = initialReminder.dueTimestamp
        } else {
          add(Calendar.DAY_OF_YEAR, 7) // default 1 week
        }
      }
    )
  }

  fun updatePreset(preset: String) {
    selectedPreset = preset
    val cal = Calendar.getInstance()
    when (preset) {
      "يوم" -> cal.add(Calendar.DAY_OF_YEAR, 1)
      "يومين" -> cal.add(Calendar.DAY_OF_YEAR, 2)
      "5 أيام" -> cal.add(Calendar.DAY_OF_YEAR, 5)
      "أسبوع" -> cal.add(Calendar.DAY_OF_YEAR, 7)
      "10 أيام" -> cal.add(Calendar.DAY_OF_YEAR, 10)
      "شهر" -> cal.add(Calendar.DAY_OF_YEAR, 30)
    }
    dueCalendar = cal
  }

  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
  val displayDateFormat = SimpleDateFormat("yyyy-MM-dd (EEEE)", Locale.forLanguageTag("ar"))

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFD97706))
        Text(
          text = if (initialReminder != null) "تعديل تنبيه العميل" else "إضافة تنبيه / مهلة لعميل",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 1. اختيار العميل
        Text("العميل المستهدف:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { isCustomerDropdownExpanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFD97706))
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = selectedCustomer?.let { "${it.name} (${it.accountNumber})" } ?: "اختر العميل...",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text("▼", fontSize = 10.sp, color = Color(0xFFD97706))
            }
          }

          DropdownMenu(
            expanded = isCustomerDropdownExpanded,
            onDismissRequest = { isCustomerDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
          ) {
            OutlinedTextField(
              value = customerSearchQuery,
              onValueChange = { customerSearchQuery = it },
              placeholder = { Text("ابحث عن اسم أو رقم...", fontSize = 12.sp) },
              modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
              singleLine = true
            )
            val filteredList = customers.filter {
              customerSearchQuery.isBlank() ||
              it.name.contains(customerSearchQuery.trim()) ||
              it.accountNumber.contains(customerSearchQuery.trim())
            }.take(30)

            filteredList.forEach { cust ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("حساب: ${cust.accountNumber} | رصيد: ${ArabicNumberHelper.formatAmount(cust.balance)} $", fontSize = 11.sp, color = Color.Gray)
                  }
                },
                onClick = {
                  selectedCustomer = cust
                  if (cust.balance > 0 && amountStr.isBlank()) {
                    amountStr = cust.balance.toString()
                  }
                  isCustomerDropdownExpanded = false
                }
              )
            }
          }
        }

        // 2. موضوع التنبيه
        Text("موضوع / سبب التنبيه:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("مثال: ميعاد تسديد قسط، متابعة حساب...") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          singleLine = true
        )

        // شرائح سريعة لموضوع التنبيه
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("تسديد قسط", "دفعة حساب", "متابعة رصيد").forEach { quickTitle ->
            Surface(
              onClick = { title = quickTitle },
              shape = RoundedCornerShape(6.dp),
              color = if (title == quickTitle) Color(0xFFD97706) else Color(0xFFF3F4F6),
              modifier = Modifier.weight(1f).height(28.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  quickTitle,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (title == quickTitle) Color.White else Color(0xFF374151)
                )
              }
            }
          }
        }

        // 3. المبلغ المستحق (اختياري)
        Text("المبلغ المطلوب تسديده (اختياري):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = ArabicNumberHelper.toEngDigits(it) },
          placeholder = { Text("0.00 $") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true
        )

        // 4. تحديد المهلة أو المدة الزمنية
        Text("تحديد المهلة الزمنية للتنبيه:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
        val presets = listOf("يوم", "يومين", "5 أيام", "أسبوع", "10 أيام", "شهر")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            presets.take(3).forEach { p ->
              Surface(
                onClick = { updatePreset(p) },
                shape = RoundedCornerShape(6.dp),
                color = if (selectedPreset == p) Color(0xFFD97706) else Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, if (selectedPreset == p) Color(0xFFB45309) else Color(0xFFFDE68A)),
                modifier = Modifier.weight(1f).height(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    p,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedPreset == p) Color.White else Color(0xFF92400E)
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            presets.drop(3).forEach { p ->
              Surface(
                onClick = { updatePreset(p) },
                shape = RoundedCornerShape(6.dp),
                color = if (selectedPreset == p) Color(0xFFD97706) else Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, if (selectedPreset == p) Color(0xFFB45309) else Color(0xFFFDE68A)),
                modifier = Modifier.weight(1f).height(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    p,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedPreset == p) Color.White else Color(0xFF92400E)
                  )
                }
              }
            }
          }
        }

        // اختيار تاريخ مخصص
        OutlinedButton(
          onClick = {
            DatePickerDialog(
              context,
              { _, y, m, d ->
                val cal = Calendar.getInstance().apply {
                  set(y, m, d, 23, 59, 59)
                }
                dueCalendar = cal
                selectedPreset = "مخصص"
              },
              dueCalendar.get(Calendar.YEAR),
              dueCalendar.get(Calendar.MONTH),
              dueCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
          },
          modifier = Modifier.fillMaxWidth().height(38.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
            Text(
              "تاريخ الاستحقاق المحدد: ${dateFormat.format(dueCalendar.time)}",
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp,
              color = Color(0xFF78350F)
            )
          }
        }

        // 5. ملاحظات إضافية
        Text("ملاحظات إضافية:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1F2937))
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          placeholder = { Text("أي تفاصيل إضافية عن الاتفاق أو التنبيه...") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val customer = selectedCustomer ?: return@Button
          val amountVal = amountStr.toDoubleOrNull() ?: 0.0
          val rem = CustomerReminder(
            id = initialReminder?.id ?: java.util.UUID.randomUUID().toString(),
            customerAccount = customer.accountNumber,
            customerName = customer.name,
            customerPhone = customer.phone,
            title = title.ifBlank { "ميعاد تسديد قسط" },
            note = note,
            amountDue = amountVal,
            currency = "YER",
            createdAt = initialReminder?.createdAt ?: ArabicNumberHelper.formatDateTime(),
            dueDate = dateFormat.format(dueCalendar.time),
            dueTimestamp = dueCalendar.timeInMillis,
            periodPreset = selectedPreset,
            isCompleted = initialReminder?.isCompleted ?: false,
            completedAt = initialReminder?.completedAt ?: ""
          )
          onSave(rem)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
      ) {
        Text("حفظ التنبيه ✔️", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}
