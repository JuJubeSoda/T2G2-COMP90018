package com.example.myapplication.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    //  调试期用明文 HTTP；Manifest 里需要 usesCleartextTraffic="true" + networkSecurityConfig
    private static final String BASE_URL = "http://10.0.2.2:9999/";

    public static ApiService create(Context context) {
        // 调试阶段查看请求/响应，发布时可移除
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 在 OkHttpClient 中加入 AuthInterceptor，自动带上 token
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context)) // 自动添加 Authorization 头
                .addInterceptor(logging)                     // 调试日志拦截器
                .connectTimeout(15, TimeUnit.SECONDS)        // 连接超时
                .readTimeout(15, TimeUnit.SECONDS)           // 读取超时
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)                           // 后端基础地址
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // Gson 解析 JSON
                .build();

        return retrofit.create(ApiService.class);
    }
}
