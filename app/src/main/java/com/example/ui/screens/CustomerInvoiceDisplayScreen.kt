package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.data.Customer
import com.example.data.InvoiceData
import com.example.data.ReportCustomizationConfig
import com.example.data.StoreConfig

@Composable
fun CustomerInvoiceDisplayScreen(
  invoice: InvoiceData,
  storeConfig: StoreConfig,
  reportConfig: ReportCustomizationConfig = ReportCustomizationConfig(),
  customer: Customer? = null,
  onOpenClassicReport: () -> Unit,
  onToggleTerms: ((Boolean) -> Unit)? = null,
  onExportPdf: () -> Unit,
  onBack: () -> Unit
) {
  // Ultra-clear Customer Display Mode matching Image 2
  InvoiceReportScreen(
    invoice = invoice,
    storeConfig = storeConfig,
    reportConfig = reportConfig,
    initialDisplayMode = true,
    onOpenClassicReport = onOpenClassicReport,
    onToggleTerms = onToggleTerms,
    onExportPdf = onExportPdf,
    onBack = onBack
  )
}
