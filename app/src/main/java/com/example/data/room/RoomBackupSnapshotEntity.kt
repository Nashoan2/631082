package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_snapshots")
data class RoomBackupSnapshotEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val timestamp: String,
  val invoiceCount: Int,
  val customerCount: Int,
  val backupDataJson: String,
  val note: String = "",
  val createdAtMillis: Long = System.currentTimeMillis()
)
