package com.example.myapplication.map;

import com.example.myapplication.network.GardenDto;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Cluster item for gardens used by Google Maps Utils ClusterManager.
 */
public class GardenClusterItem implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;
    private final GardenDto garden;

    public GardenClusterItem(GardenDto garden) {
        this.garden = garden;
        this.position = new LatLng(garden.getLatitude(), garden.getLongitude());
        this.title = garden.getName();
        this.snippet = garden.getDescription();
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

    @Override
    public Float getZIndex() {
        return 0f;
    }

    public GardenDto getGarden() {
        return garden;
    }
}


