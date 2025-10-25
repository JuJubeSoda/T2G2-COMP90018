package com.example.myapplication.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SensorDataCollector implements SensorEventListener, LocationListener {
    
    private static final String TAG = "SensorDataCollector";
    
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private Context context;
    
    // Sensors
    private Sensor lightSensor;
    private Sensor temperatureSensor;
    private Sensor humiditySensor;
    private Sensor pressureSensor;
    
    // Current sensor data
    private Map<String, Object> currentSensorData = new HashMap<>();
    private Location currentLocation;
    
    // Data collection flags
    private boolean hasCollectedSensorData = false;
    private boolean hasCollectedLocation = false;
    private long startCollectionTime = 0;
    private static final long COLLECTION_TIMEOUT = 5000; // 5 seconds timeout
    
    // Callback interface
    public interface SensorDataCallback {
        void onSensorDataUpdated(Map<String, Object> sensorData);
        void onLocationUpdated(Location location);
        void onError(String error);
    }
    
    private SensorDataCallback callback;
    
    public SensorDataCollector(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        initializeSensors();
    }
    
    private void initializeSensors() {
        // Initialize available sensors
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        
        Log.d(TAG, "Sensors initialized:");
        Log.d(TAG, "Light sensor available: " + (lightSensor != null));
        Log.d(TAG, "Temperature sensor available: " + (temperatureSensor != null));
        Log.d(TAG, "Humidity sensor available: " + (humiditySensor != null));
        Log.d(TAG, "Pressure sensor available: " + (pressureSensor != null));
    }
    
    public void startCollecting(SensorDataCallback callback) {
        this.callback = callback;
        this.startCollectionTime = System.currentTimeMillis();
        this.hasCollectedSensorData = false;
        this.hasCollectedLocation = false;
        
        // Register sensor listeners (one-time collection)
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (humiditySensor != null) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        // Start location updates
        startLocationUpdates();
        
        Log.d(TAG, "Started collecting sensor data (one-time collection)");
        
        // Auto-stop after timeout
        new android.os.Handler().postDelayed(() -> {
            if (!hasCollectedSensorData || !hasCollectedLocation) {
                Log.d(TAG, "Collection timeout - stopping with partial data");
                stopCollecting();
            }
        }, COLLECTION_TIMEOUT);
    }
    
    public void stopCollecting() {
        sensorManager.unregisterListener(this);
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        Log.d(TAG, "Stopped collecting sensor data");
    }
    
    private void startLocationUpdates() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 
                    10000, // 10 seconds
                    10, // 10 meters
                    this
                );
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 
                    10000, // 10 seconds
                    10, // 10 meters
                    this
                );
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
            if (callback != null) {
                callback.onError("Location permission not granted");
            }
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Skip if already collected
        if (hasCollectedSensorData) {
            return;
        }
        
        float value = event.values[0];
        String sensorType = "";
        String unit = "";
        
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                sensorType = "lightLevel";
                unit = " lx";
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                sensorType = "temperature";
                unit = "°C";
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                sensorType = "humidity";
                unit = "%";
                break;
            case Sensor.TYPE_PRESSURE:
                sensorType = "pressure";
                unit = " hPa";
                break;
        }
        
        if (!sensorType.isEmpty()) {
            currentSensorData.put(sensorType, String.format("%.1f%s", value, unit));
            
            // Add additional context
            addSensorContext(sensorType, value);
            
            Log.d(TAG, sensorType + ": " + value + unit + " (collected once)");
            
            // Mark as collected and stop sensor updates
            hasCollectedSensorData = true;
            
            if (callback != null) {
                callback.onSensorDataUpdated(new HashMap<>(currentSensorData));
            }
            
            // Check if we can stop collecting
            checkAndStopIfComplete();
        }
    }
    
    private void addSensorContext(String sensorType, float value) {
        switch (sensorType) {
            case "lightLevel":
                if (value < 100) {
                    currentSensorData.put("lightCondition", "Low light");
                } else if (value < 1000) {
                    currentSensorData.put("lightCondition", "Moderate light");
                } else {
                    currentSensorData.put("lightCondition", "Bright light");
                }
                break;
            case "temperature":
                if (value < 10) {
                    currentSensorData.put("temperatureCondition", "Cold");
                } else if (value < 25) {
                    currentSensorData.put("temperatureCondition", "Moderate");
                } else {
                    currentSensorData.put("temperatureCondition", "Warm");
                }
                break;
            case "humidity":
                if (value < 30) {
                    currentSensorData.put("humidityCondition", "Dry");
                } else if (value < 70) {
                    currentSensorData.put("humidityCondition", "Moderate");
                } else {
                    currentSensorData.put("humidityCondition", "Humid");
                }
                break;
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Can be ignored for most use cases
    }
    
    @Override
    public void onLocationChanged(Location location) {
        // Skip if already collected
        if (hasCollectedLocation) {
            return;
        }
        
        currentLocation = location;
        
        // Add location data to sensor data
        currentSensorData.put("latitude", location.getLatitude());
        currentSensorData.put("longitude", location.getLongitude());
        currentSensorData.put("altitude", location.getAltitude());
        currentSensorData.put("accuracy", location.getAccuracy() + " meters");
        
        Log.d(TAG, "Location collected once: " + location.getLatitude() + ", " + location.getLongitude());
        
        // Mark as collected
        hasCollectedLocation = true;
        
        if (callback != null) {
            callback.onLocationUpdated(location);
            callback.onSensorDataUpdated(new HashMap<>(currentSensorData));
        }
        
        // Check if we can stop collecting
        checkAndStopIfComplete();
    }
    
    // Check if collection is complete and stop if needed
    private void checkAndStopIfComplete() {
        if (hasCollectedSensorData && hasCollectedLocation) {
            Log.d(TAG, "All data collected - stopping sensors");
            stopCollecting();
        }
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Location provider enabled: " + provider);
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Location provider disabled: " + provider);
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Location provider status changed: " + provider + " - " + status);
    }
    
    public Map<String, Object> getCurrentSensorData() {
        return new HashMap<>(currentSensorData);
    }
    
    public Location getCurrentLocation() {
        return currentLocation;
    }
    
    public String getLocationString() {
        if (currentLocation != null) {
            return String.format("%.4f, %.4f", 
                currentLocation.getLatitude(), 
                currentLocation.getLongitude());
        }
        return "Unknown location";
    }
    
    public boolean hasLocationPermission() {
        return context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) 
            == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
}
