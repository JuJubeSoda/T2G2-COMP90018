package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantMapDto;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 地图显示管理器 - 负责在地图上显示植物和花园数据
 * 统一管理地图显示相关的功能，包括GeoJSON图层和标记
 */
public class MapDisplayManager {
    
    private static final String TAG = "MapDisplayManager";
    
    private final Context context;
    private final GoogleMap googleMap;
    private GeoJsonLayer currentGeoJsonLayer;
    // Clustering for plants
    private ClusterManager<PlantClusterItem> plantClusterManager;
    private DefaultClusterRenderer<PlantClusterItem> plantClusterRenderer;
    private List<PlantClusterItem> currentPlantItems = new ArrayList<>();
    private List<Marker> currentGardenMarkers = new ArrayList<>();
    private List<PlantMapDto> currentPlants = new ArrayList<>();
    private List<GardenDto> currentGardens = new ArrayList<>();
    
    // 回调接口
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
        setupClusterManagerIfNeeded();
    }
    
    public void setOnGardenClickListener(OnGardenMapClickListener listener) {
        this.gardenClickListener = listener;
    }
    
    public void setOnPlantClickListener(OnPlantMapClickListener listener) {
        this.plantClickListener = listener;
    }
    
    /**
     * 在地图上显示植物列表 - 使用Marker方式（适合频繁刷新）
     */
    public void displayPlantsOnMap(List<PlantMapDto> plants) {
        Log.d(TAG, "=== Display Plants Debug ===");
        Log.d(TAG, "Received plants list: " + (plants == null ? "null" : "size=" + plants.size()));
        Log.d(TAG, "GoogleMap instance: " + (googleMap == null ? "null" : "available"));
        
        if (plants != null) {
            for (int i = 0; i < plants.size(); i++) {
                PlantMapDto plant = plants.get(i);
                Log.d(TAG, "Plant " + i + ": " + plant.toString());
                Log.d(TAG, "  - Name: " + plant.getName());
                Log.d(TAG, "  - Latitude: " + plant.getLatitude());
                Log.d(TAG, "  - Longitude: " + plant.getLongitude());
                Log.d(TAG, "  - PlantId: " + plant.getPlantId());
            }
        }
        
        Log.d(TAG, "Clearing existing plant items...");
        clearPlantMarkers();
        currentPlants.clear();
        currentPlantItems.clear();
        
        int validPlants = 0;
        if (plants != null) {
            for (PlantMapDto plant : plants) {
                if (plant.getLatitude() != null && plant.getLongitude() != null) {
                    Log.d(TAG, "Adding marker for plant: " + plant.getName() + " at (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
                    try {
                        addPlantClusterItem(plant);
                        currentPlants.add(plant);
                        validPlants++;
                        Log.d(TAG, "Successfully added marker for: " + plant.getName());
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to add marker for plant: " + plant.getName(), e);
                    }
                } else {
                    Log.w(TAG, "Skipping plant with null coordinates: " + plant.getName());
                }
            }
        }
        
        Log.d(TAG, "Final cluster item count: " + currentPlantItems.size());
        Log.d(TAG, "Valid plants: " + validPlants + " out of " + (plants != null ? plants.size() : 0));
        Log.d(TAG, "Current plants list size: " + currentPlants.size());
        
        // 检查地图状态
        if (googleMap != null) {
            Log.d(TAG, "GoogleMap is ready, camera position: " + googleMap.getCameraPosition());
            Log.d(TAG, "GoogleMap is ready, visible region: " + googleMap.getProjection().getVisibleRegion());
        } else {
            Log.e(TAG, "GoogleMap is null! Cannot display markers.");
        }
        
        // UI feedback should be handled at higher layer
        Log.d(TAG, "=== End Display Plants Debug ===");
    }
    
    /**
     * 增量更新植物显示（添加新植物）
     */
    public void addNewPlants(List<PlantMapDto> newPlants) {
        for (PlantMapDto plant : newPlants) {
            if (plant.getLatitude() != null && plant.getLongitude() != null && !currentPlants.contains(plant)) {
                addPlantClusterItem(plant);
                currentPlants.add(plant);
            }
        }
        
        Log.d(TAG, "Added " + newPlants.size() + " new plants to map");
    }
    
    /**
     * 移除植物标记
     */
    public void removePlants(List<PlantMapDto> plantsToRemove) {
        for (PlantMapDto plant : plantsToRemove) {
            removePlantMarker(plant);
            currentPlants.remove(plant);
        }
        
        Log.d(TAG, "Removed " + plantsToRemove.size() + " plants from map");
    }
    
    /**
     * 添加单个植物标记
     */
    private void addPlantMarker(PlantMapDto plant) {
        Log.d(TAG, "=== Add Plant Marker Debug ===");
        Log.d(TAG, "Plant: " + plant.getName());
        Log.d(TAG, "Coordinates: (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
        Log.d(TAG, "GoogleMap null check: " + (googleMap == null));
        
        if (googleMap == null) {
            Log.e(TAG, "GoogleMap is null! Cannot add marker.");
            return;
        }
        
        LatLng position = new LatLng(plant.getLatitude(), plant.getLongitude());
        Log.d(TAG, "Created LatLng: " + position);
        
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(plant.getName())
                .snippet(plant.getDescription())
                .icon(createPlantIcon());
        
        Log.d(TAG, "MarkerOptions created: " + markerOptions);
        
        Marker marker = googleMap.addMarker(markerOptions);
        Log.d(TAG, "Marker created: " + (marker == null ? "null" : "success"));
        
        if (marker != null) {
            marker.setTag(plant);
            // Legacy path (kept for potential fallback)
            
            // 设置点击监听器
            setupPlantMarkerClickListener(marker);
            Log.d(TAG, "Marker click listener set");
        } else {
            Log.e(TAG, "Failed to create marker for plant: " + plant.getName());
        }
        Log.d(TAG, "=== End Add Plant Marker Debug ===");
    }

    /**
     * 添加Cluster item并刷新聚合
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
     * 移除单个植物标记
     */
    private void removePlantMarker(PlantMapDto plant) {
        if (plantClusterManager == null || currentPlantItems.isEmpty()) return;
        for (int i = currentPlantItems.size() - 1; i >= 0; i--) {
            PlantClusterItem item = currentPlantItems.get(i);
            if (item.getPlant().getPlantId() == plant.getPlantId()) {
                currentPlantItems.remove(i);
                plantClusterManager.removeItem(item);
                break;
            }
        }
        plantClusterManager.cluster();
    }
    
    /**
     * 设置植物标记点击监听器
     */
    private void setupPlantMarkerClickListener(Marker marker) {
        // 点击监听器通过GoogleMap的setOnMarkerClickListener统一处理
    }
    
    /**
     * 在地图上显示花园列表 - 使用GeoJSON方式（适合静态数据）
     */
    public void displayGardensOnMap(List<GardenDto> gardens) {
        try {
            clearGardenLayer();
            currentGardens.clear();
            
            JSONObject geoJson = convertGardensToGeoJson(gardens);
            GeoJsonLayer layer = new GeoJsonLayer(googleMap, geoJson);
            setupGardenGeoJsonLayer(layer);
            
            currentGeoJsonLayer = layer;
            currentGardens.addAll(gardens);
            
            Log.d(TAG, "Successfully displayed " + gardens.size() + " gardens on map");
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to display gardens on map", e);
        }
    }
    
    /**
     * 清除植物标记
     */
    public void clearPlantMarkers() {
        // Clear clustering items
        if (plantClusterManager != null) {
            plantClusterManager.clearItems();
        }
        currentPlantItems.clear();
        // Legacy markers list is no longer used
    }
    
    /**
     * 清除花园GeoJSON图层
     */
    public void clearGardenLayer() {
        if (currentGeoJsonLayer != null) {
            currentGeoJsonLayer.removeLayerFromMap();
            currentGeoJsonLayer = null;
        }
    }
    
    
    /**
     * 将花园列表转换为GeoJSON格式
     */
    private JSONObject convertGardensToGeoJson(List<GardenDto> gardens) throws JSONException {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");
        
        JSONArray features = new JSONArray();
        
        for (GardenDto garden : gardens) {
            if (garden.getLatitude() != null && garden.getLongitude() != null) {
                JSONObject feature = createGardenFeature(garden);
                features.put(feature);
            }
        }
        
        geoJson.put("features", features);
        return geoJson;
    }
    
    
    /**
     * 创建花园GeoJSON特征
     */
    private JSONObject createGardenFeature(GardenDto garden) throws JSONException {
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");
        
        // 几何信息
        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        JSONArray coordinates = new JSONArray();
        coordinates.put(garden.getLongitude()); // GeoJSON使用[经度, 纬度]顺序
        coordinates.put(garden.getLatitude());
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);
        
        // 属性信息
        JSONObject properties = new JSONObject();
        properties.put("gardenId", garden.getGardenId());
        properties.put("name", garden.getName());
        properties.put("description", garden.getDescription());
        properties.put("latitude", garden.getLatitude());
        properties.put("longitude", garden.getLongitude());
        feature.put("properties", properties);
        
        return feature;
    }
    
    
    /**
     * 设置花园GeoJSON图层
     */
    private void setupGardenGeoJsonLayer(GeoJsonLayer layer) {
        applyGardenMarkerStyles(layer);
        layer.addLayerToMap();
        
        layer.setOnFeatureClickListener(new GeoJsonLayer.OnFeatureClickListener() {
            @Override
            public void onFeatureClick(Feature feature) {
                Log.d(TAG, "Garden GeoJSON feature clicked!");
                handleGardenFeatureClick(feature);
            }
        });
    }
    
    
    /**
     * 处理花园GeoJSON特征点击事件
     */
    private void handleGardenFeatureClick(Feature feature) {
        try {
            GardenDto garden = createGardenFromFeature(feature);
            if (garden != null && gardenClickListener != null) {
                gardenClickListener.onGardenClick(garden);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle garden feature click", e);
        }
    }
    
    
    /**
     * 从GeoJSON特征创建Garden对象
     */
    private GardenDto createGardenFromFeature(Feature feature) {
        try {
            String gardenIdStr = feature.getProperty("gardenId");
            String name = feature.getProperty("name");
            String description = feature.getProperty("description");
            String latitudeStr = feature.getProperty("latitude");
            String longitudeStr = feature.getProperty("longitude");
            
            if (gardenIdStr != null && latitudeStr != null && longitudeStr != null) {
                GardenDto garden = new GardenDto();
                garden.setGardenId(Long.parseLong(gardenIdStr));
                garden.setName(name);
                garden.setDescription(description);
                garden.setLatitude(Double.parseDouble(latitudeStr));
                garden.setLongitude(Double.parseDouble(longitudeStr));
                return garden;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create garden from feature", e);
        }
        return null;
    }
    
    
    /**
     * 应用花园标记样式
     */
    private void applyGardenMarkerStyles(GeoJsonLayer geoJsonLayer) {
        for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            pointStyle.setIcon(createGardenIcon());
            feature.setPointStyle(pointStyle);
        }
    }
    
    /**
     * 创建植物图标
     */
    private BitmapDescriptor createPlantIcon() {
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.flower);
    }
    
    /**
     * 创建花园图标
     */
    private BitmapDescriptor createGardenIcon() {
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.plantbulb_background);
    }
    
    /**
     * 清除当前显示
     */
    public void clearCurrentDisplay() {
        // 清除花园GeoJSON图层
        clearGardenLayer();
        
        // 清除植物标记
        clearPlantMarkers();
        
        // 清除数据缓存
        currentPlants.clear();
        currentGardens.clear();
    }
    
    /**
     * 处理植物标记点击事件
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
     * 销毁管理器
     */
    public void destroy() {
        clearCurrentDisplay();
    }

    /**
     * 初始化ClusterManager并绑定点击监听
     */
    private void setupClusterManagerIfNeeded() {
        if (googleMap == null || plantClusterManager != null) return;

        plantClusterManager = new ClusterManager<>(context, googleMap);
        plantClusterRenderer = new DefaultClusterRenderer<>(context, googleMap, plantClusterManager) {
            @Override
            protected void onBeforeClusterItemRendered(PlantClusterItem item, MarkerOptions markerOptions) {
                markerOptions.title(item.getTitle())
                        .snippet(item.getSnippet())
                        .icon(createPlantIcon());
            }
        };
        plantClusterManager.setRenderer(plantClusterRenderer);

        // Marker点击：传递到回调，让上层展示BottomSheet
        plantClusterManager.setOnClusterItemClickListener(clusterItem -> {
            if (plantClickListener != null) {
                plantClickListener.onPlantClick(clusterItem.getPlant());
                return true;
            }
            return false;
        });

        // 将地图的各种事件委托给ClusterManager
        // Do not set listeners here; coordinator will attach composite listeners
    }

    public ClusterManager<PlantClusterItem> getPlantClusterManager() {
        return plantClusterManager;
    }
}
