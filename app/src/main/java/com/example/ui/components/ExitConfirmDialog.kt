package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.system.exitProcess

@Composable
fun ExitConfirmDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = Color.White,
      shape = RoundedCornerShape(16.dp),
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(Color(0xFFFFEBEE), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text("🚪", fontSize = 20.sp)
          }
          Column {
            Text(
              text = "الخروج من التطبيق نهائياً",
              fontSize = 17.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFFC62828)
            )
            Text(
              text = "إغلاق التطبيق وإنهاء الجلسة",
              fontSize = 12.sp,
              color = Color(0xFF757575)
            )
          }
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Text(
            text = "هل أنت متأكد من رغبتك في إغلاق التطبيق والخروج منه نهائياً؟",
            fontSize = 14.5.sp,
            color = Color(0xFF374151),
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "سيتم حفظ كافة البيانات والفواتير السابقة بأمان.",
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDismiss()
            val activity = context as? Activity
            activity?.finishAffinity()
            exitProcess(0)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text("🚪", fontSize = 14.sp)
            Text(
              text = "نعم، خروج نهائي",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp
            )
          }
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(
            text = "إلغاء والتراجع",
            color = Color(0xFF4B5563),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp
          )
        }
      }
    )
  }
}
