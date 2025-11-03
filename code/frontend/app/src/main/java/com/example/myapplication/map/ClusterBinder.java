package com.example.myapplication.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.example.myapplication.util.LogUtil;

/**
 * Centralizes binding of GoogleMap listeners to a given ClusterManager.
 * This reduces chances of listener overrides and concentrates event wiring.
 * 
 * Now supports dual-mode binding for both Plant and Garden ClusterManagers.
 */
public class ClusterBinder {

    private static final String TAG = "ClusterBinder";
    
    private final GoogleMap googleMap;
    
    // Track both managers for dual-mode support
    private ClusterManager<PlantClusterItem> plantCM;
    private ClusterManager<GardenClusterItem> gardenCM;
    
    // Track active mode and callbacks
    private boolean isPlantMode = true;
    private Runnable plantCameraIdleCallback;
    private Runnable gardenCameraIdleCallback;

    public ClusterBinder(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    /**
     * Set up dual-mode binding for both Plant and Garden ClusterManagers.
     * This allows switching between modes without losing event handling.
     */
    public void setupDualModeBinding(
            ClusterManager<PlantClusterItem> plantCM, 
            Runnable plantCameraIdleCallback,
            ClusterManager<GardenClusterItem> gardenCM,
            Runnable gardenCameraIdleCallback) {
        if (googleMap == null) return;
        
        this.plantCM = plantCM;
        this.gardenCM = gardenCM;
        this.plantCameraIdleCallback = plantCameraIdleCallback;
        this.gardenCameraIdleCallback = gardenCameraIdleCallback;
        
        // Set up unified listeners that route to the active ClusterManager
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Try both managers - the correct one will handle the marker
                if (ClusterBinder.this.plantCM != null && ClusterBinder.this.plantCM.onMarkerClick(marker)) {
                    LogUtil.d(TAG, "Marker handled by plant CM");
                    return true;
                }
                if (ClusterBinder.this.gardenCM != null && ClusterBinder.this.gardenCM.onMarkerClick(marker)) {
                    LogUtil.d(TAG, "Marker handled by garden CM");
                    return true;
                }
                return false;
            }
        });

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // Call both managers' onCameraIdle for clustering logic
                if (ClusterBinder.this.plantCM != null) {
                    ClusterBinder.this.plantCM.onCameraIdle();
                }
                if (ClusterBinder.this.gardenCM != null) {
                    ClusterBinder.this.gardenCM.onCameraIdle();
                }
                
                // Only call the callback for the active mode
                if (ClusterBinder.this.isPlantMode && ClusterBinder.this.plantCameraIdleCallback != null) {
                    ClusterBinder.this.plantCameraIdleCallback.run();
                } else if (!ClusterBinder.this.isPlantMode && ClusterBinder.this.gardenCameraIdleCallback != null) {
                    ClusterBinder.this.gardenCameraIdleCallback.run();
                }
            }
        });
        
        LogUtil.d(TAG, "Dual-mode binding set up");
    }
    
    /**
     * Switch the active mode for camera idle callbacks.
     */
    public void setActiveMode(boolean isPlantMode) {
        this.isPlantMode = isPlantMode;
        LogUtil.d(TAG, "Active mode switched to: " + (isPlantMode ? "Plants" : "Gardens"));
    }

    /**
     * Bind marker click and camera idle events to the provided ClusterManager.
     * Optionally run an extra action after the CM processes onCameraIdle (e.g., viewport refresh or debounce logic).
     * 
     * @deprecated Use setupDualModeBinding instead for better mode switching support
     */
    @Deprecated
    public <T extends ClusterItem> void bind(ClusterManager<T> clusterManager, Runnable afterCameraIdle) {
        if (googleMap == null || clusterManager == null) return;

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return clusterManager.onMarkerClick(marker);
            }
        });

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                clusterManager.onCameraIdle();
                if (afterCameraIdle != null) afterCameraIdle.run();
            }
        });
    }
}


