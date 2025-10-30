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
    
    /** Unique ID in plant wiki database */
    @SerializedName("plantWikiId")
    private Long plantWikiId;

    /** Common/display name of the plant */
    @SerializedName("name")
    private String name;

    /** Scientific/botanical name */
    @SerializedName("scientificName")
    private String scientificName;

    /** Base64 encoded image string */
    @SerializedName("image")
    private String image;

    // ===== Descriptive Information =====
    
    /** Detailed description of the plant (used in overview tab) */
    @SerializedName("description")
    private String description;

    /** Physical and botanical features (used in features tab) */
    @SerializedName("features")
    private String features;

    /** Complete care instructions (used in care guide tab) */
    @SerializedName("careGuide")
    private String careGuide;

    // ===== Care Requirements =====
    
    /** Water requirement description (e.g., "Moderate", "Low") */
    @SerializedName("waterNeeds")
    private String waterNeeds;

    /** Light requirement description (e.g., "Full Sun", "Partial Shade") */
    @SerializedName("lightNeeds")
    private String lightNeeds;

    /** Growing difficulty level (e.g., "Easy", "Moderate", "Difficult") */
    @SerializedName("difficulty")
    private String difficulty;

    /** Expected mature height (e.g., "2-3 meters", "30-50 cm") */
    @SerializedName("growthHeight")
    private String growthHeight;

    /**
     * Converts PlantWikiDto to domain model (Plant) with wiki-specific field mapping.
     * 
     * Mapping Strategy:
     * 1. Core fields: Direct mapping (name, scientificName, image, description)
     * 2. Wiki-specific: Map to Plant setters (features, careGuide, matureHeight)
     * 3. Care requirements: Map waterNeeds→waterRequirement, lightNeeds→lightRequirement
     * 4. Tags: Generate from wiki fields for display/search
     * 5. User fields: Set to defaults (userId=0, gardenId=0, isFavourite=false)
     * 6. Location: Set to null (wiki plants are not location-specific)
     * 7. Timestamps: Set to null (wiki plants don't have creation dates)
     * 
     * @return Plant object ready for display in PlantWikiFragment and detail views
     */
    public Plant toPlant() {
        // Log conversion for debugging
        Log.d(TAG, "Converting PlantWikiDto to Plant:");
        Log.d(TAG, "  Name: " + name);
        Log.d(TAG, "  Description: " + description);
        Log.d(TAG, "  WaterNeeds: " + waterNeeds);
        Log.d(TAG, "  LightNeeds: " + lightNeeds);
        Log.d(TAG, "  GrowthHeight: " + growthHeight);
        Log.d(TAG, "  Features: " + features);
        Log.d(TAG, "  CareGuide: " + careGuide);
        
        // Create searchable tags from wiki-specific information
        List<String> tags = new java.util.ArrayList<>();
        if (waterNeeds != null && !waterNeeds.isEmpty()) {
            tags.add("Water: " + waterNeeds);
        }
        if (lightNeeds != null && !lightNeeds.isEmpty()) {
            tags.add("Light: " + lightNeeds);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            tags.add("Difficulty: " + difficulty);
        }
        if (growthHeight != null && !growthHeight.isEmpty()) {
            tags.add("Height: " + growthHeight);
        }

        // Create Plant object with wiki data mapped to appropriate fields
        Plant plant = new Plant(
                plantWikiId != null ? plantWikiId.intValue() : 0, // Use wiki ID as plant ID
                0, // No user ownership for wiki plants
                name != null ? name : "Unknown Plant", // Display name
                image, // Base64 image string
                description, // Detailed description for overview tab
                null, // Wiki plants don't have GPS coordinates
                null, // Wiki plants don't have GPS coordinates
                scientificName, // Botanical name
                0, // Wiki plants don't belong to gardens
                false, // Wiki plants can't be favourited (until added to garden)
                null, // Wiki plants don't have creation timestamps
                null, // Wiki plants don't have update timestamps
                tags, // Generated tags for search/filter
                "Wiki", // Source identifier
                lightNeeds, // Care requirement: light
                waterNeeds, // Care requirement: water
                null, // Temperature not provided by wiki API
                null  // Humidity not provided by wiki API
        );
        
        // Set wiki-specific fields using setters (not in constructor)
        plant.setMatureHeight(growthHeight); // Expected growth height
        plant.setFeatures(features); // Physical/botanical features
        plant.setCareGuide(careGuide); // Complete care instructions
        
        // Log successful conversion
        Log.d(TAG, "Plant object created with description: " + plant.getDescription());
        Log.d(TAG, "Plant object created with waterRequirement: " + plant.getWaterRequirement());
        Log.d(TAG, "Plant object created with lightRequirement: " + plant.getLightRequirement());
        
        return plant;
    }

    // ===== Getters =====
    // Provide access to DTO fields for direct usage
    
    public Long getPlantWikiId() { return plantWikiId; }
    public String getName() { return name; }
    public String getScientificName() { return scientificName; }
    public String getImage() { return image; }
    public String getDescription() { return description; }
    public String getFeatures() { return features; }
    public String getCareGuide() { return careGuide; }
    public String getWaterNeeds() { return waterNeeds; }
    public String getLightNeeds() { return lightNeeds; }
    public String getDifficulty() { return difficulty; }
    public String getGrowthHeight() { return growthHeight; }
}
