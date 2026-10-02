package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.InvoiceViewModel

@Composable
fun YearEndClosingModal(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  var targetNewYear by remember(uiState.activeFiscalYear) {
    mutableStateOf(uiState.activeFiscalYear + 1)
  }
  var showConfirmDialog by remember { mutableStateOf(false) }

  val customersWithBalanceCount = uiState.customers.count { Math.abs(it.balance) >= 0.005 }
  val customersZeroBalanceCount = uiState.customers.size - customersWithBalanceCount

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.95f)
          .fillMaxHeight(0.92f)
          .padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          // ==================== Header ====================
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .background(
                    Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C))),
                    RoundedCornerShape(12.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LockReset,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
              Column {
                Text(
                  text = "الإقفال السنوي وفتح سنة جديدة",
                  fontSize = 17.5.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF881337)
                )
                Text(
                  text = "نظام إقفال الحسابات وترحيل الأرصدة الافتتاحية",
                  fontSize = 11.5.sp,
                  color = Color(0xFF64748B)
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Card 1: ملخص السنة الحالية
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              border = BorderStroke(1.2.dp, Color(0xFFE2E8F0))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = Color(0xFFFFF1F2),
                      modifier = Modifier.size(28.dp)
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Text("📅", fontSize = 14.sp)
                      }
                    }
                    Text(
                      text = "السنة الحالية المُراد إقفالها:",
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF334155)
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFBE123C)
                  ) {
                    Text(
                      text = "${uiState.activeFiscalYear}",
                      color = Color.White,
                      fontWeight = FontWeight.Black,
                      fontSize = 15.sp,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                  }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  ClosingStatItem(
                    label = "فواتير السنة",
                    value = "${uiState.savedInvoices.size}",
                    color = Color(0xFF6D28D9)
                  )
                  ClosingStatItem(
                    label = "إجمالي العملاء",
                    value = "${uiState.customers.size}",
                    color = Color(0xFF0369A1)
                  )
                  ClosingStatItem(
                    label = "عملاء بأرصدة",
                    value = "$customersWithBalanceCount",
                    color = Color(0xFFB91C1C)
                  )
                  ClosingStatItem(
                    label = "عملاء بدون رصيد",
                    value = "$customersZeroBalanceCount",
                    color = Color(0xFF15803D)
                  )
                }
              }
            }

            // Card 2: تحديد السنة المالية الجديدة
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
              border = BorderStroke(1.2.dp, Color(0xFF86EFAC))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "السنة المالية الجديدة القادمة:",
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 14.sp,
                      color = Color(0xFF14532D)
                    )
                    Text(
                      text = "سيتم فتح هذه السنة لبدء العمليات الجديدة",
                      fontSize = 11.sp,
                      color = Color(0xFF166534)
                    )
                  }

                  // Stepper to adjust year if desired
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    IconButton(
                      onClick = { if (targetNewYear > uiState.activeFiscalYear) targetNewYear-- },
                      modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFF86EFAC), CircleShape)
                    ) {
                      Icon(Icons.Default.Remove, contentDescription = "تقليل", modifier = Modifier.size(16.dp), tint = Color(0xFF15803D))
                    }

                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = Color(0xFF15803D),
                      shadowElevation = 2.dp
                    ) {
                      Text(
                        text = "$targetNewYear",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                      )
                    }

                    IconButton(
                      onClick = { targetNewYear++ },
                      modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFF86EFAC), CircleShape)
                    ) {
                      Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp), tint = Color(0xFF15803D))
                    }
                  }
                }
              }
            }

            // Card 3: تفاصيل القواعد المحاسبية عند الإقفال
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = "📋 ما الذي سيحدث عند الإقفال السنوي وفتح سنة $targetNewYear؟",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF1E293B)
                )

                ClosingRuleItem(
                  icon = "👥",
                  title = "ترحيل جميع العملاء وأرقام حساباتهم كما هي:",
                  desc = "يتم نقل جميع العملاء (${uiState.customers.size} عميل) سواء لديهم أرصدة أو لا، مع الاحتفاظ بأرقام الحسابات والأسماء والهواتف ذاتها دون أي تغيير."
                )

                ClosingRuleItem(
                  icon = "⚖️",
                  title = "ترحيل الأرصدة كأرصدة افتتاحية:",
                  desc = "العملاء الذين عليهم أو لهم مبالغ سيتم ترحيل رصيدهم لسنة $targetNewYear كحركة أولى «رصيد افتتاحي» بتاريخ 01/01/$targetNewYear."
                )

                ClosingRuleItem(
                  icon = "🔢",
                  title = "بدء ترقيم الفواتير والسندات من الرقم (1):",
                  desc = "فواتير السنة الجديدة ستبدأ من الفاتورة رقم (1)، وسندات القبض ستبدأ من (1)، وسندات الصرف ستبدأ من (1)."
                )

                ClosingRuleItem(
                  icon = "📂",
                  title = "حفظ كامل فواتير وكشوفات سنة ${uiState.activeFiscalYear}:",
                  desc = "تظل جميع فواتير وسندات وكشوفات حساب سنة ${uiState.activeFiscalYear} محفوظة بالكامل، وتستطيع الرجوع إليها في أي وقت عبر اختيار السنة من أعلى الواجهة الرئيسية."
                )
              }
            }

            // Card 4: تنبيه الأمان
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFFEF2F2),
              border = BorderStroke(1.dp, Color(0xFFFECDD3))
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFBE123C), modifier = Modifier.size(20.dp))
                Text(
                  text = "سيتم تلقائياً حفظ نسخة احتياطية آمنة في قاعدة بيانات Room لبيانات سنة ${uiState.activeFiscalYear} بالكامل قبل إتمام الإقفال.",
                  fontSize = 11.5.sp,
                  color = Color(0xFF9F1239),
                  lineHeight = 16.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // ==================== Footer Action Buttons ====================
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
            ) {
              Text("إلغاء وتراجع", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            Button(
              onClick = { showConfirmDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1.6f)
                .height(48.dp)
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "تنفيذ الإقفال وفتح ($targetNewYear)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }

  // Confirmation Alert Dialog
  if (showConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFBE123C), modifier = Modifier.size(36.dp))
      },
      title = {
        Text(
          text = "تأكيد الإقفال السنوي لـ (${uiState.activeFiscalYear})؟",
          fontWeight = FontWeight.Black,
          fontSize = 16.sp,
          color = Color(0xFFBE123C),
          textAlign = TextAlign.Center
        )
      },
      text = {
        Text(
          text = "هل أنت متأكد من إقفال السنة المالية (${uiState.activeFiscalYear}) وفتح السنة الجديدة ($targetNewYear)؟\n\n• سيتم نقل ${uiState.customers.size} عميل بأرقام حساباتهم.\n• سيتم ترحيل الأرصدة كأرصدة افتتاحية.\n• ستبدأ الفواتير والسندات في ($targetNewYear) من الرقم 1.\n• يمكنك الرجوع لبيانات (${uiState.activeFiscalYear}) في أي وقت من الواجهة الرئيسية.",
          fontSize = 12.5.sp,
          lineHeight = 18.sp,
          color = Color(0xFF1E293B)
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showConfirmDialog = false
            viewModel.performYearEndClosing(uiState.activeFiscalYear, targetNewYear)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C))
        ) {
          Text("نعم، إقفال وفتح سنة $targetNewYear", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        Button(
          onClick = { showConfirmDialog = false }
        ) {
          Text("تراجع")
        }
      }
    )
  }
}

@Composable
private fun ClosingStatItem(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      fontSize = 16.sp,
      fontWeight = FontWeight.Black,
      color = color
    )
    Text(
      text = label,
      fontSize = 10.5.sp,
      color = Color.Gray
    )
  }
}

@Composable
private fun ClosingRuleItem(icon: String, title: String, desc: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(icon, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
      Text(
        text = desc,
        fontSize = 11.sp,
        color = Color(0xFF475569),
        lineHeight = 15.sp
      )
    }
  }
}
