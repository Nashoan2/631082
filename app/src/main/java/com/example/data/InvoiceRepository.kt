package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.room.RoomBackupManager
import com.example.util.ArabicNumberHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ImportResult(
  val success: Boolean,
  val invoiceCount: Int = 0,
  val customerCount: Int = 0,
  val errorMessage: String = ""
)

class InvoiceRepository(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("mamlaka_prefs", Context.MODE_PRIVATE)
  val roomBackupManager = RoomBackupManager(context)
  private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  var activeFiscalYear: Int = prefs.getInt("activeFiscalYear", 2026)
    private set

  var availableFiscalYears: MutableList<Int> = loadAvailableFiscalYears()
    private set

  private fun loadAvailableFiscalYears(): MutableList<Int> {
    val raw = prefs.getString("availableFiscalYears", null)
    val list = try {
      if (raw != null) {
        val arr = JSONArray(raw)
        val l = mutableListOf<Int>()
        for (i in 0 until arr.length()) {
          l.add(arr.getInt(i))
        }
        l
      } else {
        mutableListOf(2025, 2026)
      }
    } catch (_: Exception) {
      mutableListOf(2025, 2026)
    }
    if (!list.contains(activeFiscalYear)) {
      list.add(activeFiscalYear)
    }
    if (!list.contains(2025)) {
      list.add(2025)
    }
    return list.distinct().sorted().toMutableList()
  }

  fun saveAvailableFiscalYears(years: List<Int>) {
    availableFiscalYears = years.distinct().sorted().toMutableList()
    val arr = JSONArray()
    for (y in availableFiscalYears) {
      arr.put(y)
    }
    prefs.edit().putString("availableFiscalYears", arr.toString()).apply()
  }

  fun addFiscalYear(year: Int) {
    if (!availableFiscalYears.contains(year)) {
      availableFiscalYears.add(year)
      availableFiscalYears.sort()
      saveAvailableFiscalYears(availableFiscalYears)
    }
  }

  var storeConfig: StoreConfig = loadStoreConfig()
    private set

  var exchangeRates: ExchangeRates = loadExchangeRates()
    private set

  var savedInvoices: MutableList<InvoiceData> = loadSavedInvoices()
    private set

  var customers: MutableList<Customer> = loadCustomers()
    private set

  var nextReceiptVoucherNum: Int = prefs.getInt("nextReceiptVoucherNum_$activeFiscalYear", if (activeFiscalYear == 2026) prefs.getInt("nextReceiptVoucherNum", 1) else 1)
    private set

  var nextPaymentVoucherNum: Int = prefs.getInt("nextPaymentVoucherNum_$activeFiscalYear", if (activeFiscalYear == 2026) prefs.getInt("nextPaymentVoucherNum", 1) else 1)
    private set

  var uiCustomizationConfig: UiCustomizationConfig = loadUiCustomizationConfig()
    private set

  var reportCustomizationConfig: ReportCustomizationConfig = loadReportCustomizationConfig()
    private set

  var isAutoDailyBackupEnabled: Boolean = prefs.getBoolean("isAutoDailyBackupEnabled", false)
    private set

  var lastAutoBackupDate: String = prefs.getString("lastAutoBackupDate", "") ?: ""
    private set

  var customerReminders: MutableList<CustomerReminder> = loadCustomerReminders()
    private set

  fun setAutoDailyBackupEnabled(enabled: Boolean) {
    isAutoDailyBackupEnabled = enabled
    prefs.edit().putBoolean("isAutoDailyBackupEnabled", enabled).apply()
  }

  fun recordAutoDailyBackupCompleted(dateStr: String) {
    lastAutoBackupDate = dateStr
    prefs.edit().putString("lastAutoBackupDate", dateStr).apply()
  }

  fun loadCustomerReminders(): MutableList<CustomerReminder> {
    val raw = prefs.getString("customerRemindersData", null) ?: return mutableListOf()
    return try {
      val arr = JSONArray(raw)
      val list = mutableListOf<CustomerReminder>()
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list.add(
          CustomerReminder(
            id = o.optString("id", java.util.UUID.randomUUID().toString()),
            customerAccount = o.optString("customerAccount", ""),
            customerName = o.optString("customerName", ""),
            customerPhone = o.optString("customerPhone", ""),
            title = o.optString("title", "ميعاد تسديد قسط"),
            note = o.optString("note", ""),
            amountDue = o.optDouble("amountDue", 0.0),
            currency = o.optString("currency", "YER"),
            createdAt = o.optString("createdAt", ""),
            dueDate = o.optString("dueDate", ""),
            dueTimestamp = o.optLong("dueTimestamp", 0L),
            periodPreset = o.optString("periodPreset", "أسبوع"),
            isCompleted = o.optBoolean("isCompleted", false),
            completedAt = o.optString("completedAt", "")
          )
        )
      }
      list
    } catch (_: Exception) {
      mutableListOf()
    }
  }

  fun saveCustomerReminders(reminders: List<CustomerReminder>) {
    customerReminders = reminders.toMutableList()
    try {
      val arr = JSONArray()
      for (r in reminders) {
        val o = JSONObject().apply {
          put("id", r.id)
          put("customerAccount", r.customerAccount)
          put("customerName", r.customerName)
          put("customerPhone", r.customerPhone)
          put("title", r.title)
          put("note", r.note)
          put("amountDue", r.amountDue)
          put("currency", r.currency)
          put("createdAt", r.createdAt)
          put("dueDate", r.dueDate)
          put("dueTimestamp", r.dueTimestamp)
          put("periodPreset", r.periodPreset)
          put("isCompleted", r.isCompleted)
          put("completedAt", r.completedAt)
        }
        arr.put(o)
      }
      prefs.edit().putString("customerRemindersData", arr.toString()).apply()
    } catch (_: Exception) {}
  }

  suspend fun performDailyAutoBackup(context: Context, force: Boolean = false): File? = withContext(Dispatchers.IO) {
    if (!isAutoDailyBackupEnabled && !force) return@withContext null
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    if (!force && lastAutoBackupDate == todayStr) return@withContext null

    try {
      // 1. Create Room database backup snapshot
      roomBackupManager.createBackupSnapshot(
        invoices = savedInvoices,
        customers = customers,
        storeConfig = storeConfig,
        exchangeRates = exchangeRates,
        nextReceiptVoucherNum = nextReceiptVoucherNum,
        nextPaymentVoucherNum = nextPaymentVoucherNum,
        note = "نسخة احتياطية تلقائية يومية ($todayStr)"
      )

      // 2. Export physical file to app backup directory
      val backupDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "RoomDatabaseBackups/AutoDaily")
      if (!backupDir.exists()) backupDir.mkdirs()
      val file = File(backupDir, "Mamlaka_AutoBackup_$todayStr.json")
      val json = roomBackupManager.buildBackupJson(
        invoices = savedInvoices,
        customers = customers,
        storeConfig = storeConfig,
        exchangeRates = exchangeRates,
        nextReceiptVoucherNum = nextReceiptVoucherNum,
        nextPaymentVoucherNum = nextPaymentVoucherNum
      )
      file.writeText(json)

      // 3. Also export copy to Documents or Downloads if possible
      try {
        val publicDocs = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Mamlaka_Backups")
        if (!publicDocs.exists()) publicDocs.mkdirs()
        if (publicDocs.exists() && publicDocs.canWrite()) {
          val publicFile = File(publicDocs, "Mamlaka_AutoBackup_$todayStr.json")
          publicFile.writeText(json)
        }
      } catch (_: Throwable) {}

      recordAutoDailyBackupCompleted(todayStr)
      file
    } catch (e: Exception) {
      null
    }
  }

  init {
    repoScope.launch {
      try {
        val roomInvoices = roomBackupManager.loadInvoicesFromRoom()
        val roomCustomers = roomBackupManager.loadCustomersFromRoom()
        var needUpdate = false
        if (savedInvoices.isEmpty() && roomInvoices.isNotEmpty()) {
          savedInvoices = roomInvoices.toMutableList()
          needUpdate = true
        }
        if (customers.isEmpty() && roomCustomers.isNotEmpty()) {
          customers = roomCustomers.map { recalculateCustomerBalance(it) }.toMutableList()
          needUpdate = true
        }
        if (needUpdate) {
          saveInvoices(savedInvoices)
          saveCustomers(customers)
        }
        // Always guarantee latest state is saved in Room database
        roomBackupManager.syncAllToRoom(savedInvoices, customers)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun loadStoreConfig(): StoreConfig {
    val raw = prefs.getString("storeConfigData", null) ?: return StoreConfig()
    return try {
      val obj = JSONObject(raw)
      StoreConfig(
        storeNameAr = obj.optString("storeNameAr", "المملكة للإلكترونيات"),
        storeNameEn = obj.optString("storeNameEn", "almamlak Electronics"),
        branch = obj.optString("branch", "فرع شارع تعز"),
        phone = obj.optString("phone", "772707736"),
        addressAr = obj.optString("addressAr", "إب الشارع العام تحت فندق معين"),
        addressEn = obj.optString("addressEn", "YEMEN Ibb"),
        wmAr = obj.optString("wmAr", "المملكة للإلكترونيات"),
        terms = obj.optString("terms", "• البضاعة المباعة لا ترد ولا تستبدل بعد خروجها من المحل.\n• استلمت البضاعة الموضحة أعلاه كاملة ، سليمة ، ولعدد ذلك."),
        logoBase64 = obj.optString("logoBase64", "")
      )
    } catch (_: Exception) {
      StoreConfig()
    }
  }

  fun saveStoreConfig(config: StoreConfig) {
    storeConfig = config
    if (config.wmAr.isNotBlank() && reportCustomizationConfig.customWatermarkText != config.wmAr) {
      reportCustomizationConfig = reportCustomizationConfig.copy(customWatermarkText = config.wmAr)
      val rObj = JSONObject().apply {
        put("fontScalePercent", reportCustomizationConfig.fontScalePercent)
        put("fontFamily", reportCustomizationConfig.fontFamily)
        put("primaryTextColorHex", reportCustomizationConfig.primaryTextColorHex)
        put("headerColorHex", reportCustomizationConfig.headerColorHex)
        put("tableBorderColorHex", reportCustomizationConfig.tableBorderColorHex)
        put("tableHeaderBgHex", reportCustomizationConfig.tableHeaderBgHex)
        put("watermarkColorHex", reportCustomizationConfig.watermarkColorHex)
        put("customerNameColorHex", reportCustomizationConfig.customerNameColorHex)
        put("customerAccountColorHex", reportCustomizationConfig.customerAccountColorHex)
        put("currencySymbolColorHex", reportCustomizationConfig.currencySymbolColorHex)
        put("invoiceAmountColorHex", reportCustomizationConfig.invoiceAmountColorHex)
        put("unitPriceColorHex", reportCustomizationConfig.unitPriceColorHex)
        put("totalValueColorHex", reportCustomizationConfig.totalValueColorHex)
        put("quantityColorHex", reportCustomizationConfig.quantityColorHex)
        put("itemNumberColorHex", reportCustomizationConfig.itemNumberColorHex)
        put("grandTotalAmountColorHex", reportCustomizationConfig.grandTotalAmountColorHex)
        put("showLogo", reportCustomizationConfig.showLogo)
        put("showStoreInfo", reportCustomizationConfig.showStoreInfo)
        put("showDateTime", reportCustomizationConfig.showDateTime)
        put("showCustomerAccountNumber", reportCustomizationConfig.showCustomerAccountNumber)
        put("showBranch", reportCustomizationConfig.showBranch)
        put("showAmountInWords", reportCustomizationConfig.showAmountInWords)
        put("showTermsAndNotes", reportCustomizationConfig.showTermsAndNotes)
        put("showSignatures", reportCustomizationConfig.showSignatures)
        put("showWatermark", reportCustomizationConfig.showWatermark)
        put("showCardSubscriptionBox", reportCustomizationConfig.showCardSubscriptionBox)
        put("customWatermarkText", reportCustomizationConfig.customWatermarkText)
        put("customHeaderTitle", reportCustomizationConfig.customHeaderTitle)
        put("customFooterText", reportCustomizationConfig.customFooterText)
        put("taxOrCrNumber", reportCustomizationConfig.taxOrCrNumber)
        put("showStampSeal", reportCustomizationConfig.showStampSeal)
        put("customNoticeBadge", reportCustomizationConfig.customNoticeBadge)
        put("accountantSignatureName", reportCustomizationConfig.accountantSignatureName)
        put("managerSignatureName", reportCustomizationConfig.managerSignatureName)
      }
      prefs.edit().putString("reportCustomizationConfig", rObj.toString()).apply()
    }
    val obj = JSONObject().apply {
      put("storeNameAr", config.storeNameAr)
      put("storeNameEn", config.storeNameEn)
      put("branch", config.branch)
      put("phone", config.phone)
      put("addressAr", config.addressAr)
      put("addressEn", config.addressEn)
      put("wmAr", config.wmAr)
      put("terms", config.terms)
      put("logoBase64", config.logoBase64)
    }
    prefs.edit().putString("storeConfigData", obj.toString()).apply()
  }

  private fun loadUiCustomizationConfig(): UiCustomizationConfig {
    val raw = prefs.getString("uiCustomizationConfig", null) ?: return UiCustomizationConfig()
    return try {
      val obj = JSONObject(raw)
      val uiConfigVersion = obj.optInt("uiConfigVersion", 0)
      if (uiConfigVersion < 3) {
        val def = UiCustomizationConfig()
        saveUiCustomizationConfig(def)
        return def
      }

      val sizeName = obj.optString("buttonSize", ButtonSize.MEDIUM.name)
      val layoutName = obj.optString("buttonLayout", ButtonLayout.SINGLE.name)
      val showShortcuts = obj.optBoolean("showQuickShortcutsBar", true)

      val btnArray = obj.optJSONArray("buttons")
      val loadedButtons = mutableListOf<AppActionButton>()
      if (btnArray != null) {
        for (i in 0 until btnArray.length()) {
          val b = btnArray.getJSONObject(i)
          val btnId = b.optString("id")
          var btnLabel = b.optString("label")
          var btnEmoji = b.optString("iconEmoji")
          var btnColor = b.optString("colorHex")
          var btnAction = b.optString("actionType")

          if (btnId == "PERCENTAGE_CALCULATOR") {
            if (btnLabel.isBlank()) btnLabel = "حاسبة النسبة المئوية"
            if (btnEmoji.isBlank()) btnEmoji = "٪"
            if (btnColor.isBlank()) btnColor = "#1B4D3E"
            if (btnAction.isBlank()) btnAction = "PERCENTAGE_CALCULATOR"
          } else if (btnId == "FULL_CALCULATOR") {
            if (btnLabel.isBlank()) btnLabel = "اله حاسبه النسبة "
            if (btnEmoji.isBlank()) btnEmoji = "🧮"
            if (btnColor.isBlank()) btnColor = "#1E293B"
            if (btnAction.isBlank()) btnAction = "FULL_CALCULATOR"
          }

          if (btnAction.isBlank()) btnAction = btnId

          loadedButtons.add(
            AppActionButton(
              id = btnId,
              label = btnLabel,
              iconEmoji = btnEmoji,
              colorHex = btnColor,
              actionType = btnAction,
              isVisible = b.optBoolean("isVisible", true),
              isQuickShortcut = b.optBoolean("isQuickShortcut", false),
              customParam = b.optString("customParam", ""),
              customDesc = b.optString("customDesc", "")
            )
          )
        }
      }

      val finalButtons = if (loadedButtons.isEmpty()) {
        UiCustomizationConfig.defaultButtons()
      } else {
        val existingIds = loadedButtons.map { it.id }.toSet()
        val missingDefaults = UiCustomizationConfig.defaultButtons().filter { it.id !in existingIds }
        // Ensure both PERCENTAGE_CALCULATOR and FULL_CALCULATOR have isVisible=true and isQuickShortcut=true if newly added
        val adjustedMissing = missingDefaults.map { def ->
          if (def.id == "PERCENTAGE_CALCULATOR" || def.id == "FULL_CALCULATOR") {
            def.copy(isVisible = true, isQuickShortcut = true)
          } else def
        }
        loadedButtons + adjustedMissing
      }

      val hasMigratedHideAllCustomers = prefs.getBoolean("migrated_hide_all_customers_v3", false)
      val processedButtons = if (!hasMigratedHideAllCustomers) {
        prefs.edit().putBoolean("migrated_hide_all_customers_v3", true).apply()
        finalButtons.map { btn ->
          if (btn.id == "ALL_CUSTOMERS") btn.copy(isVisible = false) else btn
        }
      } else {
        finalButtons
      }

      val formFieldsArray = obj.optJSONArray("formFields")
      val loadedFields = mutableListOf<FormCustomField>()
      if (formFieldsArray != null) {
        for (i in 0 until formFieldsArray.length()) {
          val f = formFieldsArray.getJSONObject(i)
          loadedFields.add(
            FormCustomField(
              id = f.optString("id"),
              title = f.optString("title"),
              iconEmoji = f.optString("iconEmoji"),
              isVisible = f.optBoolean("isVisible", true),
              isRequired = f.optBoolean("isRequired", false)
            )
          )
        }
      }

      val finalFields = if (loadedFields.isEmpty()) {
        UiCustomizationConfig.defaultFormFields()
      } else {
        val existingFieldIds = loadedFields.map { it.id }.toSet()
        val missingFields = UiCustomizationConfig.defaultFormFields().filter { it.id !in existingFieldIds }
        // Ensure default titles and icons are maintained if empty
        val updatedLoaded = loadedFields.map { loaded ->
          val def = UiCustomizationConfig.defaultFormFields().find { it.id == loaded.id }
          if (def != null) {
            loaded.copy(
              title = if (loaded.title.isBlank()) def.title else loaded.title,
              iconEmoji = if (loaded.iconEmoji.isBlank()) def.iconEmoji else loaded.iconEmoji,
              isRequired = def.isRequired
            )
          } else loaded
        }
        updatedLoaded + missingFields
      }

      val shadedColorHex = obj.optString("shadedFieldColorHex", "#FFF0F3")
      val shadedAlpha = obj.optDouble("shadedFieldAlpha", 1.0).toFloat().coerceIn(0.05f, 1.0f)

      UiCustomizationConfig(
        buttonSize = try { ButtonSize.valueOf(sizeName) } catch (_: Exception) { ButtonSize.MEDIUM },
        buttonLayout = try { ButtonLayout.valueOf(layoutName) } catch (_: Exception) { ButtonLayout.SINGLE },
        showQuickShortcutsBar = showShortcuts,
        buttons = processedButtons,
        formFields = finalFields,
        shadedFieldColorHex = shadedColorHex,
        shadedFieldAlpha = shadedAlpha
      )
    } catch (_: Exception) {
      UiCustomizationConfig()
    }
  }

  fun saveUiCustomizationConfig(config: UiCustomizationConfig) {
    uiCustomizationConfig = config
    val obj = JSONObject().apply {
      put("uiConfigVersion", 3)
      put("buttonSize", config.buttonSize.name)
      put("buttonLayout", config.buttonLayout.name)
      put("showQuickShortcutsBar", config.showQuickShortcutsBar)
      put("shadedFieldColorHex", config.shadedFieldColorHex)
      put("shadedFieldAlpha", config.shadedFieldAlpha.toDouble())
      val arr = JSONArray()
      config.buttons.forEach { b ->
        arr.put(JSONObject().apply {
          put("id", b.id)
          put("label", b.label)
          put("iconEmoji", b.iconEmoji)
          put("colorHex", b.colorHex)
          put("actionType", b.actionType)
          put("isVisible", b.isVisible)
          put("isQuickShortcut", b.isQuickShortcut)
          put("customParam", b.customParam)
          put("customDesc", b.customDesc)
        })
      }
      put("buttons", arr)

      val fArr = JSONArray()
      config.formFields.forEach { f ->
        fArr.put(JSONObject().apply {
          put("id", f.id)
          put("title", f.title)
          put("iconEmoji", f.iconEmoji)
          put("isVisible", f.isVisible)
          put("isRequired", f.isRequired)
        })
      }
      put("formFields", fArr)
    }
    prefs.edit().putString("uiCustomizationConfig", obj.toString()).apply()
  }

  fun resetUiCustomizationConfig(): UiCustomizationConfig {
    val defaults = UiCustomizationConfig()
    saveUiCustomizationConfig(defaults)
    return defaults
  }

  private fun loadReportCustomizationConfig(): ReportCustomizationConfig {
    val raw = prefs.getString("reportCustomizationConfig", null) ?: return ReportCustomizationConfig()
    return try {
      val obj = JSONObject(raw)
      val hasMigratedHideBranch = prefs.getBoolean("migrated_hide_branch_default_v2", false)
      val branchVal = if (!hasMigratedHideBranch) {
        prefs.edit().putBoolean("migrated_hide_branch_default_v2", true).apply()
        false
      } else {
        obj.optBoolean("showBranch", false)
      }

      val hasMigratedClosed = prefs.getBoolean("migrated_closed_terms_and_sigs_v1", false)
      val termsVal = if (!hasMigratedClosed) {
        prefs.edit().putBoolean("migrated_closed_terms_and_sigs_v1", true).apply()
        false
      } else {
        obj.optBoolean("showTermsAndNotes", false)
      }
      val sigsVal = if (!hasMigratedClosed) {
        false
      } else {
        obj.optBoolean("showSignatures", false)
      }

      val loadedWatermarkText = obj.optString("customWatermarkText", "")
      val effectiveWatermarkText = if (loadedWatermarkText.isNotBlank()) loadedWatermarkText else storeConfig.wmAr

      ReportCustomizationConfig(
        fontScalePercent = obj.optInt("fontScalePercent", 100),
        fontFamily = obj.optString("fontFamily", "Cairo"),
        primaryTextColorHex = obj.optString("primaryTextColorHex", "#000000"),
        headerColorHex = obj.optString("headerColorHex", "#5E258D"),
        tableBorderColorHex = obj.optString("tableBorderColorHex", "#0070BA"),
        tableHeaderBgHex = obj.optString("tableHeaderBgHex", "#EBF5FB"),
        watermarkColorHex = obj.optString("watermarkColorHex", "#5E258D"),
        customerNameColorHex = obj.optString("customerNameColorHex", "#0070BA"),
        customerAccountColorHex = obj.optString("customerAccountColorHex", "#C62828"),
        currencySymbolColorHex = obj.optString("currencySymbolColorHex", "#111111"),
        invoiceAmountColorHex = obj.optString("invoiceAmountColorHex", "#111111"),
        unitPriceColorHex = obj.optString("unitPriceColorHex", "#111111"),
        totalValueColorHex = obj.optString("totalValueColorHex", "#111111"),
        quantityColorHex = obj.optString("quantityColorHex", "#111111"),
        itemNumberColorHex = obj.optString("itemNumberColorHex", "#111111"),
        grandTotalAmountColorHex = obj.optString("grandTotalAmountColorHex", "#000000"),
        showLogo = obj.optBoolean("showLogo", true),
        showStoreInfo = obj.optBoolean("showStoreInfo", true),
        showDateTime = obj.optBoolean("showDateTime", true),
        showCustomerAccountNumber = obj.optBoolean("showCustomerAccountNumber", true),
        showBranch = branchVal,
        showAmountInWords = obj.optBoolean("showAmountInWords", true),
        showTermsAndNotes = termsVal,
        showSignatures = sigsVal,
        showWatermark = obj.optBoolean("showWatermark", true),
        showCardSubscriptionBox = obj.optBoolean("showCardSubscriptionBox", true),
        customWatermarkText = effectiveWatermarkText,
        customHeaderTitle = obj.optString("customHeaderTitle", ""),
        customFooterText = obj.optString("customFooterText", ""),
        taxOrCrNumber = obj.optString("taxOrCrNumber", ""),
        showStampSeal = obj.optBoolean("showStampSeal", false),
        customNoticeBadge = obj.optString("customNoticeBadge", ""),
        accountantSignatureName = obj.optString("accountantSignatureName", ""),
        managerSignatureName = obj.optString("managerSignatureName", "")
      )
    } catch (_: Exception) {
      ReportCustomizationConfig(
        customWatermarkText = storeConfig.wmAr
      )
    }
  }

  fun saveReportCustomizationConfig(config: ReportCustomizationConfig) {
    reportCustomizationConfig = config
    if (config.customWatermarkText.isNotBlank() && storeConfig.wmAr != config.customWatermarkText) {
      storeConfig = storeConfig.copy(wmAr = config.customWatermarkText)
      val sObj = JSONObject().apply {
        put("storeNameAr", storeConfig.storeNameAr)
        put("storeNameEn", storeConfig.storeNameEn)
        put("branch", storeConfig.branch)
        put("phone", storeConfig.phone)
        put("addressAr", storeConfig.addressAr)
        put("addressEn", storeConfig.addressEn)
        put("wmAr", storeConfig.wmAr)
        put("terms", storeConfig.terms)
        put("logoBase64", storeConfig.logoBase64)
      }
      prefs.edit().putString("storeConfigData", sObj.toString()).apply()
    }
    val obj = JSONObject().apply {
      put("fontScalePercent", config.fontScalePercent)
      put("fontFamily", config.fontFamily)
      put("primaryTextColorHex", config.primaryTextColorHex)
      put("headerColorHex", config.headerColorHex)
      put("tableBorderColorHex", config.tableBorderColorHex)
      put("tableHeaderBgHex", config.tableHeaderBgHex)
      put("watermarkColorHex", config.watermarkColorHex)
      put("customerNameColorHex", config.customerNameColorHex)
      put("customerAccountColorHex", config.customerAccountColorHex)
      put("currencySymbolColorHex", config.currencySymbolColorHex)
      put("invoiceAmountColorHex", config.invoiceAmountColorHex)
      put("unitPriceColorHex", config.unitPriceColorHex)
      put("totalValueColorHex", config.totalValueColorHex)
      put("quantityColorHex", config.quantityColorHex)
      put("itemNumberColorHex", config.itemNumberColorHex)
      put("grandTotalAmountColorHex", config.grandTotalAmountColorHex)
      put("showLogo", config.showLogo)
      put("showStoreInfo", config.showStoreInfo)
      put("showDateTime", config.showDateTime)
      put("showCustomerAccountNumber", config.showCustomerAccountNumber)
      put("showBranch", config.showBranch)
      put("showAmountInWords", config.showAmountInWords)
      put("showTermsAndNotes", config.showTermsAndNotes)
      put("showSignatures", config.showSignatures)
      put("showWatermark", config.showWatermark)
      put("showCardSubscriptionBox", config.showCardSubscriptionBox)
      put("customWatermarkText", config.customWatermarkText)
      put("customHeaderTitle", config.customHeaderTitle)
      put("customFooterText", config.customFooterText)
      put("taxOrCrNumber", config.taxOrCrNumber)
      put("showStampSeal", config.showStampSeal)
      put("customNoticeBadge", config.customNoticeBadge)
      put("accountantSignatureName", config.accountantSignatureName)
      put("managerSignatureName", config.managerSignatureName)
    }
    prefs.edit().putString("reportCustomizationConfig", obj.toString()).apply()
  }

  fun resetReportCustomizationConfig(): ReportCustomizationConfig {
    val defaults = ReportCustomizationConfig()
    saveReportCustomizationConfig(defaults)
    return defaults
  }

  fun loadCustomers(year: Int = activeFiscalYear): MutableList<Customer> {
    var raw = prefs.getString("customersData_$year", null)
    if (raw == null && year == 2026) {
      raw = prefs.getString("customersData", null)
    }
    if (raw == null) {
      if (year == 2026) {
        val defaults = mutableListOf(
          Customer(
            id = 1,
            accountNumber = "100",
            name = "أحمد علي",
            phone = "777111222",
            address = "صنعاء",
            balance = 0.0,
            transactions = emptyList()
          ),
          Customer(
            id = 2,
            accountNumber = "101",
            name = "محمد حسن",
            phone = "777333444",
            address = "عدن",
            balance = 0.0,
            transactions = emptyList()
          )
        )
        saveCustomers(defaults, 2026)
        return defaults
      } else {
        return mutableListOf()
      }
    }
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<Customer>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val txList = mutableListOf<TransactionRecord>()
        val txArray = obj.optJSONArray("transactions")
        if (txArray != null) {
          for (j in 0 until txArray.length()) {
            val tObj = txArray.getJSONObject(j)
            txList.add(
              TransactionRecord(
                date = tObj.optString("date"),
                type = tObj.optString("type"),
                amount = tObj.optDouble("amount"),
                currency = tObj.optString("currency", "$"),
                note = tObj.optString("note"),
                voucherNum = if (tObj.has("voucherNum") && !tObj.isNull("voucherNum")) tObj.optString("voucherNum") else null,
                balanceAfter = tObj.optDouble("balanceAfter")
              )
            )
          }
        }
        list.add(
          Customer(
            id = obj.optLong("id", System.currentTimeMillis()),
            accountNumber = obj.optString("accountNumber", (100 + i).toString()),
            name = obj.optString("name"),
            phone = obj.optString("phone"),
            address = obj.optString("address"),
            balance = obj.optDouble("balance", 0.0),
            transactions = txList
          )
        )
      }
      // Migrate initial untouched template customers from 1001/1002 to 100/101 if applicable
      if (list.size == 2 && list[0].accountNumber == "1001" && list[1].accountNumber == "1002" && list[0].transactions.isEmpty() && list[1].transactions.isEmpty()) {
        val migrated = list.mapIndexed { idx, c -> c.copy(accountNumber = (100 + idx).toString()) }.toMutableList()
        saveCustomers(migrated, year)
        return migrated
      }
      val recalculated = list.map {
        try {
          recalculateCustomerBalance(it)
        } catch (_: Throwable) {
          it
        }
      }.toMutableList()
      recalculated
    } catch (_: Exception) {
      mutableListOf()
    }
  }

  fun saveCustomers(list: List<Customer> = customers, year: Int = activeFiscalYear) {
    if (year == activeFiscalYear) {
      customers = list.toMutableList()
    }
    val array = JSONArray()
    for (c in list) {
      val cObj = JSONObject().apply {
        put("id", c.id)
        put("accountNumber", c.accountNumber)
        put("name", c.name)
        put("phone", c.phone)
        put("address", c.address)
        put("balance", c.balance)
        val txArray = JSONArray()
        for (t in c.transactions) {
          txArray.put(JSONObject().apply {
            put("date", t.date)
            put("type", t.type)
            put("amount", t.amount)
            put("currency", t.currency)
            put("note", t.note)
            put("voucherNum", t.voucherNum)
            put("balanceAfter", t.balanceAfter)
          })
        }
        put("transactions", txArray)
      }
      array.put(cObj)
    }
    val jsonStr = array.toString()
    prefs.edit().putString("customersData_$year", jsonStr).apply()
    if (year == activeFiscalYear) {
      prefs.edit().putString("customersData", jsonStr).apply()
      repoScope.launch {
        try {
          roomBackupManager.syncAllToRoom(savedInvoices, customers)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun getNextAccountNumber(): String {
    if (customers.isEmpty()) return "100"
    var maxAcc = 99
    for (c in customers) {
      val num = ArabicNumberHelper.toEngDigits(c.accountNumber).toIntOrNull()
      if (num != null && num > maxAcc) {
        maxAcc = num
      }
    }
    return if (maxAcc < 100) "100" else (maxAcc + 1).toString()
  }

  fun findCustomerByAccount(account: String): Customer? {
    val clean = ArabicNumberHelper.toEngDigits(account).trim()
    return customers.find { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == clean }
  }

  fun recalculateCustomerBalance(customer: Customer): Customer {
    if (customer.transactions.isEmpty()) {
      return customer
    }

    val rates = try { exchangeRates } catch (_: Throwable) { null } ?: loadExchangeRates()
    // Determine the base currency of this customer account (first non-empty transaction currency or USD)
    val baseCurrency = customer.transactions.firstOrNull { it.currency.isNotBlank() }?.currency ?: "$"

    var currentBalanceInBase = 0.0
    val updatedTransactions = customer.transactions.map { t ->
      val tCurrency = if (t.currency.isNotBlank()) t.currency else baseCurrency
      val convertedAmount = ArabicNumberHelper.convertCurrency(t.amount, tCurrency, baseCurrency, rates)

      when (t.type) {
        "قبض" -> currentBalanceInBase -= convertedAmount
        "صرف", "فاتورة" -> currentBalanceInBase += convertedAmount
        "افتتاح" -> if (t.amount < 0) currentBalanceInBase -= Math.abs(convertedAmount) else currentBalanceInBase += convertedAmount
        else -> currentBalanceInBase += convertedAmount
      }

      val cleanBalance = if (Math.abs(currentBalanceInBase) < 0.005) 0.0 else Math.round(currentBalanceInBase * 100.0) / 100.0
      t.copy(balanceAfter = cleanBalance)
    }

    val finalBalance = if (Math.abs(currentBalanceInBase) < 0.005) 0.0 else Math.round(currentBalanceInBase * 100.0) / 100.0
    return customer.copy(balance = finalBalance, transactions = updatedTransactions)
  }

  fun recalculateAllCustomerBalances(): List<Customer> {
    customers = customers.map { recalculateCustomerBalance(it) }.toMutableList()
    saveCustomers(customers)
    return customers
  }

  fun updateCustomerBalance(
    account: String,
    amount: Double,
    type: String, // "قبض", "صرف", "فاتورة"
    note: String,
    voucherNum: String? = null,
    currency: String = "$",
    customDate: String? = null
  ): Customer? {
    val customerIndex = customers.indexOfFirst {
      ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == ArabicNumberHelper.toEngDigits(account).trim()
    }
    if (customerIndex == -1) return null

    val customer = customers[customerIndex]
    val dateToUse = if (!customDate.isNullOrBlank()) customDate else ArabicNumberHelper.formatDateTime()

    val fullNote = note.trim()

    val updatedTx = customer.transactions.toMutableList().apply {
      add(
        TransactionRecord(
          date = dateToUse,
          type = type,
          amount = amount,
          currency = currency,
          note = fullNote,
          voucherNum = voucherNum,
          balanceAfter = 0.0
        )
      )
    }

    val updatedCustomer = recalculateCustomerBalance(customer.copy(transactions = updatedTx))
    customers[customerIndex] = updatedCustomer
    saveCustomers()
    return updatedCustomer
  }

  fun getAllVouchers(type: String): List<VoucherItem> {
    val list = mutableListOf<VoucherItem>()
    for (c in customers) {
      for (t in c.transactions) {
        if (t.type == type && t.voucherNum != null) {
          list.add(
            VoucherItem(
              voucherNum = t.voucherNum,
              type = t.type,
              account = c.accountNumber,
              customerName = c.name,
              date = t.date,
              amount = t.amount,
              currency = t.currency,
              note = t.note
            )
          )
        }
      }
    }
    return list.sortedByDescending { it.voucherNum.toIntOrNull() ?: 0 }
  }

  fun findVoucher(voucherNum: String, type: String, account: String? = null): Pair<Customer, TransactionRecord>? {
    val cleanVNum = ArabicNumberHelper.toEngDigits(voucherNum).trim()
    val cleanAcc = account?.let { ArabicNumberHelper.toEngDigits(it).trim() }

    if (!cleanAcc.isNullOrEmpty()) {
      val customer = customers.find { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanAcc }
      if (customer != null) {
        val tx = customer.transactions.find { t ->
          t.type == type && (
            (t.voucherNum != null && ArabicNumberHelper.toEngDigits(t.voucherNum).trim() == cleanVNum) ||
            t.note.contains("($cleanVNum)") ||
            t.note.contains("رقم $cleanVNum")
          )
        }
        if (tx != null) return Pair(customer, tx)
      }
    }

    for (c in customers) {
      for (t in c.transactions) {
        if (t.type == type && (
          (t.voucherNum != null && ArabicNumberHelper.toEngDigits(t.voucherNum).trim() == cleanVNum) ||
          t.note.contains("($cleanVNum)") ||
          t.note.contains("رقم $cleanVNum")
        )) {
          return Pair(c, t)
        }
      }
    }
    return null
  }

  fun voucherExists(
    voucherNum: String,
    type: String,
    excludeVoucherNum: String? = null,
    excludeAccount: String? = null
  ): Boolean {
    val cleanNum = ArabicNumberHelper.toEngDigits(voucherNum).trim()
    if (cleanNum.isEmpty()) return false
    val cleanExcludeNum = excludeVoucherNum?.let { ArabicNumberHelper.toEngDigits(it).trim() }
    val cleanExcludeAcc = excludeAccount?.let { ArabicNumberHelper.toEngDigits(it).trim() }

    for (c in customers) {
      val isExcludedAcc = cleanExcludeAcc != null && ArabicNumberHelper.toEngDigits(c.accountNumber).trim() == cleanExcludeAcc
      for (t in c.transactions) {
        if (t.type != type) continue
        val vNum = t.voucherNum?.let { ArabicNumberHelper.toEngDigits(it).trim() }
        val note = ArabicNumberHelper.toEngDigits(t.note)

        val matchesNum = (vNum != null && vNum == cleanNum) ||
            note.contains("($cleanNum)") ||
            note.contains("رقم $cleanNum")

        if (matchesNum) {
          if (isExcludedAcc && cleanExcludeNum != null) {
            val isOldSelf = (vNum != null && vNum == cleanExcludeNum) ||
                note.contains("($cleanExcludeNum)") ||
                note.contains("رقم $cleanExcludeNum")
            if (isOldSelf) {
              continue
            }
          }
          return true
        }
      }
    }
    return false
  }

  fun getUniqueNextReceiptVoucherNumber(): String {
    var num = nextReceiptVoucherNum
    while (voucherExists(num.toString(), "قبض")) {
      num++
    }
    nextReceiptVoucherNum = num + 1
    prefs.edit().putInt("nextReceiptVoucherNum_${activeFiscalYear}", nextReceiptVoucherNum).putInt("nextReceiptVoucherNum", nextReceiptVoucherNum).apply()
    return num.toString()
  }

  fun getUniqueNextPaymentVoucherNumber(): String {
    var num = nextPaymentVoucherNum
    while (voucherExists(num.toString(), "صرف")) {
      num++
    }
    nextPaymentVoucherNum = num + 1
    prefs.edit().putInt("nextPaymentVoucherNum_${activeFiscalYear}", nextPaymentVoucherNum).putInt("nextPaymentVoucherNum", nextPaymentVoucherNum).apply()
    return num.toString()
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
    val cleanOldVNum = ArabicNumberHelper.toEngDigits(oldVoucherNum).trim()
    val match = findVoucher(cleanOldVNum, type, origAccount) ?: return false
    val (origCustomer, origTx) = match
    val cleanTargetAcc = ArabicNumberHelper.toEngDigits(targetAccount).trim()
    val cleanOrigAcc = ArabicNumberHelper.toEngDigits(origCustomer.accountNumber).trim()
    val finalVoucherNum = ArabicNumberHelper.toEngDigits(newVoucherNum).trim().ifEmpty { cleanOldVNum }

    val formattedNote = newNote.trim()

    val updatedRecord = origTx.copy(
      voucherNum = finalVoucherNum,
      amount = newAmount,
      currency = newCurrency,
      note = formattedNote,
      date = if (newDate.isNotBlank()) newDate else origTx.date
    )

    // Advance sequence if higher voucher number entered
    finalVoucherNum.toIntOrNull()?.let { num ->
      if (type == "قبض" && num >= nextReceiptVoucherNum) {
        nextReceiptVoucherNum = num + 1
        prefs.edit().putInt("nextReceiptVoucherNum_${activeFiscalYear}", nextReceiptVoucherNum).putInt("nextReceiptVoucherNum", nextReceiptVoucherNum).apply()
      } else if (type == "صرف" && num >= nextPaymentVoucherNum) {
        nextPaymentVoucherNum = num + 1
        prefs.edit().putInt("nextPaymentVoucherNum_${activeFiscalYear}", nextPaymentVoucherNum).putInt("nextPaymentVoucherNum", nextPaymentVoucherNum).apply()
      }
    }

    fun isMatchingVoucherTx(t: TransactionRecord): Boolean {
      if (t.type != type) return false
      val vNum = t.voucherNum?.let { ArabicNumberHelper.toEngDigits(it).trim() }
      if (!vNum.isNullOrEmpty() && (vNum == cleanOldVNum || vNum == finalVoucherNum)) return true
      val engNote = ArabicNumberHelper.toEngDigits(t.note)
      if (engNote.contains("($cleanOldVNum)") || engNote.contains("($finalVoucherNum)")) return true
      if (engNote.contains("رقم $cleanOldVNum") || engNote.contains("رقم $finalVoucherNum")) return true
      return false
    }

    if (cleanTargetAcc == cleanOrigAcc) {
      // Same customer: update in place and remove any duplicate records
      val custIdx = customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanOrigAcc }
      if (custIdx == -1) return false
      val currentCust = customers[custIdx]
      var replaced = false
      val newTxList = currentCust.transactions.mapNotNull { t ->
        if (isMatchingVoucherTx(t)) {
          if (!replaced) {
            replaced = true
            updatedRecord
          } else {
            null // Remove duplicate
          }
        } else {
          t
        }
      }
      customers[custIdx] = recalculateCustomerBalance(currentCust.copy(transactions = newTxList))
    } else {
      // Different customer: transfer transaction
      val targetIdx = customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanTargetAcc }
      if (targetIdx == -1) return false
      val origIdx = customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanOrigAcc }
      if (origIdx == -1) return false

      // Remove from original
      val currentOrigCust = customers[origIdx]
      val origNewTx = currentOrigCust.transactions.filterNot { isMatchingVoucherTx(it) }
      customers[origIdx] = recalculateCustomerBalance(currentOrigCust.copy(transactions = origNewTx))

      // Add to target, removing any duplicates
      val currentTargetCust = customers[targetIdx]
      val targetCleanedTx = currentTargetCust.transactions.filterNot { isMatchingVoucherTx(it) }
      val targetNewTx = targetCleanedTx + updatedRecord
      customers[targetIdx] = recalculateCustomerBalance(currentTargetCust.copy(transactions = targetNewTx))
    }

    saveCustomers()
    return true
  }

  fun deleteVoucher(voucherNum: String, type: String, account: String? = null): Boolean {
    val cleanVNum = ArabicNumberHelper.toEngDigits(voucherNum).trim()
    val match = findVoucher(cleanVNum, type, account) ?: return false
    val (origCustomer, _) = match
    val cleanOrigAcc = ArabicNumberHelper.toEngDigits(origCustomer.accountNumber).trim()
    val custIdx = customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == cleanOrigAcc }
    if (custIdx == -1) return false

    fun isMatchingVoucherTx(t: TransactionRecord): Boolean {
      if (t.type != type) return false
      val vNum = t.voucherNum?.let { ArabicNumberHelper.toEngDigits(it).trim() }
      if (!vNum.isNullOrEmpty() && vNum == cleanVNum) return true
      val engNote = ArabicNumberHelper.toEngDigits(t.note)
      if (engNote.contains("($cleanVNum)") || engNote.contains("رقم $cleanVNum")) return true
      return false
    }

    val updatedTx = origCustomer.transactions.filterNot { isMatchingVoucherTx(it) }
    customers[custIdx] = recalculateCustomerBalance(origCustomer.copy(transactions = updatedTx))
    saveCustomers()
    return true
  }

  fun updateSavedInvoice(updatedInvoice: InvoiceData, oldInvoice: InvoiceData? = null): Boolean {
    val targetId = updatedInvoice.id
    val cleanNewInvNum = ArabicNumberHelper.toEngDigits(updatedInvoice.invNum).trim()

    // Locate existing invoice by ID or invoice number
    val idx = savedInvoices.indexOfFirst {
      it.id == targetId || ArabicNumberHelper.toEngDigits(it.invNum).trim() == cleanNewInvNum
    }
    val existingOldInv = oldInvoice ?: (if (idx != -1) savedInvoices[idx] else null)
    val cleanOldInvNum = existingOldInv?.let { ArabicNumberHelper.toEngDigits(it.invNum).trim() } ?: cleanNewInvNum
    val oldCustAcc = existingOldInv?.let { ArabicNumberHelper.toEngDigits(it.customerAccount).trim() } ?: ""
    val newCustAcc = ArabicNumberHelper.toEngDigits(updatedInvoice.customerAccount).trim()

    // 1. Update savedInvoices list without duplicates
    val updatedInvoices = savedInvoices.filterNot {
      it.id == targetId || ArabicNumberHelper.toEngDigits(it.invNum).trim() == cleanNewInvNum
    }.toMutableList()

    val insertIndex = if (idx in 0..updatedInvoices.size) idx else 0
    updatedInvoices.add(insertIndex, updatedInvoice)
    savedInvoices.clear()
    savedInvoices.addAll(updatedInvoices)
    saveInvoices()

    // 2. Synchronize transactions in customer accounts
    val newInvNote = if (updatedInvoice.desc.isNotBlank()) {
      "فاتورة رقم (${updatedInvoice.invNum}) - ${updatedInvoice.desc}"
    } else {
      "فاتورة رقم (${updatedInvoice.invNum})"
    }

    val updatedTxRecord = TransactionRecord(
      date = if (updatedInvoice.createdAt.isNotBlank()) updatedInvoice.createdAt else ArabicNumberHelper.formatDateTime(),
      type = "فاتورة",
      amount = updatedInvoice.grandTotal,
      currency = updatedInvoice.currency,
      note = newInvNote,
      voucherNum = updatedInvoice.invNum,
      balanceAfter = 0.0
    )

    fun isMatchingInvoiceTx(t: TransactionRecord): Boolean {
      if (t.type != "فاتورة") return false
      val vNum = t.voucherNum?.let { ArabicNumberHelper.toEngDigits(it).trim() }
      if (!vNum.isNullOrEmpty() && (vNum == cleanOldInvNum || vNum == cleanNewInvNum)) return true
      val engNote = ArabicNumberHelper.toEngDigits(t.note)
      if (engNote.contains("($cleanOldInvNum)") || engNote.contains("($cleanNewInvNum)")) return true
      if (engNote.contains("رقم $cleanOldInvNum") || engNote.contains("رقم $cleanNewInvNum")) return true
      if (existingOldInv != null && t.date == existingOldInv.createdAt && Math.abs(t.amount - existingOldInv.grandTotal) < 0.001) return true
      return false
    }

    // A) If customer account changed, remove old invoice transactions from previous customer
    if (oldCustAcc.isNotEmpty() && oldCustAcc != newCustAcc) {
      val oldCustIdx = customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == oldCustAcc }
      if (oldCustIdx != -1) {
        val oldCust = customers[oldCustIdx]
        val filteredTx = oldCust.transactions.filterNot { isMatchingInvoiceTx(it) }
        customers[oldCustIdx] = recalculateCustomerBalance(oldCust.copy(transactions = filteredTx))
      }
    }

    // B) If invoice is "أجل", update transaction in target customer
    if (updatedInvoice.invType == "أجل") {
      val targetCustIdx = customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == newCustAcc }
      if (targetCustIdx != -1) {
        val targetCust = customers[targetCustIdx]
        val existingTxIndex = targetCust.transactions.indexOfFirst { isMatchingInvoiceTx(it) }
        val newTxList: List<TransactionRecord>
        if (existingTxIndex != -1) {
          // Replace in place and eliminate any duplicates
          var replaced = false
          newTxList = targetCust.transactions.mapNotNull { t ->
            if (isMatchingInvoiceTx(t)) {
              if (!replaced) {
                replaced = true
                updatedTxRecord
              } else {
                null // remove duplicate
              }
            } else {
              t
            }
          }
        } else {
          newTxList = targetCust.transactions + updatedTxRecord
        }
        customers[targetCustIdx] = recalculateCustomerBalance(targetCust.copy(transactions = newTxList))
      }
    } else {
      // If invoice changed to "نقداً", ensure it is removed from customer transactions
      val targetCustIdx = customers.indexOfFirst { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == newCustAcc }
      if (targetCustIdx != -1) {
        val targetCust = customers[targetCustIdx]
        val filteredTx = targetCust.transactions.filterNot { isMatchingInvoiceTx(it) }
        if (filteredTx.size != targetCust.transactions.size) {
          customers[targetCustIdx] = recalculateCustomerBalance(targetCust.copy(transactions = filteredTx))
        }
      }
    }

    saveCustomers()
    return true
  }

  fun deleteCustomer(account: String): Boolean {
    val clean = ArabicNumberHelper.toEngDigits(account).trim()
    val removed = customers.removeAll { ArabicNumberHelper.toEngDigits(it.accountNumber).trim() == clean }
    if (removed) {
      saveCustomers()
    }
    return removed
  }

  fun incrementReceiptVoucher(): Int {
    val current = nextReceiptVoucherNum
    nextReceiptVoucherNum++
    prefs.edit().putInt("nextReceiptVoucherNum_${activeFiscalYear}", nextReceiptVoucherNum).putInt("nextReceiptVoucherNum", nextReceiptVoucherNum).apply()
    return current
  }

  fun updateNextReceiptVoucherNum(num: Int) {
    if (num > nextReceiptVoucherNum) {
      nextReceiptVoucherNum = num
      prefs.edit().putInt("nextReceiptVoucherNum_${activeFiscalYear}", nextReceiptVoucherNum).putInt("nextReceiptVoucherNum", nextReceiptVoucherNum).apply()
    }
  }

  fun incrementPaymentVoucher(): Int {
    val current = nextPaymentVoucherNum
    nextPaymentVoucherNum++
    prefs.edit().putInt("nextPaymentVoucherNum_${activeFiscalYear}", nextPaymentVoucherNum).putInt("nextPaymentVoucherNum", nextPaymentVoucherNum).apply()
    return current
  }

  fun updateNextPaymentVoucherNum(num: Int) {
    if (num > nextPaymentVoucherNum) {
      nextPaymentVoucherNum = num
      prefs.edit().putInt("nextPaymentVoucherNum_${activeFiscalYear}", nextPaymentVoucherNum).putInt("nextPaymentVoucherNum", nextPaymentVoucherNum).apply()
    }
  }

  private fun loadExchangeRates(): ExchangeRates {
    val raw = prefs.getString("exchangeRatesData", null) ?: return ExchangeRates()
    return try {
      val obj = JSONObject(raw)
      ExchangeRates(
        yerToUsd = obj.optDouble("yerToUsd", 0.001),
        usdToYer = obj.optDouble("usdToYer", 1000.0),
        yerToSar = obj.optDouble("yerToSar", 0.0027),
        sarToYer = obj.optDouble("sarToYer", 370.0),
        usdToSar = obj.optDouble("usdToSar", 3.75),
        sarToUsd = obj.optDouble("sarToUsd", 0.2667)
      )
    } catch (_: Exception) {
      ExchangeRates()
    }
  }

  fun saveExchangeRates(rates: ExchangeRates) {
    exchangeRates = rates
    val obj = JSONObject().apply {
      put("yerToUsd", rates.yerToUsd)
      put("usdToYer", rates.usdToYer)
      put("yerToSar", rates.yerToSar)
      put("sarToYer", rates.sarToYer)
      put("usdToSar", rates.usdToSar)
      put("sarToUsd", rates.sarToUsd)
    }
    prefs.edit().putString("exchangeRatesData", obj.toString()).apply()
    val recalculated = customers.map { recalculateCustomerBalance(it) }.toMutableList()
    saveCustomers(recalculated)
  }

  fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
    return ArabicNumberHelper.convertCurrency(amount, fromCurrency, toCurrency, exchangeRates)
  }

  fun loadSavedInvoices(year: Int = activeFiscalYear): MutableList<InvoiceData> {
    var raw = prefs.getString("savedInvoicesData_$year", null)
    if (raw == null && year == 2026) {
      raw = prefs.getString("savedInvoicesData", null)
    }
    if (raw == null) return mutableListOf()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<InvoiceData>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val extraArray = obj.optJSONArray("extraItems")
        val extras = mutableListOf<ExtraItem>()
        if (extraArray != null) {
          for (j in 0 until extraArray.length()) {
            val eObj = extraArray.getJSONObject(j)
            extras.add(
              ExtraItem(
                id = eObj.optLong("id", j.toLong()),
                description = eObj.optString("description"),
                type = eObj.optString("type", "بطولة"),
                price = eObj.optDouble("price", 0.0),
                qty = eObj.optDouble("qty", 1.0),
                currency = eObj.optString("currency", "$")
              )
            )
          }
        }
        list.add(
          InvoiceData(
            id = obj.optLong("id", System.currentTimeMillis()),
            invNum = obj.optString("invNum", "1"),
            invType = obj.optString("invType", "نقداً"),
            customerAccount = obj.optString("customerAccount"),
            customerName = obj.optString("customerName"),
            cardId = obj.optString("cardId"),
            price = obj.optDouble("price", 0.0),
            type = obj.optString("type", "months"),
            qty = obj.optDouble("qty", 1.0),
            desc = obj.optString("desc").trim().ifEmpty { "تجديد باقه تميز" },
            endDate = obj.optString("endDate"),
            currency = obj.optString("currency", "$"),
            extraItems = extras,
            grandTotal = obj.optDouble("grandTotal", 0.0),
            createdAt = obj.optString("createdAt")
          )
        )
      }
      list
    } catch (_: Exception) {
      mutableListOf()
    }
  }

  fun saveInvoices(list: List<InvoiceData> = savedInvoices, year: Int = activeFiscalYear) {
    if (year == activeFiscalYear) {
      savedInvoices = list.toMutableList()
    }
    val array = JSONArray()
    for (inv in list) {
      val obj = JSONObject().apply {
        put("id", inv.id)
        put("invNum", inv.invNum)
        put("invType", inv.invType)
        put("customerAccount", inv.customerAccount)
        put("customerName", inv.customerName)
        put("cardId", inv.cardId)
        put("price", inv.price)
        put("type", inv.type)
        put("qty", inv.qty)
        put("desc", inv.desc)
        put("endDate", inv.endDate)
        put("currency", inv.currency)
        put("grandTotal", inv.grandTotal)
        put("createdAt", inv.createdAt)
        val exArray = JSONArray()
        for (e in inv.extraItems) {
          exArray.put(JSONObject().apply {
            put("id", e.id)
            put("description", e.description)
            put("type", e.type)
            put("price", e.price)
            put("qty", e.qty)
            put("currency", e.currency)
          })
        }
        put("extraItems", exArray)
      }
      array.put(obj)
    }
    val jsonStr = array.toString()
    prefs.edit().putString("savedInvoicesData_$year", jsonStr).apply()
    if (year == activeFiscalYear) {
      prefs.edit().putString("savedInvoicesData", jsonStr).apply()
      repoScope.launch {
        try {
          roomBackupManager.syncAllToRoom(savedInvoices, customers)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun switchFiscalYear(targetYear: Int) {
    // 1. Save currently active year's data
    saveInvoices(savedInvoices, activeFiscalYear)
    saveCustomers(customers, activeFiscalYear)

    // 2. Set new active year
    activeFiscalYear = targetYear
    prefs.edit().putInt("activeFiscalYear", targetYear).apply()

    // 3. Reload data for the chosen year
    savedInvoices = loadSavedInvoices(targetYear)
    customers = loadCustomers(targetYear)
    nextReceiptVoucherNum = prefs.getInt("nextReceiptVoucherNum_$targetYear", if (targetYear == 2026) prefs.getInt("nextReceiptVoucherNum", 1) else 1)
    nextPaymentVoucherNum = prefs.getInt("nextPaymentVoucherNum_$targetYear", if (targetYear == 2026) prefs.getInt("nextPaymentVoucherNum", 1) else 1)

    // 4. Sync in-memory data to Room
    repoScope.launch {
      try {
        roomBackupManager.syncAllToRoom(savedInvoices, customers)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun performYearEndClosing(fromYear: Int, toYear: Int): Boolean {
    return try {
      // 1. Save all fromYear data
      saveInvoices(savedInvoices, fromYear)
      saveCustomers(customers, fromYear)

      // 2. Transfer customers:
      // "وينقل جميع العملاء الذين معهم ارصده والذين لا يوجد معهم ارصده الذين معهم ارصده في الرصيد سيكون في السنه الجديده يعتبر رصيد افتتاحي"
      // "اما ارقام العملاء تظل كما هي"
      val fromCustomers = loadCustomers(fromYear)
      val newCustomers = fromCustomers.map { c ->
        val endBalance = c.balance
        val newTransactions = if (Math.abs(endBalance) >= 0.005) {
          listOf(
            TransactionRecord(
              date = "01/01/$toYear",
              type = "افتتاح",
              amount = endBalance,
              currency = "$",
              note = "رصيد افتتاحي مرحل من إقفال سنة $fromYear",
              voucherNum = "افتتاحي",
              balanceAfter = endBalance
            )
          )
        } else {
          emptyList()
        }
        c.copy(
          balance = endBalance,
          transactions = newTransactions
        )
      }

      // 3. Set up toYear:
      // Invoices start from 1 (empty list)
      saveInvoices(emptyList(), toYear)
      saveCustomers(newCustomers, toYear)
      prefs.edit()
        .putInt("nextReceiptVoucherNum_$toYear", 1)
        .putInt("nextPaymentVoucherNum_$toYear", 1)
        .apply()

      // 4. Update available fiscal years list
      if (!availableFiscalYears.contains(fromYear)) {
        availableFiscalYears.add(fromYear)
      }
      if (!availableFiscalYears.contains(toYear)) {
        availableFiscalYears.add(toYear)
      }
      availableFiscalYears.sort()
      saveAvailableFiscalYears(availableFiscalYears)

      // 5. Automatically switch active year to toYear
      switchFiscalYear(toYear)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  fun getNextInvoiceNumber(): String {
    var maxNum = 0
    for (inv in savedInvoices) {
      val num = ArabicNumberHelper.toEngDigits(inv.invNum).toIntOrNull() ?: 0
      if (num > maxNum) maxNum = num
    }
    return (maxNum + 1).toString()
  }

  fun exportAllBackupJson(): String {
    return roomBackupManager.buildBackupJson(
      invoices = savedInvoices,
      customers = customers,
      storeConfig = storeConfig,
      exchangeRates = exchangeRates,
      nextReceiptVoucherNum = nextReceiptVoucherNum,
      nextPaymentVoucherNum = nextPaymentVoucherNum
    )
  }

  private fun cleanJson(raw: String): String {
    var s = raw.trim()
    if (s.startsWith("\uFEFF")) {
      s = s.substring(1).trim()
    }
    if (s.startsWith("```")) {
      val firstNewline = s.indexOf('\n')
      s = if (firstNewline != -1) {
        s.substring(firstNewline + 1)
      } else {
        s.removePrefix("```json").removePrefix("```")
      }
      if (s.endsWith("```")) {
        s = s.substring(0, s.length - 3)
      }
      s = s.trim()
    }
    return s
  }

  private fun parseSafeDouble(obj: JSONObject, key: String, default: Double = 0.0): Double {
    if (!obj.has(key) || obj.isNull(key)) return default
    val raw = obj.opt(key) ?: return default
    if (raw is Number) return raw.toDouble()
    val str = ArabicNumberHelper.toEngDigits(raw.toString().trim())
      .replace(",", "")
      .replace("$", "")
      .replace("ر.ي", "")
      .replace("ر.س", "")
      .trim()
    return str.toDoubleOrNull() ?: default
  }

  private fun parseSafeLong(obj: JSONObject, key: String, default: Long = 0L): Long {
    if (!obj.has(key) || obj.isNull(key)) return default
    val raw = obj.opt(key) ?: return default
    if (raw is Number) return raw.toLong()
    val str = ArabicNumberHelper.toEngDigits(raw.toString().trim())
      .replace(",", "")
      .trim()
    return str.toLongOrNull() ?: default
  }

  private fun parseInvoicesArray(invArray: JSONArray): List<InvoiceData> {
    val invList = mutableListOf<InvoiceData>()
    for (i in 0 until invArray.length()) {
      val o = invArray.optJSONObject(i) ?: continue
      val exList = mutableListOf<ExtraItem>()
      val exArray = o.optJSONArray("extraItems")
      if (exArray != null) {
        for (j in 0 until exArray.length()) {
          val eo = exArray.optJSONObject(j) ?: continue
          exList.add(
            ExtraItem(
              id = parseSafeLong(eo, "id", System.currentTimeMillis() + j),
              description = eo.optString("description", eo.optString("desc", "")),
              type = eo.optString("type", "بطولة"),
              price = parseSafeDouble(eo, "price", 0.0),
              qty = parseSafeDouble(eo, "qty", 1.0),
              currency = eo.optString("currency", "$")
            )
          )
        }
      }
      val invId = parseSafeLong(o, "id", System.currentTimeMillis() + i)
      val invNum = ArabicNumberHelper.toEngDigits(o.optString("invNum", (i + 1).toString()))
      val invType = o.optString("invType", "نقداً")
      val customerAccount = o.optString("customerAccount", "")
      val customerName = o.optString("customerName", "")
      val cardId = o.optString("cardId", "")
      val price = parseSafeDouble(o, "price", 0.0)
      val type = o.optString("type", "months")
      val qty = parseSafeDouble(o, "qty", 1.0)
      val desc = o.optString("desc").trim().ifEmpty { "تجديد باقه تميز" }
      val endDate = o.optString("endDate", "")
      val currency = o.optString("currency", "$")
      val grandTotal = parseSafeDouble(o, "grandTotal", price * qty)
      val createdAt = o.optString("createdAt", "")

      invList.add(
        InvoiceData(
          id = invId,
          invNum = invNum,
          invType = invType,
          customerAccount = customerAccount,
          customerName = customerName,
          cardId = cardId,
          price = price,
          type = type,
          qty = qty,
          desc = desc,
          endDate = endDate,
          currency = currency,
          extraItems = exList,
          grandTotal = grandTotal,
          createdAt = createdAt
        )
      )
    }
    return invList
  }

  private fun parseCustomersArray(cArray: JSONArray): List<Customer> {
    val cList = mutableListOf<Customer>()
    for (i in 0 until cArray.length()) {
      val co = cArray.optJSONObject(i) ?: continue
      val txList = mutableListOf<TransactionRecord>()
      val txArray = co.optJSONArray("transactions")
      if (txArray != null) {
        for (j in 0 until txArray.length()) {
          val to = txArray.optJSONObject(j) ?: continue
          val rawVoucher = to.opt("voucherNum")?.toString()?.trim()
          val voucherNum = if (rawVoucher != null && rawVoucher != "null" && rawVoucher.isNotBlank()) rawVoucher else null
          txList.add(
            TransactionRecord(
              date = to.optString("date", ""),
              type = to.optString("type", ""),
              amount = parseSafeDouble(to, "amount", 0.0),
              currency = to.optString("currency", "$"),
              note = to.optString("note", ""),
              voucherNum = voucherNum,
              balanceAfter = parseSafeDouble(to, "balanceAfter", 0.0)
            )
          )
        }
      }
      val cId = parseSafeLong(co, "id", System.currentTimeMillis() + i)
      val accNum = ArabicNumberHelper.toEngDigits(co.optString("accountNumber", (100 + i).toString()))
      val name = co.optString("name", "")
      val phone = co.optString("phone", "")
      val address = co.optString("address", "")
      val balance = parseSafeDouble(co, "balance", 0.0)

      cList.add(
        Customer(
          id = cId,
          accountNumber = accNum,
          name = name,
          phone = phone,
          address = address,
          balance = balance,
          transactions = txList
        )
      )
    }
    return cList
  }

  fun importBackupJson(jsonStr: String): ImportResult {
    return try {
      val clean = cleanJson(jsonStr)
      if (clean.isBlank()) {
        return ImportResult(false, errorMessage = "ملف أو نص النسخة الاحتياطية فارغ.")
      }

      // Case 1: Direct JSON Array
      if (clean.startsWith("[")) {
        val arr = JSONArray(clean)
        if (arr.length() == 0) {
          return ImportResult(false, errorMessage = "مصفوفة البيانات فارغة.")
        }
        val first = arr.optJSONObject(0)
        if (first != null && (first.has("invNum") || first.has("cardId") || first.has("customerName") || first.has("grandTotal") || first.has("extraItems"))) {
          val parsedInvoices = parseInvoicesArray(arr)
          saveInvoices(parsedInvoices)
          return ImportResult(true, invoiceCount = parsedInvoices.size, customerCount = customers.size)
        } else if (first != null && (first.has("accountNumber") || first.has("transactions") || first.has("balance"))) {
          val parsedCustomers = parseCustomersArray(arr)
          saveCustomers(parsedCustomers)
          return ImportResult(true, invoiceCount = savedInvoices.size, customerCount = parsedCustomers.size)
        } else {
          return ImportResult(false, errorMessage = "تنسيق مصفوفة البيانات غير معروف.")
        }
      }

      // Case 2: JSON Object
      var root = JSONObject(clean)
      if (root.has("backupDataJson")) {
        val inner = cleanJson(root.getString("backupDataJson"))
        if (inner.startsWith("{")) {
          root = JSONObject(inner)
        }
      }
      if (root.has("data") && root.optJSONObject("data") != null) {
        root = root.getJSONObject("data")
      }

      var restoredInvoices = false
      var restoredCustomers = false
      var restoredStore = false
      var restoredRates = false

      // 1. Invoices
      val invArray = root.optJSONArray("savedInvoices")
        ?: root.optJSONArray("invoices")
        ?: root.optJSONArray("invoiceList")
        ?: (if (root.has("savedInvoicesData")) {
          try { JSONArray(root.getString("savedInvoicesData")) } catch (_: Exception) { null }
        } else null)

      if (invArray != null) {
        val parsedInvoices = parseInvoicesArray(invArray)
        savedInvoices = parsedInvoices.toMutableList()
        val array = JSONArray()
        for (inv in savedInvoices) {
          val obj = JSONObject().apply {
            put("id", inv.id)
            put("invNum", inv.invNum)
            put("invType", inv.invType)
            put("customerAccount", inv.customerAccount)
            put("customerName", inv.customerName)
            put("cardId", inv.cardId)
            put("price", inv.price)
            put("type", inv.type)
            put("qty", inv.qty)
            put("desc", inv.desc)
            put("endDate", inv.endDate)
            put("currency", inv.currency)
            put("grandTotal", inv.grandTotal)
            put("createdAt", inv.createdAt)
            val extraArray = JSONArray()
            for (ex in inv.extraItems) {
              extraArray.put(JSONObject().apply {
                put("id", ex.id)
                put("description", ex.description)
                put("type", ex.type)
                put("price", ex.price)
                put("qty", ex.qty)
                put("currency", ex.currency)
              })
            }
            put("extraItems", extraArray)
          }
          array.put(obj)
        }
        prefs.edit().putString("savedInvoicesData", array.toString()).apply()
        restoredInvoices = true
      }

      // 2. Customers
      val cArray = root.optJSONArray("customers")
        ?: root.optJSONArray("customerList")
        ?: root.optJSONArray("clients")
        ?: (if (root.has("customersData")) {
          try { JSONArray(root.getString("customersData")) } catch (_: Exception) { null }
        } else null)

      if (cArray != null) {
        val parsedCustomers = parseCustomersArray(cArray)
        customers = parsedCustomers.toMutableList()
        val array = JSONArray()
        for (c in customers) {
          val cObj = JSONObject().apply {
            put("id", c.id)
            put("accountNumber", c.accountNumber)
            put("name", c.name)
            put("phone", c.phone)
            put("address", c.address)
            put("balance", c.balance)
            val txArray = JSONArray()
            for (t in c.transactions) {
              txArray.put(JSONObject().apply {
                put("date", t.date)
                put("type", t.type)
                put("amount", t.amount)
                put("currency", t.currency)
                put("note", t.note)
                put("voucherNum", t.voucherNum)
                put("balanceAfter", t.balanceAfter)
              })
            }
            put("transactions", txArray)
          }
          array.put(cObj)
        }
        prefs.edit().putString("customersData", array.toString()).apply()
        restoredCustomers = true
      }

      // 3. Store Config
      val sc = root.optJSONObject("storeConfig")
        ?: root.optJSONObject("store")
        ?: root.optJSONObject("config")
      if (sc != null) {
        saveStoreConfig(
          StoreConfig(
            storeNameAr = sc.optString("storeNameAr", storeConfig.storeNameAr),
            storeNameEn = sc.optString("storeNameEn", storeConfig.storeNameEn),
            branch = sc.optString("branch", storeConfig.branch),
            phone = sc.optString("phone", storeConfig.phone),
            addressAr = sc.optString("addressAr", storeConfig.addressAr),
            addressEn = sc.optString("addressEn", storeConfig.addressEn),
            wmAr = sc.optString("wmAr", storeConfig.wmAr),
            terms = sc.optString("terms", storeConfig.terms),
            logoBase64 = sc.optString("logoBase64", storeConfig.logoBase64)
          )
        )
        restoredStore = true
      }

      // 4. Exchange Rates
      val r = root.optJSONObject("exchangeRates")
        ?: root.optJSONObject("rates")
      if (r != null) {
        saveExchangeRates(
          ExchangeRates(
            yerToUsd = parseSafeDouble(r, "yerToUsd", exchangeRates.yerToUsd),
            usdToYer = parseSafeDouble(r, "usdToYer", exchangeRates.usdToYer),
            yerToSar = parseSafeDouble(r, "yerToSar", exchangeRates.yerToSar),
            sarToYer = parseSafeDouble(r, "sarToYer", exchangeRates.sarToYer),
            usdToSar = parseSafeDouble(r, "usdToSar", exchangeRates.usdToSar),
            sarToUsd = parseSafeDouble(r, "sarToUsd", exchangeRates.sarToUsd)
          )
        )
        restoredRates = true
      }

      // 5. Counters
      if (root.has("nextReceiptVoucherNum")) {
        nextReceiptVoucherNum = root.optInt("nextReceiptVoucherNum", nextReceiptVoucherNum)
        prefs.edit().putInt("nextReceiptVoucherNum", nextReceiptVoucherNum).apply()
      }
      if (root.has("nextPaymentVoucherNum")) {
        nextPaymentVoucherNum = root.optInt("nextPaymentVoucherNum", nextPaymentVoucherNum)
        prefs.edit().putInt("nextPaymentVoucherNum", nextPaymentVoucherNum).apply()
      }

      if (!restoredInvoices && !restoredCustomers && !restoredStore && !restoredRates) {
        return ImportResult(false, errorMessage = "لم يتم العثور على بيانات صالحة (فواتير، عملاء، أو إعدادات) في هذا الملف.")
      }

      // Synchronize atomically to Room
      repoScope.launch {
        try {
          roomBackupManager.syncAllToRoom(savedInvoices, customers)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }

      ImportResult(
        success = true,
        invoiceCount = savedInvoices.size,
        customerCount = customers.size
      )
    } catch (e: Exception) {
      e.printStackTrace()
      ImportResult(false, errorMessage = "خطأ في قراءة ملف النسخة: ${e.localizedMessage ?: e.message}")
    }
  }
}
