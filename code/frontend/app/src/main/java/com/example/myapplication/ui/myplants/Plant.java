package com.example.myapplication.ui.myplants;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Plant - Core domain model representing a plant in the application.
 *
 * Purpose:
 * - Central data model for all plant-related features
 * - Implements Parcelable for efficient fragment navigation
 * - Supports both user-uploaded plants and wiki encyclopedia plants
 * - Unifies data from PlantDto and PlantWikiDto
 *
 * Data Sources:
 * 1. User-Uploaded Plants (via PlantDto):
 *    - Personal plant collections
 *    - Nearby discoveries
 *    - Includes GPS coordinates and timestamps
 *
 * 2. Wiki Encyclopedia Plants (via PlantWikiDto):
 *    - Curated plant information
 *    - Detailed care guides and features
 *    - No location or user-specific data
 *
 * Key Features:
 * - Parcelable: Can be passed between fragments via Bundle
 * - Gson Serializable: Can be converted to/from JSON
 * - Comprehensive: Supports both basic and detailed plant information
 * - Flexible: Handles nullable fields gracefully
 *
 * Usage Throughout App:
 * - MyGardenFragment: Display user's plant collection
 * - PlantWikiFragment: Display encyclopedia entries
 * - PlantDetailFragment: Show detailed plant information
 * - UploadCompleteFragment: Preview uploaded plant
 * - PlantWikiOverview/Features/CareGuide: Display wiki tabs
 *
 * Parcelable Implementation:
 * - Enables passing Plant objects via navigation arguments
 * - Preserves all fields across configuration changes
 * - Efficient serialization for inter-fragment communication
 */
public class Plant implements Parcelable {

    // ===== Core Identity Fields =====

    /** Unique database ID for this plant */
    @SerializedName("plantId")
    private int plantId;

    /** ID of user who uploaded this plant (0 for wiki plants) */
    @SerializedName("userId")
    private int userId;

    /** Common/display name of the plant */
    @SerializedName("name")
    private String name;

    /** Base64 encoded image string or URL */
    @SerializedName("image")
    private String imageUrl;

    /** Detailed description or user notes */
    @SerializedName("description")
    private String description;

    // ===== Location Data (User Plants Only) =====

    /** GPS latitude where plant was discovered (null for wiki plants) */
    @SerializedName("latitude")
    private Double latitude;

    /** GPS longitude where plant was discovered (null for wiki plants) */
    @SerializedName("longitude")
    private Double longitude;

    // ===== Botanical Information =====

    /** Scientific/botanical name */
    @SerializedName("scientificName")
    private String scientificName;

    // ===== User Organization (User Plants Only) =====

    /** ID of garden this plant belongs to (0 for wiki plants) */
    @SerializedName("gardenId")
    private int gardenId;

    /** Whether user marked this plant as favourite */
    @SerializedName("isFavourite")
    private boolean isFavourite;

    // ===== Timestamps (User Plants Only) =====

    /** ISO 8601 timestamp when plant was created (null for wiki plants) */
    @SerializedName("createdAt")
    private String createdAt;

    /** ISO 8601 timestamp when plant was last updated (null for wiki plants) */
    @SerializedName("updatedAt")
    private String updatedAt;

    // ===== Extended Metadata =====

    /** User-generated or auto-generated tags for categorization */
    @SerializedName("tags")
    private List<String> tags;

    /** Username of person who discovered/uploaded plant */
    @SerializedName("discoveredBy")
    private String discoveredBy;

    // ===== Care Requirements =====

    /** Light requirement (e.g., "Full Sun", "Partial Shade") */
    @SerializedName("lightRequirement")
    private String lightRequirement;

    /** Water requirement (e.g., "Daily", "Weekly", "Moderate") */
    @SerializedName("waterRequirement")
    private String waterRequirement;

    /** Temperature requirement description */
    @SerializedName("temperatureRequirement")
    private String temperatureRequirement;

    /** Humidity requirement description */
    @SerializedName("humidityRequirement")
    private String humidityRequirement;

    // ===== Wiki-Specific Fields =====
    // These fields are primarily populated for encyclopedia plants

    /** Expected mature height (e.g., "2-3 meters", "30-50 cm") */
    @SerializedName("matureHeight")
    private String matureHeight;

    /** Leaf type description (e.g., "Broad", "Needle", "Compound") */
    @SerializedName("leafType")
    private String leafType;

    /** Toxicity information (e.g., "Non-toxic", "Toxic to pets") */
    @SerializedName("toxicity")
    private String toxicity;

    /** Air purifying properties (e.g., "Yes", "No", "Moderate") */
    @SerializedName("airPurifying")
    private String airPurifying;

    /** Soil requirements and recommendations */
    @SerializedName("soilGuide")
    private String soilGuide;

    /** Fertilizer requirements and schedule */
    @SerializedName("fertilizerGuide")
    private String fertilizerGuide;

    /** Growing difficulty level (e.g., "Easy", "Moderate", "Hard") */
    @SerializedName("difficulty")
    private String difficulty;

    /** Physical and botanical features (used in PlantWikiFeatures tab) */
    @SerializedName("features")
    private String features;

    /** Complete care instructions (used in PlantWikiCareGuide tab) */
    @SerializedName("careGuide")
    private String careGuide;

    /**
     * Primary constructor for creating Plant objects from DTOs.
     *
     * Used by:
     * - PlantDto.toPlant() - Converting user-uploaded plants
     * - PlantWikiDto.toPlant() - Converting wiki encyclopedia plants
     *
     * Note: Wiki-specific fields (features, careGuide, matureHeight, etc.) are set via setters
     * after construction for clarity and flexibility.
     *
     * @param plantId Unique database ID
     * @param userId Owner's user ID (0 for wiki plants)
     * @param name Common/display name
     * @param imageUrl Base64 encoded image or URL
     * @param description Detailed description
     * @param latitude GPS latitude (null for wiki plants)
     * @param longitude GPS longitude (null for wiki plants)
     * @param scientificName Botanical name
     * @param gardenId Garden ID (0 for wiki plants)
     * @param isFavourite Favourite status
     * @param createdAt Creation timestamp (null for wiki plants)
     * @param updatedAt Update timestamp (null for wiki plants)
     * @param tags Categorization tags
     * @param discoveredBy Username of uploader
     * @param lightRequirement Light care requirement
     * @param waterRequirement Water care requirement
     * @param temperatureRequirement Temperature requirement
     * @param humidityRequirement Humidity requirement
     */
    public Plant(int plantId, int userId, String name, String imageUrl, String description,
                 Double latitude, Double longitude, String scientificName, int gardenId,
                 boolean isFavourite, String createdAt, String updatedAt, List<String> tags,
                 String discoveredBy, String lightRequirement, String waterRequirement,
                 String temperatureRequirement, String humidityRequirement) {
        this.plantId = plantId;
        this.userId = userId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scientificName = scientificName;
        this.gardenId = gardenId;
        this.isFavourite = isFavourite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = tags;
        this.discoveredBy = discoveredBy;
        this.lightRequirement = lightRequirement;
        this.waterRequirement = waterRequirement;
        this.temperatureRequirement = temperatureRequirement;
        this.humidityRequirement = humidityRequirement;
    }

    /**
     * Parcelable constructor - Reconstructs Plant object from Parcel.
     * This must read values in the same order they are written in writeToParcel().
     *
     * @param in Parcel containing serialized Plant data
     */
    protected Plant(Parcel in) {
        plantId = in.readInt();
        userId = in.readInt();
        name = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        if (in.readByte() == 0) { latitude = null; } else { latitude = in.readDouble(); }
        if (in.readByte() == 0) { longitude = null; } else { longitude = in.readDouble(); }
        scientificName = in.readString();
        gardenId = in.readInt();
        isFavourite = in.readByte() != 0;
        createdAt = in.readString();
        updatedAt = in.readString();
        tags = in.createStringArrayList();
        discoveredBy = in.readString();
        lightRequirement = in.readString();
        waterRequirement = in.readString();
        temperatureRequirement = in.readString();
        humidityRequirement = in.readString();
        // Wiki-specific fields
        matureHeight = in.readString();
        leafType = in.readString();
        toxicity = in.readString();
        airPurifying = in.readString();
        soilGuide = in.readString();
        fertilizerGuide = in.readString();
        features = in.readString();
        careGuide = in.readString();
        difficulty = in.readString(); // Read new difficulty field
    }

    /**
     * Serializes Plant object to a Parcel for inter-fragment communication.
     * This must write values in the same order they are read in the Plant(Parcel in) constructor.
     *
     * @param dest Destination parcel
     * @param flags Additional flags (unused)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(plantId);
        dest.writeInt(userId);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(description);
        // Nullable Double fields require a byte flag to indicate if they are null
        if (latitude == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeDouble(latitude); }
        if (longitude == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeDouble(longitude); }
        dest.writeString(scientificName);
        dest.writeInt(gardenId);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeStringList(tags);
        dest.writeString(discoveredBy);
        dest.writeString(lightRequirement);
        dest.writeString(waterRequirement);
        dest.writeString(temperatureRequirement);
        dest.writeString(humidityRequirement);
        // Wiki-specific fields
        dest.writeString(matureHeight);
        dest.writeString(leafType);
        dest.writeString(toxicity);
        dest.writeString(airPurifying);
        dest.writeString(soilGuide);
        dest.writeString(fertilizerGuide);
        dest.writeString(features);
        dest.writeString(careGuide);
        dest.writeString(difficulty); // Write new difficulty field
    }

    /** Required by Parcelable interface - returns 0 for standard objects */
    @Override
    public int describeContents() { return 0; }

    /** Parcelable CREATOR - Enables Android to reconstruct Plant objects from a Parcel */
    public static final Creator<Plant> CREATOR = new Creator<Plant>() {
        @Override
        public Plant createFromParcel(Parcel in) { return new Plant(in); }
        @Override
        public Plant[] newArray(int size) { return new Plant[size]; }
    };

    // ===== Getters =====
    // Provide read-only access to all plant fields

    public int getPlantId() { return plantId; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getScientificName() { return scientificName; }
    public int getGardenId() { return gardenId; }
    public boolean isFavourite() { return isFavourite; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public List<String> getTags() { return tags; }
    public String getDiscoveredBy() { return discoveredBy; }
    public String getLightRequirement() { return lightRequirement; }
    public String getWaterRequirement() { return waterRequirement; }
    public String getTemperatureRequirement() { return temperatureRequirement; }
    public String getHumidityRequirement() { return humidityRequirement; }
    public String getMatureHeight() { return matureHeight; }
    public String getLeafType() { return leafType; }
    public String getToxicity() { return toxicity; }
    public String getAirPurifying() { return airPurifying; }
    public String getSoilGuide() { return soilGuide; }
    public String getFertilizerGuide() { return fertilizerGuide; }
    public String getFeatures() { return features; }
    public String getCareGuide() { return careGuide; }
    public String getDifficulty() { return difficulty; }

    // ===== Setters =====
    // Setters are provided for fields populated after construction, primarily from DTOs.

    public void setMatureHeight(String matureHeight) { this.matureHeight = matureHeight; }
    public void setFeatures(String features) { this.features = features; }
    public void setCareGuide(String careGuide) { this.careGuide = careGuide; }
    public void setSoilGuide(String soilGuide) { this.soilGuide = soilGuide; }
    public void setFertilizerGuide(String fertilizerGuide) { this.fertilizerGuide = fertilizerGuide; }
    public void setLeafType(String leafType) { this.leafType = leafType; }
    public void setToxicity(String toxicity) { this.toxicity = toxicity; }
    public void setAirPurifying(String airPurifying) { this.airPurifying = airPurifying; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
