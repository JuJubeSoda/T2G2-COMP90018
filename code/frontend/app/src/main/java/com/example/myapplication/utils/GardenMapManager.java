package com.example.myapplication.utils;

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
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 花园和植物地图管理器
 * 提供两种方式显示花园和植物数据：GeoJSON图层和批量标记
 */
public class GardenMapManager {
    
    private static final String TAG = "GardenMapManager";
    
    private final Context context;
    private final GoogleMap googleMap;
    private GeoJsonLayer currentGeoJsonLayer;
    private List<Marker> currentMarkers = new ArrayList<>();
    
    // 回调接口
    public interface OnGardenClickListener {
        void onGardenClick(Garden garden);
    }
    
    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }
    
    private OnGardenClickListener gardenClickListener;
    private OnPlantClickListener plantClickListener;
    
    public GardenMapManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
    }
    
    public void setOnGardenClickListener(OnGardenClickListener listener) {
        this.gardenClickListener = listener;
    }
    
    public void setOnPlantClickListener(OnPlantClickListener listener) {
        this.plantClickListener = listener;
    }
    
    /**
     * 方法1：将花园JSON数据转换为GeoJSON并显示
     * 优势：使用GeoJSON图层，统一样式，批量处理，支持点击事件
     */
    public void displayGardensAsGeoJson(List<Garden> gardens) {
        try {
            // 清除现有图层
            clearCurrentDisplay();
            
            // 转换为GeoJSON格式
            JSONObject geoJson = convertGardensToGeoJson(gardens);
            
            // 创建GeoJSON图层
            GeoJsonLayer layer = new GeoJsonLayer(googleMap, geoJson);
            setupGeoJsonLayer(layer);
            
            currentGeoJsonLayer = layer;
            
            Log.d(TAG, "Successfully displayed " + gardens.size() + " gardens as GeoJSON layer");
            Toast.makeText(context, "Displayed " + gardens.size() + " gardens", Toast.LENGTH_SHORT).show();
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to convert gardens to GeoJSON", e);
            Toast.makeText(context, "Failed to display gardens", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 方法1：将植物JSON数据转换为GeoJSON并显示
     */
    public void displayPlantsAsGeoJson(List<Plant> plants) {
        try {
            // 清除现有图层
            clearCurrentDisplay();
            
            // 转换为GeoJSON格式
            JSONObject geoJson = convertPlantsToGeoJson(plants);
            
            // 创建GeoJSON图层
            GeoJsonLayer layer = new GeoJsonLayer(googleMap, geoJson);
            setupPlantGeoJsonLayer(layer);
            
            currentGeoJsonLayer = layer;
            
            Log.d(TAG, "Successfully displayed " + plants.size() + " plants as GeoJSON layer");
            Toast.makeText(context, "Displayed " + plants.size() + " plants", Toast.LENGTH_SHORT).show();
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to convert plants to GeoJSON", e);
            Toast.makeText(context, "Failed to display plants", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 方法2：批量添加标记显示花园
     * 优势：简单直接，无需格式转换
     */
    public void displayGardensAsMarkers(List<Garden> gardens) {
        // 清除现有标记
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
                    marker.setTag(garden); // 存储Garden对象用于点击事件
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
                
                features.put(feature);
            }
        }
        
        geoJson.put("features", features);
        return geoJson;
    }
    
    /**
     * 设置植物GeoJSON图层
     */
    private void setupPlantGeoJsonLayer(GeoJsonLayer layer) {
        // 应用样式
        applyStylesToPlantMarkers(layer);
        
        // 添加到地图
        layer.addLayerToMap();
        
        // 设置点击监听器
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
    private void setupGeoJsonLayer(GeoJsonLayer layer) {
        // 应用样式
        applyStylesToGardenMarkers(layer);
        
        // 添加到地图
        layer.addLayerToMap();
        
        // 设置点击监听器
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
                
                if (plantClickListener != null) {
                    plantClickListener.onPlantClick(plant);
                }
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
                
                if (gardenClickListener != null) {
                    gardenClickListener.onGardenClick(garden);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle garden feature click", e);
        }
    }
    
    /**
     * 应用植物标记样式
     */
    private void applyStylesToPlantMarkers(GeoJsonLayer geoJsonLayer) {
        for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            pointStyle.setIcon(createPlantIcon());
            feature.setPointStyle(pointStyle);
        }
    }
    
    /**
     * 应用花园标记样式
     */
    private void applyStylesToGardenMarkers(GeoJsonLayer geoJsonLayer) {
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
        // 使用花朵图标表示植物
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.flower);
    }
    
    /**
     * 创建花园图标
     */
    private BitmapDescriptor createGardenIcon() {
        // 使用植物图标表示花园
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
