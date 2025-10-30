package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Plant DTO specifically designed for map functionality.
 * Optimized for map display with reduced data payload.
 * Excludes image and scientificName fields to improve performance.
 */
public class PlantMapDto implements Serializable {

    // Core identification fields
    @SerializedName("plantId")
    private int plantId;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    // Location fields (essential for map functionality)
    @SerializedName("latitude")
    private Double latitude;
    
    @SerializedName("longitude")
    private Double longitude;
    
    @SerializedName("gardenId")
    private int gardenId;
    
    // Status/time fields已移除：isFavourite/createdAt/updatedAt

    // Additional info for map display
    @SerializedName("tags")
    private List<String> tags;

    // Constructors
    public PlantMapDto() {}

    public PlantMapDto(String name, String description, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PlantMapDto(int plantId, int userId, String name, String description, 
                      Double latitude, Double longitude, int gardenId) {
        this.plantId = plantId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gardenId = gardenId;
    }

    // Getters
    public int getPlantId() {
        return plantId;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public int getGardenId() {
        return gardenId;
    }

    public List<String> getTags() {
        return tags;
    }

    // Setters
    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setGardenId(int gardenId) {
        this.gardenId = gardenId;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Converts PlantMapDto to PlantDto for compatibility with existing code.
     * Note: image and scientificName will be null in the converted PlantDto.
     */
    public PlantDto toPlantDto() {
        PlantDto plantDto = new PlantDto();
        plantDto.setPlantId(this.plantId);
        plantDto.setUserId(this.userId);
        plantDto.setName(this.name);
        plantDto.setDescription(this.description);
        plantDto.setLatitude(this.latitude);
        plantDto.setLongitude(this.longitude);
        plantDto.setGardenId(this.gardenId);
        plantDto.setTags(this.tags);
        // image and scientificName remain null
        return plantDto;
    }

    /**
     * Creates PlantMapDto from PlantDto.
     * Excludes image and scientificName fields.
     */
    public static PlantMapDto fromPlantDto(PlantDto plantDto) {
        PlantMapDto plantMapDto = new PlantMapDto();
        plantMapDto.setPlantId(plantDto.getPlantId());
        plantMapDto.setUserId(plantDto.getUserId());
        plantMapDto.setName(plantDto.getName());
        plantMapDto.setDescription(plantDto.getDescription());
        plantMapDto.setLatitude(plantDto.getLatitude());
        plantMapDto.setLongitude(plantDto.getLongitude());
        plantMapDto.setGardenId(plantDto.getGardenId());
        plantMapDto.setTags(plantDto.getTags());
        return plantMapDto;
    }


    /**
     * Checks if the plant has valid coordinates for map display.
     */
    public boolean hasValidCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Gets a short description for map marker popup.
     */
    public String getMapPopupText() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (description != null && !description.isEmpty()) {
            sb.append("\n").append(description);
        }
        if (tags != null && !tags.isEmpty()) {
            sb.append("\nTags: ").append(String.join(", ", tags));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "PlantMapDto{" +
                "plantId=" + plantId +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", isFavourite=" + isFavourite +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlantMapDto that = (PlantMapDto) obj;
        return plantId == that.plantId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(plantId);
    }
}
