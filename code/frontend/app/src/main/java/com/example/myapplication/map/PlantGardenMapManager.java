package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.model.Garden;
import com.example.myapplication.model.Plant;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * 植物花园地图管理器 - 统一管理植物和花园的地图功能
 * 整合了位置管理、数据显示、数据获取等所有地图相关功能
 */
public class PlantGardenMapManager {
    
    private static final String TAG = "PlantGardenMapManager";
    
    private final Context context;
    private final GoogleMap googleMap;
    
    // 子管理器
    private final MapLocationManager locationManager;
    private final MapDisplayManager displayManager;
    private final MapDataManager dataManager;
    
    // 当前状态
    private boolean isShowingPlants = true; // true = plants, false = gardens
    
    public PlantGardenMapManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        
        // 初始化子管理器
        this.locationManager = new MapLocationManager(context, googleMap);
        this.displayManager = new MapDisplayManager(context, googleMap);
        this.dataManager = new MapDataManager(context, displayManager);
        
        Log.d(TAG, "PlantGardenMapManager initialized");
    }
    
    /**
     * 初始化地图设置
     */
    public void initializeMap() {
        locationManager.requestLocationPermission();
        locationManager.updateLocationUI();
        
        // 设置点击监听器
        setupClickListeners();
        
        // 获取设备位置
        locationManager.getDeviceLocation(new MapLocationManager.OnLocationResultCallback() {
            @Override
            public void onLocationSuccess(android.location.Location location) {
                Log.d(TAG, "Device location obtained successfully");
            }
            
            @Override
            public void onLocationError(String error) {
                Log.e(TAG, "Failed to get device location: " + error);
            }
        });
    }
    
    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        displayManager.setOnPlantClickListener(new MapDisplayManager.OnPlantMapClickListener() {
            @Override
            public void onPlantClick(Plant plant) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantClick(plant);
                }
            }
        });
        
        displayManager.setOnGardenClickListener(new MapDisplayManager.OnGardenMapClickListener() {
            @Override
            public void onGardenClick(Garden garden) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onGardenClick(garden);
                }
            }
        });
    }
    
    /**
     * 搜索附近数据（根据当前模式搜索植物或花园）
     */
    public void searchNearbyData() {
        if (!locationManager.hasLocationPermission() || locationManager.getLastKnownLocation() == null) {
            if (onPlantGardenMapInteractionListener != null) {
                onPlantGardenMapInteractionListener.onSearchError("Location permission required to search nearby data");
            }
            return;
        }
        
        android.location.Location location = locationManager.getLastKnownLocation();
        int radius = MapDataManager.getDefaultSearchRadius();
        
        if (isShowingPlants) {
            searchNearbyPlants(location.getLatitude(), location.getLongitude(), radius);
        } else {
            searchNearbyGardens(location.getLatitude(), location.getLongitude(), radius);
        }
    }
    
    /**
     * 搜索附近植物
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius) {
        Log.d(TAG, "Searching for nearby plants");
        
        dataManager.searchNearbyPlants(latitude, longitude, radius, new MapDataManager.MapDataCallback<List<Plant>>() {
            @Override
            public void onSuccess(List<Plant> plants) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantsFound(plants);
                }
            }
            
            @Override
            public void onError(String message) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onSearchError(message);
                }
            }
        });
    }
    
    /**
     * 搜索附近花园
     */
    public void searchNearbyGardens(double latitude, double longitude, int radius) {
        Log.d(TAG, "Searching for nearby gardens");
        
        dataManager.searchNearbyGardens(latitude, longitude, radius, new MapDataManager.MapDataCallback<List<Garden>>() {
            @Override
            public void onSuccess(List<Garden> gardens) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onGardensFound(gardens);
                }
            }
            
            @Override
            public void onError(String message) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onSearchError(message);
                }
            }
        });
    }
    
    /**
     * 切换数据显示模式
     */
    public void toggleDataType() {
        isShowingPlants = !isShowingPlants;
        
        // 清除当前显示
        displayManager.clearCurrentDisplay();
        
        String message = isShowingPlants ? "Switched to Plants view" : "Switched to Gardens view";
        Log.d(TAG, message);
        
        if (onPlantGardenMapInteractionListener != null) {
            onPlantGardenMapInteractionListener.onDataTypeChanged(isShowingPlants);
        }
    }
    
    /**
     * 点赞植物
     */
    public void likePlant(Long plantId) {
        dataManager.likePlant(plantId, new MapDataManager.MapDataCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantLiked(true);
                }
            }
            
            @Override
            public void onError(String message) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantLikeError(message);
                }
            }
        });
    }
    
    /**
     * 取消点赞植物
     */
    public void unlikePlant(Long plantId) {
        dataManager.unlikePlant(plantId, new MapDataManager.MapDataCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantLiked(false);
                }
            }
            
            @Override
            public void onError(String message) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantLikeError(message);
                }
            }
        });
    }
    
    /**
     * 处理权限请求结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    /**
     * 清除当前显示
     */
    public void clearCurrentDisplay() {
        displayManager.clearCurrentDisplay();
    }
    
    /**
     * 销毁管理器
     */
    public void destroy() {
        displayManager.destroy();
        Log.d(TAG, "PlantGardenMapManager destroyed");
    }
    
    // Getter方法
    public boolean isShowingPlants() {
        return isShowingPlants;
    }
    
    public boolean hasLocationPermission() {
        return locationManager.hasLocationPermission();
    }
    
    public android.location.Location getLastKnownLocation() {
        return locationManager.getLastKnownLocation();
    }
    
    // 交互监听器
    private OnPlantGardenMapInteractionListener onPlantGardenMapInteractionListener;
    
    public void setOnPlantGardenMapInteractionListener(OnPlantGardenMapInteractionListener listener) {
        this.onPlantGardenMapInteractionListener = listener;
    }
    
    /**
     * 植物花园地图交互监听器接口
     */
    public interface OnPlantGardenMapInteractionListener {
        void onPlantClick(Plant plant);
        void onGardenClick(Garden garden);
        void onPlantsFound(java.util.List<Plant> plants);
        void onGardensFound(java.util.List<Garden> gardens);
        void onSearchError(String message);
        void onDataTypeChanged(boolean isShowingPlants);
        void onPlantLiked(boolean liked);
        void onPlantLikeError(String message);
    }
}
