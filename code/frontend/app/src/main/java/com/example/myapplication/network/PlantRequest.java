package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;

/**
 * PlantRequest - Data Transfer Object for creating new plants via API.
 * 
 * Purpose:
 * - Encapsulates all plant data needed for POST /api/plants/add endpoint
 * - Serializes to JSON format expected by backend
 * - Matches backend Plant entity structure
 * 
 * Usage:
 * 1. User captures plant photo and fills details in UploadFragment
 * 2. Create PlantRequest with all fields
 * 3. Send via ApiService.addPlant()
 * 4. Backend creates Plant entity and stores in database
 * 
 * Key Fields:
 * - image: Base64 encoded string (not file path)
 * - isPublic: Controls visibility in nearby discoveries
 * - timestamps: ISO 8601 UTC format (e.g., "2025-10-25T12:00:00.000Z")
 * - location: Nullable (user may deny permission)
 */
public class PlantRequest {

    /** Common name of the plant (user-provided or from wiki) */
    @SerializedName("name")
    private String name;

    /** Base64 encoded image string (converted from URI before upload) */
    @SerializedName("image")
    private String image;

    /** User-provided description or introduction text */
    @SerializedName("description")
    private String description;

    /** GPS latitude (null if location permission denied) */
    @SerializedName("latitude")
    private Double latitude;

    /** GPS longitude (null if location permission denied) */
    @SerializedName("longitude")
    private Double longitude;

    /** Scientific/botanical name of the plant */
    @SerializedName("scientificName")
    private String scientificName;

    /** Timestamp when plant was created (ISO 8601 UTC format) */
    @SerializedName("createdAt")
    private String createdAt;

    /** Timestamp when plant was last updated (ISO 8601 UTC format) */
    @SerializedName("updatedAt")
    private String updatedAt;

    /** ID of garden this plant belongs to (null for new plants) */
    @SerializedName("gardenId")
    private Long gardenId;

    /** Whether user marked this plant as favourite */
    @SerializedName("isFavourite")
    private Boolean isFavourite;

    /** Whether plant should be visible to nearby users (public sharing toggle) */
    @SerializedName("shareable")
    private Boolean shareable;

    /** Default constructor for Gson deserialization */
    public PlantRequest() {}

    /**
     * Full constructor for creating a complete plant upload request.
     * 
     * @param name Common name of the plant
     * @param image Base64 encoded image string
     * @param description User's description/notes
     * @param latitude GPS latitude (nullable)
     * @param longitude GPS longitude (nullable)
     * @param scientificName Scientific/botanical name
     * @param createdAt Creation timestamp (ISO 8601 UTC)
     * @param updatedAt Update timestamp (ISO 8601 UTC)
     * @param gardenId Garden ID (nullable for new plants)
     * @param isFavourite Favourite status
     * @param shareable Public visibility toggle
     */
    public PlantRequest(String name, String image, String description, Double latitude,
                        Double longitude, String scientificName, String createdAt, String updatedAt,
                        Long gardenId, Boolean isFavourite, Boolean shareable) {
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
        this.shareable = shareable;
    }

    // ===== Getters and Setters =====
    // Standard JavaBean accessors for all fields
    
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

    public Boolean getIsPublic() { return shareable; }
    public void setIsPublic(Boolean isPublic) { this.shareable = isPublic; }
}
