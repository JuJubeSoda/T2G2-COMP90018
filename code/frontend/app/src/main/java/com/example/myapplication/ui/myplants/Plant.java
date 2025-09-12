package com.example.myapplication.ui.myplants; // Adjust the package name to match your project structure

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a plant with its common details.
 * Implements Parcelable to allow objects of this class to be passed between components
 * (e.g., via Intent extras or Fragment arguments).
 */
public class Plant implements Parcelable {

    private String id; // Unique identifier for the plant (optional, but good for database operations)
    private String name; // Common name of the plant
    private String scientificName; // Scientific (botanical) name
    private String imageUrl; // URL or URI string for the plant's image
    private String description; // A short description or notes about the plant
    private long dateAdded; // Timestamp (e.g., System.currentTimeMillis()) when the plant was added
    private boolean isFavourite; // To track if the plant is marked as a favourite

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Plant.class)
     * when using Firebase Realtime Database or Firestore.
     */
    public Plant() {
        // Default constructor
    }

    /**
     * Constructs a new Plant object.
     *
     * @param id             Unique ID of the plant.
     * @param name           Common name of the plant.
     * @param scientificName Scientific name of the plant.
     * @param imageUrl       URL or URI for the plant's image.
     * @param description    Description or notes for the plant.
     * @param dateAdded      Timestamp when the plant was added to the collection.
     * @param isFavourite    Boolean indicating if the plant is a favourite.
     */
    public Plant(String id, String name, String scientificName, String imageUrl, String description, long dateAdded, boolean isFavourite) {
        this.id = id;
        this.name = name;
        this.scientificName = scientificName;
        this.imageUrl = imageUrl;
        this.description = description;
        this.dateAdded = dateAdded;
        this.isFavourite = isFavourite;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    // Setters (useful if you need to modify plant objects after creation)
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    // Parcelable implementation (allows passing Plant objects between components)
    protected Plant(Parcel in) {
        id = in.readString();
        name = in.readString();
        scientificName = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        dateAdded = in.readLong();
        isFavourite = in.readByte() != 0; // Read boolean as byte
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(scientificName);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeLong(dateAdded);
        dest.writeByte((byte) (isFavourite ? 1 : 0)); // Write boolean as byte
    }

    @Override
    public int describeContents() {
        return 0; // Typically 0, unless it contains a FileDescriptor
    }

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

    // equals() and hashCode() (important for comparing Plant objects, e.g., in lists or sets)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plant plant = (Plant) o;
        // Primarily check based on ID if available and unique, otherwise combine key fields
        if (id != null) {
            return id.equals(plant.id);
        }
        // Fallback if ID is not the primary comparator or not always present
        return dateAdded == plant.dateAdded &&
                isFavourite == plant.isFavourite &&
                Objects.equals(name, plant.name) &&
                Objects.equals(scientificName, plant.scientificName) &&
                Objects.equals(imageUrl, plant.imageUrl) &&
                Objects.equals(description, plant.description);
    }

    @Override
    public int hashCode() {
        // Use ID if available and unique, otherwise combine key fields
        if (id != null) {
            return Objects.hash(id);
        }
        // Fallback
        return Objects.hash(name, scientificName, imageUrl, description, dateAdded, isFavourite);
    }

    // toString() (useful for logging and debugging)
    @NonNull
    @Override
    public String toString() {
        return "Plant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", dateAdded=" + dateAdded +
                ", isFavourite=" + isFavourite +
                '}';
    }
}
