package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for adding a new plant. Matches the backend entity.
 */
public class PlantRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image; // Base64 encoded

    @SerializedName("description")
    private String description;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("scientificName")
    private String scientificName;

    // --- FIX: Add createdAt and updatedAt fields ---
    @SerializedName("createdAt")
    private String createdAt; // ISO 8601 String

    @SerializedName("updatedAt")
    private String updatedAt; // ISO 8601 String

    @SerializedName("gardenId")
    private Long gardenId;

    @SerializedName("isFavourite")
    private Boolean isFavourite;

    // Constructors
    public PlantRequest() {}

    // --- FIX: Update constructor to include all fields ---
    public PlantRequest(String name, String image, String description, Double latitude,
                        Double longitude, String scientificName, String createdAt, String updatedAt,
                        Long gardenId, Boolean isFavourite) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scientificName = scientificName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.gardenId = gardenId;
        this.isFavourite = isFavourite;
    }

    // Getters and Setters for all fields...
    // (You can auto-generate these in Android Studio)
}
