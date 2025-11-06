package com.example.myapplication.ui.home; // Or your package

/**
 * DiscoveryItem - Data model for nearby plant discoveries in HomeFragment carousel.
 * 
 * Purpose:
 * - Represents a plant discovery card shown on home screen
 * - Displays plant name, distance, image, and description
 * - Supports both Base64 images (from API) and resource IDs (placeholders)
 * 
 * Fields:
 * - plantId: Database ID for navigation to PlantDetailFragment
 * - name: Plant common name
 * - distance: Formatted distance string (e.g., "1.2 km away")
 * - imageResId: Drawable resource ID for placeholder/fallback
 * - description: Short plant description or location
 * - base64Image: Base64 encoded image data from backend API
 * 
 * Usage:
 * - HomeFragment creates DiscoveryItems from PlantDto responses
 * - DiscoveryAdapter displays in horizontal RecyclerView carousel
 * - User clicks to navigate to full plant details
 */
public class DiscoveryItem {
    /** Database plant ID for navigation */
    private Long plantId; // optional: used for navigation to details
    
    /** Plant common name */
    private String name;
    
    /** Distance from user (e.g., "1.2 km away") */
    private String distance;
    
    /** Placeholder/fallback drawable resource ID */
    private int imageResId;
    
    /** Short plant description */
    private String description;
    
    /** Base64 encoded image string from backend API */
    private String base64Image; // Base64 encoded image from API

    /**
     * Constructor with drawable resource ID (for placeholder).
     * 
     * @param plantId Database plant ID
     * @param name Plant name
     * @param distance Distance string
     * @param imageResId Drawable resource ID
     * @param description Plant description
     */
    public DiscoveryItem(Long plantId, String name, String distance, int imageResId, String description) {
        this.plantId = plantId;
        this.name = name;
        this.distance = distance;
        this.imageResId = imageResId;
        this.description = description;
        this.base64Image = null;
    }

    /**
     * Constructor with Base64 image string (from API).
     * 
     * @param plantId Database plant ID
     * @param name Plant name
     * @param distance Distance string
     * @param imageResId Fallback drawable resource ID
     * @param description Plant description
     * @param base64Image Base64 encoded image data
     */
    public DiscoveryItem(Long plantId, String name, String distance, int imageResId, String description, String base64Image) {
        this.plantId = plantId;
        this.name = name;
        this.distance = distance;
        this.imageResId = imageResId;
        this.description = description;
        this.base64Image = base64Image;
    }
    
    // Getters
    public Long getPlantId() { return plantId; }

    public String getName() { return name; }
    public String getDistance() { return distance; }
    public int getImageResId() { return imageResId; }
    public String getDescription() { return description; }
    public String getBase64Image() { return base64Image; }
    
    /** Check if Base64 image data is available */
    public boolean hasBase64Image() { 
        return base64Image != null && !base64Image.isEmpty(); 
    }
}