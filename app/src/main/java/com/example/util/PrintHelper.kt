package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.print.PdfPrintHelper
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.R
import com.example.data.Customer
import com.example.data.ExchangeRates
import com.example.data.InvoiceData
import com.example.data.ReportCustomizationConfig
import com.example.data.StoreConfig
import com.example.data.VoucherItem
import java.io.ByteArrayOutputStream
import java.io.File

object PrintHelper {

  private val activeWebViews = mutableSetOf<WebView>()

  private fun getGoogleFontLink(fontFamily: String): String {
    return when (fontFamily.lowercase().trim()) {
      "cairo" -> """<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800;900&display=swap" rel="stylesheet">"""
      "tajawal" -> """<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@400;500;700;800;900&display=swap" rel="stylesheet">"""
      "almarai" -> """<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Almarai:wght@400;700;800&display=swap" rel="stylesheet">"""
      "amiri" -> """<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Amiri:wght@400;700&display=swap" rel="stylesheet">"""
      else -> ""
    }
  }

  private fun getFontCssFamily(fontFamily: String): String {
    return when (fontFamily.lowercase().trim()) {
      "cairo" -> "'Cairo', 'Noto Sans Arabic', 'Segoe UI', Tahoma, sans-serif"
      "tajawal" -> "'Tajawal', 'Noto Sans Arabic', 'Segoe UI', Tahoma, sans-serif"
      "almarai" -> "'Almarai', 'Noto Sans Arabic', 'Segoe UI', Tahoma, sans-serif"
      "amiri" -> "'Amiri', 'Traditional Arabic', serif"
      "tahoma" -> "Tahoma, 'Noto Sans Arabic', Arial, sans-serif"
      else -> "'Cairo', 'Noto Sans Arabic', system-ui, -apple-system, sans-serif"
    }
  }

  private fun getSignaturesHtml(reportConfig: ReportCustomizationConfig, storeConfig: StoreConfig): String {
    if (!reportConfig.showSignatures) return ""
    val sellerTitle = if (reportConfig.accountantSignatureName.isNotBlank()) {
      "توقيع البائع (${reportConfig.accountantSignatureName})"
    } else {
      "توقيع البائع"
    }
    val buyerTitle = "توقيع المشتري"
    return """
      <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-top:20px;margin-bottom:8px;padding:12px 20px 6px 20px;border-top:1.5px dashed ${reportConfig.tableBorderColorHex};">
        <div style="text-align:center;width:38%;">
          <div style="font-size:${12.5 * reportConfig.fontScale}px;font-weight:800;color:${reportConfig.primaryTextColorHex};margin-bottom:26px;">$sellerTitle</div>
          <div style="border-top:1.5px solid #555;width:80%;margin:0 auto;"></div>
        </div>
        ${if (reportConfig.showStampSeal) """
          <div style="text-align:center;width:24%;display:flex;flex-direction:column;align-items:center;justify-content:center;">
            <div style="border:2.5px solid #C62828;color:#C62828;border-radius:50%;width:68px;height:68px;display:flex;flex-direction:column;align-items:center;justify-content:center;transform:rotate(-8deg);font-weight:900;font-size:10px;line-height:1.2;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
              <div>معتمد</div>
              <div style="font-size:7px;letter-spacing:1px;">APPROVED</div>
              <div style="font-size:12px;">✓</div>
            </div>
          </div>
        """ else """<div style="width:24%;"></div>"""}
        <div style="text-align:center;width:38%;">
          <div style="font-size:${12.5 * reportConfig.fontScale}px;font-weight:800;color:${reportConfig.primaryTextColorHex};margin-bottom:26px;">$buyerTitle</div>
          <div style="border-top:1.5px solid #555;width:80%;margin:0 auto;"></div>
        </div>
      </div>
    """.trimIndent()
  }

  private fun getCustomHeaderTitleHtml(reportConfig: ReportCustomizationConfig): String {
    if (reportConfig.customHeaderTitle.isBlank()) return ""
    return """
      <div style="text-align:center;font-size:${14 * reportConfig.fontScale}px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:8px;padding:4px 10px;border-bottom:1.5px solid ${reportConfig.headerColorHex};letter-spacing:1px;">
        ${reportConfig.customHeaderTitle}
      </div>
    """.trimIndent()
  }

  private fun getCustomNoticeBadgeHtml(reportConfig: ReportCustomizationConfig): String {
    if (reportConfig.customNoticeBadge.isBlank()) return ""
    return """
      <div style="text-align:center;font-size:${12 * reportConfig.fontScale}px;font-weight:800;color:#856404;background:#FFF3CD;border:1.5px solid #FFEEBA;padding:6px 10px;border-radius:6px;margin:8px 0;">
        ⚠️ ${reportConfig.customNoticeBadge}
      </div>
    """.trimIndent()
  }

  private fun getCustomFooterTextHtml(reportConfig: ReportCustomizationConfig): String {
    if (reportConfig.customFooterText.isBlank()) return ""
    return """
      <div style="text-align:center;font-size:${11.5 * reportConfig.fontScale}px;font-weight:bold;color:${reportConfig.primaryTextColorHex};margin-top:10px;padding:6px 12px;background:#F8F9FA;border-radius:6px;border:1px solid #E9ECEF;">
        ${reportConfig.customFooterText}
      </div>
    """.trimIndent()
  }

  private const val SVG_LOGO = """<svg viewBox="0 0 500 500" width="100%" height="100%">
    <circle cx="250" cy="250" r="240" fill="#5e258d" stroke="#cccccc" stroke-width="12"/>
    <circle cx="250" cy="250" r="222" fill="none" stroke="white" stroke-width="4"/>
    <text x="250" y="180" fill="white" font-family="'Cairo', sans-serif" font-size="75" font-weight="900" text-anchor="middle">المملكة</text>
    <rect x="110" y="215" width="280" height="50" rx="10" fill="white"/>
    <text x="250" y="250" fill="#5e258d" font-family="Arial, sans-serif" font-size="32" font-weight="900" text-anchor="middle">beIN SPORTS</text>
    <text x="250" y="335" fill="white" font-family="'Cairo', sans-serif" font-size="52" font-weight="800" text-anchor="middle">للإلكترونيات</text>
    <g transform="translate(185, 360)">
        <rect x="0" y="0" width="130" height="28" rx="5" fill="#e0e0e0" stroke="white" stroke-width="2"/>
        <circle cx="20" cy="14" r="4" fill="#00ff00"/>
        <rect x="80" y="10" width="35" height="8" fill="#5e258d"/>
    </g>
    <g transform="translate(160, 395)">
        <rect x="0" y="0" width="180" height="38" rx="6" fill="#ffffff" stroke="#cccccc" stroke-width="2"/>
        <circle cx="25" cy="19" r="5" fill="#ff0000"/>
        <rect x="110" y="14" width="45" height="10" fill="#5e258d"/>
    </g>
</svg>"""

  @Volatile
  private var cachedAppLogoBase64: String? = null

  fun getDefaultLogoBase64(context: Context?): String {
    cachedAppLogoBase64?.let { return it }
    if (context == null) return ""
    return try {
      val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
      if (bitmap != null) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, baos)
        val bytes = baos.toByteArray()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        cachedAppLogoBase64 = base64
        base64
      } else {
        val inputStream = context.resources.openRawResource(R.drawable.app_logo)
        val bytes = inputStream.readBytes()
        inputStream.close()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        cachedAppLogoBase64 = base64
        base64
      }
    } catch (_: Throwable) {
      ""
    }
  }

  private fun getEffectiveLogoDataUrl(storeConfig: StoreConfig, context: Context? = null): String? {
    if (storeConfig.logoBase64.isNotBlank()) {
      return if (storeConfig.logoBase64.startsWith("data:")) {
        storeConfig.logoBase64
      } else {
        "data:image/png;base64,${storeConfig.logoBase64}"
      }
    }
    val defaultBase64 = getDefaultLogoBase64(context)
    if (defaultBase64.isNotBlank()) {
      return "data:image/jpeg;base64,$defaultBase64"
    }
    return null
  }

  private fun getLogoHtml(storeConfig: StoreConfig, context: Context? = null): String {
    val dataUrl = getEffectiveLogoDataUrl(storeConfig, context)
    return if (dataUrl != null) {
      """<img src="$dataUrl" style="width:90px;height:90px;object-fit:cover;border-radius:50%;border:3px solid #5e258d;box-shadow:0 2px 5px rgba(0,0,0,0.15);">"""
    } else {
      """<div class="logo-container">$SVG_LOGO</div>"""
    }
  }

  private fun getWatermarkHtml(
    storeConfig: StoreConfig,
    reportConfig: ReportCustomizationConfig,
    context: Context? = null
  ): String {
    if (!reportConfig.showWatermark) return ""
    val dataUrl = getEffectiveLogoDataUrl(storeConfig, context)
    val logoHtml = if (dataUrl != null) {
      """<img src="$dataUrl" class="wm-logo-img" alt="Watermark Logo" />"""
    } else {
      """<div class="wm-logo-svg">$SVG_LOGO</div>"""
    }
    val storeNameAr = if (reportConfig.customWatermarkText.isNotBlank()) {
      reportConfig.customWatermarkText
    } else if (storeConfig.wmAr.isNotBlank()) {
      storeConfig.wmAr
    } else {
      storeConfig.storeNameAr.ifEmpty { "المملكة للإلكترونيات" }
    }
    val storeNameEn = storeConfig.storeNameEn.ifEmpty { "ALMAMLAK ELECTRONICS" }

    return """
      <div class="card-watermark">
        $logoHtml
        <div class="wm-title-ar">$storeNameAr</div>
        <div class="wm-title-en">${storeNameEn.uppercase()}</div>
      </div>
    """.trimIndent()
  }

  fun getInvoiceHtml(
    invoice: InvoiceData,
    storeConfig: StoreConfig,
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    context: Context? = null
  ): String {
    val sym = ArabicNumberHelper.getCurrencySymbol(invoice.currency)
    val currName = ArabicNumberHelper.getCurrencyName(invoice.currency)
    val grandTotal = invoice.grandTotal
    val isCash = invoice.invType.contains("نقد") || invoice.invType.equals("cash", ignoreCase = true)
    val amountInWords = if (reportConfig.showAmountInWords) {
      "${ArabicNumberHelper.numberToArabicWords(grandTotal)} $currName"
    } else ""
    val scale = reportConfig.fontScale
    val formattedGrandTotal = ArabicNumberHelper.formatAmount(grandTotal)
    val amountFontSizePx = 14 * scale
    val totalAmountFontSize = 14 * scale
    val totalSymFontSize = 14 * scale
    val amountInWordsFontSize = when {
      amountInWords.length > 68 -> 8.5 * scale
      amountInWords.length > 50 -> 9.5 * scale
      amountInWords.length > 35 -> 11.0 * scale
      else -> 12.5 * scale
    }
    val invDate = if (invoice.createdAt.isNotEmpty()) ArabicNumberHelper.formatTo24HourDateTime(invoice.createdAt) else ArabicNumberHelper.formatDateTime()
    val fontCss = getFontCssFamily(reportConfig.fontFamily)
    val googleFontLink = getGoogleFontLink(reportConfig.fontFamily)

    // Main row total price = unit price (ignoring quantity)
    val mainTotalPrice = invoice.price
    val mainUnitPrice = invoice.price
    val mainQty = if (invoice.type == "months") {
      val q = ArabicNumberHelper.toEngDigits(invoice.qty.toString().removeSuffix(".0"))
      "$q أشهر"
    } else {
      ArabicNumberHelper.toEngDigits(invoice.qty.toString().removeSuffix(".0"))
    }

    var rows = """
      <tr>
        <td class="amount-cell"><span dir="ltr" style="font-size:${amountFontSizePx}px;font-weight:900;"><span style="color:${reportConfig.currencySymbolColorHex};font-weight:900;font-size:${amountFontSizePx}px;">$sym</span>&nbsp;<span style="color:${reportConfig.totalValueColorHex};font-weight:900;font-size:${amountFontSizePx}px;">${ArabicNumberHelper.formatAmount(mainTotalPrice)}</span></span></td>
        <td class="amount-cell"><span dir="ltr" style="font-size:${amountFontSizePx}px;font-weight:900;"><span style="color:${reportConfig.currencySymbolColorHex};font-weight:900;font-size:${amountFontSizePx}px;">$sym</span>&nbsp;<span style="color:${reportConfig.unitPriceColorHex};font-weight:900;font-size:${amountFontSizePx}px;">${ArabicNumberHelper.formatAmount(mainUnitPrice)}</span></span></td>
        <td class="qty-cell" style="color:${reportConfig.quantityColorHex};font-weight:900;">$mainQty</td>
        <td class="extra-details-cell"><div class="extra-description">${invoice.desc.trim().ifEmpty { "تجديد باقة تميز" }}</div></td>
        <td class="num-cell" style="color:${reportConfig.itemNumberColorHex};font-weight:900;">1</td>
      </tr>
    """.trimIndent()

    invoice.extraItems.forEachIndexed { index, item ->
      val itemNumber = index + 2
      val itemTotalPrice = item.price * item.qty
      val itemCurr = ArabicNumberHelper.getCurrencySymbol(if (item.currency.isNotEmpty()) item.currency else invoice.currency)
      rows += """
        <tr>
          <td class="amount-cell"><span dir="ltr" style="font-size:${amountFontSizePx}px;font-weight:900;"><span style="color:${reportConfig.currencySymbolColorHex};font-weight:900;font-size:${amountFontSizePx}px;">$itemCurr</span>&nbsp;<span style="color:${reportConfig.totalValueColorHex};font-weight:900;font-size:${amountFontSizePx}px;">${ArabicNumberHelper.formatAmount(itemTotalPrice)}</span></span></td>
          <td class="amount-cell"><span dir="ltr" style="font-size:${amountFontSizePx}px;font-weight:900;"><span style="color:${reportConfig.currencySymbolColorHex};font-weight:900;font-size:${amountFontSizePx}px;">$itemCurr</span>&nbsp;<span style="color:${reportConfig.unitPriceColorHex};font-weight:900;font-size:${amountFontSizePx}px;">${ArabicNumberHelper.formatAmount(item.price)}</span></span></td>
          <td class="qty-cell" style="color:${reportConfig.quantityColorHex};font-weight:900;">${ArabicNumberHelper.toEngDigits(item.qty.toString().removeSuffix(".0"))}</td>
          <td class="extra-details-cell"><div class="extra-description">${item.description.ifEmpty { "—" }}</div></td>
          <td class="num-cell" style="color:${reportConfig.itemNumberColorHex};font-weight:900;">$itemNumber</td>
        </tr>
      """.trimIndent()
    }

    val effectiveStoreNameAr = storeConfig.storeNameAr.ifBlank { "المملكة للإلكترونيات" }
    val effectiveStoreNameEn = storeConfig.storeNameEn.ifBlank { "ALMamlaka Electronics" }
    val effectiveAddressAr = storeConfig.addressAr.ifBlank { "إب شارع تعز" }
    val effectiveAddressEn = storeConfig.addressEn.ifBlank { "YEMEN Ibb" }
    val effectivePhone = storeConfig.phone.ifBlank { "772707736" }

    val storeInfoRightHtml = if (reportConfig.showStoreInfo) {
      """
        <div class="company-info" style="text-align:right;">
          <div class="store-name-ar">$effectiveStoreNameAr</div>
          <div class="store-address" style="margin:2px 0;">$effectiveAddressAr</div>
          <div class="store-phone">تلفون / <span class="phone-num">$effectivePhone</span></div>
          ${if (reportConfig.taxOrCrNumber.isNotBlank()) "<div style='font-size:11px;color:${reportConfig.primaryTextColorHex};'>الرقم الضريبي/السجل: ${reportConfig.taxOrCrNumber}</div>" else ""}
        </div>
      """.trimIndent()
    } else """<div class="company-info"></div>"""

    val storeInfoLeftHtml = if (reportConfig.showStoreInfo) {
      """
        <div class="company-info" style="text-align:left;" dir="ltr">
          <div class="store-name-en">$effectiveStoreNameEn</div>
          <div class="store-address" style="margin:2px 0;">$effectiveAddressEn</div>
          <div class="store-phone">TEL/ <span class="phone-num">$effectivePhone</span></div>
        </div>
      """.trimIndent()
    } else """<div class="company-info"></div>"""

    val logoHtml = if (reportConfig.showLogo) getLogoHtml(storeConfig, context) else ""

    val dateHtml = if (reportConfig.showDateTime) {
      """<div class="inv-date-box">تاريخ / <span dir="ltr">$invDate</span></div>"""
    } else """<div class="inv-date-box"></div>"""

    val branchHtml = if (reportConfig.showBranch) {
      """<div class="inv-store-box">${storeConfig.branch.ifEmpty { "فرع شارع تعز" }}</div>"""
    } else """<div class="inv-store-box"></div>"""

    val customerAccountHtml = if (reportConfig.showCustomerAccountNumber && invoice.customerAccount.isNotBlank()) {
      """ <span style="font-size:${12 * scale}px;color:${reportConfig.headerColorHex};">(حساب: ${invoice.customerAccount})</span>"""
    } else ""

    val subCardHtml = if (reportConfig.showCardSubscriptionBox) {
      """
        <div class="sub-card-box">
          <div class="watermark-bg">
            <div class="wm-en" style="font-size:26px;font-weight:900;letter-spacing:4px;opacity:0.08;color:${reportConfig.headerColorHex};white-space:nowrap;">ALMAMLAK ELECTRONIC</div>
          </div>
          <div class="overlay-content">
            <div style="display:flex;justify-content:space-around;align-items:center;width:100%;margin-bottom:6px;">
              <div style="flex:1;text-align:center;font-size:${14 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};">رقم الاشتراك</div>
              <div style="font-size:18px;color:#90A4AE;font-weight:bold;margin:0 12px;">|</div>
              <div style="flex:1;text-align:center;font-size:${14 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};">تاريخ انتهاء الاشتراك</div>
            </div>
            <div style="display:flex;justify-content:space-around;align-items:center;width:100%;">
              <div style="flex:1;text-align:center;font-size:${17 * scale}px;font-weight:900;color:#C62828;" dir="ltr">${invoice.cardId.trim().ifEmpty { "—" }}</div>
              <div style="width:24px;"></div>
              <div style="flex:1;text-align:center;font-size:${17 * scale}px;font-weight:900;color:#C62828;" dir="ltr">${invoice.endDate.trim().ifEmpty { "—" }}</div>
            </div>
          </div>
        </div>
      """.trimIndent()
    } else ""

    val termsHtml = if (reportConfig.showTermsAndNotes) {
      val defaultTerms = "• البضاعة المباعة لا ترد ولا تستبدل بعد خروجها من المحل.\n• استلمت البضاعة الموضحة أعلاه كاملة ، سليمة ، ولعدد ذلك."
      val termsText = storeConfig.terms.trim().ifEmpty { defaultTerms }
      val formattedTerms = termsText.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("") { line ->
          val cleanLine = line.removePrefix("•").removePrefix("▪").removePrefix("-").trim()
          """<div class="term-line" style="margin-bottom:3px;text-align:right;direction:rtl;font-size:${11 * scale}px;font-weight:700;color:${reportConfig.primaryTextColorHex};"><span style="color:${reportConfig.tableBorderColorHex};font-weight:900;margin-left:6px;">▪</span>$cleanLine</div>"""
        }
      """
        <div class="terms-text" dir="rtl" style="text-align:right;direction:rtl;margin-top:10px;padding:2px 4px;">
          $formattedTerms
        </div>
      """.trimIndent()
    } else ""

    return """
      <!DOCTYPE html>
      <html lang="ar" dir="rtl">
      <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>فاتورة ${invoice.invNum}</title>
      $googleFontLink
      <style>
      @page {
        size: A4 portrait;
        margin: 5mm 6mm;
      }
      * {
        box-sizing: border-box;
        font-family: $fontCss;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      html, body {
        background: #fff;
        margin: 0;
        padding: 0;
        width: 100%;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      body {
        padding: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        box-sizing: border-box;
      }
      .invoice-card {
        width: 100%;
        max-width: 820px;
        background: #fff;
        border: 3.5px solid ${reportConfig.tableBorderColorHex};
        border-radius: 12px;
        padding: 16px 18px 20px 18px;
        position: relative;
        box-sizing: border-box;
        overflow: hidden;
      }
      @media print {
        body {
          padding: 0 !important;
          background: transparent !important;
        }
        .invoice-card {
          width: 100% !important;
          max-width: 100% !important;
          border: 2.5px solid ${reportConfig.tableBorderColorHex} !important;
          border-radius: 8px !important;
          padding: 12px 14px !important;
          page-break-inside: avoid !important;
        }
      }
      .inv-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;position:relative;z-index:2;}
      .company-info{font-size:${18 * scale}px;line-height:1.4;font-weight:700;width:37%;color:${reportConfig.headerColorHex};}
      .store-name-ar{font-size:${22 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:2px;white-space:nowrap;}
      .store-name-en{font-size:${19 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:2px;white-space:nowrap;}
      .store-address{font-size:${18 * scale}px;font-weight:700;color:${reportConfig.primaryTextColorHex};margin:2px 0;}
      .store-phone{font-size:${18 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};}
      .phone-num{font-size:${18 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};}
      .brand-box{text-align:center;width:24%;display:flex;justify-content:center;align-items:center;}
      .logo-container{width:96px;height:96px;border-radius:50%;overflow:hidden;display:flex;justify-content:center;align-items:center;}
      .meta-bar-top{border-top:3px solid ${reportConfig.tableBorderColorHex};padding-top:10px;margin-top:8px;position:relative;z-index:2;}
      .meta-row-top{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
      .inv-date-box{font-weight:800;font-size:${14 * scale}px;color:${reportConfig.primaryTextColorHex};width:38%;text-align:left;}
      .inv-account-box{font-weight:900;font-size:${14 * scale}px;color:${reportConfig.primaryTextColorHex};width:38%;text-align:right;}
      .inv-customer-box{font-weight:900;font-size:${14.5 * scale}px;color:#B71C1C;text-align:right;}
      .inv-num-box{font-weight:900;font-size:${14.5 * scale}px;color:${reportConfig.headerColorHex};text-align:left;}
      .inv-type-tag-box{width:24%;text-align:center;}
      .inv-type-tag{background:#B71C1C;color:#fff;padding:5px 18px;font-size:${14 * scale}px;font-weight:900;border-radius:8px;display:inline-block;}
      .meta-details-grid{display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;}
      .items-table{width:100%;table-layout:fixed;border-collapse:collapse;border:3px solid ${reportConfig.tableBorderColorHex};margin-bottom:0;position:relative;z-index:2;background:transparent;}
      .items-table th,.items-table td{border:2px solid ${reportConfig.tableBorderColorHex};text-align:center;padding:6px 4px;vertical-align:middle;word-wrap:break-word;overflow-wrap:break-word;}
      .items-table th{background:rgba(255,255,255,0.92);color:${reportConfig.headerColorHex};font-size:${13.5 * scale}px;font-weight:900;}
      .items-table td{background:rgba(255,255,255,0.72);}
      .num-cell{font-size:${13.5 * scale}px;font-weight:900;color:${reportConfig.primaryTextColorHex};}
      .extra-details-cell{text-align:center;padding:5px 6px;}
      .extra-description{font-size:${13.5 * scale}px;font-weight:900;color:${reportConfig.primaryTextColorHex};line-height:1.3;display:inline-block;}
      .qty-cell{font-size:${13.5 * scale}px;font-weight:900;color:${reportConfig.primaryTextColorHex};white-space:nowrap;}
      .amount-cell{font-size:${14 * scale}px;font-weight:900;color:${reportConfig.primaryTextColorHex};white-space:nowrap;letter-spacing:-0.2px;}
      .total-words-box{font-size:${12.5 * scale}px;font-weight:800;color:${reportConfig.primaryTextColorHex};text-align:center;word-wrap:break-word;overflow-wrap:break-word;}
      .total-label-box{font-size:${13.5 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};}
      .sub-card-box{margin-top:14px;border:3px solid ${reportConfig.tableBorderColorHex};border-radius:10px;position:relative;background:rgba(255,255,255,0.85);padding:10px 14px;overflow:hidden;z-index:2;}
      .watermark-bg{position:absolute;inset:0;display:flex;flex-direction:column;justify-content:center;align-items:center;pointer-events:none;z-index:0;}
      .card-watermark{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);display:flex;flex-direction:column;justify-content:center;align-items:center;pointer-events:none;z-index:0;opacity:0.12;width:100%;text-align:center;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;}
      .wm-logo-img{width:190px;height:190px;object-fit:cover;border-radius:50%;margin-bottom:8px;display:block;border:4px solid ${reportConfig.headerColorHex};}
      .wm-logo-svg{width:190px;height:190px;margin-bottom:8px;display:flex;justify-content:center;align-items:center;}
      .wm-title-ar{font-size:26px;font-weight:900;color:${reportConfig.watermarkColorHex};margin-bottom:4px;line-height:1.2;}
      .wm-title-en{font-size:17px;font-weight:900;color:${reportConfig.watermarkColorHex};letter-spacing:3px;line-height:1.2;}
      @media print {
        .card-watermark{opacity:0.15 !important;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;}
      }
      .overlay-content{position:relative;z-index:1;display:flex;flex-direction:column;justify-content:center;align-items:center;width:100%;}
      .terms-text{margin-top:10px;font-size:${11 * scale}px;font-weight:700;color:${reportConfig.primaryTextColorHex};line-height:1.6;text-align:right;direction:rtl;position:relative;z-index:2;}
      </style>
      </head>
      <body>
      <div class="invoice-card">
        ${getWatermarkHtml(storeConfig, reportConfig, context)}
        ${getCustomHeaderTitleHtml(reportConfig)}
        ${getCustomNoticeBadgeHtml(reportConfig)}
        <div class="inv-header">
          $storeInfoRightHtml
          <div class="brand-box">$logoHtml</div>
          $storeInfoLeftHtml
        </div>
        <div class="meta-bar-top">
          <div class="meta-row-top">
            ${
              if (isCash) {
                """<div class="inv-account-box"><span style="color:#0070BA;">رقم الفاتورة / </span><span style="color:#C62828;">${invoice.invNum.trim().ifEmpty { "1" }}</span></div>"""
              } else {
                """<div class="inv-account-box">رقم الحساب <span style="color:${reportConfig.customerAccountColorHex};font-weight:900;">${invoice.customerAccount.trim().ifEmpty { "—" }}</span></div>"""
              }
            }
            <div class="inv-type-tag-box"><span class="inv-type-tag">فاتورة ${invoice.invType.trim().ifEmpty { if (isCash) "نقداً" else "أجل" }}</span></div>
            <div class="inv-date-box">تاريخ / $invDate</div>
          </div>
          <div class="meta-details-grid">
            <div class="inv-customer-box"><span style="color:#B71C1C;">اسم العميل: </span><span style="color:${reportConfig.customerNameColorHex};">${invoice.customerName.trim().ifEmpty { if (isCash) "عميل نقدي" else "—" }}</span></div>
            ${
              if (isCash) {
                """<div></div>"""
              } else {
                """<div class="inv-num-box"><span style="color:#0070BA;">رقم الفاتورة / </span><span style="color:#C62828;">${invoice.invNum.trim().ifEmpty { "1" }}</span></div>"""
              }
            }
          </div>
        </div>
        <table class="items-table">
          <thead>
            <tr>
              <th style="width:24%;">القيمة الإجمالية</th>
              <th style="width:23%;">سعر الوحدة</th>
              <th style="width:13%;">العدد</th>
              <th style="width:31%;">التفاصيل</th>
              <th style="width:9%;">رقم الصنف</th>
            </tr>
          </thead>
          <tbody>
            $rows
            <tr>
              <td colspan="2" style="padding:4px 6px;text-align:center;">
                <div style="color:#2E7D32;font-size:${13.5 * scale}px;font-weight:900;margin-bottom:2px;">الإجمالي</div>
                <div style="color:${reportConfig.grandTotalAmountColorHex};font-size:${totalAmountFontSize}px;font-weight:900;" dir="ltr"><span style="color:${reportConfig.currencySymbolColorHex};font-size:${totalSymFontSize}px;font-weight:900;">$sym</span>&nbsp;<span style="color:${reportConfig.grandTotalAmountColorHex};font-size:${totalAmountFontSize}px;font-weight:900;">$formattedGrandTotal</span></div>
              </td>
              <td colspan="2" class="total-words-box" style="padding:4px 6px;font-size:${amountInWordsFontSize}px;line-height:1.3;">$amountInWords</td>
              <td class="total-label-box" style="text-align:center;">Total</td>
            </tr>
          </tbody>
        </table>
        $subCardHtml
        $termsHtml
        ${getCustomFooterTextHtml(reportConfig)}
      </div>
      </body>
      </html>
    """.trimIndent()
  }

  fun printInvoice(
    context: Context,
    invoice: InvoiceData,
    storeConfig: StoreConfig,
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig()
  ) {
    val html = getInvoiceHtml(invoice, storeConfig, reportConfig, context)
    printHtml(context, html, "Invoice_${invoice.invNum}")
  }

  fun exportInvoiceToPdf(
    context: Context,
    invoice: InvoiceData,
    storeConfig: StoreConfig,
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    onComplete: (File?) -> Unit
  ) {
    val html = getInvoiceHtml(invoice, storeConfig, reportConfig, context)
    val numStr = invoice.invNum.ifEmpty { "1" }
    val fileName = "فاتورة_رقم_${numStr}.pdf"
    exportHtmlToPdf(context, html, fileName, onComplete)
  }

  private fun formatDateTimeHtml(raw: String): String {
    val text24 = ArabicNumberHelper.formatTo24HourDateTime(raw)
    val parts = text24.split(" ").filter { it.isNotBlank() }
    val datePart = parts.firstOrNull { it.contains("/") || it.contains("-") } ?: ""
    val timeDigits = parts.filter { it != datePart }.firstOrNull { it.contains(":") } ?: ""

    val sb = StringBuilder()
    if (datePart.isNotEmpty()) {
      sb.append("""<span style="color:#000000;font-weight:800;">$datePart</span> """)
    }
    if (timeDigits.isNotEmpty()) {
      sb.append("""<span style="color:#C62828;font-weight:900;">$timeDigits</span>""")
    }
    if (sb.isEmpty()) return text24
    return sb.toString().trim()
  }

  fun getStatementHtml(
    customer: Customer,
    storeConfig: StoreConfig,
    startDateStr: String = "",
    endDateStr: String = "",
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    context: Context? = null,
    exchangeRates: ExchangeRates = ExchangeRates()
  ): String {
    val dateStr = ArabicNumberHelper.formatReportDateTime()
    val scale = reportConfig.fontScale
    val fontCss = getFontCssFamily(reportConfig.fontFamily)
    val googleFontLink = getGoogleFontLink(reportConfig.fontFamily)

    // Filter transactions by date range if provided
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

    val baseCurrency = customer.transactions.firstOrNull { it.currency.isNotBlank() }?.currency
      ?: filteredTransactions.firstOrNull { it.currency.isNotBlank() }?.currency
      ?: "USD"

    val debitTransactions = filteredTransactions.filter {
      it.type == "صرف" || it.type == "فاتورة" || (it.type == "افتتاح" && it.amount > 0)
    }
    val creditTransactions = filteredTransactions.filter {
      it.type == "قبض" || (it.type == "افتتاح" && it.amount < 0)
    }

    val convertedTotalDebit = debitTransactions.sumOf {
      val curr = if (it.currency.isNotBlank()) it.currency else baseCurrency
      ArabicNumberHelper.convertCurrency(it.amount, curr, baseCurrency, exchangeRates)
    }

    val convertedTotalCredit = creditTransactions.sumOf {
      val amt = if (it.type == "افتتاح") Math.abs(it.amount) else it.amount
      val curr = if (it.currency.isNotBlank()) it.currency else baseCurrency
      ArabicNumberHelper.convertCurrency(amt, curr, baseCurrency, exchangeRates)
    }

    val rawNet = convertedTotalDebit - convertedTotalCredit
    val finalBalance = if (Math.abs(rawNet) < 0.005) 0.0 else rawNet

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

    val totalDebit = debitTransactions.sumOf { it.amount }
    val totalCredit = creditTransactions.sumOf { if (it.type == "افتتاح") Math.abs(it.amount) else it.amount }

    val debitCurrency = debitTransactions.lastOrNull { it.currency.isNotBlank() }?.currency ?: baseCurrency
    val creditCurrency = creditTransactions.lastOrNull { it.currency.isNotBlank() }?.currency ?: baseCurrency

    val debitSym = ArabicNumberHelper.getCurrencySymbol(debitCurrency)
    val creditSym = ArabicNumberHelper.getCurrencySymbol(creditCurrency)
    val finalSym = ArabicNumberHelper.getCurrencySymbol(baseCurrency)

    val debitSymBlue = """<span style="color:#0070BA;font-weight:bold;">$debitSym</span>"""
    val creditSymBlue = """<span style="color:#0070BA;font-weight:bold;">$creditSym</span>"""
    val finalSymBlue = """<span style="color:#0070BA;font-weight:bold;">$finalSym</span>"""

    val defaultCurrency = baseCurrency

    val periodBannerText = if (startDateStr.isNotBlank() && endDateStr.isNotBlank()) {
      "كشف حساب تفصيلي للفترة من: $startDateStr إلى: $endDateStr 📅"
    } else {
      "كشف حساب تفصيلي لجميع الحركات (${filteredTransactions.size} حركة) 📅"
    }
    val periodSubHeader = """
      <div style="font-size:${14.5 * scale}px;font-weight:900;color:#0070BA;text-align:center;margin:8px 0 12px 0;padding:8px 12px;background:#F0F7FD;border:1.5px solid #BAE6FD;border-radius:8px;">
        $periodBannerText
      </div>
    """.trimIndent()

    val effectiveStoreNameAr = storeConfig.storeNameAr.ifBlank { "المملكة للإلكترونيات" }
    val effectiveStoreNameEn = storeConfig.storeNameEn.ifBlank { "ALMamlaka Electronics" }
    val effectiveAddressAr = storeConfig.addressAr.ifBlank { "إب شارع تعز" }
    val effectiveAddressEn = storeConfig.addressEn.ifBlank { "YEMEN Ibb" }
    val effectivePhone = storeConfig.phone.ifBlank { "772707736" }

    val storeInfoRightHtml = if (reportConfig.showStoreInfo) {
      """
        <div class="company-info" style="text-align:right;">
          <div class="store-name-ar">$effectiveStoreNameAr</div>
          <div class="store-address" style="margin:2px 0;">$effectiveAddressAr</div>
          <div class="store-phone">تلفون / <span class="phone-num">$effectivePhone</span></div>
        </div>
      """.trimIndent()
    } else """<div class="company-info"></div>"""

    val storeInfoLeftHtml = if (reportConfig.showStoreInfo) {
      """
        <div class="company-info" style="text-align:left;" dir="ltr">
          <div class="store-name-en">$effectiveStoreNameEn</div>
          <div class="store-address" style="margin:2px 0;">$effectiveAddressEn</div>
          <div class="store-phone">TEL/ <span class="phone-num">$effectivePhone</span></div>
        </div>
      """.trimIndent()
    } else """<div class="company-info"></div>"""

    val logoHtml = if (reportConfig.showLogo) getLogoHtml(storeConfig, context) else ""

    val dateHtml = """<div class="inv-date-box" dir="rtl"><span style="margin-left:4px;color:#0070BA;">📅</span><span dir="ltr">${formatDateTimeHtml(dateStr)}</span></div>"""

    val accountHtml = """<div class="inv-account-box">رقم الحساب / <span style="color:${reportConfig.customerAccountColorHex};font-weight:900;">${customer.accountNumber}</span></div>"""

    var rows = ""
    if (filteredTransactions.isEmpty()) {
      rows = "<tr><td colspan=\"6\" style=\"padding:18px;font-weight:800;color:#888;font-size:${15 * scale}px;\">لا توجد حركات في الفترة المحددة</td></tr>"
    } else {
      filteredTransactions.forEach { t ->
        val tSym = ArabicNumberHelper.getCurrencySymbol(t.currency.ifEmpty { defaultCurrency })
        val symBlue = """<span style="color:#0070BA;font-weight:bold;">$tSym</span>"""

        val lakum = if (t.type == "قبض") {
          "$symBlue ${ArabicNumberHelper.formatAmount(t.amount)}"
        } else if (t.type == "افتتاح" && t.amount < 0) {
          "$symBlue ${ArabicNumberHelper.formatAmount(Math.abs(t.amount))}"
        } else {
          "-"
        }

        val alaykum = if (t.type == "صرف" || t.type == "فاتورة") {
          "$symBlue ${ArabicNumberHelper.formatAmount(t.amount)}"
        } else if (t.type == "افتتاح" && t.amount > 0) {
          "$symBlue ${ArabicNumberHelper.formatAmount(t.amount)}"
        } else {
          "-"
        }

        val rowBal = transactionRunningBalances[t] ?: t.balanceAfter
        val balanceAfterStr = if (Math.abs(rowBal) < 0.005) {
          "0"
        } else {
          "$finalSymBlue ${ArabicNumberHelper.formatAmount(rowBal)}"
        }

        val cleanNote = ArabicNumberHelper.cleanStatementNote(t.note)
        val displayNote = cleanNote.ifEmpty {
          if (!t.note.startsWith("سند رقم") && !t.note.startsWith("فاتورة رقم")) t.note else "—"
        }

        rows += """
          <tr>
            <td style="font-size:${13 * scale}px;font-weight:700;" dir="ltr">${formatDateTimeHtml(t.date)}</td>
            <td style="font-size:${13 * scale}px;font-weight:700;color:#000;white-space:nowrap;">${ArabicNumberHelper.formatTransactionType(t)}</td>
            <td style="font-size:${13 * scale}px;font-weight:700;color:#000;text-align:center;">${displayNote.ifEmpty { "—" }}</td>
            <td style="font-size:${14 * scale}px;font-weight:800;color:#000000;" dir="ltr">$alaykum</td>
            <td style="font-size:${14 * scale}px;font-weight:800;color:#1E824C;" dir="ltr">$lakum</td>
            <td style="font-size:${14 * scale}px;font-weight:800;color:#D32F2F;" dir="ltr">$balanceAfterStr</td>
          </tr>
        """.trimIndent()
      }
    }

    val finalBalTitle = if (finalBalance > 0.005) {
      "الباقي عليكم"
    } else if (finalBalance < -0.005) {
      "الباقي لكم"
    } else {
      "الباقي (لكم / عليكم)"
    }

    val finalBalVal = if (Math.abs(finalBalance) < 0.005) {
      "0"
    } else {
      "$finalSymBlue ${ArabicNumberHelper.formatAmount(Math.abs(finalBalance))}"
    }

    val exchangeNoteHtml = ""

    return """
      <!DOCTYPE html>
      <html lang="ar" dir="rtl">
      <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>كشف حساب ${customer.accountNumber}</title>
      $googleFontLink
      <style>
      @page {
        size: A4 portrait;
        margin: 5mm 6mm;
      }
      * {
        box-sizing: border-box;
        font-family: $fontCss;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      html, body {
        background: #fff;
        margin: 0;
        padding: 0;
        width: 100%;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      body {
        padding: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        box-sizing: border-box;
      }
      .invoice-card {
        width: 100%;
        max-width: 880px;
        background: #fff;
        border: 3.5px solid ${reportConfig.tableBorderColorHex};
        border-radius: 12px;
        padding: 16px 18px 20px 18px;
        position: relative;
        box-sizing: border-box;
        overflow: hidden;
      }
      @media print {
        body {
          padding: 0 !important;
          background: transparent !important;
        }
        .invoice-card {
          width: 100% !important;
          max-width: 100% !important;
          border: 2.5px solid ${reportConfig.tableBorderColorHex} !important;
          border-radius: 8px !important;
          padding: 12px 14px !important;
        }
      }
      .inv-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;position:relative;z-index:2;}
      .company-info{font-size:${18 * scale}px;line-height:1.4;font-weight:700;width:37%;color:${reportConfig.headerColorHex};}
      .store-name-ar{font-size:${22 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:2px;white-space:nowrap;}
      .store-name-en{font-size:${19 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:2px;white-space:nowrap;}
      .store-address{font-size:${18 * scale}px;font-weight:700;color:${reportConfig.primaryTextColorHex};margin:2px 0;}
      .store-phone{font-size:${18 * scale}px;font-weight:800;color:${reportConfig.headerColorHex};}
      .phone-num{font-size:${18 * scale}px;font-weight:800;color:${reportConfig.headerColorHex};}
      .brand-box{text-align:center;width:24%;display:flex;justify-content:center;align-items:center;}
      .logo-container{width:92px;height:92px;border-radius:50%;overflow:hidden;display:flex;justify-content:center;align-items:center;}
      .meta-bar-top{border-top:3px solid ${reportConfig.tableBorderColorHex};padding-top:10px;margin-top:8px;position:relative;z-index:2;}
      .meta-row-top{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
      .inv-account-box{font-weight:900;font-size:${14.5 * scale}px;color:${reportConfig.primaryTextColorHex};flex:1;text-align:right;}
      .inv-type-tag-box{flex:0 0 auto;text-align:center;padding:0 8px;}
      .inv-type-tag{background:${reportConfig.headerColorHex};color:#fff;padding:5px 18px;font-size:${14 * scale}px;font-weight:900;border-radius:8px;display:inline-block;white-space:nowrap;line-height:1.2;}
      .inv-date-box{font-weight:900;font-size:${14.5 * scale}px;color:${reportConfig.primaryTextColorHex};flex:1;text-align:left;}
      .inv-customer-box{font-weight:900;font-size:${17.5 * scale}px;color:#C62828;text-align:right;margin-top:4px;margin-bottom:8px;}
      .statement-table{width:100%;border-collapse:collapse;border:3px solid ${reportConfig.tableBorderColorHex};margin-bottom:0;position:relative;z-index:2;background:transparent;}
      .statement-table th,.statement-table td{border:2px solid ${reportConfig.tableBorderColorHex};text-align:center;padding:9px 6px;vertical-align:middle;}
      .statement-table th{background:rgba(255,255,255,0.92);color:${reportConfig.headerColorHex};font-size:${14 * scale}px;font-weight:800;}
      .statement-table td{background:rgba(255,255,255,0.72);color:${reportConfig.primaryTextColorHex};}
      .footer-summary-container{display:flex;justify-content:space-between;gap:12px;margin-top:16px;position:relative;z-index:2;}
      .card-watermark{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);display:flex;flex-direction:column;justify-content:center;align-items:center;pointer-events:none;z-index:0;opacity:0.12;width:100%;text-align:center;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;}
      .wm-logo-img{width:200px;height:200px;object-fit:cover;border-radius:50%;margin-bottom:8px;display:block;border:4px solid ${reportConfig.headerColorHex};}
      .wm-logo-svg{width:200px;height:200px;margin-bottom:8px;display:flex;justify-content:center;align-items:center;}
      .wm-title-ar{font-size:26px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:4px;white-space:nowrap;line-height:1.2;}
      .wm-title-en{font-size:17px;font-weight:900;color:${reportConfig.headerColorHex};letter-spacing:3px;white-space:nowrap;line-height:1.2;}
      @media print {
        .card-watermark{opacity:0.15 !important;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;}
      }
      </style>
      </head>
      <body>
      <div class="invoice-card">
        ${getWatermarkHtml(storeConfig, reportConfig, context)}
        ${getCustomHeaderTitleHtml(reportConfig)}
        ${getCustomNoticeBadgeHtml(reportConfig)}
        <div class="inv-header">
          $storeInfoRightHtml
          <div class="brand-box">$logoHtml</div>
          $storeInfoLeftHtml
        </div>
        <div class="meta-bar-top">
          <div class="meta-row-top">
            $accountHtml
            <div class="inv-type-tag-box"><span class="inv-type-tag">كشف حساب</span></div>
            $dateHtml
          </div>
          <div class="inv-customer-box" style="color:${reportConfig.customerNameColorHex};">اسم العميل : ${customer.name}</div>
          $periodSubHeader
        </div>
        <table class="statement-table">
          <thead>
            <tr>
              <th style="width:21%;font-size:${14 * scale}px;font-weight:800;">التاريخ التوقيت</th>
              <th style="width:10%;white-space:nowrap;font-size:${14 * scale}px;font-weight:800;">النوع</th>
              <th style="width:33%;font-size:${14 * scale}px;font-weight:800;">البيان</th>
              <th style="width:12%;font-size:${14 * scale}px;font-weight:800;">عليكم</th>
              <th style="width:12%;font-size:${14 * scale}px;font-weight:800;">لكم</th>
              <th style="width:12%;font-size:${14 * scale}px;font-weight:800;">الرصيد بعد</th>
            </tr>
          </thead>
          <tbody>
            $rows
          </tbody>
        </table>
        <div class="footer-summary-container">
          <div style="flex:1;border:2px solid #EF5350;background:#fff;padding:12px 10px;border-radius:10px;text-align:center;">
            <div style="font-size:${14.5 * scale}px;font-weight:900;color:#C62828;">إجمالي عليكم</div>
            <div style="font-size:${23 * scale}px;font-weight:900;color:#C62828;margin-top:4px;" dir="ltr">$debitSymBlue ${ArabicNumberHelper.formatAmount(totalDebit)}</div>
          </div>
          <div style="flex:1;border:2px solid #66BB6A;background:#fff;padding:12px 10px;border-radius:10px;text-align:center;">
            <div style="font-size:${14.5 * scale}px;font-weight:900;color:#2E7D32;">الإجمالي لكم</div>
            <div style="font-size:${23 * scale}px;font-weight:900;color:#2E7D32;margin-top:4px;" dir="ltr">$creditSymBlue ${ArabicNumberHelper.formatAmount(totalCredit)}</div>
          </div>
          <div style="flex:1;border:2px solid #5E258D;background:#fff;padding:12px 10px;border-radius:10px;text-align:center;">
            <div style="font-size:${14.5 * scale}px;font-weight:900;color:#5E258D;">$finalBalTitle</div>
            <div style="font-size:${23 * scale}px;font-weight:900;color:#5E258D;margin-top:4px;" dir="ltr">$finalBalVal</div>
          </div>
        </div>
        ${getSignaturesHtml(reportConfig, storeConfig)}
        ${getCustomFooterTextHtml(reportConfig)}
      </div>
      </body>
      </html>
    """.trimIndent()
  }

  fun printStatement(
    context: Context,
    customer: Customer,
    storeConfig: StoreConfig,
    startDateStr: String = "",
    endDateStr: String = "",
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    exchangeRates: ExchangeRates = ExchangeRates()
  ) {
    val html = getStatementHtml(customer, storeConfig, startDateStr, endDateStr, reportConfig, context, exchangeRates)
    printHtml(context, html, "Statement_${customer.accountNumber}")
  }

  fun exportStatementToPdf(
    context: Context,
    customer: Customer,
    storeConfig: StoreConfig,
    startDateStr: String = "",
    endDateStr: String = "",
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    exchangeRates: ExchangeRates = ExchangeRates(),
    onComplete: (File?) -> Unit
  ) {
    val html = getStatementHtml(customer, storeConfig, startDateStr, endDateStr, reportConfig, context, exchangeRates)
    val safeName = customer.name.replace(Regex("[^\\p{L}\\p{Nd}_-]"), "_").trim('_').ifEmpty { customer.accountNumber }
    val fileName = "كشف_حساب_${safeName}.pdf"
    exportHtmlToPdf(context, html, fileName, onComplete)
  }

  fun getAllCustomersHtml(
    customers: List<Customer>,
    storeConfig: StoreConfig,
    title: String = "كشف حساب جميع العملاء",
    subtitle: String = "",
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    context: Context? = null
  ): String {
    val dateStr = ArabicNumberHelper.formatDateTime()
    val scale = reportConfig.fontScale
    val fontCss = getFontCssFamily(reportConfig.fontFamily)
    val googleFontLink = getGoogleFontLink(reportConfig.fontFamily)
    var totalBalance = 0.0
    var rows = ""

    val sortedCustomers = customers.sortedWith(
      compareByDescending<Customer> {
        ArabicNumberHelper.toEngDigits(it.accountNumber).toLongOrNull() ?: Long.MIN_VALUE
      }.thenByDescending {
        ArabicNumberHelper.toEngDigits(it.accountNumber).trim()
      }.thenBy {
        it.name.trim()
      }
    )

    sortedCustomers.forEach { c ->
      totalBalance += c.balance
      val rowColor = if (c.balance > 0) "color:#d32f2f;" else if (c.balance < 0) "color:#28a745;" else ""
      rows += """
        <tr>
          <td style="color:${reportConfig.customerAccountColorHex};font-weight:900;">${c.accountNumber}</td>
          <td style="color:${reportConfig.customerNameColorHex};font-weight:bold;">${c.name}</td>
          <td>${c.phone}</td>
          <td style="$rowColor font-weight:800;">${ArabicNumberHelper.formatAmount(c.balance)}</td>
          <td>${c.transactions.size}</td>
        </tr>
      """.trimIndent()
    }

    val subtitleHtml = if (subtitle.isNotBlank()) """<p style="text-align:center;color:#666;font-size:${12 * scale}px;margin-top:-6px;font-weight:bold;">$subtitle</p>""" else ""

    return """
      <!DOCTYPE html>
      <html lang="ar" dir="rtl">
      <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>$title</title>
      $googleFontLink
      <style>
      @page {
        size: A4 portrait;
        margin: 5mm 6mm;
      }
      * {
        box-sizing: border-box;
        font-family: $fontCss;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      body {
        background: #fff;
        margin: 0;
        padding: 8px;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      .invoice-card {
        width: 100%;
        background: #fff;
        border: 2px solid ${reportConfig.tableBorderColorHex};
        padding: 15px;
        box-sizing: border-box;
        position: relative;
        overflow: hidden;
      }
      @media print {
        body { padding: 0 !important; }
        .invoice-card { width: 100% !important; max-width: 100% !important; }
        .card-watermark { opacity: 0.15 !important; -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
      }
      .statement-table{width:100%;border-collapse:collapse;position:relative;z-index:2;background:transparent;}
      .statement-table th,.statement-table td{border:2px solid ${reportConfig.tableBorderColorHex};text-align:center;padding:7px;color:${reportConfig.primaryTextColorHex};}
      .statement-table th{background:${reportConfig.tableHeaderBgHex};color:${reportConfig.tableBorderColorHex};font-size:${14 * scale}px;font-weight:800;}
      .statement-table td{background:rgba(255,255,255,0.75);}
      .card-watermark{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);display:flex;flex-direction:column;justify-content:center;align-items:center;pointer-events:none;z-index:0;opacity:0.12;width:100%;text-align:center;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;}
      .wm-logo-img{width:200px;height:200px;object-fit:cover;border-radius:50%;margin-bottom:8px;display:block;border:4px solid #5E258D;}
      .wm-logo-svg{width:200px;height:200px;margin-bottom:8px;display:flex;justify-content:center;align-items:center;}
      .wm-title-ar{font-size:26px;font-weight:900;color:#5E258D;margin-bottom:4px;white-space:nowrap;line-height:1.2;}
      .wm-title-en{font-size:17px;font-weight:900;color:#5E258D;letter-spacing:3px;white-space:nowrap;line-height:1.2;}
      </style>
      </head>
      <body>
      <div class="invoice-card">
        ${getWatermarkHtml(storeConfig, reportConfig, context)}
        ${getCustomHeaderTitleHtml(reportConfig)}
        ${getCustomNoticeBadgeHtml(reportConfig)}
        <h2 style="text-align:center;color:${reportConfig.headerColorHex};position:relative;z-index:2;">${storeConfig.storeNameAr} - $title</h2>
        $subtitleHtml
        <p style="text-align:center;color:${reportConfig.primaryTextColorHex};position:relative;z-index:2;">التاريخ: $dateStr | عدد العملاء: ${customers.size}</p>
        <table class="statement-table">
          <thead>
            <tr>
              <th>رقم الحساب</th><th>الاسم</th><th>الهاتف</th><th>الرصيد الحالي</th><th>عدد المعاملات</th>
            </tr>
          </thead>
          <tbody>$rows</tbody>
        </table>
        <h3 style="margin-top:15px;color:${if (totalBalance > 0) "#d32f2f" else "#28a745"};position:relative;z-index:2;">
          إجمالي أرصدة العملاء: ${ArabicNumberHelper.formatAmount(totalBalance)}
        </h3>
        ${getSignaturesHtml(reportConfig, storeConfig)}
        ${getCustomFooterTextHtml(reportConfig)}
      </div>
      </body>
      </html>
    """.trimIndent()
  }

  fun printAllCustomers(
    context: Context,
    customers: List<Customer>,
    storeConfig: StoreConfig,
    title: String = "كشف حساب جميع العملاء",
    subtitle: String = "",
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig()
  ) {
    val html = getAllCustomersHtml(customers, storeConfig, title, subtitle, reportConfig, context)
    printHtml(context, html, "All_Customers_Statement")
  }

  fun exportAllCustomersToPdf(
    context: Context,
    customers: List<Customer>,
    storeConfig: StoreConfig,
    title: String = "كشف حساب جميع العملاء",
    subtitle: String = "",
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    onComplete: (File?) -> Unit
  ) {
    val html = getAllCustomersHtml(customers, storeConfig, title, subtitle, reportConfig, context)
    val fileName = "كشف_جميع_العملاء.pdf"
    exportHtmlToPdf(context, html, fileName, onComplete)
  }

  fun getVoucherHtml(
    voucher: VoucherItem,
    storeConfig: StoreConfig,
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    context: Context? = null
  ): String {
    val isPayment = voucher.type == "صرف"
    val titleAr = if (isPayment) "سند صـــــرف" else "سند قـــــبض"
    val titleEn = if (isPayment) "PAYMENT VOUCHER" else "RECEIPT VOUCHER"
    val mainColor = if (isPayment) "#C62828" else "#2E7D32"
    val lightBg = if (isPayment) "#FFEBEE" else "#E8F5E9"
    val sym = ArabicNumberHelper.getCurrencySymbol(voucher.currency)
    val currName = ArabicNumberHelper.getCurrencyName(voucher.currency)
    val amountInWords = if (reportConfig.showAmountInWords) {
      "${ArabicNumberHelper.numberToArabicWords(voucher.amount)} $currName"
    } else ""
    val recipientLabel = if (isPayment) "يصرف للمكرم / السيد:" else "استلمنا من المكرم / السيد:"
    val voucherDate = if (voucher.date.isNotBlank()) voucher.date else ArabicNumberHelper.formatDateTime()
    val scale = reportConfig.fontScale
    val fontCss = getFontCssFamily(reportConfig.fontFamily)
    val googleFontLink = getGoogleFontLink(reportConfig.fontFamily)

    val effectiveStoreNameAr = storeConfig.storeNameAr.ifBlank { "المملكة للإلكترونيات" }
    val effectiveStoreNameEn = storeConfig.storeNameEn.ifBlank { "ALMamlaka Electronics" }
    val effectiveAddressAr = storeConfig.addressAr.ifBlank { "إب شارع تعز" }
    val effectiveAddressEn = storeConfig.addressEn.ifBlank { "YEMEN Ibb" }
    val effectivePhone = storeConfig.phone.ifBlank { "772707736" }

    val storeInfoRightHtml = if (reportConfig.showStoreInfo) {
      """
        <div class="company-info" style="text-align:right;">
          <div class="store-name-ar">$effectiveStoreNameAr</div>
          <div class="store-address" style="margin:2px 0;">$effectiveAddressAr</div>
          <div class="store-phone">تلفون: <span class="phone-num">$effectivePhone</span></div>
          ${if (reportConfig.taxOrCrNumber.isNotBlank()) "<br><span style='font-size:11px;color:${reportConfig.primaryTextColorHex};'>الرقم الضريبي/السجل: ${reportConfig.taxOrCrNumber}</span>" else ""}
        </div>
      """.trimIndent()
    } else """<div class="company-info"></div>"""

    val storeInfoLeftHtml = if (reportConfig.showStoreInfo) {
      """
        <div class="company-info" style="text-align:left;" dir="ltr">
          <div class="store-name-en">$effectiveStoreNameEn</div>
          <div class="store-address" style="margin:2px 0;">$effectiveAddressEn</div>
          <div class="store-phone">TEL: <span class="phone-num">$effectivePhone</span></div>
        </div>
      """.trimIndent()
    } else """<div class="company-info"></div>"""

    val logoHtml = if (reportConfig.showLogo) getLogoHtml(storeConfig, context) else ""

    val dateHtml = if (reportConfig.showDateTime) {
      """<div class="meta-item">التاريخ: <span class="meta-val" dir="ltr">$voucherDate</span></div>"""
    } else """<div class="meta-item"></div>"""

    val branchHtml = if (reportConfig.showBranch) {
      """<div class="meta-item">الفرع: <span style="color:#0070ba;">${storeConfig.branch}</span></div>"""
    } else """<div class="meta-item"></div>"""

    val accountHtml = if (reportConfig.showCustomerAccountNumber && voucher.account.isNotBlank()) {
      """ <span style="color:${reportConfig.customerAccountColorHex};font-size:${13 * scale}px;">(رقم الحساب: ${voucher.account})</span>"""
    } else ""

    val signaturesHtml = if (reportConfig.showSignatures) {
      """
        <div class="signatures-row">
          <div class="sig-box">
            <div class="sig-title">توقيع أمين الصندوق / المحاسب</div>
            <div class="sig-line"></div>
          </div>
          ${if (reportConfig.showStampSeal) """
            <div style="text-align:center;width:20%;display:flex;flex-direction:column;align-items:center;justify-content:center;">
              <div style="border:2.5px solid #C62828;color:#C62828;border-radius:50%;width:65px;height:65px;display:flex;flex-direction:column;align-items:center;justify-content:center;transform:rotate(-8deg);font-weight:900;font-size:9px;line-height:1.2;">
                <div>معتمد</div>
                <div style="font-size:6px;letter-spacing:1px;">APPROVED</div>
                <div style="font-size:11px;">✓</div>
              </div>
            </div>
          """ else """<div style="width:20%;"></div>"""}
          <div class="sig-box">
            <div class="sig-title">توقيع المستلم / العميل</div>
            <div class="sig-line"></div>
          </div>
        </div>
      """.trimIndent()
    } else ""

    return """
      <!DOCTYPE html>
      <html lang="ar" dir="rtl">
      <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>$titleAr ${voucher.voucherNum}</title>
      $googleFontLink
      <style>
      @page {
        size: A4 portrait;
        margin: 5mm 6mm;
      }
      * {
        box-sizing: border-box;
        font-family: $fontCss;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      html, body {
        background: #fff;
        margin: 0;
        padding: 0;
        width: 100%;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      body {
        padding: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        box-sizing: border-box;
      }
      .voucher-card {
        width: 100%;
        max-width: 820px;
        background: #fff;
        border: 2.5px solid $mainColor;
        border-radius: 4px;
        padding: 16px;
        position: relative;
        overflow: hidden;
        box-sizing: border-box;
      }
      @media print {
        body { padding: 0 !important; }
        .voucher-card { width: 100% !important; max-width: 100% !important; page-break-inside: avoid !important; }
        .card-watermark { opacity: 0.15 !important; -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
      }
      .card-watermark{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);display:flex;flex-direction:column;justify-content:center;align-items:center;pointer-events:none;z-index:0;opacity:0.12;width:100%;text-align:center;-webkit-print-color-adjust:exact !important;print-color-adjust:exact !important;}
      .wm-logo-img{width:200px;height:200px;object-fit:cover;border-radius:50%;margin-bottom:8px;display:block;border:4px solid ${reportConfig.headerColorHex};}
      .wm-logo-svg{width:200px;height:200px;margin-bottom:8px;display:flex;justify-content:center;align-items:center;}
      .wm-title-ar{font-size:26px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:4px;white-space:nowrap;line-height:1.2;}
      .wm-title-en{font-size:17px;font-weight:900;color:${reportConfig.headerColorHex};letter-spacing:3px;white-space:nowrap;line-height:1.2;}
      .header-row{display:flex;justify-content:space-between;align-items:center;border-bottom:2px solid $mainColor;padding-bottom:10px;margin-bottom:12px;position:relative;z-index:2;}
      .company-info{width:38%;font-size:${18 * scale}px;line-height:1.4;font-weight:700;color:${reportConfig.headerColorHex};}
      .store-name-ar{font-size:${22 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:2px;white-space:nowrap;}
      .store-name-en{font-size:${19 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};margin-bottom:2px;white-space:nowrap;}
      .store-address{font-size:${18 * scale}px;font-weight:700;color:${reportConfig.primaryTextColorHex};margin:2px 0;}
      .store-phone{font-size:${18 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};}
      .phone-num{font-size:${18 * scale}px;font-weight:900;color:${reportConfig.headerColorHex};}
      .brand-box{width:24%;display:flex;justify-content:center;align-items:center;}
      .voucher-title-box{text-align:center;margin:10px 0 14px 0;background:$lightBg;padding:8px;border-radius:6px;border:1.5px dashed $mainColor;}
      .voucher-title{font-size:${22 * scale}px;font-weight:900;color:$mainColor;}
      .sub-title{font-size:${12 * scale}px;font-weight:800;color:#555;letter-spacing:1px;margin-top:2px;}
      .meta-grid{display:flex;justify-content:space-between;margin-bottom:12px;background:#f8f9fa;padding:8px 12px;border-radius:4px;border:1px solid #e9ecef;}
      .meta-item{font-size:${14 * scale}px;font-weight:800;color:${reportConfig.primaryTextColorHex};}
      .meta-val{color:$mainColor;font-weight:900;}
      .amount-display{display:flex;justify-content:space-between;align-items:center;background:$lightBg;border:2px solid $mainColor;padding:10px 14px;border-radius:6px;margin-bottom:14px;}
      .amount-num{font-size:${22 * scale}px;font-weight:900;color:$mainColor;direction:ltr;}
      .amount-words{font-size:${14 * scale}px;font-weight:800;color:${reportConfig.primaryTextColorHex};}
      .fields-table{width:100%;border-collapse:collapse;margin-bottom:16px;}
      .fields-table td{padding:8px 6px;border-bottom:1px dashed #ccc;}
      .field-label{width:25%;font-weight:800;color:${reportConfig.primaryTextColorHex};font-size:${14 * scale}px;}
      .field-val{width:75%;font-weight:900;color:${reportConfig.primaryTextColorHex};font-size:${15 * scale}px;}
      .signatures-row{display:flex;justify-content:space-between;align-items:flex-end;margin-top:25px;padding:0 15px 10px 15px;}
      .sig-box{width:38%;text-align:center;}
      .sig-title{font-size:${13 * scale}px;font-weight:800;color:${reportConfig.primaryTextColorHex};margin-bottom:32px;}
      .sig-line{border-top:1.5px solid #6c757d;}
      .footer-note{text-align:center;font-size:${11 * scale}px;color:#888;margin-top:14px;border-top:1px solid #eee;padding-top:6px;}
      </style>
      </head>
      <body>
      <div class="voucher-card">
        ${getWatermarkHtml(storeConfig, reportConfig, context)}
        ${getCustomHeaderTitleHtml(reportConfig)}
        ${getCustomNoticeBadgeHtml(reportConfig)}
        <div class="header-row">
          $storeInfoRightHtml
          <div class="brand-box">$logoHtml</div>
          $storeInfoLeftHtml
        </div>
        <div class="voucher-title-box">
          <div class="voucher-title">$titleAr</div>
          <div class="sub-title">$titleEn</div>
        </div>
        <div class="meta-grid">
          <div class="meta-item">رقم السند: <span class="meta-val">${voucher.voucherNum}</span></div>
          $branchHtml
          $dateHtml
        </div>
        <div class="amount-display">
          <div class="amount-words"><strong>المبلغ كتابة:</strong> $amountInWords</div>
          <div class="amount-num">${ArabicNumberHelper.formatAmount(voucher.amount)} $sym</div>
        </div>
        <table class="fields-table">
          <tr>
            <td class="field-label">$recipientLabel</td>
            <td class="field-val"><span style="color:${reportConfig.customerNameColorHex};">${voucher.customerName}</span>$accountHtml</td>
          </tr>
          <tr>
            <td class="field-label">وذلك مقابل / البيان:</td>
            <td class="field-val">${voucher.note.ifEmpty { "تسديد حساب / حركة نقدية" }}</td>
          </tr>
        </table>
        $signaturesHtml
        <div class="footer-note">
          تم إصدار هذا السند عبر نظام إدارة الحسابات والفواتير الإلكتروني - ${storeConfig.storeNameAr}
        </div>
        ${getCustomFooterTextHtml(reportConfig)}
      </div>
      </body>
      </html>
    """.trimIndent()
  }

  fun printVoucher(
    context: Context,
    voucher: VoucherItem,
    storeConfig: StoreConfig,
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig()
  ) {
    val isPayment = voucher.type == "صرف"
    val html = getVoucherHtml(voucher, storeConfig, reportConfig, context)
    printHtml(context, html, "${if (isPayment) "Payment" else "Receipt"}_Voucher_${voucher.voucherNum}")
  }

  fun exportVoucherToPdf(
    context: Context,
    voucher: VoucherItem,
    storeConfig: StoreConfig,
    reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
    onComplete: (File?) -> Unit
  ) {
    val html = getVoucherHtml(voucher, storeConfig, reportConfig, context)
    val typeName = if (voucher.type == "صرف") "سند_صرف" else "سند_قبض"
    val fileName = "${typeName}_رقم_${voucher.voucherNum}.pdf"
    exportHtmlToPdf(context, html, fileName, onComplete)
  }

  fun exportHtmlToPdf(
    context: Context,
    html: String,
    fileName: String,
    onComplete: (File?) -> Unit
  ) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post {
      try {
        val cleanFileName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
        val exportDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "PDF_Exports").apply { mkdirs() }
        val outputFile = File(exportDir, cleanFileName)
        if (outputFile.exists()) {
          outputFile.delete()
        }

        val webView = WebView(context)
        activeWebViews.add(webView)

        webView.settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          useWideViewPort = false
          loadWithOverviewMode = false
          cacheMode = WebSettings.LOAD_DEFAULT
          loadsImagesAutomatically = true
        }

        webView.webViewClient = object : WebViewClient() {
          override fun onPageFinished(view: WebView?, url: String?) {
            // Delay slightly to ensure Web fonts and SVG shapes are rendered cleanly
            mainHandler.postDelayed({
              try {
                val printAdapter = webView.createPrintDocumentAdapter(cleanFileName)
                val printAttributes = PrintAttributes.Builder()
                  .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                  .setResolution(PrintAttributes.Resolution("pdf_res_ultra", "Ultra High Resolution", 600, 600))
                  .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                  .build()

                PdfPrintHelper.print(
                  printAdapter,
                  printAttributes,
                  outputFile,
                  object : PdfPrintHelper.Callback {
                    override fun onSuccess(file: File?) {
                      activeWebViews.remove(webView)
                      mainHandler.post { onComplete(file ?: outputFile) }
                    }

                    override fun onError(error: String?) {
                      activeWebViews.remove(webView)
                      mainHandler.post { onComplete(null) }
                    }
                  }
                )
              } catch (e: Exception) {
                activeWebViews.remove(webView)
                mainHandler.post { onComplete(null) }
              }
            }, 300)
          }
        }

        webView.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null)
      } catch (e: Exception) {
        mainHandler.post { onComplete(null) }
      }
    }
  }

  fun sharePdf(context: Context, file: File, subject: String = "مشاركة مستند PDF") {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(intent, "مشاركة ملف PDF عبر:").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "تعذر مشاركة الملف: ${e.localizedMessage ?: e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun shareInvoiceText(context: Context, invoice: InvoiceData, storeConfig: StoreConfig) {
    try {
      val sym = ArabicNumberHelper.getCurrencySymbol(invoice.currency)
      val currName = ArabicNumberHelper.getCurrencyName(invoice.currency)
      val itemsText = buildString {
        appendLine("• ${invoice.desc} (${invoice.qty}) = ${ArabicNumberHelper.formatAmount(invoice.price)} $sym")
        invoice.extraItems.forEach { extra ->
          val total = extra.price * extra.qty
          val eSym = ArabicNumberHelper.getCurrencySymbol(extra.currency)
          appendLine("• ${extra.description} (${extra.qty}) = ${ArabicNumberHelper.formatAmount(total)} $eSym")
        }
      }
      val storeName = storeConfig.storeNameAr.ifEmpty { "المملكة beIN SPORTS للإلكترونيات" }
      val text = """
        🧾 *فاتورة $storeName*
        رقم الفاتورة: #${invoice.invNum} (${invoice.invType})
        👤 العميل: ${invoice.customerName.ifEmpty { "عميل نقدي" }}
        📅 التاريخ: ${invoice.createdAt}
        ----------------------------------
        📋 *تفاصيل الفاتورة:*
        $itemsText
        ----------------------------------
        💰 *المجموع النهائي: ${ArabicNumberHelper.formatAmount(invoice.grandTotal)} $sym*
        ${ArabicNumberHelper.numberToArabicWords(invoice.grandTotal)} $currName
        
        ${if (storeConfig.phone.isNotBlank()) "📞 للتواصل: ${storeConfig.phone}" else ""}
        ${if (storeConfig.branch.isNotBlank()) "📍 ${storeConfig.branch}" else ""}
        ✨ شكراً لتعاملكم معنا ونسعد بخدمتكم دائماً!
      """.trimIndent()

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "فاتورة رقم ${invoice.invNum} - $storeName")
      }
      val chooser = Intent.createChooser(intent, "إرسال الفاتورة للعميل عبر:").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "تعذر مشاركة الفاتورة: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun openPdf(context: Context, file: File) {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      // Fallback: If no direct PDF viewer is installed, open sharing dialog
      sharePdf(context, file, file.nameWithoutExtension)
    }
  }

  private fun printHtml(context: Context, html: String, jobName: String) {
    try {
      val webView = WebView(context)
      webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        useWideViewPort = false
        loadWithOverviewMode = false
        cacheMode = WebSettings.LOAD_DEFAULT
        loadsImagesAutomatically = true
      }
      webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
          Handler(Looper.getMainLooper()).postDelayed({
            try {
              val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
              val adapter = webView.createPrintDocumentAdapter(jobName)
              val attributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("res_ultra", "Ultra High Resolution", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
              printManager?.print(jobName, adapter, attributes)
            } catch (_: Exception) {}
          }, 300)
        }
      }
      webView.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null)
    } catch (_: Exception) {
      // In case print manager is not available, fallback silently
    }
  }
}
