package com.example.myapplication.map;

import android.content.Context;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.util.LogUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

/**
 * Controller dedicated to gardens map logic: data fetching, viewport filtering, rendering, and interactions.
 */
public class GardensMapController {

    private static final String TAG = "GardensMapController";

    private final Context context;
    private final GoogleMap googleMap;
    private final MapDisplayManager displayManager;
    private final MapDataManager dataManager;

    public GardensMapController(Context context, GoogleMap googleMap, MapDisplayManager displayManager, MapDataManager dataManager) {
        this.context = context;
        this.googleMap = googleMap;
        this.displayManager = displayManager;
        this.dataManager = dataManager;
    }

    public void fetchAllGardens(MapDataManager.MapDataCallback<List<GardenDto>> callback) {
        dataManager.fetchAllGardens(callback);
    }

    public void displayGardens(List<GardenDto> gardens) {
        displayManager.displayGardensOnMap(gardens);
    }

    public void refreshGardensForViewport(LatLngBounds bounds, int limit) {
        displayManager.refreshGardensForViewport(bounds, limit);
    }

    public ClusterManager<GardenClusterItem> getClusterManager() {
        return displayManager.getGardenClusterManager();
    }

    public void setOnGardenClickListener(MapDisplayManager.OnGardenMapClickListener listener) {
        displayManager.setOnGardenClickListener(listener);
    }
}


