package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.ripple
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.room.RoomBackupSnapshotEntity
import com.example.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoomBackupModal(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  var showCreateDialog by remember { mutableStateOf(false) }
  var newBackupTitle by remember { mutableStateOf("") }
  var newBackupNote by remember { mutableStateOf("") }

  var snapshotToRestore by remember { mutableStateOf<RoomBackupSnapshotEntity?>(null) }
  var snapshotToDelete by remember { mutableStateOf<RoomBackupSnapshotEntity?>(null) }

  var showManualPasteDialog by remember { mutableStateOf(false) }
  var manualJsonText by remember { mutableStateOf("") }

  val clipboardManager = LocalClipboardManager.current

  // Save Backup directly to user storage location (Downloads, Documents, etc.)
  val saveFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    uri?.let {
      viewModel.saveBackupToUri(context, it)
    }
  }

  // File Picker for importing json backup from device storage
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let {
      viewModel.importBackupFromUri(context, it)
    }
  }

  LaunchedEffect(Unit) {
    viewModel.loadRoomBackupData()
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          // Modal Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF28A745).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Storage,
                  contentDescription = null,
                  tint = Color(0xFF28A745)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "💾 النسخ الاحتياطي في Room محلياً",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF1B5E20)
                )
                Text(
                  text = "حماية بيانات الفواتير والعملاء محلياً على ذاكرة الجهاز",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

          // 1. Status Banner Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD4EDDA)))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF28A745),
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "قاعدة بيانات Room نشطة ومتزامنة محلياً على الجهاز",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF155724)
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                StatusItem(
                  title = "الفواتير الحالية",
                  value = "${uiState.savedInvoices.size}",
                  color = Color(0xFF5E258D)
                )
                StatusItem(
                  title = "العملاء والحسابات",
                  value = "${uiState.customers.size}",
                  color = Color(0xFF007BFF)
                )
                StatusItem(
                  title = "نسخ Room الاحتياطية",
                  value = "${uiState.roomSnapshots.size}",
                  color = Color(0xFF28A745)
                )
              }
            }
          }

          // بطاقة إعداد النسخ الاحتياطي التلقائي اليومي (تم إخفاء زر التصدير اليدوي منها لأنه موجود ومنظم بالأسفل)
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (uiState.isAutoDailyBackupEnabled) Color(0xFFF0FDF4) else Color(0xFFF9FAFB)
            ),
            border = BorderStroke(
              1.2.dp,
              if (uiState.isAutoDailyBackupEnabled) Color(0xFF86EFAC) else Color(0xFFE5E7EB)
            )
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
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (uiState.isAutoDailyBackupEnabled) Color(0xFF16A34A) else Color(0xFF6B7280),
                    modifier = Modifier.size(30.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text("🔄", fontSize = 14.sp)
                    }
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = "النسخ الاحتياطي التلقائي اليومي",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = if (uiState.isAutoDailyBackupEnabled) Color(0xFF14532D) else Color(0xFF1F2937)
                      )
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (uiState.isAutoDailyBackupEnabled) Color(0xFFDCFCE7) else Color(0xFFE5E7EB)
                      ) {
                        Text(
                          text = if (uiState.isAutoDailyBackupEnabled) "مُفعل" else "مُعطل",
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (uiState.isAutoDailyBackupEnabled) Color(0xFF15803D) else Color(0xFF4B5563),
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                    Text(
                      text = if (uiState.isAutoDailyBackupEnabled)
                        "يتم حفظ نسخة احتياطية تلقائياً يومياً في ذاكرة الهاتف عند فتح التطبيق"
                      else
                        "قم بتفعيل المفتاح لحفظ نسخة احتياطية يومياً في ذاكرة الهاتف تلقائياً",
                      fontSize = 10.5.sp,
                      color = if (uiState.isAutoDailyBackupEnabled) Color(0xFF166534) else Color(0xFF6B7280)
                    )
                  }
                }
                Switch(
                  checked = uiState.isAutoDailyBackupEnabled,
                  onCheckedChange = { viewModel.setAutoDailyBackupEnabled(it) },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF16A34A),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D5DB)
                  )
                )
              }

              if (uiState.isAutoDailyBackupEnabled && uiState.lastAutoBackupDate.isNotEmpty()) {
                Text(
                  text = "📅 آخر نسخة تلقائية: ${uiState.lastAutoBackupDate}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF15803D)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // ==================== أزرار النسخ الاحتياطي مرتبة ومنظمة بوضوح ====================

          // القسم الأول: إنشاء وتصدير النسخ الاحتياطية
          Text(
            text = "💾 إنشاء وتصدير النسخ الاحتياطية",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 6.dp)
          )

          // شبكة أزرار الإنشاء والتصدير (2x2)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // 1. حفظ نسخة في Room
            BackupActionButtonCard(
              title = "حفظ نسخة في Room",
              subtitle = "نسخة محلية فورية",
              icon = Icons.Default.Backup,
              containerColor = Color(0xFF15803D),
              textColor = Color.White,
              iconColor = Color.White,
              modifier = Modifier.weight(1f),
              onClick = {
                newBackupTitle = ""
                newBackupNote = ""
                showCreateDialog = true
              }
            )

            // 2. تصدير وحفظ ملف بالجهاز
            BackupActionButtonCard(
              title = "حفظ ملف بالجهاز",
              subtitle = "تنزيل ملف JSON",
              icon = Icons.Default.Save,
              containerColor = Color(0xFF0369A1),
              textColor = Color.White,
              iconColor = Color.White,
              modifier = Modifier.weight(1f),
              onClick = {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                saveFileLauncher.launch("Mamlaka_Backup_$timeStamp.json")
              }
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // 3. تصدير ومشاركة
            BackupActionButtonCard(
              title = "مشاركة النسخة",
              subtitle = "واتساب، إيميل، درايف",
              icon = Icons.Default.Share,
              containerColor = Color(0xFF0F766E),
              textColor = Color.White,
              iconColor = Color.White,
              modifier = Modifier.weight(1f),
              onClick = { viewModel.exportAndShareBackup(context) }
            )

            // 4. نسخ كود النسخة JSON
            BackupActionButtonCard(
              title = "نسخ كود النسخة",
              subtitle = "نسخ JSON للحافظة",
              icon = Icons.Default.ContentCopy,
              containerColor = Color(0xFF6D28D9),
              textColor = Color.White,
              iconColor = Color.White,
              modifier = Modifier.weight(1f),
              onClick = {
                val json = viewModel.exportBackup()
                clipboardManager.setText(AnnotatedString(json))
                viewModel.showToast("📋 تم نسخ كود النسخة الاحتياطية إلى الحافظة بنجاح!")
              }
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // القسم الثاني: استيراد واستعادة البيانات
          Text(
            text = "📥 استيراد واستعادة البيانات",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 6.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // 5. استيراد من ملف
            BackupActionButtonCard(
              title = "استيراد من ملف JSON",
              subtitle = "اختيار ملف من الهاتف",
              icon = Icons.Default.FileUpload,
              containerColor = Color(0xFF2563EB),
              textColor = Color.White,
              iconColor = Color.White,
              modifier = Modifier.weight(1f),
              onClick = { filePickerLauncher.launch(arrayOf("application/json", "text/*", "*/*")) }
            )

            // 6. استعادة من كود ملصوق
            BackupActionButtonCard(
              title = "استعادة من كود",
              subtitle = "لصق نص من الحافظة",
              icon = Icons.Default.FileDownload,
              containerColor = Color(0xFF7C3AED),
              textColor = Color.White,
              iconColor = Color.White,
              modifier = Modifier.weight(1f),
              onClick = {
                manualJsonText = ""
                showManualPasteDialog = true
              }
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 3. Snapshots List Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "📋 سجل النسخ الاحتياطية المحفوظة في Room (${uiState.roomSnapshots.size})",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF343A40)
            )
            if (uiState.isRoomOperationInProgress) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // 4. Snapshots List
          if (uiState.roomSnapshots.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(10.dp))
                .padding(20.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "📦 لا توجد نسخ احتياطية محفوظة حالياً في Room",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "اضغط على «➕ نسخة احتياطية جديدة» لإنشاء أول نسخة فوراً لحماية الفواتير والعملاء.",
                  fontSize = 12.sp,
                  color = Color.DarkGray
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(uiState.roomSnapshots, key = { it.id }) { snapshot ->
                SnapshotCard(
                  snapshot = snapshot,
                  onRestore = { snapshotToRestore = snapshot },
                  onDelete = { snapshotToDelete = snapshot }
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog 1: Create New Backup
  if (showCreateDialog) {
    AlertDialog(
      onDismissRequest = { showCreateDialog = false },
      title = { Text("💾 إنشاء نسخة احتياطية محلية في Room", fontWeight = FontWeight.Bold, color = Color(0xFF28A745)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "سيتم حفظ جميع الفواتير الحالية (${uiState.savedInvoices.size}) وحسابات العملاء (${uiState.customers.size}) في قاعدة بيانات Room المحلية بشكل آمن.",
            fontSize = 13.sp,
            color = Color.DarkGray
          )
          OutlinedTextField(
            value = newBackupTitle,
            onValueChange = { newBackupTitle = it },
            label = { Text("اسم النسخة (اختياري)") },
            placeholder = { Text("مثال: نسخة نهاية الشهر / قبل الجرد") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = newBackupNote,
            onValueChange = { newBackupNote = it },
            label = { Text("ملاحظة إضافية (اختياري)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.createLocalRoomBackup(newBackupTitle, newBackupNote)
            showCreateDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
        ) {
          Text("حفظ الآن", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(onClick = { showCreateDialog = false }) {
          Text("إلغاء")
        }
      }
    )
  }

  // Dialog 2: Restore Snapshot Confirm
  snapshotToRestore?.let { snapshot ->
    AlertDialog(
      onDismissRequest = { snapshotToRestore = null },
      title = { Text("⚠️ تأكيد استعادة النسخة الاحتياطية", fontWeight = FontWeight.Bold, color = Color(0xFFDC3545)) },
      text = {
        Text(
          text = "هل أنت متأكد من استعادة النسخة «${snapshot.title}» بتاريخ (${snapshot.timestamp})؟\n\nتحتوي على (${snapshot.invoiceCount}) فاتورة و (${snapshot.customerCount}) حساب عميل.\nسيتم تحديث البيانات الحالية في النظام بمحتوى هذه النسخة.",
          fontSize = 13.sp,
          color = Color.DarkGray
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.restoreFromRoomSnapshot(snapshot)
            snapshotToRestore = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
        ) {
          Text("نعم، استعادة الآن", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(onClick = { snapshotToRestore = null }) {
          Text("إلغاء")
        }
      }
    )
  }

  // Dialog 3: Delete Snapshot Confirm
  snapshotToDelete?.let { snapshot ->
    AlertDialog(
      onDismissRequest = { snapshotToDelete = null },
      title = { Text("🗑️ تأكيد حذف النسخة", fontWeight = FontWeight.Bold, color = Color(0xFFDC3545)) },
      text = {
        Text(
          text = "هل أنت متأكد من حذف النسخة الاحتياطية «${snapshot.title}»؟ لن تتمكن من التراجع عن هذا الإجراء.",
          fontSize = 13.sp,
          color = Color.DarkGray
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteRoomSnapshot(snapshot.id)
            snapshotToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
        ) {
          Text("حذف", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(onClick = { snapshotToDelete = null }) {
          Text("إلغاء")
        }
      }
    )
  }

  // Dialog 4: Manual Paste JSON
  if (showManualPasteDialog) {
    AlertDialog(
      onDismissRequest = { showManualPasteDialog = false },
      title = { Text("📋 استعادة من كود أو نص النسخة", fontWeight = FontWeight.Bold, color = Color(0xFF5E258D)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("الصق نص النسخة الاحتياطية (JSON) هنا للاستعادة الفورية:", fontSize = 12.sp, color = Color.Gray)
          OutlinedButton(
            onClick = {
              val clip = clipboardManager.getText()?.text
              if (!clip.isNullOrBlank()) {
                manualJsonText = clip
                viewModel.showToast("📋 تم لصق النص من الحافظة تلقائياً!")
              } else {
                viewModel.showToast("⚠️ الحافظة فارغة")
              }
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("📋 لصق مباشر من الحافظة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          OutlinedTextField(
            value = manualJsonText,
            onValueChange = { manualJsonText = it },
            placeholder = { Text("الصق بيانات JSON هنا...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (manualJsonText.isNotBlank()) {
              val ok = viewModel.importBackup(manualJsonText)
              if (ok) {
                showManualPasteDialog = false
              }
            } else {
              viewModel.showToast("⚠️ يرجى لصق نص النسخة أولاً")
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
        ) {
          Text("استعادة الآن", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(onClick = { showManualPasteDialog = false }) {
          Text("إلغاء")
        }
      }
    )
  }
}

@Composable
fun StatusItem(title: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
    Text(text = title, fontSize = 11.sp, color = Color.Gray)
  }
}

@Composable
fun SnapshotCard(
  snapshot: RoomBackupSnapshotEntity,
  onRestore: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = snapshot.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212529)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "📅 ${snapshot.timestamp}",
            fontSize = 11.sp,
            color = Color.Gray
          )
          if (snapshot.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "📝 ${snapshot.note}",
              fontSize = 11.sp,
              color = Color(0xFF5E258D)
            )
          }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Button(
            onClick = onRestore,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("استعادة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "حذف",
              tint = Color(0xFFDC3545),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Data tags
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .background(Color(0xFFEDE7F6), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "📄 ${snapshot.invoiceCount} فاتورة",
            fontSize = 11.sp,
            color = Color(0xFF5E258D),
            fontWeight = FontWeight.Bold
          )
        }

        Box(
          modifier = Modifier
            .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "👥 ${snapshot.customerCount} حساب عميل",
            fontSize = 11.sp,
            color = Color(0xFF1976D2),
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun BackupActionButtonCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  containerColor: Color,
  textColor: Color = Color.White,
  iconColor: Color = Color.White,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessHigh)
  )

  Surface(
    modifier = modifier
      .height(52.dp)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clip(RoundedCornerShape(10.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(color = Color.White.copy(alpha = 0.35f)),
        onClick = onClick
      ),
    shape = RoundedCornerShape(10.dp),
    color = containerColor,
    shadowElevation = if (isPressed) 1.dp else 3.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.22f),
        modifier = Modifier.size(32.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = title,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = textColor,
          maxLines = 1
        )
        Text(
          text = subtitle,
          fontSize = 9.5.sp,
          color = textColor.copy(alpha = 0.85f),
          maxLines = 1
        )
      }
    }
  }
}
