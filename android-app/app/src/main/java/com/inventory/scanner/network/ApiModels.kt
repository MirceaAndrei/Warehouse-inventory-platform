package com.inventory.scanner.network

data class ScanRequest(
    val barcode: String,
    val name: String? = null, 
    val type: String,
    val quantity: Int,
    val notes: String?,
    val deviceId: String,
    val location: String? = null,
    val category: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String?
)

data class UserDto(
    val id: Long = 0,
    val username: String = "",
    val email: String? = null,
    val role: String = "EMPLOYEE",
    val enabled: Boolean = true,
    val mustChangePassword: Boolean = false,
    val createdAt: String? = null,
    val lastLogin: String? = null
)

data class UpdateUserRequest(
    val username: String,
    val email: String?,
    val role: String,
    val password: String? = null
)
