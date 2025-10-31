package com.example.myapplication.map;

import android.content.Context;

import com.example.myapplication.network.PlantMapDto;
import com.example.myapplication.util.LogUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller dedicated to plants map logic: data fetching, rendering, and interactions.
 */
public class PlantsMapController {

    private static final String TAG = "PlantsMapController";

    private final Context context;
    private final GoogleMap googleMap;
    private final MapDisplayManager displayManager;
    private final MapDataManager dataManager;

    // Request de-dup signature
    private static class SearchSignature {
        final double lat; final double lng; final int radius;
        SearchSignature(double lat, double lng, int radius) { this.lat = lat; this.lng = lng; this.radius = radius; }
        @Override public boolean equals(Object o) { if (!(o instanceof SearchSignature)) return false; SearchSignature s = (SearchSignature) o; return Math.abs(s.lat-lat) < 1e-6 && Math.abs(s.lng-lng) < 1e-6 && s.radius==radius; }
        @Override public int hashCode() { return (int)(lat*1000) ^ (int)(lng*1000) ^ radius; }
    }
    private SearchSignature lastSignature = null;

    public PlantsMapController(Context context, GoogleMap googleMap, MapDisplayManager displayManager, MapDataManager dataManager) {
        this.context = context;
        this.googleMap = googleMap;
        this.displayManager = displayManager;
        this.dataManager = dataManager;
    }

    public void searchNearbyPlants(double latitude, double longitude, int radius, MapDataManager.MapDataCallback<List<PlantMapDto>> callback) {
        SearchSignature sig = new SearchSignature(latitude, longitude, radius);
        if (lastSignature != null && lastSignature.equals(sig)) {
            LogUtil.d(TAG, "Deduped identical plants search; skipping network call");
            return;
        }
        lastSignature = sig;
        dataManager.searchNearbyPlants(latitude, longitude, radius, callback);
    }

    public void displayPlants(List<PlantMapDto> plants) {
        displayManager.displayPlantsOnMap(plants);
    }

    // No longer needed to convert from PlantDto; API returns PlantMapDto directly

    public ClusterManager<PlantClusterItem> getClusterManager() {
        return displayManager.getPlantClusterManager();
    }

    public void setOnPlantClickListener(MapDisplayManager.OnPlantMapClickListener listener) {
        displayManager.setOnPlantClickListener(listener);
    }
}


