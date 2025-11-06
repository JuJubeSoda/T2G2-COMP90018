package com.example.myapplication.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient - Factory for creating Retrofit API service instances.
 * 
 * Configuration:
 * - Base URL: https://mobile.kevinauhome.com (production server)
 * - Authentication: Automatic via AuthInterceptor
 * - Logging: BODY level (headers + request/response bodies)
 * - Timeouts: 60s connect, 120s read, 60s write
 * 
 * Features:
 * - Gson converter for JSON serialization/deserialization
 * - HTTP request/response logging for debugging
 * - JWT token injection via AuthInterceptor
 * - Extended timeouts for image uploads
 * 
 * Usage:
 * ApiService apiService = ApiClient.create(context);
 * Call<ApiResponse<List<PlantDto>>> call = apiService.getPlantsByUser();
 */
public class ApiClient {
    // Production server base URL
    //private static final String BASE_URL = "http://localhost:9999/";
    private static final String BASE_URL = "https://mobile.kevinauhome.com";

    /**
     * Creates a configured ApiService instance.
     * 
     * @param context Application context for AuthInterceptor to access SharedPreferences
     * @return Configured Retrofit ApiService implementation
     */
    public static ApiService create(Context context) {

        // Setup HTTP request/response logging for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Build OkHttpClient with interceptors and timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))  // Add JWT token to requests
                .addInterceptor(logging)                       // Log HTTP traffic
                .connectTimeout(60, TimeUnit.SECONDS)          // Connection timeout
                .readTimeout(120, TimeUnit.SECONDS)            // Read timeout (for large responses)
                .writeTimeout(60, TimeUnit.SECONDS)            // Write timeout (for uploads)
                .build();

        // Build Retrofit instance with Gson converter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // Gson parses JSON
                .build();

        return retrofit.create(ApiService.class);
    }
}
