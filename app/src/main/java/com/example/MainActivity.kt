package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.example.ui.theme.LocalShadedFieldColor
import com.example.ui.theme.LocalShadedFieldBorder
import com.example.ui.theme.computeShadedBorderColor
import com.example.ui.theme.parseHexColor
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppThemeCustomizerModal
import com.example.ui.components.ExitConfirmDialog
import com.example.ui.components.ExportedPdfDialog
import com.example.ui.components.ReportCustomizerModal
import com.example.ui.components.UiCustomizerModal
import com.example.ui.screens.CurrencyConverterScreen
import com.example.ui.screens.CustomerInvoiceDisplayScreen
import com.example.ui.screens.CustomerStatementDateRangeDialog
import com.example.ui.screens.CustomerStatementScreen
import com.example.ui.screens.CustomersModal
import com.example.ui.screens.FullCalculatorScreen
import com.example.ui.screens.InvoiceReportScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.PercentageCalculatorScreen
import com.example.ui.screens.RoomBackupModal
import com.example.ui.screens.SavedInvoicesModal
import com.example.ui.screens.SettingsModal
import com.example.ui.screens.YearEndClosingModal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.PrintHelper

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      android.webkit.WebView.enableSlowWholeDocumentDraw()
    } catch (_: Throwable) {}
    enableEdgeToEdge()
    setContent {
      val viewModel: InvoiceViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsState()
      val currentTheme = uiState.uiCustomizationConfig.currentTheme()
      MyApplicationTheme(
        themePreset = currentTheme,
        customPrimaryHex = uiState.uiCustomizationConfig.customPrimaryColorHex,
        customSecondaryHex = uiState.uiCustomizationConfig.customSecondaryColorHex,
        customBgHex = uiState.uiCustomizationConfig.customBgColorHex
      ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AlmamlakaApp(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel
          )
        }
      }
    }

    // Run WebView filesystem warmup in a background thread so it never delays the first frame
    Thread {
      try {
        val webViewBaseDir = java.io.File(dataDir, "app_webview")
        if (!webViewBaseDir.exists()) webViewBaseDir.mkdirs()
        
        val webViewDefaultCache = java.io.File(webViewBaseDir, "Default/HTTP Cache")
        if (!webViewDefaultCache.exists()) webViewDefaultCache.mkdirs()

        val fakeIndex = java.io.File(webViewDefaultCache, "index-dir")
        if (!fakeIndex.exists()) fakeIndex.mkdirs()

        val codeCacheJs = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
        if (!codeCacheJs.exists()) codeCacheJs.mkdirs()
        val codeCacheWasm = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
        if (!codeCacheWasm.exists()) codeCacheWasm.mkdirs()
      } catch (_: Exception) {}
    }.start()
  }
}

@Composable
fun AlmamlakaApp(
  modifier: Modifier = Modifier,
  viewModel: InvoiceViewModel = viewModel()
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission(),
      onResult = { isGranted ->
        if (isGranted) {
          viewModel.checkAndNotifyDueRemindersNow()
        }
      }
    )
    LaunchedEffect(Unit) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  BackHandler(enabled = true) {
    if (uiState.isFormVisible && uiState.currentScreen == AppScreen.MAIN) {
      viewModel.closeInvoiceForm()
    } else if (uiState.currentScreen != AppScreen.MAIN) {
      viewModel.navigateTo(AppScreen.MAIN)
    } else {
      viewModel.setExitConfirmDialogVisible(true)
    }
  }

  val shadedColor = remember(uiState.uiCustomizationConfig.shadedFieldColorHex, uiState.uiCustomizationConfig.shadedFieldAlpha) {
    val rawHex = uiState.uiCustomizationConfig.shadedFieldColorHex
    val base = parseHexColor(rawHex, defaultColor = androidx.compose.ui.graphics.Color(0xFFFFF0F3))
    base.copy(alpha = uiState.uiCustomizationConfig.shadedFieldAlpha.coerceIn(0.05f, 1.0f))
  }
  val shadedBorder = remember(shadedColor) {
    computeShadedBorderColor(shadedColor)
  }

  CompositionLocalProvider(
    LocalShadedFieldColor provides shadedColor,
    LocalShadedFieldBorder provides shadedBorder
  ) {
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
    when (uiState.currentScreen) {
      AppScreen.MAIN -> {
        MainScreen(viewModel = viewModel)
      }
      AppScreen.INVOICE_REPORT -> {
        uiState.previewInvoice?.let { inv ->
          InvoiceReportScreen(
            invoice = inv,
            storeConfig = uiState.storeConfig,
            reportConfig = uiState.reportCustomizationConfig,
            onToggleTerms = { enabled -> viewModel.toggleReportElement("termsAndNotes", enabled) },
            onExportPdf = { viewModel.exportInvoiceToPdf(context, inv) },
            onOpenCustomerDisplay = { viewModel.openCustomerDisplayMode(inv) },
            onBack = { viewModel.navigateTo(AppScreen.MAIN) }
          )
        } ?: run {
          MainScreen(viewModel = viewModel)
        }
      }
      AppScreen.CUSTOMER_INVOICE_DISPLAY -> {
        uiState.previewInvoice?.let { inv ->
          val cust = uiState.customers.find { it.accountNumber == inv.customerAccount }
          CustomerInvoiceDisplayScreen(
            invoice = inv,
            storeConfig = uiState.storeConfig,
            reportConfig = uiState.reportCustomizationConfig,
            customer = cust,
            onToggleTerms = { enabled -> viewModel.toggleReportElement("termsAndNotes", enabled) },
            onOpenClassicReport = { viewModel.openInvoiceReport(inv) },
            onExportPdf = { viewModel.exportInvoiceToPdf(context, inv) },
            onBack = { viewModel.navigateTo(AppScreen.MAIN) }
          )
        } ?: run {
          MainScreen(viewModel = viewModel)
        }
      }
      AppScreen.CUSTOMER_STATEMENT -> {
        uiState.statementCustomer?.let { customer ->
          CustomerStatementScreen(
            customer = customer,
            storeConfig = uiState.storeConfig,
            reportConfig = uiState.reportCustomizationConfig,
            exchangeRates = uiState.exchangeRates,
            startDateStr = uiState.statementStartDate,
            endDateStr = uiState.statementEndDate,
            onChangePeriod = { viewModel.requestStatementDateRange(customer) },
            onExportPdf = {
              viewModel.exportStatementToPdf(
                context = context,
                customer = customer,
                startDateStr = uiState.statementStartDate,
                endDateStr = uiState.statementEndDate
              )
            },
            onBack = { viewModel.navigateTo(AppScreen.MAIN) }
          )
        } ?: run {
          MainScreen(viewModel = viewModel)
        }
      }
      AppScreen.CURRENCY_CONVERTER -> {
        CurrencyConverterScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateTo(AppScreen.MAIN) }
        )
      }
      AppScreen.PERCENTAGE_CALCULATOR -> {
        PercentageCalculatorScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateTo(AppScreen.MAIN) }
        )
      }
      AppScreen.FULL_CALCULATOR -> {
        FullCalculatorScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateTo(AppScreen.MAIN) }
        )
      }
    }

    // Modal Dialogs
    if (uiState.showDateRangeDialog) {
      CustomerStatementDateRangeDialog(
        viewModel = viewModel,
        onDismiss = { viewModel.dismissDateRangeDialog() }
      )
    }

    if (uiState.showCustomersModal) {
      CustomersModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setCustomersModalVisible(false) }
      )
    }

    if (uiState.showSavedInvoicesModal) {
      SavedInvoicesModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setSavedInvoicesModalVisible(false) }
      )
    }

    if (uiState.showSettingsModal) {
      SettingsModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setSettingsModalVisible(false) }
      )
    }

    if (uiState.showRoomBackupModal) {
      RoomBackupModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setRoomBackupModalVisible(false) }
      )
    }

    if (uiState.showUiCustomizerModal) {
      UiCustomizerModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setUiCustomizerModalVisible(false) }
      )
    }

    if (uiState.showReportCustomizerModal) {
      ReportCustomizerModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setReportCustomizerModalVisible(false) }
      )
    }

    if (uiState.showAppThemeCustomizerModal) {
      AppThemeCustomizerModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setAppThemeCustomizerModalVisible(false) }
      )
    }

    if (uiState.showExitConfirmDialog) {
      ExitConfirmDialog(
        onDismiss = { viewModel.setExitConfirmDialogVisible(false) }
      )
    }

    if (uiState.showYearEndClosingModal) {
      YearEndClosingModal(
        viewModel = viewModel,
        onDismiss = { viewModel.setYearEndClosingModalVisible(false) }
      )
    }

    // Exported PDF Success & Action Dialog
    uiState.exportedPdf?.let { pdfInfo ->
      ExportedPdfDialog(
        info = pdfInfo,
        onPrintRequested = {
          when (pdfInfo.source) {
            "invoice" -> uiState.previewInvoice?.let { PrintHelper.printInvoice(context, it, uiState.storeConfig, uiState.reportCustomizationConfig) }
            "statement" -> uiState.statementCustomer?.let {
              PrintHelper.printStatement(
                context = context,
                customer = it,
                storeConfig = uiState.storeConfig,
                startDateStr = uiState.statementStartDate,
                endDateStr = uiState.statementEndDate,
                reportConfig = uiState.reportCustomizationConfig,
                exchangeRates = uiState.exchangeRates
              )
            }
            "all_customers" -> PrintHelper.printAllCustomers(
              context = context,
              customers = uiState.customers,
              storeConfig = uiState.storeConfig,
              reportConfig = uiState.reportCustomizationConfig
            )
            else -> PrintHelper.openPdf(context, pdfInfo.file)
          }
        },
        onDismiss = { viewModel.dismissExportedPdfDialog() }
      )
    }
  }
  }
}
