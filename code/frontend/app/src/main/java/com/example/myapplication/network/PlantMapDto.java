package com.example.myapplication.network;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Plant DTO specifically designed for map functionality.
 * Matches backend PlantVO for nearby plants payload.
 */
public class PlantMapDto implements Serializable {

    // Core identification fields (match backend PlantVO types)
    @SerializedName("plantId")
    private Long plantId;
    
    @SerializedName("userId")
    private Long userId;
    
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
    private Long gardenId;

    // New field from backend VO
    @SerializedName("scientificName")
    private String scientificName;
    
    @SerializedName("discoveredBy")
    private String discoveredBy;

    // Constructors
    public PlantMapDto() {}

    public PlantMapDto(String name, String description, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PlantMapDto(Long plantId, Long userId, String name, String description, 
                      Double latitude, Double longitude, Long gardenId, String scientificName) {
        this.plantId = plantId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gardenId = gardenId;
        this.scientificName = scientificName;
    }

    // Getters
    public Long getPlantId() {
        return plantId;
    }

    public Long getUserId() {
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

    public Long getGardenId() {
        return gardenId;
    }
    
    public String getScientificName() {
        return scientificName;
    }
    
    public String getDiscoveredBy() {
        return discoveredBy;
    }

    // Setters
    public void setPlantId(Long plantId) {
        this.plantId = plantId;
    }

    public void setUserId(Long userId) {
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

    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }
    
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
    
    public void setDiscoveredBy(String discoveredBy) {
        this.discoveredBy = discoveredBy;
    }

    /**
     * Converts PlantMapDto to PlantDto for compatibility with existing code.
     * Note: image and scientificName will be null in the converted PlantDto.
     */
    public PlantDto toPlantDto() {
        PlantDto plantDto = new PlantDto();
        plantDto.setPlantId(this.plantId != null ? this.plantId.intValue() : 0);
        plantDto.setUserId(this.userId != null ? this.userId.intValue() : 0);
        plantDto.setName(this.name);
        plantDto.setDescription(this.description);
        plantDto.setLatitude(this.latitude);
        plantDto.setLongitude(this.longitude);
        plantDto.setGardenId(this.gardenId != null ? this.gardenId.intValue() : 0);
        return plantDto;
    }

    /**
     * Creates PlantMapDto from PlantDto.
     * Excludes image and scientificName fields.
     */
    public static PlantMapDto fromPlantDto(PlantDto plantDto) {
        PlantMapDto plantMapDto = new PlantMapDto();
        plantMapDto.setPlantId((long) plantDto.getPlantId());
        plantMapDto.setUserId((long) plantDto.getUserId());
        plantMapDto.setName(plantDto.getName());
        plantMapDto.setDescription(plantDto.getDescription());
        plantMapDto.setLatitude(plantDto.getLatitude());
        plantMapDto.setLongitude(plantDto.getLongitude());
        plantMapDto.setGardenId((long) plantDto.getGardenId());
        plantMapDto.setScientificName(plantDto.getScientificName());
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
        if (scientificName != null && !scientificName.isEmpty()) {
            sb.append("\n").append(scientificName);
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
                ", scientificName='" + scientificName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlantMapDto that = (PlantMapDto) obj;
        return plantId != null && plantId.equals(that.plantId);
    }

    @Override
    public int hashCode() {
        return plantId != null ? plantId.hashCode() : 0;
    }
}
