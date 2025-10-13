package com.example.myapplication.network;

import com.example.myapplication.ui.myplants.Plant;
import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object (DTO) for a plant, matching the structure from the backend API.
 */
public class PlantDto {

    @SerializedName("plantId")
    private Long plantId;

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image; // Base64 encoded string

    @SerializedName("description")
    private String description;

    @SerializedName("scientificName")
    private String scientificName;

    @SerializedName("gardenId")
    private Long gardenId;

    /**
     * Converts this Data Transfer Object (DTO) to a domain model object (Plant).
     * This method correctly maps the DTO fields to the Plant's constructor arguments.
     *
     * @return A new Plant object populated with data from this DTO.
     */
    public Plant toPlant() {
        return new Plant(
            this.plantId != null ? String.valueOf(this.plantId) : "",
            this.scientificName,
            this.name,
            this.description, // DTO's description maps to Plant's introduction
            this.gardenId != null ? String.valueOf(this.gardenId) : "", // DTO's gardenId maps to Plant's location
            "", // searchTag - not provided by backend, set to empty
            null, // discoveredBy - not provided, set to null
            0L,   // discoveredOn - not provided, set to 0
            this.image, // DTO's image (Base64) maps to Plant's imageUrl
            false // isFavourite - not provided, default to false
        );
    }
}
