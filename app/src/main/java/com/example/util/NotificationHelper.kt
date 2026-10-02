package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.CustomerReminder
import java.net.URLEncoder

object NotificationHelper {
  private const val CHANNEL_ID = "customer_reminders_channel"
  private const val CHANNEL_NAME = "تنبيهات ومواعيد العملاء"
  private const val CHANNEL_DESC = "إشعارات تذكير بمواعيد تسديد الحسابات والأقساط للعملاء"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        enableLights(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun checkAndNotifyDueReminders(context: Context, reminders: List<CustomerReminder>, storeName: String = "المتجر") {
    createNotificationChannel(context)
    val now = System.currentTimeMillis()
    val dueReminders = reminders.filter { !it.isCompleted && it.dueTimestamp <= now }
    if (dueReminders.isEmpty()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!hasPermission) return
    }

    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
      )

      if (dueReminders.size == 1) {
        val rem = dueReminders.first()
        val amountStr = if (rem.amountDue > 0) " بمبلغ ${ArabicNumberHelper.formatAmount(rem.amountDue)} $" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentTitle("⏰ تذكير مستحق: ${rem.customerName}")
          .setContentText("موعد استحقاق (${rem.title})$amountStr للعميل ${rem.customerName}")
          .setStyle(
            NotificationCompat.BigTextStyle()
              .bigText("انتهت مهلة المتابعة المحددة للعميل (${rem.customerName})$amountStr.\nالبيان: ${rem.title}\n${if (rem.note.isNotBlank()) "ملاحظات: ${rem.note}" else ""}")
          )
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true)
          .build()

        NotificationManagerCompat.from(context).notify(rem.id.hashCode(), notification)
      } else {
        val count = dueReminders.size
        val summary = dueReminders.take(3).joinToString(", ") { it.customerName } + if (count > 3) " وغيرهم" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentTitle("🔔 لديك $count تنبيهات ومواعيد مستحقة اليوم!")
          .setContentText("عملاء مستحقون: $summary")
          .setStyle(
            NotificationCompat.BigTextStyle()
              .bigText("لديك $count مواعيد وتنبيهات مستحقة للعملاء اليوم تشمل: $summary.\nاضغط هنا لفتح إدارة التنبيهات ومتابعة السداد.")
          )
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true)
          .build()

        NotificationManagerCompat.from(context).notify(998877, notification)
      }
    } catch (_: Exception) {}
  }

  fun sendWhatsAppReminder(context: Context, reminder: CustomerReminder, storeName: String) {
    val cleanPhone = ArabicNumberHelper.toEngDigits(reminder.customerPhone).replace(Regex("[^0-9+]"), "")
    val amountText = if (reminder.amountDue > 0) " بمبلغ قدره (${ArabicNumberHelper.formatAmount(reminder.amountDue)} $)" else ""
    val message = """
      السلام عليكم ورحمة الله وبركاته،
      الأخ الكريم / ${reminder.customerName} المحترم،
      
      نود تذكيركم بموعد (${reminder.title})$amountText بتاريخ ${reminder.dueDate}.
      ${if (reminder.note.isNotBlank()) "ملاحظة: ${reminder.note}\n" else ""}
      شاكرين ومقدرين حسن تعاونكم الدائم معنا.
      
      — مرسل من: $storeName
    """.trimIndent()

    try {
      val encodedMsg = URLEncoder.encode(message, "UTF-8")
      val formattedPhone = when {
        cleanPhone.startsWith("+") -> cleanPhone.removePrefix("+")
        cleanPhone.startsWith("00") -> cleanPhone.removePrefix("00")
        cleanPhone.length == 9 && (cleanPhone.startsWith("7") || cleanPhone.startsWith("1")) -> "967$cleanPhone"
        else -> cleanPhone
      }

      val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg")
      val waIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(waIntent)
    } catch (e: Exception) {
      // Fallback to general share (SMS or other chat apps)
      try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, message)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(shareIntent, "إرسال التذكير عبر:")
        chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooser)
      } catch (ex: Exception) {
        Toast.makeText(context, "تعذر فتح تطبيق المراسلة: ${ex.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}
