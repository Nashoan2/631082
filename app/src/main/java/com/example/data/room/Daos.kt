package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
  @Query("SELECT * FROM invoices ORDER BY id DESC")
  suspend fun getAllInvoices(): List<InvoiceEntity>

  @Query("SELECT * FROM invoices ORDER BY id DESC")
  fun getAllInvoicesFlow(): Flow<List<InvoiceEntity>>

  @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
  suspend fun getInvoiceById(id: Long): InvoiceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvoice(invoice: InvoiceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvoices(invoices: List<InvoiceEntity>)

  @Query("DELETE FROM invoices WHERE id = :id")
  suspend fun deleteInvoiceById(id: Long)

  @Query("DELETE FROM invoices")
  suspend fun deleteAllInvoices()

  @Query("SELECT COUNT(*) FROM invoices")
  suspend fun getInvoiceCount(): Int
}

@Dao
interface CustomerDao {
  @Query("SELECT * FROM customers ORDER BY id ASC")
  suspend fun getAllCustomers(): List<CustomerEntity>

  @Query("SELECT * FROM customers ORDER BY id ASC")
  fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

  @Query("SELECT * FROM customers WHERE accountNumber = :accountNumber LIMIT 1")
  suspend fun getCustomerByAccount(accountNumber: String): CustomerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: CustomerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomers(customers: List<CustomerEntity>)

  @Query("DELETE FROM customers WHERE accountNumber = :accountNumber")
  suspend fun deleteCustomerByAccount(accountNumber: String)

  @Query("DELETE FROM customers")
  suspend fun deleteAllCustomers()

  @Query("SELECT COUNT(*) FROM customers")
  suspend fun getCustomerCount(): Int
}

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY id ASC")
  suspend fun getAllTransactions(): List<TransactionEntity>

  @Query("SELECT * FROM transactions WHERE customerAccount = :accountNumber ORDER BY id ASC")
  suspend fun getTransactionsForAccount(accountNumber: String): List<TransactionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<TransactionEntity>)

  @Query("DELETE FROM transactions WHERE customerAccount = :accountNumber")
  suspend fun deleteTransactionsForAccount(accountNumber: String)

  @Query("DELETE FROM transactions")
  suspend fun deleteAllTransactions()
}

@Dao
interface RoomBackupDao {
  @Query("SELECT * FROM backup_snapshots ORDER BY id DESC")
  suspend fun getAllSnapshots(): List<RoomBackupSnapshotEntity>

  @Query("SELECT * FROM backup_snapshots ORDER BY id DESC")
  fun getAllSnapshotsFlow(): Flow<List<RoomBackupSnapshotEntity>>

  @Query("SELECT * FROM backup_snapshots WHERE id = :id LIMIT 1")
  suspend fun getSnapshotById(id: Long): RoomBackupSnapshotEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSnapshot(snapshot: RoomBackupSnapshotEntity): Long

  @Query("DELETE FROM backup_snapshots WHERE id = :id")
  suspend fun deleteSnapshotById(id: Long)

  @Query("DELETE FROM backup_snapshots")
  suspend fun deleteAllSnapshots()

  @Query("SELECT COUNT(*) FROM backup_snapshots")
  suspend fun getSnapshotCount(): Int
}
