package com.example.myapplication.network;

import com.example.myapplication.ui.myplants.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data Transfer Object (DTO) for a plant, matching the JSON structure from the backend API.
 * This class is used by Retrofit and GSON to parse the network response.
 */
public class PlantDto {

    // All fields from your backend API response's "data" object
    @SerializedName("plantId")
    private int plantId;
    @SerializedName("userId")
    private int userId;
    @SerializedName("name")
    private String name;
    @SerializedName("image")
    private String image;
    @SerializedName("description")
    private String description;
    @SerializedName("latitude")
    private Double latitude;
    @SerializedName("longitude")
    private Double longitude;
    @SerializedName("scientificName")
    private String scientificName;
    @SerializedName("gardenId")
    private int gardenId;
    @SerializedName("isFavourite")
    private boolean isFavourite;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;

    // These fields are included for completeness based on previous errors.
    // If your API doesn't send them, they will simply be null.
    @SerializedName("tags")
    private List<String> tags;
    @SerializedName("discoveredBy")
    private String discoveredBy;
    @SerializedName("lightRequirement")
    private String lightRequirement;
    @SerializedName("waterRequirement")
    private String waterRequirement;
    @SerializedName("temperatureRequirement")
    private String temperatureRequirement;
    @SerializedName("humidityRequirement")
    private String humidityRequirement;

    /**
     * Converts this Data Transfer Object (DTO) to the app's domain model object (Plant).
     * This allows the rest of the app to work with a clean, Parcelable Plant object.
     * @return A new Plant object populated with data from this DTO.
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

    // --- Getters ---
    // The presence of this method will fix the "Cannot resolve method" error.
    public String getScientificName() {
        return scientificName;
    }

    // It's good practice to add getters for any other fields you might need to access directly.
    public int getPlantId() {
        return plantId;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDiscoveredBy() {
        return discoveredBy;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
