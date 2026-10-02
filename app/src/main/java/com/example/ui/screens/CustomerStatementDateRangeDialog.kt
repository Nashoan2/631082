package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.standardAppTextFieldColors
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ArabicNumberHelper
import java.util.Calendar

@Composable
fun CustomerStatementDateRangeDialog(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit = { viewModel.dismissDateRangeDialog() }
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val customer = uiState.pendingStatementCustomer ?: uiState.statementCustomer ?: return

  val purpleBrand = Color(0xFF1A237E)
  val primaryBlue = Color(0xFF007BFF)

  // Helper to open Android DatePickerDialog
  fun showDatePicker(initialDateStr: String, onDateSelected: (String) -> Unit) {
    val cal = Calendar.getInstance()
    val parsed = ArabicNumberHelper.parseDate(initialDateStr)
    if (parsed != null) {
      cal.time = parsed
    }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val day = cal.get(Calendar.DAY_OF_MONTH)

    val picker = DatePickerDialog(
      context,
      { _, y, m, d ->
        val chosenCal = Calendar.getInstance().apply {
          set(Calendar.YEAR, y)
          set(Calendar.MONTH, m)
          set(Calendar.DAY_OF_MONTH, d)
        }
        onDateSelected(ArabicNumberHelper.formatDateOnly(chosenCal.time))
      },
      year,
      month,
      day
    )
    picker.show()
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Dialog Header with Title and Close Icon
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "📊 كشف حساب تفصيلي",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = purpleBrand
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "تحديد الفترة الزمنية للتقرير",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(34.dp)
                .background(Color(0xFFF1F3F5), RoundedCornerShape(17.dp))
            ) {
              Icon(
                Icons.Default.Close,
                contentDescription = "إلغاء",
                tint = Color(0xFF495057),
                modifier = Modifier.size(18.dp)
              )
            }
          }

          HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 1.dp,
            color = Color(0xFFE9ECEF)
          )

          // Customer Info Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7FC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DBF0))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "اسم العميل: ${customer.name}",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "رقم الحساب: ${customer.accountNumber} | العمليات: ${customer.transactions.size}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF666666)
                )
              }
              Text(
                text = "${ArabicNumberHelper.formatAmount(customer.balance)} $",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (customer.balance > 0) Color(0xFFD32F2F) else Color(0xFF28A745)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Quick Filter Buttons Row (هذا الشهر، الشهر الماضي، آخر 3 أشهر، آخر 6 أشهر، الكل)
          Text(
            text = "فترات سريعة جاهزة:",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF495057),
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(6.dp))

          val scrollQuick = rememberScrollState()
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(scrollQuick),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            QuickPeriodChip("هذا الشهر") { viewModel.setStatementQuickPeriod("this_month") }
            QuickPeriodChip("الشهر الماضي") { viewModel.setStatementQuickPeriod("last_month") }
            QuickPeriodChip("آخر 3 أشهر") { viewModel.setStatementQuickPeriod("3_months") }
            QuickPeriodChip("آخر 6 أشهر") { viewModel.setStatementQuickPeriod("6_months") }
            QuickPeriodChip("مخصص") { viewModel.setStatementQuickPeriod("custom") }
            QuickPeriodChip("جميع الحركات") { viewModel.setStatementQuickPeriod("all") }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Date input fields: من تاريخ / إلى تاريخ
          OutlinedTextField(
            value = uiState.statementStartDate,
            onValueChange = { viewModel.updateStatementStartDate(it) },
            label = { Text("من تاريخ (يوم/شهر/سنة)", fontWeight = FontWeight.Bold) },
            placeholder = { Text("01/01/2026", color = Color(0xFF6B7280)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
            trailingIcon = {
              IconButton(onClick = {
                showDatePicker(uiState.statementStartDate) { chosen ->
                  viewModel.updateStatementStartDate(chosen)
                }
              }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "اختر تاريخ البداية", tint = purpleBrand)
              }
            },
            colors = standardAppTextFieldColors(focusedBorderColor = purpleBrand)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = uiState.statementEndDate,
            onValueChange = { viewModel.updateStatementEndDate(it) },
            label = { Text("إلى تاريخ (يوم/شهر/سنة)", fontWeight = FontWeight.Bold) },
            placeholder = { Text("31/12/2026", color = Color(0xFF6B7280)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
            trailingIcon = {
              IconButton(onClick = {
                showDatePicker(uiState.statementEndDate) { chosen ->
                  viewModel.updateStatementEndDate(chosen)
                }
              }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "اختر تاريخ النهاية", tint = purpleBrand)
              }
            },
            colors = standardAppTextFieldColors(focusedBorderColor = purpleBrand)
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Action Buttons: عرض الكشف / إلغاء
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                viewModel.applyDateRangeAndOpenStatement()
              },
              colors = ButtonDefaults.buttonColors(containerColor = purpleBrand),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1.3f)
                .height(46.dp)
            ) {
              Text(
                text = "👁️ عرض الكشف",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            Button(
              onClick = onDismiss,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C757D)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
            ) {
              Text(
                text = "إلغاء",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun QuickPeriodChip(text: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .background(Color(0xFFF1F3F5), RoundedCornerShape(6.dp))
      .border(1.dp, Color(0xFFCED4DA), RoundedCornerShape(6.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF343A40),
      textAlign = TextAlign.Center
    )
  }
}
