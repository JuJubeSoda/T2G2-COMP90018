package com.example.myapplication.ui.home; // Or your package

public class DiscoveryItem {
    private Long plantId; // optional: used for navigation to details
    private String name;
    private String distance;
    private int imageResId;
    private String description;
    private String base64Image; // Base64 encoded image from API

    // Constructor with resource ID
    public DiscoveryItem(Long plantId, String name, String distance, int imageResId, String description) {
        this.plantId = plantId;
        this.name = name;
        this.distance = distance;
        this.imageResId = imageResId;
        this.description = description;
        this.base64Image = null;
    }

    // Constructor with Base64 image string
    public DiscoveryItem(Long plantId, String name, String distance, int imageResId, String description, String base64Image) {
        this.plantId = plantId;
        this.name = name;
        this.distance = distance;
        this.imageResId = imageResId;
        this.description = description;
        this.base64Image = base64Image;
    }
    public Long getPlantId() { return plantId; }

    public String getName() { return name; }
    public String getDistance() { return distance; }
    public int getImageResId() { return imageResId; }
    public String getDescription() { return description; }
    public String getBase64Image() { return base64Image; }
    
    public boolean hasBase64Image() { 
        return base64Image != null && !base64Image.isEmpty(); 
    }
}