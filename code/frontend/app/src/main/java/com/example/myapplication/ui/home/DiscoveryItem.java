package com.example.myapplication.ui.home; // Or your package

public class DiscoveryItem {
    private String name;
    private String distance;
    private int imageResId;
    private String description;

    public DiscoveryItem(String name, String distance, int imageResId, String description) {
        this.name = name;
        this.distance = distance;
        this.imageResId = imageResId;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDistance() { return distance; }
    public int getImageResId() { return imageResId; }
    public String getDescription() { return description; }
}