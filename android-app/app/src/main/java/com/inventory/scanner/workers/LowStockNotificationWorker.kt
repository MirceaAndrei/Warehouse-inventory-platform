package com.inventory.scanner.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventory.scanner.InventoryScannerApp
import com.inventory.scanner.LoginActivity
import com.inventory.scanner.MainActivity
import com.inventory.scanner.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LowStockNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "LowStockWorker"
        const val WORK_NAME = "low_stock_check"
        const val CHANNEL_ID = "low_stock_alerts"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val authManager = InventoryScannerApp.authManager
            if (!authManager.isLoggedIn()) return@withContext Result.success()

            val response = ApiClient.apiService.getAllProducts()
            if (!response.isSuccessful) return@withContext Result.retry()

            val products = response.body() ?: return@withContext Result.success()

            val lowStock = products.filter { product ->
                val qty = product.quantity ?: 0
                val threshold = if ((product.minQuantity ?: 0) > 0) product.minQuantity!! else 5
                qty in 1..threshold
            }
            val outOfStock = products.filter { (it.quantity ?: 0) == 0 }

            if (lowStock.isEmpty() && outOfStock.isEmpty()) {
                Log.d(TAG, "All stock levels OK")
                return@withContext Result.success()
            }

            sendNotification(lowStock.size, outOfStock.size, lowStock.firstOrNull()?.name)
            Log.d(TAG, "Notification sent: ${lowStock.size} low, ${outOfStock.size} out of stock")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stock: ${e.message}", e)
            Result.retry()
        }
    }

    private fun sendNotification(lowCount: Int, outCount: Int, firstName: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            outCount > 0 && lowCount > 0 -> "⚠️ Stock Alert"
            outCount > 0 -> "❌ Out of Stock Alert"
            else -> "⚠️ Low Stock Alert"
        }

        val lines = mutableListOf<String>()
        if (outCount > 0) lines.add("$outCount product(s) are out of stock")
        if (lowCount > 0) lines.add("$lowCount product(s) running low${if (firstName != null) " (e.g. $firstName)" else ""}")

        val bigText = lines.joinToString("\n")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(lines.firstOrNull() ?: "")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }
}
