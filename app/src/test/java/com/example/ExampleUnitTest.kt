package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCustomerMultiCurrencyReconciliation_UsdInvoiceAndYerReceiptVoucher() {
    val rates = com.example.data.ExchangeRates(
      usdToYer = 533.0,
      yerToUsd = 1.0 / 533.0
    )

    // Invoice of 100 USD (عليكم)
    val invoiceAmountUsd = 100.0
    val invoiceCurrency = "$"

    // Receipt voucher of 53,300 YER (لكم)
    val voucherAmountYer = 53300.0
    val voucherCurrency = "YER"

    // Base currency of customer is USD
    val baseCurrency = "$"

    val convertedDebit = com.example.util.ArabicNumberHelper.convertCurrency(
      invoiceAmountUsd,
      invoiceCurrency,
      baseCurrency,
      rates
    )
    val convertedCredit = com.example.util.ArabicNumberHelper.convertCurrency(
      voucherAmountYer,
      voucherCurrency,
      baseCurrency,
      rates
    )

    assertEquals(100.0, convertedDebit, 0.001)
    assertEquals(100.0, convertedCredit, 0.001)

    val netBalance = convertedDebit - convertedCredit
    val finalBalance = if (Math.abs(netBalance) < 0.005) 0.0 else netBalance

    assertEquals(0.0, finalBalance, 0.0)

    val finalBalTitle = if (finalBalance > 0.005) {
      "الباقي عليكم"
    } else if (finalBalance < -0.005) {
      "الباقي لكم"
    } else {
      "الباقي (لكم / عليكم)"
    }
    assertEquals("الباقي (لكم / عليكم)", finalBalTitle)
  }

  @Test
  fun testCustomerBalancePersistenceCalculation() {
    val rates = com.example.data.ExchangeRates(
      usdToYer = 533.0,
      yerToUsd = 1.0 / 533.0
    )
    val txList = listOf(
      com.example.data.TransactionRecord(
        date = "2026-10-01 10:00",
        type = "فاتورة",
        amount = 150.0,
        currency = "$",
        note = "فاتورة مبيعات",
        balanceAfter = 150.0
      ),
      com.example.data.TransactionRecord(
        date = "2026-10-01 11:00",
        type = "قبض",
        amount = 50.0,
        currency = "$",
        note = "سند قبض نقدي",
        balanceAfter = 100.0
      )
    )
    val customer = com.example.data.Customer(
      id = 1L,
      accountNumber = "105",
      name = "سعيد أحمد",
      phone = "777000111",
      address = "صنعاء",
      balance = 0.0,
      transactions = txList
    )

    // Calculate customer balance
    val baseCurrency = customer.transactions.firstOrNull { it.currency.isNotBlank() }?.currency ?: "$"
    var currentBalanceInBase = 0.0
    val updatedTransactions = customer.transactions.map { t ->
      val tCurrency = if (t.currency.isNotBlank()) t.currency else baseCurrency
      val convertedAmount = com.example.util.ArabicNumberHelper.convertCurrency(t.amount, tCurrency, baseCurrency, rates)
      when (t.type) {
        "قبض" -> currentBalanceInBase -= convertedAmount
        "صرف", "فاتورة" -> currentBalanceInBase += convertedAmount
        else -> currentBalanceInBase += convertedAmount
      }
      t.copy(balanceAfter = currentBalanceInBase)
    }
    val finalCustomer = customer.copy(balance = currentBalanceInBase, transactions = updatedTransactions)

    assertEquals(100.0, finalCustomer.balance, 0.001)
    assertEquals(150.0, finalCustomer.transactions[0].balanceAfter, 0.001)
    assertEquals(100.0, finalCustomer.transactions[1].balanceAfter, 0.001)
  }
}
