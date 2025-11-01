package com.example.myapplication.ui.myplants.share;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PlantCardAdapter - RecyclerView adapter for displaying plant cards in grid or list view.
 *
 * Purpose:
 * - Display plant collections in MyGardenFragment and PlantWikiFragment
 * - Support two view modes: grid (2 columns) and list (with dates)
 * - Handle Base64 encoded images from backend
 * - Manage click events for navigation to plant details
 *
 * View Types:
 * 1. Grid View (VIEW_TYPE_GRID):
 *    - 2-column layout
 *    - Large square image (160dp)
 *    - Plant name below image
 *    - No date displayed
 *
 * 2. List View (VIEW_TYPE_LIST_WITH_DATE):
 *    - Single column
 *    - Small circular image (60dp) on left
 *    - Plant name and creation date on right
 *    - Displays formatted date
 *
 * Key Features:
 * - Dynamic layout switching via ConstraintSet
 * - Base64 image decoding with Glide
 * - Robust date parsing (multiple ISO 8601 formats)
 * - Error handling for malformed data
 * - Placeholder images for missing/invalid images
 *
 * Usage:
 * - MyGardenFragment: User's plant collection with grid/list toggle
 * - PlantWikiFragment: Encyclopedia plants in grid view only
 */
public class PlantCardAdapter extends RecyclerView.Adapter<PlantCardAdapter.PlantViewHolder> {

    private static final String TAG = "PlantCardAdapter";

    /** Grid view constant - 2 columns, large images */
    public static final int VIEW_TYPE_GRID = 0;

    /** List view constant - single column with dates */
    public static final int VIEW_TYPE_LIST_WITH_DATE = 1;

    /** List of plants to display */
    private List<Plant> plantList;

    /** Click listener for plant selection */
    private final OnPlantClickListener onPlantClickListener;

    /** Current view mode (grid or list) */
    private int currentViewType = VIEW_TYPE_GRID;

    /** Application context for resource access */
    private final Context context;

    /**
     * Callback interface for handling plant card clicks.
     * Implemented by MyGardenFragment and PlantWikiFragment.
     */
    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    /**
     * Constructor with context, plant list, and click listener.
     *
     * @param context Application context
     * @param plantList Initial list of plants
     * @param onPlantClickListener Callback for card clicks
     */
    public PlantCardAdapter(Context context, ArrayList<Plant> plantList, OnPlantClickListener onPlantClickListener) {
        this.context = context.getApplicationContext();
        this.plantList = plantList;
        this.onPlantClickListener = onPlantClickListener;
    }

    /**
     * Changes view type and refreshes all items.
     * Called by MyGardenFragment when user toggles grid/list view.
     *
     * @param viewType VIEW_TYPE_GRID or VIEW_TYPE_LIST_WITH_DATE
     */
    public void setViewType(int viewType) {
        if (this.currentViewType != viewType) {
            this.currentViewType = viewType;
            notifyDataSetChanged(); // Refresh all items with new layout
        }
    }

    /**
     * Returns current view type for the item at position.
     * All items use the same view type (determined by currentViewType).
     */
    @Override
    public int getItemViewType(int position) {
        return currentViewType;
    }

    /**
     * Creates ViewHolder for plant card item.
     * Uses same layout (item_plant_card.xml) for both grid and list views.
     */
    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    /**
     * Binds plant data to ViewHolder and configures layout based on view type.
     *
     * Process:
     * 1. Set plant name
     * 2. Load Base64 image with Glide (with error handling)
     * 3. Set click listener
     * 4. Apply view-specific layout constraints
     * 5. Format and display date (list view only)
     *
     * Image Handling:
     * - Decodes Base64 string to byte array
     * - Uses Glide for efficient loading
     * - Shows placeholder on error
     * - Handles null/empty/malformed Base64
     *
     * Layout Switching:
     * - Uses ConstraintSet to dynamically adjust layout
     * - Grid: Large image on top, name below
     * - List: Small image on left, name and date on right
     */
    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        // Set plant name
        holder.textViewPlantName.setText(plant.getName());

        // Handle Base64 encoded image
        String base64Image = plant.getImageUrl();

        try {
            if (base64Image != null && !base64Image.isEmpty()) {
                // Decode Base64 to byte array
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);

                // Load with Glide
                Glide.with(holder.itemView.getContext())
                        .load(imageBytes)
                        .placeholder(R.drawable.plantbulb_foreground)
                        .error(R.drawable.plantbulb_foreground)
                        .into(holder.imageViewPlant);
            } else {
                // No image - show placeholder
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.plantbulb_foreground)
                        .into(holder.imageViewPlant);
            }
        } catch (IllegalArgumentException e) {
            // Malformed Base64 - show placeholder
            Log.e(TAG, "Failed to decode Base64 string for plant: " + plant.getName(), e);
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.plantbulb_foreground)
                    .into(holder.imageViewPlant);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> onPlantClickListener.onPlantClick(plant));

        // Apply view-specific layout constraints
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.plantCardConstraintLayout);

        // List view: Show date and configure horizontal layout
        if (currentViewType == VIEW_TYPE_LIST_WITH_DATE) {
            holder.textViewPlantDate.setVisibility(View.VISIBLE);

            // Format creation date with robust parsing
            try {
                String createdAt = plant.getCreatedAt();
                String formattedDate = "Date not available";

                if (createdAt != null && !createdAt.isEmpty()) {
                    try {
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                        try {
                            // Try ISO 8601 with milliseconds first
                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                            java.util.Date date = isoFormat.parse(createdAt);
                            formattedDate = displayFormat.format(date);
                        } catch (java.text.ParseException e1) {
                            try {
                                // Try ISO 8601 without milliseconds
                                SimpleDateFormat simpleIsoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                                simpleIsoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                                java.util.Date date = simpleIsoFormat.parse(createdAt);
                                formattedDate = displayFormat.format(date);
                            } catch (java.text.ParseException e2) {
                                // If all parsing fails, display raw string
                                formattedDate = createdAt;
                            }
                        }
                    } catch (Exception e) {
                        formattedDate = "Date not available";
                    }
                }

                holder.textViewPlantDate.setText(context.getString(R.string.plant_added_on_date_format, formattedDate));
            } catch (Exception e) {
                holder.textViewPlantDate.setText(context.getString(R.string.plant_added_on_date_format, "Date not available"));
            }

            // List view layout: Small image on left, text on right
            constraintSet.constrainWidth(R.id.imageViewPlant, dpToPx(60));
            constraintSet.constrainHeight(R.id.imageViewPlant, dpToPx(60));
            constraintSet.setDimensionRatio(R.id.imageViewPlant, null);
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(0));
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, dpToPx(0));
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dpToPx(0));
            constraintSet.clear(R.id.imageViewPlant, ConstraintSet.END);

            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.START, R.id.imageViewPlant, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.TOP, R.id.imageViewPlant, ConstraintSet.TOP, dpToPx(0));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.clear(R.id.textViewPlantName, ConstraintSet.BOTTOM);

            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.START, R.id.textViewPlantName, ConstraintSet.START);
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.TOP, R.id.textViewPlantName, ConstraintSet.BOTTOM, dpToPx(2));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.BOTTOM, R.id.imageViewPlant, ConstraintSet.BOTTOM);

        } else { // Grid view: Large image on top, name below
            holder.textViewPlantDate.setVisibility(View.GONE);

            // Grid view layout: Full-width image on top
            constraintSet.constrainWidth(R.id.imageViewPlant, 0); // Match constraints
            constraintSet.constrainHeight(R.id.imageViewPlant, dpToPx(160));
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
            constraintSet.clear(R.id.imageViewPlant, ConstraintSet.BOTTOM);

            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.TOP, R.id.imageViewPlant, ConstraintSet.BOTTOM, dpToPx(8));

            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.TOP, R.id.textViewPlantName, ConstraintSet.BOTTOM, dpToPx(2));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dpToPx(12));
        }

        // Apply all constraint changes
        constraintSet.applyTo(holder.plantCardConstraintLayout);
    }

    /** Returns total number of plants in list. */
    @Override
    public int getItemCount() {
        return plantList.size();
    }

    /**
     * Updates plant list and refreshes RecyclerView.
     * Called by MyGardenFragment and PlantWikiFragment when data changes.
     *
     * @param newPlants New list of plants to display
     */
    public void setPlants(List<Plant> newPlants) {
        this.plantList.clear();
        this.plantList.addAll(newPlants);
        notifyDataSetChanged();
    }

    /**
     * Converts density-independent pixels (dp) to pixels (px).
     * Used for consistent sizing across different screen densities.
     *
     * @param dp Value in dp
     * @return Value in px
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * ViewHolder for plant card items.
     * Holds references to views in item_plant_card.xml layout.
     */
    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout plantCardConstraintLayout;
        ImageView imageViewPlant;
        TextView textViewPlantName;
        TextView textViewPlantDate;

        PlantViewHolder(View itemView) {
            super(itemView);
            plantCardConstraintLayout = itemView.findViewById(R.id.plantCardConstraintLayout);
            imageViewPlant = itemView.findViewById(R.id.imageViewPlant);
            textViewPlantName = itemView.findViewById(R.id.textViewPlantName);
            textViewPlantDate = itemView.findViewById(R.id.textViewPlantDate);
        }
    }
}
