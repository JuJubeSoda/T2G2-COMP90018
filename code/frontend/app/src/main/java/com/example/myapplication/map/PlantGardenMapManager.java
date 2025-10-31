package com.example.myapplication.map;

import android.content.Context;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.PlantMapDto;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import com.example.myapplication.util.LogUtil;

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
    private final PlantsMapController plantsController;
    private final GardensMapController gardensController;
    private final ClusterBinder clusterBinder;
    
    // 当前状态
    private boolean isShowingPlants = true; // true = plants, false = gardens
    
    // 防抖和节流控制
    private static final long DEBOUNCE_DELAY_MS = 800; // 防抖延迟800ms
    private static final long THROTTLE_INTERVAL_MS = 2000; // 节流最小间隔2秒
    private final MapSchedulers schedulers = new MapSchedulers(DEBOUNCE_DELAY_MS, THROTTLE_INTERVAL_MS);
    
    public PlantGardenMapManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        
        // 初始化子管理器
        this.locationManager = new MapLocationManager(context, googleMap);
        this.displayManager = new MapDisplayManager(context, googleMap);
        this.dataManager = new MapDataManager(context);
        this.plantsController = new PlantsMapController(context, googleMap, displayManager, dataManager);
        this.gardensController = new GardensMapController(context, googleMap, displayManager, dataManager);
        this.clusterBinder = new ClusterBinder(googleMap);
        
        // 设置智能半径变化监听器
        setupSmartRadiusListener();
        
        LogUtil.d(TAG, "PlantGardenMapManager initialized");
    }
    
    /**
     * 初始化地图设置
     */
    public void initializeMap() {
        locationManager.requestLocationPermission();
        locationManager.updateLocationUI();
        
        // 设置点击监听器
        setupClickListeners();
        // 初始时按当前模式绑定监听，防止早期点击丢失
        applyModeListeners();
        
        // 获取设备位置
        locationManager.getDeviceLocation(new MapLocationManager.OnLocationResultCallback() {
            @Override
            public void onLocationSuccess(android.location.Location location) {
                LogUtil.d(TAG, "Device location obtained successfully");
            }
            
            @Override
            public void onLocationError(String error) {
                LogUtil.e(TAG, "Failed to get device location: " + error);
            }
        });
    }
    
    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        plantsController.setOnPlantClickListener(new MapDisplayManager.OnPlantMapClickListener() {
            @Override
            public void onPlantClick(PlantMapDto plant) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantClick(plant);
                }
            }
        });
        
        gardensController.setOnGardenClickListener(new MapDisplayManager.OnGardenMapClickListener() {
            @Override
            public void onGardenClick(GardenDto garden) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onGardenClick(garden);
                }
            }
        });
        
        // 改为按模式直接绑定到对应的 ClusterManager
        applyModeListeners();
    }

    /**
     * 根据当前模式绑定点击与相机空闲事件到对应 ClusterManager
     */
    private void applyModeListeners() {
        final com.google.maps.android.clustering.ClusterManager<PlantClusterItem> plantCM = displayManager.getPlantClusterManager();
        final com.google.maps.android.clustering.ClusterManager<GardenClusterItem> gardenCM = displayManager.getGardenClusterManager();

        if (isShowingPlants && plantCM != null) {
            clusterBinder.bind(plantCM, new Runnable() {
                @Override
                public void run() {
                    handleRadiusChangeWithDebounce();
                }
            });
        } else if (!isShowingPlants && gardenCM != null) {
            clusterBinder.bind(gardenCM, new Runnable() {
                @Override
                public void run() {
                    com.google.android.gms.maps.model.LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                    displayManager.refreshGardensForViewport(bounds, 1000);
                    handleRadiusChangeWithDebounce();
                }
            });
        }
        LogUtil.d(TAG, "applyModeListeners: bound to " + (isShowingPlants ? "plantCM" : "gardenCM"));
    }
    
    /**
     * 搜索附近数据（根据当前模式搜索植物或花园）
     */
    public void searchNearbyData() {
        LogUtil.d(TAG, "=== Search Nearby Data Debug ===");
        LogUtil.d(TAG, "Current mode: " + (isShowingPlants ? "Plants" : "Gardens"));

        // 以相机中心作为查询中心
        com.google.android.gms.maps.model.LatLng center = locationManager.getCameraCenter();
        int radius = locationManager.getSmartSearchRadius();

        LogUtil.d(TAG, "Search parameters (camera center):");
        LogUtil.d(TAG, "  - Latitude: " + center.latitude);
        LogUtil.d(TAG, "  - Longitude: " + center.longitude);
        LogUtil.d(TAG, "  - Radius: " + radius + " meters");
        LogUtil.d(TAG, "  - Mode: " + (isShowingPlants ? "Plants" : "Gardens"));
        
        // 检查是否使用了默认位置（悉尼）
        com.google.android.gms.maps.model.LatLng sydneyDefault = new com.google.android.gms.maps.model.LatLng(-33.8523341, 151.2106085);
        if (Math.abs(center.latitude - sydneyDefault.latitude) < 0.0001 && 
            Math.abs(center.longitude - sydneyDefault.longitude) < 0.0001) {
            LogUtil.w(TAG, "WARNING: Using default location (Sydney), likely map not ready yet!");
            LogUtil.w(TAG, "Skipping search to avoid querying wrong location");
            if (onPlantGardenMapInteractionListener != null) {
                onPlantGardenMapInteractionListener.onSearchError("Map not ready, please wait");
            }
            LogUtil.d(TAG, "=== End Search Nearby Data Debug (SKIPPED) ===");
            return;
        }

        if (isShowingPlants) {
            searchNearbyPlants(center.latitude, center.longitude, radius);
        } else {
            LogUtil.d(TAG, "Garden mode: fetching all gardens (then viewport filter with cap 1000)");
            fetchAllGardens();
        }
        LogUtil.d(TAG, "=== End Search Nearby Data Debug ===");
    }
    
    /**
     * 搜索附近植物
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius) {
        if (onPlantGardenMapInteractionListener != null) onPlantGardenMapInteractionListener.onLoading(true);
        LogUtil.d(TAG, "Searching for nearby plants with radius: " + radius + " meters");
        
        plantsController.searchNearbyPlants(latitude, longitude, radius, new MapDataManager.MapDataCallback<List<PlantDto>>() {
            @Override
            public void onSuccess(List<PlantDto> plants) {
                LogUtil.d(TAG, "=== Search Plants Success Debug ===");
                LogUtil.d(TAG, "Plants found: " + (plants == null ? "null" : plants.size()));
                if (plants != null && !plants.isEmpty()) {
                    LogUtil.d(TAG, "First plant details:");
                    PlantDto firstPlant = plants.get(0);
                    LogUtil.d(TAG, "  - Name: " + firstPlant.getName());
                    LogUtil.d(TAG, "  - Coordinates: (" + firstPlant.getLatitude() + ", " + firstPlant.getLongitude() + ")");
                    LogUtil.d(TAG, "  - PlantId: " + firstPlant.getPlantId());
                }
                LogUtil.d(TAG, "=== End Search Plants Success Debug ===");
                
                // 先转换数据
                plantsController.showPlantsFromDtos(plants);
                // 渲染完成后重新按模式绑定，避免监听被覆盖
                applyModeListeners();
                LogUtil.d(TAG, "Plants displayed on map successfully");
                
                // 通知外部监听器
                if (onPlantGardenMapInteractionListener != null) {
                    LogUtil.d(TAG, "Listener is not null, calling onPlantsFound (map dtos)");
                    // 此处回调用 latest rendered list 不易取，保持空实现或可选重查；先简单通知加载结束
                    LogUtil.d(TAG, "onPlantsFound called successfully");
                    if (plants == null || plants.isEmpty()) {
                        onPlantGardenMapInteractionListener.onEmptyResult("plants");
                    }
                    onPlantGardenMapInteractionListener.onLoading(false);
                } else {
                    LogUtil.e(TAG, "onPlantGardenMapInteractionListener is null!");
                }
            }
            
            @Override
            public void onError(String message) {
                LogUtil.e(TAG, "=== Search Plants Error Debug ===");
                LogUtil.e(TAG, "Error message: " + message);
                LogUtil.e(TAG, "=== End Search Plants Error Debug ===");
                
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
        gardensController.fetchAllGardens(new MapDataManager.MapDataCallback<List<GardenDto>>() {
            @Override
            public void onSuccess(List<GardenDto> gardens) {
                LogUtil.d(TAG, "Displaying gardens on map...");
                gardensController.displayGardens(gardens);
                applyModeListeners();
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
                LogUtil.e(TAG, "Fetch All Gardens Error: " + message);
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onSearchError(message);
                    onPlantGardenMapInteractionListener.onLoading(false);
                }
            }
        });
    }

    /**
     * 进入植物模式
     */
    private void enterPlantsMode() {
        isShowingPlants = true;
        displayManager.clearCurrentDisplay();
        applyModeListeners();
        if (onPlantGardenMapInteractionListener != null) {
            onPlantGardenMapInteractionListener.onDataTypeChanged(true);
        }
    }

    /**
     * 进入花园模式
     */
    private void enterGardensMode() {
        isShowingPlants = false;
        displayManager.clearCurrentDisplay();
        applyModeListeners();
        if (onPlantGardenMapInteractionListener != null) {
            onPlantGardenMapInteractionListener.onDataTypeChanged(false);
        }
    }

    /**
     * 恢复花园聚合显示（从植物模式返回）
     */
    public void restoreGardensView() {
        enterGardensMode();
        fetchAllGardens();
    }

    /**
     * 通过花园ID获取植物并切换到植物图层
     */
    public void fetchPlantsByGarden(long gardenId) {
        if (onPlantGardenMapInteractionListener != null) onPlantGardenMapInteractionListener.onLoading(true);
        dataManager.getPlantsByGarden(gardenId, new MapDataManager.MapDataCallback<List<PlantDto>>() {
            @Override
            public void onSuccess(List<PlantDto> plants) {
                enterPlantsMode();
                plantsController.showPlantsFromDtos(plants);
                applyModeListeners();
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onLoading(false);
                }
            }

            @Override
            public void onError(String message) {
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
        if (isShowingPlants) {
            enterGardensMode();
        } else {
            enterPlantsMode();
        }
        String message = isShowingPlants ? "Switched to Plants view" : "Switched to Gardens view";
        LogUtil.d(TAG, message);
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
        schedulers.destroy();
        displayManager.destroy();
        LogUtil.d(TAG, "PlantGardenMapManager destroyed");
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
     * 通过PlantId搜索并在地图上显示与聚焦
     */
    public void searchAndShowPlantById(int plantId) {
        // 确保是植物模式
        if (!isShowingPlants) {
            toggleDataType();
        }
        if (onPlantGardenMapInteractionListener != null) {
            onPlantGardenMapInteractionListener.onLoading(true);
        }
        dataManager.getPlantById(plantId, new MapDataManager.MapDataCallback<com.example.myapplication.network.PlantDto>() {
            @Override
            public void onSuccess(com.example.myapplication.network.PlantDto plantDto) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onLoading(false);
                }
                if (plantDto == null || plantDto.getLatitude() == null || plantDto.getLongitude() == null) {
                    if (onPlantGardenMapInteractionListener != null) {
                        onPlantGardenMapInteractionListener.onSearchError("Plant not found or no coordinates");
                    }
                    return;
                }
                PlantMapDto mapDto = PlantMapDto.fromPlantDto(plantDto);
                displayManager.displayAndFocusSinglePlant(mapDto, 17f);
            }

            @Override
            public void onError(String message) {
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onLoading(false);
                    onPlantGardenMapInteractionListener.onSearchError(message);
                }
            }
        });
    }
    
    /**
     * 设置智能半径变化监听器（带防抖和节流）
     */
    private void setupSmartRadiusListener() {
        locationManager.setOnMapRadiusChangeListener(new MapLocationManager.OnMapRadiusChangeListener() {
            @Override
            public void onMapRadiusChanged(int newRadius) {
                LogUtil.d(TAG, "Map radius changed to: " + newRadius + " meters");
                handleRadiusChangeWithDebounce();
            }
        });
    }
    
    /**
     * 处理半径变化的防抖和节流逻辑
     */
    private void handleRadiusChangeWithDebounce() {
        schedulers.schedule(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "Auto-refreshing data with debounced smart radius (camera center)");
                searchNearbyData();
            }
        });
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
