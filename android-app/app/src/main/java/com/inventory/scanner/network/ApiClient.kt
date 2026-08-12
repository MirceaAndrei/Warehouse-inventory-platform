package com.inventory.scanner.network

import com.inventory.scanner.auth.AuthInterceptor
import com.inventory.scanner.auth.AuthManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    
    

    
    
    
    
    private const val BASE_URL = "http://192.168.100.101:8082/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private var authManager: AuthManager? = null
    
    

    fun initialize(authManager: AuthManager) {
        this.authManager = authManager
    }
    
    private val okHttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
            
            
            authManager?.let { manager ->
                builder.addInterceptor(AuthInterceptor(manager))
            }
            
            return builder.build()
        }
    
    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    
    val apiService: InventoryApiService
        get() = retrofit.create(InventoryApiService::class.java)
}
