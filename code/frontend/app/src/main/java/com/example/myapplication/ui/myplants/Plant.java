package com.example.myapplication.ui.myplants;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Represents a plant object. This class is Parcelable to allow it
 * to be passed between Android components like Fragments.
 */
public class Plant implements Parcelable {

    // --- Fields based on your requirements ---
    private String plantId;        // Unique ID for the plant
    private String name;           // Common name, e.g., "Oak Tree"
    private String scientificName; // e.g., "Quercus robur"
    private String introduction;   // Description of the plant
    private String location;       // Where it was found
    private String searchTag;      // User-defined tags for searching
    private String discoveredBy;   // User who discovered it
    private long discoveredOn;     // Timestamp of discovery
    private String imageUrl;       // URL for the plant's image
    private boolean isFavourite;   // If it's marked as a favourite

    /**
     * Full constructor for creating a new Plant object.
     */
    public Plant(String plantId, String scientificName, String name, String introduction,
                 String location, String searchTag, String discoveredBy, long discoveredOn,
                 String imageUrl, boolean isFavourite) {
        this.plantId = plantId;
        this.name = name;
        this.scientificName = scientificName;
        this.introduction = introduction;
        this.location = location;
        this.searchTag = searchTag;
        this.discoveredBy = discoveredBy;
        this.discoveredOn = discoveredOn;
        this.imageUrl = imageUrl;
        this.isFavourite = isFavourite;
    }

    // --- Getters ---
    public String getPlantId() { return plantId; }
    public String getName() { return name; }
    public String getScientificName() { return scientificName; }
    public String getIntroduction() { return introduction; }
    public String getLocation() { return location; }
    public String getSearchTag() { return searchTag; }
    public String getDiscoveredBy() { return discoveredBy; }
    public long getDiscoveredOn() { return discoveredOn; }
    public String getImageUrl() { return imageUrl; }
    public boolean isFavourite() { return isFavourite; }

    // --- Setters ---
    public void setFavourite(boolean favourite) { isFavourite = favourite; }


    // --- PARCELABLE IMPLEMENTATION ---

    /**
     * Constructor to create a Plant object from a Parcel.
     * The order of reads MUST match the order of writes in writeToParcel().
     */
    protected Plant(Parcel in) {
        plantId = in.readString();
        name = in.readString();
        scientificName = in.readString();
        introduction = in.readString();
        location = in.readString();
        searchTag = in.readString();
        discoveredBy = in.readString();
        discoveredOn = in.readLong();
        imageUrl = in.readString();
        isFavourite = in.readByte() != 0; // readByte returns 1 for true, 0 for false
    }

    /**
     * Required Creator for generating instances of your Parcelable class from a Parcel.
     */
    public static final Creator<Plant> CREATOR = new Creator<Plant>() {
        @Override
        public Plant createFromParcel(Parcel in) {
            return new Plant(in);
        }

        @Override
        public Plant[] newArray(int size) {
            return new Plant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0; // Default implementation
    }

    /**
     * Flattens this object into a Parcel.
     * The order of writes MUST match the order of reads in the constructor.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(plantId);
        dest.writeString(name);
        dest.writeString(scientificName);
        dest.writeString(introduction);
        dest.writeString(location);
        dest.writeString(searchTag);
        dest.writeString(discoveredBy);
        dest.writeLong(discoveredOn);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isFavourite ? 1 : 0)); // Write boolean as a byte
    }
}
