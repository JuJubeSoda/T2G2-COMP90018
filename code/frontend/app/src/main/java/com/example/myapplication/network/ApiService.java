package com.example.myapplication.network;

import com.example.myapplication.auth.model.LoginRequest;
import com.example.myapplication.auth.model.RegisterRequest;
import com.example.myapplication.model.Garden;
import com.example.myapplication.model.Plant;
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
     * Fetches nearby plants within a specified radius of the given coordinates.
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate  
     * @param radius The search radius in meters
     * @return Call containing ApiResponse with list of nearby plants
     */
    @GET("api/map/plants/nearby")
    Call<ApiResponse<List<Plant>>> getNearbyPlants(@Query("latitude") double latitude, 
                                                   @Query("longitude") double longitude, 
                                                   @Query("radius") int radius);
    
    /**
     * Fetches nearby gardens within a specified radius of the given coordinates.
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param radius The search radius in meters  
     * @return Call containing ApiResponse with list of nearby gardens
     */
    @GET("api/map/gardens/nearby")
    Call<ApiResponse<List<Garden>>> getNearbyGardens(@Query("latitude") double latitude,
                                                     @Query("longitude") double longitude,
                                                     @Query("radius") int radius);
    
    /**
     * Likes a plant by its ID.
     * @param plantId The ID of the plant to like
     * @return Call containing ApiResponse with success message
     */
    @POST("api/plants/like")
    Call<ApiResponse<String>> likePlant(@Query("plantId") Long plantId);
    
    /**
     * Unlikes a plant by its ID.
     * @param plantId The ID of the plant to unlike
     * @return Call containing ApiResponse with success message
     */
    @POST("api/plants/unlike")
    Call<ApiResponse<String>> unlikePlant(@Query("plantId") Long plantId);

}
