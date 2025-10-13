package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Plant data model corresponding to backend Plant entity
 */
public class Plant implements Serializable {
    
    @SerializedName("plantId")
    private Long plantId;
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("image")
    private String image; // Base64 encoded image or URL
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("latitude")
    private Double latitude;
    
    @SerializedName("longitude")
    private Double longitude;
    
    @SerializedName("scientificName")
    private String scientificName;
    
    @SerializedName("createdAt")
    private String createdAt; // Using String for JSON parsing
    
    @SerializedName("updatedAt")
    private String updatedAt; // Using String for JSON parsing
    
    @SerializedName("gardenId")
    private Long gardenId;
    
    @SerializedName("isFavourite")
    private boolean isFavourite;
    
    // Default constructor
    public Plant() {}
    
    // Constructor with basic fields
    public Plant(String name, String description, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public Long getPlantId() {
        return plantId;
    }
    
    public void setPlantId(Long plantId) {
        this.plantId = plantId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getScientificName() {
        return scientificName;
    }
    
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getGardenId() {
        return gardenId;
    }
    
    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }
    
    public boolean isFavourite() {
        return isFavourite;
    }
    
    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
    
    @Override
    public String toString() {
        return "Plant{" +
                "plantId=" + plantId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", scientificName='" + scientificName + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", gardenId=" + gardenId +
                ", isFavourite=" + isFavourite +
                '}';
    }
}
