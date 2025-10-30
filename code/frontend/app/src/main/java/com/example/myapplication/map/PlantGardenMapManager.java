package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.model.Garden;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.PlantMapDto;
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
        this.dataManager = new MapDataManager(context);
        
        // 设置智能半径变化监听器
        setupSmartRadiusListener();
        
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
            public void onPlantClick(PlantMapDto plant) {
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
        
        // 设置地图标记点击监听器（用于植物标记）
        googleMap.setOnMarkerClickListener(marker -> {
            // 尝试处理植物标记点击
            if (displayManager.handlePlantMarkerClick(marker)) {
                return true; // 植物标记点击已处理
            }
            // 如果不是植物标记，返回false让其他监听器处理
            return false;
        });
    }
    
    /**
     * 搜索附近数据（根据当前模式搜索植物或花园）
     */
    public void searchNearbyData() {
        Log.d(TAG, "=== Search Nearby Data Debug ===");
        Log.d(TAG, "Location permission granted: " + locationManager.hasLocationPermission());
        Log.d(TAG, "Last known location: " + (locationManager.getLastKnownLocation() != null ? "available" : "null"));
        Log.d(TAG, "Current mode: " + (isShowingPlants ? "Plants" : "Gardens"));
        
        if (!locationManager.hasLocationPermission() || locationManager.getLastKnownLocation() == null) {
            Log.e(TAG, "Location permission or location not available");
            if (onPlantGardenMapInteractionListener != null) {
                onPlantGardenMapInteractionListener.onSearchError("Location permission required to search nearby data");
            }
            return;
        }
        
        android.location.Location location = locationManager.getLastKnownLocation();
        // 使用智能半径替代固定半径
        int radius = locationManager.getSmartSearchRadius();
        
        Log.d(TAG, "Search parameters:");
        Log.d(TAG, "  - Latitude: " + location.getLatitude());
        Log.d(TAG, "  - Longitude: " + location.getLongitude());
        Log.d(TAG, "  - Radius: " + radius + " meters");
        Log.d(TAG, "  - Mode: " + (isShowingPlants ? "Plants" : "Gardens"));
        
        if (isShowingPlants) {
            searchNearbyPlants(location.getLatitude(), location.getLongitude(), radius);
        } else {
            searchNearbyGardens(location.getLatitude(), location.getLongitude(), radius);
        }
        Log.d(TAG, "=== End Search Nearby Data Debug ===");
    }
    
    /**
     * 搜索附近植物
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius) {
        Log.d(TAG, "Searching for nearby plants with radius: " + radius + " meters");
        
        dataManager.searchNearbyPlants(latitude, longitude, radius, new MapDataManager.MapDataCallback<List<PlantDto>>() {
            @Override
            public void onSuccess(List<PlantDto> plants) {
                Log.d(TAG, "=== Search Plants Success Debug ===");
                Log.d(TAG, "Plants found: " + (plants == null ? "null" : plants.size()));
                if (plants != null && !plants.isEmpty()) {
                    Log.d(TAG, "First plant details:");
                    PlantDto firstPlant = plants.get(0);
                    Log.d(TAG, "  - Name: " + firstPlant.getName());
                    Log.d(TAG, "  - Coordinates: (" + firstPlant.getLatitude() + ", " + firstPlant.getLongitude() + ")");
                    Log.d(TAG, "  - PlantId: " + firstPlant.getPlantId());
                }
                Log.d(TAG, "=== End Search Plants Success Debug ===");
                
                // 先转换数据
                java.util.ArrayList<PlantMapDto> mapDtos = new java.util.ArrayList<>();
                for (PlantDto p : plants) {
                    mapDtos.add(PlantMapDto.fromPlantDto(p));
                }
                
                // 统一在协调层进行渲染
                Log.d(TAG, "Displaying plants on map...");
                displayManager.displayPlantsOnMap(mapDtos);
                Log.d(TAG, "Plants displayed on map successfully");
                
                // 通知外部监听器
                if (onPlantGardenMapInteractionListener != null) {
                    Log.d(TAG, "Listener is not null, calling onPlantsFound (map dtos)");
                    onPlantGardenMapInteractionListener.onPlantsFound(mapDtos);
                    Log.d(TAG, "onPlantsFound called successfully");
                } else {
                    Log.e(TAG, "onPlantGardenMapInteractionListener is null!");
                }
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "=== Search Plants Error Debug ===");
                Log.e(TAG, "Error message: " + message);
                Log.e(TAG, "=== End Search Plants Error Debug ===");
                
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
        Log.d(TAG, "Searching for nearby gardens with radius: " + radius + " meters");
        
        dataManager.searchNearbyGardens(latitude, longitude, radius, new MapDataManager.MapDataCallback<List<Garden>>() {
            @Override
            public void onSuccess(List<Garden> gardens) {
                Log.d(TAG, "=== Search Gardens Success Debug ===");
                Log.d(TAG, "Gardens found: " + (gardens == null ? "null" : gardens.size()));
                
                // 统一在协调层进行渲染
                Log.d(TAG, "Displaying gardens on map...");
                displayManager.displayGardensOnMap(gardens);
                Log.d(TAG, "Gardens displayed on map successfully");
                
                // 通知外部监听器
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onGardensFound(gardens);
                }
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Search Gardens Error: " + message);
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
     * 增量更新植物显示（添加新植物）
     */
    public void addNewPlants(List<PlantMapDto> newPlants) {
        if (isShowingPlants) {
            displayManager.addNewPlants(newPlants);
        }
    }
    
    /**
     * 移除植物显示
     */
    public void removePlants(List<PlantMapDto> plantsToRemove) {
        if (isShowingPlants) {
            displayManager.removePlants(plantsToRemove);
        }
    }
    
    /**
     * 刷新植物显示（全量更新）
     */
    public void refreshPlants(List<PlantMapDto> plants) {
        if (isShowingPlants) {
            displayManager.displayPlantsOnMap(plants);
        }
    }
    
    /**
     * 点赞植物
     */
    public void likePlant(int plantId) {
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
    public void unlikePlant(int plantId) {
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
    
    /**
     * 获取位置管理器
     */
    public MapLocationManager getLocationManager() {
        return locationManager;
    }
    
    // 交互监听器
    private OnPlantGardenMapInteractionListener onPlantGardenMapInteractionListener;
    
    public void setOnPlantGardenMapInteractionListener(OnPlantGardenMapInteractionListener listener) {
        this.onPlantGardenMapInteractionListener = listener;
    }
    
    /**
     * 设置智能半径变化监听器
     */
    private void setupSmartRadiusListener() {
        locationManager.setOnMapRadiusChangeListener(new MapLocationManager.OnMapRadiusChangeListener() {
            @Override
            public void onMapRadiusChanged(int newRadius) {
                Log.d(TAG, "Map radius changed to: " + newRadius + " meters");
                
                // 如果用户有位置权限且已获取位置，自动重新搜索
                if (locationManager.hasLocationPermission() && locationManager.getLastKnownLocation() != null) {
                    Log.d(TAG, "Auto-refreshing data with new smart radius");
                    searchNearbyData();
                }
            }
        });
    }
    
    /**
     * 植物花园地图交互监听器接口
     */
    public interface OnPlantGardenMapInteractionListener {
        void onPlantClick(PlantMapDto plant);
        void onGardenClick(Garden garden);
        void onPlantsFound(java.util.List<PlantMapDto> plants);
        void onGardensFound(java.util.List<Garden> gardens);
        void onSearchError(String message);
        void onDataTypeChanged(boolean isShowingPlants);
        void onPlantLiked(boolean liked);
        void onPlantLikeError(String message);
    }
}
