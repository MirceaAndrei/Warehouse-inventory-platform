package com.inventory.scanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.inventory.scanner.auth.AuthManager
import com.inventory.scanner.network.ApiClient
import com.inventory.scanner.workers.LowStockNotificationWorker

class InventoryScannerApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set

        lateinit var authManager: AuthManager
            private set
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "inventory_db"
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()

        authManager = AuthManager(applicationContext)
        ApiClient.initialize(authManager)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            LowStockNotificationWorker.CHANNEL_ID,
            "Low Stock Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when inventory items are running low or out of stock"
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}
