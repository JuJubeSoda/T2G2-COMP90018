package com.example.myapplication.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * - 每次发请求时，会自动从 SharedPreferences 里读取 token
 * - 如果 token 存在，就在请求头里加上 Authorization: <token>
 */
public class AuthInterceptor implements Interceptor {

    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 读取 token
        SharedPreferences sp = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = sp.getString("jwt_token", "");

        // 在请求头里加 Authorization
        Request.Builder builder = chain.request().newBuilder();
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", token);
        }

        return chain.proceed(builder.build());
    }
}
