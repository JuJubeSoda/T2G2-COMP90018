package com.example.myapplication.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AuthInterceptor - OkHttp interceptor for adding authentication tokens to requests
 * 
 * Purpose:
 * - Automatically retrieves token from SharedPreferences on each request
 * - Adds Authorization header if token exists
 * - Ensures authenticated API calls without manual header management
 */
public class AuthInterceptor implements Interceptor {

    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Retrieve token from SharedPreferences
        SharedPreferences sp = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = sp.getString("jwt_token", "");

        // Add Authorization header to request
        Request.Builder builder = chain.request().newBuilder();
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", token);
        }

        return chain.proceed(builder.build());
    }
}
