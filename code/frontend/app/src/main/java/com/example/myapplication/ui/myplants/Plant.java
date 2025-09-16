// Specifies the package where this Plant class resides.
// Ensure this matches your project's structure for proper compilation and access.
package com.example.myapplication.ui.myplants;

// Android framework imports for core functionalities.
import android.os.Parcel; // For creating Parcelable objects, enabling them to be passed between components.
import android.os.Parcelable; // Interface for classes whose instances can be written to and restored from a Parcel.

// AndroidX (Jetpack) library import for annotations.
import androidx.annotation.NonNull; // Annotation indicating a parameter, field, or method return value can never be null.

// Java utility import for object operations.
import java.util.Objects; // Utility class for operations on objects, such as `equals` and `hashCode`.

/**
 * Represents a plant with its common details, such as name, scientific name, image URL, etc.
 * This class implements the {@link Parcelable} interface, which allows objects of this class
 * to be efficiently passed between different Android components (e.g., via Intent extras when
 * starting an Activity, or as arguments when creating a Fragment).
 */
public class Plant implements Parcelable {

    // --- Class Fields ---

    // Unique identifier for the plant.
    // This is optional but highly recommended for database operations or unique identification.
    private String id;

    // Common name of the plant (e.g., "Rose", "Sunflower").
    private String name;

    // Scientific (botanical) name of the plant (e.g., "Rosa rubiginosa", "Helianthus annuus").
    private String scientificName;

    // URL or URI string pointing to an image of the plant.
    private String imageUrl;

    // A short description or notes about the plant.
    private String description;

    // Timestamp (e.g., from System.currentTimeMillis()) indicating when the plant was added
    // to a collection or database. Stored as a long.
    private long dateAdded;

    // Boolean flag to track if the plant has been marked as a favourite by the user.
    private boolean isFavourite;


    // --- Constructors ---

    /**
     * Default (no-argument) constructor.
     * This is often required by libraries like Firebase Realtime Database or Firestore
     * when deserializing data from a DataSnapshot directly into a Plant object
     * (e.g., using `DataSnapshot.getValue(Plant.class)`).
     */
    public Plant() {
        // Default constructor: Initializes fields to their default values (null for objects, 0 for long, false for boolean).
    }

    /**
     * Constructs a new Plant object with specified details.
     *
     * @param id             The unique identifier for the plant. Can be null if not used or generated later.
     * @param name           The common name of the plant.
     * @param scientificName The scientific (botanical) name of the plant.
     * @param imageUrl       The URL or URI string for the plant's image.
     * @param description    A short description or notes about the plant.
     * @param dateAdded      The timestamp (long) when the plant was added.
     * @param isFavourite    A boolean indicating if the plant is marked as a favourite.
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


    // --- Getters ---
    // Provide read-only access to the plant's properties.

    /** @return The unique identifier of the plant. */
    public String getId() {
        return id;
    }

    /** @return The common name of the plant. */
    public String getName() {
        return name;
    }

    /** @return The scientific name of the plant. */
    public String getScientificName() {
        return scientificName;
    }

    /** @return The URL or URI for the plant's image. */
    public String getImageUrl() {
        return imageUrl;
    }

    /** @return A description or notes for the plant. */
    public String getDescription() {
        return description;
    }

    /** @return The timestamp when the plant was added. */
    public long getDateAdded() {
        return dateAdded;
    }

    /** @return True if the plant is marked as a favourite, false otherwise. */
    public boolean isFavourite() { // Standard getter name for boolean is "isPropertyName"
        return isFavourite;
    }


    // --- Setters ---
    // Provide methods to modify the plant's properties after creation.
    // Useful if plant objects need to be updated (e.g., changing favourite status, editing details).

    /** Sets the unique identifier of the plant. @param id The new ID. */
    public void setId(String id) {
        this.id = id;
    }

    /** Sets the common name of the plant. @param name The new common name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Sets the scientific name of the plant. @param scientificName The new scientific name. */
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    /** Sets the URL or URI for the plant's image. @param imageUrl The new image URL. */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** Sets the description for the plant. @param description The new description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Sets the timestamp when the plant was added. @param dateAdded The new timestamp. */
    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    /** Sets the favourite status of the plant. @param favourite True if favourite, false otherwise. */
    public void setFavourite(boolean favourite) { // Parameter name 'favourite' matches the field.
        isFavourite = favourite;
    }


    // --- Parcelable Implementation ---
    // This section makes Plant objects serializable so they can be passed between Android components.

    /**
     * Constructor used when creating a Plant object from a Parcel.
     * Reads the object's properties from the Parcel in the same order they were written.
     *
     * @param in The Parcel to read the object's data from.
     */
    protected Plant(Parcel in) {
        id = in.readString();
        name = in.readString();
        scientificName = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        dateAdded = in.readLong();
        // Booleans are written as bytes (0 or 1) in Parcelable.
        // Read the byte and convert it back to boolean (0 means false, non-zero means true).
        isFavourite = in.readByte() != 0;
    }

    /**
     * Writes the Plant object's data to the provided Parcel.
     * Properties must be written in a specific order, and read back in the same order
     * by the `Plant(Parcel in)` constructor.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(scientificName);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeLong(dateAdded);
        // Write boolean as a byte (1 for true, 0 for false).
        dest.writeByte((byte) (isFavourite ? 1 : 0));
    }

    /**
     * Describes the kinds of special objects contained in this Parcelable instance's marshaled representation.
     * For most objects, this will return 0. If the object includes a FileDescriptor,
     * this method should return {@link #CONTENTS_FILE_DESCRIPTOR}.
     *
     * @return A bitmask indicating the set of special object types marshaled by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0; // Typically 0, unless the object contains a FileDescriptor.
    }

    /**
     * Static field that generates instances of your Parcelable class from a Parcel.
     * This CREATOR field is required for any class that implements Parcelable.
     */
    public static final Creator<Plant> CREATOR = new Creator<Plant>() {
        /**
         * Create a new instance of the Plant class, instantiating it from the given Parcel
         * whose data had previously been written by {@link Parcelable#writeToParcel Parcelable.writeToParcel()}.
         *
         * @param in The Parcel to read the object's data from.
         * @return Returns a new instance of the Plant class.
         */
        @Override
        public Plant createFromParcel(Parcel in) {
            return new Plant(in);
        }

        /**
         * Create a new array of the Plant class.
         *
         * @param size Size of the array to create.
         * @return Returns an array of the Plant class, with every entry initialized to null.
         */
        @Override
        public Plant[] newArray(int size) {
            return new Plant[size];
        }
    };


    // --- equals() and hashCode() ---
    // These methods are important for correctly comparing Plant objects,
    // especially when used in collections like Lists or Sets, or as keys in Maps.

    /**
     * Indicates whether some other object is "equal to" this one.
     * The `equals` method implements an equivalence relation on non-null object references.
     * This implementation primarily checks for equality based on the `id` field if it's available and unique.
     * If `id` is null or not the primary comparator, it falls back to comparing other key fields.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        // Check for reference equality (if they are the same object).
        if (this == o) return true;
        // Check if the other object is null or of a different class.
        if (o == null || getClass() != o.getClass()) return false;
        // Cast the other object to Plant type.
        Plant plant = (Plant) o;

        // Primary comparison based on 'id' if it's considered unique and available.
        if (id != null) {
            return id.equals(plant.id);
        }
        // Fallback comparison if 'id' is not the primary key or not always present.
        // Compares other significant fields for equality.
        return dateAdded == plant.dateAdded &&
                isFavourite == plant.isFavourite &&
                Objects.equals(name, plant.name) && // Objects.equals handles nulls gracefully.
                Objects.equals(scientificName, plant.scientificName) &&
                Objects.equals(imageUrl, plant.imageUrl) &&
                Objects.equals(description, plant.description);
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit
     * of hash tables such as those provided by {@link java.util.HashMap}.
     * The general contract of `hashCode` is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during an execution of a
     * Java application, the `hashCode` method must consistently return the same integer,
     * provided no information used in `equals` comparisons on the object is modified.
     * <li>If two objects are equal according to the `equals(Object)` method, then calling
     * the `hashCode` method on each of the two objects must produce the same integer result.
     * </ul>
     * This implementation uses the `id` for hashing if available, otherwise it combines
     * hash codes of other key fields.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        // Use 'id' for hash code generation if it's available and considered unique.
        if (id != null) {
            return Objects.hash(id);
        }
        // Fallback: Generate hash code based on other significant fields.
        return Objects.hash(name, scientificName, imageUrl, description, dateAdded, isFavourite);
    }


    // --- toString() ---
    // Provides a string representation of the Plant object.
    // Useful for logging, debugging, and quickly understanding the state of an object.

    /**
     * Returns a string representation of the {@code Plant} object.
     * This typically includes the class name and the values of its important fields.
     *
     * @return A string representation of this plant.
     */
    @NonNull // Indicates that this method will never return null.
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
