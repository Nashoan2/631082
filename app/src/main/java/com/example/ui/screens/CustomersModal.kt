package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.mandatoryTextFieldColors
import com.example.ui.theme.optionalYellowTextFieldColors
import com.example.ui.theme.standardAppTextFieldColors
import com.example.ui.theme.LocalShadedFieldColor
import com.example.ui.theme.LocalShadedFieldBorder
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Customer
import com.example.data.ExchangeRates
import com.example.data.ReportCustomizationConfig
import com.example.data.StoreConfig
import com.example.data.VoucherItem
import androidx.compose.material3.LocalTextStyle
import com.example.ui.theme.getReportFontFamily
import com.example.ui.theme.parseHexColor
import com.example.ui.components.AlmamlakaLogoBadge
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ArabicNumberHelper
import com.example.util.PrintHelper
import java.util.Calendar

@Composable
fun CustomersModal(viewModel: InvoiceViewModel, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val dynamicShadedColor = LocalShadedFieldColor.current
  val dynamicShadedBorder = LocalShadedFieldBorder.current
  val tabs = listOf(
    "1. 💸 سند قبض",
    "2. 💰 سند صرف",
    "3. 📋 جميع العملاء",
    "4. ➕ عميل جديد",
    "5. 📊 كشف حساب",
    "6. 📅 كشف فترة",
    "7. ⚖️ أصحاب الأرصدة",
    "8. 🔔 التنبيهات"
  )
  var selectedTab by remember(uiState.customersModalInitialTab) {
    mutableStateOf(uiState.customersModalInitialTab)
  }

  LaunchedEffect(uiState.customersModalInitialTab) {
    selectedTab = uiState.customersModalInitialTab
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.96f)
          .fillMaxHeight(0.94f)
          .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3FA))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          if (selectedTab < 0) {
            // === شاشة إدارة العملاء الرئيسية مطابقة للصورة 11 تماماً ===
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF1A237E),
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      Icons.Default.People,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
                Text(
                  text = "إدارة العملاء والحسابات",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF1A237E)
                )
              }
              IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF1A237E))
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // 1. إضافة عميل جديد 👥 (Green)
              Button(
                onClick = { selectedTab = 3 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Text(
                  text = "إضافة عميل جديد 👥",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              // 2. سند قبض 💰 (Golden / Amber)
              Button(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD49B00)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Text(
                  text = "سند قبض 💰",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              // 3. سند صرف 💸 (Slate Blue)
              Button(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A86BA)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Text(
                  text = "سند صرف 💸",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              // 4. جميع العملاء 📋 (Vivid Blue)
              Button(
                onClick = { selectedTab = 2 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Text(
                  text = "جميع العملاء 📋",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              // 5. كشف حساب عميل 📊 (Slate Blue)
              Button(
                onClick = { selectedTab = 4 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A86BA)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Text(
                  text = "كشف حساب عميل 📊",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              // 6. كشف حساب عميل من فترة الى فترة 📊 (Dark Charcoal)
              Button(
                onClick = { selectedTab = 5 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF495057)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Text(
                  text = "كشف حساب عميل من فترة الى فترة 📊",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              // 7. العملاء الذين في حساباتهم ارصده فقط
              Button(
                onClick = { selectedTab = 6 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = "العملاء الذين في حساباتهم ارصده فقط",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "⚖️",
                    fontSize = 18.sp
                  )
                }
              }

              // 8. تنبيهات ومواعيد العملاء 🔔 (Amber/Orange)
              Button(
                onClick = { selectedTab = 7 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = "تنبيهات ومواعيد العملاء",
                      fontSize = 17.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                    Text("🔔", fontSize = 18.sp)
                  }
                  if (uiState.dueRemindersCount > 0) {
                    Surface(
                      color = Color(0xFFDC2626),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Text(
                        text = "${uiState.dueRemindersCount} مستحق",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                      )
                    }
                  }
                }
              }
            }
          } else {
            // === ترويسة إدارة العملاء بتصميم متناسق وأنيق ===
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // زر القائمة في اليمين (تحت اتجاه RTL) بتصميم كبسولة أنيقة ومتناسقة
              Surface(
                onClick = { selectedTab = -1 },
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFE8EAF6),
                border = BorderStroke(1.dp, Color(0xFFC5CAE9)),
                modifier = Modifier.height(36.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color(0xFF1A237E),
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = "القائمة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                  )
                }
              }

              // في الوسط: إدارة العملاء 👥
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "إدارة العملاء",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF1A237E)
                )
                Icon(
                  imageVector = Icons.Default.People,
                  contentDescription = null,
                  tint = Color(0xFF1A237E),
                  modifier = Modifier.size(24.dp)
                )
              }

              // في اليسار (تحت اتجاه RTL): زر الإغلاق ✕ بتصميم متناسق تماماً ومطابق للارتفاع
              Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFE8EAF6),
                border = BorderStroke(1.dp, Color(0xFFC5CAE9)),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = Color(0xFF1A237E),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // شريط التبويبات مطابق للصورة (سند قبض أولاً ثم سند صرف ثم جميع العملاء)
            ScrollableTabRow(
              selectedTabIndex = selectedTab.coerceIn(0, tabs.lastIndex),
              edgePadding = 0.dp,
              containerColor = Color.Transparent,
              divider = {},
              indicator = { tabPositions ->
                val safeTab = selectedTab.coerceIn(0, tabs.lastIndex)
                if (safeTab in tabPositions.indices) {
                  TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[safeTab]),
                    color = Color(0xFF1A237E),
                    height = 3.dp
                  )
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                  selected = isSelected,
                  onClick = { selectedTab = index },
                  text = {
                    Text(
                      text = title,
                      fontSize = 13.5.sp,
                      fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                      color = if (isSelected) Color(0xFF1A237E) else Color(0xFF1E293B),
                      maxLines = 1
                    )
                  }
                )
              }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 12.dp), color = Color(0xFFE9ECEF))

            // Scrollable Container for Active Content
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
            ) {
              when (selectedTab) {
                0 -> TabReceiptVoucher(viewModel)
                1 -> TabPaymentVoucher(viewModel)
                2 -> TabAllCustomers(viewModel, onDismiss)
                3 -> TabAddCustomer(viewModel)
                4 -> TabCustomerStatement(viewModel, onDismiss)
                5 -> TabCustomerStatementDateRange(viewModel, onDismiss)
                6 -> TabCustomersWithBalancesOnly(viewModel, onDismiss)
                7 -> TabCustomerReminders(viewModel, onDismiss)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TabAddCustomer(viewModel: InvoiceViewModel) {
  val uiState by viewModel.uiState.collectAsState()
  var customAccount by remember { mutableStateOf(viewModel.getNextCustomerAccount()) }
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var balanceStr by remember { mutableStateOf("") }
  var selectedCurrency by remember { mutableStateOf("YR") }
  var currencyMenuExpanded by remember { mutableStateOf(false) }
  var isFormVisible by remember { mutableStateOf(true) }
  var savedCustomerAccount by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    if (customAccount.isEmpty() && savedCustomerAccount.isEmpty()) {
      customAccount = viewModel.getNextCustomerAccount()
    }
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      "➕ إضافة عميل جديد",
      fontWeight = FontWeight.ExtraBold,
      fontSize = 16.sp,
      color = Color(0xFF1A237E)
    )

    if (!isFormVisible) {
      val savedCustomer = uiState.customers.find { it.accountNumber == savedCustomerAccount }
      if (savedCustomer != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
          border = BorderStroke(1.dp, Color(0xFFC5CAE9))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text("✅ تم حفظ بيانات العميل بنجاح:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 14.sp)
            Text("• رقم حساب العميل: ${savedCustomer.accountNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("• اسم العميل: ${savedCustomer.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (savedCustomer.phone.isNotBlank()) Text("• رقم الهاتف: ${savedCustomer.phone}", fontSize = 13.sp)
            if (savedCustomer.address.isNotBlank()) Text("• العنوان: ${savedCustomer.address}", fontSize = 13.sp)
            if (savedCustomer.balance != 0.0) Text("• الرصيد الافتتاحي: ${savedCustomer.balance} $selectedCurrency", fontSize = 13.sp)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = {
            if (savedCustomer != null) {
              customAccount = savedCustomer.accountNumber
              name = savedCustomer.name
              phone = savedCustomer.phone
              address = savedCustomer.address
              balanceStr = if (savedCustomer.balance != 0.0) savedCustomer.balance.toString() else ""
            }
            isFormVisible = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
        ) {
          Text("✏️ تعديل", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }

        Button(
          onClick = {
            customAccount = viewModel.getNextCustomerAccount()
            name = ""
            phone = ""
            address = ""
            balanceStr = ""
            selectedCurrency = "YR"
            savedCustomerAccount = ""
            isFormVisible = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
        ) {
          Text("➕ إضافة عميل", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }
      }
    } else {
      // الصف الأفقي المطلوب: رقم العميل مصغر | تحديد العملة (YR افتراضياً) | الرصيد الافتتاحي
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
      ) {
        // 1. رقم العميل (تم تصغيره قليلاً)
        Column(modifier = Modifier.weight(0.85f)) {
          Text("رقم العميل", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = Color(0xFF111827))
          Spacer(modifier = Modifier.height(2.dp))
          OutlinedTextField(
            value = customAccount,
            onValueChange = { customAccount = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            colors = mandatoryTextFieldColors(),
            singleLine = true
          )
        }

        // 2. تحديد العملة (الرمز يكفي وظهور YR افتراضياً)
        Column(modifier = Modifier.weight(0.80f)) {
          Text("العملة", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = Color(0xFF111827))
          Spacer(modifier = Modifier.height(2.dp))
          Box {
            OutlinedCard(
              modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { currencyMenuExpanded = true },
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, Color(0xFFC5CAE9)),
              colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF8FAFC))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = selectedCurrency,
                  fontWeight = FontWeight.Black,
                  fontSize = 15.sp,
                  color = Color(0xFF1A237E)
                )
                Text("▾", fontSize = 12.sp, color = Color.Gray)
              }
            }

            DropdownMenu(
              expanded = currencyMenuExpanded,
              onDismissRequest = { currencyMenuExpanded = false }
            ) {
              listOf("YR", "$", "SR").forEach { curr ->
                DropdownMenuItem(
                  text = {
                    val label = when (curr) {
                      "YR" -> "YR (ريال يمني)"
                      "$" -> "$ (دولار أمريكي)"
                      "SR" -> "SR (ريال سعودي)"
                      else -> curr
                    }
                    Text(
                      text = label,
                      fontWeight = if (curr == selectedCurrency) FontWeight.Black else FontWeight.Normal,
                      color = if (curr == selectedCurrency) Color(0xFF1A237E) else Color.Unspecified
                    )
                  },
                  onClick = {
                    selectedCurrency = curr
                    currencyMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        // 3. الرصيد الافتتاحي
        Column(modifier = Modifier.weight(1.15f)) {
          Text("الرصيد الافتتاحي", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = Color(0xFF111827))
          Spacer(modifier = Modifier.height(2.dp))
          OutlinedTextField(
            value = balanceStr,
            onValueChange = { balanceStr = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            placeholder = { Text("0.0", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            colors = mandatoryTextFieldColors(),
            singleLine = true
          )
        }
      }

      Text("اسم العميل", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color(0xFF111827))
      Spacer(modifier = Modifier.height(2.dp))
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
        colors = mandatoryTextFieldColors()
      )

      Text("رقم الهاتف", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color(0xFF111827))
      Spacer(modifier = Modifier.height(2.dp))
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
        colors = mandatoryTextFieldColors()
      )

      Text("العنوان", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color(0xFF111827))
      Spacer(modifier = Modifier.height(2.dp))
      OutlinedTextField(
        value = address,
        onValueChange = { address = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
        colors = mandatoryTextFieldColors()
      )

      Button(
        onClick = {
          if (savedCustomerAccount.isNotBlank() && uiState.customers.any { it.accountNumber == savedCustomerAccount } && customAccount == savedCustomerAccount) {
            val bal = balanceStr.toDoubleOrNull() ?: 0.0
            viewModel.editCustomer(savedCustomerAccount, name, phone, address, bal)
            isFormVisible = false
          } else {
            val ok = viewModel.addCustomer(name, phone, address, balanceStr.toDoubleOrNull() ?: 0.0, customAccount, selectedCurrency)
            if (ok) {
              val cleanCustom = ArabicNumberHelper.toEngDigits(customAccount).trim()
              savedCustomerAccount = cleanCustom.ifEmpty { viewModel.uiState.value.customers.lastOrNull()?.accountNumber ?: "" }
              isFormVisible = false
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Text("💾 حفظ العميل", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
      }
    }
  }
}

@Composable
fun VoucherCustomerPickerDialog(
  customers: List<Customer>,
  currency: String,
  onDismiss: () -> Unit,
  onSelectCustomer: (Customer) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val filtered = customers.filter {
    val q = searchQuery.trim()
    if (q.isEmpty()) true
    else it.name.contains(q, ignoreCase = true) || it.accountNumber.contains(q)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF1D4ED8))
          Text("دليل العملاء", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF111827))
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "إغلاق")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("بحث بالاسم أو برقم الحساب...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        )

        if (filtered.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("لا يوجد عميل مطابق للبحث", color = Color.Gray, fontSize = 13.sp)
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(filtered) { customer ->
              Card(
                onClick = {
                  onSelectCustomer(customer)
                  onDismiss()
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = customer.name,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = Color(0xFF111827)
                    )
                    Text(
                      text = "رقم الحساب: ${customer.accountNumber}",
                      fontSize = 12.sp,
                      color = Color(0xFF6B7280)
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    val bal = customer.balance
                    val balColor = if (bal > 0) Color(0xFFDC2626) else if (bal < 0) Color(0xFF16A34A) else Color(0xFF4B5563)
                    val statusText = if (bal > 0) "عليه" else if (bal < 0) "له" else "متزن"
                    Text(
                      text = "${ArabicNumberHelper.formatAmount(bal)} $currency",
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 13.sp,
                      color = balColor
                    )
                    Text(
                      text = statusText,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = balColor
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء", fontWeight = FontWeight.Bold)
      }
    }
  )
}

@Composable
fun TabPaymentVoucher(viewModel: InvoiceViewModel) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val dynamicShadedColor = LocalShadedFieldColor.current
  val dynamicShadedBorder = LocalShadedFieldBorder.current
  var acc by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var curr by remember { mutableStateOf("YER") }
  var currMenuExpanded by remember { mutableStateOf(false) }
  var note by remember { mutableStateOf("") }
  var voucherDate by remember { mutableStateOf(ArabicNumberHelper.formatDateTime()) }
  var customVoucherNum by remember(uiState.nextPaymentVoucherNum) { mutableStateOf(uiState.nextPaymentVoucherNum.toString()) }
  LaunchedEffect(uiState.nextPaymentVoucherNum) {
    customVoucherNum = uiState.nextPaymentVoucherNum.toString()
  }
  var isFormVisible by remember { mutableStateOf(true) }
  var showCustomerPickerDialog by remember { mutableStateOf(false) }

  // Voucher Action dialog states
  var editingVoucher by remember { mutableStateOf<VoucherItem?>(null) }
  var previewingVoucher by remember { mutableStateOf<VoucherItem?>(null) }
  var deletingVoucher by remember { mutableStateOf<VoucherItem?>(null) }

  val cleanAcc = ArabicNumberHelper.toEngDigits(acc).trim()
  val isAccEntered = cleanAcc.isNotEmpty()
  val matchedCustomer = if (isAccEntered) {
    uiState.customers.find {
      ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanAcc
    }
  } else null
  val isAccNotFound = isAccEntered && matchedCustomer == null
  val customerName = matchedCustomer?.name ?: ""

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (!isFormVisible) {
      Button(
        onClick = { isFormVisible = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Text("➕ إضافة سند صرف جديد", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
      }
    } else {
      // Row 1: رقم السند (يمين) | العملة (يسار) - مطابق للصورة تماماً
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // رقم السند
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "رقم السند",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 6.dp)
          )
          BasicTextField(
            value = customVoucherNum,
            onValueChange = { customVoucherNum = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
              fontSize = 17.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0F172A),
              textAlign = TextAlign.Start
            ),
            cursorBrush = SolidColor(Color(0xFF0F172A)),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .background(dynamicShadedColor, RoundedCornerShape(10.dp))
              .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
              .padding(horizontal = 14.dp),
            decorationBox = { innerTextField ->
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
              ) {
                innerTextField()
              }
            }
          )
        }

        // العملة (تعرض YER مع سهم القائمة في أقصى اليسار)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "العملة",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp)
          )
          Box(modifier = Modifier.fillMaxWidth()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(dynamicShadedColor, RoundedCornerShape(10.dp))
                .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
                .clickable { currMenuExpanded = true }
                .padding(horizontal = 12.dp)
            ) {
              Text(
                text = curr,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                modifier = Modifier.align(Alignment.Center)
              )
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "اختر العملة",
                tint = Color(0xFF1E293B),
                modifier = Modifier
                  .size(24.dp)
                  .align(Alignment.CenterEnd)
              )
            }
            DropdownMenu(
              expanded = currMenuExpanded,
              onDismissRequest = { currMenuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("YER (ريال يمني - YR)", fontWeight = FontWeight.Bold) },
                onClick = { curr = "YER"; currMenuExpanded = false }
              )
              DropdownMenuItem(
                text = { Text("USD ($)", fontWeight = FontWeight.Bold) },
                onClick = { curr = "$"; currMenuExpanded = false }
              )
              DropdownMenuItem(
                text = { Text("SAR (ريال سعودي - SR)", fontWeight = FontWeight.Bold) },
                onClick = { curr = "SAR"; currMenuExpanded = false }
              )
            }
          }
        }
      }

      // Row 2: رقم حساب العميل مع زر دليل العملاء
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "رقم حساب العميل",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0F172A)
        )
        Surface(
          onClick = { showCustomerPickerDialog = true },
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFEFF6FF),
          border = BorderStroke(1.dp, Color(0xFF93C5FD))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = Color(0xFF1D4ED8),
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "دليل العملاء",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1D4ED8)
            )
          }
        }
      }

      BasicTextField(
        value = acc,
        onValueChange = { acc = it },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
          fontSize = 16.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        ),
        cursorBrush = SolidColor(Color(0xFF0F172A)),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .background(dynamicShadedColor, RoundedCornerShape(10.dp))
          .border(
            1.dp,
            if (isAccNotFound) Color(0xFFEF4444) else dynamicShadedBorder,
            RoundedCornerShape(10.dp)
          )
          .padding(horizontal = 14.dp),
        decorationBox = { innerTextField ->
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
          ) {
            innerTextField()
          }
        }
      )

      // Row 3: بطاقة معلومات الحساب التلقائية - مطابقة للصورة
      if (isAccNotFound) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
          border = BorderStroke(1.5.dp, Color(0xFFE57373))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = Color(0xFFC62828),
              modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "⚠️ هذا الحساب غير موجود!",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color(0xFFC62828)
              )
              Text(
                text = "رقم الحساب ($acc) غير مسجل في دليل الحسابات. يرجى التأكد من كتابة الرقم الصحيح.",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFFB71C1C)
              )
            }
          }
        }
      } else if (matchedCustomer != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
          border = BorderStroke(1.dp, Color(0xFF81C784))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF2E7D32),
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "👤 اسم صاحب الحساب:",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF333333)
                )
              }
              Text(
                text = customerName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color(0xFF1B5E20)
              )
            }

            HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFA5D6A7))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "💰 الرصيد الحالي للعميل:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF333333)
              )
              val bal = matchedCustomer.balance
              val statusText = if (bal > 0) " (عليه دين)" else if (bal < 0) " (له دائن)" else " (متزن)"
              val balColor = if (bal > 0) Color(0xFFC62828) else if (bal < 0) Color(0xFF2E7D32) else Color(0xFF455A64)
              Text(
                text = "${ArabicNumberHelper.formatAmount(bal)} $curr$statusText",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = balColor
              )
            }
          }
        }
      }

      // Row 4: المبلغ
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "المبلغ",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0F172A),
          modifier = Modifier.padding(bottom = 6.dp)
        )
        BasicTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          textStyle = TextStyle(
            fontSize = 16.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
          ),
          cursorBrush = SolidColor(Color(0xFF0F172A)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(dynamicShadedColor, RoundedCornerShape(10.dp))
            .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
          decorationBox = { innerTextField ->
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.CenterStart
            ) {
              innerTextField()
            }
          }
        )
      }

      // Row 5: البيان
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "البيان",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0F172A),
          modifier = Modifier.padding(bottom = 6.dp)
        )
        BasicTextField(
          value = note,
          onValueChange = { note = it },
          singleLine = true,
          textStyle = TextStyle(
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          ),
          cursorBrush = SolidColor(Color(0xFF0F172A)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(dynamicShadedColor, RoundedCornerShape(10.dp))
            .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
          decorationBox = { innerTextField ->
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.CenterStart
            ) {
              innerTextField()
            }
          }
        )
      }

      // Row 6: زر حفظ سند الصرف - أخضر كامل العرض مع أيقونة الحفظ مطابقة للصورة
      Button(
        onClick = {
          if (acc.isBlank()) {
            viewModel.showToast("❌ يرجى إدخال رقم حساب العميل أولاً.")
            return@Button
          }
          if (matchedCustomer == null) {
            viewModel.showToast("⚠️ هذا الحساب ($acc) غير موجود في النظام! يرجى التأكد من رقم الحساب.")
            return@Button
          }
          val amt = amountStr.toDoubleOrNull() ?: 0.0
          if (amt <= 0.0) {
            viewModel.showToast("❌ يرجى إدخال مبلغ صحيح أكبر من الصفر.")
            return@Button
          }
          val ok = viewModel.addPaymentVoucher(
            account = acc,
            amount = amt,
            currency = curr,
            note = note,
            date = voucherDate,
            voucherNum = customVoucherNum
          )
          if (ok) {
            acc = ""
            amountStr = ""
            note = ""
            voucherDate = ArabicNumberHelper.formatDateTime()
            isFormVisible = false
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "حفظ سند الصرف",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = Color.White
          )
          Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider()

    // CRITICAL REQUIREMENT: إمكانية تعديل سندات الصرف بعد حفظها مع إمكانية تعديل جميع بيانات السند
    Text(
      "📋 سندات الصرف المحفوظة (اضغط على تعديل لتعديل أي سند):",
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      color = Color(0xFF1A237E)
    )

    val paymentVouchers = viewModel.getAllVouchers("صرف")
    if (paymentVouchers.isEmpty()) {
      Text(
        "لا توجد سندات صرف مسجلة حتى الآن.",
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 4.dp)
      )
    } else {
      paymentVouchers.forEach { v ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                "سند صرف رقم: (${v.voucherNum})",
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFC62828)
              )
              Text(
                "${ArabicNumberHelper.formatAmount(v.amount)} ${v.currency}",
                fontWeight = FontWeight.Black,
                color = Color(0xFFB71C1C)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("صاحب الحساب: ${v.customerName} (حساب: ${v.account})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("التاريخ: ${v.date}", fontSize = 12.sp, color = Color.Gray)
            Text("البيان: ${v.note}", fontSize = 12.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // معاينة
              Button(
                onClick = { previewingVoucher = v },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .height(34.dp)
                  .weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
              ) {
                Icon(Icons.Default.Visibility, contentDescription = "معاينة", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("معاينة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              // تعديل
              Button(
                onClick = { editingVoucher = v },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .height(34.dp)
                  .weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
              ) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              // حذف
              Button(
                onClick = { deletingVoucher = v },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .height(34.dp)
                  .weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
              ) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("حذف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }
    }
  }

  // Preview Payment Voucher Dialog
  previewingVoucher?.let { v ->
    VoucherPreviewDialog(
      voucher = v,
      storeConfig = uiState.storeConfig,
      reportConfig = uiState.reportCustomizationConfig,
      onDismiss = { previewingVoucher = null },
      onPrint = {
        PrintHelper.printVoucher(context, v, uiState.storeConfig, uiState.reportCustomizationConfig)
      },
      onExportPdf = {
        viewModel.exportVoucherToPdf(context, v)
      },
      onEdit = {
        val target = previewingVoucher
        previewingVoucher = null
        editingVoucher = target
      },
      onDelete = {
        val target = previewingVoucher
        previewingVoucher = null
        deletingVoucher = target
      }
    )
  }

  // Delete Payment Voucher Confirmation Dialog
  deletingVoucher?.let { v ->
    VoucherDeleteDialog(
      voucher = v,
      onConfirm = {
        viewModel.deleteVoucher(v.voucherNum, v.type, account = v.account)
        deletingVoucher = null
      },
      onDismiss = { deletingVoucher = null }
    )
  }

  // Edit Payment Voucher Dialog - Modifies ALL fields
  editingVoucher?.let { v ->
    var editVoucherNum by remember { mutableStateOf(v.voucherNum) }
    var editAccount by remember { mutableStateOf(v.account) }
    var editAmountStr by remember { mutableStateOf(v.amount.toString()) }
    var editCurr by remember { mutableStateOf(v.currency) }
    var editNote by remember { mutableStateOf(v.note) }
    var editDate by remember { mutableStateOf(v.date) }
    var editCurrExpanded by remember { mutableStateOf(false) }
    var showEditCustomerPicker by remember { mutableStateOf(false) }

    val cleanEditAcc = ArabicNumberHelper.toEngDigits(editAccount).trim()
    val matchedEditCust = if (cleanEditAcc.isNotEmpty()) {
      uiState.customers.find {
        ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanEditAcc
      }
    } else null
    val isEditAccInvalid = cleanEditAcc.isNotEmpty() && matchedEditCust == null
    val liveCustomerName = matchedEditCust?.name ?: viewModel.getCustomerNameForAccount(editAccount).ifEmpty { v.customerName }

    AlertDialog(
      onDismissRequest = { editingVoucher = null },
      title = {
        Text("✏️ تعديل سند صرف رقم (${editVoucherNum})", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E))
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // رقم السند (قابل للتعديل - بدون تظليل برتقالي)
          OutlinedTextField(
            value = editVoucherNum,
            onValueChange = { editVoucherNum = it },
            label = { Text("🔢 رقم السند (قابل للتعديل)", fontWeight = FontWeight.ExtraBold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            colors = standardAppTextFieldColors()
          )

          // رقم حساب العميل (مظلل بالبرتقالي الخفيف) مع زر دليل العملاء
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("رقم حساب العميل", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color(0xFF111827))
            Surface(
              onClick = { showEditCustomerPicker = true },
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFEFF6FF),
              border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.People,
                  contentDescription = null,
                  tint = Color(0xFF1D4ED8),
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "دليل العملاء",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1D4ED8)
                )
              }
            }
          }
          OutlinedTextField(
            value = editAccount,
            onValueChange = { editAccount = it },
            placeholder = { Text("أدخل رقم الحساب أو اختر من الدليل", color = Color(0xFF64748B)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            isError = isEditAccInvalid,
            supportingText = if (isEditAccInvalid) {
              {
                Text("⚠️ هذا الحساب غير موجود في النظام!", color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            } else null,
            colors = mandatoryTextFieldColors()
          )

          // اسم صاحب الحساب أو رسالة تنبيه
          if (isEditAccInvalid) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
              border = BorderStroke(1.dp, Color(0xFFE57373))
            ) {
              Text(
                "⚠️ تنبيه: رقم الحساب ($editAccount) غير مسجل في دليل الحسابات!",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828),
                fontSize = 12.5.sp
              )
            }
          } else {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
              border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
              Text(
                "اسم صاحب الحساب: $liveCustomerName",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                fontSize = 13.sp
              )
            }
          }

          // تاريخ السند (قابل للتعديل ومظلل بالبرتقالي الخفيف)
          OutlinedTextField(
            value = editDate,
            onValueChange = { editDate = it },
            label = { Text("📅 تاريخ السند (قابـل للتعديل)", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )

          // المبلغ (مظلل بالبرتقالي الخفيف)
          OutlinedTextField(
            value = editAmountStr,
            onValueChange = { editAmountStr = it },
            label = { Text("المبلغ", fontWeight = FontWeight.ExtraBold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )

          // العملة (مظلل بالبرتقالي الخفيف)
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = editCurr,
              onValueChange = {},
              readOnly = true,
              label = { Text("العملة", fontWeight = FontWeight.ExtraBold) },
              trailingIcon = {
                IconButton(onClick = { editCurrExpanded = true }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = "اختر العملة", tint = Color.Black)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .clickable { editCurrExpanded = true },
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
              colors = mandatoryTextFieldColors()
            )
            DropdownMenu(
              expanded = editCurrExpanded,
              onDismissRequest = { editCurrExpanded = false }
            ) {
              DropdownMenuItem(text = { Text("YER (ريال يمني - YR)", fontWeight = FontWeight.Bold) }, onClick = { editCurr = "YER"; editCurrExpanded = false })
              DropdownMenuItem(text = { Text("USD ($)", fontWeight = FontWeight.Bold) }, onClick = { editCurr = "$"; editCurrExpanded = false })
              DropdownMenuItem(text = { Text("SAR (ريال سعودي - SR)", fontWeight = FontWeight.Bold) }, onClick = { editCurr = "SAR"; editCurrExpanded = false })
            }
          }

          // البيان (مظلل بالبرتقالي الخفيف)
          OutlinedTextField(
            value = editNote,
            onValueChange = { editNote = it },
            label = { Text("البيان", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editAccount.isBlank()) {
              viewModel.showToast("❌ يرجى إدخال رقم الحساب.")
              return@Button
            }
            if (isEditAccInvalid || matchedEditCust == null) {
              viewModel.showToast("⚠️ هذا الحساب ($editAccount) غير موجود في النظام! لا يمكن التعديل لحساب غير مسجل.")
              return@Button
            }
            val amt = editAmountStr.toDoubleOrNull() ?: 0.0
            if (amt <= 0.0) {
              viewModel.showToast("❌ يرجى إدخال مبلغ صحيح أكبر من الصفر.")
              return@Button
            }
            viewModel.editVoucher(
              oldVoucherNum = v.voucherNum,
              type = "صرف",
              targetAccount = editAccount,
              newAmount = amt,
              newCurrency = editCurr,
              newNote = editNote,
              newDate = editDate,
              newVoucherNum = editVoucherNum,
              origAccount = v.account
            )
            editingVoucher = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
        ) {
          Text("💾 حفظ التعديلات", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        Button(onClick = { editingVoucher = null }) {
          Text("إلغاء")
        }
      }
    )

    if (showEditCustomerPicker) {
      VoucherCustomerPickerDialog(
        customers = uiState.customers,
        currency = editCurr,
        onDismiss = { showEditCustomerPicker = false },
        onSelectCustomer = { selectedCust ->
          editAccount = selectedCust.accountNumber
        }
      )
    }
  }

  if (showCustomerPickerDialog) {
    VoucherCustomerPickerDialog(
      customers = uiState.customers,
      currency = curr,
      onDismiss = { showCustomerPickerDialog = false },
      onSelectCustomer = { selectedCust ->
        acc = selectedCust.accountNumber
      }
    )
  }
}

@Composable
fun TabReceiptVoucher(viewModel: InvoiceViewModel) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val dynamicShadedColor = LocalShadedFieldColor.current
  val dynamicShadedBorder = LocalShadedFieldBorder.current
  var acc by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var curr by remember { mutableStateOf("YER") }
  var currMenuExpanded by remember { mutableStateOf(false) }
  var note by remember { mutableStateOf("") }
  var voucherDate by remember { mutableStateOf(ArabicNumberHelper.formatDateTime()) }
  var customVoucherNum by remember(uiState.nextReceiptVoucherNum) { mutableStateOf(uiState.nextReceiptVoucherNum.toString()) }
  LaunchedEffect(uiState.nextReceiptVoucherNum) {
    customVoucherNum = uiState.nextReceiptVoucherNum.toString()
  }
  var isFormVisible by remember { mutableStateOf(true) }
  var showCustomerPickerDialog by remember { mutableStateOf(false) }

  // Voucher action dialog states
  var editingVoucher by remember { mutableStateOf<VoucherItem?>(null) }
  var previewingVoucher by remember { mutableStateOf<VoucherItem?>(null) }
  var deletingVoucher by remember { mutableStateOf<VoucherItem?>(null) }

  val cleanAcc = ArabicNumberHelper.toEngDigits(acc).trim()
  val isAccEntered = cleanAcc.isNotEmpty()
  val matchedCustomer = if (isAccEntered) {
    uiState.customers.find {
      ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanAcc
    }
  } else null
  val isAccNotFound = isAccEntered && matchedCustomer == null
  val customerName = matchedCustomer?.name ?: ""

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (!isFormVisible) {
      Button(
        onClick = { isFormVisible = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Text("➕ إضافة سند قبض جديد", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
      }
    } else {
      // Row 1: رقم السند (يمين) | العملة (يسار) - مطابق للصورة تماماً
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // رقم السند
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "رقم السند",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 6.dp)
          )
          BasicTextField(
            value = customVoucherNum,
            onValueChange = { customVoucherNum = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
              fontSize = 17.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0F172A),
              textAlign = TextAlign.Start
            ),
            cursorBrush = SolidColor(Color(0xFF0F172A)),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .background(dynamicShadedColor, RoundedCornerShape(10.dp))
              .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
              .padding(horizontal = 14.dp),
            decorationBox = { innerTextField ->
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
              ) {
                innerTextField()
              }
            }
          )
        }

        // العملة (تعرض YER مع سهم القائمة في أقصى اليسار)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "العملة",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp)
          )
          Box(modifier = Modifier.fillMaxWidth()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(dynamicShadedColor, RoundedCornerShape(10.dp))
                .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
                .clickable { currMenuExpanded = true }
                .padding(horizontal = 12.dp)
            ) {
              Text(
                text = curr,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                modifier = Modifier.align(Alignment.Center)
              )
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "اختر العملة",
                tint = Color(0xFF1E293B),
                modifier = Modifier
                  .size(24.dp)
                  .align(Alignment.CenterEnd)
              )
            }
            DropdownMenu(
              expanded = currMenuExpanded,
              onDismissRequest = { currMenuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("YER (ريال يمني - YR)", fontWeight = FontWeight.Bold) },
                onClick = { curr = "YER"; currMenuExpanded = false }
              )
              DropdownMenuItem(
                text = { Text("USD ($)", fontWeight = FontWeight.Bold) },
                onClick = { curr = "$"; currMenuExpanded = false }
              )
              DropdownMenuItem(
                text = { Text("SAR (ريال سعودي - SR)", fontWeight = FontWeight.Bold) },
                onClick = { curr = "SAR"; currMenuExpanded = false }
              )
            }
          }
        }
      }

      // Row 2: رقم حساب العميل مع زر دليل العملاء
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "رقم حساب العميل",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0F172A)
        )
        Surface(
          onClick = { showCustomerPickerDialog = true },
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFEFF6FF),
          border = BorderStroke(1.dp, Color(0xFF93C5FD))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = Color(0xFF1D4ED8),
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "دليل العملاء",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1D4ED8)
            )
          }
        }
      }

      BasicTextField(
        value = acc,
        onValueChange = { acc = it },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
          fontSize = 16.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        ),
        cursorBrush = SolidColor(Color(0xFF0F172A)),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .background(dynamicShadedColor, RoundedCornerShape(10.dp))
          .border(
            1.dp,
            if (isAccNotFound) Color(0xFFEF4444) else dynamicShadedBorder,
            RoundedCornerShape(10.dp)
          )
          .padding(horizontal = 14.dp),
        decorationBox = { innerTextField ->
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
          ) {
            innerTextField()
          }
        }
      )

      // Row 3: بطاقة معلومات الحساب التلقائية - مطابقة للصورة
      if (isAccNotFound) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
          border = BorderStroke(1.5.dp, Color(0xFFE57373))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = Color(0xFFC62828),
              modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "⚠️ هذا الحساب غير موجود!",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color(0xFFC62828)
              )
              Text(
                text = "رقم الحساب ($acc) غير مسجل في دليل الحسابات. يرجى التأكد من كتابة الرقم الصحيح.",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFFB71C1C)
              )
            }
          }
        }
      } else if (matchedCustomer != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
          border = BorderStroke(1.dp, Color(0xFF81C784))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF2E7D32),
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "👤 اسم صاحب الحساب:",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF333333)
                )
              }
              Text(
                text = customerName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color(0xFF1B5E20)
              )
            }

            HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFA5D6A7))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "💰 الرصيد الحالي للعميل:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF333333)
              )
              val bal = matchedCustomer.balance
              val statusText = if (bal > 0) " (عليه دين سابق)" else if (bal < 0) " (له دائن)" else " (متزن)"
              val balColor = if (bal > 0) Color(0xFFC62828) else if (bal < 0) Color(0xFF2E7D32) else Color(0xFF455A64)
              Text(
                text = "${ArabicNumberHelper.formatAmount(bal)} $curr$statusText",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = balColor
              )
            }
          }
        }
      }

      // Row 4: المبلغ
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "المبلغ",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0F172A),
          modifier = Modifier.padding(bottom = 6.dp)
        )
        BasicTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          textStyle = TextStyle(
            fontSize = 16.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
          ),
          cursorBrush = SolidColor(Color(0xFF0F172A)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(dynamicShadedColor, RoundedCornerShape(10.dp))
            .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
          decorationBox = { innerTextField ->
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.CenterStart
            ) {
              innerTextField()
            }
          }
        )
      }

      // Row 5: البيان
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "البيان",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0F172A),
          modifier = Modifier.padding(bottom = 6.dp)
        )
        BasicTextField(
          value = note,
          onValueChange = { note = it },
          singleLine = true,
          textStyle = TextStyle(
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          ),
          cursorBrush = SolidColor(Color(0xFF0F172A)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(dynamicShadedColor, RoundedCornerShape(10.dp))
            .border(1.dp, dynamicShadedBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
          decorationBox = { innerTextField ->
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.CenterStart
            ) {
              innerTextField()
            }
          }
        )
      }

      // Row 6: زر حفظ سند القبض - أخضر كامل العرض مع أيقونة الحفظ مطابقة للصورة
      Button(
        onClick = {
          if (acc.isBlank()) {
            viewModel.showToast("❌ يرجى إدخال رقم حساب العميل أولاً.")
            return@Button
          }
          if (matchedCustomer == null) {
            viewModel.showToast("⚠️ هذا الحساب ($acc) غير موجود في النظام! يرجى التأكد من رقم الحساب.")
            return@Button
          }
          val amt = amountStr.toDoubleOrNull() ?: 0.0
          if (amt <= 0.0) {
            viewModel.showToast("❌ يرجى إدخال مبلغ صحيح أكبر من الصفر.")
            return@Button
          }
          val ok = viewModel.addReceiptVoucher(
            account = acc,
            amount = amt,
            currency = curr,
            note = note,
            date = voucherDate,
            voucherNum = customVoucherNum
          )
          if (ok) {
            acc = ""
            amountStr = ""
            note = ""
            voucherDate = ArabicNumberHelper.formatDateTime()
            isFormVisible = false
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "حفظ سند القبض",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = Color.White
          )
          Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider()

    // CRITICAL REQUIREMENT: إمكانية تعديل سندات القبض بعد حفظها مع إمكانية تعديل جميع بيانات السند
    Text(
      "📋 سندات القبض المحفوظة (اضغط على تعديل لتعديل أي سند):",
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      color = Color(0xFF1A237E)
    )

    val receiptVouchers = viewModel.getAllVouchers("قبض")
    if (receiptVouchers.isEmpty()) {
      Text(
        "لا توجد سندات قبض مسجلة حتى الآن.",
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 4.dp)
      )
    } else {
      receiptVouchers.forEach { v ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FFF8)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                "سند قبض رقم: (${v.voucherNum})",
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
              )
              Text(
                "${ArabicNumberHelper.formatAmount(v.amount)} ${v.currency}",
                fontWeight = FontWeight.Black,
                color = Color(0xFF1B5E20)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("صاحب الحساب: ${v.customerName} (حساب: ${v.account})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("التاريخ: ${v.date}", fontSize = 12.sp, color = Color.Gray)
            Text("البيان: ${v.note}", fontSize = 12.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // معاينة
              Button(
                onClick = { previewingVoucher = v },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .height(34.dp)
                  .weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
              ) {
                Icon(Icons.Default.Visibility, contentDescription = "معاينة", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("معاينة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              // تعديل
              Button(
                onClick = { editingVoucher = v },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .height(34.dp)
                  .weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
              ) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              // حذف
              Button(
                onClick = { deletingVoucher = v },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .height(34.dp)
                  .weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
              ) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("حذف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }
    }
  }

  // Preview Receipt Voucher Dialog
  previewingVoucher?.let { v ->
    VoucherPreviewDialog(
      voucher = v,
      storeConfig = uiState.storeConfig,
      reportConfig = uiState.reportCustomizationConfig,
      onDismiss = { previewingVoucher = null },
      onPrint = {
        PrintHelper.printVoucher(context, v, uiState.storeConfig, uiState.reportCustomizationConfig)
      },
      onExportPdf = {
        viewModel.exportVoucherToPdf(context, v)
      },
      onEdit = {
        val target = previewingVoucher
        previewingVoucher = null
        editingVoucher = target
      },
      onDelete = {
        val target = previewingVoucher
        previewingVoucher = null
        deletingVoucher = target
      }
    )
  }

  // Delete Receipt Voucher Confirmation Dialog
  deletingVoucher?.let { v ->
    VoucherDeleteDialog(
      voucher = v,
      onConfirm = {
        viewModel.deleteVoucher(v.voucherNum, v.type, account = v.account)
        deletingVoucher = null
      },
      onDismiss = { deletingVoucher = null }
    )
  }

  // Edit Receipt Voucher Dialog - Modifies ALL fields
  editingVoucher?.let { v ->
    var editVoucherNum by remember { mutableStateOf(v.voucherNum) }
    var editAccount by remember { mutableStateOf(v.account) }
    var editAmountStr by remember { mutableStateOf(v.amount.toString()) }
    var editCurr by remember { mutableStateOf(v.currency) }
    var editNote by remember { mutableStateOf(v.note) }
    var editDate by remember { mutableStateOf(v.date) }
    var editCurrExpanded by remember { mutableStateOf(false) }
    var showEditCustomerPicker by remember { mutableStateOf(false) }

    val cleanEditAcc = ArabicNumberHelper.toEngDigits(editAccount).trim()
    val matchedEditCust = if (cleanEditAcc.isNotEmpty()) {
      uiState.customers.find {
        ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanEditAcc
      }
    } else null
    val isEditAccInvalid = cleanEditAcc.isNotEmpty() && matchedEditCust == null
    val liveCustomerName = matchedEditCust?.name ?: viewModel.getCustomerNameForAccount(editAccount).ifEmpty { v.customerName }

    AlertDialog(
      onDismissRequest = { editingVoucher = null },
      title = {
        Text("✏️ تعديل سند قبض رقم (${editVoucherNum})", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E))
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // رقم السند (قابل للتعديل - بدون تظليل برتقالي)
          OutlinedTextField(
            value = editVoucherNum,
            onValueChange = { editVoucherNum = it },
            label = { Text("🔢 رقم السند (قابل للتعديل)", fontWeight = FontWeight.ExtraBold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            colors = standardAppTextFieldColors()
          )

          // رقم حساب العميل (مظلل بالبرتقالي الخفيف) مع زر دليل العملاء
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("رقم حساب العميل", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color(0xFF111827))
            Surface(
              onClick = { showEditCustomerPicker = true },
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFEFF6FF),
              border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.People,
                  contentDescription = null,
                  tint = Color(0xFF1D4ED8),
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "دليل العملاء",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1D4ED8)
                )
              }
            }
          }
          OutlinedTextField(
            value = editAccount,
            onValueChange = { editAccount = it },
            placeholder = { Text("أدخل رقم الحساب أو اختر من الدليل", color = Color(0xFF64748B)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            isError = isEditAccInvalid,
            supportingText = if (isEditAccInvalid) {
              {
                Text("⚠️ هذا الحساب غير موجود في النظام!", color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            } else null,
            colors = mandatoryTextFieldColors()
          )

          // اسم صاحب الحساب أو رسالة تنبيه
          if (isEditAccInvalid) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
              border = BorderStroke(1.dp, Color(0xFFE57373))
            ) {
              Text(
                "⚠️ تنبيه: رقم الحساب ($editAccount) غير مسجل في دليل الحسابات!",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828),
                fontSize = 12.5.sp
              )
            }
          } else {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
              border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
              Text(
                "اسم صاحب الحساب: $liveCustomerName",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                fontSize = 13.sp
              )
            }
          }

          // تاريخ السند (قابل للتعديل ومظلل بالبرتقالي الخفيف)
          OutlinedTextField(
            value = editDate,
            onValueChange = { editDate = it },
            label = { Text("📅 تاريخ السند (قابـل للتعديل)", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )

          // المبلغ (مظلل بالبرتقالي الخفيف)
          OutlinedTextField(
            value = editAmountStr,
            onValueChange = { editAmountStr = it },
            label = { Text("المبلغ", fontWeight = FontWeight.ExtraBold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )

          // العملة (مظلل بالبرتقالي الخفيف)
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = editCurr,
              onValueChange = {},
              readOnly = true,
              label = { Text("العملة", fontWeight = FontWeight.ExtraBold) },
              trailingIcon = {
                IconButton(onClick = { editCurrExpanded = true }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = "اختر العملة", tint = Color.Black)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .clickable { editCurrExpanded = true },
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black),
              colors = mandatoryTextFieldColors()
            )
            DropdownMenu(
              expanded = editCurrExpanded,
              onDismissRequest = { editCurrExpanded = false }
            ) {
              DropdownMenuItem(text = { Text("YER (ريال يمني - YR)", fontWeight = FontWeight.Bold) }, onClick = { editCurr = "YER"; editCurrExpanded = false })
              DropdownMenuItem(text = { Text("USD ($)", fontWeight = FontWeight.Bold) }, onClick = { editCurr = "$"; editCurrExpanded = false })
              DropdownMenuItem(text = { Text("SAR (ريال سعودي - SR)", fontWeight = FontWeight.Bold) }, onClick = { editCurr = "SAR"; editCurrExpanded = false })
            }
          }

          // البيان (مظلل بالبرتقالي الخفيف)
          OutlinedTextField(
            value = editNote,
            onValueChange = { editNote = it },
            label = { Text("البيان", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editAccount.isBlank()) {
              viewModel.showToast("❌ يرجى إدخال رقم الحساب.")
              return@Button
            }
            if (isEditAccInvalid || matchedEditCust == null) {
              viewModel.showToast("⚠️ هذا الحساب ($editAccount) غير موجود في النظام! لا يمكن التعديل لحساب غير مسجل.")
              return@Button
            }
            val amt = editAmountStr.toDoubleOrNull() ?: 0.0
            if (amt <= 0.0) {
              viewModel.showToast("❌ يرجى إدخال مبلغ صحيح أكبر من الصفر.")
              return@Button
            }
            viewModel.editVoucher(
              oldVoucherNum = v.voucherNum,
              type = "قبض",
              targetAccount = editAccount,
              newAmount = amt,
              newCurrency = editCurr,
              newNote = editNote,
              newDate = editDate,
              newVoucherNum = editVoucherNum,
              origAccount = v.account
            )
            editingVoucher = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
        ) {
          Text("💾 حفظ التعديلات", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        Button(onClick = { editingVoucher = null }) {
          Text("إلغاء")
        }
      }
    )

    if (showEditCustomerPicker) {
      VoucherCustomerPickerDialog(
        customers = uiState.customers,
        currency = editCurr,
        onDismiss = { showEditCustomerPicker = false },
        onSelectCustomer = { selectedCust ->
          editAccount = selectedCust.accountNumber
        }
      )
    }
  }

  if (showCustomerPickerDialog) {
    VoucherCustomerPickerDialog(
      customers = uiState.customers,
      currency = curr,
      onDismiss = { showCustomerPickerDialog = false },
      onSelectCustomer = { selectedCust ->
        acc = selectedCust.accountNumber
      }
    )
  }
}

@Composable
fun TabAllCustomers(viewModel: InvoiceViewModel, onDismiss: () -> Unit = {}) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var editingCustomer by remember { mutableStateOf<Customer?>(null) }
  var customerToDelete by remember { mutableStateOf<Customer?>(null) }
  var showPreviewWithBalance by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var isAscending by remember { mutableStateOf(false) }

  val sortedCustomers = remember(uiState.customers, isAscending) {
    if (isAscending) {
      uiState.customers.sortedWith(
        compareBy<Customer> {
          ArabicNumberHelper.toEngDigits(it.accountNumber).toLongOrNull() ?: Long.MAX_VALUE
        }.thenBy {
          ArabicNumberHelper.toEngDigits(it.accountNumber).trim()
        }.thenBy {
          it.name.trim()
        }
      )
    } else {
      uiState.customers.sortedWith(
        compareByDescending<Customer> {
          ArabicNumberHelper.toEngDigits(it.accountNumber).toLongOrNull() ?: Long.MIN_VALUE
        }.thenByDescending {
          ArabicNumberHelper.toEngDigits(it.accountNumber).trim()
        }.thenBy {
          it.name.trim()
        }
      )
    }
  }

  val filteredCustomers = remember(sortedCustomers, searchQuery) {
    if (searchQuery.isBlank()) {
      sortedCustomers
    } else {
      val q = searchQuery.trim().lowercase()
      sortedCustomers.filter { c ->
        c.name.lowercase().contains(q) ||
        c.accountNumber.contains(q) ||
        c.phone.contains(q) ||
        c.address.lowercase().contains(q)
      }
    }
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. شريط العنوان وعدد العملاء
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
      shape = RoundedCornerShape(10.dp),
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
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text("📋", fontSize = 18.sp)
          Text(
            "قائمة جميع العملاء والحسابات",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = Color(0xFF1A237E)
          )
        }
        Surface(
          color = Color(0xFF1A237E),
          shape = RoundedCornerShape(14.dp),
          shadowElevation = 1.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "${uiState.customers.size}",
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 13.sp
            )
            Text(
              text = "عميل",
              color = Color.White.copy(alpha = 0.9f),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // 2. أزرار العمليات العلوية (متناسقة العرض والارتفاع وبدون أي التفاف رأسي)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { showPreviewWithBalance = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "معاينة",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
          )
        }
      }

      Button(
        onClick = { PrintHelper.printAllCustomers(context, sortedCustomers, uiState.storeConfig) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17A2B8)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "طباعة الكشف",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
          )
        }
      }

      Button(
        onClick = { viewModel.exportAllCustomersToPdf(context) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "تصدير PDF",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
          )
        }
      }
    }

    // 3. شريط البحث السريع وزر الترتيب (تصاعدي / تنازلي)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("🔍 بحث بالاسم، رقم الحساب، أو الهاتف...", fontSize = 12.5.sp, color = Color(0xFF6B7280)) },
        modifier = Modifier.weight(1f),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
        colors = standardAppTextFieldColors(focusedBorderColor = Color(0xFF1A237E))
      )

      Surface(
        onClick = { isAscending = !isAscending },
        color = if (isAscending) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isAscending) Color(0xFF81C784) else Color(0xFFFFB74D)),
        modifier = Modifier.height(54.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = if (isAscending) "تصاعدي ⬆️" else "تنازلي (الأحدث أولاً) ⬇️",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isAscending) Color(0xFF2E7D32) else Color(0xFFE65100)
          )
        }
      }
    }

    // 4. بطاقات العملاء
    if (filteredCustomers.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text("👥", fontSize = 32.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            if (searchQuery.isNotBlank()) "لا توجد نتائج مطابقة لبحثك" else "لا يوجد عملاء مسجلين حالياً.",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    } else {
      filteredCustomers.forEach { c ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // الصف الأول: الاسم ورقم الحساب + شارة الرصيد
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
                    .size(36.dp)
                    .background(Color(0xFFEDE7F6), shape = CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text("👤", fontSize = 16.sp)
                }
                Column {
                  Text(
                    text = c.name,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212529),
                    fontSize = 15.sp
                  )
                  Text(
                    text = "رقم الحساب: (${c.accountNumber})",
                    fontSize = 12.sp,
                    color = Color(0xFF1A237E),
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              // شارة الرصيد
              Surface(
                color = if (c.balance > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (c.balance > 0) Color(0xFFFFCDD2) else Color(0xFFC8E6C9))
              ) {
                Text(
                  text = "الرصيد: ${ArabicNumberHelper.formatAmount(c.balance)} $",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 13.sp,
                  color = if (c.balance > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(6.dp))

            // الصف الثاني: الهاتف، العنوان، الحركات
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("📞 ${c.phone.ifEmpty { "غير محدد" }}", fontSize = 12.sp, color = Color(0xFF4B5563))
              Text("📍 ${c.address.ifEmpty { "غير محدد" }}", fontSize = 12.sp, color = Color(0xFF4B5563))
              Text("📊 الحركات: ${c.transactions.size}", fontSize = 12.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // الصف الثالث: أزرار العمليات الثلاثة (موزعة بنسب متناسقة weight وبدون أي تداخل أو قص!)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // 1. كشف الحساب
              Button(
                onClick = { viewModel.requestStatementDateRange(c) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1.3f)
                  .height(36.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Text("📊 كشف حساب", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
              }

              // 2. تعديل
              Button(
                onClick = { editingCustomer = c },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("تعديل", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
              }

              // 3. حذف
              Button(
                onClick = { customerToDelete = c },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("حذف", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
              }
            }
          }
        }
      }
    }
  }

  // Delete Customer Confirmation Dialog
  customerToDelete?.let { c ->
    AlertDialog(
      onDismissRequest = { customerToDelete = null },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text("⚠️", fontSize = 20.sp)
          Text("تأكيد حذف العميل", fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "هل أنت متأكد من رغبتك في حذف العميل التالي نهائياً؟",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF212529)
          )
          Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
            border = BorderStroke(1.dp, Color(0xFFFFEEBA)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text("👤 الاسم: ${c.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("🔢 رقم الحساب: ${c.accountNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              val bal = c.balance
              val balColor = if (bal > 0) Color(0xFFC62828) else if (bal < 0) Color(0xFF2E7D32) else Color(0xFF495057)
              Text(
                "💰 الرصيد الحالي: ${ArabicNumberHelper.formatAmount(bal)} $",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = balColor
              )
            }
          }
          Text(
            "⚠️ تنبيه: سيتم حذف العميل وحسابه وسجلاته بالكامل. هل تريد تأكيد الحذف أم التراجع؟",
            fontSize = 12.5.sp,
            color = Color(0xFF721C24),
            fontWeight = FontWeight.SemiBold
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteCustomer(c.accountNumber)
            customerToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
        ) {
          Text("🗑️ نعم، حذف", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { customerToDelete = null }
        ) {
          Text("↩️ تراجع / إلغاء", fontWeight = FontWeight.Bold, color = Color(0xFF495057))
        }
      }
    )
  }

  // Edit Customer Dialog
  editingCustomer?.let { c ->
    var newName by remember { mutableStateOf(c.name) }
    var newPhone by remember { mutableStateOf(c.phone) }
    var newAddress by remember { mutableStateOf(c.address) }
    val initialTx = c.transactions.firstOrNull { it.type == "افتتاح" }
    val initialAmt = initialTx?.amount ?: if (c.transactions.isEmpty()) c.balance else 0.0
    var balanceStr by remember {
      mutableStateOf(if (initialAmt != 0.0) ArabicNumberHelper.toEngDigits(Math.abs(initialAmt).toString().removeSuffix(".0")) else "")
    }
    var isDebit by remember { mutableStateOf(initialAmt >= 0) }

    AlertDialog(
      onDismissRequest = { editingCustomer = null },
      title = { Text("تعديل بيانات العميل: ${c.name}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("اسم العميل", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )
          OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it },
            label = { Text("رقم الهاتف", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )
          OutlinedTextField(
            value = newAddress,
            onValueChange = { newAddress = it },
            label = { Text("العنوان", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )

          // جدول الافتتاحي
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  "📊 جدول افتتاحي",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF1E293B)
                )
                Text(
                  if (balanceStr.isBlank() || balanceStr == "0") "متزن (0.00)" else if (isDebit) "حساب مدين (عليه)" else "حساب دائن (له)",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (balanceStr.isBlank() || balanceStr == "0") Color.Gray else if (isDebit) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              // Mini Table Header
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF0070BA), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                  .padding(vertical = 5.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("البيان", modifier = Modifier.weight(1.1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("طبيعة الرصيد", modifier = Modifier.weight(1.5f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("المبلغ", modifier = Modifier.weight(1.4f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
              }

              // Mini Table Body
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.White, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                  .border(0.5.dp, Color(0xFF0070BA), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                  .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Cell 1: البيان
                Text(
                  "افتتاحي",
                  modifier = Modifier.weight(1.1f),
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  color = Color(0xFF1E293B)
                )

                // Cell 2: طبيعة الرصيد (عليكم / لكم)
                Row(
                  modifier = Modifier.weight(1.5f),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .background(
                        if (isDebit) Color(0xFFFFCDD2) else Color(0xFFF1F5F9),
                        RoundedCornerShape(4.dp)
                      )
                      .border(
                        1.dp,
                        if (isDebit) Color(0xFFC62828) else Color(0xFFCBD5E1),
                        RoundedCornerShape(4.dp)
                      )
                      .clickable { isDebit = true }
                      .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      "عليكم",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDebit) Color(0xFFC62828) else Color.Gray
                    )
                  }
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .background(
                        if (!isDebit) Color(0xFFC8E6C9) else Color(0xFFF1F5F9),
                        RoundedCornerShape(4.dp)
                      )
                      .border(
                        1.dp,
                        if (!isDebit) Color(0xFF2E7D32) else Color(0xFFCBD5E1),
                        RoundedCornerShape(4.dp)
                      )
                      .clickable { isDebit = false }
                      .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      "لكم",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (!isDebit) Color(0xFF2E7D32) else Color.Gray
                    )
                  }
                }

                // Cell 3: المبلغ
                Box(modifier = Modifier.weight(1.4f), contentAlignment = Alignment.Center) {
                  BasicTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = TextStyle(
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDebit) Color(0xFFC62828) else Color(0xFF2E7D32),
                      textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                      .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp))
                      .padding(vertical = 5.dp, horizontal = 4.dp),
                    decorationBox = { innerTextField ->
                      if (balanceStr.isEmpty()) {
                        Text("0.00", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                      }
                      innerTextField()
                    }
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val parsedAmt = ArabicNumberHelper.toEngDigits(balanceStr).toDoubleOrNull() ?: 0.0
            val finalInitialBal = if (isDebit) parsedAmt else -parsedAmt
            viewModel.editCustomer(c.accountNumber, newName, newPhone, newAddress, finalInitialBal)
            editingCustomer = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
        ) {
          Text("حفظ التعديل", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        Button(onClick = { editingCustomer = null }) { Text("إلغاء") }
      }
    )
  }

  if (showPreviewWithBalance) {
    val customersWithBalance = remember(sortedCustomers) {
      sortedCustomers.filter { Math.abs(it.balance) > 0.001 }
    }
    CustomersWithBalancePreviewDialog(
      customers = customersWithBalance,
      allCustomersCount = uiState.customers.size,
      storeConfig = uiState.storeConfig,
      onDismiss = { showPreviewWithBalance = false },
      onPrint = {
        PrintHelper.printAllCustomers(
          context = context,
          customers = customersWithBalance,
          storeConfig = uiState.storeConfig,
          title = "كشف حساب العملاء (ذوي الأرصدة)",
          subtitle = "العملاء الذين لديهم رصيد فقط - مستبعد الأرصدة الصفرية"
        )
      }
    )
  }
}

@Composable
fun TabCustomersWithBalancesOnly(viewModel: InvoiceViewModel, onDismiss: () -> Unit = {}) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var editingCustomer by remember { mutableStateOf<Customer?>(null) }
  var customerToDelete by remember { mutableStateOf<Customer?>(null) }
  var showPreviewWithBalance by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var sortMode by remember { mutableStateOf(0) } // 0: Highest balance desc, 1: Lowest balance asc, 2: Account desc, 3: Name
  var filterType by remember { mutableStateOf(0) } // 0: All with balance, 1: Debit (>0), 2: Credit (<0)

  // Filter only customers with non-zero balance
  val allWithBalance = remember(uiState.customers) {
    uiState.customers.filter { Math.abs(it.balance) > 0.001 }
  }

  val totalDebit = remember(allWithBalance) {
    allWithBalance.filter { it.balance > 0 }.sumOf { it.balance }
  }
  val debitCount = remember(allWithBalance) {
    allWithBalance.count { it.balance > 0 }
  }
  val totalCredit = remember(allWithBalance) {
    allWithBalance.filter { it.balance < 0 }.sumOf { Math.abs(it.balance) }
  }
  val creditCount = remember(allWithBalance) {
    allWithBalance.count { it.balance < 0 }
  }
  val netBalance = remember(allWithBalance) {
    allWithBalance.sumOf { it.balance }
  }

  val typeFiltered = remember(allWithBalance, filterType) {
    when (filterType) {
      1 -> allWithBalance.filter { it.balance > 0 }
      2 -> allWithBalance.filter { it.balance < 0 }
      else -> allWithBalance
    }
  }

  val searchFiltered = remember(typeFiltered, searchQuery) {
    if (searchQuery.isBlank()) {
      typeFiltered
    } else {
      val q = searchQuery.trim().lowercase()
      typeFiltered.filter { c ->
        c.name.lowercase().contains(q) ||
        c.accountNumber.contains(q) ||
        c.phone.contains(q) ||
        c.address.lowercase().contains(q)
      }
    }
  }

  val sortedCustomers = remember(searchFiltered, sortMode) {
    when (sortMode) {
      0 -> searchFiltered.sortedByDescending { it.balance }
      1 -> searchFiltered.sortedBy { it.balance }
      2 -> searchFiltered.sortedWith(
        compareByDescending<Customer> {
          ArabicNumberHelper.toEngDigits(it.accountNumber).toLongOrNull() ?: Long.MIN_VALUE
        }.thenByDescending {
          ArabicNumberHelper.toEngDigits(it.accountNumber).trim()
        }
      )
      else -> searchFiltered.sortedBy { it.name.trim() }
    }
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. شريط العنوان وعدد العملاء
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(1.2.dp, Color(0xFFCE93D8)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
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
            color = Color(0xFF6A1B9A),
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text("⚖️", fontSize = 17.sp)
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              "العملاء الذين في حساباتهم أرصدة فقط",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp,
              color = Color(0xFF4A148C),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              "مستبعد الحسابات ذات الأرصدة الصفرية (0.00)",
              fontSize = 11.sp,
              color = Color(0xFF6A1B9A),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
          color = Color(0xFF6A1B9A),
          shape = RoundedCornerShape(14.dp),
          shadowElevation = 1.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "${allWithBalance.size}",
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 13.sp
            )
            Text(
              text = "عميل",
              color = Color.White.copy(alpha = 0.9f),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // 2. كروت إحصائيات الأرصدة
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
          Text("🔴 مدين (عليهم)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
          Text(
            "${ArabicNumberHelper.formatAmount(totalDebit)} $",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFB71C1C)
          )
          Text("$debitCount عميل", fontSize = 10.5.sp, color = Color(0xFF721C24))
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text("🟢 دائن (لهم)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
          Text(
            "${ArabicNumberHelper.formatAmount(totalCredit)} $",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1B5E20)
          )
          Text("$creditCount عميل", fontSize = 10.5.sp, color = Color(0xFF155724))
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
        border = BorderStroke(1.dp, Color(0xFFD1C4E9)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text("⚖️ الصافي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF512DA8))
          Text(
            "${ArabicNumberHelper.formatAmount(netBalance)} $",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = if (netBalance > 0) Color(0xFFC62828) else if (netBalance < 0) Color(0xFF2E7D32) else Color(0xFF4527A0)
          )
          Text(if (netBalance > 0) "فائض مدين" else if (netBalance < 0) "فائض دائن" else "متزن", fontSize = 10.5.sp, color = Color(0xFF311B92))
        }
      }
    }

    // 3. أزرار العمليات العلوية (معاينة - طباعة - تصدير PDF)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { showPreviewWithBalance = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "معاينة",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
          )
        }
      }

      Button(
        onClick = {
          PrintHelper.printAllCustomers(
            context = context,
            customers = sortedCustomers,
            storeConfig = uiState.storeConfig,
            title = "كشف حساب العملاء (الذين في حساباتهم أرصدة فقط)",
            subtitle = "العملاء الذين في حساباتهم أرصدة قائمة - مستبعد الأرصدة الصفرية",
            reportConfig = uiState.reportCustomizationConfig
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17A2B8)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "طباعة الكشف",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
          )
        }
      }

      Button(
        onClick = { viewModel.exportCustomersWithBalanceToPdf(context) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
          .weight(1f)
          .height(42.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "تصدير PDF",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
          )
        }
      }
    }

    // 4. أزرار تصفية سريعة (الكل | مدين فقط | دائن فقط)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Surface(
        onClick = { filterType = 0 },
        color = if (filterType == 0) Color(0xFF6A1B9A) else Color(0xFFEDE7F6),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (filterType == 0) Color(0xFF4A148C) else Color(0xFFD1C4E9)),
        modifier = Modifier.weight(1f).height(34.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            "الكل (${allWithBalance.size})",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (filterType == 0) Color.White else Color(0xFF4A148C)
          )
        }
      }

      Surface(
        onClick = { filterType = 1 },
        color = if (filterType == 1) Color(0xFFC62828) else Color(0xFFFFEBEE),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (filterType == 1) Color(0xFFB71C1C) else Color(0xFFFFCDD2)),
        modifier = Modifier.weight(1f).height(34.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            "مدينون (${debitCount})",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (filterType == 1) Color.White else Color(0xFFC62828)
          )
        }
      }

      Surface(
        onClick = { filterType = 2 },
        color = if (filterType == 2) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (filterType == 2) Color(0xFF1B5E20) else Color(0xFFC8E6C9)),
        modifier = Modifier.weight(1f).height(34.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            "دائنون (${creditCount})",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (filterType == 2) Color.White else Color(0xFF2E7D32)
          )
        }
      }
    }

    // 5. شريط البحث السريع وزر الترتيب
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("🔍 بحث بالاسم، رقم الحساب، أو الهاتف...", fontSize = 12.sp, color = Color(0xFF6B7280)) },
        modifier = Modifier.weight(1f),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        textStyle = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
        colors = standardAppTextFieldColors(focusedBorderColor = Color(0xFF6A1B9A))
      )

      Surface(
        onClick = { sortMode = (sortMode + 1) % 4 },
        color = Color(0xFFEDE7F6),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFCE93D8)),
        modifier = Modifier.height(54.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          val sortLabel = when (sortMode) {
            0 -> "الأعلى رصيداً ⬇️"
            1 -> "الأقل رصيداً ⬆️"
            2 -> "رقم الحساب 🔢"
            else -> "الاسم أبجدياً 🔤"
          }
          Text(
            text = sortLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = Color(0xFF4A148C)
          )
        }
      }
    }

    // 6. بطاقات العملاء
    if (sortedCustomers.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(if (allWithBalance.isEmpty()) "✅" else "🔍", fontSize = 32.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            if (allWithBalance.isEmpty())
              "لا يوجد أي عملاء في حساباتهم أرصدة حالياً (جميع الحسابات متزنة 0.00 $)"
            else
              "لا توجد نتائج مطابقة لبحثك في العملاء ذوي الأرصدة.",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      sortedCustomers.forEach { c ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // الصف الأول: الاسم ورقم الحساب + شارة الرصيد
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
                    .size(36.dp)
                    .background(Color(0xFFEDE7F6), shape = CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text("👤", fontSize = 16.sp)
                }
                Column {
                  Text(
                    text = c.name,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212529),
                    fontSize = 15.sp
                  )
                  Text(
                    text = "رقم الحساب: (${c.accountNumber})",
                    fontSize = 12.sp,
                    color = Color(0xFF6A1B9A),
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              // شارة الرصيد مع توضيح مدين / دائن
              val isDebit = c.balance > 0
              Surface(
                color = if (isDebit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (isDebit) Color(0xFFFFCDD2) else Color(0xFFC8E6C9))
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  horizontalAlignment = Alignment.End
                ) {
                  Text(
                    text = "${ArabicNumberHelper.formatAmount(Math.abs(c.balance))} $",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.5.sp,
                    color = if (isDebit) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                  )
                  Text(
                    text = if (isDebit) "مدين (عليه)" else "دائن (له)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = if (isDebit) Color(0xFFC62828) else Color(0xFF2E7D32)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(6.dp))

            // الصف الثاني: الهاتف، العنوان، الحركات
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("📞 ${c.phone.ifEmpty { "غير محدد" }}", fontSize = 12.sp, color = Color(0xFF4B5563))
              Text("📍 ${c.address.ifEmpty { "غير محدد" }}", fontSize = 12.sp, color = Color(0xFF4B5563))
              Text("📊 الحركات: ${c.transactions.size}", fontSize = 12.sp, color = Color(0xFF6A1B9A), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // الصف الثالث: أزرار العمليات (كشف حساب، تعديل، حذف)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Button(
                onClick = { viewModel.requestStatementDateRange(c) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1.3f)
                  .height(36.dp)
              ) {
                Text("📊 كشف حساب", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
              }

              Button(
                onClick = { editingCustomer = c },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("تعديل", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
              }

              Button(
                onClick = { customerToDelete = c },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("حذف", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
              }
            }
          }
        }
      }
    }
  }

  // Delete Customer Confirmation Dialog
  customerToDelete?.let { c ->
    AlertDialog(
      onDismissRequest = { customerToDelete = null },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text("⚠️", fontSize = 20.sp)
          Text("تأكيد حذف العميل", fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "هل أنت متأكد من رغبتك في حذف العميل التالي نهائياً؟",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF212529)
          )
          Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
            border = BorderStroke(1.dp, Color(0xFFFFEEBA)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text("👤 الاسم: ${c.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("🔢 رقم الحساب: ${c.accountNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              val bal = c.balance
              val balColor = if (bal > 0) Color(0xFFC62828) else if (bal < 0) Color(0xFF2E7D32) else Color(0xFF495057)
              Text(
                "💰 الرصيد الحالي: ${ArabicNumberHelper.formatAmount(bal)} $",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = balColor
              )
            }
          }
          Text(
            "⚠️ تنبيه: سيتم حذف العميل وحسابه وسجلاته بالكامل. هل تريد تأكيد الحذف أم التراجع؟",
            fontSize = 12.5.sp,
            color = Color(0xFF721C24),
            fontWeight = FontWeight.SemiBold
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteCustomer(c.accountNumber)
            customerToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
        ) {
          Text("🗑️ نعم، حذف", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { customerToDelete = null }
        ) {
          Text("↩️ تراجع / إلغاء", fontWeight = FontWeight.Bold, color = Color(0xFF495057))
        }
      }
    )
  }

  // Edit Customer Dialog
  editingCustomer?.let { c ->
    var newName by remember { mutableStateOf(c.name) }
    var newPhone by remember { mutableStateOf(c.phone) }
    var newAddress by remember { mutableStateOf(c.address) }
    val initialTx = c.transactions.firstOrNull { it.type == "افتتاح" }
    val initialAmt = initialTx?.amount ?: if (c.transactions.isEmpty()) c.balance else 0.0
    var balanceStr by remember {
      mutableStateOf(if (initialAmt != 0.0) ArabicNumberHelper.toEngDigits(Math.abs(initialAmt).toString().removeSuffix(".0")) else "")
    }
    var isDebit by remember { mutableStateOf(initialAmt >= 0) }

    AlertDialog(
      onDismissRequest = { editingCustomer = null },
      title = { Text("تعديل بيانات العميل: ${c.name}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("اسم العميل", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )
          OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it },
            label = { Text("رقم الهاتف", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )
          OutlinedTextField(
            value = newAddress,
            onValueChange = { newAddress = it },
            label = { Text("العنوان", fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            colors = mandatoryTextFieldColors()
          )

          // جدول الافتتاحي
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  "📊 جدول افتتاحي",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF1E293B)
                )
                Text(
                  if (balanceStr.isBlank() || balanceStr == "0") "متزن (0.00)" else if (isDebit) "حساب مدين (عليه)" else "حساب دائن (له)",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (balanceStr.isBlank() || balanceStr == "0") Color.Gray else if (isDebit) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              // Mini Table Header
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF0070BA), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                  .padding(vertical = 5.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("البيان", modifier = Modifier.weight(1.1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                Text("طبيعة الرصيد", modifier = Modifier.weight(1.5f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                Text("المبلغ", modifier = Modifier.weight(1.4f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
              }

              // Mini Table Body
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.White, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                  .border(0.5.dp, Color(0xFF0070BA), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                  .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  "افتتاحي",
                  modifier = Modifier.weight(1.1f),
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp,
                  textAlign = TextAlign.Center,
                  color = Color(0xFF1E293B)
                )

                Row(
                  modifier = Modifier.weight(1.5f),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .background(
                        if (isDebit) Color(0xFFFFCDD2) else Color(0xFFF1F5F9),
                        RoundedCornerShape(4.dp)
                      )
                      .border(
                        1.dp,
                        if (isDebit) Color(0xFFC62828) else Color(0xFFCBD5E1),
                        RoundedCornerShape(4.dp)
                      )
                      .clickable { isDebit = true }
                      .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      "عليكم",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDebit) Color(0xFFC62828) else Color.Gray
                    )
                  }
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .background(
                        if (!isDebit) Color(0xFFC8E6C9) else Color(0xFFF1F5F9),
                        RoundedCornerShape(4.dp)
                      )
                      .border(
                        1.dp,
                        if (!isDebit) Color(0xFF2E7D32) else Color(0xFFCBD5E1),
                        RoundedCornerShape(4.dp)
                      )
                      .clickable { isDebit = false }
                      .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      "لكم",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (!isDebit) Color(0xFF2E7D32) else Color.Gray
                    )
                  }
                }

                Box(modifier = Modifier.weight(1.4f), contentAlignment = Alignment.Center) {
                  BasicTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = TextStyle(
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDebit) Color(0xFFC62828) else Color(0xFF2E7D32),
                      textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                      .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp))
                      .padding(vertical = 5.dp, horizontal = 4.dp),
                    decorationBox = { innerTextField ->
                      if (balanceStr.isEmpty()) {
                        Text("0.00", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                      }
                      innerTextField()
                    }
                  )
                }
              }
            }
          }
        }
      },
    confirmButton = {
      Button(
        onClick = {
          val parsedAmt = ArabicNumberHelper.toEngDigits(balanceStr).toDoubleOrNull() ?: 0.0
          val finalInitialBal = if (isDebit) parsedAmt else -parsedAmt
          viewModel.editCustomer(c.accountNumber, newName, newPhone, newAddress, finalInitialBal)
          editingCustomer = null
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
      ) {
        Text("حفظ التعديل", fontWeight = FontWeight.Bold, color = Color.White)
      }
    },
    dismissButton = {
      Button(onClick = { editingCustomer = null }) { Text("إلغاء") }
    }
  )
}

  if (showPreviewWithBalance) {
    CustomersWithBalancePreviewDialog(
      customers = sortedCustomers,
      allCustomersCount = uiState.customers.size,
      storeConfig = uiState.storeConfig,
      onDismiss = { showPreviewWithBalance = false },
      onPrint = {
        PrintHelper.printAllCustomers(
          context = context,
          customers = sortedCustomers,
          storeConfig = uiState.storeConfig,
          title = "كشف حساب العملاء (الذين في حساباتهم أرصدة فقط)",
          subtitle = "العملاء الذين في حساباتهم أرصدة قائمة - مستبعد الأرصدة الصفرية"
        )
      }
    )
  }
}

@Composable
fun TabCustomerStatement(viewModel: InvoiceViewModel, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var accNumber by remember { mutableStateOf("") }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("📊 كشف حساب عميل مفصل", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A237E))

    OutlinedTextField(
      value = accNumber,
      onValueChange = { accNumber = it },
      label = { Text("رقم حساب العميل", fontWeight = FontWeight.Bold) },
      placeholder = { Text("أدخل رقم الحساب (مثال: 100)", color = Color(0xFF6B7280)) },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier.fillMaxWidth(),
      textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
      colors = standardAppTextFieldColors(focusedBorderColor = Color(0xFF1A237E))
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Button(
        onClick = {
          if (viewModel.openStatementByAccount(accNumber)) {
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
        modifier = Modifier
          .weight(1f)
          .height(40.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
      ) {
        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("معاينة", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White, maxLines = 1)
      }

      Button(
        onClick = {
          val clean = ArabicNumberHelper.toEngDigits(accNumber).trim()
          val customer = uiState.customers.find { it.accountNumber == clean }
          if (customer != null) {
            PrintHelper.printStatement(context, customer, uiState.storeConfig, reportConfig = uiState.reportCustomizationConfig, exchangeRates = uiState.exchangeRates)
          } else {
            viewModel.showToast("❌ لا يوجد عميل بهذا الرقم.")
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17A2B8)),
        modifier = Modifier
          .weight(1f)
          .height(40.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
      ) {
        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("طباعة", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White, maxLines = 1)
      }

      Button(
        onClick = {
          val clean = ArabicNumberHelper.toEngDigits(accNumber).trim()
          val customer = uiState.customers.find { it.accountNumber == clean }
          if (customer != null) {
            viewModel.exportStatementToPdf(context, customer)
          } else {
            viewModel.showToast("❌ لا يوجد عميل بهذا الرقم.")
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
        modifier = Modifier
          .weight(1f)
          .height(40.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
      ) {
        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("تصدير PDF", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color.White, maxLines = 1)
      }
    }

    Text("أو اختر عميلاً من القائمة لعرض كشف حسابه:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Gray)
    val sortedStatementCustomers = remember(uiState.customers) {
      uiState.customers.sortedWith(
        compareByDescending<Customer> {
          ArabicNumberHelper.toEngDigits(it.accountNumber).toLongOrNull() ?: Long.MIN_VALUE
        }.thenByDescending {
          ArabicNumberHelper.toEngDigits(it.accountNumber).trim()
        }.thenBy {
          it.name.trim()
        }
      )
    }
    sortedStatementCustomers.forEach { customer ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            viewModel.requestStatementDateRange(customer)
          },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(customer.name, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), fontSize = 14.sp)
            Text("حساب: ${customer.accountNumber} | هاتف: ${customer.phone.ifEmpty { "—" }}", fontSize = 12.sp, color = Color.Gray)
          }
          Text(
            "${ArabicNumberHelper.formatAmount(customer.balance)} $",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = if (customer.balance > 0) Color(0xFFD32F2F) else Color(0xFF28A745)
          )
        }
      }
    }
  }
}

@Composable
fun TabExchangeRates(viewModel: InvoiceViewModel) {
  val uiState by viewModel.uiState.collectAsState()
  var usdToYer by remember { mutableStateOf(uiState.exchangeRates.usdToYer.toString()) }
  var sarToYer by remember { mutableStateOf(uiState.exchangeRates.sarToYer.toString()) }
  var usdToSar by remember { mutableStateOf(uiState.exchangeRates.usdToSar.toString()) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      "💱 أسعار صرف العملات والتحويل",
      fontWeight = FontWeight.ExtraBold,
      fontSize = 16.sp,
      color = Color(0xFF1A237E)
    )

    OutlinedTextField(
      value = usdToYer,
      onValueChange = { usdToYer = it },
      label = { Text("سعر الدولار (USD) مقابل الريال اليمني (YER)", fontWeight = FontWeight.Bold) },
      modifier = Modifier.fillMaxWidth(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    OutlinedTextField(
      value = sarToYer,
      onValueChange = { sarToYer = it },
      label = { Text("سعر الريال السعودي (SAR) مقابل الريال اليمني (YER)", fontWeight = FontWeight.Bold) },
      modifier = Modifier.fillMaxWidth(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    OutlinedTextField(
      value = usdToSar,
      onValueChange = { usdToSar = it },
      label = { Text("سعر الدولار (USD) مقابل الريال السعودي (SAR)", fontWeight = FontWeight.Bold) },
      modifier = Modifier.fillMaxWidth(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    Button(
      onClick = {
        val u2y = usdToYer.toDoubleOrNull() ?: 1000.0
        val s2y = sarToYer.toDoubleOrNull() ?: 370.0
        val u2s = usdToSar.toDoubleOrNull() ?: 3.75
        val rates = ExchangeRates(
          usdToYer = u2y,
          sarToYer = s2y,
          usdToSar = u2s,
          yerToUsd = if (u2y > 0) 1.0 / u2y else 0.001,
          yerToSar = if (s2y > 0) 1.0 / s2y else 0.0027,
          sarToUsd = if (u2s > 0) 1.0 / u2s else 0.2667
        )
        viewModel.updateExchangeRates(rates)
      },
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
      Text("💾 حفظ أسعار العملات", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
    }
  }
}

@Composable
fun TabCustomerStatementDateRange(viewModel: InvoiceViewModel, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedCustomer by remember { mutableStateOf<Customer?>(uiState.pendingStatementCustomer) }

  val purpleBrand = Color(0xFF1A237E)

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

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = "📅 كشف حساب تفصيلي لعميل حسب الفترة",
      fontWeight = FontWeight.ExtraBold,
      fontSize = 16.sp,
      color = purpleBrand
    )

    if (selectedCustomer == null) {
      Text(
        text = "الخطوة الأولى: اختر العميل لتحديد الفترة له",
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF495057)
      )

      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("ابحث باسم العميل أو رقم حسابه", fontWeight = FontWeight.Bold) },
        placeholder = { Text("اكتب اسم العميل أو رقمه...", color = Color(0xFF6B7280)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
        colors = standardAppTextFieldColors(focusedBorderColor = purpleBrand)
      )

      val sortedDateRangeCustomers = remember(uiState.customers) {
        uiState.customers.sortedWith(
          compareByDescending<Customer> {
            ArabicNumberHelper.toEngDigits(it.accountNumber).toLongOrNull() ?: Long.MIN_VALUE
          }.thenByDescending {
            ArabicNumberHelper.toEngDigits(it.accountNumber).trim()
          }.thenBy {
            it.name.trim()
          }
        )
      }

      val filteredCustomers = sortedDateRangeCustomers.filter {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) true
        else it.name.lowercase().contains(q) || it.accountNumber.contains(q) || it.phone.contains(q)
      }

      if (filteredCustomers.isEmpty()) {
        Text("لا يوجد عملاء مطابقون للبحث.", color = Color.Gray, fontSize = 13.sp)
      } else {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          filteredCustomers.forEach { customer ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedCustomer = customer
                  viewModel.selectCustomerForDateRange(customer)
                },
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEE2E6))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(customer.name, fontWeight = FontWeight.Bold, color = purpleBrand, fontSize = 14.sp)
                  Text("حساب: ${customer.accountNumber} | هاتف: ${customer.phone.ifEmpty { "—" }}", fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                  text = "${ArabicNumberHelper.formatAmount(customer.balance)} $",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp,
                  color = if (customer.balance > 0) Color(0xFFD32F2F) else Color(0xFF28A745)
                )
              }
            }
          }
        }
      }
    } else {
      val customer = selectedCustomer!!

      // Selected Customer Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7FC)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE5DBF0))
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
              fontSize = 16.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "رقم الحساب: ${customer.accountNumber} | الرصيد الحالي: ${ArabicNumberHelper.formatAmount(customer.balance)} $",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF555555)
            )
          }

          Button(
            onClick = {
              selectedCustomer = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C757D)),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text("تغيير العميل", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Title & Instruction
      Text(
        text = "تحديد الفترة الزمنية:",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = purpleBrand
      )

      // Quick Buttons Row
      Text(
        text = "أزرار سريعة للفترة:",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF495057)
      )
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

      // Date fields: من تاريخ / إلى تاريخ
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

      Spacer(modifier = Modifier.height(6.dp))

      // Action Buttons: عرض الكشف / إلغاء
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            if (viewModel.applyDateRangeAndOpenStatement()) {
              onDismiss()
            }
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
          onClick = {
            selectedCustomer = null
          },
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

@Composable
fun VoucherPreviewDialog(
  voucher: VoucherItem,
  storeConfig: StoreConfig,
  reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
  onDismiss: () -> Unit,
  onPrint: () -> Unit,
  onExportPdf: () -> Unit = {},
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val isPayment = voucher.type == "صرف"
  val title = if (isPayment) "سند صـــــرف" else "سند قـــــبض"
  val titleEn = if (isPayment) "PAYMENT VOUCHER" else "RECEIPT VOUCHER"
  val mainColor = if (isPayment) Color(0xFFC62828) else Color(0xFF2E7D32)
  val lightBg = if (isPayment) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
  val sym = ArabicNumberHelper.getCurrencySymbol(voucher.currency)
  val currName = ArabicNumberHelper.getCurrencyName(voucher.currency)
  val amountInWords = if (reportConfig.showAmountInWords) "${ArabicNumberHelper.numberToArabicWords(voucher.amount)} $currName" else ""

  val scale = reportConfig.fontScale
  val reportFont = getReportFontFamily(reportConfig.fontFamily)
  val headerColor = parseHexColor(reportConfig.headerColorHex, Color(0xFF1A237E))
  val primaryText = parseHexColor(reportConfig.primaryTextColorHex, Color(0xFF111111))
  val tableBorder = parseHexColor(reportConfig.tableBorderColorHex, mainColor)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    CompositionLocalProvider(
      LocalLayoutDirection provides LayoutDirection.Rtl,
      LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = reportFont)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.96f)
          .fillMaxHeight(0.92f)
          .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, tableBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
        ) {
          // Top bar: Close button & title
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "👁️ معاينة $title",
              fontWeight = FontWeight.ExtraBold,
              fontSize = (17 * scale).sp,
              color = headerColor
            )
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

          // Scrollable Voucher Document
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Optional Notice Badge
            if (reportConfig.customNoticeBadge.isNotBlank()) {
              Surface(
                color = Color(0xFFFFF9E6),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Color(0xFFFFB300)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = reportConfig.customNoticeBadge,
                  fontSize = (11.5 * scale).sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFB78103),
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(vertical = 3.dp, horizontal = 6.dp)
                )
              }
            }

            // Optional Custom Header Title
            if (reportConfig.customHeaderTitle.isNotBlank()) {
              Text(
                text = reportConfig.customHeaderTitle,
                fontSize = (15 * scale).sp,
                fontWeight = FontWeight.Black,
                color = headerColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
              )
            }

            // Header with Store Info & Logo
            if (reportConfig.showStoreInfo || reportConfig.showLogo) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Right: Arabic info
                if (reportConfig.showStoreInfo) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(storeConfig.storeNameAr.ifBlank { "المملكة للإلكترونيات" }, fontWeight = FontWeight.Black, fontSize = (15 * scale).sp, color = headerColor)
                    Text(storeConfig.addressAr.ifBlank { "إب شارع تعز" }, fontSize = (11 * scale).sp, color = primaryText)
                    Text("هاتف: ${storeConfig.phone.ifBlank { "772707736" }}", fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = headerColor)
                    if (reportConfig.showBranch && storeConfig.branch.isNotBlank()) {
                      Text("الفرع: ${storeConfig.branch}", fontSize = (10.5 * scale).sp, fontWeight = FontWeight.Bold, color = headerColor)
                    }
                    if (reportConfig.taxOrCrNumber.isNotBlank()) {
                      Text("الرقم الضريبي/السجل: ${reportConfig.taxOrCrNumber}", fontSize = (10.5 * scale).sp, fontWeight = FontWeight.Bold, color = primaryText)
                    }
                  }
                } else {
                  Spacer(modifier = Modifier.weight(1f))
                }

                // Center: Logo
                if (reportConfig.showLogo) {
                  Box(
                    modifier = Modifier.weight(0.8f),
                    contentAlignment = Alignment.Center
                  ) {
                    AlmamlakaLogoBadge(size = 68.dp, logoBase64 = storeConfig.logoBase64)
                  }
                } else {
                  Spacer(modifier = Modifier.weight(0.8f))
                }

                // Left: English info
                if (reportConfig.showStoreInfo) {
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                      modifier = Modifier.weight(1f),
                      horizontalAlignment = Alignment.End
                    ) {
                      Text(storeConfig.storeNameEn.ifBlank { "ALMamlaka Electronics" }, fontWeight = FontWeight.Black, fontSize = (13 * scale).sp, color = headerColor)
                      Text(storeConfig.addressEn.ifBlank { "YEMEN Ibb" }, fontSize = (10 * scale).sp, color = primaryText)
                      Text("TEL: ${storeConfig.phone.ifBlank { "772707736" }}", fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = headerColor)
                    }
                  }
                } else {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }

            // Title Banner
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(mainColor, RoundedCornerShape(20.dp))
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = (17 * scale).sp)
                Text(titleEn, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, fontSize = (11 * scale).sp)
              }
            }

            // Meta Info Bar
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = lightBg),
              border = BorderStroke(1.dp, mainColor.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("رقم السند:", fontSize = (12 * scale).sp, color = Color.DarkGray)
                  Text(voucher.voucherNum, fontWeight = FontWeight.Black, fontSize = (15 * scale).sp, color = mainColor)
                }
                if (reportConfig.showBranch && storeConfig.branch.isNotBlank()) {
                  Column {
                    Text("الفرع:", fontSize = (12 * scale).sp, color = Color.DarkGray)
                    Text(storeConfig.branch, fontWeight = FontWeight.Bold, fontSize = (13 * scale).sp, color = headerColor)
                  }
                }
                if (reportConfig.showDateTime) {
                  Column {
                    Text("التاريخ:", fontSize = (12 * scale).sp, color = Color.DarkGray)
                    Text(voucher.date, fontWeight = FontWeight.Bold, fontSize = (13 * scale).sp, color = primaryText)
                  }
                }
              }
            }

            // Amount Box
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
              border = BorderStroke(1.5.dp, Color(0xFFFFA000))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("المبلغ الإجمالي", fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(
                  "${ArabicNumberHelper.formatAmount(voucher.amount)} $sym",
                  fontSize = (24 * scale).sp,
                  fontWeight = FontWeight.Black,
                  color = mainColor
                )
                if (reportConfig.showAmountInWords && amountInWords.isNotBlank()) {
                  Text(
                    amountInWords,
                    fontSize = (13 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }

            // Details Table Card
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
              border = BorderStroke(1.dp, tableBorder)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    if (isPayment) "يصرف للمكرم:" else "استلمنا من المكرم:",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (13 * scale).sp,
                    color = Color.DarkGray,
                    modifier = Modifier.width(130.dp)
                  )
                  val accountStr = if (reportConfig.showCustomerAccountNumber && voucher.account.isNotBlank()) " (حساب: ${voucher.account})" else ""
                  Text(
                    "${voucher.customerName}$accountStr",
                    fontWeight = FontWeight.Black,
                    fontSize = (14 * scale).sp,
                    color = primaryText
                  )
                }
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Row(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    "وذلك مقابل / البيان:",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (13 * scale).sp,
                    color = Color.DarkGray,
                    modifier = Modifier.width(130.dp)
                  )
                  Text(
                    voucher.note.ifEmpty { "تسديد حساب / حركة نقدية" },
                    fontWeight = FontWeight.Bold,
                    fontSize = (13 * scale).sp,
                    color = primaryText
                  )
                }
              }
            }

            // Signatures & Official Stamp
            if (reportConfig.showSignatures) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 14.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = if (reportConfig.accountantSignatureName.isNotBlank())
                      "توقيع أمين الصندوق (${reportConfig.accountantSignatureName})"
                    else
                      "توقيع أمين الصندوق / المحاسب",
                    fontSize = (11.5 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                  )
                  Spacer(modifier = Modifier.height(24.dp))
                  HorizontalDivider(modifier = Modifier.fillMaxWidth(0.7f), thickness = 1.dp, color = Color.DarkGray)
                }

                if (reportConfig.showStampSeal) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                    modifier = Modifier.padding(horizontal = 4.dp)
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(
                        text = "★ معتمد ★",
                        fontSize = (11 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC62828)
                      )
                      Text(
                        text = "APPROVED",
                        fontSize = (8 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                      )
                    }
                  }
                }

                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = if (reportConfig.managerSignatureName.isNotBlank())
                      "توقيع المستلم (${reportConfig.managerSignatureName})"
                    else
                      "توقيع المستلم / العميل",
                    fontSize = (11.5 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                  )
                  Spacer(modifier = Modifier.height(24.dp))
                  HorizontalDivider(modifier = Modifier.fillMaxWidth(0.7f), thickness = 1.dp, color = Color.DarkGray)
                }
              }
            } else if (reportConfig.showStampSeal) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE),
                border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
              ) {
                Text(
                  text = "★ معتمد رسمياً APPROVED ★",
                  fontSize = (11.5 * scale).sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFFC62828),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
              }
            }

            // Custom Footer Note
            if (reportConfig.customFooterText.isNotBlank()) {
              Text(
                text = reportConfig.customFooterText,
                fontSize = (11 * scale).sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFF8F9FA), RoundedCornerShape(4.dp))
                  .padding(6.dp)
              )
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

          // Action buttons footer
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Button(
              onClick = onPrint,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070BA)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("🖨️ طباعة", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
              onClick = onExportPdf,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("📄 PDF", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
              onClick = onEdit,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("✏️ تعديل", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
              onClick = onDelete,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("🗑️ حذف", fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }
    }
  }
}

@Composable
fun VoucherDeleteDialog(
  voucher: VoucherItem,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        "🗑️ تأكيد حذف سند ال${voucher.type}",
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFC62828)
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          "هل أنت متأكد من حذف سند ال${voucher.type} رقم (${voucher.voucherNum})؟",
          fontWeight = FontWeight.Bold
        )
        Text(
          "• الحساب: ${voucher.customerName} (${voucher.account})",
          fontSize = 13.sp
        )
        Text(
          "• المبلغ: ${ArabicNumberHelper.formatAmount(voucher.amount)} ${voucher.currency}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFB71C1C)
        )
        Text(
          "⚠️ تحذير: سيتم حذف هذه الحركة بالكامل وإعادة احتساب رصيد العميل بشكل تلقائي وفوري.",
          fontSize = 12.sp,
          color = Color(0xFFD32F2F),
          fontWeight = FontWeight.Bold
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
      ) {
        Text("نعم، حذف السند", fontWeight = FontWeight.Bold, color = Color.White)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}

@Composable
fun CustomersWithBalancePreviewDialog(
  customers: List<Customer>,
  allCustomersCount: Int,
  storeConfig: StoreConfig,
  onDismiss: () -> Unit,
  onPrint: () -> Unit
) {
  val totalBalance = customers.sumOf { it.balance }
  val dateStr = ArabicNumberHelper.formatDateTime()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.96f)
          .fillMaxHeight(0.92f)
          .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFF1A237E))
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
        ) {
          // Top bar: Title and Close button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Visibility,
                contentDescription = null,
                tint = Color(0xFF1A237E),
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "👁️ معاينة كشف العملاء (ذوي الأرصدة)",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color(0xFF1A237E)
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

          // Scrollable Document Content
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Header with Store Info & Logo
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Right: Arabic store info
              Column(modifier = Modifier.weight(1f)) {
                Text(storeConfig.storeNameAr, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF1A237E))
                if (storeConfig.addressAr.isNotBlank()) {
                  Text(storeConfig.addressAr, fontSize = 11.sp, color = Color.DarkGray)
                }
                if (storeConfig.phone.isNotBlank()) {
                  Text("هاتف: ${storeConfig.phone}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                }
                Text("التاريخ: $dateStr", fontSize = 11.sp, color = Color.Gray)
              }

              // Center: Logo
              Box(
                modifier = Modifier.weight(0.7f),
                contentAlignment = Alignment.Center
              ) {
                AlmamlakaLogoBadge(size = 64.dp, logoBase64 = storeConfig.logoBase64)
              }

              // Left: Subtitle info
              Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
              ) {
                Text(
                  "تقرير الأرصدة القائمة",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2E7D32)
                )
              }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Status / Summary banner
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
              border = BorderStroke(1.dp, Color(0xFFCE93D8))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    "📊 عدد العملاء ذوي الأرصدة: ${customers.size} (من إجمالي $allCustomersCount)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF4A148C)
                  )
                  Text(
                    "إجمالي الأرصدة: ${ArabicNumberHelper.formatAmount(totalBalance)} $",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = if (totalBalance > 0) Color(0xFFD32F2F) else Color(0xFF28A745)
                  )
                }
              }
            }

            if (customers.isEmpty()) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text("✅ جميع الحسابات رصيدها صفري", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                  Text("لا يوجد أي عميل لديه رصيد مستحق أو دائن حالياً.", fontSize = 13.sp, color = Color(0xFF558B2F))
                }
              }
            } else {
              // Table Header
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF2B5797), shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                  .padding(vertical = 8.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("رقم الحساب", modifier = Modifier.weight(1.2f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                Text("الاسم", modifier = Modifier.weight(2.0f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                Text("الهاتف", modifier = Modifier.weight(1.4f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                Text("الرصيد الحالي", modifier = Modifier.weight(1.5f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                Text("الحركات", modifier = Modifier.weight(0.9f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
              }

              // Table Rows
              customers.forEachIndexed { idx, c ->
                val bg = if (idx % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFF9F9F9)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .border(0.5.dp, Color(0xFFC5CAE9))
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(c.accountNumber, modifier = Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                  Text(c.name, modifier = Modifier.weight(2.0f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                  Text(c.phone.ifEmpty { "-" }, modifier = Modifier.weight(1.4f), fontSize = 11.5.sp, color = Color.DarkGray)
                  Text(
                    "${ArabicNumberHelper.formatAmount(c.balance)} $",
                    modifier = Modifier.weight(1.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (c.balance > 0) Color(0xFFD32F2F) else Color(0xFF28A745)
                  )
                  Text("${c.transactions.size}", modifier = Modifier.weight(0.9f), fontSize = 11.5.sp, color = Color.Gray)
                }
              }

              // Total row
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFEDE7F6), shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                  .border(1.dp, Color(0xFFB39DDB))
                  .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  "المجموع الإجمالي للأرصدة (${customers.size} عميل):",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 13.sp,
                  color = Color(0xFF4A148C)
                )
                Text(
                  "${ArabicNumberHelper.formatAmount(totalBalance)} $",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp,
                  color = if (totalBalance > 0) Color(0xFFD32F2F) else Color(0xFF28A745)
                )
              }
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

          // Bottom Actions Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onPrint,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .height(42.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("طباعة الكشف", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
            }

            OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(0.5f)
                .height(42.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("إغلاق", fontWeight = FontWeight.Bold, color = Color(0xFF555555), fontSize = 13.sp, maxLines = 1)
            }
          }
        }
      }
    }
  }
}
