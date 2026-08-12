package com.inventory.scanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventory.scanner.InventoryScannerApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val username = InventoryScannerApp.authManager.getUsername() ?: "User"
    val userRole = InventoryScannerApp.authManager.getRole() ?: "EMPLOYEE"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = userRole,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                    
                    when (userRole) {
                        "EMPLOYEE" -> {
                            PermissionItem(Icons.Default.QrCodeScanner, "Scan & process transactions", true)
                            PermissionItem(Icons.Default.Check, "View products", true)
                            PermissionItem(Icons.Default.Add, "Add new products", false)
                            PermissionItem(Icons.Default.Edit, "Edit products", false)
                            PermissionItem(Icons.Default.Delete, "Delete products", false)
                            PermissionItem(Icons.Default.Delete, "Delete transactions", false)
                            PermissionItem(Icons.Default.Category, "Manage categories", false)
                            PermissionItem(Icons.Default.People, "Manage users", false)
                        }
                        "MANAGER" -> {
                            PermissionItem(Icons.Default.QrCodeScanner, "Scan & process transactions", true)
                            PermissionItem(Icons.Default.Check, "View products", true)
                            PermissionItem(Icons.Default.Add, "Add products", true)
                            PermissionItem(Icons.Default.Edit, "Edit products", true)
                            PermissionItem(Icons.Default.Delete, "Delete products", true)
                            PermissionItem(Icons.Default.Delete, "Delete transactions", true)
                            PermissionItem(Icons.Default.Category, "Manage categories", true)
                            PermissionItem(Icons.Default.Warning, "Set min quantities", false)
                            PermissionItem(Icons.Default.People, "Manage users", false)
                        }
                        "ADMIN" -> {
                            PermissionItem(Icons.Default.QrCodeScanner, "Scan & process transactions", true)
                            PermissionItem(Icons.Default.Check, "View products", true)
                            PermissionItem(Icons.Default.Add, "Add products", true)
                            PermissionItem(Icons.Default.Edit, "Edit products", true)
                            PermissionItem(Icons.Default.Delete, "Delete products", true)
                            PermissionItem(Icons.Default.Delete, "Delete transactions", true)
                            PermissionItem(Icons.Default.Category, "Manage categories", true)
                            PermissionItem(Icons.Default.Warning, "Set min quantities", true)
                            PermissionItem(Icons.Default.People, "Manage users", true)
                            PermissionItem(Icons.Default.Settings, "System administration", true)
                        }
                    }
                }
            }
            
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "App Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    InfoRow("App Name", "Inventory Scanner")
                    InfoRow("Version", "1.0.0")
                    InfoRow("Backend", "Spring Boot 3.2.0")
                    InfoRow("Database", "H2 (Persistent)")
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    allowed: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (allowed) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (allowed) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Icon(
            if (allowed) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (allowed) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
