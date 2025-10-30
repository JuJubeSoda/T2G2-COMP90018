package com.example.myapplication.map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.network.GardenDto;
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
    
    // 防抖和节流控制
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable = null;
    private long lastSearchTime = 0;
    private static final long DEBOUNCE_DELAY_MS = 800; // 防抖延迟800ms
    private static final long THROTTLE_INTERVAL_MS = 2000; // 节流最小间隔2秒
    
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
            public void onGardenClick(GardenDto garden) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onGardenClick(garden);
                }
            }
        });
        
        // 由ClusterManager处理Marker点击；由协调层组合CameraIdle事件
        if (displayManager.getPlantClusterManager() != null) {
            com.google.maps.android.clustering.ClusterManager<PlantClusterItem> cm = displayManager.getPlantClusterManager();
            googleMap.setOnMarkerClickListener(cm);
            googleMap.setOnCameraIdleListener(new com.google.android.gms.maps.GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    cm.onCameraIdle();
                    // 触发半径变化的去抖刷新
                    handleRadiusChangeWithDebounce();
                }
            });
        }
    }
    
    /**
     * 搜索附近数据（根据当前模式搜索植物或花园）
     */
    public void searchNearbyData() {
        Log.d(TAG, "=== Search Nearby Data Debug ===");
        Log.d(TAG, "Current mode: " + (isShowingPlants ? "Plants" : "Gardens"));

        // 以相机中心作为查询中心
        com.google.android.gms.maps.model.LatLng center = locationManager.getCameraCenter();
        int radius = locationManager.getSmartSearchRadius();

        Log.d(TAG, "Search parameters (camera center):");
        Log.d(TAG, "  - Latitude: " + center.latitude);
        Log.d(TAG, "  - Longitude: " + center.longitude);
        Log.d(TAG, "  - Radius: " + radius + " meters");
        Log.d(TAG, "  - Mode: " + (isShowingPlants ? "Plants" : "Gardens"));
        
        // 检查是否使用了默认位置（悉尼）
        com.google.android.gms.maps.model.LatLng sydneyDefault = new com.google.android.gms.maps.model.LatLng(-33.8523341, 151.2106085);
        if (Math.abs(center.latitude - sydneyDefault.latitude) < 0.0001 && 
            Math.abs(center.longitude - sydneyDefault.longitude) < 0.0001) {
            Log.w(TAG, "WARNING: Using default location (Sydney), likely map not ready yet!");
            Log.w(TAG, "Skipping search to avoid querying wrong location");
            if (onPlantGardenMapInteractionListener != null) {
                onPlantGardenMapInteractionListener.onSearchError("Map not ready, please wait");
            }
            Log.d(TAG, "=== End Search Nearby Data Debug (SKIPPED) ===");
            return;
        }

        if (isShowingPlants) {
            searchNearbyPlants(center.latitude, center.longitude, radius);
        } else {
            Log.d(TAG, "Garden mode: fetching all gardens (no nearby search)");
            fetchAllGardens();
        }
        Log.d(TAG, "=== End Search Nearby Data Debug ===");
    }
    
    /**
     * 搜索附近植物
     */
    // Request de-dup signature
    private static class SearchSignature {
        final double lat; final double lng; final int radius; final boolean showPlants;
        SearchSignature(double lat, double lng, int radius, boolean showPlants) { this.lat = lat; this.lng = lng; this.radius = radius; this.showPlants = showPlants; }
        @Override public boolean equals(Object o) { if (!(o instanceof SearchSignature)) return false; SearchSignature s = (SearchSignature) o; return Math.abs(s.lat-lat) < 1e-6 && Math.abs(s.lng-lng) < 1e-6 && s.radius==radius && s.showPlants==showPlants; }
        @Override public int hashCode() { return (int)(lat*1000) ^ (int)(lng*1000) ^ radius ^ (showPlants?1:0); }
    }
    private SearchSignature lastSignature = null;

    public void searchNearbyPlants(double latitude, double longitude, int radius) {
        SearchSignature sig = new SearchSignature(latitude, longitude, radius, true);
        if (lastSignature != null && lastSignature.equals(sig)) {
            Log.d(TAG, "Deduped identical plants search; skipping network call");
            return;
        }
        lastSignature = sig;
        if (onPlantGardenMapInteractionListener != null) onPlantGardenMapInteractionListener.onLoading(true);
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
                    if (mapDtos.isEmpty()) {
                        onPlantGardenMapInteractionListener.onEmptyResult("plants");
                    }
                    onPlantGardenMapInteractionListener.onLoading(false);
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
                    onPlantGardenMapInteractionListener.onLoading(false);
                }
            }
        });
    }
    
    // Removed: searchNearbyGardens in favor of fetchAllGardens

    /**
     * 拉取全部花园并渲染
     */
    public void fetchAllGardens() {
        if (onPlantGardenMapInteractionListener != null) onPlantGardenMapInteractionListener.onLoading(true);
        dataManager.fetchAllGardens(new MapDataManager.MapDataCallback<List<GardenDto>>() {
            @Override
            public void onSuccess(List<GardenDto> gardens) {
                Log.d(TAG, "Displaying gardens on map...");
                displayManager.displayGardensOnMap(gardens);
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onGardensFound(gardens);
                    if (gardens == null || gardens.isEmpty()) {
                        onPlantGardenMapInteractionListener.onEmptyResult("gardens");
                    }
                    onPlantGardenMapInteractionListener.onLoading(false);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Fetch All Gardens Error: " + message);
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onSearchError(message);
                    onPlantGardenMapInteractionListener.onLoading(false);
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
        // 清理防抖待执行任务
        if (pendingSearchRunnable != null) {
            debounceHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }
        
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
     * 设置智能半径变化监听器（带防抖和节流）
     */
    private void setupSmartRadiusListener() {
        locationManager.setOnMapRadiusChangeListener(new MapLocationManager.OnMapRadiusChangeListener() {
            @Override
            public void onMapRadiusChanged(int newRadius) {
                Log.d(TAG, "Map radius changed to: " + newRadius + " meters");
                handleRadiusChangeWithDebounce();
            }
        });
    }
    
    /**
     * 处理半径变化的防抖和节流逻辑
     */
    private void handleRadiusChangeWithDebounce() {
        long currentTime = System.currentTimeMillis();
        
        // 节流：如果距离上次搜索太近，忽略本次请求
        if (currentTime - lastSearchTime < THROTTLE_INTERVAL_MS) {
            Log.d(TAG, "Throttled: too soon since last search");
            return;
        }
        
        // 取消之前的待执行搜索
        if (pendingSearchRunnable != null) {
            debounceHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }
        
        // 创建新的延迟搜索任务
        pendingSearchRunnable = new Runnable() {
            @Override
            public void run() {
                // 以相机中心为准，不再依赖设备定位权限
                Log.d(TAG, "Auto-refreshing data with debounced smart radius (camera center)");
                lastSearchTime = System.currentTimeMillis();
                searchNearbyData();
                pendingSearchRunnable = null;
            }
        };
        
        // 防抖：延迟执行搜索
        debounceHandler.postDelayed(pendingSearchRunnable, DEBOUNCE_DELAY_MS);
        Log.d(TAG, "Debounce timer started, will search in " + DEBOUNCE_DELAY_MS + "ms");
    }
    
    /**
     * 植物花园地图交互监听器接口
     */
    public interface OnPlantGardenMapInteractionListener {
        void onPlantClick(PlantMapDto plant);
        void onGardenClick(GardenDto garden);
        void onPlantsFound(java.util.List<PlantMapDto> plants);
        void onGardensFound(java.util.List<GardenDto> gardens);
        void onSearchError(String message);
        void onDataTypeChanged(boolean isShowingPlants);
        void onPlantLiked(boolean liked);
        void onPlantLikeError(String message);
        default void onLoading(boolean show) {}
        default void onEmptyResult(String type) {}
    }
}
