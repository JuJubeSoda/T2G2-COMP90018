package com.example.myapplication.sensor;

import java.util.HashMap;
import java.util.Map;

public class SensorDataModel {
    
    private String location;
    private Map<String, Object> sensorData;
    private long timestamp;
    
    public SensorDataModel() {
        this.sensorData = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public SensorDataModel(String location, Map<String, Object> sensorData) {
        this.location = location;
        this.sensorData = sensorData != null ? new HashMap<>(sensorData) : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Map<String, Object> getSensorData() {
        return sensorData;
    }
    
    public void setSensorData(Map<String, Object> sensorData) {
        this.sensorData = sensorData != null ? new HashMap<>(sensorData) : new HashMap<>();
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    // Helper methods
    public void addSensorValue(String key, Object value) {
        this.sensorData.put(key, value);
    }
    
    public Object getSensorValue(String key) {
        return this.sensorData.get(key);
    }
    
    public boolean hasSensorValue(String key) {
        return this.sensorData.containsKey(key);
    }
    
    // Convert to API format
    public Map<String, Object> toApiFormat() {
        Map<String, Object> apiData = new HashMap<>();
        apiData.put("location", location);
        apiData.put("sensorData", sensorData);
        apiData.put("timestamp", timestamp);
        return apiData;
    }
    
    @Override
    public String toString() {
        return "SensorDataModel{" +
                "location='" + location + '\'' +
                ", sensorData=" + sensorData +
                ", timestamp=" + timestamp +
                '}';
    }
}
