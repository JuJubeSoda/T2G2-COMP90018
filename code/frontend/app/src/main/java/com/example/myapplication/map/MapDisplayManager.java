package com.example.myapplication.map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.model.Garden;
import com.example.myapplication.model.Plant;
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
    private List<Marker> currentMarkers = new ArrayList<>();
    
    // 回调接口
    public interface OnGardenMapClickListener {
        void onGardenClick(Garden garden);
    }
    
    public interface OnPlantMapClickListener {
        void onPlantClick(Plant plant);
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
     * 在地图上显示植物列表
     */
    public void displayPlantsOnMap(List<Plant> plants) {
        try {
            clearCurrentDisplay();
            
            JSONObject geoJson = convertPlantsToGeoJson(plants);
            GeoJsonLayer layer = new GeoJsonLayer(googleMap, geoJson);
            setupPlantGeoJsonLayer(layer);
            
            currentGeoJsonLayer = layer;
            
            Log.d(TAG, "Successfully displayed " + plants.size() + " plants on map");
            Toast.makeText(context, "Displayed " + plants.size() + " plants", Toast.LENGTH_SHORT).show();
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to display plants on map", e);
            Toast.makeText(context, "Failed to display plants", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 在地图上显示花园列表
     */
    public void displayGardensOnMap(List<Garden> gardens) {
        try {
            clearCurrentDisplay();
            
            JSONObject geoJson = convertGardensToGeoJson(gardens);
            GeoJsonLayer layer = new GeoJsonLayer(googleMap, geoJson);
            setupGardenGeoJsonLayer(layer);
            
            currentGeoJsonLayer = layer;
            
            Log.d(TAG, "Successfully displayed " + gardens.size() + " gardens on map");
            Toast.makeText(context, "Displayed " + gardens.size() + " gardens", Toast.LENGTH_SHORT).show();
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to display gardens on map", e);
            Toast.makeText(context, "Failed to display gardens", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 使用标记方式显示花园（备选方案）
     */
    public void displayGardensAsMarkers(List<Garden> gardens) {
        clearCurrentDisplay();
        
        for (Garden garden : gardens) {
            if (garden.getLatitude() != null && garden.getLongitude() != null) {
                LatLng position = new LatLng(garden.getLatitude(), garden.getLongitude());
                
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(garden.getName())
                        .snippet(garden.getDescription())
                        .icon(createGardenIcon());
                
                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    marker.setTag(garden);
                    currentMarkers.add(marker);
                }
            }
        }
        
        Log.d(TAG, "Successfully displayed " + currentMarkers.size() + " gardens as markers");
        Toast.makeText(context, "Displayed " + currentMarkers.size() + " gardens", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 将植物列表转换为GeoJSON格式
     */
    private JSONObject convertPlantsToGeoJson(List<Plant> plants) throws JSONException {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");
        
        JSONArray features = new JSONArray();
        
        for (Plant plant : plants) {
            if (plant.getLatitude() != null && plant.getLongitude() != null) {
                JSONObject feature = createPlantFeature(plant);
                features.put(feature);
            }
        }
        
        geoJson.put("features", features);
        return geoJson;
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
     * 创建植物GeoJSON特征
     */
    private JSONObject createPlantFeature(Plant plant) throws JSONException {
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");
        
        // 几何信息
        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        JSONArray coordinates = new JSONArray();
        coordinates.put(plant.getLongitude()); // GeoJSON使用[经度, 纬度]顺序
        coordinates.put(plant.getLatitude());
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);
        
        // 属性信息
        JSONObject properties = new JSONObject();
        properties.put("plantId", plant.getPlantId());
        properties.put("name", plant.getName());
        properties.put("description", plant.getDescription());
        properties.put("scientificName", plant.getScientificName());
        properties.put("latitude", plant.getLatitude());
        properties.put("longitude", plant.getLongitude());
        properties.put("gardenId", plant.getGardenId());
        properties.put("isFavourite", plant.isFavourite());
        feature.put("properties", properties);
        
        return feature;
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
     * 设置植物GeoJSON图层
     */
    private void setupPlantGeoJsonLayer(GeoJsonLayer layer) {
        applyPlantMarkerStyles(layer);
        layer.addLayerToMap();
        
        layer.setOnFeatureClickListener(new GeoJsonLayer.OnFeatureClickListener() {
            @Override
            public void onFeatureClick(Feature feature) {
                Log.d(TAG, "Plant GeoJSON feature clicked!");
                handlePlantFeatureClick(feature);
            }
        });
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
     * 处理植物GeoJSON特征点击事件
     */
    private void handlePlantFeatureClick(Feature feature) {
        try {
            Plant plant = createPlantFromFeature(feature);
            if (plant != null && plantClickListener != null) {
                plantClickListener.onPlantClick(plant);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle plant feature click", e);
        }
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
     * 从GeoJSON特征创建Plant对象
     */
    private Plant createPlantFromFeature(Feature feature) {
        try {
            String plantIdStr = feature.getProperty("plantId");
            String name = feature.getProperty("name");
            String description = feature.getProperty("description");
            String scientificName = feature.getProperty("scientificName");
            String latitudeStr = feature.getProperty("latitude");
            String longitudeStr = feature.getProperty("longitude");
            String gardenIdStr = feature.getProperty("gardenId");
            String isFavouriteStr = feature.getProperty("isFavourite");
            
            if (plantIdStr != null && latitudeStr != null && longitudeStr != null) {
                Plant plant = new Plant();
                plant.setPlantId(Long.parseLong(plantIdStr));
                plant.setName(name);
                plant.setDescription(description);
                plant.setScientificName(scientificName);
                plant.setLatitude(Double.parseDouble(latitudeStr));
                plant.setLongitude(Double.parseDouble(longitudeStr));
                if (gardenIdStr != null) {
                    plant.setGardenId(Long.parseLong(gardenIdStr));
                }
                if (isFavouriteStr != null) {
                    plant.setFavourite(Boolean.parseBoolean(isFavouriteStr));
                }
                return plant;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create plant from feature", e);
        }
        return null;
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
     * 应用植物标记样式
     */
    private void applyPlantMarkerStyles(GeoJsonLayer geoJsonLayer) {
        for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            pointStyle.setIcon(createPlantIcon());
            feature.setPointStyle(pointStyle);
        }
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
        // 清除GeoJSON图层
        if (currentGeoJsonLayer != null) {
            currentGeoJsonLayer.removeLayerFromMap();
            currentGeoJsonLayer = null;
        }
        
        // 清除标记
        for (Marker marker : currentMarkers) {
            marker.remove();
        }
        currentMarkers.clear();
    }
    
    /**
     * 销毁管理器
     */
    public void destroy() {
        clearCurrentDisplay();
    }
}
