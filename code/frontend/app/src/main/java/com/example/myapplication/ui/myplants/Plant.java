package com.example.myapplication.ui.myplants;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Plant implements Parcelable {

    // All fields from your backend API response, correctly typed
    @SerializedName("plantId")
    private int plantId;
    @SerializedName("userId")
    private int userId;
    @SerializedName("name")
    private String name;
    @SerializedName("image")
    private String imageUrl;
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

    // --- We will assume these are also part of your full Plant object based on previous errors ---
    // If they are not, they can be safely removed, but it's better to have them.
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
    @SerializedName("matureHeight")
    private String matureHeight;
    @SerializedName("leafType")
    private String leafType;
    @SerializedName("toxicity")
    private String toxicity;
    @SerializedName("airPurifying")
    private String airPurifying;
    @SerializedName("soilGuide")
    private String soilGuide;
    @SerializedName("fertilizerGuide")
    private String fertilizerGuide;
    @SerializedName("features")
    private String features;
    @SerializedName("careGuide")
    private String careGuide;

    // --- FIX: A public constructor for the DTO to use for conversion ---
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

    // --- Parcelable implementation (handles passing object between fragments) ---
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
        matureHeight = in.readString();
        leafType = in.readString();
        toxicity = in.readString();
        airPurifying = in.readString();
        soilGuide = in.readString();
        fertilizerGuide = in.readString();
        features = in.readString();
        careGuide = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(plantId);
        dest.writeInt(userId);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(description);
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
        dest.writeString(matureHeight);
        dest.writeString(leafType);
        dest.writeString(toxicity);
        dest.writeString(airPurifying);
        dest.writeString(soilGuide);
        dest.writeString(fertilizerGuide);
        dest.writeString(features);
        dest.writeString(careGuide);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Plant> CREATOR = new Creator<Plant>() {
        @Override
        public Plant createFromParcel(Parcel in) { return new Plant(in); }
        @Override
        public Plant[] newArray(int size) { return new Plant[size]; }
    };

    // --- Getters for all fields ---
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

    public String getMatureHeight() {
        return matureHeight;
    }

    public String getLeafType() {
        return leafType;
    }

    public String getToxicity() {
        return toxicity;
    }

    public String getAirPurifying() {
        return airPurifying;
    }
    public String getSoilGuide() { return soilGuide; }
    public String getFertilizerGuide() { return fertilizerGuide; }
    public String getFeatures() { return features; }
    public String getCareGuide() { return careGuide; }

    // Setters for wiki-specific fields
    public void setMatureHeight(String matureHeight) { this.matureHeight = matureHeight; }
    public void setFeatures(String features) { this.features = features; }
    public void setCareGuide(String careGuide) { this.careGuide = careGuide; }
}
