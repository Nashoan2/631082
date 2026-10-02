package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ExportedPdfInfo
import com.example.util.PrintHelper

@Composable
fun ExportedPdfDialog(
  info: ExportedPdfInfo,
  onPrintRequested: () -> Unit = {},
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    AlertDialog(
      onDismissRequest = onDismiss,
      shape = RoundedCornerShape(16.dp),
      containerColor = Color.White,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Description,
              contentDescription = "PDF",
              tint = Color(0xFF2E7D32),
              modifier = Modifier.size(24.dp)
            )
          }
          Column {
            Text(
              text = "تم تصدير ملف PDF بنجاح",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = Color(0xFF1B5E20)
            )
            Text(
              text = "المستند جاهز للمشاركة والطباعة والحفظ",
              fontSize = 12.sp,
              color = Color.Gray
            )
          }
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = info.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color(0xFF0D47A1)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "اسم الملف: ${info.file.name}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF37474F)
              )
              if (info.sizeFormatted.isNotBlank()) {
                Text(
                  text = "الحجم: ${info.sizeFormatted}",
                  fontSize = 12.sp,
                  color = Color.Gray
                )
              }
            }
          }

          Text(
            text = "اختر الإجراء المطلوب:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF455A64)
          )

          // 1. Share PDF (WhatsApp, Telegram, Mail, Drive, etc.)
          Button(
            onClick = {
              PrintHelper.sharePdf(context, info.file, info.title)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
          ) {
            Icon(
              Icons.Default.Share,
              contentDescription = "مشاركة",
              tint = Color.White,
              modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "📤 مشاركة عبر واتساب والتطبيقات",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          }

          // 2. Open / View PDF
          Button(
            onClick = {
              PrintHelper.openPdf(context, info.file)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070BA)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
          ) {
            Icon(
              Icons.Default.Visibility,
              contentDescription = "عرض",
              tint = Color.White,
              modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "👁️ فتح وعرض ملف PDF",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          }

          // 3. Print
          Button(
            onClick = {
              onDismiss()
              onPrintRequested()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E258D)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
          ) {
            Icon(
              Icons.Default.Print,
              contentDescription = "طباعة",
              tint = Color.White,
              modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "🖨️ طباعة المستند الآن",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          }
        }
      },
      confirmButton = {
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
          Text("إغلاق", fontWeight = FontWeight.Bold, color = Color(0xFF555555))
        }
      }
    )
  }
}
