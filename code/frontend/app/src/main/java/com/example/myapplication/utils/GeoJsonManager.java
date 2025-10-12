package com.example.myapplication.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 共享的GeoJSON管理工具类，用于处理GeoJSON数据的加载、缓存和显示
 * 为PlantMapFragment提供统一的GeoJSON数据处理功能
 */
public class GeoJsonManager {
    
    private static final String TAG = "GeoJsonManager";
    
    // Cache related constants
    private static final String CACHE_FILE_NAME = "earthquake_data_cache.json";
    private static final long CACHE_VALIDITY_PERIOD = 60000; // 1 minute cache validity
    
    // Context and map references
    private final Context context;
    private final GoogleMap googleMap;
    private GeoJsonLayer currentLayer;
    
    // Cache management
    private long lastCacheUpdate = 0;
    
    // Callback interface for feature clicks
    public interface OnFeatureClickListener {
        void onFeatureClick(String magnitude, String location, String time, 
                           double latitude, double longitude);
    }
    
    private OnFeatureClickListener featureClickListener;
    
    public GeoJsonManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
    }
    
    /**
     * 设置特征点击监听器
     */
    public void setOnFeatureClickListener(OnFeatureClickListener listener) {
        this.featureClickListener = listener;
    }
    
    /**
     * 加载GeoJSON数据，优先从缓存，然后远程URL，最后本地资源
     */
    public void loadGeoJsonData(String remoteUrl, int localResourceId) {
        if (isCacheValid()) {
            loadFromCache();
        } else {
            loadFromRemoteUrl(remoteUrl, localResourceId);
        }
    }
    
    /**
     * 从远程URL加载GeoJSON数据
     */
    public void loadFromRemoteUrl(String url, int fallbackResourceId) {
        new LoadGeoJsonFromRemoteTask(fallbackResourceId).execute(url);
    }
    
    /**
     * 从本地资源加载GeoJSON数据
     */
    public void loadFromResource(int resourceId) {
        try {
            GeoJsonLayer layer = new GeoJsonLayer(googleMap, resourceId, context);
            setupGeoJsonLayer(layer);
        } catch (IOException e) {
            Log.e(TAG, "GeoJSON file could not be read", e);
        } catch (JSONException e) {
            Log.e(TAG, "GeoJSON file could not be converted to a JSONObject", e);
        }
    }
    
    /**
     * 设置GeoJSON图层
     */
    private void setupGeoJsonLayer(GeoJsonLayer layer) {
        // 移除现有图层
        if (currentLayer != null) {
            currentLayer.removeLayerFromMap();
        }
        
        currentLayer = layer;
        
        // 应用样式
        applyStylesToMarkers(layer);
        
        // 添加到地图
        layer.addLayerToMap();
        
        // 设置点击监听器
        layer.setOnFeatureClickListener(new GeoJsonLayer.OnFeatureClickListener() {
            @Override
            public void onFeatureClick(Feature feature) {
                Log.d(TAG, "GeoJSON feature clicked!");
                handleFeatureClick(feature);
            }
        });
    }
    
    /**
     * 处理特征点击事件
     */
    private void handleFeatureClick(Feature feature) {
        // 提取地震信息
        String magnitude = feature.getProperty("mag");
        String location = feature.getProperty("place");
        String time = feature.getProperty("time");
        
        // 获取坐标
        double latitude = 0.0;
        double longitude = 0.0;
        
        if (feature.getGeometry() != null && feature.getGeometry().getGeometryType().equals("Point")) {
            com.google.maps.android.data.geojson.GeoJsonPoint point =
                (com.google.maps.android.data.geojson.GeoJsonPoint) feature.getGeometry();
            latitude = point.getCoordinates().latitude;
            longitude = point.getCoordinates().longitude;
        }
        
        // 格式化时间
        String formattedTime = formatTime(time);
        
        // 回调给监听器
        if (featureClickListener != null) {
            featureClickListener.onFeatureClick(
                magnitude != null ? magnitude : "Unknown",
                location != null ? location : "Unknown location",
                formattedTime,
                latitude,
                longitude
            );
        }
    }
    
    /**
     * 格式化时间戳
     */
    private String formatTime(String time) {
        if (time == null) return "Unknown";
        
        try {
            long timestamp = Long.parseLong(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date(timestamp));
        } catch (NumberFormatException e) {
            return time; // 如果解析失败，返回原始时间
        }
    }
    
    /**
     * 应用标记样式
     */
    private void applyStylesToMarkers(GeoJsonLayer geoJsonLayer) {
        for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            
            // 根据震级设置图标
            String magnitudeStr = feature.getProperty("mag");
            if (magnitudeStr != null) {
                try {
                    double magnitude = Double.parseDouble(magnitudeStr);
                    BitmapDescriptor icon = createMagnitudeIcon(magnitude);
                    pointStyle.setIcon(icon);
                } catch (NumberFormatException e) {
                    pointStyle.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            } else {
                pointStyle.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            
            feature.setPointStyle(pointStyle);
        }
    }
    
    /**
     * 根据震级创建图标
     */
    private BitmapDescriptor createMagnitudeIcon(double magnitude) {
        // 使用flower图标表示地震点
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.flower);
    }
    
    /**
     * 检查缓存是否有效
     */
    private boolean isCacheValid() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastCacheUpdate) < CACHE_VALIDITY_PERIOD;
    }
    
    /**
     * 从缓存加载
     */
    private void loadFromCache() {
        try {
            File cacheFile = new File(context.getCacheDir(), CACHE_FILE_NAME);
            if (cacheFile.exists()) {
                FileInputStream fis = new FileInputStream(cacheFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                
                reader.close();
                fis.close();
                
                // 从缓存数据创建GeoJsonLayer
                GeoJsonLayer cachedLayer = new GeoJsonLayer(googleMap, new JSONObject(result.toString()));
                setupGeoJsonLayer(cachedLayer);
                
                Log.d(TAG, "Successfully loaded GeoJSON data from cache");
            } else {
                Log.d(TAG, "Cache file doesn't exist");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load from cache", e);
        }
    }
    
    /**
     * 保存到缓存
     */
    public void saveToCache(String jsonData) {
        try {
            File cacheFile = new File(context.getCacheDir(), CACHE_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(cacheFile);
            fos.write(jsonData.getBytes());
            fos.close();
            
            lastCacheUpdate = System.currentTimeMillis();
            Log.d(TAG, "Successfully saved GeoJSON data to cache");
        } catch (Exception e) {
            Log.e(TAG, "Failed to save to cache", e);
        }
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        try {
            File cacheFile = new File(context.getCacheDir(), CACHE_FILE_NAME);
            if (cacheFile.exists()) {
                cacheFile.delete();
                lastCacheUpdate = 0;
                Log.d(TAG, "Cache cleared");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cache", e);
        }
    }
    
    /**
     * 移除当前图层
     */
    public void removeCurrentLayer() {
        if (currentLayer != null) {
            currentLayer.removeLayerFromMap();
            currentLayer = null;
        }
    }
    
    /**
     * 异步任务：从远程URL加载GeoJSON
     */
    private class LoadGeoJsonFromRemoteTask extends AsyncTask<String, Void, GeoJsonLayer> {
        private String jsonData;
        private final int fallbackResourceId;
        
        public LoadGeoJsonFromRemoteTask(int fallbackResourceId) {
            this.fallbackResourceId = fallbackResourceId;
        }
        
        @Override
        protected GeoJsonLayer doInBackground(String... urls) {
            try {
                String geojsonUrl = urls[0];
                Log.d(TAG, "Loading GeoJSON from URL: " + geojsonUrl);
                
                // 从URL打开流
                @SuppressWarnings("deprecation")
                InputStream stream = new URL(geojsonUrl).openStream();
                
                String line;
                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                
                reader.close();
                stream.close();
                
                // 存储JSON数据用于缓存
                jsonData = result.toString();
                
                // 从JSON创建GeoJsonLayer
                return new GeoJsonLayer(googleMap, new JSONObject(jsonData));
                
            } catch (IOException e) {
                Log.e(TAG, "Failed to load GeoJSON from remote URL", e);
                return null;
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse GeoJSON from remote URL", e);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(GeoJsonLayer geoJsonLayer) {
            if (geoJsonLayer != null) {
                setupGeoJsonLayer(geoJsonLayer);
                
                // 保存到缓存
                if (jsonData != null) {
                    saveToCache(jsonData);
                }
                
                Log.d(TAG, "Successfully loaded and updated GeoJSON data from remote source");
                Toast.makeText(context, "Map data updated", Toast.LENGTH_SHORT).show();
            } else {
                // 回退到本地资源
                Log.w(TAG, "Remote loading failed, falling back to local resource");
                loadFromResource(fallbackResourceId);
            }
        }
    }
}
