package com.inventory.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventory.scanner.InventoryScannerApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onProductListClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUsersClick: () -> Unit = {},
    onLogout: () -> Unit,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val userRole = InventoryScannerApp.authManager.getRole() ?: "EMPLOYEE"
    val username = InventoryScannerApp.authManager.getUsername() ?: "User"
    val canAdd = userRole == "MANAGER" || userRole == "ADMIN"
    val isAdmin = userRole == "ADMIN"
    var showQuickGuide by remember { mutableStateOf(false) }
    var showPermissions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Inventory Scanner",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            
            WelcomeRow(username = username, userRole = userRole)

            
            ScanActionCard(onClick = onScanClick)

            
            if (canAdd) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.List,
                        label = "Products",
                        gradient = Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488))),
                        onClick = onProductListClick
                    )
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        label = "Add New",
                        gradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                        onClick = onAddProductClick
                    )
                }
            } else {
                
                ActionTile(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.List,
                    label = "Products",
                    gradient = Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488))),
                    onClick = onProductListClick
                )
            }

            
            if (isAdmin) {
                ActionTile(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.People,
                    label = "Manage Users",
                    gradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))),
                    onClick = onUsersClick
                )
            }

            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoToggleButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Info,
                    label = "Quick Guide",
                    expanded = showQuickGuide,
                    onClick = { showQuickGuide = !showQuickGuide; if (showQuickGuide) showPermissions = false }
                )
                InfoToggleButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Lock,
                    label = "Permissions",
                    expanded = showPermissions,
                    onClick = { showPermissions = !showPermissions; if (showPermissions) showQuickGuide = false }
                )
            }

            AnimatedVisibility(visible = showQuickGuide) {
                QuickGuideCard()
            }

            AnimatedVisibility(visible = showPermissions) {
                PermissionsCard(userRole = userRole)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WelcomeRow(username: String, userRole: String) {
    val (roleColor, roleLabel) = when (userRole) {
        "ADMIN"   -> Pair(Color(0xFFEF4444), "ADMIN")
        "MANAGER" -> Pair(Color(0xFFF59E0B), "MANAGER")
        else      -> Pair(Color(0xFF10B981), "EMPLOYEE")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Hi, $username",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ready to scan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = roleColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = roleLabel,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                color = roleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun ScanActionCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SCAN",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Point camera at a barcode",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun InfoToggleButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = if (expanded) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (expanded) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (expanded) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Quick Guide", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            GuideItem(Icons.Default.QrCodeScanner, "Scan a barcode to find or add a product")
            GuideItem(Icons.Default.Add,           "Use IN to add stock to inventory")
            GuideItem(Icons.Default.Remove,        "Use OUT to remove stock from inventory")
            GuideItem(Icons.Default.Refresh,       "ADJUST sets the exact quantity directly")
        }
    }
}

@Composable
private fun GuideItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PermissionsCard(userRole: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Role Permissions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            PermissionRow("EMPLOYEE", Color(0xFF10B981), "Scan barcodes · View products · Process IN/OUT/ADJUST", userRole == "EMPLOYEE")
            PermissionRow("MANAGER",  Color(0xFFF59E0B), "Add & edit products · Delete transactions · Manage categories", userRole == "MANAGER")
            PermissionRow("ADMIN",    Color(0xFFEF4444), "Full access · Manage users · Delete products & transactions", userRole == "ADMIN")
        }
    }
}

@Composable
private fun PermissionRow(role: String, color: Color, description: String, isCurrentRole: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
            Text(
                text = role,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Column {
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isCurrentRole) {
                Text("← your role", fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
