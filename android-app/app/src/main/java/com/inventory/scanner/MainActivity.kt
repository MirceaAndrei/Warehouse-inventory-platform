package com.inventory.scanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import com.inventory.scanner.ui.theme.InventoryScannerTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.inventory.scanner.data.Product
import com.inventory.scanner.ui.*
import com.inventory.scanner.utils.NetworkMonitor
import com.inventory.scanner.workers.LowStockNotificationWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class Screen {
    object Home : Screen()
    object Scan : Screen()
    object ProductList : Screen()
    object AddProduct : Screen()
    object Profile : Screen()
    object UserManagement : Screen()
    data class ScanResult(val barcode: String) : Screen()
    data class ProductDetail(val product: Product) : Screen()
}

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        
        val authManager = InventoryScannerApp.authManager
        if (!authManager.isLoggedIn()) {
            
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
        
        
        NetworkMonitor.schedulePeriodicSync(this)
        scheduleLowStockCheck()
        
        
        lifecycleScope.launch {
            NetworkMonitor.observeNetworkStatus(this@MainActivity).collect { isOnline ->
                if (isOnline) {
                    
                    NetworkMonitor.scheduleSyncWork(this@MainActivity)
                }
            }
        }
        
        setContent {
            val prefs = getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }

            InventoryScannerTheme(darkTheme = isDarkMode) {
                InventoryApp(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = {
                        isDarkMode = !isDarkMode
                        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
                    }
                )
            }
        }
    }

    private fun scheduleLowStockCheck() {
        val wm = WorkManager.getInstance(this)
        
        wm.enqueue(OneTimeWorkRequestBuilder<LowStockNotificationWorker>().build())
        
        val periodic = PeriodicWorkRequestBuilder<LowStockNotificationWorker>(
            15, TimeUnit.MINUTES
        ).build()
        wm.enqueueUniquePeriodicWork(
            LowStockNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )
    }

    

    @Composable
    fun InventoryApp(isDarkMode: Boolean = false, onToggleDarkMode: () -> Unit = {}) {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        var refreshTrigger by remember { mutableStateOf(0) }

        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    onScanClick = { currentScreen = Screen.Scan },
                    onProductListClick = { currentScreen = Screen.ProductList },
                    onAddProductClick = { currentScreen = Screen.AddProduct },
                    onProfileClick = { currentScreen = Screen.Profile },
                    onUsersClick = { currentScreen = Screen.UserManagement },
                    onLogout = {
                        InventoryScannerApp.authManager.logout()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }
            
            is Screen.Scan -> {
                SimpleBarcodeScanner(
                    onBarcodeScanned = { barcode ->
                        currentScreen = Screen.ScanResult(barcode)
                    },
                    onClose = {
                        currentScreen = Screen.Home
                    }
                )
            }
            
            is Screen.ScanResult -> {
                ScanResultScreen(
                    barcode = screen.barcode,
                    userRole = InventoryScannerApp.authManager.getRole() ?: "EMPLOYEE",
                    onProductUpdated = {
                        refreshTrigger++
                    },
                    onClose = {
                        currentScreen = Screen.Home
                    }
                )
            }
            
            is Screen.ProductList -> {
                ProductListScreen(
                    onProductClick = { product ->
                        currentScreen = Screen.ProductDetail(product)
                    },
                    onBack = {
                        currentScreen = Screen.Home
                    }
                )
            }
            
            is Screen.ProductDetail -> {
                ProductDetailScreen(
                    product = screen.product,
                    onBack = {
                        currentScreen = Screen.ProductList
                    },
                    onProductUpdated = {
                        refreshTrigger++
                        currentScreen = Screen.ProductList
                    },
                    onProductDeleted = {
                        refreshTrigger++
                        currentScreen = Screen.ProductList
                    }
                )
            }
            
            is Screen.AddProduct -> {
                AddProductScreen(
                    onBack = {
                        currentScreen = Screen.Home
                    },
                    onProductAdded = {
                        refreshTrigger++
                        currentScreen = Screen.Home
                    }
                )
            }
            
            is Screen.Profile -> {
                ProfileScreen(
                    onBack = { currentScreen = Screen.Home }
                )
            }

            is Screen.UserManagement -> {
                UsersScreen(
                    onBack = { currentScreen = Screen.Home }
                )
            }
        }
    }
}
