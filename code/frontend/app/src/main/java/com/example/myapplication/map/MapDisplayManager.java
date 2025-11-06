package com.example.myapplication.map;

import android.content.Context;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantMapDto;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.util.LogUtil;

/**
 * Map Display Manager - responsible for showing plants and gardens on the map.
 * Centralizes map display features, including clustering and markers.
 */
public class MapDisplayManager {
    
    private static final String TAG = "MapDisplayManager";
    
    private final Context context;
    private final GoogleMap googleMap;
    // Clustering for plants
    private ClusterManager<PlantClusterItem> plantClusterManager;
    private DefaultClusterRenderer<PlantClusterItem> plantClusterRenderer;
    private List<PlantClusterItem> currentPlantItems = new ArrayList<>();
    // Clustering for gardens
    private ClusterManager<GardenClusterItem> gardenClusterManager;
    private DefaultClusterRenderer<GardenClusterItem> gardenClusterRenderer;
    private List<GardenClusterItem> currentGardenItems = new ArrayList<>();
    private List<PlantMapDto> currentPlants = new ArrayList<>();
    private List<GardenDto> currentGardens = new ArrayList<>();
    
    // Callback interfaces
    public interface OnGardenMapClickListener {
        void onGardenClick(GardenDto garden);
    }
    
    public interface OnPlantMapClickListener {
        void onPlantClick(PlantMapDto plant);
    }
    
    private OnGardenMapClickListener gardenClickListener;
    private OnPlantMapClickListener plantClickListener;
    
    public MapDisplayManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        LogUtil.d(TAG, "Initializing MapDisplayManager");
        setupClusterManagerIfNeeded();
        // Delay Garden ClusterManager creation until needed
        // setupGardenClusterManagerIfNeeded();
        // Disable default InfoWindow
        if (googleMap != null) {
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) { return null; }
                @Override
                public View getInfoContents(Marker marker) { return null; }
            });
        }
    }
    
    public void setOnGardenClickListener(OnGardenMapClickListener listener) {
        this.gardenClickListener = listener;
    }
    
    public void setOnPlantClickListener(OnPlantMapClickListener listener) {
        this.plantClickListener = listener;
    }
    
    /**
     * Display plants on the map - using markers (suitable for frequent refreshes)
     */
    public void displayPlantsOnMap(List<PlantMapDto> plants) {
        LogUtil.d(TAG, "=== Display Plants Debug ===");
        LogUtil.d(TAG, "Received plants list: " + (plants == null ? "null" : "size=" + plants.size()));
        LogUtil.d(TAG, "GoogleMap instance: " + (googleMap == null ? "null" : "available"));
        
        if (plants == null || plants.isEmpty()) {
            LogUtil.d(TAG, "Empty result: preserve existing markers (no clear)");
            LogUtil.d(TAG, "=== End Display Plants Debug ===");
            return;
        }

        if (plants != null) {
            for (int i = 0; i < plants.size(); i++) {
                PlantMapDto plant = plants.get(i);
                LogUtil.d(TAG, "Plant " + i + ": " + plant.toString());
                LogUtil.d(TAG, "  - Name: " + plant.getName());
                LogUtil.d(TAG, "  - Latitude: " + plant.getLatitude());
                LogUtil.d(TAG, "  - Longitude: " + plant.getLongitude());
                LogUtil.d(TAG, "  - PlantId: " + plant.getPlantId());
            }
        }
        
        LogUtil.d(TAG, "Clearing existing plant items...");
        clearPlantMarkers();
        currentPlants.clear();
        currentPlantItems.clear();
        
        int validPlants = 0;
        if (plants != null) {
            for (PlantMapDto plant : plants) {
                if (plant.getLatitude() != null && plant.getLongitude() != null) {
                    LogUtil.d(TAG, "Adding marker for plant: " + plant.getName() + " at (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
                    try {
                        addPlantClusterItem(plant);
                        currentPlants.add(plant);
                        validPlants++;
                        LogUtil.d(TAG, "Successfully added marker for: " + plant.getName());
                    } catch (Exception e) {
                        LogUtil.e(TAG, "Failed to add marker for plant: " + plant.getName(), e);
                    }
                } else {
                    LogUtil.w(TAG, "Skipping plant with null coordinates: " + plant.getName());
                }
            }
        }
        
        LogUtil.d(TAG, "Final cluster item count: " + currentPlantItems.size());
        LogUtil.d(TAG, "Valid plants: " + validPlants + " out of " + (plants != null ? plants.size() : 0));
        LogUtil.d(TAG, "Current plants list size: " + currentPlants.size());
        
        // Check map readiness/status
        if (googleMap != null) {
            LogUtil.d(TAG, "GoogleMap is ready, camera position: " + googleMap.getCameraPosition());
            LogUtil.d(TAG, "GoogleMap is ready, visible region: " + googleMap.getProjection().getVisibleRegion());
        } else {
            LogUtil.e(TAG, "GoogleMap is null! Cannot display markers.");
        }
        
        // UI feedback should be handled at higher layer
        LogUtil.d(TAG, "=== End Display Plants Debug ===");
    }
    
    /**
     * Incrementally update plant display (add new plants)
     */
    public void addNewPlants(List<PlantMapDto> newPlants) {
        for (PlantMapDto plant : newPlants) {
            if (plant.getLatitude() != null && plant.getLongitude() != null && !currentPlants.contains(plant)) {
                addPlantClusterItem(plant);
                currentPlants.add(plant);
            }
        }
        
        LogUtil.d(TAG, "Added " + newPlants.size() + " new plants to map");
    }
    
    /**
     * Remove plant markers
     */
    public void removePlants(List<PlantMapDto> plantsToRemove) {
        for (PlantMapDto plant : plantsToRemove) {
            removePlantMarker(plant);
            currentPlants.remove(plant);
        }
        
        LogUtil.d(TAG, "Removed " + plantsToRemove.size() + " plants from map");
    }
    
    /**
     * Add a single plant marker
     */
    private void addPlantMarker(PlantMapDto plant) {
        LogUtil.d(TAG, "=== Add Plant Marker Debug ===");
        LogUtil.d(TAG, "Plant: " + plant.getName());
        LogUtil.d(TAG, "Coordinates: (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
        LogUtil.d(TAG, "GoogleMap null check: " + (googleMap == null));
        
        if (googleMap == null) {
            LogUtil.e(TAG, "GoogleMap is null! Cannot add marker.");
            return;
        }
        
        LatLng position = new LatLng(plant.getLatitude(), plant.getLongitude());
        LogUtil.d(TAG, "Created LatLng: " + position);
        
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(plant.getName())
                .snippet(plant.getDescription())
                .icon(createPlantIcon());
        
        LogUtil.d(TAG, "MarkerOptions created: " + markerOptions);
        
        Marker marker = googleMap.addMarker(markerOptions);
        LogUtil.d(TAG, "Marker created: " + (marker == null ? "null" : "success"));
        
        if (marker != null) {
            marker.setTag(plant);
            // Legacy path (kept for potential fallback)
            
            // Set click listener
            setupPlantMarkerClickListener(marker);
            LogUtil.d(TAG, "Marker click listener set");
        } else {
            LogUtil.e(TAG, "Failed to create marker for plant: " + plant.getName());
        }
        LogUtil.d(TAG, "=== End Add Plant Marker Debug ===");
    }

    /**
     * Add cluster item and refresh clustering
     */
    private void addPlantClusterItem(PlantMapDto plant) {
        if (plantClusterManager == null) {
            setupClusterManagerIfNeeded();
        }
        PlantClusterItem item = new PlantClusterItem(plant);
        currentPlantItems.add(item);
        plantClusterManager.addItem(item);
        plantClusterManager.cluster();
    }
    
    /**
     * Remove a single plant marker
     */
    private void removePlantMarker(PlantMapDto plant) {
        if (plantClusterManager == null || currentPlantItems.isEmpty()) return;
        for (int i = currentPlantItems.size() - 1; i >= 0; i--) {
            PlantClusterItem item = currentPlantItems.get(i);
            if (item.getPlant() != null && java.util.Objects.equals(item.getPlant().getPlantId(), plant.getPlantId())) {
                currentPlantItems.remove(i);
                plantClusterManager.removeItem(item);
                break;
            }
        }
        plantClusterManager.cluster();
    }
    
    /**
     * Configure plant marker click listener
     */
    private void setupPlantMarkerClickListener(Marker marker) {
        // Click listener is handled via GoogleMap's setOnMarkerClickListener
    }
    
    /**
     * Display gardens on the map (using clustering; suitable for static data)
     */
    public void displayGardensOnMap(List<GardenDto> gardens) {
        clearGardenMarkers();
        currentGardens.clear();
        if (gardens != null) {
            currentGardens.addAll(gardens);
        }
        // Render for current viewport initially; subsequent refresh via camera events
        if (googleMap != null) {
            LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
            refreshGardensForViewport(bounds, 1000);
        }
    }
    
    /**
     * Clear plant markers
     */
    public void clearPlantMarkers() {
        // Clear clustering items
        if (plantClusterManager != null) {
            plantClusterManager.clearItems();
            plantClusterManager.cluster();
        }
        currentPlantItems.clear();
        // Legacy markers list is no longer used
    }
    
    /**
    * Clear garden clustering
    */
    public void clearGardenMarkers() {
        if (gardenClusterManager != null) {
            gardenClusterManager.clearItems();
            gardenClusterManager.cluster();
        }
        currentGardenItems.clear();
    }
    
    
    /**
     * Refresh gardens within the given viewport (filter and cluster)
     */
    public void refreshGardensForViewport(LatLngBounds bounds, int limit) {
        if (bounds == null) return;
        setupGardenClusterManagerIfNeeded();
        clearGardenMarkers();
        int count = 0;
        for (GardenDto g : currentGardens) {
            if (g.getLatitude() == null || g.getLongitude() == null) continue;
            LatLng pos = new LatLng(g.getLatitude(), g.getLongitude());
            if (bounds.contains(pos)) {
                gardenClusterManager.addItem(new GardenClusterItem(g));
                count++;
                if (count >= limit) break;
            }
        }
        gardenClusterManager.cluster();
        LogUtil.d(TAG, "Displayed gardens in viewport: " + count);
    }
    
    
    /**
     * Create garden GeoJSON features (removed)
     */
    // GeoJSON-related methods removed since we now use clustering for gardens
    
    
    /**
     * Setup garden GeoJSON layer (removed)
     */
    // GeoJSON layer setup removed
    
    
    /**
     * Handle garden GeoJSON feature clicks (removed)
     */
    // Feature click handling removed with GeoJSON
    
    
    /**
     * Create Garden from GeoJSON feature (removed)
     */
    // Conversion from GeoJSON removed
    
    
    /**
     * Apply garden marker style (removed)
     */
    // Style application for GeoJSON removed
    
    /**
     * Create plant icon
     */
    private BitmapDescriptor createPlantIcon() {
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.flower);
    }
    
    /**
     * Create garden icon
     */
    private BitmapDescriptor createGardenIcon() {
        // Ensure garden icon matches plant icon visual size
        try {
            // Decode plant icon to get target dimensions
            Bitmap plantBmp = BitmapFactory.decodeResource(context.getResources(), com.example.myapplication.R.drawable.flower);
            int targetW = plantBmp != null ? plantBmp.getWidth() : 0;
            int targetH = plantBmp != null ? plantBmp.getHeight() : 0;

            Bitmap gardenBmp = BitmapFactory.decodeResource(context.getResources(), com.example.myapplication.R.drawable.gardon);
            if (gardenBmp != null && targetW > 0 && targetH > 0) {
                Bitmap scaled = Bitmap.createScaledBitmap(gardenBmp, targetW, targetH, true);
                return BitmapDescriptorFactory.fromBitmap(scaled);
            }
        } catch (Exception ignored) {}
        // Fallback to raw resource if scaling failed
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.gardon);
    }
    
    /**
     * Clear current display
     */
    public void clearCurrentDisplay() {
        // Clear garden clustering
        clearGardenMarkers();
        
        // Clear plant markers
        clearPlantMarkers();
        
        // Clear data caches
        currentPlants.clear();
        currentGardens.clear();
        LogUtil.d(TAG, "clearCurrentDisplay: plants and gardens cleared and clusters refreshed");
    }
    
    /**
     * Handle plant marker click
     */
    public boolean handlePlantMarkerClick(Marker marker) {
        PlantMapDto plant = (PlantMapDto) marker.getTag();
        if (plant != null && plantClickListener != null) {
            plantClickListener.onPlantClick(plant);
            return true;
        }
        return false;
    }
    
    /**
     * Destroy manager
     */
    public void destroy() {
        clearCurrentDisplay();
    }

    /**
     * Initialize ClusterManager and bind click listeners
     */
    private void setupClusterManagerIfNeeded() {
        if (googleMap == null || plantClusterManager != null) return;

        plantClusterManager = new ClusterManager<>(context, googleMap);
        plantClusterRenderer = new DefaultClusterRenderer<>(context, googleMap, plantClusterManager) {
            @Override
            protected void onBeforeClusterItemRendered(PlantClusterItem item, MarkerOptions markerOptions) {
                // Do not set title/snippet to avoid default InfoWindow popup entirely
                markerOptions.icon(createPlantIcon());
            }

            @Override
            protected void onClusterItemUpdated(PlantClusterItem item, Marker marker) {
                // Ensure no title/snippet on update to prevent default InfoWindow
                marker.setTitle(null);
                marker.setSnippet(null);
                marker.setIcon(createPlantIcon());
                super.onClusterItemUpdated(item, marker);
            }

            @Override
            protected void onClusterItemRendered(PlantClusterItem clusterItem, Marker marker) {
                // Clear again after render to prevent default renderer writing back
                marker.setTitle(null);
                marker.setSnippet(null);
                super.onClusterItemRendered(clusterItem, marker);
            }
        };
        plantClusterManager.setRenderer(plantClusterRenderer);

        // Marker click: pass to callback so the upper layer shows the BottomSheet
        plantClusterManager.setOnClusterItemClickListener(clusterItem -> {
            LogUtil.d(TAG, "Plant cluster ITEM clicked: " + clusterItem.getPosition());
            if (plantClickListener != null) {
                plantClickListener.onPlantClick(clusterItem.getPlant());
                return true;
            }
            return false;
        });

        // Log only; do not change existing behavior
        plantClusterManager.setOnClusterClickListener(cluster -> {
            LogUtil.d(TAG, "Plant CLUSTER clicked, size=" + cluster.getSize());
            return false; // Do not intercept; keep default zoom behavior
        });

        // Delegate various map events to ClusterManager
        // Do not set listeners here; coordinator will attach composite listeners
    }

    public ClusterManager<PlantClusterItem> getPlantClusterManager() {
        return plantClusterManager;
    }

    /**
     * Initialize Garden ClusterManager and bind click listeners
     */
    private void setupGardenClusterManagerIfNeeded() {
        if (googleMap == null || gardenClusterManager != null) return;

        gardenClusterManager = new ClusterManager<>(context, googleMap);
        gardenClusterRenderer = new DefaultClusterRenderer<>(context, googleMap, gardenClusterManager) {
            @Override
            protected void onBeforeClusterItemRendered(GardenClusterItem item, MarkerOptions markerOptions) {
                // Do not set title/snippet to avoid default InfoWindow popup entirely
                markerOptions.icon(createGardenIcon());
            }

            @Override
            protected void onClusterItemUpdated(GardenClusterItem item, Marker marker) {
                // Ensure no title/snippet on update to prevent default InfoWindow
                marker.setTitle(null);
                marker.setSnippet(null);
                marker.setIcon(createGardenIcon());
                super.onClusterItemUpdated(item, marker);
            }

            @Override
            protected void onClusterItemRendered(GardenClusterItem clusterItem, Marker marker) {
                marker.setTitle(null);
                marker.setSnippet(null);
                super.onClusterItemRendered(clusterItem, marker);
            }
        };
        gardenClusterManager.setRenderer(gardenClusterRenderer);

        gardenClusterManager.setOnClusterItemClickListener(clusterItem -> {
            LogUtil.d(TAG, "Garden cluster ITEM clicked: " + clusterItem.getPosition());
            if (gardenClickListener != null) {
                gardenClickListener.onGardenClick(clusterItem.getGarden());
                return true;
            }
            return false;
        });

        gardenClusterManager.setOnClusterClickListener(cluster -> {
            LogUtil.d(TAG, "Garden CLUSTER clicked, size=" + cluster.getSize());
            return false;
        });
    }

    public ClusterManager<GardenClusterItem> getGardenClusterManager() {
        setupGardenClusterManagerIfNeeded();
        return gardenClusterManager;
    }

    /**
     * Display a single plant and focus the camera
     */
    public void displayAndFocusSinglePlant(PlantMapDto plant, float zoom) {
        if (plant == null || plant.getLatitude() == null || plant.getLongitude() == null || googleMap == null) return;
        // Clear existing plant clusters and show only this point
        clearPlantMarkers();
        currentPlants.clear();
        addPlantClusterItem(plant);
        currentPlants.add(plant);
        // Focus
        LatLng pos = new LatLng(plant.getLatitude(), plant.getLongitude());
        googleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(pos, zoom <= 0 ? 17f : zoom));
        // Directly trigger click callback to open BottomSheet
        if (plantClickListener != null) {
            plantClickListener.onPlantClick(plant);
        }
    }
}
