package com.example.myapplication.network;

import com.example.myapplication.auth.model.LoginRequest;
import com.example.myapplication.auth.model.RegisterRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Defines the REST API endpoints for the application.
 * This interface is used by Retrofit to generate the network client.
 */
public interface ApiService {

    // --- User Authentication Endpoints ---

    @POST("user/reg")
    Call<BaseResponse> register(@Body RegisterRequest req);

    @POST("user/login")
    Call<BaseResponse> login(@Body LoginRequest req);

    @GET("user/info")
    Call<BaseResponse> getUserInfo();

    // --- Plant Data Endpoints ---

    /**
     * Fetches a list of plants for the current user.
     * The response is wrapped in an ApiResponse containing a list of PlantDto objects.
     */
    @GET("/api/plants/by-user")
    Call<ApiResponse<List<PlantDto>>> getPlantsByUser();

    @GET("api/ai_bot/ask")
    Call<BaseResponse> askQuestion(@Query("q") String question);

    @POST("/api/plants/add")
    Call<ApiResponse> addPlant(@Body PlantRequest plantRequest);

    @GET("/api/wiki/all")
    Call<ApiResponse<List<PlantDto>>> getAllPlants();
    
    /**
     * Fetches all wiki plants from the database.
     * This endpoint corresponds to the wiki-controller/getAllWikis endpoint.
     * Returns PlantWikiDto objects with richer plant information.
     */
    @GET("/api/wiki/all")
    Call<ApiResponse<List<PlantWikiDto>>> getAllWikis();

}
