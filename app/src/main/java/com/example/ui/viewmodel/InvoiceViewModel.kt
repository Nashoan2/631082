package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppActionButton
import com.example.data.AppThemePreset
import com.example.data.ButtonLayout
import com.example.data.ButtonSize
import com.example.data.Customer
import com.example.data.CustomerReminder
import com.example.util.NotificationHelper
import com.example.data.ExchangeRates
import com.example.data.ExtraItem
import com.example.data.HomeScreenStyle
import com.example.data.InvoiceData
import com.example.data.InvoiceRepository
import com.example.data.ReportCustomizationConfig
import com.example.data.StoreConfig
import com.example.data.UiCustomizationConfig
import com.example.data.VoucherItem
import com.example.data.room.RoomBackupSnapshotEntity
import com.example.data.room.RoomStats
import com.example.util.ArabicNumberHelper
import com.example.util.PrintHelper
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ExportedPdfInfo(
  val file: File,
  val title: String,
  val sizeFormatted: String = "",
  val source: String = "" // "invoice", "statement", "all_customers", "voucher"
)

enum class AppScreen {
  MAIN,
  INVOICE_REPORT,
  CUSTOMER_STATEMENT,
  CUSTOMER_INVOICE_DISPLAY,
  CURRENCY_CONVERTER,
  PERCENTAGE_CALCULATOR,
  FULL_CALCULATOR
}

data class UiState(
  val currentScreen: AppScreen = AppScreen.MAIN,
  val storeConfig: StoreConfig = StoreConfig(),
  val customers: List<Customer> = emptyList(),
  val exchangeRates: ExchangeRates = ExchangeRates(),
  val savedInvoices: List<InvoiceData> = emptyList(),
  val nextReceiptVoucherNum: Int = 1,
  val nextPaymentVoucherNum: Int = 1,

  // Invoice form state
  val isFormVisible: Boolean = false,
  val editingInvoiceId: Long? = null,
  val invoiceDate: String = ArabicNumberHelper.formatDateTime(),
  val invNum: String = "1",
  val invType: String = "نقداً",
  val customerAccount: String = "",
  val customerName: String = "",
  val cardId: String = "",
  val price: String = "",
  val subscriptionType: String = "months", // "months" or "tournament"
  val qty: String = "3",
  val desc: String = "تجديد باقة تميز",
  val endDate: String = "",
  val currency: String = "YER",
  val extraItems: List<ExtraItem> = emptyList(),

  // Report & Statement preview
  val previewInvoice: InvoiceData? = null,
  val statementCustomer: Customer? = null,
  val statementStartDate: String = "",
  val statementEndDate: String = "",
  val showDateRangeDialog: Boolean = false,
  val pendingStatementCustomer: Customer? = null,
  val screenshotRequest: String? = null,

  // Modals
  val showCustomersModal: Boolean = false,
  val customersModalInitialTab: Int = -1,
  val showSavedInvoicesModal: Boolean = false,
  val showSettingsModal: Boolean = false,
  val showRoomBackupModal: Boolean = false,
  val roomSnapshots: List<RoomBackupSnapshotEntity> = emptyList(),
  val roomStats: RoomStats? = null,
  val isRoomOperationInProgress: Boolean = false,
  val toastMessage: String? = null,
  val exportedPdf: ExportedPdfInfo? = null,
  val isExportingPdf: Boolean = false,
  val uiCustomizationConfig: UiCustomizationConfig = UiCustomizationConfig(),
  val showUiCustomizerModal: Boolean = false,
  val reportCustomizationConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
  val showReportCustomizerModal: Boolean = false,
  val showAppThemeCustomizerModal: Boolean = false,
  val showExitConfirmDialog: Boolean = false,
  val customerReminders: List<CustomerReminder> = emptyList(),
  val isAutoDailyBackupEnabled: Boolean = false,
  val lastAutoBackupDate: String = "",
  val dueRemindersCount: Int = 0,
  val showDueRemindersAlert: Boolean = false,
  val activeFiscalYear: Int = 2026,
  val availableFiscalYears: List<Int> = listOf(2026),
  val showYearEndClosingModal: Boolean = false
)

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = InvoiceRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(
    UiState(
      storeConfig = repository.storeConfig,
      customers = repository.customers,
      exchangeRates = repository.exchangeRates,
      savedInvoices = repository.savedInvoices,
      nextReceiptVoucherNum = repository.nextReceiptVoucherNum,
      nextPaymentVoucherNum = repository.nextPaymentVoucherNum,
      uiCustomizationConfig = repository.uiCustomizationConfig,
      reportCustomizationConfig = repository.reportCustomizationConfig,
      invNum = repository.getNextInvoiceNumber().ifEmpty { "1" },
      endDate = calculateInitialExpiryDate(3),
      customerReminders = repository.customerReminders,
      isAutoDailyBackupEnabled = repository.isAutoDailyBackupEnabled,
      lastAutoBackupDate = repository.lastAutoBackupDate,
      dueRemindersCount = repository.customerReminders.count { !it.isCompleted && it.dueTimestamp <= System.currentTimeMillis() },
      showDueRemindersAlert = repository.customerReminders.any { !it.isCompleted && it.dueTimestamp <= System.currentTimeMillis() },
      activeFiscalYear = repository.activeFiscalYear,
      availableFiscalYears = repository.availableFiscalYears,
      showYearEndClosingModal = false
    )
  )
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private var extraIdCounter = 0L

  init {
    calculateExpiryDate()
    viewModelScope.launch {
      try {
        repository.recalculateAllCustomerBalances()
        _uiState.value = _uiState.value.copy(
          customers = repository.customers,
          savedInvoices = repository.savedInvoices
        )
        performDailyAutoBackupIfNeeded(getApplication())
        NotificationHelper.checkAndNotifyDueReminders(
          getApplication(),
          repository.customerReminders,
          repository.storeConfig.storeNameAr.ifBlank { "المتجر" }
        )
      } catch (_: Exception) {}
    }
  }

  fun clearToast() {
    _uiState.value = _uiState.value.copy(toastMessage = null)
  }

  fun showToast(msg: String) {
    _uiState.value = _uiState.value.copy(toastMessage = msg)
  }

  fun dismissExportedPdfDialog() {
    _uiState.value = _uiState.value.copy(exportedPdf = null)
  }

  fun exportInvoiceToPdf(context: Context, invoice: InvoiceData, autoShare: Boolean = false, customReportConfig: ReportCustomizationConfig? = null) {
    val configToUse = customReportConfig ?: _uiState.value.reportCustomizationConfig
    _uiState.value = _uiState.value.copy(isExportingPdf = true)
    showToast("⏳ جاري تجهيز وتصدير ملف PDF للفاتورة...")
    PrintHelper.exportInvoiceToPdf(context, invoice, _uiState.value.storeConfig, configToUse) { file ->
      _uiState.value = _uiState.value.copy(isExportingPdf = false)
      if (file != null && file.exists()) {
        val sizeKb = String.format(Locale.US, "%.1f", file.length() / 1024.0) + " KB"
        val info = ExportedPdfInfo(
          file = file,
          title = "فاتورة رقم ${invoice.invNum.ifEmpty { "1" }}",
          sizeFormatted = sizeKb,
          source = "invoice"
        )
        _uiState.value = _uiState.value.copy(exportedPdf = info)
        if (autoShare) {
          PrintHelper.sharePdf(context, file, info.title)
        }
      } else {
        showToast("❌ حدث خطأ أثناء تصدير ملف PDF للفاتورة.")
      }
    }
  }

  fun exportStatementToPdf(
    context: Context,
    customer: Customer,
    startDateStr: String = "",
    endDateStr: String = "",
    autoShare: Boolean = false
  ) {
    _uiState.value = _uiState.value.copy(isExportingPdf = true)
    showToast("⏳ جاري تجهيز وتصدير كشف الحساب بصيغة PDF...")
    PrintHelper.exportStatementToPdf(context, customer, _uiState.value.storeConfig, startDateStr, endDateStr, _uiState.value.reportCustomizationConfig, _uiState.value.exchangeRates) { file ->
      _uiState.value = _uiState.value.copy(isExportingPdf = false)
      if (file != null && file.exists()) {
        val sizeKb = String.format(Locale.US, "%.1f", file.length() / 1024.0) + " KB"
        val periodText = if (startDateStr.isNotBlank() && endDateStr.isNotBlank()) " (الفترة: $startDateStr إلى $endDateStr)" else ""
        val info = ExportedPdfInfo(
          file = file,
          title = "كشف حساب: ${customer.name}$periodText",
          sizeFormatted = sizeKb,
          source = "statement"
        )
        _uiState.value = _uiState.value.copy(exportedPdf = info)
        if (autoShare) {
          PrintHelper.sharePdf(context, file, info.title)
        }
      } else {
        showToast("❌ حدث خطأ أثناء تصدير كشف الحساب إلى PDF.")
      }
    }
  }

  fun exportAllCustomersToPdf(context: Context, autoShare: Boolean = false) {
    _uiState.value = _uiState.value.copy(isExportingPdf = true)
    showToast("⏳ جاري تجهيز كشف جميع العملاء بصيغة PDF...")
    val sortedCustomers = _uiState.value.customers.sortedWith(
      compareByDescending<Customer> {
        ArabicNumberHelper.toEngDigits(it.accountNumber).toLongOrNull() ?: Long.MIN_VALUE
      }.thenByDescending {
        ArabicNumberHelper.toEngDigits(it.accountNumber).trim()
      }.thenBy {
        it.name.trim()
      }
    )
    PrintHelper.exportAllCustomersToPdf(
      context = context,
      customers = sortedCustomers,
      storeConfig = _uiState.value.storeConfig,
      reportConfig = _uiState.value.reportCustomizationConfig
    ) { file ->
      _uiState.value = _uiState.value.copy(isExportingPdf = false)
      if (file != null && file.exists()) {
        val sizeKb = String.format(Locale.US, "%.1f", file.length() / 1024.0) + " KB"
        val info = ExportedPdfInfo(
          file = file,
          title = "كشف حساب جميع العملاء",
          sizeFormatted = sizeKb,
          source = "all_customers"
        )
        _uiState.value = _uiState.value.copy(exportedPdf = info)
        if (autoShare) {
          PrintHelper.sharePdf(context, file, info.title)
        }
      } else {
        showToast("❌ حدث خطأ أثناء تصدير كشف العملاء إلى PDF.")
      }
    }
  }

  fun exportCustomersWithBalanceToPdf(context: Context, autoShare: Boolean = false) {
    _uiState.value = _uiState.value.copy(isExportingPdf = true)
    showToast("⏳ جاري تجهيز كشف حساب العملاء ذوي الأرصدة بصيغة PDF...")
    val customersWithBalance = _uiState.value.customers
      .filter { Math.abs(it.balance) > 0.001 }
      .sortedByDescending { it.balance }
    PrintHelper.exportAllCustomersToPdf(
      context = context,
      customers = customersWithBalance,
      storeConfig = _uiState.value.storeConfig,
      title = "كشف حساب العملاء (الذين في حساباتهم أرصدة فقط)",
      subtitle = "العملاء الذين في حساباتهم أرصدة قائمة فقط - مستبعد الأرصدة الصفرية",
      reportConfig = _uiState.value.reportCustomizationConfig
    ) { file ->
      _uiState.value = _uiState.value.copy(isExportingPdf = false)
      if (file != null && file.exists()) {
        val sizeKb = String.format(Locale.US, "%.1f", file.length() / 1024.0) + " KB"
        val info = ExportedPdfInfo(
          file = file,
          title = "كشف حساب العملاء (ذوي الأرصدة فقط)",
          sizeFormatted = sizeKb,
          source = "customers_with_balance"
        )
        _uiState.value = _uiState.value.copy(exportedPdf = info)
        if (autoShare) {
          PrintHelper.sharePdf(context, file, info.title)
        }
      } else {
        showToast("❌ حدث خطأ أثناء تصدير كشف العملاء ذوي الأرصدة إلى PDF.")
      }
    }
  }

  fun exportVoucherToPdf(context: Context, voucher: VoucherItem, autoShare: Boolean = false) {
    _uiState.value = _uiState.value.copy(isExportingPdf = true)
    showToast("⏳ جاري تجهيز السند بصيغة PDF...")
    PrintHelper.exportVoucherToPdf(context, voucher, _uiState.value.storeConfig, _uiState.value.reportCustomizationConfig) { file ->
      _uiState.value = _uiState.value.copy(isExportingPdf = false)
      if (file != null && file.exists()) {
        val sizeKb = String.format(Locale.US, "%.1f", file.length() / 1024.0) + " KB"
        val typeTitle = if (voucher.type == "صرف") "سند صرف" else "سند قبض"
        val info = ExportedPdfInfo(
          file = file,
          title = "$typeTitle رقم ${voucher.voucherNum}",
          sizeFormatted = sizeKb,
          source = "voucher"
        )
        _uiState.value = _uiState.value.copy(exportedPdf = info)
        if (autoShare) {
          PrintHelper.sharePdf(context, file, info.title)
        }
      } else {
        showToast("❌ حدث خطأ أثناء تصدير السند إلى PDF.")
      }
    }
  }

  fun navigateTo(screen: AppScreen) {
    _uiState.value = _uiState.value.copy(currentScreen = screen)
  }

  fun setCustomersModalVisible(visible: Boolean, initialTab: Int = -1) {
    _uiState.value = _uiState.value.copy(
      showCustomersModal = visible,
      customersModalInitialTab = initialTab
    )
  }

  fun openCustomersModal(initialTab: Int = -1) {
    _uiState.value = _uiState.value.copy(
      showCustomersModal = true,
      customersModalInitialTab = initialTab
    )
  }

  fun printLatestSavedOrCurrent(context: Context) {
    val inv = getLatestSavedInvoiceOrCurrent()
    com.example.util.PrintHelper.printInvoice(context, inv, _uiState.value.storeConfig, _uiState.value.reportCustomizationConfig)
  }

  fun openReceiptVoucherShortcut() {
    _uiState.value = _uiState.value.copy(
      showCustomersModal = true,
      customersModalInitialTab = 0 // Tab "1. 💸 سند قبض"
    )
  }

  fun openAllCustomersShortcut() {
    _uiState.value = _uiState.value.copy(
      showCustomersModal = true,
      customersModalInitialTab = 2 // Tab "3. 📋 جميع العملاء"
    )
  }

  fun openCustomersWithBalanceShortcut() {
    _uiState.value = _uiState.value.copy(
      showCustomersModal = true,
      customersModalInitialTab = 6 // Tab "7. ⚖️ العملاء الذين في حساباتهم ارصده فقط"
    )
  }

  fun setSavedInvoicesModalVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showSavedInvoicesModal = visible)
  }

  fun setSettingsModalVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showSettingsModal = visible)
  }

  fun setExitConfirmDialogVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showExitConfirmDialog = visible)
  }

  fun toggleFormVisibility() {
    _uiState.value = _uiState.value.copy(isFormVisible = !_uiState.value.isFormVisible)
  }

  fun setFormVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(isFormVisible = visible)
  }

  fun updateInvNum(num: String) {
    _uiState.value = _uiState.value.copy(invNum = ArabicNumberHelper.toEngDigits(num))
  }

  fun updateInvType(type: String) {
    var newAccount = _uiState.value.customerAccount
    if (type == "أجل") {
      val cleanName = _uiState.value.customerName.trim()
      if (cleanName.isNotBlank() && newAccount.isBlank()) {
        val match = repository.customers.find { it.name.trim().equals(cleanName, ignoreCase = true) }
        newAccount = match?.accountNumber ?: repository.getNextAccountNumber()
      }
    }
    _uiState.value = _uiState.value.copy(
      invType = type,
      customerAccount = newAccount
    )
  }

  fun updateCustomerAccount(acc: String) {
    val clean = ArabicNumberHelper.toEngDigits(acc).trim()
    val match = repository.findCustomerByAccount(clean)
    _uiState.value = _uiState.value.copy(
      customerAccount = clean,
      customerName = match?.name ?: _uiState.value.customerName
    )
  }

  fun updateCustomerName(name: String) {
    val cleanName = name.trim()
    val match = repository.customers.find { it.name.trim().equals(cleanName, ignoreCase = true) }
    val newAccount = when {
      match != null -> match.accountNumber
      _uiState.value.invType == "أجل" -> {
        if (cleanName.isNotEmpty()) {
          if (_uiState.value.customerAccount.isBlank()) {
            repository.getNextAccountNumber()
          } else {
            _uiState.value.customerAccount
          }
        } else {
          if (repository.findCustomerByAccount(_uiState.value.customerAccount) == null) "" else _uiState.value.customerAccount
        }
      }
      else -> _uiState.value.customerAccount
    }
    _uiState.value = _uiState.value.copy(
      customerName = name,
      customerAccount = newAccount
    )
  }

  fun selectCustomerForInvoice(customer: Customer) {
    _uiState.value = _uiState.value.copy(
      customerAccount = customer.accountNumber,
      customerName = customer.name
    )
    showToast("✅ تم اختيار العميل: ${customer.name} (حساب: ${customer.accountNumber})")
  }

  fun generateNewCustomerAccount() {
    val next = repository.getNextAccountNumber()
    _uiState.value = _uiState.value.copy(customerAccount = next)
    showToast("⚡ تم تعيين رقم حساب جديد: $next")
  }

  fun updateCardId(cardId: String) {
    _uiState.value = _uiState.value.copy(cardId = ArabicNumberHelper.toEngDigits(cardId))
  }

  fun updatePrice(price: String) {
    _uiState.value = _uiState.value.copy(price = ArabicNumberHelper.toEngDigits(price))
  }

  fun updateSubscriptionType(type: String) {
    _uiState.value = _uiState.value.copy(subscriptionType = type)
    calculateExpiryDate()
  }

  fun updateQty(qty: String) {
    _uiState.value = _uiState.value.copy(qty = ArabicNumberHelper.toEngDigits(qty))
    calculateExpiryDate()
  }

  fun updateDesc(desc: String) {
    _uiState.value = _uiState.value.copy(desc = desc)
  }

  fun updateInvoiceDate(date: String) {
    _uiState.value = _uiState.value.copy(invoiceDate = date)
    calculateExpiryDate()
  }

  fun updateInvoiceDateOnly(invoiceId: Long, newDate: String) {
    val inv = repository.savedInvoices.find { it.id == invoiceId } ?: return
    val updated = inv.copy(createdAt = newDate)
    repository.updateSavedInvoice(updated)
    _uiState.value = _uiState.value.copy(savedInvoices = repository.savedInvoices)
    showToast("✅ تم تعديل تاريخ الفاتورة رقم (${inv.invNum}) إلى $newDate بنجاح.")
  }

  fun updateEndDate(endDate: String) {
    _uiState.value = _uiState.value.copy(endDate = endDate)
  }

  fun updateCurrency(curr: String) {
    _uiState.value = _uiState.value.copy(currency = curr)
  }

  private fun calculateInitialExpiryDate(months: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, months)
    cal.add(Calendar.DAY_OF_MONTH, -1)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    return sdf.format(cal.time)
  }

  fun calculateExpiryDate() {
    // في حالة تعديل الفاتورة لا يتغير تاريخ الانتهاء نهائياً
    if (_uiState.value.editingInvoiceId != null) return
    if (_uiState.value.subscriptionType == "tournament") return
    val months = _uiState.value.qty.toIntOrNull() ?: 3
    val cal = Calendar.getInstance()
    val invoiceDateStr = _uiState.value.invoiceDate.trim()
    val parsedDate = if (invoiceDateStr.isNotBlank()) ArabicNumberHelper.parseDate(invoiceDateStr) else null
    if (parsedDate != null) {
      cal.time = parsedDate
    }
    cal.add(Calendar.MONTH, months)
    cal.add(Calendar.DAY_OF_MONTH, -1)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    _uiState.value = _uiState.value.copy(endDate = sdf.format(cal.time))
  }

  fun addExtraItem(
    desc: String = "",
    price: Double = 0.0,
    qty: Double = 1.0,
    curr: String = _uiState.value.currency
  ) {
    val item = ExtraItem(
      id = ++extraIdCounter,
      description = desc,
      price = price,
      qty = qty,
      currency = curr
    )
    _uiState.value = _uiState.value.copy(
      extraItems = _uiState.value.extraItems + item
    )
  }

  fun removeExtraItem(id: Long) {
    _uiState.value = _uiState.value.copy(
      extraItems = _uiState.value.extraItems.filter { it.id != id }
    )
  }

  fun updateExtraItem(id: Long, desc: String, price: Double, qty: Double, curr: String) {
    val updated = _uiState.value.extraItems.map {
      if (it.id == id) it.copy(description = desc, price = price, qty = qty, currency = curr) else it
    }
    _uiState.value = _uiState.value.copy(extraItems = updated)
  }

  fun setUiCustomizerModalVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showUiCustomizerModal = visible)
  }

  fun updateButtonSize(size: ButtonSize) {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(buttonSize = size)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("تم تغيير حجم الأزرار إلى: ${size.label}")
  }

  fun updateButtonLayout(layout: ButtonLayout) {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(buttonLayout = layout)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("تم تغيير نمط التخطيط إلى: ${layout.label}")
  }

  fun toggleQuickShortcutsBar(show: Boolean) {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(showQuickShortcutsBar = show)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
  }

  fun reorderButtons(fromIndex: Int, toIndex: Int) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.buttons.toMutableList()
    if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
      val item = list.removeAt(fromIndex)
      list.add(toIndex, item)
      val updated = current.copy(buttons = list)
      repository.saveUiCustomizationConfig(updated)
      _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    }
  }

  fun moveButtonUp(buttonId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.buttons.toMutableList()
    val index = list.indexOfFirst { it.id == buttonId }
    if (index > 0) {
      val item = list.removeAt(index)
      list.add(index - 1, item)
      val updated = current.copy(buttons = list)
      repository.saveUiCustomizationConfig(updated)
      _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    }
  }

  fun moveButtonDown(buttonId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.buttons.toMutableList()
    val index = list.indexOfFirst { it.id == buttonId }
    if (index in 0 until list.lastIndex) {
      val item = list.removeAt(index)
      list.add(index + 1, item)
      val updated = current.copy(buttons = list)
      repository.saveUiCustomizationConfig(updated)
      _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    }
  }

  fun toggleButtonVisibility(buttonId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.buttons.map {
      if (it.id == buttonId) it.copy(isVisible = !it.isVisible) else it
    }
    val updated = current.copy(buttons = list)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
  }

  fun toggleButtonQuickShortcut(buttonId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.buttons.map {
      if (it.id == buttonId) it.copy(isQuickShortcut = !it.isQuickShortcut) else it
    }
    val updated = current.copy(buttons = list)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
  }

  fun setActionButtonVisibility(buttonId: String, visible: Boolean) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.buttons.map {
      if (it.id == buttonId) it.copy(isVisible = visible) else it
    }
    val updated = current.copy(buttons = list)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    val msg = if (visible) "✅ تم إظهار الزر في الواجهة الرئيسية" else "👁️ تم إخفاء الزر من الواجهة الرئيسية"
    showToast(msg)
  }

  fun setActionButtonQuickShortcut(buttonId: String, isShortcut: Boolean) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.buttons.map {
      if (it.id == buttonId) it.copy(isQuickShortcut = isShortcut) else it
    }
    val updated = current.copy(buttons = list)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    val msg = if (isShortcut) "⭐ تم تثبيت الزر في شريط الاختصارات" else "تم إلغاء تثبيت الزر من شريط الاختصارات"
    showToast(msg)
  }

  fun setPercentageCalculatorVisibility(visible: Boolean) {
    setActionButtonVisibility("PERCENTAGE_CALCULATOR", visible)
  }

  fun setPercentageCalculatorQuickShortcut(isShortcut: Boolean) {
    setActionButtonQuickShortcut("PERCENTAGE_CALCULATOR", isShortcut)
  }

  // --- Form Fields Customization (Drag & Drop Reordering and Visibility) ---
  fun reorderFormFields(fromIndex: Int, toIndex: Int) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.formFields.toMutableList()
    if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
      val item = list.removeAt(fromIndex)
      list.add(toIndex, item)
      val updated = current.copy(formFields = list)
      repository.saveUiCustomizationConfig(updated)
      _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    }
  }

  fun moveFormFieldUp(fieldId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.formFields.toMutableList()
    val index = list.indexOfFirst { it.id == fieldId }
    if (index > 0) {
      val item = list.removeAt(index)
      list.add(index - 1, item)
      val updated = current.copy(formFields = list)
      repository.saveUiCustomizationConfig(updated)
      _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    }
  }

  fun moveFormFieldDown(fieldId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.formFields.toMutableList()
    val index = list.indexOfFirst { it.id == fieldId }
    if (index in 0 until list.lastIndex) {
      val item = list.removeAt(index)
      list.add(index + 1, item)
      val updated = current.copy(formFields = list)
      repository.saveUiCustomizationConfig(updated)
      _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    }
  }

  fun toggleFormFieldVisibility(fieldId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val list = current.formFields.map {
      if (it.id == fieldId) {
        if (it.isRequired) {
          showToast("⚠️ هذا الحقل إلزامي لعمل الفاتورة ولا يمكن إخفاؤه.")
          it
        } else {
          it.copy(isVisible = !it.isVisible)
        }
      } else it
    }
    val updated = current.copy(formFields = list)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
  }

  fun resetFormFieldsToDefault() {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(formFields = UiCustomizationConfig.defaultFormFields())
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("🔄 تم استعادة ترتيب الحقول الافتراضي.")
  }

  fun addCustomButton(button: AppActionButton) {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(buttons = current.buttons + button)
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("✅ تم إضافة الزر الجديد (${button.label}) بنجاح.")
  }

  fun updateButton(button: AppActionButton) {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(buttons = current.buttons.map { if (it.id == button.id) button else it })
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("✅ تم حفظ تعديلات الزر.")
  }

  fun deleteCustomButton(buttonId: String) {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(buttons = current.buttons.filterNot { it.id == buttonId })
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("تم حذف الزر.")
  }

  fun updateShadedFieldColor(hex: String, alpha: Float = 1.0f) {
    val current = _uiState.value.uiCustomizationConfig
    val updated = current.copy(shadedFieldColorHex = hex, shadedFieldAlpha = alpha.coerceIn(0.05f, 1.0f))
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("✅ تم حفظ لون وشفافية الحقول المظللة.")
  }

  fun resetShadedFieldColor() {
    updateShadedFieldColor("#FFF0F3", 1.0f)
  }

  fun resetUiCustomizationToDefault() {
    val defaults = repository.resetUiCustomizationConfig()
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = defaults)
    showToast("🔄 تم استعادة الترتيب والأحجام الافتراضية للأزرار.")
  }

  // ==================== APP THEME & DESIGN METHODS ====================
  fun setAppThemeCustomizerModalVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showAppThemeCustomizerModal = visible)
  }

  fun setAppThemePreset(preset: AppThemePreset) {
    val updated = _uiState.value.uiCustomizationConfig.copy(
      themePresetId = preset.id,
      customPrimaryColorHex = preset.primaryHex,
      customSecondaryColorHex = preset.secondaryHex,
      customBgColorHex = preset.bgHex,
      customCardColorHex = preset.cardHex
    )
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("🎨 تم تطبيق ثيم: ${preset.titleAr}")
  }

  fun setHomeScreenStyle(style: HomeScreenStyle) {
    val updated = _uiState.value.uiCustomizationConfig.copy(
      homeScreenStyleId = style.id
    )
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("📱 تم تغيير شكل الواجهة الرئيسية إلى: ${style.titleAr}")
  }

  fun updateCustomThemeColors(primary: String, secondary: String, bg: String, card: String) {
    val updated = _uiState.value.uiCustomizationConfig.copy(
      themePresetId = AppThemePreset.CUSTOM.id,
      customPrimaryColorHex = primary,
      customSecondaryColorHex = secondary,
      customBgColorHex = bg,
      customCardColorHex = card
    )
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("🎨 تم حفظ وتطبيق ألوان الثيم المخصص بنجاح.")
  }

  fun uploadCustomThemeJson(jsonString: String): Boolean {
    return try {
      val json = JSONObject(jsonString)
      val themeName = json.optString("themeName", "تصميم مخصص")
      val primary = json.optString("primaryHex", "#5E258D")
      val secondary = json.optString("secondaryHex", "#8B5CF6")
      val bg = json.optString("bgHex", "#EEF2F5")
      val card = json.optString("cardHex", "#FFFFFF")
      val homeStyle = json.optString("homeScreenStyleId", HomeScreenStyle.MODERN_CARDS.id)

      val updated = _uiState.value.uiCustomizationConfig.copy(
        themePresetId = AppThemePreset.CUSTOM.id,
        homeScreenStyleId = homeStyle,
        customPrimaryColorHex = primary,
        customSecondaryColorHex = secondary,
        customBgColorHex = bg,
        customCardColorHex = card,
        customThemeJson = jsonString
      )
      repository.saveUiCustomizationConfig(updated)
      _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
      showToast("✅ تم رفع وتطبيق الثيم الجديد بنجاح: $themeName")
      true
    } catch (_: Exception) {
      showToast("❌ ملف التصميم غير صالح: تأكد من تنسيق كود JSON.")
      false
    }
  }

  fun exportCurrentThemeJson(): String {
    val config = _uiState.value.uiCustomizationConfig
    val theme = config.currentTheme()
    val obj = JSONObject().apply {
      put("themeName", if (config.themePresetId == AppThemePreset.CUSTOM.id) "تصميم مخصص" else theme.titleAr)
      put("themePresetId", config.themePresetId)
      put("homeScreenStyleId", config.homeScreenStyleId)
      put("primaryHex", if (config.themePresetId == AppThemePreset.CUSTOM.id) config.customPrimaryColorHex else theme.primaryHex)
      put("secondaryHex", if (config.themePresetId == AppThemePreset.CUSTOM.id) config.customSecondaryColorHex else theme.secondaryHex)
      put("bgHex", if (config.themePresetId == AppThemePreset.CUSTOM.id) config.customBgColorHex else theme.bgHex)
      put("cardHex", if (config.themePresetId == AppThemePreset.CUSTOM.id) config.customCardColorHex else theme.cardHex)
      put("textDarkHex", theme.textDarkHex)
      put("accentHex", theme.accentHex)
      put("exportedAt", ArabicNumberHelper.formatDateTime())
    }
    return obj.toString(2)
  }

  fun setCustomBackgroundImage(base64Image: String) {
    val updated = _uiState.value.uiCustomizationConfig.copy(
      customBackgroundImageBase64 = base64Image
    )
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("🖼️ تم تحميل وتطبيق خلفية التصميم بنجاح على واجهة التطبيق.")
  }

  fun removeCustomBackgroundImage() {
    val updated = _uiState.value.uiCustomizationConfig.copy(
      customBackgroundImageBase64 = ""
    )
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("🗑️ تم حذف خلفية الصورة واستعادة مظهر الألوان المعتاد.")
  }

  fun updateBgImageAlpha(alpha: Float) {
    val updated = _uiState.value.uiCustomizationConfig.copy(
      bgImageAlpha = alpha.coerceIn(0.05f, 1.0f)
    )
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
  }

  fun resetAppThemeToDefault() {
    val updated = _uiState.value.uiCustomizationConfig.copy(
      themePresetId = AppThemePreset.ROYAL_PURPLE.id,
      homeScreenStyleId = HomeScreenStyle.MODERN_CARDS.id,
      customPrimaryColorHex = "#5E258D",
      customSecondaryColorHex = "#8B5CF6",
      customBgColorHex = "#EEF2F5",
      customCardColorHex = "#FFFFFF",
      customThemeJson = "",
      customBackgroundImageBase64 = "",
      bgImageAlpha = 0.85f
    )
    repository.saveUiCustomizationConfig(updated)
    _uiState.value = _uiState.value.copy(uiCustomizationConfig = updated)
    showToast("🔄 تم استعادة الثيم والشكل الافتراضي للتطبيق بنجاح.")
  }

  // Report Customization Methods
  fun setReportCustomizerModalVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showReportCustomizerModal = visible)
  }

  fun updateReportCustomizationConfig(config: ReportCustomizationConfig) {
    repository.saveReportCustomizationConfig(config)
    _uiState.value = _uiState.value.copy(reportCustomizationConfig = config)
  }

  fun resetReportCustomizationToDefault() {
    val defaults = repository.resetReportCustomizationConfig()
    _uiState.value = _uiState.value.copy(reportCustomizationConfig = defaults)
    showToast("🔄 تم استعادة جميع إعدادات التقارير الافتراضية بنجاح.")
  }

  fun updateReportFontScale(scale: Float) {
    val cleanScale = String.format(Locale.US, "%.2f", scale).toFloat()
    val updated = _uiState.value.reportCustomizationConfig.copy(
      fontScalePercent = Math.round(cleanScale * 100).toInt().coerceIn(70, 180)
    )
    updateReportCustomizationConfig(updated)
  }

  fun increaseReportFontScale() {
    val current = _uiState.value.reportCustomizationConfig.fontScale
    val newScale = (current + 0.1f).coerceAtMost(1.8f)
    updateReportFontScale(newScale)
    showToast("🔍 حجم خط التقارير: ${(newScale * 100).toInt()}%")
  }

  fun decreaseReportFontScale() {
    val current = _uiState.value.reportCustomizationConfig.fontScale
    val newScale = (current - 0.1f).coerceAtLeast(0.6f)
    updateReportFontScale(newScale)
    showToast("🔍 حجم خط التقارير: ${(newScale * 100).toInt()}%")
  }

  fun updateReportFontFamily(fontFamily: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(fontFamily = fontFamily)
    updateReportCustomizationConfig(updated)
    showToast("✅ تم تغيير نوع الخط إلى: $fontFamily")
  }

  fun updateReportPrimaryColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(primaryTextColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportHeaderColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(headerColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportTableBorderColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(tableBorderColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportCustomerNameColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(customerNameColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportCustomerAccountColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(customerAccountColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportCurrencySymbolColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(currencySymbolColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportInvoiceAmountColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(invoiceAmountColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportUnitPriceColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(unitPriceColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportTotalValueColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(totalValueColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportQuantityColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(quantityColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportItemNumberColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(itemNumberColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateReportGrandTotalAmountColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(grandTotalAmountColorHex = colorHex)
    updateReportCustomizationConfig(updated)
  }

  fun updateAllAmountsColor(colorHex: String) {
    val updated = _uiState.value.reportCustomizationConfig.copy(
      invoiceAmountColorHex = colorHex,
      unitPriceColorHex = colorHex,
      totalValueColorHex = colorHex,
      grandTotalAmountColorHex = colorHex
    )
    updateReportCustomizationConfig(updated)
  }

  fun toggleReportElement(field: String, enabled: Boolean) {
    val current = _uiState.value.reportCustomizationConfig
    val updated = when (field) {
      "storeInfo" -> current.copy(showStoreInfo = enabled)
      "logo" -> current.copy(showLogo = enabled)
      "dateTime" -> current.copy(showDateTime = enabled)
      "branch" -> current.copy(showBranch = enabled)
      "accountNumber" -> current.copy(showCustomerAccountNumber = enabled)
      "amountInWords" -> current.copy(showAmountInWords = enabled)
      "subscriptionBox" -> current.copy(showCardSubscriptionBox = enabled)
      "watermark" -> current.copy(showWatermark = enabled)
      "termsAndNotes" -> current.copy(showTermsAndNotes = enabled)
      "signatures" -> current.copy(showSignatures = enabled)
      "stampSeal" -> current.copy(showStampSeal = enabled)
      else -> current
    }
    updateReportCustomizationConfig(updated)
  }

  fun openPaymentVoucherShortcut() {
    _uiState.value = _uiState.value.copy(
      showCustomersModal = true,
      customersModalInitialTab = 1
    )
  }

  fun openCurrencyConverter() {
    _uiState.value = _uiState.value.copy(currentScreen = AppScreen.CURRENCY_CONVERTER)
  }

  fun openPercentageCalculator() {
    _uiState.value = _uiState.value.copy(currentScreen = AppScreen.PERCENTAGE_CALCULATOR)
  }

  fun openFullCalculator() {
    _uiState.value = _uiState.value.copy(currentScreen = AppScreen.FULL_CALCULATOR)
  }

  fun executeActionButton(button: AppActionButton, context: Context) {
    val action = if (button.actionType.isNotBlank()) button.actionType else button.id
    when (action) {
      "PERCENTAGE_CALCULATOR" -> openPercentageCalculator()
      "FULL_CALCULATOR" -> openFullCalculator()
      "CURRENCY_CONVERTER" -> openCurrencyConverter()
      "NEW_INVOICE" -> addNewInvoice()
      "OPEN_REPORT" -> openLatestSavedOrCurrentReport()
      "CUSTOMER_DISPLAY" -> openCustomerDisplayMode()
      "PRINT_INVOICE" -> {
        val inv = getLatestSavedInvoiceOrCurrent()
        PrintHelper.printInvoice(context, inv, _uiState.value.storeConfig, _uiState.value.reportCustomizationConfig)
      }
      "EXPORT_PDF" -> {
        val inv = getLatestSavedInvoiceOrCurrent()
        exportInvoiceToPdf(context, inv)
      }
      "SAVED_INVOICES" -> setSavedInvoicesModalVisible(true)
      "CUSTOMERS" -> openCustomersModal(-1)
      "RECEIPT_VOUCHER" -> openReceiptVoucherShortcut()
      "PAYMENT_VOUCHER" -> openPaymentVoucherShortcut()
      "ALL_CUSTOMERS" -> openAllCustomersShortcut()
      "CUSTOMERS_WITH_BALANCE" -> openCustomersWithBalanceShortcut()
      "REPORT_CONTROL" -> setReportCustomizerModalVisible(true)
      "ROOM_BACKUP" -> setRoomBackupModalVisible(true)
      "SETTINGS" -> setSettingsModalVisible(true)
      "EXIT_APP" -> {
        _uiState.value = _uiState.value.copy(showExitConfirmDialog = false)
        var act: android.app.Activity? = context as? android.app.Activity
        var ctx = context
        while (act == null && ctx is android.content.ContextWrapper) {
          ctx = ctx.baseContext
          act = ctx as? android.app.Activity
        }
        act?.finishAffinity() ?: act?.finish()
      }
      "CUSTOM_PACKAGE" -> {
        val priceVal = button.customParam.toDoubleOrNull() ?: 0.0
        val descText = button.customDesc.ifEmpty { button.label }
        _uiState.value = _uiState.value.copy(
          price = if (priceVal > 0) priceVal.toString() else _uiState.value.price,
          desc = descText,
          isFormVisible = true
        )
        showToast("⚡ تم تطبيق باقة (${button.label}) في حقول الفاتورة.")
      }
      "CUSTOM_STATEMENT" -> {
        val acc = button.customParam.trim()
        val customer = _uiState.value.customers.find { it.accountNumber == acc }
        if (customer != null) {
          openCustomerStatement(customer)
        } else {
          showToast("❌ لم يتم العثور على عميل برقم الحساب ($acc)")
        }
      }
      "CUSTOM_PHONE" -> {
        try {
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${button.customParam}"))
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          context.startActivity(intent)
        } catch (e: Exception) {
          showToast("تعذر فتح الاتصال: ${e.message}")
        }
      }
      "CUSTOM_URL" -> {
        try {
          var url = button.customParam.trim()
          if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
          }
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          context.startActivity(intent)
        } catch (e: Exception) {
          showToast("تعذر فتح الرابط: ${e.message}")
        }
      }
      else -> {
        showToast("إجراء: ${button.label}")
      }
    }
  }

  // Calculation rules from user JavaScript code:
  // getMainTotal() = unit price (ignoring quantity for main item as explicitly requested)
  fun getMainTotal(): Double {
    return _uiState.value.price.toDoubleOrNull() ?: 0.0
  }

  fun getExtraItemsTotal(targetCurrency: String = _uiState.value.currency): Double {
    var sum = 0.0
    for (item in _uiState.value.extraItems) {
      val itemTotal = item.price * item.qty
      sum += repository.convertCurrency(itemTotal, item.currency, targetCurrency)
    }
    return sum
  }

  fun getGrandTotal(): Double {
    return getMainTotal() + getExtraItemsTotal()
  }

  fun saveCurrentInvoice(): Boolean {
    val state = _uiState.value
    val invNum = ArabicNumberHelper.toEngDigits(state.invNum).trim()
    val price = state.price.toDoubleOrNull() ?: 0.0
    val qty = state.qty.toDoubleOrNull() ?: 3.0

    if (invNum.isEmpty() || (invNum.toIntOrNull() ?: 0) <= 0) {
      showToast("⚠️ رقم الفاتورة يجب أن يكون رقماً صحيحاً أكبر من صفر.")
      return false
    }
    if (price <= 0.0 && state.extraItems.isEmpty()) {
      showToast("⚠️ يرجى إدخال سعر الاشتراك أو إضافة تفاصيل إضافية بالمبلغ.")
      return false
    }
    if (qty <= 0.0) {
      showToast("⚠️ العدد يجب أن يكون أكبر من صفر.")
      return false
    }

    val grandTotal = getGrandTotal()
    if (grandTotal <= 0.0) {
      showToast("⚠️ المبلغ الإجمالي يجب أن يكون أكبر من صفر.")
      return false
    }

    var acc = ArabicNumberHelper.toEngDigits(state.customerAccount).trim()
    var customer = if (acc.isNotEmpty()) repository.findCustomerByAccount(acc) else null

    // If customer not found by account, check if an existing customer matches the entered name
    if (customer == null && state.customerName.isNotBlank()) {
      customer = repository.customers.find { it.name.trim().equals(state.customerName.trim(), ignoreCase = true) }
      if (customer != null) {
        acc = customer.accountNumber
      }
    }

    if (state.invType == "أجل") {
      if (customer == null && state.customerName.isBlank()) {
        showToast("⚠️ يرجى إدخال اسم المشترك أو رقم حسابه لإنشاء الحساب وحفظ الفاتورة الآجل.")
        return false
      }
      if (customer == null) {
        // Customer is new: use entered account number or generate next available account number
        val targetAcc = if (acc.isNotEmpty()) {
          val isConflict = repository.customers.any { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == acc }
          if (isConflict) {
            repository.getNextAccountNumber()
          } else {
            acc
          }
        } else {
          repository.getNextAccountNumber()
        }

        customer = Customer(
          id = System.currentTimeMillis(),
          accountNumber = targetAcc,
          name = state.customerName.trim(),
          phone = "",
          address = "",
          balance = 0.0,
          transactions = emptyList()
        )
        val updatedCustomers = repository.customers.toMutableList().apply { add(customer) }
        repository.saveCustomers(updatedCustomers)
        acc = targetAcc
      }
    }

    val dateToUse = if (state.invoiceDate.isNotBlank()) state.invoiceDate else ArabicNumberHelper.formatDateTime()

    val cleanInvNum = ArabicNumberHelper.toEngDigits(invNum).trim()
    var finalInvNum = invNum

    // Verify duplicate invoice number before saving
    if (state.editingInvoiceId != null) {
      val isDuplicate = repository.savedInvoices.any {
        it.id != state.editingInvoiceId && ArabicNumberHelper.toEngDigits(it.invNum).trim() == cleanInvNum
      }
      if (isDuplicate) {
        showToast("❌ رقم الفاتورة ($invNum) موجود بالفعل في فاتورة أخرى! لا يمكن تكرار رقم الفاتورة.")
        return false
      }
    } else {
      val isDuplicate = repository.savedInvoices.any {
        ArabicNumberHelper.toEngDigits(it.invNum).trim() == cleanInvNum
      }
      if (isDuplicate) {
        finalInvNum = repository.getNextInvoiceNumber().ifEmpty { ((invNum.toIntOrNull() ?: 1) + 1).toString() }
      }
    }

    val mainDesc = state.desc.trim().ifEmpty { "تجديد باقه تميز" }

    if (state.editingInvoiceId != null) {
      val existingInv = repository.savedInvoices.find { it.id == state.editingInvoiceId }
      if (existingInv != null) {
        // Modifying existing invoice - preserve existing endDate strictly
        val updatedInvoice = InvoiceData(
          id = existingInv.id,
          invNum = finalInvNum,
          invType = state.invType,
          customerAccount = acc,
          customerName = state.customerName.ifEmpty { customer?.name ?: "" },
          cardId = state.cardId,
          price = price,
          type = state.subscriptionType,
          qty = qty,
          desc = mainDesc,
          endDate = if (existingInv.endDate.isNotBlank()) existingInv.endDate else state.endDate,
          currency = state.currency,
          extraItems = state.extraItems,
          grandTotal = grandTotal,
          createdAt = dateToUse
        )
        repository.updateSavedInvoice(updatedInvoice, oldInvoice = existingInv)
        showToast("✅ تم حفظ تعديلات الفاتورة رقم $finalInvNum وتاريخها ($dateToUse) بنجاح دون تكرار.")

        val nextInv = repository.getNextInvoiceNumber().ifEmpty { ((finalInvNum.toIntOrNull() ?: 1) + 1).toString() }
        _uiState.value = _uiState.value.copy(
          savedInvoices = repository.savedInvoices,
          customers = repository.customers,
          isFormVisible = false,
          editingInvoiceId = null,
          invNum = nextInv,
          customerName = "",
          cardId = "",
          price = "",
          desc = "تجديد باقه تميز",
          customerAccount = "",
          extraItems = emptyList(),
          invoiceDate = ArabicNumberHelper.formatDateTime(),
          previewInvoice = updatedInvoice
        )
        calculateExpiryDate()
        return true
      }
    }

    // New invoice
    val invoice = InvoiceData(
      id = System.currentTimeMillis(),
      invNum = finalInvNum,
      invType = state.invType,
      customerAccount = acc,
      customerName = state.customerName.ifEmpty { customer?.name ?: "" },
      cardId = state.cardId,
      price = price,
      type = state.subscriptionType,
      qty = qty,
      desc = mainDesc,
      endDate = state.endDate,
      currency = state.currency,
      extraItems = state.extraItems,
      grandTotal = grandTotal,
      createdAt = dateToUse
    )

    val updatedInvoices = repository.savedInvoices.toMutableList().apply { add(invoice) }
    repository.saveInvoices(updatedInvoices)

    if (state.invType == "أجل") {
      repository.updateCustomerBalance(
        account = acc,
        amount = grandTotal,
        type = "فاتورة",
        note = mainDesc,
        voucherNum = finalInvNum,
        currency = state.currency,
        customDate = dateToUse
      )
      showToast("✅ تم حفظ الفاتورة وإضافة $grandTotal ${state.currency} لحساب العميل $acc")
    } else {
      showToast("✅ تم حفظ الفاتورة النقدية رقم $finalInvNum بإجمالي ${ArabicNumberHelper.formatAmount(grandTotal)} ${state.currency}")
    }

    // Reset inputs and hide invoice form after saving
    val nextInv = repository.getNextInvoiceNumber().ifEmpty { ((finalInvNum.toIntOrNull() ?: 1) + 1).toString() }
    _uiState.value = _uiState.value.copy(
      savedInvoices = updatedInvoices,
      customers = repository.customers,
      isFormVisible = false,
      editingInvoiceId = null,
      invNum = nextInv,
      customerName = "",
      cardId = "",
      price = "",
      currency = "YER",
      desc = "تجديد باقة تميز",
      customerAccount = "",
      qty = "3",
      extraItems = emptyList(),
      invoiceDate = ArabicNumberHelper.formatDateTime(),
      previewInvoice = invoice
    )
    calculateExpiryDate()
    return true
  }

  fun addNewInvoice() {
    val nextInv = repository.getNextInvoiceNumber().ifEmpty { "1" }
    _uiState.value = _uiState.value.copy(
      isFormVisible = true,
      editingInvoiceId = null,
      invNum = nextInv,
      customerName = "",
      cardId = "",
      price = "",
      currency = "YER",
      desc = "تجديد باقة تميز",
      customerAccount = "",
      qty = "3",
      extraItems = emptyList(),
      invoiceDate = ArabicNumberHelper.formatDateTime()
    )
    calculateExpiryDate()
  }

  fun closeInvoiceForm() {
    _uiState.value = _uiState.value.copy(
      isFormVisible = false,
      editingInvoiceId = null
    )
  }

  fun openLatestSavedOrCurrentReport() {
    val lastSaved = repository.savedInvoices.lastOrNull()
    if (lastSaved != null) {
      _uiState.value = _uiState.value.copy(
        previewInvoice = lastSaved,
        currentScreen = AppScreen.INVOICE_REPORT
      )
    } else {
      openCurrentReport()
    }
  }

  fun getLatestSavedInvoiceOrCurrent(): InvoiceData {
    val lastSaved = repository.savedInvoices.lastOrNull()
    if (lastSaved != null) return lastSaved
    val state = _uiState.value
    val price = state.price.toDoubleOrNull() ?: 0.0
    val qty = state.qty.toDoubleOrNull() ?: 1.0
    val linkedCust = if (state.customerAccount.isNotBlank()) repository.findCustomerByAccount(state.customerAccount) else null
    val effectiveCustName = state.customerName.trim().ifEmpty { linkedCust?.name ?: "" }
    return InvoiceData(
      id = 0L,
      invNum = state.invNum.trim().ifEmpty { "1" },
      invType = state.invType,
      customerAccount = state.customerAccount.trim(),
      customerName = effectiveCustName,
      cardId = state.cardId.trim(),
      price = price,
      type = state.subscriptionType,
      qty = qty,
      desc = state.desc.trim().ifEmpty { "تجديد باقة تميز" },
      endDate = state.endDate.trim(),
      currency = state.currency,
      extraItems = state.extraItems,
      grandTotal = getGrandTotal(),
      createdAt = if (state.invoiceDate.isNotBlank()) state.invoiceDate else ArabicNumberHelper.formatDateTime()
    )
  }

  fun openCurrentReport() {
    val state = _uiState.value
    val price = state.price.toDoubleOrNull() ?: 0.0
    val qty = state.qty.toDoubleOrNull() ?: 1.0
    val linkedCust = if (state.customerAccount.isNotBlank()) repository.findCustomerByAccount(state.customerAccount) else null
    val effectiveCustName = state.customerName.trim().ifEmpty { linkedCust?.name ?: "" }
    val currentInvoice = InvoiceData(
      id = 0L,
      invNum = state.invNum.trim().ifEmpty { "1" },
      invType = state.invType,
      customerAccount = state.customerAccount.trim(),
      customerName = effectiveCustName,
      cardId = state.cardId.trim(),
      price = price,
      type = state.subscriptionType,
      qty = qty,
      desc = state.desc.trim().ifEmpty { "تجديد باقة تميز" },
      endDate = state.endDate.trim(),
      currency = state.currency,
      extraItems = state.extraItems,
      grandTotal = getGrandTotal(),
      createdAt = if (state.invoiceDate.isNotBlank()) state.invoiceDate else ArabicNumberHelper.formatDateTime()
    )
    _uiState.value = _uiState.value.copy(
      previewInvoice = currentInvoice,
      currentScreen = AppScreen.INVOICE_REPORT
    )
  }

  fun openInvoiceReport(invoice: InvoiceData) {
    _uiState.value = _uiState.value.copy(
      previewInvoice = invoice,
      currentScreen = AppScreen.INVOICE_REPORT
    )
  }

  fun openCustomerDisplayMode(invoice: InvoiceData? = null) {
    val target = invoice ?: getLatestSavedInvoiceOrCurrent()
    _uiState.value = _uiState.value.copy(
      previewInvoice = target,
      currentScreen = AppScreen.CUSTOMER_INVOICE_DISPLAY
    )
  }

  fun requestStatementDateRange(customer: Customer) {
    val today = Date()
    val todayStr = ArabicNumberHelper.formatDateOnly(today)
    val cal = Calendar.getInstance()
    cal.time = today
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonthStr = ArabicNumberHelper.formatDateOnly(cal.time)

    _uiState.value = _uiState.value.copy(
      pendingStatementCustomer = customer,
      statementStartDate = startOfMonthStr,
      statementEndDate = todayStr,
      showDateRangeDialog = true
    )
  }

  fun dismissDateRangeDialog() {
    _uiState.value = _uiState.value.copy(
      showDateRangeDialog = false,
      pendingStatementCustomer = null
    )
  }

  fun updateStatementStartDate(date: String) {
    _uiState.value = _uiState.value.copy(statementStartDate = date)
  }

  fun updateStatementEndDate(date: String) {
    _uiState.value = _uiState.value.copy(statementEndDate = date)
  }

  fun setStatementQuickPeriod(type: String) {
    val cal = Calendar.getInstance()
    val today = Date()
    val endStr = ArabicNumberHelper.formatDateOnly(today)
    var startStr = endStr

    when (type) {
      "this_month" -> {
        cal.time = today
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startStr = ArabicNumberHelper.formatDateOnly(cal.time)
      }
      "last_month" -> {
        cal.time = today
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startStr = ArabicNumberHelper.formatDateOnly(cal.time)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, maxDay)
        val lastMonthEnd = ArabicNumberHelper.formatDateOnly(cal.time)
        _uiState.value = _uiState.value.copy(
          statementStartDate = startStr,
          statementEndDate = lastMonthEnd
        )
        return
      }
      "3_months" -> {
        cal.time = today
        cal.add(Calendar.MONTH, -3)
        startStr = ArabicNumberHelper.formatDateOnly(cal.time)
      }
      "6_months" -> {
        cal.time = today
        cal.add(Calendar.MONTH, -6)
        startStr = ArabicNumberHelper.formatDateOnly(cal.time)
      }
      "all" -> {
        startStr = "01/01/2020"
      }
      "custom" -> {
        _uiState.value = _uiState.value.copy(
          statementStartDate = "",
          statementEndDate = ""
        )
        return
      }
    }
    _uiState.value = _uiState.value.copy(
      statementStartDate = startStr,
      statementEndDate = endStr
    )
  }

  fun selectCustomerForDateRange(customer: Customer) {
    val today = Date()
    val todayStr = ArabicNumberHelper.formatDateOnly(today)
    val cal = Calendar.getInstance()
    cal.time = today
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonthStr = ArabicNumberHelper.formatDateOnly(cal.time)

    _uiState.value = _uiState.value.copy(
      pendingStatementCustomer = customer,
      statementStartDate = startOfMonthStr,
      statementEndDate = todayStr
    )
  }

  fun applyDateRangeAndOpenStatement(): Boolean {
    val customer = _uiState.value.pendingStatementCustomer ?: _uiState.value.statementCustomer
    if (customer == null) {
      showToast("❌ لم يتم تحديد عميل.")
      return false
    }

    val startRaw = _uiState.value.statementStartDate.trim()
    val endRaw = _uiState.value.statementEndDate.trim()

    // 7. إذا لم يحدد المستخدم التاريخين
    if (startRaw.isBlank() || endRaw.isBlank()) {
      showToast("الرجاء تحديد تاريخ البداية والنهاية")
      return false
    }

    val startDate = ArabicNumberHelper.parseDate(startRaw)
    val endDate = ArabicNumberHelper.parseDate(endRaw)

    if (startDate == null || endDate == null) {
      showToast("الرجاء إدخال تاريخ صحيح بصيغة: يوم/شهر/سنة (مثال: 01/05/2026)")
      return false
    }

    // Compare date objects (calendar normalized to start of day)
    val startDayCal = Calendar.getInstance().apply {
      time = startDate
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val endDayCal = Calendar.getInstance().apply {
      time = endDate
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    // 8. إذا كان تاريخ البداية بعد تاريخ النهاية
    if (startDayCal.after(endDayCal)) {
      showToast("تاريخ البداية يجب أن يكون قبل تاريخ النهاية")
      return false
    }

    _uiState.value = _uiState.value.copy(
      statementCustomer = repository.findCustomerByAccount(customer.accountNumber) ?: customer,
      pendingStatementCustomer = null,
      showDateRangeDialog = false,
      showCustomersModal = false,
      currentScreen = AppScreen.CUSTOMER_STATEMENT
    )
    return true
  }

  fun openCustomerStatement(customer: Customer, startDate: String = "", endDate: String = "") {
    val freshCustomer = repository.findCustomerByAccount(customer.accountNumber) ?: customer
    _uiState.value = _uiState.value.copy(
      statementCustomer = freshCustomer,
      statementStartDate = startDate,
      statementEndDate = endDate,
      currentScreen = AppScreen.CUSTOMER_STATEMENT
    )
  }

  fun triggerScreenshot(title: String) {
    _uiState.value = _uiState.value.copy(screenshotRequest = title)
  }

  fun clearScreenshotRequest() {
    _uiState.value = _uiState.value.copy(screenshotRequest = null)
  }

  fun openStatementByAccount(account: String): Boolean {
    val clean = ArabicNumberHelper.toEngDigits(account).trim()
    val customer = repository.findCustomerByAccount(clean)
    if (customer == null) {
      showToast("❌ لا يوجد عميل بهذا الرقم.")
      return false
    }
    requestStatementDateRange(customer)
    return true
  }

  fun getNextCustomerAccount(): String {
    return repository.getNextAccountNumber()
  }

  fun addCustomer(name: String, phone: String, address: String, initialBalance: Double, customAccount: String = "", currency: String = "YR"): Boolean {
    if (name.isBlank()) {
      showToast("❌ يرجى إدخال اسم العميل.")
      return false
    }
    val cleanCustom = ArabicNumberHelper.toEngDigits(customAccount).trim()
    val acc = if (cleanCustom.isNotEmpty()) cleanCustom else repository.getNextAccountNumber()

    // Verify duplicate customer account number before saving
    if (repository.customers.any { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == acc }) {
      showToast("❌ رقم حساب العميل ($acc) موجود بالفعل! لا يمكن تكرار رقم الحساب.")
      return false
    }

    val initialTx = if (initialBalance != 0.0) {
      listOf(
        com.example.data.TransactionRecord(
          date = ArabicNumberHelper.formatDateTime(),
          type = "افتتاح",
          amount = initialBalance,
          currency = currency.ifBlank { "YR" },
          note = "افتتاحي",
          balanceAfter = initialBalance
        )
      )
    } else emptyList()

    val newC = Customer(
      id = System.currentTimeMillis(),
      accountNumber = acc,
      name = name.trim(),
      phone = ArabicNumberHelper.toEngDigits(phone).trim(),
      address = address.trim(),
      balance = initialBalance,
      transactions = initialTx
    )
    val updated = repository.customers.toMutableList().apply { add(newC) }
    repository.saveCustomers(updated)
    _uiState.value = _uiState.value.copy(customers = updated)
    showToast("✅ تم إضافة العميل $name برقم حساب $acc")
    return true
  }

  fun editCustomer(
    account: String,
    newName: String,
    newPhone: String,
    newAddress: String,
    newInitialBalance: Double? = null
  ) {
    val clean = ArabicNumberHelper.toEngDigits(account).trim()
    val idx = repository.customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == clean }
    if (idx != -1) {
      val c = repository.customers[idx]
      val updatedTransactions = c.transactions.toMutableList()

      if (newInitialBalance != null) {
        val openIdx = updatedTransactions.indexOfFirst { it.type == "افتتاح" }
        if (openIdx != -1) {
          if (newInitialBalance == 0.0 && updatedTransactions.size > 1) {
            updatedTransactions.removeAt(openIdx)
          } else {
            updatedTransactions[openIdx] = updatedTransactions[openIdx].copy(
              amount = newInitialBalance,
              note = "افتتاحي"
            )
          }
        } else if (newInitialBalance != 0.0) {
          val newTx = com.example.data.TransactionRecord(
            date = ArabicNumberHelper.formatDateTime(),
            type = "افتتاح",
            amount = newInitialBalance,
            currency = "USD",
            note = "افتتاحي",
            balanceAfter = newInitialBalance
          )
          updatedTransactions.add(0, newTx)
        }
      }

      val updatedCustomer = c.copy(
        name = newName.ifEmpty { c.name },
        phone = ArabicNumberHelper.toEngDigits(newPhone),
        address = newAddress,
        transactions = updatedTransactions
      )

      val recalculatedCustomer = repository.recalculateCustomerBalance(updatedCustomer)
      repository.customers[idx] = recalculatedCustomer
      repository.saveCustomers()
      _uiState.value = _uiState.value.copy(customers = repository.customers)
      showToast("✅ تم تعديل بيانات العميل بنجاح.")
    }
  }

  fun deleteCustomer(account: String) {
    val removed = repository.deleteCustomer(account)
    if (removed) {
      _uiState.value = _uiState.value.copy(customers = repository.customers)
      showToast("تم حذف العميل بنجاح.")
    }
  }

  fun addReceiptVoucher(
    account: String,
    amount: Double,
    currency: String,
    note: String,
    date: String = "",
    voucherNum: String = ""
  ): Boolean {
    val cleanAcc = ArabicNumberHelper.toEngDigits(account).trim()
    if (cleanAcc.isBlank() || amount <= 0.0) {
      showToast("❌ يرجى إدخال رقم الحساب والمبلغ بشكٍل صحيح.")
      return false
    }

    val customerExists = repository.customers.any {
      ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanAcc
    }
    if (!customerExists) {
      showToast("⚠️ هذا الحساب ($cleanAcc) غير موجود في النظام! يرجى التأكد من رقم الحساب.")
      return false
    }

    val cleanVNum = ArabicNumberHelper.toEngDigits(voucherNum).trim()
    val vNum = if (cleanVNum.isNotEmpty()) {
      cleanVNum
    } else {
      repository.getUniqueNextReceiptVoucherNumber()
    }

    // Verify duplicate receipt voucher number before saving
    if (repository.voucherExists(vNum, "قبض")) {
      showToast("❌ رقم سند القبض ($vNum) موجود بالفعل! لا يمكن تكرار رقم السند.")
      return false
    }

    cleanVNum.toIntOrNull()?.let { num ->
      if (num >= repository.nextReceiptVoucherNum) {
        repository.updateNextReceiptVoucherNum(num + 1)
      }
    }

    val updatedCustomer = repository.updateCustomerBalance(
      account = cleanAcc,
      amount = amount,
      type = "قبض",
      note = note.trim(),
      voucherNum = vNum,
      currency = currency,
      customDate = if (date.isNotBlank()) date else null
    )
    if (updatedCustomer == null) {
      showToast("❌ العميل غير موجود.")
      return false
    }
    _uiState.value = _uiState.value.copy(
      customers = repository.customers,
      nextReceiptVoucherNum = repository.nextReceiptVoucherNum,
      statementCustomer = if (_uiState.value.statementCustomer?.accountNumber == cleanAcc) updatedCustomer else _uiState.value.statementCustomer
    )
    showToast("✅ تم تسديد مبلغ ${ArabicNumberHelper.formatAmount(amount)} $currency بالسند رقم ($vNum)")
    return true
  }

  fun addPaymentVoucher(
    account: String,
    amount: Double,
    currency: String,
    note: String,
    date: String = "",
    voucherNum: String = ""
  ): Boolean {
    val cleanAcc = ArabicNumberHelper.toEngDigits(account).trim()
    if (cleanAcc.isBlank() || amount <= 0.0) {
      showToast("❌ يرجى إدخال رقم الحساب والمبلغ بشكٍل صحيح.")
      return false
    }

    val customerExists = repository.customers.any {
      ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanAcc
    }
    if (!customerExists) {
      showToast("⚠️ هذا الحساب ($cleanAcc) غير موجود في النظام! يرجى التأكد من رقم الحساب.")
      return false
    }

    val cleanVNum = ArabicNumberHelper.toEngDigits(voucherNum).trim()
    val vNum = if (cleanVNum.isNotEmpty()) {
      cleanVNum
    } else {
      repository.getUniqueNextPaymentVoucherNumber()
    }

    // Verify duplicate payment voucher number before saving
    if (repository.voucherExists(vNum, "صرف")) {
      showToast("❌ رقم سند الصرف ($vNum) موجود بالفعل! لا يمكن تكرار رقم السند.")
      return false
    }

    cleanVNum.toIntOrNull()?.let { num ->
      if (num >= repository.nextPaymentVoucherNum) {
        repository.updateNextPaymentVoucherNum(num + 1)
      }
    }

    val updatedCustomer = repository.updateCustomerBalance(
      account = cleanAcc,
      amount = amount,
      type = "صرف",
      note = note.trim(),
      voucherNum = vNum,
      currency = currency,
      customDate = if (date.isNotBlank()) date else null
    )
    if (updatedCustomer == null) {
      showToast("❌ العميل غير موجود.")
      return false
    }
    _uiState.value = _uiState.value.copy(
      customers = repository.customers,
      nextPaymentVoucherNum = repository.nextPaymentVoucherNum,
      statementCustomer = if (_uiState.value.statementCustomer?.accountNumber == cleanAcc) updatedCustomer else _uiState.value.statementCustomer
    )
    showToast("✅ تم صرف مبلغ ${ArabicNumberHelper.formatAmount(amount)} $currency بالسند رقم ($vNum)")
    return true
  }

  fun editVoucher(
    oldVoucherNum: String,
    type: String,
    targetAccount: String,
    newAmount: Double,
    newCurrency: String,
    newNote: String,
    newDate: String,
    newVoucherNum: String = oldVoucherNum,
    origAccount: String? = null
  ): Boolean {
    val cleanOld = ArabicNumberHelper.toEngDigits(oldVoucherNum).trim()
    val cleanNew = ArabicNumberHelper.toEngDigits(newVoucherNum).trim().ifEmpty { cleanOld }

    // Check if new voucher number is already used in another voucher
    if (cleanNew != cleanOld && repository.voucherExists(cleanNew, type, excludeVoucherNum = cleanOld, excludeAccount = origAccount)) {
      showToast("❌ رقم سند ال$type ($cleanNew) موجود بالفعل في سند آخر! لا يمكن تكرار رقم السند.")
      return false
    }

    val success = repository.editVoucher(
      oldVoucherNum = oldVoucherNum,
      type = type,
      targetAccount = targetAccount,
      newAmount = newAmount,
      newCurrency = newCurrency,
      newNote = newNote,
      newDate = newDate,
      newVoucherNum = cleanNew,
      origAccount = origAccount
    )
    if (success) {
      _uiState.value = _uiState.value.copy(
        customers = repository.customers,
        nextReceiptVoucherNum = repository.nextReceiptVoucherNum,
        nextPaymentVoucherNum = repository.nextPaymentVoucherNum
      )
      showToast("✅ تم تعديل سند ال$type رقم ($cleanNew) وتحديث الحساب بنجاح دون تكرار.")
    } else {
      showToast("❌ فشل تعديل السند. تأكد من صحة رقم السند ورقم الحساب.")
    }
    return success
  }

  fun deleteVoucher(voucherNum: String, type: String, account: String? = null): Boolean {
    val success = repository.deleteVoucher(voucherNum, type, account)
    if (success) {
      _uiState.value = _uiState.value.copy(customers = repository.customers)
      showToast("✅ تم حذف سند ال$type رقم ($voucherNum) وتعديل رصيد الحساب بنجاح.")
    } else {
      showToast("❌ تعذر حذف السند.")
    }
    return success
  }

  fun getAllVouchers(type: String): List<com.example.data.VoucherItem> {
    return repository.getAllVouchers(type)
  }

  fun getCustomerNameForAccount(account: String): String {
    val clean = ArabicNumberHelper.toEngDigits(account).trim()
    return repository.findCustomerByAccount(clean)?.name ?: ""
  }

  fun updateExchangeRates(rates: ExchangeRates) {
    repository.saveExchangeRates(rates)
    val updatedStatementCustomer = _uiState.value.statementCustomer?.let { cur ->
      repository.findCustomerByAccount(cur.accountNumber) ?: cur
    }
    _uiState.value = _uiState.value.copy(
      exchangeRates = rates,
      customers = repository.customers,
      statementCustomer = updatedStatementCustomer
    )
    showToast("✅ تم حفظ أسعار العملات بنجاح.")
  }

  fun updateStoreConfig(config: StoreConfig) {
    repository.saveStoreConfig(config)
    _uiState.value = _uiState.value.copy(storeConfig = config)
    showToast("✅ تم حفظ إعدادات المحل والتقرير بنجاح.")
  }

  fun updateStoreLogo(base64: String) {
    val updated = _uiState.value.storeConfig.copy(logoBase64 = base64)
    repository.saveStoreConfig(updated)
    _uiState.value = _uiState.value.copy(storeConfig = updated)
    showToast("✅ تم تحديث وحفظ شعار المحل بنجاح.")
  }

  fun resetStoreLogo() {
    val updated = _uiState.value.storeConfig.copy(logoBase64 = "")
    repository.saveStoreConfig(updated)
    _uiState.value = _uiState.value.copy(storeConfig = updated)
    showToast("✅ تم استعادة الشعار الافتراضي.")
  }

  fun editSavedInvoice(invoice: InvoiceData) {
    _uiState.value = _uiState.value.copy(
      isFormVisible = true,
      editingInvoiceId = invoice.id,
      invoiceDate = if (invoice.createdAt.isNotBlank()) invoice.createdAt else ArabicNumberHelper.formatDateTime(),
      invNum = invoice.invNum,
      invType = invoice.invType,
      customerAccount = invoice.customerAccount,
      customerName = invoice.customerName,
      cardId = invoice.cardId,
      price = if (invoice.price > 0.0) invoice.price.toString() else "",
      subscriptionType = invoice.type,
      qty = if (invoice.qty > 0.0) invoice.qty.toString().removeSuffix(".0") else "1",
      desc = invoice.desc.trim().ifEmpty { "تجديد باقه تميز" },
      endDate = invoice.endDate,
      currency = invoice.currency,
      extraItems = invoice.extraItems,
      showSavedInvoicesModal = false,
      currentScreen = AppScreen.MAIN
    )
    showToast("تم تحميل الفاتورة رقم ${invoice.invNum} للتعديل. يمكنك تغيير التاريخ وجميع البيانات.")
  }

  fun deleteSavedInvoice(id: Long) {
    val invToDelete = repository.savedInvoices.find { it.id == id }
    val updated = repository.savedInvoices.filter { it.id != id }
    repository.saveInvoices(updated)
    if (invToDelete != null && invToDelete.invType == "أجل") {
      val targetAcc = ArabicNumberHelper.toEngDigits(invToDelete.customerAccount).trim()
      val cleanInvNum = ArabicNumberHelper.toEngDigits(invToDelete.invNum).trim()
      val custIdx = repository.customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == targetAcc }
      if (custIdx != -1) {
        val cust = repository.customers[custIdx]
        val filteredTx = cust.transactions.filterNot { t ->
          t.type == "فاتورة" && (
            (t.voucherNum != null && ArabicNumberHelper.toEngDigits(t.voucherNum).trim() == cleanInvNum) ||
            t.note.contains("($cleanInvNum)") ||
            t.note.contains("رقم $cleanInvNum")
          )
        }
        repository.customers[custIdx] = repository.recalculateCustomerBalance(cust.copy(transactions = filteredTx))
        repository.saveCustomers()
      }
    }
    _uiState.value = _uiState.value.copy(
      savedInvoices = updated,
      customers = repository.customers
    )
    showToast("تم حذف الفاتورة.")
  }

  fun clearAllInvoices() {
    repository.saveInvoices(emptyList())
    _uiState.value = _uiState.value.copy(savedInvoices = emptyList())
    showToast("تم حذف جميع الفواتير المحفوظة.")
  }

  fun exportBackup(): String {
    return repository.exportAllBackupJson()
  }

  fun importBackup(json: String): Boolean {
    val result = repository.importBackupJson(json)
    if (result.success) {
      _uiState.value = _uiState.value.copy(
        storeConfig = repository.storeConfig,
        exchangeRates = repository.exchangeRates,
        customers = repository.customers,
        savedInvoices = repository.savedInvoices,
        invNum = repository.getNextInvoiceNumber()
      )
      loadRoomBackupData()
      showToast("✅ تم استعادة النسخة الاحتياطية بنجاح (${result.invoiceCount} فاتورة، ${result.customerCount} عميل).")
      return true
    } else {
      showToast("❌ ${result.errorMessage.ifBlank { "فشل استعادة الملف. تأكد من صحة التنسيق." }}")
      return false
    }
  }

  fun setRoomBackupModalVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showRoomBackupModal = visible)
    if (visible) {
      loadRoomBackupData()
    }
  }

  fun setYearEndClosingModalVisible(visible: Boolean) {
    _uiState.value = _uiState.value.copy(showYearEndClosingModal = visible)
  }

  fun addFiscalYear(year: Int) {
    repository.addFiscalYear(year)
    _uiState.value = _uiState.value.copy(availableFiscalYears = repository.availableFiscalYears)
  }

  fun switchFiscalYear(targetYear: Int) {
    repository.switchFiscalYear(targetYear)
    val nextInv = repository.getNextInvoiceNumber().ifEmpty { "1" }
    _uiState.value = _uiState.value.copy(
      activeFiscalYear = repository.activeFiscalYear,
      savedInvoices = repository.savedInvoices,
      customers = repository.customers,
      nextReceiptVoucherNum = repository.nextReceiptVoucherNum,
      nextPaymentVoucherNum = repository.nextPaymentVoucherNum,
      invNum = nextInv,
      isFormVisible = false,
      editingInvoiceId = null
    )
    showToast("✅ تم التبديل إلى السنة المالية ($targetYear) وتحديث جميع البيانات بنجاح.")
  }

  fun performYearEndClosing(fromYear: Int, toYear: Int) {
    val success = repository.performYearEndClosing(fromYear, toYear)
    if (success) {
      val nextInv = repository.getNextInvoiceNumber().ifEmpty { "1" }
      _uiState.value = _uiState.value.copy(
        activeFiscalYear = repository.activeFiscalYear,
        availableFiscalYears = repository.availableFiscalYears,
        savedInvoices = repository.savedInvoices,
        customers = repository.customers,
        nextReceiptVoucherNum = 1,
        nextPaymentVoucherNum = 1,
        invNum = nextInv,
        isFormVisible = false,
        editingInvoiceId = null,
        showYearEndClosingModal = false
      )
      showToast("🎉 تم الإقفال السنوي لسنة $fromYear وفتح السنة المالية $toYear بنجاح! الأرصدة رحلت كأرصدة افتتاحية وبدأت الفواتير والسندات من الرقم 1.")
    } else {
      showToast("❌ تعذر إتمام الإقفال السنوي.")
    }
  }

  fun loadRoomBackupData() {
    viewModelScope.launch {
      try {
        val snapshots = repository.roomBackupManager.getAllRoomSnapshots()
        val stats = repository.roomBackupManager.getRoomDatabaseStats()
        _uiState.value = _uiState.value.copy(
          roomSnapshots = snapshots,
          roomStats = stats
        )
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun createLocalRoomBackup(title: String = "", note: String = "") {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRoomOperationInProgress = true)
      try {
        val snapshot = repository.roomBackupManager.createLocalRoomBackup(
          title = title,
          invoices = repository.savedInvoices,
          customers = repository.customers,
          storeConfig = repository.storeConfig,
          exchangeRates = repository.exchangeRates,
          nextReceiptVoucherNum = repository.nextReceiptVoucherNum,
          nextPaymentVoucherNum = repository.nextPaymentVoucherNum,
          note = note
        )
        val snapshots = repository.roomBackupManager.getAllRoomSnapshots()
        val stats = repository.roomBackupManager.getRoomDatabaseStats()
        _uiState.value = _uiState.value.copy(
          roomSnapshots = snapshots,
          roomStats = stats,
          isRoomOperationInProgress = false
        )
        showToast("✅ تم إنشاء نسخة احتياطية محلياً بنجاح! (${snapshot.invoiceCount} فاتورة، ${snapshot.customerCount} عميل)")
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isRoomOperationInProgress = false)
        showToast("❌ حدث خطأ أثناء إنشاء النسخة الاحتياطية: ${e.message}")
      }
    }
  }

  fun restoreFromRoomSnapshot(snapshot: RoomBackupSnapshotEntity) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRoomOperationInProgress = true)
      try {
        val result = repository.importBackupJson(snapshot.backupDataJson)
        if (result.success) {
          _uiState.value = _uiState.value.copy(
            storeConfig = repository.storeConfig,
            exchangeRates = repository.exchangeRates,
            customers = repository.customers,
            savedInvoices = repository.savedInvoices,
            invNum = repository.getNextInvoiceNumber(),
            isRoomOperationInProgress = false
          )
          loadRoomBackupData()
          showToast("✅ تم استعادة البيانات بنجاح من النسخة (${snapshot.title}).")
        } else {
          _uiState.value = _uiState.value.copy(isRoomOperationInProgress = false)
          showToast("❌ فشل استعادة النسخة: ${result.errorMessage}")
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isRoomOperationInProgress = false)
        showToast("❌ خطأ أثناء الاستعادة: ${e.message}")
      }
    }
  }

  fun deleteRoomSnapshot(id: Long) {
    viewModelScope.launch {
      try {
        repository.roomBackupManager.deleteSnapshot(id)
        loadRoomBackupData()
        showToast("تم حذف النسخة الاحتياطية من قاعدة البيانات.")
      } catch (e: Exception) {
        showToast("خطأ أثناء الحذف: ${e.message}")
      }
    }
  }

  fun exportAndShareBackup(context: Context) {
    viewModelScope.launch {
      try {
        val file = repository.roomBackupManager.exportBackupFile(
          invoices = repository.savedInvoices,
          customers = repository.customers,
          storeConfig = repository.storeConfig,
          exchangeRates = repository.exchangeRates,
          nextReceiptVoucherNum = repository.nextReceiptVoucherNum,
          nextPaymentVoucherNum = repository.nextPaymentVoucherNum
        )
        if (file != null && file.exists()) {
          val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
          )
          val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("Mamlaka_Backup", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          }
          val chooser = Intent.createChooser(intent, "مشاركة / حفظ النسخة الاحتياطية")
          chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          context.startActivity(chooser)
          showToast("✅ تم إنشاء وتصدير ملف النسخة الاحتياطية: ${file.name}")
        } else {
          showToast("❌ تعذر تصدير ملف النسخة الاحتياطية.")
        }
      } catch (e: Exception) {
        showToast("❌ خطأ أثناء تصدير الملف: ${e.message}")
      }
    }
  }

  fun saveBackupToUri(context: Context, uri: Uri) {
    viewModelScope.launch {
      try {
        val json = repository.exportAllBackupJson()
        context.contentResolver.openOutputStream(uri)?.use { output ->
          output.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(json)
          }
        }
        showToast("✅ تم حفظ ملف النسخة الاحتياطية في جهازك بنجاح!")
      } catch (e: Exception) {
        showToast("❌ فشل حفظ الملف: ${e.message}")
      }
    }
  }

  fun importBackupFromUri(context: Context, uri: Uri) {
    viewModelScope.launch {
      try {
        val content = context.contentResolver.openInputStream(uri)?.use { input ->
          input.bufferedReader(Charsets.UTF_8).readText()
        }
        if (!content.isNullOrBlank()) {
          val ok = importBackup(content)
          if (ok) {
            loadRoomBackupData()
          }
        } else {
          showToast("❌ الملف فارغ أو تعذر قراءته.")
        }
      } catch (e: Exception) {
        showToast("❌ خطأ أثناء قراءة الملف: ${e.message}")
      }
    }
  }

  // ==========================================
  // النسخ الاحتياطي التلقائي اليومي
  // ==========================================

  fun setAutoDailyBackupEnabled(enabled: Boolean) {
    repository.setAutoDailyBackupEnabled(enabled)
    _uiState.value = _uiState.value.copy(isAutoDailyBackupEnabled = enabled)
    showToast(if (enabled) "تم تفعيل النسخ الاحتياطي التلقائي اليومي بنجاح." else "تم إيقاف النسخ الاحتياطي التلقائي اليومي.")
    if (enabled) {
      performDailyAutoBackupIfNeeded(getApplication())
    }
  }

  fun triggerManualAutoBackupNow(context: Context) {
    viewModelScope.launch {
      showToast("⏳ جاري إنشاء وتصدير النسخة الاحتياطية إلى وحدة التخزين...")
      val file = repository.performDailyAutoBackup(context, force = true)
      if (file != null && file.exists()) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        _uiState.value = _uiState.value.copy(
          lastAutoBackupDate = todayStr,
          roomSnapshots = repository.roomBackupManager.getAllRoomSnapshots()
        )
        showToast("✅ تم تصدير النسخة الاحتياطية بنجاح إلى وحدة التخزين:\n${file.name}")
      } else {
        showToast("❌ تعذر تصدير النسخة الاحتياطية.")
      }
    }
  }

  fun performDailyAutoBackupIfNeeded(context: Context) {
    if (!repository.isAutoDailyBackupEnabled) return
    viewModelScope.launch {
      val file = repository.performDailyAutoBackup(context, force = false)
      if (file != null && file.exists()) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        _uiState.value = _uiState.value.copy(
          lastAutoBackupDate = todayStr,
          roomSnapshots = repository.roomBackupManager.getAllRoomSnapshots()
        )
        showToast("🔄 تم تصدير نسخة احتياطية تلقائية لليوم إلى وحدة التخزين بنجاح.")
      }
    }
  }

  // ==========================================
  // إدارة تنبيهات ومواعيد العملاء
  // ==========================================

  fun addCustomerReminder(reminder: CustomerReminder) {
    val current = _uiState.value.customerReminders.toMutableList()
    current.add(0, reminder)
    repository.saveCustomerReminders(current)
    val dueCount = current.count { !it.isCompleted && it.dueTimestamp <= System.currentTimeMillis() }
    _uiState.value = _uiState.value.copy(
      customerReminders = current,
      dueRemindersCount = dueCount
    )
    showToast("✅ تم حفظ التنبيه للعميل (${reminder.customerName}) بنجاح.")
  }

  fun updateCustomerReminder(reminder: CustomerReminder) {
    val current = _uiState.value.customerReminders.map { if (it.id == reminder.id) reminder else it }
    repository.saveCustomerReminders(current)
    val dueCount = current.count { !it.isCompleted && it.dueTimestamp <= System.currentTimeMillis() }
    _uiState.value = _uiState.value.copy(
      customerReminders = current,
      dueRemindersCount = dueCount
    )
    showToast("✅ تم تحديث التنبيه بنجاح.")
  }

  fun deleteCustomerReminder(reminderId: String) {
    val current = _uiState.value.customerReminders.filter { it.id != reminderId }
    repository.saveCustomerReminders(current)
    val dueCount = current.count { !it.isCompleted && it.dueTimestamp <= System.currentTimeMillis() }
    _uiState.value = _uiState.value.copy(
      customerReminders = current,
      dueRemindersCount = dueCount
    )
    showToast("🗑️ تم حذف التنبيه بنجاح.")
  }

  fun toggleCustomerReminderCompleted(reminderId: String) {
    val current = _uiState.value.customerReminders.map { rem ->
      if (rem.id == reminderId) {
        val nextCompleted = !rem.isCompleted
        rem.copy(
          isCompleted = nextCompleted,
          completedAt = if (nextCompleted) ArabicNumberHelper.formatDateTime() else ""
        )
      } else rem
    }
    repository.saveCustomerReminders(current)
    val dueCount = current.count { !it.isCompleted && it.dueTimestamp <= System.currentTimeMillis() }
    _uiState.value = _uiState.value.copy(
      customerReminders = current,
      dueRemindersCount = dueCount
    )
    showToast("✔️ تم تحديث حالة التنبيه بنجاح.")
  }

  fun dismissDueRemindersAlert() {
    _uiState.value = _uiState.value.copy(showDueRemindersAlert = false)
  }

  fun openCustomerRemindersShortcut() {
    _uiState.value = _uiState.value.copy(
      showCustomersModal = true,
      customersModalInitialTab = 7, // Tab 8: "8. 🔔 التنبيهات"
      showDueRemindersAlert = false
    )
  }

  fun checkAndNotifyDueRemindersNow() {
    try {
      NotificationHelper.checkAndNotifyDueReminders(
        getApplication(),
        repository.customerReminders,
        repository.storeConfig.storeNameAr.ifBlank { "المتجر" }
      )
    } catch (_: Exception) {}
  }
}
