package com.example.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    InvoiceEntity::class,
    CustomerEntity::class,
    TransactionEntity::class,
    RoomBackupSnapshotEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun invoiceDao(): InvoiceDao
  abstract fun customerDao(): CustomerDao
  abstract fun transactionDao(): TransactionDao
  abstract fun roomBackupDao(): RoomBackupDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "mamlaka_app_database.db"
        )
          .fallbackToDestructiveMigration(true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
