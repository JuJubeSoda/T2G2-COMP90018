package com.example.myapplication.map;

import android.content.Context;

import com.example.myapplication.network.PlantDto;
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

    public PlantsMapController(Context context, GoogleMap googleMap, MapDisplayManager displayManager, MapDataManager dataManager) {
        this.context = context;
        this.googleMap = googleMap;
        this.displayManager = displayManager;
        this.dataManager = dataManager;
    }

    public void searchNearbyPlants(double latitude, double longitude, int radius, MapDataManager.MapDataCallback<List<PlantDto>> callback) {
        dataManager.searchNearbyPlants(latitude, longitude, radius, callback);
    }

    public void displayPlants(List<PlantMapDto> plants) {
        displayManager.displayPlantsOnMap(plants);
    }

    public void showPlantsFromDtos(List<PlantDto> plantDtos) {
        ArrayList<PlantMapDto> mapDtos = new ArrayList<>();
        if (plantDtos != null) {
            for (PlantDto p : plantDtos) {
                mapDtos.add(PlantMapDto.fromPlantDto(p));
            }
        }
        LogUtil.d(TAG, "Rendering plants count=" + mapDtos.size());
        displayPlants(mapDtos);
    }

    public ClusterManager<PlantClusterItem> getClusterManager() {
        return displayManager.getPlantClusterManager();
    }

    public void setOnPlantClickListener(MapDisplayManager.OnPlantMapClickListener listener) {
        displayManager.setOnPlantClickListener(listener);
    }
}


