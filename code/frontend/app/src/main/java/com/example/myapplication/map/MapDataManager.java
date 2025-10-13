package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.model.Garden;
import com.example.myapplication.model.Plant;
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
    
    /**
     * 搜索附近的植物
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius) {
        searchNearbyPlants(latitude, longitude, radius, new MapDataCallback<List<Plant>>() {
            @Override
            public void onSuccess(List<Plant> plants) {
                if (plants.isEmpty()) {
                    showToast("No plants found nearby");
                    return;
                }
                mapDisplayManager.displayPlantsOnMap(plants);
            }
            
            @Override
            public void onError(String message) {
                showToast("Failed to load nearby plants: " + message);
            }
        });
    }
    
    /**
     * 搜索附近的花园
     */
    public void searchNearbyGardens(double latitude, double longitude, int radius) {
        searchNearbyGardens(latitude, longitude, radius, new MapDataCallback<List<Garden>>() {
            @Override
            public void onSuccess(List<Garden> gardens) {
                if (gardens.isEmpty()) {
                    showToast("No gardens found nearby");
                    return;
                }
                mapDisplayManager.displayGardensOnMap(gardens);
            }
            
            @Override
            public void onError(String message) {
                showToast("Failed to load nearby gardens: " + message);
            }
        });
    }
    
    /**
     * 搜索附近的植物（带回调）
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius, MapDataCallback<List<Plant>> callback) {
        Log.d(TAG, "Searching for nearby plants at: " + latitude + ", " + longitude + " radius: " + radius);
        
        Call<ApiResponse<List<Plant>>> call = apiService.getNearbyPlants(latitude, longitude, radius);
        call.enqueue(new Callback<ApiResponse<List<Plant>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Plant>>> call, Response<ApiResponse<List<Plant>>> response) {
                handleApiResponse(response, "plants", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Plant>>> call, Throwable t) {
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
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> apiResponse = response.body();
            
            if (apiResponse.isSuccessful() && apiResponse.getData() != null) {
                Log.d(TAG, "Successfully received " + dataType + " data");
                callback.onSuccess(apiResponse.getData());
            } else {
                Log.e(TAG, "API call failed for " + dataType + ": " + apiResponse.getMessage());
                callback.onError(apiResponse.getMessage());
            }
        } else {
            Log.e(TAG, "HTTP request failed for " + dataType + ": " + response.code() + " " + response.message());
            callback.onError("HTTP error: " + response.code());
        }
    }
    
    /**
     * 点赞植物
     */
    public void likePlant(Long plantId, MapDataCallback<String> callback) {
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
    public void unlikePlant(Long plantId, MapDataCallback<String> callback) {
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
     * 获取默认搜索半径
     */
    public static int getDefaultSearchRadius() {
        return DEFAULT_SEARCH_RADIUS;
    }
}
