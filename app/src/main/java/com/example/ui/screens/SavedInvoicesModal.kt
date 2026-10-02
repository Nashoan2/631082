package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ArabicNumberHelper

@Composable
fun SavedInvoicesModal(viewModel: InvoiceViewModel, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var showClearConfirm by remember { mutableStateOf(false) }
  var invoiceToDelete by remember { mutableStateOf<com.example.data.InvoiceData?>(null) }
  var invoiceToEditDate by remember { mutableStateOf<com.example.data.InvoiceData?>(null) }

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
        colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "📂 إدارة الفواتير المحفوظة",
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF5E258D)
            )
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

          // Top Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showClearConfirm = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color.White)
              Spacer(modifier = Modifier.width(4.dp))
              Text("🗑️ حذف الكل", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Invoices List
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .verticalScroll(rememberScrollState())
          ) {
            if (uiState.savedInvoices.isEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "لا توجد فواتير محفوظة حتى الآن.",
                  color = Color.Gray,
                  fontWeight = FontWeight.Bold
                )
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.savedInvoices.reversed().forEach { inv ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          "فاتورة رقم ${inv.invNum} (${inv.invType})",
                          fontWeight = FontWeight.ExtraBold,
                          color = Color(0xFF2B5797),
                          fontSize = 14.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            "${ArabicNumberHelper.formatAmount(inv.grandTotal)} ${inv.currency}",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF28A745),
                            fontSize = 14.5.sp
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          IconButton(
                            onClick = { invoiceToDelete = inv },
                            modifier = Modifier.size(32.dp)
                          ) {
                            Icon(
                              Icons.Default.Delete,
                              contentDescription = "حذف الفاتورة",
                              tint = Color(0xFFDC3545),
                              modifier = Modifier.size(18.dp)
                            )
                          }
                        }
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      Text("العميل: ${inv.customerName.ifEmpty { "—" }}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                      Text("التاريخ: ${inv.createdAt}", fontSize = 12.sp, color = Color.Gray)

                      Spacer(modifier = Modifier.height(10.dp))

                      // Action Buttons arranged in 2 responsive rows so no button is cut off
                      Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          // Customer Display Mode (High Clarity)
                          Button(
                            onClick = {
                              viewModel.openCustomerDisplayMode(inv)
                              onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                              .weight(1f)
                              .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                          ) {
                            Text("📱 عرض للعميل", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                          }

                          // View & Print
                          Button(
                            onClick = {
                              viewModel.openInvoiceReport(inv)
                              onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E258D)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                              .weight(1f)
                              .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                          ) {
                            Text("👁️ تقرير الطباعة", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                          }

                          // Export PDF
                          Button(
                            onClick = {
                              viewModel.exportInvoiceToPdf(context, inv)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                              .weight(0.9f)
                              .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                          ) {
                            Text("📄 PDF", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                          }
                        }

                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          // Edit
                          Button(
                            onClick = {
                              viewModel.editSavedInvoice(inv)
                              onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                              .weight(1f)
                              .height(36.dp)
                          ) {
                            Text("✏️ تعديل", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                          }

                          // Edit Date Only
                          Button(
                            onClick = {
                              invoiceToEditDate = inv
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                              .weight(1f)
                              .height(36.dp)
                          ) {
                            Text("📅 التاريخ", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                          }

                          // Delete Button
                          Button(
                            onClick = { invoiceToDelete = inv },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                              .weight(1f)
                              .height(36.dp)
                          ) {
                            Text("🗑️ حذف", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
      text = { Text("هل أنت متأكد من حذف جميع الفواتير المحفوظة؟ لا يمكن التراجع عن هذا الإجراء.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllInvoices()
            showClearConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
        ) {
          Text("نعم، احذف الكل", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(onClick = { showClearConfirm = false }) {
          Text("إلغاء")
        }
      }
    )
  }

  // Dialog تأكيد حذف فاتورة محددة
  invoiceToDelete?.let { inv ->
    AlertDialog(
      onDismissRequest = { invoiceToDelete = null },
      title = {
        Text("⚠️ تأكيد حذف الفاتورة", fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            "هل أنت متأكد من رغبتك في حذف الفاتورة رقم (${inv.invNum})؟",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          if (inv.customerName.isNotBlank()) {
            Text("العميل: ${inv.customerName}", fontSize = 13.sp, color = Color.DarkGray)
          }
          Text(
            "المبلغ: ${ArabicNumberHelper.formatAmount(inv.grandTotal)} ${inv.currency}",
            fontSize = 13.sp,
            color = Color.DarkGray
          )
          Text(
            "تنبيه: لا يمكن التراجع عن هذا الإجراء بعد الحذف.",
            fontSize = 12.sp,
            color = Color(0xFFC62828),
            fontWeight = FontWeight.SemiBold
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteSavedInvoice(inv.id)
            invoiceToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
        ) {
          Text("🗑️ نعم، حذف الفاتورة", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(
          onClick = { invoiceToDelete = null },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C757D))
        ) {
          Text("إلغاء")
        }
      }
    )
  }

  // Edit Invoice Date Dialog
  invoiceToEditDate?.let { inv ->
    var newDateStr by remember { mutableStateOf(inv.createdAt) }

    AlertDialog(
      onDismissRequest = { invoiceToEditDate = null },
      title = { Text("📅 تعديل تاريخ الفاتورة رقم (${inv.invNum})", fontWeight = FontWeight.Bold, color = Color(0xFF5E258D)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("تاريخ الفاتورة الحالي: ${inv.createdAt}", fontSize = 13.sp, color = Color.Gray)
          OutlinedTextField(
            value = newDateStr,
            onValueChange = { newDateStr = it },
            label = { Text("التاريخ الجديد", fontWeight = FontWeight.Bold) },
            placeholder = { Text("dd/MM/yyyy HH:mm") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newDateStr.isNotBlank()) {
              viewModel.updateInvoiceDateOnly(inv.id, newDateStr)
              invoiceToEditDate = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
        ) {
          Text("حفظ التاريخ الجديد", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(onClick = { invoiceToEditDate = null }) {
          Text("إلغاء")
        }
      }
    )
  }
}
