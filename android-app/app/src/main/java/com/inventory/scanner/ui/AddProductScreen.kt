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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventory.scanner.InventoryScannerApp
import com.inventory.scanner.data.Category
import com.inventory.scanner.data.PendingTransaction
import com.inventory.scanner.data.Product
import com.inventory.scanner.network.ApiClient
import com.inventory.scanner.network.ScanRequest
import com.inventory.scanner.utils.NetworkMonitor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    onProductAdded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRole = InventoryScannerApp.authManager.getRole() ?: "EMPLOYEE"
    val canCreateCategory = userRole == "MANAGER" || userRole == "ADMIN"

    var barcode by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var transactionType by remember { mutableStateOf("IN") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var minQuantity by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    
    var barcodeExists by remember { mutableStateOf<Product?>(null) }
    var isCheckingBarcode by remember { mutableStateOf(false) }

    
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getAllCategories()
            if (res.isSuccessful && res.body() != null) {
                categories = res.body()!!
            }
        } catch (_: Exception) {}
    }

    
    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false; newCategoryName = "" },
            title = { Text("New Category", fontWeight = FontWeight.Bold) },
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
                                    Category(id = 0L, name = name)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            
            Text("Required Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = barcode,
                onValueChange = {
                    barcode = it
                    barcodeExists = null
                },
                label = { Text("Barcode *") },
                placeholder = { Text("Enter or scan barcode") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fs ->
                        if (!fs.isFocused && barcode.isNotBlank()) {
                            scope.launch {
                                isCheckingBarcode = true
                                try {
                                    val res = ApiClient.apiService.getProductByBarcode(barcode.trim())
                                    barcodeExists = if (res.isSuccessful) res.body() else null
                                } catch (_: Exception) {
                                    barcodeExists = null
                                } finally {
                                    isCheckingBarcode = false
                                }
                            }
                        }
                    },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                trailingIcon = {
                    if (isCheckingBarcode) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            )

            
            barcodeExists?.let { existing ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Barcode already in inventory",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "Product: ${existing.name}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "Current stock: ${existing.quantity}  •  Category: ${existing.category ?: "N/A"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                "You are adding a transaction to this existing product.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text(if (barcodeExists == null) "Product Name *" else "Product Name") },
                placeholder = { Text("e.g., Laptop Dell XPS 15") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = barcodeExists == null
            )

            
            Text("Transaction Type *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = transactionType == "IN",
                    onClick = { transactionType = "IN" },
                    label = { Text("IN") },
                    leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = transactionType == "OUT",
                    onClick = { transactionType = "OUT" },
                    label = { Text("OUT") },
                    leadingIcon = { Icon(Icons.Default.Clear, null, Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = transactionType == "ADJUST",
                    onClick = { transactionType = "ADJUST" },
                    label = { Text("ADJUST") },
                    leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            
            Text("Optional Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location / Shelf") },
                placeholder = { Text("e.g., Warehouse A, Shelf 5") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.LocationOn, null) }
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
                            onClick = { category = cat.name; categoryExpanded = false }
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
                                    Text("New Category...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
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
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            if (userRole == "ADMIN") {
                OutlinedTextField(
                    value = minQuantity,
                    onValueChange = { minQuantity = it },
                    label = { Text("Minimum Quantity (Admin Only)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Warning, null) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            
            Button(
                onClick = {
                    if (barcode.isBlank()) { Toast.makeText(context, "Barcode is required", Toast.LENGTH_SHORT).show(); return@Button }
                    if (barcodeExists == null && productName.isBlank()) { Toast.makeText(context, "Product name is required", Toast.LENGTH_SHORT).show(); return@Button }
                    val qtyInt = quantity.toIntOrNull()
                    if (qtyInt == null || qtyInt < 0) { Toast.makeText(context, "Invalid quantity", Toast.LENGTH_SHORT).show(); return@Button }

                    scope.launch {
                        isSubmitting = true
                        try {
                            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                            val isOnline = NetworkMonitor.isNetworkAvailable(context)

                            if (!isOnline) {
                                val db = InventoryScannerApp.database
                                db.transactionDao().insert(PendingTransaction(barcode = barcode, name = if (barcodeExists == null) productName.takeIf { it.isNotBlank() } else null, type = transactionType, quantity = qtyInt, notes = notes.takeIf { it.isNotBlank() }))
                                Toast.makeText(context, "Saved offline — will sync when online", Toast.LENGTH_LONG).show()
                                onProductAdded()
                                isSubmitting = false
                                return@launch
                            }

                            val scanRequest = ScanRequest(
                                barcode = barcode,
                                name = if (barcodeExists == null) productName else null,
                                type = transactionType,
                                quantity = qtyInt,
                                notes = notes.takeIf { it.isNotBlank() },
                                deviceId = deviceId,
                                location = location.takeIf { it.isNotBlank() },
                                category = category.takeIf { it.isNotBlank() }
                            )

                            val response = ApiClient.apiService.sendScan(scanRequest)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Transaction recorded successfully!", Toast.LENGTH_SHORT).show()
                                onProductAdded()
                            } else {
                                Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            try {
                                val db = InventoryScannerApp.database
                                db.transactionDao().insert(PendingTransaction(barcode = barcode, name = if (barcodeExists == null) productName.takeIf { it.isNotBlank() } else null, type = transactionType, quantity = quantity.toIntOrNull() ?: 1, notes = notes.takeIf { it.isNotBlank() }))
                                Toast.makeText(context, "Network error — saved offline", Toast.LENGTH_LONG).show()
                                onProductAdded()
                            } catch (_: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Product")
                }
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
