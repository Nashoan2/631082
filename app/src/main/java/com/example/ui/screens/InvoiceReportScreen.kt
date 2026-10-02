package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InvoiceData
import com.example.data.ReportCustomizationConfig
import com.example.data.StoreConfig
import com.example.ui.components.AlmamlakaLogoBadge
import com.example.util.ArabicNumberHelper
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.compositionLocalOf
import com.example.ui.theme.getReportFontFamily
import com.example.ui.theme.parseHexColor
import com.example.util.PrintHelper
import com.example.util.ScreenshotHelper
import kotlinx.coroutines.launch

val LocalReportBorderColor = compositionLocalOf { Color(0xFF0070BA) }
val LocalReportHeaderColor = compositionLocalOf { Color(0xFF5E258D) }
val LocalReportTextColor = compositionLocalOf { Color(0xFF111111) }
val LocalReportFontScale = compositionLocalOf { 1.0f }

@Composable
fun InvoiceReportScreen(
  invoice: InvoiceData,
  storeConfig: StoreConfig,
  reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
  initialDisplayMode: Boolean = false,
  onToggleTerms: ((Boolean) -> Unit)? = null,
  onExportPdf: (() -> Unit)? = null,
  onOpenCustomerDisplay: (() -> Unit)? = null,
  onOpenClassicReport: (() -> Unit)? = null,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()
  val graphicsLayer = rememberGraphicsLayer()
  var cardBoundsInWindow by remember { mutableStateOf<android.graphics.Rect?>(null) }
  var isCapturingScreenshot by remember { mutableStateOf(false) }
  var isDisplayMode by remember { mutableStateOf(initialDisplayMode) }
  val effectiveReportConfig = reportConfig

  val tableBorderColor = parseHexColor(effectiveReportConfig.tableBorderColorHex, Color(0xFF0070BA))
  val headerColor = parseHexColor(effectiveReportConfig.headerColorHex, Color(0xFF5E258D))
  val primaryTextColor = parseHexColor(effectiveReportConfig.primaryTextColorHex, Color(0xFF111111))
  val fontScale = effectiveReportConfig.fontScale

  val customerNameColor = parseHexColor(effectiveReportConfig.customerNameColorHex, Color(0xFF0070BA))
  val customerAccountColor = parseHexColor(effectiveReportConfig.customerAccountColorHex, Color(0xFFC62828))
  val currencySymbolColor = parseHexColor(effectiveReportConfig.currencySymbolColorHex, primaryTextColor)
  val invoiceAmountColor = parseHexColor(effectiveReportConfig.invoiceAmountColorHex, primaryTextColor)
  val unitPriceColor = parseHexColor(effectiveReportConfig.unitPriceColorHex, invoiceAmountColor)
  val totalValueColor = parseHexColor(effectiveReportConfig.totalValueColorHex, invoiceAmountColor)
  val quantityColor = parseHexColor(effectiveReportConfig.quantityColorHex, primaryTextColor)
  val itemNumberColor = parseHexColor(effectiveReportConfig.itemNumberColorHex, primaryTextColor)
  val grandTotalAmountColor = parseHexColor(effectiveReportConfig.grandTotalAmountColorHex, Color.Black)

  val sym = ArabicNumberHelper.getCurrencySymbol(invoice.currency)
  val currName = ArabicNumberHelper.getCurrencyName(invoice.currency)
  val grandTotal = invoice.grandTotal
  val amountInWords = "${ArabicNumberHelper.numberToArabicWords(grandTotal)} $currName"
  val invDate = if (invoice.createdAt.isNotEmpty()) ArabicNumberHelper.formatTo24HourDateTime(invoice.createdAt) else ArabicNumberHelper.formatDateTime()

  val effectiveStoreNameAr = storeConfig.storeNameAr.ifBlank { "المملكة للإلكترونيات" }
  val effectiveStoreNameEn = storeConfig.storeNameEn.ifBlank { "ALMamlaka Electronics" }
  val effectiveAddressAr = storeConfig.addressAr.ifBlank { "إب شارع تعز" }
  val effectiveAddressEn = storeConfig.addressEn.ifBlank { "YEMEN Ibb" }
  val effectivePhone = storeConfig.phone.ifBlank { "772707736" }

  val reportFont = getReportFontFamily(effectiveReportConfig.fontFamily)

  CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
    LocalReportBorderColor provides tableBorderColor,
    LocalReportHeaderColor provides headerColor,
    LocalReportTextColor provides primaryTextColor,
    LocalReportFontScale provides fontScale,
    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = reportFont)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFEEF1F5))
        .padding(horizontal = 10.dp, vertical = 10.dp)
        .verticalScroll(scrollState),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Navigation / Actions Bar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 840.dp)
          .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Row 1: Print + Export PDF
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { PrintHelper.printInvoice(context, invoice, storeConfig, effectiveReportConfig) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E258D)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
          ) {
            Icon(
              Icons.Default.Print,
              contentDescription = "طباعة",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "طباعة الفاتورة",
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = Color.White,
              maxLines = 1
            )
          }

          Button(
            onClick = {
              if (onExportPdf != null) {
                onExportPdf()
              } else {
                PrintHelper.exportInvoiceToPdf(context, invoice, storeConfig, effectiveReportConfig) { file ->
                  if (file != null) PrintHelper.sharePdf(context, file, "فاتورة رقم ${invoice.invNum}")
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
          ) {
            Icon(
              Icons.Default.Share,
              contentDescription = "تصدير PDF",
              tint = Color.White,
              modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "تصدير PDF",
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = Color.White,
              maxLines = 1
            )
          }
        }

        // Row 2: Mode Switch (Green) + Screenshot + Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Green Mode Toggle Button
          Button(
            onClick = {
              if (isDisplayMode) {
                isDisplayMode = false
                onOpenClassicReport?.invoke()
              } else {
                isDisplayMode = true
                onOpenCustomerDisplay?.invoke()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1.2f)
              .height(42.dp)
          ) {
            Text(
              text = if (isDisplayMode) "الوضع الكلاسيكي" else "وضع العرض 📱",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp,
              color = Color.White,
              maxLines = 1
            )
          }

          // Dedicated Manual Screenshot Button - Invoice Document Only
          Button(
            enabled = !isCapturingScreenshot,
            onClick = {
              isCapturingScreenshot = true
              coroutineScope.launch {
                try {
                  val invoiceBitmap = try {
                    graphicsLayer.toImageBitmap().asAndroidBitmap()
                  } catch (_: Exception) {
                    null
                  }

                  if (invoiceBitmap != null && invoiceBitmap.width > 50 && invoiceBitmap.height > 50) {
                    // Exact Composable capture of the invoice document only
                    ScreenshotHelper.saveBitmapAndNotify(
                      context = context,
                      bitmap = invoiceBitmap,
                      title = "فاتورة رقم ${invoice.invNum}",
                      showToast = true,
                      autoCrop = false,
                      onSuccess = {
                        isCapturingScreenshot = false
                      }
                    )
                  } else if (cardBoundsInWindow != null && cardBoundsInWindow!!.width() > 50 && cardBoundsInWindow!!.height() > 50) {
                    // Window PixelCopy fallback cropped strictly to the invoice card bounds
                    ScreenshotHelper.captureAndSaveScreenshot(
                      context = context,
                      targetRectInWindow = cardBoundsInWindow,
                      title = "فاتورة رقم ${invoice.invNum}",
                      showToast = true,
                      onSuccess = {
                        isCapturingScreenshot = false
                      }
                    )
                  } else {
                    // Fallback to HTML render if graphicsLayer/bounds not yet available
                    val invoiceHtml = PrintHelper.getInvoiceHtml(invoice, storeConfig, effectiveReportConfig, context)
                    ScreenshotHelper.captureHtmlToBitmap(
                      context = context,
                      html = invoiceHtml,
                      title = "فاتورة رقم ${invoice.invNum}",
                      targetWidth = 1080,
                      showToast = true,
                      fallbackBitmap = null,
                      onSuccess = {
                        isCapturingScreenshot = false
                      }
                    )
                  }
                } catch (e: Exception) {
                  isCapturingScreenshot = false
                  Toast.makeText(context, "⚠️ تعذر التقاط الفاتورة: ${e.message}", Toast.LENGTH_SHORT).show()
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(1.05f)
              .height(42.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              if (isCapturingScreenshot) {
                CircularProgressIndicator(
                  modifier = Modifier.size(14.dp),
                  color = Color.White,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "جاري...",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1
                )
              } else {
                Text(
                  text = "📸 لقطة الشاشة",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color.White,
                  maxLines = 1
                )
              }
            }
          }

          // Close Button
          Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF557A8B)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            modifier = Modifier
              .weight(0.85f)
              .height(42.dp)
          ) {
            Text(
              text = "✖ إغلاق",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color.White,
              maxLines = 1
            )
          }
        }
      }

      // Invoice Document Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 840.dp)
          .onGloballyPositioned { coordinates ->
            val b = coordinates.boundsInWindow()
            cardBoundsInWindow = android.graphics.Rect(b.left.toInt(), b.top.toInt(), b.right.toInt(), b.bottom.toInt())
          }
          .drawWithContent {
            graphicsLayer.record {
              this@drawWithContent.drawContent()
            }
            drawLayer(graphicsLayer)
          }
          .border(3.dp, tableBorderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          // WATERMARK (العلامة المائية)
          if (effectiveReportConfig.showWatermark) {
            Box(
              modifier = Modifier
                .matchParentSize()
                .padding(top = 90.dp, bottom = 70.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                if (effectiveReportConfig.showLogo) {
                  AlmamlakaLogoBadge(
                    size = 230.dp,
                    logoBase64 = storeConfig.logoBase64,
                    modifier = Modifier.alpha(0.08f)
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                val watermarkTextAr = if (effectiveReportConfig.customWatermarkText.isNotBlank()) effectiveReportConfig.customWatermarkText else if (storeConfig.wmAr.isNotBlank()) storeConfig.wmAr else effectiveStoreNameAr
                Text(
                  text = watermarkTextAr,
                  fontSize = (24 * fontScale).sp,
                  fontWeight = FontWeight.Black,
                  color = headerColor.copy(alpha = 0.08f)
                )
                Text(
                  text = effectiveStoreNameEn.uppercase(),
                  fontSize = (18 * fontScale).sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 2.sp,
                  color = headerColor.copy(alpha = 0.08f)
                )
              }
            }
          }

          // Main Invoice Foreground Content
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            // Optional Custom Header Title
            if (effectiveReportConfig.customHeaderTitle.isNotBlank()) {
              Text(
                text = effectiveReportConfig.customHeaderTitle,
                fontSize = (16 * fontScale).sp,
                fontWeight = FontWeight.Black,
                color = headerColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 6.dp)
              )
            }

            // Optional Custom Notice Badge
            if (effectiveReportConfig.customNoticeBadge.isNotBlank()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 8.dp)
                  .background(Color(0xFFFFF9E6), RoundedCornerShape(4.dp))
                  .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(4.dp))
                  .padding(vertical = 4.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = effectiveReportConfig.customNoticeBadge,
                  fontSize = (12 * fontScale).sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFB78103),
                  textAlign = TextAlign.Center
                )
              }
            }

            // 1. HEADER: Arabic Store Info (Right) | Logo (Center) | English Store Info (Left)
            if (effectiveReportConfig.showStoreInfo || effectiveReportConfig.showLogo) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Arabic Info (Right side in RTL)
                if (effectiveReportConfig.showStoreInfo) {
                  Column(
                    modifier = Modifier.weight(1.35f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                  ) {
                    Text(
                      text = effectiveStoreNameAr,
                      fontSize = (14.5 * fontScale).sp,
                      fontWeight = FontWeight.Black,
                      color = headerColor,
                      lineHeight = (18 * fontScale).sp,
                      maxLines = 2
                    )
                    Text(
                      text = effectiveAddressAr,
                      fontSize = (11.5 * fontScale).sp,
                      fontWeight = FontWeight.Bold,
                      color = primaryTextColor,
                      lineHeight = (15 * fontScale).sp,
                      maxLines = 1
                    )
                    Text(
                      text = "تلفون : $effectivePhone",
                      fontSize = (12.5 * fontScale).sp,
                      fontWeight = FontWeight.Black,
                      color = headerColor,
                      lineHeight = (16 * fontScale).sp,
                      maxLines = 1
                    )
                  }
                } else {
                  Spacer(modifier = Modifier.weight(1.35f))
                }

                // Circular Logo in Center
                if (effectiveReportConfig.showLogo) {
                  Box(
                    modifier = Modifier.weight(0.7f),
                    contentAlignment = Alignment.Center
                  ) {
                    AlmamlakaLogoBadge(size = 64.dp, logoBase64 = storeConfig.logoBase64)
                  }
                } else {
                  Spacer(modifier = Modifier.weight(0.7f))
                }

                // English Info (Left side in RTL, displayed LTR)
                if (effectiveReportConfig.showStoreInfo) {
                  Box(
                    modifier = Modifier.weight(1.45f),
                    contentAlignment = Alignment.CenterStart
                  ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                      Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                      ) {
                        Text(
                          text = effectiveStoreNameEn,
                          fontSize = (11.5 * fontScale).sp,
                          fontWeight = FontWeight.Black,
                          color = headerColor,
                          lineHeight = (16 * fontScale).sp,
                          maxLines = 2,
                          softWrap = true
                        )
                        Text(
                          text = effectiveAddressEn,
                          fontSize = (11.5 * fontScale).sp,
                          fontWeight = FontWeight.Bold,
                          color = primaryTextColor,
                          lineHeight = (15 * fontScale).sp,
                          maxLines = 1
                        )
                        Text(
                          text = "TEL:$effectivePhone",
                          fontSize = (12.5 * fontScale).sp,
                          fontWeight = FontWeight.Black,
                          color = headerColor,
                          lineHeight = (16 * fontScale).sp,
                          maxLines = 1
                        )
                      }
                    }
                  }
                } else {
                  Spacer(modifier = Modifier.weight(1.45f))
                }
              }

              // Horizontal Divider below header
              HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                thickness = 3.dp,
                color = tableBorderColor
              )
            }

            // Check if cash invoice or credit invoice
            val isCash = invoice.invType.contains("نقد") || invoice.invType.equals("cash", ignoreCase = true)

            // 2. METADATA ROW 1:
            // Cash: Invoice Number (Right) | Badge (Center) | Date (Left)
            // Credit: Account Number (Right) | Badge (Center) | Date (Left)
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isCash) {
                // Cash Invoice: NO account number! Display Invoice Number on the right
                Row(
                  modifier = Modifier.weight(1.3f),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "رقم الفاتورة / ",
                    fontSize = (15 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = headerColor
                  )
                  Text(
                    text = invoice.invNum.trim().ifEmpty { "1" },
                    fontSize = (15.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFC62828)
                  )
                }
              } else {
                // Credit Invoice: Account Number with red value
                Row(
                  modifier = Modifier.weight(1.3f),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "رقم الحساب ",
                    fontSize = (15 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = primaryTextColor
                  )
                  Text(
                    text = invoice.customerAccount.trim().ifEmpty { "—" },
                    fontSize = (15.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = customerAccountColor
                  )
                }
              }

              // Center in RTL: Red Pill Badge (slightly resized as requested)
              Box(
                modifier = Modifier
                  .wrapContentWidth(Alignment.CenterHorizontally)
                  .background(Color(0xFFB71C1C), RoundedCornerShape(5.dp))
                  .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "فاتورة ${invoice.invType.trim().ifEmpty { if (isCash) "نقداً" else "أجل" }}",
                  color = Color.White,
                  fontSize = (12 * fontScale).sp,
                  fontWeight = FontWeight.Black,
                  maxLines = 1
                )
              }

              // Left in RTL: Date
              Text(
                text = "تاريخ / $invDate",
                fontSize = (14 * fontScale).sp,
                fontWeight = FontWeight.Black,
                color = primaryTextColor,
                modifier = Modifier.weight(1.3f),
                textAlign = TextAlign.End,
                maxLines = 1
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. METADATA ROW 2:
            // Cash: Customer Name (Right / Full Width)
            // Credit: Customer Name (Right) | Invoice Number (Left)
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isCash) {
                // Cash Invoice: Customer Name spans across cleanly
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "اسم العميل: ",
                    fontSize = (14.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFB71C1C)
                  )
                  Text(
                    text = invoice.customerName.trim().ifEmpty { "عميل نقدي" },
                    fontSize = (14.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = customerNameColor,
                    maxLines = 1
                  )
                }
              } else {
                // Credit Invoice: Customer Name (Right) | Invoice Number (Left)
                Row(
                  modifier = Modifier.weight(1.6f),
                  horizontalArrangement = Arrangement.Start,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "اسم العميل: ",
                    fontSize = (14.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFB71C1C)
                  )
                  Text(
                    text = invoice.customerName.trim().ifEmpty { "—" },
                    fontSize = (14.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = customerNameColor,
                    maxLines = 1
                  )
                }

                // Left in RTL: Invoice Number (Label in headerColor, Number in red)
                Row(
                  modifier = Modifier.weight(1.1f),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "رقم الفاتورة / ",
                    fontSize = (14.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = headerColor
                  )
                  Text(
                    text = invoice.invNum.trim().ifEmpty { "1" },
                    fontSize = (15.5 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFC62828)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. THE TABLE: Clean proportional columns preventing any broken words
            // RTL column order:
            // 1. القيمة الإجمالية (2.55f) | 2. سعر الوحدة (2.45f) | 3. العدد (1.4f) | 4. التفاصيل (3.1f) | 5. رقم الصنف (0.9f)
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, tableBorderColor)
            ) {
              // Table Header
              val headerBg = if (isDisplayMode) tableBorderColor else Color.White
              val headerTextColor = if (isDisplayMode) Color.White else headerColor

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(IntrinsicSize.Min)
                  .background(headerBg),
                verticalAlignment = Alignment.CenterVertically
              ) {
                TableCell(text = "القيمة الإجمالية", weight = 2.1f, isHeader = true, textColor = headerTextColor, fontSizeSp = 11.5f, singleLine = true)
                TableBorderV()
                TableCell(text = "سعر الوحدة", weight = 2.1f, isHeader = true, textColor = headerTextColor, fontSizeSp = 11.5f, singleLine = true)
                TableBorderV()
                TableCell(text = "العدد", weight = 1.2f, isHeader = true, textColor = headerTextColor, fontSizeSp = 11.5f, singleLine = true)
                TableBorderV()
                TableCell(text = "التفاصيل", weight = 3.9f, isHeader = true, textColor = headerTextColor, fontSizeSp = 12.5f)
                TableBorderV()
                TableCell(text = "الصنف", weight = 1.1f, isHeader = true, textColor = headerTextColor, fontSizeSp = 12f, singleLine = true)
              }
              TableBorderH()

              // Row 1: Main item
              val mainUnitPrice = invoice.price
              val mainTotalPrice = invoice.price
              val mainQtyText = if (invoice.type == "months") {
                val q = ArabicNumberHelper.toEngDigits(invoice.qty.toString().removeSuffix(".0"))
                "$q أشهر"
              } else {
                ArabicNumberHelper.toEngDigits(invoice.qty.toString().removeSuffix(".0"))
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
              ) {
                PriceCell(curr = sym, amount = mainTotalPrice, weight = 2.1f, fontSizeSp = 11.5f, isDisplayMode = isDisplayMode, currColor = currencySymbolColor, amountColor = totalValueColor)
                TableBorderV()
                PriceCell(curr = sym, amount = mainUnitPrice, weight = 2.1f, fontSizeSp = 11.5f, isDisplayMode = isDisplayMode, currColor = currencySymbolColor, amountColor = unitPriceColor)
                TableBorderV()
                TableCell(text = mainQtyText, weight = 1.2f, isBold = true, textColor = quantityColor, fontSizeSp = 11.5f, singleLine = true)
                TableBorderV()
                TableCell(text = invoice.desc.trim().ifEmpty { "تجديد باقة تميز" }, weight = 3.9f, isBold = true, fontSizeSp = 12f)
                TableBorderV()
                TableCell(text = "1", weight = 1.1f, isBold = true, textColor = itemNumberColor, fontSizeSp = 12f, singleLine = true)
              }

              // Additional Items
              invoice.extraItems.forEachIndexed { index, item ->
                TableBorderH()
                val itemNum = index + 2
                val itemTotal = item.price * item.qty
                val itemCurr = ArabicNumberHelper.getCurrencySymbol(item.currency.ifEmpty { invoice.currency })
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  PriceCell(curr = itemCurr, amount = itemTotal, weight = 2.1f, fontSizeSp = 11.5f, isDisplayMode = isDisplayMode, currColor = currencySymbolColor, amountColor = totalValueColor)
                  TableBorderV()
                  PriceCell(curr = itemCurr, amount = item.price, weight = 2.1f, fontSizeSp = 11.5f, isDisplayMode = isDisplayMode, currColor = currencySymbolColor, amountColor = unitPriceColor)
                  TableBorderV()
                  TableCell(text = ArabicNumberHelper.toEngDigits(item.qty.toString().removeSuffix(".0")), weight = 1.2f, isBold = true, textColor = quantityColor, fontSizeSp = 11.5f, singleLine = true)
                  TableBorderV()
                  TableCell(text = item.description.ifEmpty { "—" }, weight = 3.9f, isBold = true, fontSizeSp = 12f)
                  TableBorderV()
                  TableCell(text = itemNum.toString(), weight = 1.1f, isBold = true, textColor = itemNumberColor, fontSizeSp = 12f, singleLine = true)
                }
              }

              // Table Border before Total Row
              TableBorderH()

              // Total Row (RTL: Grand Total under القيمة+سعر الوحدة (4.2f) | Words under العدد+التفاصيل (5.1f) | Total under الصنف (1.1f))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(IntrinsicSize.Min)
                  .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // 1. Grand Total covering columns القيمة الإجمالية (2.1f) + سعر الوحدة (2.1f) = 4.2f
                if (isDisplayMode) {
                  // Display Mode: Solid green banner for "الإجمالي" with crisp white text
                  Column(
                    modifier = Modifier
                      .weight(4.2f)
                      .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E7D32))
                        .padding(vertical = 3.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "الإجمالي",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                      )
                    }
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        val formattedGrandTotal = remember(grandTotal) { ArabicNumberHelper.formatAmount(grandTotal) }
                        val totalLen = sym.length + formattedGrandTotal.length
                        val totalNumberFontSize = when {
                          totalLen > 15 -> 12f
                          totalLen > 12 -> 13.5f
                          else -> 14.5f
                        }
                        val totalSymFontSize = when {
                          totalLen > 15 -> 11f
                          totalLen > 12 -> 12.5f
                          else -> 13.5f
                        }
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.Center
                        ) {
                          Text(
                            text = sym,
                            fontSize = totalSymFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = currencySymbolColor
                          )
                          Spacer(modifier = Modifier.width(3.dp))
                          Text(
                            text = formattedGrandTotal,
                            fontSize = totalNumberFontSize.sp,
                            fontWeight = FontWeight.Black,
                            color = grandTotalAmountColor,
                            maxLines = 1,
                            softWrap = false
                          )
                        }
                      }
                    }
                  }
                } else {
                  // Classic Mode: Green text "الإجمالي" on white background
                  Column(
                    modifier = Modifier
                      .weight(4.2f)
                      .fillMaxHeight()
                      .padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Text(
                      text = "الإجمالي",
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color(0xFF2E7D32),
                      textAlign = TextAlign.Center
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                      val formattedGrandTotal = remember(grandTotal) { ArabicNumberHelper.formatAmount(grandTotal) }
                      val totalLen = sym.length + formattedGrandTotal.length
                      val totalNumberFontSize = when {
                        totalLen > 15 -> 12f
                        totalLen > 12 -> 13.5f
                        else -> 14.5f
                      } * fontScale
                      val totalSymFontSize = when {
                        totalLen > 15 -> 11f
                        totalLen > 12 -> 12.5f
                        else -> 13.5f
                      } * fontScale
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                      ) {
                        Text(
                          text = sym,
                          fontSize = totalSymFontSize.sp,
                          fontWeight = FontWeight.Bold,
                          color = currencySymbolColor
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                          text = formattedGrandTotal,
                          fontSize = totalNumberFontSize.sp,
                          fontWeight = FontWeight.Black,
                          color = grandTotalAmountColor,
                          maxLines = 1,
                          softWrap = false
                        )
                      }
                    }
                  }
                }
                TableBorderV()

                // 2. Amount in Words covering columns العدد (1.2f) + التفاصيل (3.9f) = 5.1f
                TableCell(
                  text = if (effectiveReportConfig.showAmountInWords) amountInWords else "",
                  weight = 5.1f,
                  isBold = true,
                  fontSizeSp = 11.5f
                )
                TableBorderV()

                // 3. Total Label under عمود الصنف (1.1f)
                TableCell(
                  text = "Total",
                  weight = 1.1f,
                  isBold = true,
                  textColor = headerColor,
                  fontSizeSp = 12f,
                  singleLine = true
                )
              }
            }

            // 5. SUBSCRIPTION / DEVICE BOX
            if (effectiveReportConfig.showCardSubscriptionBox) {
              Spacer(modifier = Modifier.height(12.dp))

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .border(3.dp, tableBorderColor, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp)
                ) {
                  // Subtle background watermark in subscription card
                  Text(
                    text = "ALMAMLAK ELECTRONICS",
                    fontSize = (19 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = headerColor.copy(alpha = 0.08f),
                    modifier = Modifier.align(Alignment.Center)
                  )

                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    // Top row labels with | divider
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceAround,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      // Right: Account/Subscription Number
                      Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        if (isDisplayMode) {
                          Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = headerColor,
                            modifier = Modifier.size(20.dp)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                          text = "رقم الاشتراك",
                          fontSize = (15 * fontScale).sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = headerColor,
                          textAlign = TextAlign.Center
                        )
                      }

                      Text(
                        text = "|",
                        fontSize = (20 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90A4AE),
                        modifier = Modifier.padding(horizontal = 8.dp)
                      )

                      // Left: End Date
                      Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        if (isDisplayMode) {
                          Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = headerColor,
                            modifier = Modifier.size(19.dp)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                          text = "تاريخ انتهاء الاشتراك",
                          fontSize = (15 * fontScale).sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = headerColor,
                          textAlign = TextAlign.Center
                        )
                      }
                    }

                    // Bottom row numbers in Bold Red
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceAround,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = invoice.cardId.trim().ifEmpty { "—" },
                        fontSize = (17 * fontScale).sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                      )
                      Spacer(modifier = Modifier.width(16.dp))
                      Text(
                        text = invoice.endDate.trim().ifEmpty { "—" },
                        fontSize = (17 * fontScale).sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                      )
                    }
                  }
                }
              }
            }

            // 6. FOOTER: Terms & Notes (controlled by report customization)
            if (effectiveReportConfig.showTermsAndNotes) {
              val defaultTerms = "• البضاعة المباعة لا ترد ولا تستبدل بعد خروجها من المحل.\n• استلمت البضاعة الموضحة أعلاه كاملة ، سليمة ، ولعدد ذلك."
              val termsText = storeConfig.terms.trim().ifEmpty { defaultTerms }
              val linesToDisplay = termsText.lines().map { it.trim() }.filter { it.isNotEmpty() }

              if (linesToDisplay.isNotEmpty()) {
                if (isDisplayMode) {
                  // Display Mode (الصورة رقم 2): Light-blue tinted container
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0F7FD),
                    border = BorderStroke(1.5.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(top = 10.dp)
                  ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                      Column(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.Start
                      ) {
                        linesToDisplay.forEachIndexed { index, line ->
                          val cleanLine = line.removePrefix("•").removePrefix("▪").removePrefix("-").trim()
                          Row(
                            modifier = Modifier
                              .fillMaxWidth()
                              .padding(vertical = 1.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                          ) {
                            Text(
                              text = "▪",
                              fontSize = (11.5 * fontScale).sp,
                              fontWeight = FontWeight.Black,
                              color = tableBorderColor,
                              modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                            )
                            Text(
                              text = cleanLine,
                              fontSize = (11.5 * fontScale).sp,
                              fontWeight = FontWeight.Bold,
                              color = primaryTextColor,
                              textAlign = TextAlign.Right,
                              modifier = Modifier.fillMaxWidth()
                            )
                          }
                        }
                      }
                    }
                  }
                } else {
                  // Classic Mode (الصورة رقم 1): Clean bullet points on white background
                  Spacer(modifier = Modifier.height(8.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, start = 8.dp, end = 8.dp),
                      horizontalAlignment = Alignment.Start
                    ) {
                      linesToDisplay.forEach { line ->
                        val cleanLine = line.removePrefix("•").removePrefix("▪").removePrefix("-").trim()
                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.5.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.Start
                        ) {
                          Text(
                            text = "▪",
                            fontSize = (11.5 * fontScale).sp,
                            fontWeight = FontWeight.Black,
                            color = tableBorderColor,
                            modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                          )
                          Text(
                            text = cleanLine,
                            fontSize = (11.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            // Optional Custom Footer Text
            if (effectiveReportConfig.customFooterText.isNotBlank()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = effectiveReportConfig.customFooterText,
                fontSize = (12 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
              )
            }

            // Signatures Section (خانات التوقيع)
            if (effectiveReportConfig.showSignatures) {
              Spacer(modifier = Modifier.height(14.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "توقيع المحاسب / أمين الصندوق",
                    fontSize = (12 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                  )
                  if (effectiveReportConfig.accountantSignatureName.isNotBlank()) {
                    Text(
                      text = "(${effectiveReportConfig.accountantSignatureName})",
                      fontSize = (11 * fontScale).sp,
                      fontWeight = FontWeight.Bold,
                      color = primaryTextColor
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = ".......................",
                    fontSize = (12 * fontScale).sp,
                    color = Color.Gray
                  )
                }

                if (effectiveReportConfig.showStampSeal) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                    modifier = Modifier.padding(horizontal = 6.dp)
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(
                        text = "★ معتمد رسمياً ★",
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC62828)
                      )
                      Text(
                        text = "APPROVED",
                        fontSize = (9 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                      )
                    }
                  }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "توقيع المستلم / العميل",
                    fontSize = (12 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                  )
                  if (effectiveReportConfig.managerSignatureName.isNotBlank()) {
                    Text(
                      text = "اعتماد: ${effectiveReportConfig.managerSignatureName}",
                      fontSize = (11 * fontScale).sp,
                      fontWeight = FontWeight.Bold,
                      color = primaryTextColor
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = ".......................",
                    fontSize = (12 * fontScale).sp,
                    color = Color.Gray
                  )
                }
              }
            } else if (effectiveReportConfig.showStampSeal) {
              Spacer(modifier = Modifier.height(10.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE),
                border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
              ) {
                Text(
                  text = "★ معتمد رسمياً APPROVED ★",
                  fontSize = (11.5 * fontScale).sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFFC62828),
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
}

fun calculateAutoShrinkFontSize(text: String, baseFontSizeSp: Float, isSingleLine: Boolean = false): Float {
  if (text.isBlank()) return baseFontSizeSp
  val len = text.length
  return if (isSingleLine) {
    when {
      len > 24 -> (baseFontSizeSp * 0.65f).coerceAtLeast(7.5f)
      len > 18 -> (baseFontSizeSp * 0.75f).coerceAtLeast(8.0f)
      len > 13 -> (baseFontSizeSp * 0.85f).coerceAtLeast(8.5f)
      len > 9 -> (baseFontSizeSp * 0.92f).coerceAtLeast(9.5f)
      else -> baseFontSizeSp
    }
  } else {
    when {
      len > 68 -> (baseFontSizeSp * 0.65f).coerceAtLeast(8.0f)
      len > 50 -> (baseFontSizeSp * 0.74f).coerceAtLeast(8.8f)
      len > 35 -> (baseFontSizeSp * 0.84f).coerceAtLeast(9.8f)
      len > 22 -> (baseFontSizeSp * 0.92f).coerceAtLeast(10.8f)
      else -> baseFontSizeSp
    }
  }
}

@Composable
fun RowScope.PriceCell(
  curr: String,
  amount: Double,
  weight: Float,
  fontSizeSp: Float = 14f,
  isDisplayMode: Boolean = false,
  currColor: Color = Color.Unspecified,
  amountColor: Color = Color.Unspecified
) {
  val fontScale = LocalReportFontScale.current
  val primaryText = LocalReportTextColor.current
  val effectiveCurrColor = if (currColor != Color.Unspecified) currColor else primaryText
  val effectiveAmountColor = if (amountColor != Color.Unspecified) amountColor else primaryText
  val scaledFontSizeSp = fontSizeSp * fontScale
  val formatted = remember(amount) { ArabicNumberHelper.formatAmount(amount) }
  val combinedLen = curr.length + formatted.length
  val effectiveFontSize = remember(combinedLen, scaledFontSizeSp) {
    when {
      combinedLen > 14 -> (scaledFontSizeSp * 0.72f).coerceAtLeast(8.5f)
      combinedLen > 11 -> (scaledFontSizeSp * 0.82f).coerceAtLeast(9.5f)
      combinedLen > 9 -> (scaledFontSizeSp * 0.90f).coerceAtLeast(10.5f)
      else -> scaledFontSizeSp
    }
  }

  Box(
    modifier = Modifier
      .weight(weight)
      .fillMaxHeight()
      .padding(horizontal = 2.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    // العملة والمبلغ في سطر واحد أفقي بتناسق تام: رمز العملة أولاً ثم المبلغ
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = curr,
          fontSize = (effectiveFontSize - 0.5f).sp,
          fontWeight = FontWeight.Bold,
          color = effectiveCurrColor,
          maxLines = 1,
          softWrap = false
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = formatted,
          fontSize = effectiveFontSize.sp,
          fontWeight = FontWeight.Black,
          color = effectiveAmountColor,
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

@Composable
fun RowScope.TableCell(
  text: String,
  weight: Float,
  isHeader: Boolean = false,
  isBold: Boolean = false,
  textColor: Color = Color.Unspecified,
  fontSizeSp: Float = 12f,
  singleLine: Boolean = false
) {
  val fontScale = LocalReportFontScale.current
  val scaledBase = fontSizeSp * fontScale
  val baseCalculated = remember(text, scaledBase, singleLine) {
    calculateAutoShrinkFontSize(text = text, baseFontSizeSp = scaledBase, isSingleLine = singleLine)
  }
  var currentFontSize by remember(text, baseCalculated) { mutableFloatStateOf(baseCalculated) }

  val headerColor = LocalReportHeaderColor.current
  val primaryText = LocalReportTextColor.current

  val effectiveColor = if (isHeader && textColor == Color.Unspecified) {
    headerColor
  } else if (textColor != Color.Unspecified) {
    textColor
  } else {
    primaryText
  }

  Box(
    modifier = Modifier
      .weight(weight)
      .fillMaxHeight()
      .padding(vertical = 5.dp, horizontal = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontSize = currentFontSize.sp,
      fontWeight = if (isHeader || isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
      color = effectiveColor,
      textAlign = TextAlign.Center,
      maxLines = if (singleLine) 1 else Int.MAX_VALUE,
      softWrap = !singleLine,
      lineHeight = (currentFontSize + 3.2f).sp,
      onTextLayout = { result ->
        if (singleLine && result.hasVisualOverflow && currentFontSize > 7.5f) {
          currentFontSize = (currentFontSize - 0.75f).coerceAtLeast(7f)
        }
      }
    )
  }
}

@Composable
fun TableBorderV() {
  val borderColor = LocalReportBorderColor.current
  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(2.dp)
      .background(borderColor)
  )
}

@Composable
fun TableBorderH() {
  val borderColor = LocalReportBorderColor.current
  HorizontalDivider(thickness = 2.dp, color = borderColor)
}
