package com.example.myapplication.network;

import android.util.Log;
import com.example.myapplication.ui.myplants.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data Transfer Object (DTO) for PlantWiki, matching the JSON structure from the wiki API.
 * This class represents the richer plant data available in the wiki.
 */
public class PlantWikiDto {
    
    private static final String TAG = "PlantWikiDto";

    @SerializedName("plantWikiId")
    private Long plantWikiId;

    @SerializedName("name")
    private String name;

    @SerializedName("scientificName")
    private String scientificName;

    @SerializedName("image")
    private String image; // Base64 encoded string

    @SerializedName("description")
    private String description;

    @SerializedName("features")
    private String features;

    @SerializedName("careGuide")
    private String careGuide;

    @SerializedName("waterNeeds")
    private String waterNeeds;

    @SerializedName("lightNeeds")
    private String lightNeeds;

    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("growthHeight")
    private String growthHeight;

    /**
     * Converts this PlantWiki DTO to the app's domain model object (Plant).
     * Maps the wiki-specific fields to the Plant object.
     */
    public Plant toPlant() {
        Log.d(TAG, "Converting PlantWikiDto to Plant:");
        Log.d(TAG, "  Name: " + name);
        Log.d(TAG, "  Description: " + description);
        Log.d(TAG, "  WaterNeeds: " + waterNeeds);
        Log.d(TAG, "  LightNeeds: " + lightNeeds);
        Log.d(TAG, "  GrowthHeight: " + growthHeight);
        Log.d(TAG, "  Features: " + features);
        Log.d(TAG, "  CareGuide: " + careGuide);
        
        // Create tags from the wiki-specific information
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

        // Create a Plant object with all wiki-specific fields properly mapped
        Plant plant = new Plant(
                plantWikiId != null ? plantWikiId.intValue() : 0, // plantId
                0, // userId (not applicable for wiki)
                name != null ? name : "Unknown Plant", // name
                image, // imageUrl
                description, // description (from API)
                null, // latitude (not available in wiki)
                null, // longitude (not available in wiki)
                scientificName, // scientificName
                0, // gardenId (not applicable for wiki)
                false, // isFavourite (not applicable for wiki)
                null, // createdAt (not available in wiki)
                null, // updatedAt (not available in wiki)
                tags, // tags (created from wiki fields)
                "Wiki", // discoveredBy (wiki source)
                lightNeeds, // lightRequirement (maps to lightNeeds from API)
                waterNeeds, // waterRequirement (maps to waterNeeds from API)
                null, // temperatureRequirement (not available)
                null  // humidityRequirement (not available)
        );
        
        // Set additional wiki-specific fields using setters
        plant.setMatureHeight(growthHeight);
        plant.setFeatures(features);
        plant.setCareGuide(careGuide);
        
        Log.d(TAG, "Plant object created with description: " + plant.getDescription());
        Log.d(TAG, "Plant object created with waterRequirement: " + plant.getWaterRequirement());
        Log.d(TAG, "Plant object created with lightRequirement: " + plant.getLightRequirement());
        
        return plant;
    }

    // Getters
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
