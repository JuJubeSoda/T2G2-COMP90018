package com.example.myapplication.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * - On every request, automatically read token from SharedPreferences
 * - If token exists, add Authorization header with the token
 */
public class AuthInterceptor implements Interceptor {

    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Read token
        SharedPreferences sp = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = sp.getString("jwt_token", "");

        // Add Authorization header
        Request.Builder builder = chain.request().newBuilder();
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", token);
        }

        return chain.proceed(builder.build());
    }
}
