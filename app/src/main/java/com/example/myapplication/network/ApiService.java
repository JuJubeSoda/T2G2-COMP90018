package com.example.myapplication.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("user/reg")
    Call<BaseResponse> register(@Body RegisterRequest req);

    @POST("user/login")
    Call<BaseResponse> login(@Body LoginRequest req);

    @GET("user/info")
    Call<BaseResponse> getUserInfo();
}
