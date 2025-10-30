package com.example.myapplication.network;

import com.example.myapplication.ui.myplants.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * PlantDto - Data Transfer Object for user-uploaded plants from backend API.
 * 
 * Purpose:
 * - Receives plant data from GET /api/plants endpoints
 * - Parses JSON responses using Gson
 * - Converts to domain model (Plant) for app usage
 * 
 * API Endpoints Using This DTO:
 * - GET /api/plants/user - User's personal plant collection
 * - GET /api/plants/nearby - Public plants from nearby users
 * - GET /api/plants/{id} - Single plant details
 * 
 * Key Differences from PlantWikiDto:
 * - Includes user-specific data (userId, gardenId, isFavourite)
 * - Has GPS coordinates (latitude, longitude)
 * - Contains timestamps (createdAt, updatedAt)
 * - May have user-generated tags and discoveredBy field
 * 
 * Data Flow:
 * 1. Backend sends JSON response
 * 2. Gson deserializes to PlantDto
 * 3. toPlant() converts to domain model
 * 4. App uses Plant object for display and logic
 */
public class PlantDto {

    // ===== Core Plant Identity =====
    
    /** Unique database ID for this plant */
    @SerializedName("plantId")
    private int plantId;
    
    /** ID of user who uploaded this plant */
    @SerializedName("userId")
    private int userId;
    
    /** Common/display name of the plant */
    @SerializedName("name")
    private String name;
    
    /** Base64 encoded image string from backend */
    @SerializedName("image")
    private String image;
    
    /** User-provided description or notes */
    @SerializedName("description")
    private String description;
    
    // ===== Location Data =====
    
    /** GPS latitude where plant was discovered (nullable) */
    @SerializedName("latitude")
    private Double latitude;
    
    /** GPS longitude where plant was discovered (nullable) */
    @SerializedName("longitude")
    private Double longitude;
    
    // ===== Botanical Information =====
    
    /** Scientific/botanical name of the plant */
    @SerializedName("scientificName")
    private String scientificName;
    
    // ===== User Organization =====
    
    /** ID of garden this plant belongs to */
    @SerializedName("gardenId")
    private int gardenId;
    
    /** Whether user marked this plant as favourite */
    @SerializedName("isFavourite")
    private boolean isFavourite;
    
    // ===== Timestamps =====
    
    /** ISO 8601 timestamp when plant was created */
    @SerializedName("createdAt")
    private String createdAt;
    
    /** ISO 8601 timestamp when plant was last updated */
    @SerializedName("updatedAt")
    private String updatedAt;

    // ===== Optional/Extended Fields =====
    // These fields may be null if not provided by backend
    
    /** User-generated tags for categorization */
    @SerializedName("tags")
    private List<String> tags;
    
    /** Username of person who discovered/uploaded plant */
    @SerializedName("discoveredBy")
    private String discoveredBy;
    
    /** Light requirement description (e.g., "Full Sun", "Partial Shade") */
    @SerializedName("lightRequirement")
    private String lightRequirement;
    
    /** Water requirement description (e.g., "Daily", "Weekly") */
    @SerializedName("waterRequirement")
    private String waterRequirement;
    
    /** Temperature requirement description */
    @SerializedName("temperatureRequirement")
    private String temperatureRequirement;
    
    /** Humidity requirement description */
    @SerializedName("humidityRequirement")
    private String humidityRequirement;

    /**
     * Converts this DTO to the app's domain model (Plant).
     * 
     * Purpose:
     * - Separates network layer from domain layer
     * - Creates Parcelable Plant object for fragment navigation
     * - Ensures all fields are properly mapped
     * 
     * @return New Plant object with all data from this DTO
     */
    public Plant toPlant() {
        return new Plant(
                this.plantId,
                this.userId,
                this.name,
                this.image,
                this.description,
                this.latitude,
                this.longitude,
                this.scientificName,
                this.gardenId,
                this.isFavourite,
                this.createdAt,
                this.updatedAt,
                this.tags,
                this.discoveredBy,
                this.lightRequirement,
                this.waterRequirement,
                this.temperatureRequirement,
                this.humidityRequirement
        );
    }

    // ===== Getters =====
    // Provide access to DTO fields for direct usage (e.g., in adapters, nearby discoveries)
    
    public String getScientificName() { return scientificName; }
    public int getPlantId() { return plantId; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public String getCreatedAt() { return createdAt; }
    public String getDiscoveredBy() { return discoveredBy; }
    public String getDescription() { return description; }
    public boolean isFavourite() { return isFavourite; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
