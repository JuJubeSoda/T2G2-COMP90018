package com.example.myapplication.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * 地图位置管理器 - 负责处理地图位置相关的功能
 * 包括权限管理、位置获取、地图定位等
 */
public class MapLocationManager {
    
    private static final String TAG = "MapLocationManager";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    
    // 默认位置（悉尼）
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    
    // 默认搜索半径
    private static final int DEFAULT_SEARCH_RADIUS = 1000; // meters
    
    private final android.content.Context context;
    private final GoogleMap googleMap;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    
    private boolean locationPermissionGranted = false;
    private Location lastKnownLocation;
    
    public MapLocationManager(android.content.Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    /**
     * 请求位置权限
     */
    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            // 权限请求需要由Fragment处理，这里只设置状态
            locationPermissionGranted = false;
        }
    }
    
    /**
     * 处理权限请求结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }
    
    /**
     * 更新地图位置UI设置
     */
    public void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        
        try {
            if (locationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.getUiSettings().setAllGesturesEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.setPadding(0, 0, 20, 100);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                googleMap.setPadding(0, 0, 0, 0);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取设备当前位置
     */
    public void getDeviceLocation(OnLocationResultCallback callback) {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                LatLng currentLocation = new LatLng(
                                    lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude()
                                );
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
                                callback.onLocationSuccess(lastKnownLocation);
                            } else {
                                moveToDefaultLocation();
                                callback.onLocationError("Current location is null");
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            moveToDefaultLocation();
                            callback.onLocationError("Failed to get location: " + task.getException());
                        }
                    }
                });
            } else {
                moveToDefaultLocation();
                callback.onLocationError("Location permission not granted");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage(), e);
            callback.onLocationError("Security exception: " + e.getMessage());
        }
    }
    
    /**
     * 移动到默认位置
     */
    private void moveToDefaultLocation() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }
    
    /**
     * 检查是否有位置权限
     */
    public boolean hasLocationPermission() {
        return locationPermissionGranted;
    }
    
    /**
     * 获取最后已知位置
     */
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
    
    /**
     * 获取默认位置
     */
    public LatLng getDefaultLocation() {
        return defaultLocation;
    }
    
    /**
     * 获取默认缩放级别
     */
    public int getDefaultZoom() {
        return DEFAULT_ZOOM;
    }
    
    /**
     * 位置结果回调接口
     */
    public interface OnLocationResultCallback {
        void onLocationSuccess(Location location);
        void onLocationError(String error);
    }
    
    /**
     * 获取权限请求码
     */
    public static int getLocationPermissionRequestCode() {
        return PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    }
    
    // ==================== 地图半径相关功能 ====================
    
    // 地图半径变化监听器
    private OnMapRadiusChangeListener radiusChangeListener;
    
    /**
     * 获取当前地图视图的搜索半径（基于可见区域）
     */
    public int getCurrentMapRadius() {
        if (googleMap == null) {
            return getDefaultSearchRadius();
        }
        
        try {
            // 获取地图投影和可见区域
            VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
            LatLngBounds bounds = visibleRegion.latLngBounds;
            
            // 计算中心点
            LatLng center = bounds.getCenter();
            LatLng northeast = bounds.northeast;
            
            // 计算对角线距离的一半作为半径
            float[] results = new float[1];
            Location.distanceBetween(
                center.latitude, center.longitude,
                northeast.latitude, northeast.longitude,
                results
            );
            
            int radius = (int) (results[0] / 2);
            
            // 设置合理的半径范围（最小100米，最大10000米）
            radius = Math.max(100, Math.min(radius, 10000));
            
            Log.d(TAG, "Calculated map radius: " + radius + " meters");
            return radius;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate map radius", e);
            return getDefaultSearchRadius();
        }
    }
    
    /**
     * 根据缩放级别获取预设半径
     */
    public int getRadiusByZoom(float zoom) {
        if (zoom >= 18) return 100;      // 街道级别 - 100米
        if (zoom >= 16) return 200;       // 社区级别 - 200米
        if (zoom >= 14) return 500;       // 区域级别 - 500米
        if (zoom >= 12) return 1000;      // 城市级别 - 1公里
        if (zoom >= 10) return 2000;      // 州级别 - 2公里
        if (zoom >= 8) return 5000;       // 国家级别 - 5公里
        return 10000;                     // 大陆级别 - 10公里
    }
    
    /**
     * 获取当前相机位置的搜索半径
     */
    public int getCurrentCameraRadius() {
        if (googleMap == null) {
            return getDefaultSearchRadius();
        }
        
        try {
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            return getRadiusByZoom(cameraPosition.zoom);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get camera radius", e);
            return getDefaultSearchRadius();
        }
    }
    
    /**
     * 获取默认搜索半径
     */
    public int getDefaultSearchRadius() {
        return DEFAULT_SEARCH_RADIUS;
    }
    
    /**
     * 设置地图半径变化监听器
     */
    public void setOnMapRadiusChangeListener(OnMapRadiusChangeListener listener) {
        this.radiusChangeListener = listener;
        setupMapRadiusListeners();
    }
    
    /**
     * 设置地图半径变化监听器
     */
    private void setupMapRadiusListeners() {
        if (googleMap == null) {
            return;
        }
        
        // 监听地图相机移动
        googleMap.setOnCameraMoveListener(() -> {
            if (radiusChangeListener != null) {
                int newRadius = getCurrentMapRadius();
                radiusChangeListener.onMapRadiusChanged(newRadius);
            }
        });
        
        // 监听地图相机移动完成
        googleMap.setOnCameraIdleListener(() -> {
            if (radiusChangeListener != null) {
                int newRadius = getCurrentMapRadius();
                radiusChangeListener.onMapRadiusChanged(newRadius);
            }
        });
    }
    
    /**
     * 获取智能搜索半径（结合可见区域和缩放级别）
     */
    public int getSmartSearchRadius() {
        if (googleMap == null) {
            return getDefaultSearchRadius();
        }
        
        try {
            // 获取基于可见区域的半径
            int visibleRadius = getCurrentMapRadius();
            
            // 获取基于缩放级别的半径
            int zoomRadius = getCurrentCameraRadius();
            
            // 取两者的较小值，确保搜索范围合理
            int smartRadius = Math.min(visibleRadius, zoomRadius);
            
            Log.d(TAG, "Smart search radius: " + smartRadius + " meters (visible: " + visibleRadius + ", zoom: " + zoomRadius + ")");
            return smartRadius;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate smart search radius", e);
            return getDefaultSearchRadius();
        }
    }
    
    /**
     * 地图半径变化监听器接口
     */
    public interface OnMapRadiusChangeListener {
        void onMapRadiusChanged(int newRadius);
    }
}
