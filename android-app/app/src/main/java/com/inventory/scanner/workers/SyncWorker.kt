package com.inventory.scanner.workers

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventory.scanner.InventoryScannerApp
import com.inventory.scanner.network.ApiClient
import com.inventory.scanner.network.ScanRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "inventory_sync_work"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Starting automatic sync...")
            
            val database = InventoryScannerApp.database
            val pendingTransactions = database.transactionDao().getPendingTransactions()

            if (pendingTransactions.isEmpty()) {
                Log.d(TAG, "✅ No pending transactions to sync")
                return@withContext Result.success()
            }

            Log.d(TAG, "📦 Found ${pendingTransactions.size} pending transactions")
            val deviceId = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            var successCount = 0
            var failCount = 0

            val timeFmt = java.text.SimpleDateFormat("HH:mm, dd MMM yyyy", java.util.Locale.getDefault())

            for (transaction in pendingTransactions) {
                try {
                    val originalTime = timeFmt.format(java.util.Date(transaction.timestamp))
                    val syncedNotes = buildString {
                        if (!transaction.notes.isNullOrBlank()) append("${transaction.notes} · ")
                        append("Offline scan: $originalTime")
                    }
                    val request = ScanRequest(
                        barcode = transaction.barcode,
                        name = transaction.name,
                        type = transaction.type,
                        quantity = transaction.quantity,
                        notes = syncedNotes,
                        deviceId = "OFFLINE-$deviceId"
                    )

                    
                    val response = ApiClient.apiService.sendScan(request)
                    
                    if (response.isSuccessful) {
                        
                        database.transactionDao().markAsSynced(transaction.id)
                        successCount++
                        Log.d(TAG, "✅ Synced: ${transaction.barcode} (ID: ${transaction.id})")
                    } else {
                        failCount++
                        Log.w(TAG, "⚠️ Failed to sync: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    failCount++
                    Log.e(TAG, "❌ Error syncing transaction: ${e.message}", e)
                }
            }

            Log.d(TAG, "🎯 Sync complete: $successCount success, $failCount failed")

            
            if (failCount == 0) {
                Result.success()
            } else if (successCount > 0) {
                
                Result.retry()
            } else {
                
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Sync worker failed: ${e.message}", e)
            Result.retry()
        }
    }
}
