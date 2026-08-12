package com.inventory.scanner.network

import com.inventory.scanner.data.*
import retrofit2.Response
import retrofit2.http.*

interface InventoryApiService {
    
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("/api/transactions/scan")
    suspend fun sendScan(@Body request: ScanRequest): Response<ApiResponse>
    
    @POST("/api/transactions")
    suspend fun processTransaction(@Body request: Map<String, Any>): Response<ApiResponse>
    
    
    
    

    @GET("/api/items/barcode/{barcode}")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<Product>
    
    

    @GET("/api/items")
    suspend fun getAllProducts(): Response<List<Product>>
    
    

    @GET("/api/items/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<Product>
    
    

    @POST("/api/items")
    suspend fun addProduct(@Body request: AddProductRequest): Response<ProductResponse>
    
    

    @PUT("/api/items/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: UpdateProductRequest
    ): Response<Product>
    
    

    @DELETE("/api/items/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<ApiResponse>
    
    
    
    

    @GET("/api/categories")
    suspend fun getAllCategories(): Response<List<Category>>

    

    @POST("/api/categories")
    suspend fun createCategory(@Body category: Category): Response<Category>

    

    @GET("/api/users")
    suspend fun getAllUsers(): Response<List<UserDto>>

    @PUT("/api/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body request: UpdateUserRequest): Response<UserDto>

    @DELETE("/api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<ApiResponse>

    @POST("/api/users/{id}/reset-password")
    suspend fun resetUserPassword(@Path("id") id: Long): Response<ApiResponse>
}
