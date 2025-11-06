package com.example.myapplication.map;

import android.content.Context;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.PlantMapDto;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;
import com.example.myapplication.util.LogUtil;

/**
 * PlantGardenMapManager - Unified manager for plant and garden map functionality
 * 
 * Integrates location management, data display, and data retrieval for all map-related features.
 * Manages the display of both plants and gardens on Google Maps with clustering support.
 */
public class PlantGardenMapManager {
    
    private static final String TAG = "PlantGardenMapManager";
    
    private final Context context;
    private final GoogleMap googleMap;
    
    // Sub-managers for different map responsibilities
    private final MapLocationManager locationManager;
    private final MapDisplayManager displayManager;
    private final MapDataManager dataManager;
    private final PlantsMapController plantsController;
    private final GardensMapController gardensController;
    private final ClusterBinder clusterBinder;
    
    // Current display state
    private boolean isShowingPlants = true; // true = plants, false = gardens
    
    // Lock: Prevents camera-driven nearby search when entering Plant list from Garden or displaying single Plant
    private boolean plantViewLocked = false;
    
    // Debounce and throttle control for API requests
    private static final long DEBOUNCE_DELAY_MS = 800; // Debounce delay 800ms
    private static final long THROTTLE_INTERVAL_MS = 2000; // Throttle minimum interval 2 seconds
    private final MapSchedulers schedulers = new MapSchedulers(DEBOUNCE_DELAY_MS, THROTTLE_INTERVAL_MS);
    
    // Debug: Request sequence number (logging only, doesn't change behavior)
    private long requestSeq = 0L;
    private long lastIssuedPlantsReqId = 0L;
    
    public PlantGardenMapManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        
        // Initialize sub-managers
        this.locationManager = new MapLocationManager(context, googleMap);
        this.displayManager = new MapDisplayManager(context, googleMap);
        this.dataManager = new MapDataManager(context);
        this.plantsController = new PlantsMapController(context, googleMap, displayManager, dataManager);
        this.gardensController = new GardensMapController(context, googleMap, displayManager, dataManager);
        this.clusterBinder = new ClusterBinder(googleMap);
        
        // Set up intelligent radius change listener
        setupSmartRadiusListener();
        
        LogUtil.d(TAG, "PlantGardenMapManager initialized");
    }
    
    /**
     * Initialize map settings
     */
    public void initializeMap() {
        locationManager.requestLocationPermission();
        locationManager.updateLocationUI();
        
        // Set up click listeners for map interactions
        setupClickListeners();
        
        // Set up initial binding - only Plant ClusterManager exists now
        rebindListenersForCurrentMode();
        
        // Get device location for initial map positioning
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
     * Rebind listeners for the current mode.
     * This ensures the correct ClusterManager's listeners are active after mode switches.
     */
    private void rebindListenersForCurrentMode() {
        if (isShowingPlants) {
            final ClusterManager<PlantClusterItem> plantCM = displayManager.getPlantClusterManager();
            if (plantCM != null) {
                clusterBinder.bind(plantCM, new Runnable() {
                    @Override
                    public void run() {
                        if (!plantViewLocked) {
                            handleRadiusChangeWithDebounce();
                        }
                    }
                });
                LogUtil.d(TAG, "Rebound listeners for Plant mode");
            }
        } else {
            // Ensure Garden ClusterManager exists before binding
            final ClusterManager<GardenClusterItem> gardenCM = displayManager.getGardenClusterManager();
            if (gardenCM != null) {
                clusterBinder.bind(gardenCM, new Runnable() {
                    @Override
                    public void run() {
                        com.google.android.gms.maps.model.LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                        displayManager.refreshGardensForViewport(bounds, 1000);
                        handleRadiusChangeWithDebounce();
                    }
                });
                LogUtil.d(TAG, "Rebound listeners for Garden mode");
            }
        }
    }
    
    /**
     * Set up dual-mode binding for both Plant and Garden ClusterManagers.
     * This allows seamless switching between modes without losing event handling.
     * @deprecated Use rebindListenersForCurrentMode() instead
     */
    @Deprecated
    private void setupDualModeBinding() {
        final ClusterManager<PlantClusterItem> plantCM = displayManager.getPlantClusterManager();
        final ClusterManager<GardenClusterItem> gardenCM = displayManager.getGardenClusterManager();
        
        // Create callbacks for camera idle events
        Runnable plantCameraIdleCallback = new Runnable() {
            @Override
            public void run() {
                if (!plantViewLocked) {
                    handleRadiusChangeWithDebounce();
                } else {
                    LogUtil.d(TAG, "Camera idle: plant mode LOCKED, suppressing auto refresh");
                }
            }
        };
        
        Runnable gardenCameraIdleCallback = new Runnable() {
            @Override
            public void run() {
                com.google.android.gms.maps.model.LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                displayManager.refreshGardensForViewport(bounds, 1000);
                handleRadiusChangeWithDebounce();
            }
        };
        
        // Set up dual-mode binding
        clusterBinder.setupDualModeBinding(
            plantCM, 
            plantCameraIdleCallback,
            gardenCM,
            gardenCameraIdleCallback
        );
        
        // Set initial active mode
        clusterBinder.setActiveMode(isShowingPlants);
        
        LogUtil.d(TAG, "Dual-mode binding initialized, active mode: " + (isShowingPlants ? "Plants" : "Gardens"));
    }
    
    /**
     * Set up click listeners
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
    }
    
    /**
     * Search nearby data (search plants or gardens based on current mode)
     */
    public void searchNearbyData() {
        LogUtil.d(TAG, "=== Search Nearby Data Debug ===");
        LogUtil.d(TAG, "Current mode: " + (isShowingPlants ? "Plants" : "Gardens"));
        if (isShowingPlants && plantViewLocked) {
            LogUtil.d(TAG, "searchNearbyData: suppressed due to plantViewLocked");
            return;
        }

        // Use camera center as query center
        com.google.android.gms.maps.model.LatLng center = locationManager.getCameraCenter();
        int radius = locationManager.getSmartSearchRadius();

        LogUtil.d(TAG, "Search parameters (camera center):");
        LogUtil.d(TAG, "  - Latitude: " + center.latitude);
        LogUtil.d(TAG, "  - Longitude: " + center.longitude);
        LogUtil.d(TAG, "  - Radius: " + radius + " meters");
        LogUtil.d(TAG, "  - Mode: " + (isShowingPlants ? "Plants" : "Gardens"));
        
        // Check if using default location (Sydney) - indicates map not ready yet
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
     * Search nearby plants
     */
    public void searchNearbyPlants(double latitude, double longitude, int radius) {
        if (onPlantGardenMapInteractionListener != null) onPlantGardenMapInteractionListener.onLoading(true);
        long reqId = ++requestSeq;
        lastIssuedPlantsReqId = reqId;
        LogUtil.d(TAG, "ISSUE plants reqId=" + reqId + ", radius=" + radius + ", lat=" + latitude + ", lng=" + longitude);
        
        plantsController.searchNearbyPlants(latitude, longitude, radius, new MapDataManager.MapDataCallback<List<PlantMapDto>>() {
            @Override
            public void onSuccess(List<PlantMapDto> plants) {
                LogUtil.d(TAG, "=== Search Plants Success Debug (reqId=" + reqId + ") ===");
                LogUtil.d(TAG, "Returned reqId=" + reqId + ", lastIssuedPlantsReqId=" + lastIssuedPlantsReqId);
                LogUtil.d(TAG, "Plants found: " + (plants == null ? "null" : plants.size()));
                if (plants != null && !plants.isEmpty()) {
                    LogUtil.d(TAG, "First plant details:");
                    PlantMapDto firstPlant = plants.get(0);
                    LogUtil.d(TAG, "  - Name: " + firstPlant.getName());
                    LogUtil.d(TAG, "  - Coordinates: (" + firstPlant.getLatitude() + ", " + firstPlant.getLongitude() + ")");
                    LogUtil.d(TAG, "  - PlantId: " + firstPlant.getPlantId());
                }
                LogUtil.d(TAG, "=== End Search Plants Success Debug (reqId=" + reqId + ") ===");
                
                // Directly render the returned PlantMapDto list
                plantsController.displayPlants(plants);
                LogUtil.d(TAG, "Plants displayed on map successfully");
                
                // Notify external listener
                if (onPlantGardenMapInteractionListener != null) {
                    LogUtil.d(TAG, "Listener is not null, calling onPlantsFound (map dtos)");
                    onPlantGardenMapInteractionListener.onPlantsFound(plants);
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
                LogUtil.e(TAG, "=== Search Plants Error Debug (reqId=" + reqId + ") ===");
                LogUtil.e(TAG, "Error message: " + message);
                LogUtil.e(TAG, "=== End Search Plants Error Debug (reqId=" + reqId + ") ===");
                
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onSearchError(message);
                    onPlantGardenMapInteractionListener.onLoading(false);
                }
            }
        });
    }
    
    // Removed: searchNearbyGardens in favor of fetchAllGardens

    /**
     * Fetch all gardens and render them
     */
    public void fetchAllGardens() {
        if (onPlantGardenMapInteractionListener != null) onPlantGardenMapInteractionListener.onLoading(true);
        gardensController.fetchAllGardens(new MapDataManager.MapDataCallback<List<GardenDto>>() {
            @Override
            public void onSuccess(List<GardenDto> gardens) {
                LogUtil.d(TAG, "Displaying gardens on map...");
                gardensController.displayGardens(gardens);
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
     * Enter plants mode
     */
    private void enterPlantsMode() {
        isShowingPlants = true;
        displayManager.clearCurrentDisplay();
        rebindListenersForCurrentMode();
        if (onPlantGardenMapInteractionListener != null) {
            onPlantGardenMapInteractionListener.onDataTypeChanged(true);
        }
    }

    /**
     * Enter gardens mode
     */
    private void enterGardensMode() {
        isShowingPlants = false;
        displayManager.clearCurrentDisplay();
        // Ensure Garden ClusterManager is created (lazy initialization)
        displayManager.getGardenClusterManager();
        rebindListenersForCurrentMode();
        if (onPlantGardenMapInteractionListener != null) {
            onPlantGardenMapInteractionListener.onDataTypeChanged(false);
        }
    }

    /**
     * Restore gardens clustering display (return from plants mode)
     */
    public void restoreGardensView() {
        enterGardensMode();
        fetchAllGardens();
    }

    /**
     * Fetch plants by garden ID and switch to plants layer
     */
    public void fetchPlantsByGarden(long gardenId) {
        if (onPlantGardenMapInteractionListener != null) onPlantGardenMapInteractionListener.onLoading(true);
        dataManager.getPlantsByGarden(gardenId, new MapDataManager.MapDataCallback<List<PlantDto>>() {
            @Override
            public void onSuccess(List<PlantDto> plants) {
                enterPlantsMode();
                plantViewLocked = true;
                java.util.ArrayList<PlantMapDto> mapDtos = new java.util.ArrayList<>();
                if (plants != null) {
                    for (PlantDto p : plants) {
                        mapDtos.add(PlantMapDto.fromPlantDto(p));
                    }
                }
                plantsController.displayPlants(mapDtos);
                if (onPlantGardenMapInteractionListener != null) {
                    onPlantGardenMapInteractionListener.onPlantsFound(mapDtos);
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
     * Toggle data display mode
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
     * Incremental update of plant display (add new plants)
     */
    public void addNewPlants(List<PlantMapDto> newPlants) {
        if (isShowingPlants) {
            displayManager.addNewPlants(newPlants);
        }
    }
    
    /**
     * Remove plant display
     */
    public void removePlants(List<PlantMapDto> plantsToRemove) {
        if (isShowingPlants) {
            displayManager.removePlants(plantsToRemove);
        }
    }
    
    /**
     * Refresh plant display (full update)
     */
    public void refreshPlants(List<PlantMapDto> plants) {
        if (isShowingPlants) {
            displayManager.displayPlantsOnMap(plants);
        }
    }
    
    /**
     * Like plant
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
     * Unlike plant
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
     * Handle permission request result
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
    
    // Getter methods for accessing manager state
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
     * Gets the location manager instance
     */
    public MapLocationManager getLocationManager() {
        return locationManager;
    }
    
    // Interaction listener for map events
    private OnPlantGardenMapInteractionListener onPlantGardenMapInteractionListener;
    
    public void setOnPlantGardenMapInteractionListener(OnPlantGardenMapInteractionListener listener) {
        this.onPlantGardenMapInteractionListener = listener;
    }

    /**
     * Searches for and displays a plant on the map by PlantId with focus
     */
    public void searchAndShowPlantById(int plantId) {
        // Ensure we're in plants mode
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
                // Do NOT lock for search-by-id; allow normal map interactions to resume
                plantViewLocked = false;
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
                if (isShowingPlants && plantViewLocked) {
                    LogUtil.d(TAG, "Debounced refresh suppressed (plantViewLocked)");
                    return;
                }
                LogUtil.d(TAG, "Auto-refreshing data with debounced smart radius (camera center)");
                searchNearbyData();
            }
        });
    }

    /** 锁定/解锁 Plant 视图 */
    public void unlockPlantView() {
        if (plantViewLocked) {
            plantViewLocked = false;
            LogUtil.d(TAG, "unlockPlantView: unlocking");
            searchNearbyData();
        }
    }

    public void lockPlantView() {
        plantViewLocked = true;
        LogUtil.d(TAG, "lockPlantView: locked");
    }

    public boolean isPlantViewLocked() {
        return plantViewLocked;
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
