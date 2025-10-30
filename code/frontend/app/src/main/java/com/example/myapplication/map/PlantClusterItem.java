package com.example.myapplication.map;

import com.example.myapplication.network.PlantMapDto;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Cluster item for plants used by Google Maps Utils ClusterManager.
 */
public class PlantClusterItem implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;
    private final PlantMapDto plant;

    public PlantClusterItem(PlantMapDto plant) {
        this.plant = plant;
        this.position = new LatLng(plant.getLatitude(), plant.getLongitude());
        this.title = plant.getName();
        this.snippet = plant.getDescription();
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public PlantMapDto getPlant() {
        return plant;
    }
}


