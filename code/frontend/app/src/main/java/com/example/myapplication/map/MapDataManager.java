package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.model.Garden;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.PlantMapDto;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.map.MapDisplayManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 地图数据管理器 - 负责处理植物和花园的API调用和数据获取
 * 统一管理地图相关的网络请求和数据流
 */
public class MapDataManager {
    
    private static final String TAG = "MapDataManager";
    
    private final Context context;
    private final ApiService apiService;
    private final MapDisplayManager mapDisplayManager;
    
    // 搜索配置
    private static final int DEFAULT_SEARCH_RADIUS = 1000; // meters
    
    public MapDataManager(Context context, MapDisplayManager mapDisplayManager) {
        this.context = context;
        this.apiService = ApiClient.create(context);
        this.mapDisplayManager = mapDisplayManager;
    }
    
    // 便捷重载移除：统一由上层协调层处理数据与显示
    
    // 便捷重载移除：统一由上层协调层处理数据与显示
    
    /**
     * 搜索附近的植物（带回调）
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius, MapDataCallback<List<PlantDto>> callback) {
        Log.d(TAG, "Searching for nearby plants at: " + latitude + ", " + longitude + " radius: " + radius);
        
        Call<ApiResponse<List<PlantDto>>> call = apiService.getNearbyPlants(latitude, longitude, radius);
        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlantDto>>> call, Response<ApiResponse<List<PlantDto>>> response) {
                handleApiResponse(response, "plants", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<PlantDto>>> call, Throwable t) {
                Log.e(TAG, "Network call failed for plants", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * 搜索附近的花园（带回调）
     */
    public void searchNearbyGardens(double latitude, double longitude, int radius, MapDataCallback<List<Garden>> callback) {
        Log.d(TAG, "Searching for nearby gardens at: " + latitude + ", " + longitude + " radius: " + radius);
        
        Call<ApiResponse<List<Garden>>> call = apiService.getNearbyGardens(latitude, longitude, radius);
        call.enqueue(new Callback<ApiResponse<List<Garden>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Garden>>> call, Response<ApiResponse<List<Garden>>> response) {
                handleApiResponse(response, "gardens", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Garden>>> call, Throwable t) {
                Log.e(TAG, "Network call failed for gardens", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * 统一的API响应处理方法
     */
    private <T> void handleApiResponse(Response<ApiResponse<T>> response, String dataType, MapDataCallback<T> callback) {
        Log.d(TAG, "=== API Response Debug ===");
        Log.d(TAG, "Response code: " + response.code());
        Log.d(TAG, "Response message: " + response.message());
        Log.d(TAG, "Response isSuccessful: " + response.isSuccessful());
        Log.d(TAG, "Response body is null: " + (response.body() == null));
        
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> apiResponse = response.body();
            Log.d(TAG, "ApiResponse code: " + apiResponse.getCode());
            Log.d(TAG, "ApiResponse message: " + apiResponse.getMessage());
            Log.d(TAG, "ApiResponse isSuccessful: " + apiResponse.isSuccessful());
            Log.d(TAG, "ApiResponse data is null: " + (apiResponse.getData() == null));
            
            if (apiResponse.getData() != null) {
                Log.d(TAG, "ApiResponse data type: " + apiResponse.getData().getClass().getSimpleName());
                Log.d(TAG, "ApiResponse data size: " + (apiResponse.getData() instanceof List ? ((List<?>) apiResponse.getData()).size() : "N/A"));
                Log.d(TAG, "ApiResponse data content: " + apiResponse.getData().toString());
            }
            
            // 修复：后端返回的JSON中没有success字段，只依赖code字段判断
            if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                Log.d(TAG, "Successfully received " + dataType + " data");
                callback.onSuccess(apiResponse.getData());
            } else {
                Log.e(TAG, "API call failed for " + dataType + ": " + apiResponse.getMessage());
                callback.onError(apiResponse.getMessage());
            }
        } else {
            Log.e(TAG, "HTTP request failed for " + dataType + ": " + response.code() + " " + response.message());
            if (response.errorBody() != null) {
                try {
                    String errorBody = response.errorBody().string();
                    Log.e(TAG, "Error body: " + errorBody);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to read error body", e);
                }
            }
            callback.onError("HTTP error: " + response.code());
        }
        Log.d(TAG, "=== End API Response Debug ===");
    }
    
    /**
     * 点赞植物
     */
    public void likePlant(int plantId, MapDataCallback<String> callback) {
        Call<ApiResponse<String>> call = apiService.likePlant(plantId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                handleApiResponse(response, "like plant", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Like plant failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * 取消点赞植物
     */
    public void unlikePlant(int plantId, MapDataCallback<String> callback) {
        Call<ApiResponse<String>> call = apiService.unlikePlant(plantId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                handleApiResponse(response, "unlike plant", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Unlike plant failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 数据回调接口
     */
    public interface MapDataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
    
    /**
     * 根据植物ID获取完整的植物详情
     */
    public void getPlantById(int plantId, MapDataCallback<PlantDto> callback) {
        Log.d(TAG, "Getting plant details for ID: " + plantId);
        
        Call<ApiResponse<PlantDto>> call = apiService.getPlantById(plantId);
        call.enqueue(new Callback<ApiResponse<PlantDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<PlantDto>> call, Response<ApiResponse<PlantDto>> response) {
                handleApiResponse(response, "plant details", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<PlantDto>> call, Throwable t) {
                Log.e(TAG, "Network call failed for plant details", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * 获取默认搜索半径
     */
    public static int getDefaultSearchRadius() {
        return DEFAULT_SEARCH_RADIUS;
    }
}
