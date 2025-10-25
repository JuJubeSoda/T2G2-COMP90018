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

    @SerializedName("isPublic")
    private Boolean isPublic; // Whether the plant should be visible to nearby users

    // Constructors
    public PlantRequest() {}

    // --- FIX: Update constructor to include all fields ---
    public PlantRequest(String name, String image, String description, Double latitude,
                        Double longitude, String scientificName, String createdAt, String updatedAt,
                        Long gardenId, Boolean isFavourite, Boolean isPublic) {
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
        this.isPublic = isPublic;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Long getGardenId() { return gardenId; }
    public void setGardenId(Long gardenId) { this.gardenId = gardenId; }

    public Boolean getIsFavourite() { return isFavourite; }
    public void setIsFavourite(Boolean isFavourite) { this.isFavourite = isFavourite; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}
