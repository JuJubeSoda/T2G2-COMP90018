package com.example.myapplication.network;

import com.example.myapplication.ui.myplants.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data Transfer Object (DTO) for PlantWiki, matching the JSON structure from the wiki API.
 * This class represents the richer plant data available in the wiki.
 */
public class PlantWikiDto {

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

    @SerializedName("GrowthHeight")
    private String growthHeight;

    /**
     * Converts this PlantWiki DTO to the app's domain model object (Plant).
     * Maps the wiki-specific fields to the Plant object.
     */
    public Plant toPlant() {
        // Create a comprehensive description that includes all wiki information
        StringBuilder fullDescription = new StringBuilder();
        if (description != null && !description.isEmpty()) {
            fullDescription.append(description);
        }
        if (features != null && !features.isEmpty()) {
            if (fullDescription.length() > 0) fullDescription.append("\n\n");
            fullDescription.append("Features: ").append(features);
        }
        if (careGuide != null && !careGuide.isEmpty()) {
            if (fullDescription.length() > 0) fullDescription.append("\n\n");
            fullDescription.append("Care Guide: ").append(careGuide);
        }

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

        return new Plant(
                plantWikiId != null ? plantWikiId.intValue() : 0, // plantId
                0, // userId (not applicable for wiki)
                name != null ? name : "Unknown Plant", // name
                image, // imageUrl
                fullDescription.toString(), // description (enhanced with wiki info)
                null, // latitude (not available in wiki)
                null, // longitude (not available in wiki)
                scientificName, // scientificName
                0, // gardenId (not applicable for wiki)
                false, // isFavourite (not applicable for wiki)
                null, // createdAt (not available in wiki)
                null, // updatedAt (not available in wiki)
                tags, // tags (created from wiki fields)
                "Wiki", // discoveredBy (wiki source)
                lightNeeds, // lightRequirement
                waterNeeds, // waterRequirement
                null, // temperatureRequirement (not available)
                null  // humidityRequirement (not available)
        );
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
