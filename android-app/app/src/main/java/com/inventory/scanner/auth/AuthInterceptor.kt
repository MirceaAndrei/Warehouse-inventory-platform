package com.inventory.scanner.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        
        val token = authManager.getToken()
        if (token == null) {
            return chain.proceed(originalRequest)
        }
        
        
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        val response = chain.proceed(authenticatedRequest)
        
        
        if (response.code == 401) {
            
            
            authManager.logout()
        }
        
        return response
    }
}
