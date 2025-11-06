package com.example.myapplication.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.Nullable;
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
import com.example.myapplication.util.LogUtil;

/**
 * Map Location Manager - handles map location related features
 * including permission management, location retrieval, and camera positioning.
 */
public class MapLocationManager {
    
    private static final String TAG = "MapLocationManager";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    
    // Default location (Sydney)
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    
    // Default search radius
    private static final int DEFAULT_SEARCH_RADIUS = 1000; // meters
    
    private final android.content.Context context;
    @Nullable
    private final GoogleMap googleMap;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    
    private boolean locationPermissionGranted = false;
    private Location lastKnownLocation;
    
    /**
     * Constructor - supports optional GoogleMap (null means not using map features)
     * @param context Android context
     * @param googleMap GoogleMap instance, can be null (for non-map scenarios)
     */
    public MapLocationManager(android.content.Context context, @Nullable GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    /**
     * Request location permission
     */
    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            // Permission request must be handled by Fragment; set state only here
            locationPermissionGranted = false;
        }
    }
    
    /**
     * Handle permission request result
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
     * Update map location UI settings
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
            LogUtil.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get device current location (location-only; does not operate the map)
     * Suitable for non-map scenarios (e.g., HomeFragment nearby discoveries)
     *
     * @param callback location result callback
     */
    public void getLocation(OnLocationResultCallback callback) {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            callback.onLocationSuccess(lastKnownLocation);
                        } else {
                            callback.onLocationError("Current location is null");
                        }
                    } else {
                        LogUtil.d(TAG, "Current location is null. Using defaults.");
                        LogUtil.e(TAG, "Exception: %s", task.getException());
                        callback.onLocationError("Failed to get location: " + task.getException());
                    }
                });
            } else {
                callback.onLocationError("Location permission not granted");
            }
        } catch (SecurityException e) {
            LogUtil.e(TAG, "Exception: " + e.getMessage(), e);
            callback.onLocationError("Security exception: " + e.getMessage());
        }
    }
    
    /**
     * Get device current location and move the map camera (map scenarios)
     * Differs from getLocation(): this operates the map and moves camera
     *
     * @param callback location result callback
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
                                // Operate the map only if map is available
                                if (googleMap != null) {
                                    LatLng currentLocation = new LatLng(
                                        lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()
                                    );
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
                                }
                                callback.onLocationSuccess(lastKnownLocation);
                            } else {
                                if (googleMap != null) {
                                    moveToDefaultLocation();
                                }
                                callback.onLocationError("Current location is null");
                            }
                        } else {
                            LogUtil.d(TAG, "Current location is null. Using defaults.");
                            LogUtil.e(TAG, "Exception: %s", task.getException());
                            if (googleMap != null) {
                                moveToDefaultLocation();
                            }
                            callback.onLocationError("Failed to get location: " + task.getException());
                        }
                    }
                });
            } else {
                if (googleMap != null) {
                    moveToDefaultLocation();
                }
                callback.onLocationError("Location permission not granted");
            }
        } catch (SecurityException e) {
            LogUtil.e(TAG, "Exception: " + e.getMessage(), e);
            callback.onLocationError("Security exception: " + e.getMessage());
        }
    }
    
    /**
     * Move to default location (only if map is available)
     */
    private void moveToDefaultLocation() {
        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }
    
    /**
     * Convenience: get current location and move the map (no callback)
     */
    public void getCurrentLocationAndMove() {
        getDeviceLocation(new OnLocationResultCallback() {
            @Override
            public void onLocationSuccess(Location location) {
                LogUtil.d(TAG, "Successfully moved to current location");
            }
            
            @Override
            public void onLocationError(String error) {
                LogUtil.e(TAG, "Failed to get current location: " + error);
            }
        });
    }
    
    /**
     * Check whether location permission is granted
     */
    public boolean hasLocationPermission() {
        return locationPermissionGranted;
    }
    
    /**
     * Get last known location
     */
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
    
    /**
     * Get default location
     */
    public LatLng getDefaultLocation() {
        return defaultLocation;
    }
    
    /**
     * Get default zoom level
     */
    public int getDefaultZoom() {
        return DEFAULT_ZOOM;
    }
    
    /**
     * Location result callback interface
     */
    public interface OnLocationResultCallback {
        void onLocationSuccess(Location location);
        void onLocationError(String error);
    }
    
    /**
     * Get permission request code
     */
    public static int getLocationPermissionRequestCode() {
        return PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    }
    
    // ==================== Map radius related features ====================
    
    // Map radius change listener
    private OnMapRadiusChangeListener radiusChangeListener;
    
    /**
     * Get current map view search radius (based on visible region)
     */
    public int getCurrentMapRadius() {
        if (googleMap == null) {
            return getDefaultSearchRadius();
        }
        
        try {
            // Get map projection and visible region
            VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
            LatLngBounds bounds = visibleRegion.latLngBounds;
            
            // Calculate center point
            LatLng center = bounds.getCenter();
            LatLng northeast = bounds.northeast;
            
            // Use half the diagonal distance as radius
            float[] results = new float[1];
            Location.distanceBetween(
                center.latitude, center.longitude,
                northeast.latitude, northeast.longitude,
                results
            );
            
            int radius = (int) (results[0] / 2);
            
            // Clamp to reasonable range (min 100m, max 10000m)
            radius = Math.max(100, Math.min(radius, 10000));
            
            LogUtil.d(TAG, "Calculated map radius: " + radius + " meters");
            return radius;
            
        } catch (Exception e) {
            LogUtil.e(TAG, "Failed to calculate map radius", e);
            return getDefaultSearchRadius();
        }
    }
    
    /**
     * Get preset radius by zoom level
     */
    public int getRadiusByZoom(float zoom) {
        if (zoom >= 18) return 100;      // Street level - 100m
        if (zoom >= 16) return 200;      // Neighborhood level - 200m
        if (zoom >= 14) return 500;      // District level - 500m
        if (zoom >= 12) return 1000;     // City level - 1km
        if (zoom >= 10) return 2000;     // State level - 2km
        if (zoom >= 8) return 5000;      // Country level - 5km
        return 10000;                     // Continent level - 10km
    }
    
    /**
     * Get search radius for current camera position
     */
    public int getCurrentCameraRadius() {
        if (googleMap == null) {
            return getDefaultSearchRadius();
        }
        
        try {
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            return getRadiusByZoom(cameraPosition.zoom);
        } catch (Exception e) {
            LogUtil.e(TAG, "Failed to get camera radius", e);
            return getDefaultSearchRadius();
        }
    }
    
    /**
     * Get default search radius
     */
    public int getDefaultSearchRadius() {
        return DEFAULT_SEARCH_RADIUS;
    }

    /**
     * Get current camera center coordinate (used as query center)
     * Returns default location if map is not ready
     */
    public LatLng getCameraCenter() {
        LogUtil.d(TAG, "=== getCameraCenter Debug ===");
        
        if (googleMap == null) {
            LogUtil.w(TAG, "GoogleMap is null, using default location (Sydney)");
            LogUtil.d(TAG, "Default location: (" + defaultLocation.latitude + ", " + defaultLocation.longitude + ")");
            return defaultLocation;
        }
        
        try {
            // Try to get center from visible region
            VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
            if (visibleRegion != null && visibleRegion.latLngBounds != null) {
                LatLng center = visibleRegion.latLngBounds.getCenter();
                LogUtil.d(TAG, "Using visible region center: (" + center.latitude + ", " + center.longitude + ")");
                return center;
            }
            
            // Fallback to camera position target
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            if (cameraPosition != null && cameraPosition.target != null) {
                LatLng target = cameraPosition.target;
                LogUtil.d(TAG, "Using camera position target: (" + target.latitude + ", " + target.longitude + ")");
                return target;
            }
            
            LogUtil.w(TAG, "Cannot get camera center from visible region or camera position, using default");
            return defaultLocation;
        } catch (Exception e) {
            LogUtil.e(TAG, "Failed to get camera center, fallback to default", e);
            LogUtil.d(TAG, "Default location: (" + defaultLocation.latitude + ", " + defaultLocation.longitude + ")");
            return defaultLocation;
        }
    }
    
    /**
     * Set map radius change listener
     */
    public void setOnMapRadiusChangeListener(OnMapRadiusChangeListener listener) {
        this.radiusChangeListener = listener;
        setupMapRadiusListeners();
    }
    
    /**
     * Configure map radius change listeners
     * Use only setOnCameraIdleListener to avoid frequent triggers during movement
     */
    private void setupMapRadiusListeners() {
        if (googleMap == null) {
            return;
        }
        
        // Remove camera move listener and keep only idle listener
        // Reduces unnecessary triggers; works better with upper debounce
        googleMap.setOnCameraMoveListener(null);
        
        // Listen for when camera movement stops (user idle)
        googleMap.setOnCameraIdleListener(() -> {
            if (radiusChangeListener != null) {
                int newRadius = getCurrentMapRadius();
                radiusChangeListener.onMapRadiusChanged(newRadius);
            }
        });
    }
    
    /**
     * Get smart search radius (combines visible region and zoom level)
     */
    public int getSmartSearchRadius() {
        LogUtil.d(TAG, "=== getSmartSearchRadius Debug ===");
        
        if (googleMap == null) {
            LogUtil.w(TAG, "GoogleMap is null, using default radius: " + getDefaultSearchRadius() + "m");
            return getDefaultSearchRadius();
        }
        
        try {
            // Get radius based on visible region
            int visibleRadius = getCurrentMapRadius();
            LogUtil.d(TAG, "Visible radius: " + visibleRadius + "m");
            // Use visible region radius directly as final radius (disable zoom cap)
            int smartRadius = visibleRadius;
            LogUtil.d(TAG, "Final smart radius: " + smartRadius + "m (use visible radius only)");
            
            // Range check for radius
            if (smartRadius > 50000) {
                LogUtil.w(TAG, "WARNING: Smart radius is very large: " + smartRadius + "m (>50km)!");
            }
            
            return smartRadius;
            
        } catch (Exception e) {
            LogUtil.e(TAG, "Failed to calculate smart search radius", e);
            LogUtil.d(TAG, "Using default radius: " + getDefaultSearchRadius() + "m");
            return getDefaultSearchRadius();
        }
    }
    
    /**
     * Map radius change listener interface
     */
    public interface OnMapRadiusChangeListener {
        void onMapRadiusChanged(int newRadius);
    }
}
