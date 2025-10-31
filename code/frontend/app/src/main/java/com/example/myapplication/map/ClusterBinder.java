package com.example.myapplication.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

/**
 * Centralizes binding of GoogleMap listeners to a given ClusterManager.
 * This reduces chances of listener overrides and concentrates event wiring.
 */
public class ClusterBinder {

    private final GoogleMap googleMap;

    public ClusterBinder(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    /**
     * Bind marker click and camera idle events to the provided ClusterManager.
     * Optionally run an extra action after the CM processes onCameraIdle (e.g., viewport refresh or debounce logic).
     */
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


