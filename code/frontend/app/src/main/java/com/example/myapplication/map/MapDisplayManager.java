package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.model.Garden;
import com.example.myapplication.network.PlantDto;
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
    private List<Marker> currentPlantMarkers = new ArrayList<>();
    private List<Marker> currentGardenMarkers = new ArrayList<>();
    private List<PlantDto> currentPlants = new ArrayList<>();
    private List<Garden> currentGardens = new ArrayList<>();
    
    // 回调接口
    public interface OnGardenMapClickListener {
        void onGardenClick(Garden garden);
    }
    
    public interface OnPlantMapClickListener {
        void onPlantClick(PlantDto plant);
    }
    
    private OnGardenMapClickListener gardenClickListener;
    private OnPlantMapClickListener plantClickListener;
    
    public MapDisplayManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
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
    public void displayPlantsOnMap(List<PlantDto> plants) {
        Log.d(TAG, "=== Display Plants Debug ===");
        Log.d(TAG, "Received plants list: " + (plants == null ? "null" : "size=" + plants.size()));
        Log.d(TAG, "GoogleMap instance: " + (googleMap == null ? "null" : "available"));
        
        if (plants != null) {
            for (int i = 0; i < plants.size(); i++) {
                PlantDto plant = plants.get(i);
                Log.d(TAG, "Plant " + i + ": " + plant.toString());
                Log.d(TAG, "  - Name: " + plant.getName());
                Log.d(TAG, "  - Latitude: " + plant.getLatitude());
                Log.d(TAG, "  - Longitude: " + plant.getLongitude());
                Log.d(TAG, "  - PlantId: " + plant.getPlantId());
            }
        }
        
        Log.d(TAG, "Clearing existing markers...");
        clearPlantMarkers();
        currentPlants.clear();
        
        int validPlants = 0;
        if (plants != null) {
            for (PlantDto plant : plants) {
                if (plant.getLatitude() != null && plant.getLongitude() != null) {
                    Log.d(TAG, "Adding marker for plant: " + plant.getName() + " at (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
                    try {
                        addPlantMarker(plant);
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
        
        Log.d(TAG, "Final marker count: " + currentPlantMarkers.size());
        Log.d(TAG, "Valid plants: " + validPlants + " out of " + (plants != null ? plants.size() : 0));
        Log.d(TAG, "Current plants list size: " + currentPlants.size());
        
        // 检查地图状态
        if (googleMap != null) {
            Log.d(TAG, "GoogleMap is ready, camera position: " + googleMap.getCameraPosition());
            Log.d(TAG, "GoogleMap is ready, visible region: " + googleMap.getProjection().getVisibleRegion());
        } else {
            Log.e(TAG, "GoogleMap is null! Cannot display markers.");
        }
        
        Toast.makeText(context, "Displayed " + currentPlantMarkers.size() + " plants", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "=== End Display Plants Debug ===");
    }
    
    /**
     * 增量更新植物显示（添加新植物）
     */
    public void addNewPlants(List<PlantDto> newPlants) {
        for (PlantDto plant : newPlants) {
            if (plant.getLatitude() != null && plant.getLongitude() != null && !currentPlants.contains(plant)) {
                addPlantMarker(plant);
                currentPlants.add(plant);
            }
        }
        
        Log.d(TAG, "Added " + newPlants.size() + " new plants to map");
    }
    
    /**
     * 移除植物标记
     */
    public void removePlants(List<PlantDto> plantsToRemove) {
        for (PlantDto plant : plantsToRemove) {
            removePlantMarker(plant);
            currentPlants.remove(plant);
        }
        
        Log.d(TAG, "Removed " + plantsToRemove.size() + " plants from map");
    }
    
    /**
     * 添加单个植物标记
     */
    private void addPlantMarker(PlantDto plant) {
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
            currentPlantMarkers.add(marker);
            Log.d(TAG, "Marker added to list. Total markers: " + currentPlantMarkers.size());
            
            // 设置点击监听器
            setupPlantMarkerClickListener(marker);
            Log.d(TAG, "Marker click listener set");
        } else {
            Log.e(TAG, "Failed to create marker for plant: " + plant.getName());
        }
        Log.d(TAG, "=== End Add Plant Marker Debug ===");
    }
    
    /**
     * 移除单个植物标记
     */
    private void removePlantMarker(PlantDto plant) {
        for (int i = currentPlantMarkers.size() - 1; i >= 0; i--) {
            Marker marker = currentPlantMarkers.get(i);
            PlantDto markerPlant = (PlantDto) marker.getTag();
            if (markerPlant != null && markerPlant.getPlantId() == plant.getPlantId()) {
                marker.remove();
                currentPlantMarkers.remove(i);
                break;
            }
        }
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
    public void displayGardensOnMap(List<Garden> gardens) {
        try {
            clearGardenLayer();
            currentGardens.clear();
            
            JSONObject geoJson = convertGardensToGeoJson(gardens);
            GeoJsonLayer layer = new GeoJsonLayer(googleMap, geoJson);
            setupGardenGeoJsonLayer(layer);
            
            currentGeoJsonLayer = layer;
            currentGardens.addAll(gardens);
            
            Log.d(TAG, "Successfully displayed " + gardens.size() + " gardens on map");
            Toast.makeText(context, "Displayed " + gardens.size() + " gardens", Toast.LENGTH_SHORT).show();
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to display gardens on map", e);
            Toast.makeText(context, "Failed to display gardens", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 清除植物标记
     */
    public void clearPlantMarkers() {
        for (Marker marker : currentPlantMarkers) {
            marker.remove();
        }
        currentPlantMarkers.clear();
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
    private JSONObject convertGardensToGeoJson(List<Garden> gardens) throws JSONException {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");
        
        JSONArray features = new JSONArray();
        
        for (Garden garden : gardens) {
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
    private JSONObject createGardenFeature(Garden garden) throws JSONException {
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
            Garden garden = createGardenFromFeature(feature);
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
    private Garden createGardenFromFeature(Feature feature) {
        try {
            String gardenIdStr = feature.getProperty("gardenId");
            String name = feature.getProperty("name");
            String description = feature.getProperty("description");
            String latitudeStr = feature.getProperty("latitude");
            String longitudeStr = feature.getProperty("longitude");
            
            if (gardenIdStr != null && latitudeStr != null && longitudeStr != null) {
                Garden garden = new Garden();
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
        PlantDto plant = (PlantDto) marker.getTag();
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
}
