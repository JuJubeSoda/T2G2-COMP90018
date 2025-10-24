package com.example.myapplication.network;

import com.example.myapplication.ui.myplants.Plant;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Unified Plant model for network transmission and data persistence.
 * This class serves as both DTO and domain model.
 */
public class PlantDto implements Serializable {

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

    // Constructors from model/Plant
    public PlantDto() {}

    public PlantDto(String name, String description, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

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

    public int getUserId() {
        return userId;
    }

    public int getGardenId() {
        return gardenId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getLightRequirement() {
        return lightRequirement;
    }

    public String getWaterRequirement() {
        return waterRequirement;
    }

    public String getTemperatureRequirement() {
        return temperatureRequirement;
    }

    public String getHumidityRequirement() {
        return humidityRequirement;
    }

    // --- Setters from model/Plant ---
    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public void setGardenId(int gardenId) {
        this.gardenId = gardenId;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setDiscoveredBy(String discoveredBy) {
        this.discoveredBy = discoveredBy;
    }

    public void setLightRequirement(String lightRequirement) {
        this.lightRequirement = lightRequirement;
    }

    public void setWaterRequirement(String waterRequirement) {
        this.waterRequirement = waterRequirement;
    }

    public void setTemperatureRequirement(String temperatureRequirement) {
        this.temperatureRequirement = temperatureRequirement;
    }

    public void setHumidityRequirement(String humidityRequirement) {
        this.humidityRequirement = humidityRequirement;
    }

    // --- toString method from model/Plant ---
    @Override
    public String toString() {
        return "PlantDto{" +
                "plantId=" + plantId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", scientificName='" + scientificName + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", gardenId=" + gardenId +
                ", isFavourite=" + isFavourite +
                '}';
    }
}
