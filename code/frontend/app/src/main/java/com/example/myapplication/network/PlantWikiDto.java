package com.example.myapplication.network;

import android.util.Log;
import com.example.myapplication.ui.myplants.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * PlantWikiDto - Data Transfer Object for plant encyclopedia data from backend.
 *
 * Purpose:
 * - Receives curated plant information from GET /api/wiki/all endpoint
 * - Contains comprehensive botanical and care information
 * - Provides richer data than user-uploaded plants (PlantDto)
 *
 * API Endpoint:
 * - GET /api/wiki/all - Returns all plants in the encyclopedia
 *
 * Key Differences from PlantDto:
 * - No user-specific data (no userId, gardenId, isFavourite)
 * - No GPS coordinates (wiki plants are not location-specific)
 * - Includes detailed care information (features, careGuide, difficulty)
 * - Has growth characteristics (growthHeight)
 * - Structured care requirements (waterNeeds, lightNeeds)
 *
 * Data Flow:
 * 1. Backend sends wiki plant data as JSON
 * 2. Gson deserializes to PlantWikiDto
 * 3. toPlant() converts to domain model with wiki-specific mapping
 * 4. PlantWikiFragment displays in searchable encyclopedia
 *
 * Usage:
 * - Plant Wiki browsing and search
 * - Pre-filling data when adding new plants
 * - Reference information for plant care
 * - Educational content display
 */
public class PlantWikiDto {

    private static final String TAG = "PlantWikiDto";

    // ===== Core Identity =====

    /** Unique ID in the plant wiki database. */
    @SerializedName("plantWikiId")
    private Long plantWikiId;

    /** Common or display name of the plant. */
    @SerializedName("name")
    private String name;

    /** Scientific or botanical name of the plant. */
    @SerializedName("scientificName")
    private String scientificName;

    /** Base64 encoded image string. */
    @SerializedName("image")
    private String image;

    // ===== Descriptive Information =====

    /** Detailed description of the plant, used in the overview tab. */
    @SerializedName("description")
    private String description;

    /** Optimal temperature or range for the plant (e.g., "18-25°C"). */
    @SerializedName("temperature")
    private String temperature;

    /** Optimal humidity level for the plant (e.g., "High", "40-60%"). */
    @SerializedName("humidity")
    private String humidity;

    // ===== Care Requirements =====

    /** Water requirement description (e.g., "Moderate", "Low"). */
    @SerializedName("waterNeeds")
    private String waterNeeds;

    /** Light requirement description (e.g., "Full Sun", "Partial Shade"). */
    @SerializedName("lightNeeds")
    private String lightNeeds;

    /** Recommended soil type (e.g., "Well-draining", "Peat-based"). */
    @SerializedName("soil")
    private String soil;

    /** Fertilizing recommendations and schedule (e.g., "Monthly during growing season"). */
    @SerializedName("fertilizing")
    private String fertilizing;

    /** Growing difficulty level (e.g., "Easy", "Moderate", "Difficult"). */
    @SerializedName("difficulty")
    private String difficulty;

    // ===== Botanical Features =====

    /** Description of the plant's physical features (e.g., leaf shape, flowers). */
    @SerializedName("features")
    private String features;

    /** Expected mature height (e.g., "2-3 meters", "30-50 cm"). */
    @SerializedName("growthHeight")
    private String growthHeight;

    /** The type of leaf (e.g., "Evergreen", "Deciduous"). */
    @SerializedName("leafType")
    private String leafType;

    /** Information on the plant's toxicity to pets or humans. */
    @SerializedName("toxicity")
    private String toxicity;

    /** Indicates if the plant has air-purifying qualities. */
    @SerializedName("airPurifying")
    private String airPurifying;


    /**
     * Converts this PlantWikiDto to the domain model (Plant) with wiki-specific field mapping.
     *
     * Mapping Strategy:
     * 1. Core fields: Direct mapping (name, scientificName, image, description).
     * 2. Wiki-specific: Map to Plant setters (e.g., features, soil, fertilizing).
     * 3. Care requirements: Map to dedicated fields (e.g., waterNeeds→waterRequirement).
     * 4. Tags: Generate from key wiki fields for display/search.
     * 5. User fields: Set to defaults (userId=0, isFavourite=false) as they don't apply.
     * 6. Location & Timestamps: Set to null as they are not relevant for wiki entries.
     *
     * @return A Plant object ready for display in UI fragments.
     */
    public Plant toPlant() {
        Log.d(TAG, "Converting PlantWikiDto to Plant for: " + name);

        // Create searchable tags from key wiki information
        List<String> tags = new java.util.ArrayList<>();
        if (waterNeeds != null && !waterNeeds.isEmpty()) tags.add("Water: " + waterNeeds);
        if (lightNeeds != null && !lightNeeds.isEmpty()) tags.add("Light: " + lightNeeds);
        if (difficulty != null && !difficulty.isEmpty()) tags.add("Difficulty: " + difficulty);
        if (growthHeight != null && !growthHeight.isEmpty()) tags.add("Height: " + growthHeight);

        // Create the base Plant object with data available in the constructor
        Plant plant = new Plant(
                plantWikiId != null ? plantWikiId.intValue() : 0, // Use wiki ID as plant ID
                0, // No user ownership for wiki plants
                name != null ? name : "Unknown Plant",
                image,
                description,
                null, // Wiki plants don't have latitude
                null, // Wiki plants don't have longitude
                scientificName,
                0, // Wiki plants don't belong to a user's garden
                false, // Wiki plants aren't favourited by default
                null, // Wiki plants don't have creation timestamps
                null, // Wiki plants don't have update timestamps
                tags,
                "Wiki", // Source identifier
                lightNeeds,
                waterNeeds,
                temperature, // Pass temperature
                humidity   // Pass humidity
        );

        // Set wiki-specific fields using setters for a cleaner constructor
        plant.setMatureHeight(growthHeight);
        plant.setFeatures(features);
        // Combine all other care details into a single "Care Guide" for the dedicated tab
        plant.setCareGuide(buildCareGuide());
        plant.setDifficulty(difficulty);
        plant.setSoilGuide(soil);
        plant.setFertilizerGuide(fertilizing);
        plant.setLeafType(leafType);
        plant.setToxicity(toxicity);
        plant.setAirPurifying(airPurifying);

        Log.d(TAG, "Successfully converted DTO to Plant object.");
        return plant;
    }

    /**
     * Helper method to build a comprehensive care guide string from various fields.
     * This can be used to populate a general-purpose care guide text view.
     * @return A formatted string containing all available care information.
     */
    private String buildCareGuide() {
        StringBuilder sb = new StringBuilder();
        if (soil != null && !soil.isEmpty()) {
            sb.append("Soil: ").append(soil).append("\n\n");
        }
        if (fertilizing != null && !fertilizing.isEmpty()) {
            sb.append("Fertilizing: ").append(fertilizing).append("\n\n");
        }
        // You can append other details like toxicity, leaf type etc. here if desired.
        // For now, we keep it focused on direct care actions.
        return sb.toString().trim();
    }


    // ===== Getters =====
    // Provide access to all DTO fields.

    public Long getPlantWikiId() { return plantWikiId; }
    public String getName() { return name; }
    public String getScientificName() { return scientificName; }
    public String getImage() { return image; }
    public String getDescription() { return description; }
    public String getTemperature() { return temperature; }
    public String getHumidity() { return humidity; }
    public String getWaterNeeds() { return waterNeeds; }
    public String getLightNeeds() { return lightNeeds; }
    public String getSoil() { return soil; }
    public String getFertilizing() { return fertilizing; }
    public String getDifficulty() { return difficulty; }
    public String getFeatures() { return features; }
    public String getGrowthHeight() { return growthHeight; }
    public String getLeafType() { return leafType; }
    public String getToxicity() { return toxicity; }
    public String getAirPurifying() { return airPurifying; }
}
