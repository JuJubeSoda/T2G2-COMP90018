package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.PlantMapDto;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.util.LogUtil;

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
    
    // 搜索配置
    private static final int DEFAULT_SEARCH_RADIUS = 1000; // meters
    
    public MapDataManager(Context context) {
        this.context = context;
        this.apiService = ApiClient.create(context);
    }
    
    // 便捷重载移除：统一由上层协调层处理数据与显示
    
    // 便捷重载移除：统一由上层协调层处理数据与显示
    
    /**
     * 搜索附近的植物（带回调）
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius, MapDataCallback<List<PlantMapDto>> callback) {
        LogUtil.d(TAG, "Searching for nearby plants at: " + latitude + ", " + longitude + " radius: " + radius);
        
        Call<ApiResponse<List<PlantMapDto>>> call = apiService.getNearbyPlants(latitude, longitude, radius);
        call.enqueue(new Callback<ApiResponse<List<PlantMapDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlantMapDto>>> call, Response<ApiResponse<List<PlantMapDto>>> response) {
                handleApiResponse(response, "plants", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<PlantMapDto>>> call, Throwable t) {
                LogUtil.e(TAG, "Network call failed for plants", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    // Removed: searchNearbyGardens in favor of fetchAllGardens

    /**
     * 拉取全部花园列表（不做附近搜索），用于本地转换为GeoJSON整层渲染
     */
    public void fetchAllGardens(MapDataCallback<List<GardenDto>> callback) {
        LogUtil.d(TAG, "Fetching all gardens");
        Call<ApiResponse<List<GardenDto>>> call = apiService.getAllGardens();
        call.enqueue(new Callback<ApiResponse<List<GardenDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GardenDto>>> call, Response<ApiResponse<List<GardenDto>>> response) {
                handleApiResponse(response, "gardens", callback);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GardenDto>>> call, Throwable t) {
                LogUtil.e(TAG, "Network call failed for all gardens", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * 获取指定花园下的植物列表
     */
    public void getPlantsByGarden(long gardenId, MapDataCallback<List<PlantDto>> callback) {
        LogUtil.d(TAG, "Fetching plants by garden: " + gardenId);
        Call<ApiResponse<List<PlantDto>>> call = apiService.getPlantsByGarden(gardenId);
        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlantDto>>> call, Response<ApiResponse<List<PlantDto>>> response) {
                handleApiResponse(response, "plants by garden", callback);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlantDto>>> call, Throwable t) {
                LogUtil.e(TAG, "Network call failed for plants by garden", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * 统一的API响应处理方法
     */
    private <T> void handleApiResponse(Response<ApiResponse<T>> response, String dataType, MapDataCallback<T> callback) {
        LogUtil.d(TAG, "=== API Response Debug ===");
        LogUtil.d(TAG, "Response code: " + response.code());
        LogUtil.d(TAG, "Response message: " + response.message());
        LogUtil.d(TAG, "Response isSuccessful: " + response.isSuccessful());
        LogUtil.d(TAG, "Response body is null: " + (response.body() == null));
        
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> apiResponse = response.body();
            LogUtil.d(TAG, "ApiResponse code: " + apiResponse.getCode());
            LogUtil.d(TAG, "ApiResponse message: " + apiResponse.getMessage());
            LogUtil.d(TAG, "ApiResponse isSuccessful: " + apiResponse.isSuccessful());
            LogUtil.d(TAG, "ApiResponse data is null: " + (apiResponse.getData() == null));
            
            if (apiResponse.getData() != null) {
                LogUtil.d(TAG, "ApiResponse data type: " + apiResponse.getData().getClass().getSimpleName());
                LogUtil.d(TAG, "ApiResponse data size: " + (apiResponse.getData() instanceof List ? ((List<?>) apiResponse.getData()).size() : "N/A"));
                LogUtil.d(TAG, "ApiResponse data content: " + apiResponse.getData().toString());
            }
            
            // 修复：后端返回的JSON中没有success字段，只依赖code字段判断
            // 对于like/unlike操作，即使data为空或为字符串"liked"/"unliked"，code=200即视为成功
            if (apiResponse.getCode() == 200) {
                if (apiResponse.getData() != null) {
                    LogUtil.d(TAG, "Successfully received " + dataType + " data: " + apiResponse.getData());
                    callback.onSuccess(apiResponse.getData());
                } else {
                    LogUtil.d(TAG, "Successfully " + dataType + " (code=200, no data)");
                    callback.onSuccess(null);
                }
            } else {
                LogUtil.e(TAG, "API call failed for " + dataType + ": " + apiResponse.getMessage());
                callback.onError(apiResponse.getMessage());
            }
        } else {
            LogUtil.e(TAG, "HTTP request failed for " + dataType + ": " + response.code() + " " + response.message());
            if (response.errorBody() != null) {
                try {
                    String errorBody = response.errorBody().string();
                    LogUtil.e(TAG, "Error body: " + errorBody);
                } catch (Exception e) {
                    LogUtil.e(TAG, "Failed to read error body", e);
                }
            }
            callback.onError("HTTP error: " + response.code());
        }
        LogUtil.d(TAG, "=== End API Response Debug ===");
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
                LogUtil.e(TAG, "Like plant failed", t);
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
                LogUtil.e(TAG, "Unlike plant failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
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
        LogUtil.d(TAG, "Getting plant details for ID: " + plantId);
        
        Call<ApiResponse<PlantDto>> call = apiService.getPlantById(plantId);
        call.enqueue(new Callback<ApiResponse<PlantDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<PlantDto>> call, Response<ApiResponse<PlantDto>> response) {
                handleApiResponse(response, "plant details", callback);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<PlantDto>> call, Throwable t) {
                LogUtil.e(TAG, "Network call failed for plant details", t);
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
