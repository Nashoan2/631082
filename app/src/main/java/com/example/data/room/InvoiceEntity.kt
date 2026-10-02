package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
  @PrimaryKey val id: Long,
  val invNum: String,
  val invType: String = "نقداً",
  val customerAccount: String = "",
  val customerName: String = "",
  val cardId: String = "",
  val price: Double = 0.0,
  val type: String = "months",
  val qty: Double = 1.0,
  val desc: String = "",
  val endDate: String = "",
  val currency: String = "$",
  val extraItemsJson: String = "[]",
  val grandTotal: Double = 0.0,
  val createdAt: String = "",
  val savedAtTimestamp: Long = System.currentTimeMillis()
)
