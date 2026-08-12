package com.inventory.scanner.ui

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.inventory.scanner.InventoryScannerApp
import com.inventory.scanner.data.PendingTransaction
import com.inventory.scanner.data.Product
import com.inventory.scanner.network.ApiClient
import com.inventory.scanner.network.ScanRequest
import com.inventory.scanner.utils.NetworkMonitor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    barcode: String,
    userRole: String,
    onProductUpdated: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var product by remember { mutableStateOf<Product?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    
    var transactionType by remember { mutableStateOf("IN") }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<com.inventory.scanner.data.Category>>(emptyList()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val canCreateCategory = userRole == "MANAGER" || userRole == "ADMIN"
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    
    LaunchedEffect(barcode) {
        isLoading = true
        try {
            val response = ApiClient.apiService.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body() != null) {
                product = response.body()
                location = product?.location ?: ""
                category = product?.category ?: ""
                
                productName = product?.name ?: ""
            }
            
            try {
                val catResp = ApiClient.apiService.getAllCategories()
                if (catResp.isSuccessful && catResp.body() != null) {
                    categories = catResp.body()!!
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    
    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false; newCategoryName = "" },
            title = { Text("New Category", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newCategoryName.trim()
                        if (name.isBlank()) return@Button
                        scope.launch {
                            try {
                                val resp = ApiClient.apiService.createCategory(
                                    com.inventory.scanner.data.Category(id = 0L, name = name)
                                )
                                if (resp.isSuccessful && resp.body() != null) {
                                    categories = categories + resp.body()!!
                                    category = resp.body()!!.name
                                    Toast.makeText(context, "Category \"$name\" created", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Could not create category", Toast.LENGTH_SHORT).show()
                                }
                            } catch (_: Exception) {
                                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                            }
                            showNewCategoryDialog = false
                            newCategoryName = ""
                        }
                    },
                    enabled = newCategoryName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false; newCategoryName = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Result",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (product != null) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (product != null) "✓ Product Found" else "⚠ New Product",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Barcode: $barcode",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (product != null) {
                                Text(
                                    text = "Name: ${product!!.name}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Current Stock: ${product!!.quantity}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    
                    if (true) {
                        
                        Text(
                            text = "Transaction Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            @OptIn(ExperimentalMaterial3Api::class)
                            FilterChip(
                                selected = transactionType == "IN",
                                onClick = { transactionType = "IN" },
                                label = { Text("IN") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            @OptIn(ExperimentalMaterial3Api::class)
                            FilterChip(
                                selected = transactionType == "OUT",
                                onClick = { transactionType = "OUT" },
                                label = { Text("OUT") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            @OptIn(ExperimentalMaterial3Api::class)
                            FilterChip(
                                selected = transactionType == "ADJUST",
                                onClick = { transactionType = "ADJUST" },
                                label = { Text("ADJUST") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity") },
                            placeholder = { Text("Enter quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text(if (product == null) "Product Name *" else "Product Name") },
                            placeholder = { Text("Enter product name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = (product == null)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            placeholder = { Text("Product description (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location / Shelf") },
                            placeholder = { Text("e.g., Warehouse A, Shelf 5") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { if (categories.isNotEmpty()) categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                readOnly = categories.isNotEmpty(),
                                trailingIcon = { if (categories.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            category = cat.name
                                            categoryExpanded = false
                                        }
                                    )
                                }
                                if (canCreateCategory) {
                                    Divider()
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                Text("New Category...", color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                            }
                                        },
                                        onClick = { categoryExpanded = false; showNewCategoryDialog = true }
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (optional)") },
                            placeholder = { Text("Add notes about this transaction") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        
                        
                        Button(
                            onClick = {
                                val qtyInt = quantity.toIntOrNull()
                                if (qtyInt == null || qtyInt <= 0) {
                                    Toast.makeText(context, "Invalid quantity", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                if (product == null && productName.isBlank()) {
                                    Toast.makeText(context, "Product name is required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                scope.launch {
                                    isSubmitting = true
                                    try {
                                        val deviceId = Settings.Secure.getString(
                                            context.contentResolver,
                                            Settings.Secure.ANDROID_ID
                                        )
                                        
                                        
                                        val isOnline = NetworkMonitor.isNetworkAvailable(context)
                                        
                                        if (!isOnline) {
                                            
                                            val db = InventoryScannerApp.database
                                            val pendingTransaction = PendingTransaction(
                                                barcode = barcode,
                                                name = if (product == null) productName.takeIf { it.isNotBlank() } else null,
                                                type = transactionType,
                                                quantity = qtyInt,
                                                notes = notes.takeIf { it.isNotBlank() }
                                            )
                                            db.transactionDao().insert(pendingTransaction)
                                            
                                            Toast.makeText(
                                                context,
                                                "💾 Saved offline - will sync when online",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onClose()
                                            isSubmitting = false
                                            return@launch
                                        }
                                        
                                        
                                        
                                        
                                        val scanRequest = ScanRequest(
                                            barcode = barcode,
                                            name = if (product == null) productName else null, 
                                            type = transactionType,
                                            quantity = qtyInt,
                                            notes = notes.takeIf { it.isNotBlank() },
                                            deviceId = deviceId,
                                            location = location.takeIf { it.isNotBlank() },
                                            category = category.takeIf { it.isNotBlank() }
                                        )
                                        
                                        val response = ApiClient.apiService.sendScan(scanRequest)
                                        
                                        if (response.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "✓ Transaction recorded successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onProductUpdated()
                                            onClose()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "⚠ Error: ${response.message()}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        
                                        try {
                                            val db = InventoryScannerApp.database
                                            val pendingTransaction = PendingTransaction(
                                                barcode = barcode,
                                                name = if (product == null) productName.takeIf { it.isNotBlank() } else null,
                                                type = transactionType,
                                                quantity = qtyInt,
                                                notes = notes.takeIf { it.isNotBlank() }
                                            )
                                            db.transactionDao().insert(pendingTransaction)

                                            Toast.makeText(
                                                context,
                                                "💾 Network error - Saved offline",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onClose()
                                        } catch (dbError: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Check, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Confirm Transaction")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
