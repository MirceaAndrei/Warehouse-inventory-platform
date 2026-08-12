package com.inventory.scanner

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inventory.scanner.data.PendingTransaction
import com.inventory.scanner.data.TransactionDao

@Database(entities = [PendingTransaction::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pending_transactions ADD COLUMN name TEXT")
            }
        }
    }
}
