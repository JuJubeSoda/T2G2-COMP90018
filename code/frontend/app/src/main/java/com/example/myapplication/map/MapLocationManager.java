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
     * 计算覆盖整个可见区域的最小半径
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
            LatLng southwest = bounds.southwest;
            
            // 计算从中心点到各个边界的距离
            float[] results = new float[1];
            
            // 计算到东北角的距离
            Location.distanceBetween(
                center.latitude, center.longitude,
                northeast.latitude, northeast.longitude,
                results
            );
            float distanceToNortheast = results[0];
            
            // 计算到西南角的距离
            Location.distanceBetween(
                center.latitude, center.longitude,
                southwest.latitude, southwest.longitude,
                results
            );
            float distanceToSouthwest = results[0];
            
            // 计算到西北角的距离
            LatLng northwest = new LatLng(northeast.latitude, southwest.longitude);
            Location.distanceBetween(
                center.latitude, center.longitude,
                northwest.latitude, northwest.longitude,
                results
            );
            float distanceToNorthwest = results[0];
            
            // 计算到东南角的距离
            LatLng southeast = new LatLng(southwest.latitude, northeast.longitude);
            Location.distanceBetween(
                center.latitude, center.longitude,
                southeast.latitude, southeast.longitude,
                results
            );
            float distanceToSoutheast = results[0];
            
            // 取最大距离作为半径，确保覆盖整个可见区域
            float maxDistance = Math.max(Math.max(distanceToNortheast, distanceToSouthwest),
                                        Math.max(distanceToNorthwest, distanceToSoutheast));
            
            int radius = (int) maxDistance;
            
            // 设置合理的半径范围（最小100米，最大50000米）
            radius = Math.max(100, Math.min(radius, 50000));
            
            Log.d(TAG, "Calculated full visible area radius: " + radius + " meters");
            Log.d(TAG, "Distances - NE:" + distanceToNortheast + ", SW:" + distanceToSouthwest + 
                       ", NW:" + distanceToNorthwest + ", SE:" + distanceToSoutheast);
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
        if (zoom >= 10) return 2000;     // 州级别 - 2公里
        if (zoom >= 8) return 5000;       // 国家级别 - 5公里
        if (zoom >= 6) return 10000;      // 大陆级别 - 10公里
        if (zoom >= 4) return 20000;      // 洲级别 - 20公里
        return 50000;                     // 全球级别 - 50公里
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
     * 优先使用完整可见区域半径，确保覆盖整个可见范围
     */
    public int getSmartSearchRadius() {
        if (googleMap == null) {
            return getDefaultSearchRadius();
        }
        
        try {
            // 获取完整可见区域的半径（确保覆盖整个可见区域）
            int fullVisibleRadius = getFullVisibleAreaRadius();
            
            // 获取基于缩放级别的半径
            int zoomRadius = getCurrentCameraRadius();
            
            // 优先使用完整可见区域半径，确保覆盖整个可见范围
            // 只有当可见区域半径过大时才考虑缩放级别限制
            int smartRadius;
            if (fullVisibleRadius > zoomRadius * 10) {
                // 如果可见区域半径过大，使用缩放级别限制（10倍）
                smartRadius = Math.min(fullVisibleRadius, zoomRadius * 10);
                Log.d(TAG, "Using zoom-limited radius due to very large visible area");
            } else {
                // 优先使用完整可见区域半径
                smartRadius = fullVisibleRadius;
                Log.d(TAG, "Using full visible area radius to cover entire map view");
            }
            
            Log.d(TAG, "Smart search radius: " + smartRadius + " meters (full visible: " + fullVisibleRadius + ", zoom: " + zoomRadius + ")");
            return smartRadius;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate smart search radius", e);
            return getDefaultSearchRadius();
        }
    }
    
    /**
     * 计算覆盖整个可见区域的最小半径（使用几何学方法）
     * 这个方法确保搜索范围完全覆盖用户可见的地图区域
     */
    public int getFullVisibleAreaRadius() {
        if (googleMap == null) {
            return getDefaultSearchRadius();
        }
        
        try {
            // 获取地图投影和可见区域
            VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
            LatLngBounds bounds = visibleRegion.latLngBounds;
            
            // 获取边界点
            LatLng northeast = bounds.northeast;
            LatLng southwest = bounds.southwest;
            LatLng center = bounds.getCenter();
            
            // 计算可见区域的宽度和高度（以米为单位）
            float[] widthResults = new float[1];
            float[] heightResults = new float[1];
            
            // 计算东西方向的距离（宽度）
            Location.distanceBetween(
                southwest.latitude, southwest.longitude,
                southwest.latitude, northeast.longitude,
                widthResults
            );
            
            // 计算南北方向的距离（高度）
            Location.distanceBetween(
                southwest.latitude, southwest.longitude,
                northeast.latitude, southwest.longitude,
                heightResults
            );
            
            float width = widthResults[0];
            float height = heightResults[0];
            
            // 计算覆盖整个矩形区域的最小半径
            // 使用勾股定理：半径 = sqrt((width/2)^2 + (height/2)^2)
            double halfWidth = width / 2.0;
            double halfHeight = height / 2.0;
            double radius = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
            
            // 添加10%的缓冲，确保完全覆盖
            radius = radius * 1.1;
            
            int finalRadius = (int) radius;
            
            // 设置合理的半径范围（最大100公里）
            finalRadius = Math.max(100, Math.min(finalRadius, 100000));
            
            Log.d(TAG, "Full visible area calculation:");
            Log.d(TAG, "  - Visible width: " + width + " meters");
            Log.d(TAG, "  - Visible height: " + height + " meters");
            Log.d(TAG, "  - Calculated radius: " + finalRadius + " meters");
            
            return finalRadius;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate full visible area radius", e);
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
