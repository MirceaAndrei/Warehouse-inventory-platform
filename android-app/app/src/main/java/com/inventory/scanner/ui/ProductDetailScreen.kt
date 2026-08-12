package com.inventory.scanner.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventory.scanner.InventoryScannerApp
import com.inventory.scanner.data.Category
import com.inventory.scanner.data.Product
import com.inventory.scanner.data.UpdateProductRequest
import com.inventory.scanner.network.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    onProductUpdated: () -> Unit,
    onProductDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRole = InventoryScannerApp.authManager.getRole() ?: "EMPLOYEE"
    
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    
    var editName by remember { mutableStateOf(product.name) }
    var editBarcode by remember { mutableStateOf(product.barcode) }
    var editQuantity by remember { mutableStateOf(product.quantity.toString()) }
    var editMinQuantity by remember { mutableStateOf(product.minQuantity?.toString() ?: "") }
    var editDescription by remember { mutableStateOf(product.description ?: "") }
    var editLocation by remember { mutableStateOf(product.location ?: "") }
    var editCategory by remember { mutableStateOf(product.category ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val canEdit = userRole == "ADMIN" || userRole == "MANAGER"
    val canDelete = userRole == "ADMIN"
    val canEditMinQuantity = userRole == "ADMIN"
    val canCreateCategory = userRole == "MANAGER" || userRole == "ADMIN"

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
                                val resp = ApiClient.apiService.createCategory(Category(id = 0L, name = name))
                                if (resp.isSuccessful && resp.body() != null) {
                                    categories = categories + resp.body()!!
                                    editCategory = resp.body()!!.name
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
                title = {
                    Text(if (isEditMode) "Edit Product" else "Product Details")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (canEdit && !isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    }
                    if (canDelete && !isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isEditMode) {
                
                EditModeContent(
                    editName = editName,
                    onNameChange = { editName = it },
                    editBarcode = editBarcode,
                    onBarcodeChange = { editBarcode = it },
                    editQuantity = editQuantity,
                    onQuantityChange = { editQuantity = it },
                    editMinQuantity = editMinQuantity,
                    onMinQuantityChange = { editMinQuantity = it },
                    editDescription = editDescription,
                    onDescriptionChange = { editDescription = it },
                    editLocation = editLocation,
                    onLocationChange = { editLocation = it },
                    editCategory = editCategory,
                    onCategoryChange = { editCategory = it },
                    categories = categories,
                    categoryExpanded = categoryExpanded,
                    onCategoryExpandedChange = { categoryExpanded = it },
                    canCreateCategory = canCreateCategory,
                    onNewCategoryClick = { showNewCategoryDialog = true },
                    userRole = userRole,
                    canEditMinQuantity = canEditMinQuantity,
                    isSaving = isSaving,
                    onSave = {
                        scope.launch {
                            isSaving = true
                            try {
                                val request = UpdateProductRequest(
                                    name = editName.takeIf { it != product.name },
                                    barcode = editBarcode.takeIf { it.isNotBlank() && it != product.barcode },
                                    quantity = editQuantity.toIntOrNull()?.takeIf { it != product.quantity },
                                    minQuantity = if (canEditMinQuantity) {
                                        editMinQuantity.toIntOrNull()
                                    } else null,
                                    description = editDescription.takeIf { it != product.description },
                                    location = editLocation.takeIf { it != product.location },
                                    category = editCategory.takeIf { it.isNotBlank() && it != product.category }
                                )
                                
                                val response = ApiClient.apiService.updateProduct(product.id!!, request)
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "✅ Product updated successfully!", Toast.LENGTH_SHORT).show()
                                    isEditMode = false
                                    onProductUpdated()
                                } else {
                                    Toast.makeText(context, "❌ Error: ${response.message()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    onCancel = {
                        isEditMode = false
                        editName = product.name
                        editBarcode = product.barcode
                        editQuantity = product.quantity.toString()
                        editMinQuantity = product.minQuantity?.toString() ?: ""
                        editDescription = product.description ?: ""
                        editLocation = product.location ?: ""
                        editCategory = product.category ?: ""
                    }
                )
            } else {
                
                ViewModeContent(
                    product = product,
                    userRole = userRole
                )
                
                
                RoleInfoCard(userRole = userRole)
            }
        }
    }
    
    
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            productName = product.name,
            onConfirm = {
                scope.launch {
                    try {
                        val response = ApiClient.apiService.deleteProduct(product.id!!)
                        if (response.isSuccessful) {
                            Toast.makeText(context, "✅ Product deleted successfully!", Toast.LENGTH_SHORT).show()
                            onProductDeleted()
                        } else {
                            Toast.makeText(context, "❌ Error: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        showDeleteDialog = false
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun ViewModeContent(
    product: Product,
    userRole: String
) {
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Barcode: ${product.barcode}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
    
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Available Quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "In Stock",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${product.quantity}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    product.quantity == 0 -> MaterialTheme.colorScheme.error
                    product.quantity < 10 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }
    }
    
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Divider()
            
            product.category?.let {
                DetailRow("🏷️ Category", it)
            }
            
            product.location?.let {
                DetailRow("📍 Location", it)
            }
            
            product.description?.let {
                DetailRow("📝 Description", it)
            }
            
            
            if (userRole == "ADMIN" && product.minQuantity != null) {
                Divider()
                DetailRow("⚠️ Minimum Quantity", product.minQuantity.toString())
                
                if (product.quantity <= product.minQuantity) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Stock below minimum threshold!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            product.updatedAt?.let {
                Divider()
                DetailRow("🕒 Last Updated", it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeContent(
    editName: String,
    onNameChange: (String) -> Unit,
    editBarcode: String,
    onBarcodeChange: (String) -> Unit,
    editQuantity: String,
    onQuantityChange: (String) -> Unit,
    editMinQuantity: String,
    onMinQuantityChange: (String) -> Unit,
    editDescription: String,
    onDescriptionChange: (String) -> Unit,
    editLocation: String,
    onLocationChange: (String) -> Unit,
    editCategory: String,
    onCategoryChange: (String) -> Unit,
    categories: List<Category>,
    categoryExpanded: Boolean,
    onCategoryExpandedChange: (Boolean) -> Unit,
    canCreateCategory: Boolean,
    onNewCategoryClick: () -> Unit,
    userRole: String,
    canEditMinQuantity: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Edit, null)
            Text(
                text = "Edit Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modify the fields below",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    
    OutlinedTextField(
        value = editName,
        onValueChange = onNameChange,
        label = { Text("Product Name *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = editBarcode,
        onValueChange = onBarcodeChange,
        label = { Text("Barcode") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Edit, null) }
    )

    OutlinedTextField(
        value = editQuantity,
        onValueChange = onQuantityChange,
        label = { Text("Quantity *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    
    
    if (canEditMinQuantity) {
        OutlinedTextField(
            value = editMinQuantity,
            onValueChange = onMinQuantityChange,
            label = { Text("Minimum Quantity (optional)") },
            placeholder = { Text("Ex: 10") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text("Only ADMIN can set this field")
            }
        )
    } else if (userRole == "MANAGER") {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "Only ADMIN can modify minimum quantity",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    
    OutlinedTextField(
        value = editLocation,
        onValueChange = onLocationChange,
        label = { Text("Location (optional)") },
        placeholder = { Text("Ex: Shelf A1") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    ExposedDropdownMenuBox(
        expanded = categoryExpanded,
        onExpandedChange = { if (categories.isNotEmpty()) onCategoryExpandedChange(!categoryExpanded) }
    ) {
        OutlinedTextField(
            value = editCategory,
            onValueChange = { onCategoryChange(it) },
            label = { Text("Category") },
            readOnly = categories.isNotEmpty(),
            trailingIcon = { if (categories.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = { onCategoryExpandedChange(false) }
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    onClick = { onCategoryChange(cat.name); onCategoryExpandedChange(false) }
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
                    onClick = { onCategoryExpandedChange(false); onNewCategoryClick() }
                )
            }
        }
    }

    OutlinedTextField(
        value = editDescription,
        onValueChange = onDescriptionChange,
        label = { Text("Description (optional)") },
        placeholder = { Text("Product details...") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 4
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = !isSaving
        ) {
            Text("Cancel")
        }
        
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            enabled = !isSaving && editName.isNotBlank() && editQuantity.toIntOrNull() != null
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun RoleInfoCard(userRole: String) {
    val (title, description, color) = when (userRole) {
        "EMPLOYEE" -> Triple(
            "Product View",
            "As an EMPLOYEE, you can view products and add new products via Scan/Add. Contact a MANAGER to modify existing products.",
            MaterialTheme.colorScheme.secondaryContainer
        )
        "MANAGER" -> Triple(
            "MANAGER Permissions",
            "You can edit name, quantity, location, and description. You cannot delete products or modify minimum quantity.",
            MaterialTheme.colorScheme.tertiaryContainer
        )
        "ADMIN" -> Triple(
            "Full ADMIN Access",
            "You have full access: you can edit all fields including minimum quantity, and delete products.",
            MaterialTheme.colorScheme.primaryContainer
        )
        else -> Triple("Info", "Permission information", MaterialTheme.colorScheme.surfaceVariant)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                when (userRole) {
                    "ADMIN" -> Icons.Default.Settings
                    "MANAGER" -> Icons.Default.AccountCircle
                    else -> Icons.Default.Info
                },
                contentDescription = null
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    productName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text("Delete Confirmation")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Are you sure you want to delete this product?")
                Text(
                    text = productName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "⚠️ This action cannot be undone!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
