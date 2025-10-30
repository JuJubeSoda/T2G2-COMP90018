package com.example.myapplication.network;

import com.example.myapplication.auth.model.LoginRequest;
import com.example.myapplication.auth.model.RegisterRequest;
import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.ApiResponse;
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

    /**
     * Fetches a single plant by its ID.
     * Used for getting detailed plant information from map.
     * @param plantId The ID of the plant to fetch
     * @return Call containing ApiResponse with single PlantDto
     */
    @GET("/api/plants/{id}")
    Call<ApiResponse<PlantDto>> getPlantById(@retrofit2.http.Path("id") int plantId);

    @GET("api/ai_bot/ask")
    Call<BaseResponse> askQuestion(@Query("q") String question);

    // --- Plant AI Endpoints ---
    
    @POST("api/plant-ai/recommendations")
    Call<BaseResponse> getPlantRecommendations(@Query("location") String location, @Body java.util.Map<String, Object> sensorData);
    
    @POST("api/plant-ai/care-advice")
    Call<BaseResponse> getPlantCareAdvice(@Query("plantName") String plantName, @Body java.util.Map<String, Object> currentConditions);
    
    @GET("api/plant-ai/ask")
    Call<BaseResponse> askPlantQuestion(@Query("question") String question);
    
    @retrofit2.http.Multipart
    @POST("api/plant-ai/identify")
    Call<BaseResponse> identifyPlant(@retrofit2.http.Part okhttp3.MultipartBody.Part imageFile, @Query("location") String location);

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

    // --- Map Data Endpoints ---
    
    /**
     * Fetches nearby plants based on user's location.
     * The backend uses the authenticated user's location to find plants within a certain radius.
     */
    @GET("/api/plants/nearby")
    Call<ApiResponse<List<PlantDto>>> getNearbyPlants();

    /**
     * Fetches all gardens without nearby search filtering.
     */
    @GET("/api/garden/all")
    Call<ApiResponse<List<GardenDto>>> getAllGardens();
    
    /**
     * Likes a plant by its ID.
     * @param plantId The ID of the plant to like
     * @return Call containing ApiResponse with success message
     */
    @POST("/api/plants/like")
    Call<ApiResponse<String>> likePlant(@Query("plantId") int plantId);
    
    /**
     * Unlikes a plant by its ID.
     * @param plantId The ID of the plant to unlike
     * @return Call containing ApiResponse with success message
     */
    @POST("/api/plants/unlike")
    Call<ApiResponse<String>> unlikePlant(@Query("plantId") int plantId);


}
