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
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.data.ExchangeRates
import com.example.data.ReportCustomizationConfig
import com.example.data.StoreConfig
import com.example.ui.components.AlmamlakaLogoBadge
import androidx.compose.material3.LocalTextStyle
import com.example.ui.theme.getReportFontFamily
import com.example.ui.theme.parseHexColor
import com.example.util.ArabicNumberHelper
import com.example.util.PrintHelper
import com.example.util.ScreenshotHelper
import kotlinx.coroutines.launch

@Composable
fun CustomerStatementScreen(
  customer: Customer,
  storeConfig: StoreConfig,
  reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
  exchangeRates: ExchangeRates = ExchangeRates(),
  startDateStr: String = "",
  endDateStr: String = "",
  onChangePeriod: () -> Unit = {},
  onExportPdf: (() -> Unit)? = null,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val verticalScrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()
  val graphicsLayer = rememberGraphicsLayer()
  var statementBoundsInWindow by remember { mutableStateOf<android.graphics.Rect?>(null) }
  var isCapturingScreenshot by remember { mutableStateOf(false) }

  val dateStr = ArabicNumberHelper.formatReportDateTime()

  // Filter transactions by date range
  val sDate = if (startDateStr.isNotBlank()) ArabicNumberHelper.parseDate(startDateStr) else null
  val eDate = if (endDateStr.isNotBlank()) ArabicNumberHelper.parseDate(endDateStr) else null

  val startCal = sDate?.let {
    java.util.Calendar.getInstance().apply {
      time = it
      set(java.util.Calendar.HOUR_OF_DAY, 0)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
  }
  val endCal = eDate?.let {
    java.util.Calendar.getInstance().apply {
      time = it
      set(java.util.Calendar.HOUR_OF_DAY, 23)
      set(java.util.Calendar.MINUTE, 59)
      set(java.util.Calendar.SECOND, 59)
      set(java.util.Calendar.MILLISECOND, 999)
    }
  }

  val filteredTransactions = customer.transactions.filter { t ->
    if (startCal == null && endCal == null) return@filter true
    val tDate = ArabicNumberHelper.parseDate(t.date)
    if (tDate != null) {
      val inAfterStart = startCal == null || !tDate.before(startCal.time)
      val inBeforeEnd = endCal == null || !tDate.after(endCal.time)
      inAfterStart && inBeforeEnd
    } else {
      true
    }
  }

  // Base currency for multi-currency statement reconciliation
  val baseCurrency = customer.transactions.firstOrNull { it.currency.isNotBlank() }?.currency
    ?: filteredTransactions.firstOrNull { it.currency.isNotBlank() }?.currency
    ?: "USD"

  val convertedTotalDebit = filteredTransactions.filter {
    it.type == "صرف" || it.type == "فاتورة" || (it.type == "افتتاح" && it.amount > 0)
  }.sumOf {
    val curr = if (it.currency.isNotBlank()) it.currency else baseCurrency
    ArabicNumberHelper.convertCurrency(it.amount, curr, baseCurrency, exchangeRates)
  }

  val convertedTotalCredit = filteredTransactions.filter {
    it.type == "قبض" || (it.type == "افتتاح" && it.amount < 0)
  }.sumOf {
    val amt = if (it.type == "افتتاح") Math.abs(it.amount) else it.amount
    val curr = if (it.currency.isNotBlank()) it.currency else baseCurrency
    ArabicNumberHelper.convertCurrency(amt, curr, baseCurrency, exchangeRates)
  }

  val rawNetBalance = convertedTotalDebit - convertedTotalCredit
  val finalBalance = if (Math.abs(rawNetBalance) < 0.005) 0.0 else rawNetBalance

  var runningBalCalc = 0.0
  val transactionRunningBalances = filteredTransactions.associateWith { t ->
    val curr = if (t.currency.isNotBlank()) t.currency else baseCurrency
    val converted = ArabicNumberHelper.convertCurrency(t.amount, curr, baseCurrency, exchangeRates)
    when (t.type) {
      "قبض" -> runningBalCalc -= converted
      "صرف", "فاتورة" -> runningBalCalc += converted
      "افتتاح" -> if (t.amount < 0) runningBalCalc -= Math.abs(converted) else runningBalCalc += converted
      else -> runningBalCalc += converted
    }
    if (Math.abs(runningBalCalc) < 0.005) 0.0 else runningBalCalc
  }

  val totalDebit = filteredTransactions.filter {
    it.type == "صرف" || it.type == "فاتورة" || (it.type == "افتتاح" && it.amount > 0)
  }.sumOf { it.amount }

  val totalCredit = filteredTransactions.filter {
    it.type == "قبض" || (it.type == "افتتاح" && it.amount < 0)
  }.sumOf { if (it.type == "افتتاح") Math.abs(it.amount) else it.amount }

  val primaryBlue = parseHexColor(reportConfig.tableBorderColorHex, Color(0xFF0070BA))
  val textBlue = parseHexColor(reportConfig.tableBorderColorHex, Color(0xFF0288D1))
  val purpleBrand = parseHexColor(reportConfig.headerColorHex, Color(0xFF1A237E))
  val primaryText = parseHexColor(reportConfig.primaryTextColorHex, Color(0xFF111111))
  val customerNameColor = parseHexColor(reportConfig.customerNameColorHex, Color(0xFFC62828))
  val customerAccountColor = parseHexColor(reportConfig.customerAccountColorHex, Color(0xFFC62828))
  val creditGreen = Color(0xFF2E7D32)
  val debitRed = Color(0xFFD32F2F)
  val fontScale = reportConfig.fontScale
  val reportFont = getReportFontFamily(reportConfig.fontFamily)

  CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = reportFont)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F3F6))
        .verticalScroll(verticalScrollState)
        .padding(horizontal = 8.dp, vertical = 10.dp),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 840.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // TOP ACTION BAR CARD - PREVENTS ANY SQUEEZING OR OVERFLOW
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Row 1: Primary Actions (Print & Export PDF)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  PrintHelper.printStatement(
                    context = context,
                    customer = customer,
                    storeConfig = storeConfig,
                    startDateStr = startDateStr,
                    endDateStr = endDateStr,
                    reportConfig = reportConfig,
                    exchangeRates = exchangeRates
                  )
                },
                colors = ButtonDefaults.buttonColors(containerColor = purpleBrand),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1.1f)
                  .height(42.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "طباعة",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                  )
                }
              }

              Button(
                onClick = {
                  if (onExportPdf != null) {
                    onExportPdf()
                  } else {
                    PrintHelper.exportStatementToPdf(context, customer, storeConfig, startDateStr, endDateStr, reportConfig, exchangeRates) { file ->
                      if (file != null) PrintHelper.sharePdf(context, file, "كشف حساب: ${customer.name}")
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "تصدير PDF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                  )
                }
              }

              // Dedicated Manual Screenshot Button - Statement Document Only
              Button(
                enabled = !isCapturingScreenshot,
                onClick = {
                  isCapturingScreenshot = true
                  coroutineScope.launch {
                    try {
                      // Direct native Compose capture - ultra crisp, vector typography, exact screen preview
                      val statementBitmap = try {
                        graphicsLayer.toImageBitmap().asAndroidBitmap()
                      } catch (_: Exception) {
                        null
                      }

                      if (statementBitmap != null && statementBitmap.width > 50 && statementBitmap.height > 50) {
                        // Exact Composable capture of the statement document only
                        ScreenshotHelper.saveBitmapAndNotify(
                          context = context,
                          bitmap = statementBitmap,
                          title = "كشف حساب: ${customer.name}",
                          showToast = true,
                          autoCrop = false,
                          onSuccess = {
                            isCapturingScreenshot = false
                          }
                        )
                      } else if (statementBoundsInWindow != null && statementBoundsInWindow!!.width() > 50 && statementBoundsInWindow!!.height() > 50) {
                        // Window PixelCopy fallback cropped strictly to the statement card bounds
                        ScreenshotHelper.captureAndSaveScreenshot(
                          context = context,
                          targetRectInWindow = statementBoundsInWindow,
                          title = "كشف حساب: ${customer.name}",
                          showToast = true,
                          onSuccess = {
                            isCapturingScreenshot = false
                          }
                        )
                      } else {
                        // Fallback to HTML render if graphicsLayer/bounds not yet available
                        val statementHtml = PrintHelper.getStatementHtml(
                          customer = customer,
                          storeConfig = storeConfig,
                          startDateStr = startDateStr,
                          endDateStr = endDateStr,
                          reportConfig = reportConfig,
                          context = context,
                          exchangeRates = exchangeRates
                        )
                        ScreenshotHelper.captureHtmlToBitmap(
                          context = context,
                          html = statementHtml,
                          title = "كشف حساب: ${customer.name}",
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
                      Toast.makeText(context, "⚠️ تعذر التقاط كشف الحساب: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  if (isCapturingScreenshot) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(16.dp),
                      color = Color.White,
                      strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "جاري الحفظ...",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      maxLines = 1
                    )
                  } else {
                    Text(
                      text = "📸 لقطة شاشة",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      maxLines = 1
                    )
                  }
                }
              }
            }

            // Row 2: Secondary Controls (Change Period, Close)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Button(
                onClick = onChangePeriod,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17A2B8)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1.2f)
                  .height(40.dp)
              ) {
                Text(
                  text = "📅 تغيير الفترة",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1
                )
              }

              Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF557A8B)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(0.8f)
                  .height(40.dp)
              ) {
                Text(
                  text = "✖ إغلاق",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1
                )
              }
            }
          }
        }

        // STATEMENT CARD - FITS SCREEN OR EXPANDS CRISPLY (Isolated for Clean Capture)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
              graphicsLayer.record {
                this@drawWithContent.drawContent()
              }
              drawLayer(graphicsLayer)
            }
            .onGloballyPositioned { coordinates ->
              val b = coordinates.boundsInWindow()
              statementBoundsInWindow = android.graphics.Rect(
                b.left.toInt(),
                b.top.toInt(),
                b.right.toInt(),
                b.bottom.toInt()
              )
            }
            .border(3.5.dp, primaryBlue, RoundedCornerShape(12.dp)),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            // Watermark (العلامة المائية) in Statement
            if (reportConfig.showWatermark) {
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
                  if (reportConfig.showLogo) {
                    AlmamlakaLogoBadge(
                      size = 180.dp,
                      logoBase64 = storeConfig.logoBase64,
                      modifier = Modifier.alpha(0.08f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                  }
                  val watermarkTextAr = if (reportConfig.customWatermarkText.isNotBlank()) reportConfig.customWatermarkText else if (storeConfig.wmAr.isNotBlank()) storeConfig.wmAr else storeConfig.storeNameAr.ifEmpty { "المملكة للإلكترونيات" }
                  Text(
                    text = watermarkTextAr,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = purpleBrand.copy(alpha = 0.08f)
                  )
                  val storeNameEn = storeConfig.storeNameEn.ifEmpty { "ALMAMLAK ELECTRONICS" }
                  Text(
                    text = storeNameEn.uppercase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = purpleBrand.copy(alpha = 0.08f)
                  )
                }
              }
            }

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 20.dp)
            ) {
              // Optional Custom Header Title
              if (reportConfig.customHeaderTitle.isNotBlank()) {
                Text(
                  text = reportConfig.customHeaderTitle,
                  fontSize = (16 * reportConfig.fontScale).sp,
                  fontWeight = FontWeight.Black,
                  color = purpleBrand,
                  textAlign = TextAlign.Center,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                )
              }

              // Optional Custom Notice Badge
              if (reportConfig.customNoticeBadge.isNotBlank()) {
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
                    text = reportConfig.customNoticeBadge,
                    fontSize = (11.5 * reportConfig.fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB78103),
                    textAlign = TextAlign.Center
                  )
                }
              }

              // HEADER: Arabic Info (Right) | Circle Logo (Center) | English Info (Left)
              if (reportConfig.showStoreInfo || reportConfig.showLogo) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  val effectiveStoreNameAr = if (storeConfig.storeNameAr.isBlank()) "المملكة للإلكترونيات" else storeConfig.storeNameAr
                  val effectiveStoreNameEn = if (storeConfig.storeNameEn.isBlank() || storeConfig.storeNameEn.contains("almamlak", ignoreCase = true)) "ALMamlaka Electronics" else storeConfig.storeNameEn
                  val effectiveAddressAr = if (storeConfig.addressAr.isBlank() || storeConfig.addressAr.contains("معين")) "إب شارع تعز" else storeConfig.addressAr
                  val effectiveAddressEn = if (storeConfig.addressEn.isBlank()) "YEMEN Ibb" else storeConfig.addressEn
                  val effectivePhone = if (storeConfig.phone.isBlank()) "772707736" else storeConfig.phone

                  // 1. Right side (Arabic Info)
                  if (reportConfig.showStoreInfo) {
                    Column(
                      modifier = Modifier.weight(1.3f),
                      horizontalAlignment = Alignment.Start
                    ) {
                      Text(
                        text = effectiveStoreNameAr,
                        fontSize = (13.5 * reportConfig.fontScale).sp,
                        fontWeight = FontWeight.Black,
                        color = purpleBrand,
                        maxLines = 1,
                        softWrap = false
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = effectiveAddressAr,
                        fontSize = (11 * reportConfig.fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        maxLines = 1,
                        softWrap = false
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = "تلفون / $effectivePhone",
                        fontSize = (11.5 * reportConfig.fontScale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = purpleBrand,
                        maxLines = 1,
                        softWrap = false
                      )
                      if (reportConfig.showBranch && storeConfig.branch.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = "الفرع / ${storeConfig.branch}",
                          fontSize = (11 * reportConfig.fontScale).sp,
                          fontWeight = FontWeight.Bold,
                          color = purpleBrand,
                          maxLines = 1,
                          softWrap = false
                        )
                      }
                      if (reportConfig.taxOrCrNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = "الرقم الضريبي/السجل: ${reportConfig.taxOrCrNumber}",
                          fontSize = (10.5 * reportConfig.fontScale).sp,
                          fontWeight = FontWeight.Bold,
                          color = primaryText,
                          maxLines = 1,
                          softWrap = false
                        )
                      }
                    }
                  } else {
                    Spacer(modifier = Modifier.weight(1.3f))
                  }

                  // 2. Center: Circular Logo
                  if (reportConfig.showLogo) {
                    Box(
                      modifier = Modifier.weight(0.7f),
                      contentAlignment = Alignment.Center
                    ) {
                      AlmamlakaLogoBadge(size = 64.dp, logoBase64 = storeConfig.logoBase64)
                    }
                  } else {
                    Spacer(modifier = Modifier.weight(0.7f))
                  }

                  // 3. Left side (English Info LTR)
                  if (reportConfig.showStoreInfo) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                      Column(
                        modifier = Modifier.weight(1.3f),
                        horizontalAlignment = Alignment.Start
                      ) {
                        Text(
                          text = effectiveStoreNameEn,
                          fontSize = (13.5 * reportConfig.fontScale).sp,
                          fontWeight = FontWeight.Black,
                          color = purpleBrand,
                          textAlign = TextAlign.Start,
                          maxLines = 1,
                          softWrap = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = effectiveAddressEn,
                          fontSize = (11 * reportConfig.fontScale).sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF333333),
                          textAlign = TextAlign.Start,
                          maxLines = 1,
                          softWrap = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = "TEL/ $effectivePhone",
                          fontSize = (11.5 * reportConfig.fontScale).sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = purpleBrand,
                          textAlign = TextAlign.Start,
                          maxLines = 1,
                          softWrap = false
                        )
                      }
                    }
                  } else {
                    Spacer(modifier = Modifier.weight(1.3f))
                  }
                }

                // BLUE DIVIDER UNDER HEADER
                HorizontalDivider(
                  modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                  thickness = 3.dp,
                  color = primaryBlue
                )
              }

            // METADATA ROW 1: Account Number (Right) | Purple Badge (Center) | Report Date (Left)
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (reportConfig.showCustomerAccountNumber) {
                Text(
                  text = buildAnnotatedString {
                    append("رقم الحساب / ")
                    withStyle(SpanStyle(color = customerAccountColor, fontWeight = FontWeight.Black)) {
                      append(customer.accountNumber)
                    }
                  },
                  fontSize = (12.5 * reportConfig.fontScale).sp,
                  fontWeight = FontWeight.Black,
                  color = primaryText,
                  textAlign = TextAlign.Start,
                  modifier = Modifier.weight(1f),
                  maxLines = 1
                )
              } else {
                Spacer(modifier = Modifier.weight(1f))
              }

              // Purple Badge in the Center: unified with invoice badge
              Box(
                modifier = Modifier
                  .wrapContentWidth(Alignment.CenterHorizontally)
                  .background(purpleBrand, RoundedCornerShape(5.dp))
                  .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "كشف حساب",
                  color = Color.White,
                  fontSize = (12 * reportConfig.fontScale).sp,
                  fontWeight = FontWeight.Black,
                  maxLines = 1,
                  softWrap = false
                )
              }

              if (reportConfig.showDateTime) {
                val dateParts = ArabicNumberHelper.formatTo24HourDateTime(dateStr).trim().split(" ")
                val rDatePart = dateParts.firstOrNull { it.contains("/") } ?: dateParts.firstOrNull() ?: ""
                val rTimePart = dateParts.filter { it != rDatePart }.joinToString(" ")

                Text(
                  text = buildAnnotatedString {
                    withStyle(SpanStyle(color = primaryBlue, fontWeight = FontWeight.Black)) {
                      append("📅 ")
                    }
                    append("$rDatePart ")
                    if (rTimePart.isNotEmpty()) {
                      withStyle(SpanStyle(color = Color(0xFFC62828), fontWeight = FontWeight.Black)) {
                        append(rTimePart)
                      }
                    }
                  },
                  fontSize = (11 * reportConfig.fontScale).sp,
                  fontWeight = FontWeight.Black,
                  color = primaryText,
                  modifier = Modifier.weight(1f),
                  textAlign = TextAlign.End,
                  maxLines = 1
                )
              } else {
                Spacer(modifier = Modifier.weight(1f))
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // METADATA ROW 2: Customer Name
            Text(
              text = "اسم العميل : ${customer.name}",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Black,
              color = customerNameColor,
              modifier = Modifier.fillMaxWidth(),
              textAlign = TextAlign.Start,
              maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            // PERIOD BANNER BOX
            val periodBannerText = if (startDateStr.isNotBlank() && endDateStr.isNotBlank()) {
              "كشف حساب تفصيلي للفترة من: $startDateStr إلى: $endDateStr 📅"
            } else {
              "كشف حساب تفصيلي لجميع الحركات (${filteredTransactions.size} حركة) 📅"
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F7FD), RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp, horizontal = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = periodBannerText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = primaryBlue,
                textAlign = TextAlign.Center,
                maxLines = 1
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TABLE CONTENT
            val tableContent = @Composable {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .border(2.5.dp, primaryBlue)
              ) {
                // Header Row (White background, Blue Text)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(IntrinsicSize.Min),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  StatementHeaderCell(text = "التاريخ", weight = 1.7f, textColor = primaryBlue, fontSize = 10.5.sp)
                  StatementBorderV(primaryBlue)
                  StatementHeaderCell(text = "النوع", weight = 0.9f, textColor = primaryBlue, fontSize = 10.5.sp)
                  StatementBorderV(primaryBlue)
                  StatementHeaderCell(text = "البيان", weight = 2.4f, textColor = primaryBlue, fontSize = 10.5.sp)
                  StatementBorderV(primaryBlue)
                  StatementHeaderCell(text = "لكم", weight = 1.45f, textColor = primaryBlue, fontSize = 10.5.sp)
                  StatementBorderV(primaryBlue)
                  StatementHeaderCell(text = "عليكم", weight = 1.45f, textColor = primaryBlue, fontSize = 10.5.sp)
                  StatementBorderV(primaryBlue)
                  StatementHeaderCell(text = "الرصيد", weight = 1.55f, textColor = primaryBlue, fontSize = 10.5.sp)
                }

                if (filteredTransactions.isEmpty()) {
                  StatementBorderH(primaryBlue)
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(24.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "لا توجد حركات في الفترة المحددة",
                      fontWeight = FontWeight.Bold,
                      color = Color.Gray,
                      fontSize = 13.sp
                    )
                  }
                } else {
                  filteredTransactions.forEach { t ->
                    StatementBorderH(primaryBlue)

                    val tSym = ArabicNumberHelper.getCurrencySymbol(t.currency.ifEmpty { "USD" })
                    val lakumStr = if (t.type == "قبض") {
                      "$tSym${ArabicNumberHelper.formatAmount(t.amount)}"
                    } else if (t.type == "افتتاح" && t.amount < 0) {
                      "$tSym${ArabicNumberHelper.formatAmount(Math.abs(t.amount))}"
                    } else {
                      "-"
                    }

                    val lakumAnnotated = if (t.type == "قبض") {
                      buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                          append(tSym)
                        }
                        withStyle(SpanStyle(color = creditGreen, fontWeight = FontWeight.Black)) {
                          append(ArabicNumberHelper.formatAmount(t.amount))
                        }
                      }
                    } else if (t.type == "افتتاح" && t.amount < 0) {
                      buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                          append(tSym)
                        }
                        withStyle(SpanStyle(color = creditGreen, fontWeight = FontWeight.Black)) {
                          append(ArabicNumberHelper.formatAmount(Math.abs(t.amount)))
                        }
                      }
                    } else null

                    val alaykumAnnotated = if (t.type == "صرف" || t.type == "فاتورة") {
                      buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                          append(tSym)
                        }
                        withStyle(SpanStyle(color = debitRed, fontWeight = FontWeight.Black)) {
                          append(ArabicNumberHelper.formatAmount(t.amount))
                        }
                      }
                    } else if (t.type == "افتتاح" && t.amount > 0) {
                      buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                          append(tSym)
                        }
                        withStyle(SpanStyle(color = debitRed, fontWeight = FontWeight.Black)) {
                          append(ArabicNumberHelper.formatAmount(t.amount))
                        }
                      }
                    } else null

                    val rowBal = transactionRunningBalances[t] ?: t.balanceAfter
                    val baseSym = ArabicNumberHelper.getCurrencySymbol(baseCurrency)
                    val balanceAfterAnnotated = buildAnnotatedString {
                      if (Math.abs(rowBal) >= 0.005) {
                        withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                          append(baseSym)
                        }
                      }
                      withStyle(SpanStyle(color = purpleBrand, fontWeight = FontWeight.Black)) {
                        append(if (Math.abs(rowBal) < 0.005) "0" else ArabicNumberHelper.formatAmount(rowBal))
                      }
                    }

                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .height(IntrinsicSize.Min),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      // 1. Date & Time (24h system)
                      val dt24 = ArabicNumberHelper.formatTo24HourDateTime(t.date)
                      val parts = dt24.trim().split(" ")
                      val datePart = parts.firstOrNull { it.contains("/") || it.contains("-") } ?: parts.firstOrNull() ?: dt24
                      val timePart = parts.filter { it != datePart }.joinToString(" ")

                      Box(
                        modifier = Modifier
                          .weight(1.7f)
                          .padding(vertical = 4.dp, horizontal = 1.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        Column(
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.Center
                        ) {
                          Text(
                            text = datePart,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                          )
                          if (timePart.isNotEmpty()) {
                            Text(
                              text = timePart,
                              fontSize = 8.5.sp,
                              fontWeight = FontWeight.Bold,
                              color = Color(0xFFC62828),
                              textAlign = TextAlign.Center,
                              maxLines = 1
                            )
                          }
                        }
                      }
                      StatementBorderV(primaryBlue)

                      // 2. Type
                      StatementBodyCell(
                        text = ArabicNumberHelper.formatShortTransactionType(t),
                        weight = 0.9f,
                        isBold = true,
                        textColor = Color.Black,
                        fontSize = 9.sp,
                        singleLine = true
                      )
                      StatementBorderV(primaryBlue)

                      // 3. Description
                      val cleanNote = ArabicNumberHelper.cleanStatementNote(t.note)
                      val displayNote = cleanNote.ifEmpty {
                        if (!t.note.startsWith("سند رقم") && !t.note.startsWith("فاتورة رقم")) t.note else "—"
                      }
                      StatementBodyCell(
                        text = displayNote.ifEmpty { "—" },
                        weight = 2.4f,
                        textColor = Color.Black,
                        isBold = true,
                        fontSize = 9.sp,
                        alignStart = false
                      )
                      StatementBorderV(primaryBlue)

                      // 4. Lakum (Credit - Bold Green with Blue Symbol)
                      StatementBodyCell(
                        text = if (lakumAnnotated == null) "-" else "",
                        annotatedText = lakumAnnotated,
                        weight = 1.45f,
                        textColor = creditGreen,
                        isBold = true,
                        fontSize = 9.5.sp,
                        isLtr = true,
                        singleLine = true
                      )
                      StatementBorderV(primaryBlue)

                      // 5. Alaykum (Debit - Bold Red with Blue Symbol)
                      StatementBodyCell(
                        text = if (alaykumAnnotated == null) "-" else "",
                        annotatedText = alaykumAnnotated,
                        weight = 1.45f,
                        textColor = debitRed,
                        isBold = true,
                        fontSize = 9.5.sp,
                        isLtr = true,
                        singleLine = true
                      )
                      StatementBorderV(primaryBlue)

                      // 6. Balance After (Bold Purple with Blue Symbol)
                      StatementBodyCell(
                        text = "",
                        annotatedText = balanceAfterAnnotated,
                        weight = 1.55f,
                        textColor = purpleBrand,
                        isBold = true,
                        fontSize = 9.5.sp,
                        isLtr = true,
                        singleLine = true
                      )
                    }
                  }
                }
              }
            }

            tableContent()

            Spacer(modifier = Modifier.height(12.dp))

            // SUMMARY 3 CARDS IN A ROW
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val lastCurrency = filteredTransactions.lastOrNull { it.currency.isNotBlank() }?.currency ?: "USD"
              val debitCurrency = filteredTransactions.filter {
                it.type == "صرف" || it.type == "فاتورة" || (it.type == "افتتاح" && it.amount > 0)
              }.lastOrNull { it.currency.isNotBlank() }?.currency ?: lastCurrency
              val creditCurrency = filteredTransactions.filter {
                it.type == "قبض" || (it.type == "افتتاح" && it.amount < 0)
              }.lastOrNull { it.currency.isNotBlank() }?.currency ?: lastCurrency

              val debitSym = ArabicNumberHelper.getCurrencySymbol(debitCurrency)
              val creditSym = ArabicNumberHelper.getCurrencySymbol(creditCurrency)
              val finalSym = ArabicNumberHelper.getCurrencySymbol(baseCurrency)

              // Card 1 (Right in RTL): إجمالي عليكم
              Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF5350))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "إجمالي عليكم",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFC62828),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                      text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                          append(debitSym)
                        }
                        withStyle(SpanStyle(color = Color(0xFFC62828), fontWeight = FontWeight.Black)) {
                          append(ArabicNumberHelper.formatAmount(totalDebit))
                        }
                      },
                      fontSize = 17.sp,
                      textAlign = TextAlign.Center,
                      maxLines = 1
                    )
                  }
                }
              }

              // Card 2 (Center): الإجمالي لكم
              Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF66BB6A))
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "الإجمالي لكم",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                      text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                          append(creditSym)
                        }
                        withStyle(SpanStyle(color = Color(0xFF2E7D32), fontWeight = FontWeight.Black)) {
                          append(ArabicNumberHelper.formatAmount(totalCredit))
                        }
                      },
                      fontSize = 17.sp,
                      textAlign = TextAlign.Center,
                      maxLines = 1
                    )
                  }
                }
              }

              // Card 3 (Left in RTL): الباقي لكم / عليكم / متزن
              val finalBalTitle = if (finalBalance > 0.005) {
                "الباقي عليكم"
              } else if (finalBalance < -0.005) {
                "الباقي لكم"
              } else {
                "الباقي (لكم / عليكم)"
              }
              val balAmt = if (Math.abs(finalBalance) < 0.005) "0" else ArabicNumberHelper.formatAmount(Math.abs(finalBalance))

              Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, purpleBrand)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = finalBalTitle,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    color = purpleBrand,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                      text = buildAnnotatedString {
                        if (Math.abs(finalBalance) >= 0.005) {
                          withStyle(SpanStyle(color = Color(0xFF0070BA), fontWeight = FontWeight.Black)) {
                            append(finalSym)
                          }
                        }
                        withStyle(SpanStyle(color = purpleBrand, fontWeight = FontWeight.Black)) {
                          append(balAmt)
                        }
                      },
                      fontSize = 17.sp,
                      fontWeight = FontWeight.Black,
                      color = purpleBrand,
                      textAlign = TextAlign.Center,
                      maxLines = 1
                    )
                  }
                }
              }
            }


            if (reportConfig.showAmountInWords && Math.abs(finalBalance) >= 0.005) {
              val curr = baseCurrency
              val words = "${ArabicNumberHelper.numberToArabicWords(Math.abs(finalBalance))} ${ArabicNumberHelper.getCurrencyName(curr)}"
              Text(
                text = "المبلغ كتابة: $words",
                fontSize = (12 * reportConfig.fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
              )
            }

            // SIGNATURES & APPROVAL SEAL (if enabled in reportConfig)
            if (reportConfig.showSignatures) {
              Spacer(modifier = Modifier.height(18.dp))
              HorizontalDivider(
                thickness = 1.2.dp,
                color = primaryBlue.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 6.dp)
              )
              Spacer(modifier = Modifier.height(14.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Official / Cashier Signature
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.weight(1.2f)
                ) {
                  Text(
                    text = if (reportConfig.accountantSignatureName.isNotBlank())
                      "توقيع المسؤول / أمين الصندوق (${reportConfig.accountantSignatureName})"
                    else
                      "توقيع المسؤول / أمين الصندوق",
                    fontSize = (11.5 * reportConfig.fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(30.dp))
                  HorizontalDivider(
                    thickness = 1.2.dp,
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth(0.85f)
                  )
                }

                // Official Seal / Stamp
                if (reportConfig.showStampSeal) {
                  Box(
                    modifier = Modifier.weight(0.8f),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      modifier = Modifier
                        .size(66.dp)
                        .border(2.5.dp, Color(0xFFC62828), RoundedCornerShape(33.dp))
                        .padding(4.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Text(
                        text = "معتمد",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC62828)
                      )
                      Text(
                        text = "APPROVED",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                      )
                      Text(
                        text = "✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC62828)
                      )
                    }
                  }
                } else {
                  Spacer(modifier = Modifier.weight(0.2f))
                }

                // Customer Signature
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.weight(1.2f)
                ) {
                  Text(
                    text = "توقيع المستلم / العميل",
                    fontSize = (11.5 * reportConfig.fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(30.dp))
                  HorizontalDivider(
                    thickness = 1.2.dp,
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth(0.85f)
                  )
                }
              }
            } else if (reportConfig.showStampSeal) {
              Spacer(modifier = Modifier.height(10.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE),
                border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
              ) {
                Text(
                  text = "★ معتمد رسمياً APPROVED ★",
                  fontSize = (11.5 * reportConfig.fontScale).sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFFC62828),
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
              }
            }

            // Custom Footer Text (if set)
            if (reportConfig.customFooterText.isNotBlank()) {
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = reportConfig.customFooterText,
                fontSize = (11 * reportConfig.fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFF8F9FA), RoundedCornerShape(4.dp))
                  .padding(8.dp)
              )
            }

            // Ample bottom padding to ensure the bottom border is never clipped in landscape or portrait
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }
    }
  }
}
}

@Composable
fun RowScope.StatementHeaderCell(
  text: String,
  weight: Float,
  textColor: Color,
  fontSize: TextUnit = 11.5.sp
) {
  Box(
    modifier = Modifier
      .weight(weight)
      .padding(vertical = 6.dp, horizontal = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontSize = fontSize,
      fontWeight = FontWeight.ExtraBold,
      color = textColor,
      textAlign = TextAlign.Center,
      maxLines = 2
    )
  }
}

@Composable
fun RowScope.StatementBodyCell(
  text: String,
  weight: Float,
  isBold: Boolean = false,
  textColor: Color = Color.Black,
  fontSize: TextUnit = 11.sp,
  alignStart: Boolean = false,
  isLtr: Boolean = false,
  annotatedText: AnnotatedString? = null,
  singleLine: Boolean = false
) {
  Box(
    modifier = Modifier
      .weight(weight)
      .padding(vertical = 5.dp, horizontal = 1.5.dp),
    contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center
  ) {
    if (annotatedText != null) {
      if (isLtr) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
          Text(
            text = annotatedText,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            maxLines = if (singleLine) 1 else 3,
            softWrap = !singleLine
          )
        }
      } else {
        Text(
          text = annotatedText,
          fontSize = fontSize,
          textAlign = if (alignStart) TextAlign.Start else TextAlign.Center,
          maxLines = if (singleLine) 1 else 3,
          softWrap = !singleLine
        )
      }
    } else {
      if (isLtr) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
          Text(
            text = text,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = if (singleLine) 1 else 3,
            softWrap = !singleLine
          )
        }
      } else {
        Text(
          text = text,
          fontSize = fontSize,
          fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
          color = textColor,
          textAlign = if (alignStart) TextAlign.Start else TextAlign.Center,
          maxLines = if (singleLine) 1 else 3,
          softWrap = !singleLine
        )
      }
    }
  }
}

@Composable
fun StatementBorderV(color: Color) {
  Box(
    modifier = Modifier
      .width(1.5.dp)
      .fillMaxHeight()
      .background(color)
  )
}

@Composable
fun StatementBorderH(color: Color) {
  HorizontalDivider(thickness = 1.5.dp, color = color)
}
