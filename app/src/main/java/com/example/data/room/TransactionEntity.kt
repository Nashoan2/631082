package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val customerAccount: String,
  val date: String,
  val type: String,
  val amount: Double,
  val currency: String = "$",
  val note: String = "",
  val voucherNum: String? = null,
  val balanceAfter: Double
)
