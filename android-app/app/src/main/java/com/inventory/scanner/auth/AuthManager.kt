package com.inventory.scanner.auth

import android.content.Context
import android.content.SharedPreferences
import com.inventory.scanner.data.LoginResponse

class AuthManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "inventory_auth",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_MUST_CHANGE_PASSWORD = "must_change_password"
    }
    
    

    fun saveLoginData(loginResponse: LoginResponse) {
        prefs.edit().apply {
            putString(KEY_TOKEN, loginResponse.token)
            putString(KEY_USERNAME, loginResponse.username)
            putString(KEY_EMAIL, loginResponse.email)
            putString(KEY_ROLE, loginResponse.role)
            putBoolean(KEY_MUST_CHANGE_PASSWORD, loginResponse.mustChangePassword)
            apply()
        }
    }
    
    

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    
    

    fun isLoggedIn(): Boolean = getToken() != null
    
    

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    
    

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    
    

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    
    

    fun mustChangePassword(): Boolean = prefs.getBoolean(KEY_MUST_CHANGE_PASSWORD, false)
    
    

    fun clearMustChangePassword() {
        prefs.edit().putBoolean(KEY_MUST_CHANGE_PASSWORD, false).apply()
    }
    
    

    fun logout() {
        prefs.edit().clear().apply()
    }
    
    

    fun hasRole(role: String): Boolean = getRole() == role
    
    

    fun isAdmin(): Boolean = hasRole("ADMIN")
    
    

    fun canManageItems(): Boolean {
        val role = getRole()
        return role == "ADMIN" || role == "MANAGER"
    }
}
