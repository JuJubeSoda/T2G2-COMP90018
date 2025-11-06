package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * GardenDto - Data Transfer Object for garden locations from backend API.
 * 
 * Purpose:
 * - Represents a community garden or plant collection location
 * - Used for map clustering and garden-based plant filtering
 * - Enables users to discover plants grouped by geographic gardens
 * 
 * API Endpoints Using This DTO:
 * - GET /api/gardens/all - All gardens in the system
 * - GET /api/gardens/nearby - Gardens within radius
 * - POST /api/gardens/create - Create new garden
 * 
 * Map Integration:
 * - Gardens are clustered on map view
 * - Clicking garden shows all plants in that garden
 * - Provides geographic organization of plant discoveries
 * 
 * Fields:
 * - gardenId: Unique database identifier
 * - name: Garden name (e.g., "Melbourne Botanic Gardens")
 * - description: Additional garden information
 * - latitude/longitude: GPS coordinates for map display
 * - createdAt/updatedAt: Timestamps for tracking
 */
public class GardenDto implements Serializable {
    /** Unique database ID for this garden */
    @SerializedName("gardenId")
    private Long gardenId;

    /** GPS latitude of garden location */
    @SerializedName("latitude")
    private Double latitude;

    /** GPS longitude of garden location */
    @SerializedName("longitude")
    private Double longitude;

    /** Garden name or title */
    @SerializedName("name")
    private String name;

    /** Garden description or additional details */
    @SerializedName("description")
    private String description;

    /** Timestamp when garden was created */
    @SerializedName("createdAt")
    private String createdAt;

    /** Timestamp when garden was last updated */
    @SerializedName("updatedAt")
    private String updatedAt;

    /** Default constructor for Gson deserialization */
    public GardenDto() {}

    /**
     * Constructor for creating new garden instances.
     * 
     * @param name Garden name
     * @param description Garden description
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     */
    public GardenDto(String name, String description, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getGardenId() { return gardenId; }
    public void setGardenId(Long gardenId) { this.gardenId = gardenId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "GardenDto{" +
                "gardenId=" + gardenId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}


