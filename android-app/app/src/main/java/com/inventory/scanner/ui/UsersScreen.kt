package com.inventory.scanner.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventory.scanner.network.ApiClient
import com.inventory.scanner.network.UpdateUserRequest
import com.inventory.scanner.network.UserDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var editingUser by remember { mutableStateOf<UserDto?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<UserDto?>(null) }

    fun reloadUsers() {
        scope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.getAllUsers()
                if (response.isSuccessful && response.body() != null) {
                    users = response.body()!!
                    errorMessage = null
                } else {
                    errorMessage = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { reloadUsers() }

    
    editingUser?.let { user ->
        UserEditDialog(
            user = user,
            onDismiss = { editingUser = null },
            onSaveRole = { newRole ->
                scope.launch {
                    try {
                        val req = UpdateUserRequest(username = user.username, email = user.email, role = newRole)
                        val resp = ApiClient.apiService.updateUser(user.id, req)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Role updated", Toast.LENGTH_SHORT).show()
                            editingUser = null
                            reloadUsers()
                        } else {
                            Toast.makeText(context, "Error: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onResetPassword = {
                scope.launch {
                    try {
                        val resp = ApiClient.apiService.resetUserPassword(user.id)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Password reset to: parola123@", Toast.LENGTH_LONG).show()
                            editingUser = null
                        } else {
                            Toast.makeText(context, "Error: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDelete = {
                editingUser = null
                showDeleteConfirm = user
            }
        )
    }

    
    showDeleteConfirm?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
            title = { Text("Delete User") },
            text = { Text("Delete \"${user.username}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val resp = ApiClient.apiService.deleteUser(user.id)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show()
                                    showDeleteConfirm = null
                                    reloadUsers()
                                } else {
                                    Toast.makeText(context, "Error: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                    showDeleteConfirm = null
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                                showDeleteConfirm = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("User Management", fontWeight = FontWeight.Bold)
                        if (users.isNotEmpty()) {
                            Text(
                                "${users.size} users registered",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }

            users.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No users found") }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    UserCard(user = user, onEdit = { editingUser = user })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserEditDialog(
    user: UserDto,
    onDismiss: () -> Unit,
    onSaveRole: (String) -> Unit,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit
) {
    val roles = listOf("EMPLOYEE", "MANAGER", "ADMIN")
    var selectedRole by remember { mutableStateOf(user.role) }
    var roleExpanded by remember { mutableStateOf(false) }

    val roleColor = when (selectedRole) {
        "ADMIN" -> Color(0xFFEF4444)
        "MANAGER" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                Text(user.username, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                user.email?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        label = { Text("Role") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = roleColor,
                            unfocusedBorderColor = roleColor.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role, fontWeight = if (role == selectedRole) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { selectedRole = role; roleExpanded = false }
                            )
                        }
                    }
                }

                
                OutlinedButton(
                    onClick = onResetPassword,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reset Password → parola123@")
                }

                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete User")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveRole(selectedRole) },
                enabled = selectedRole != user.role
            ) { Text("Save Role") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun UserCard(user: UserDto, onEdit: () -> Unit) {
    val (roleColor, roleLabel) = when (user.role) {
        "ADMIN"   -> Pair(Color(0xFFEF4444), "ADMIN")
        "MANAGER" -> Pair(Color(0xFFF59E0B), "MANAGER")
        else      -> Pair(Color(0xFF10B981), "EMPLOYEE")
    }
    val stripColor = if (user.enabled) roleColor else Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(stripColor))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(user.username, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    user.email?.takeIf { it.isNotBlank() }?.let {
                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                    }
                    if (!user.enabled) Text("Account disabled", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    if (user.mustChangePassword) Text("Password reset pending", fontSize = 11.sp, color = Color(0xFFF59E0B))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = roleColor.copy(alpha = 0.15f)) {
                            Text(roleLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = roleColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = if (user.enabled) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.12f)) {
                            Text(
                                if (user.enabled) "Active" else "Disabled",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = if (user.enabled) Color(0xFF10B981) else Color.Gray,
                                fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit user", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
