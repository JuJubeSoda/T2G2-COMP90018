// Specifies the package where this PlantCardAdapter class resides.
package com.example.myapplication.ui.myplants;

// Android framework and utility imports.
import android.content.Context; // For accessing application-specific resources and classes.
import android.util.TypedValue; // For converting complex dimension units like dp to pixels.
import android.view.LayoutInflater; // For instantiating layout XML files into View objects.
import android.view.View; // Base class for widgets, used to create interactive UI components.
import android.view.ViewGroup; // Base class for layouts, containers that hold other Views or ViewGroups.
import android.widget.ImageView; // For displaying image resources.
import android.widget.TextView; // For displaying text.

// AndroidX (Jetpack) library imports for modern Android development.
import androidx.annotation.NonNull; // Annotation indicating a parameter, field, or method return value can never be null.
import androidx.constraintlayout.widget.ConstraintLayout; // Layout that allows positioning and sizing widgets in a flexible way.
import androidx.constraintlayout.widget.ConstraintSet; // For defining and applying constraints programmatically.
import androidx.recyclerview.widget.RecyclerView; // For efficiently displaying large sets of data.

// Third-party library for image loading and caching.
import com.bumptech.glide.Glide; // Popular image loading and caching library for Android.

// Application-specific R class for accessing resources.
import com.example.myapplication.R;
// Application-specific Plant model class.
import com.example.myapplication.ui.myplants.Plant;

// Java utility classes.
import java.text.SimpleDateFormat; // For formatting and parsing dates.
import java.util.ArrayList; // Resizable-array implementation of the List interface.
import java.util.Date; // Represents a specific instant in time.
import java.util.List; // Ordered collection (also known as a sequence).
import java.util.Locale; // Represents a specific geographical, political, or cultural region.

/**
 * PlantCardAdapter is a RecyclerView.Adapter responsible for displaying a list of {@link Plant} objects.
 * It supports two view types: a default grid layout (VIEW_TYPE_GRID) and a list layout
 * that includes a date (VIEW_TYPE_LIST_WITH_DATE). The adapter dynamically adjusts the
 * constraints of the items in `onBindViewHolder` based on the current view type.
 */
public class PlantCardAdapter extends RecyclerView.Adapter<PlantCardAdapter.PlantViewHolder> {

    // Constant representing the view type for a grid layout.
    public static final int VIEW_TYPE_GRID = 0;
    // Constant representing the view type for a list layout that includes a date.
    public static final int VIEW_TYPE_LIST_WITH_DATE = 1;

    // List of Plant objects to be displayed by the adapter.
    private List<Plant> plantList;
    // Listener interface for handling clicks on individual plant items.
    private final OnPlantClickListener onPlantClickListener;
    // Tracks the current view type (grid or list) to be rendered. Defaults to grid.
    private int currentViewType = VIEW_TYPE_GRID;
    // Context used for various operations like resource access and dimension conversion.
    private final Context context;

    /**
     * Interface definition for a callback to be invoked when a plant item is clicked.
     */
    public interface OnPlantClickListener {
        /**
         * Called when a plant item has been clicked.
         *
         * @param plant The {@link Plant} object that was clicked.
         */
        void onPlantClick(Plant plant);
    }

    /**
     * Constructor for PlantCardAdapter.
     *
     * @param context            The context, typically from the calling Fragment or Activity.
     * @param plantList          The initial list of {@link Plant} objects to display.
     * @param onPlantClickListener Listener for item click events.
     */
    public PlantCardAdapter(Context context, ArrayList<Plant> plantList, OnPlantClickListener onPlantClickListener) {
        // Use application context to avoid leaks if the activity/fragment context is shorter-lived.
        this.context = context.getApplicationContext();
        this.plantList = plantList;
        this.onPlantClickListener = onPlantClickListener;
    }

    /**
     * Sets the desired view type for the RecyclerView items.
     * If the new view type is different from the current one, it notifies the adapter
     * to rebind all visible items to reflect the change.
     *
     * @param viewType The new view type, either {@link #VIEW_TYPE_GRID} or {@link #VIEW_TYPE_LIST_WITH_DATE}.
     */
    public void setViewType(int viewType) {
        if (this.currentViewType != viewType) {
            this.currentViewType = viewType;
            notifyDataSetChanged(); // Important: Rebinds all visible items to apply new layout constraints.
        }
    }

    /**
     * Returns the view type of the item at {@code position} for the purposes of view recycling.
     * In this adapter, all items share the same layout but their appearance is modified in onBindViewHolder.
     * However, returning the `currentViewType` is good practice, especially if different ViewHolder
     * types were to be used for different view types.
     *
     * @param position Position of the item.
     * @return Integer value identifying the type of the view needed to represent the item at {@code position}.
     */
    @Override
    public int getItemViewType(int position) {
        return currentViewType;
    }

    /**
     * Called when RecyclerView needs a new {@link PlantViewHolder} of the given type to represent
     * an item.
     * This adapter always inflates the same layout resource (`R.layout.item_plant_card`)
     * regardless of the `viewType` parameter, as the layout adjustments are handled programmatically
     * in `onBindViewHolder`.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new PlantViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the common item_plant_card.xml layout for all items.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link PlantViewHolder#itemView} to reflect the item at the
     * given position. It also dynamically adjusts the layout constraints of the item view based
     * on the `currentViewType` (grid or list).
     *
     * @param holder   The PlantViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        // Get the Plant object for the current position.
        Plant plant = plantList.get(position);

        // Set the plant's name to the TextView.
        holder.textViewPlantName.setText(plant.getName());

        // Load the plant's image using Glide, with placeholder and error drawables.
        Glide.with(holder.itemView.getContext())
                .load(plant.getImageUrl())
                .placeholder(R.drawable.plantbulb_foreground) // Displayed while loading.
                .error(R.drawable.plantbulb_foreground)       // Displayed if loading fails.
                .into(holder.imageViewPlant);

        // Set the click listener for the entire item view.
        holder.itemView.setOnClickListener(v -> onPlantClickListener.onPlantClick(plant));

        // --- Adjust layout programmatically based on the currentViewType ---
        ConstraintSet constraintSet = new ConstraintSet();
        // Clone the existing constraints from the root ConstraintLayout of the item view.
        constraintSet.clone(holder.plantCardConstraintLayout);

        if (currentViewType == VIEW_TYPE_LIST_WITH_DATE) {
            // --- LIST MODE Configuration ---
            // Make the date TextView visible.
            holder.textViewPlantDate.setVisibility(View.VISIBLE);
            // Format the plant's added date.
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(plant.getDiscoveredOn())); // Assumes Plant model has getDateAdded() returning long.
            // Set the formatted date string, using a string resource for localization.
            holder.textViewPlantDate.setText(context.getString(R.string.plant_added_on_date_format, formattedDate));

            // Modify constraints for the ImageView to make it smaller and position it on the left.
            constraintSet.constrainWidth(R.id.imageViewPlant, dpToPx(60)); // Set width to 60dp.
            constraintSet.constrainHeight(R.id.imageViewPlant, dpToPx(60)); // Set height to 60dp.
            constraintSet.setDimensionRatio(R.id.imageViewPlant, null); // Remove any existing dimension ratio.
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(0));
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, dpToPx(0));
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dpToPx(0));
            constraintSet.clear(R.id.imageViewPlant, ConstraintSet.END); // Remove constraint to the end of parent.

            // Modify constraints for the plant name TextView to position it to the right of the image.
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.START, R.id.imageViewPlant, ConstraintSet.END, dpToPx(12)); // 12dp margin.
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.TOP, R.id.imageViewPlant, ConstraintSet.TOP, dpToPx(0)); // Align top with image.
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.clear(R.id.textViewPlantName, ConstraintSet.BOTTOM); // Clear bottom constraint to allow text wrapping.

            // Modify constraints for the date TextView to position it below the plant name and to the right of the image.
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.START, R.id.textViewPlantName, ConstraintSet.START); // Align start with plant name.
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.TOP, R.id.textViewPlantName, ConstraintSet.BOTTOM, dpToPx(2)); // 2dp margin.
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.BOTTOM, R.id.imageViewPlant, ConstraintSet.BOTTOM); // Align bottom with image.

            // Optional: Adjust overall card padding for list view if needed.
            // Example: holder.plantCardConstraintLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        } else { // VIEW_TYPE_GRID (default card appearance)
            // --- GRID MODE Configuration ---
            // Hide the date TextView in grid mode.
            holder.textViewPlantDate.setVisibility(View.GONE);

            // Reset constraints for the ImageView to its grid appearance (larger, top-aligned).
            constraintSet.constrainWidth(R.id.imageViewPlant, 0); // 0dp for match_constraint behavior.
            constraintSet.constrainHeight(R.id.imageViewPlant, dpToPx(160)); // Set to predefined grid image height.
            // constraintSet.setDimensionRatio(R.id.imageViewPlant, "1:1"); // Uncomment if square images are desired in grid mode.
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
            constraintSet.clear(R.id.imageViewPlant, ConstraintSet.BOTTOM); // Clear bottom constraint.

            // Reset constraints for the plant name TextView to be positioned below the image.
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.TOP, R.id.imageViewPlant, ConstraintSet.BOTTOM, dpToPx(8)); // 8dp margin.

            // Reset constraints for the date TextView (even though it's GONE, good practice to clear/reset).
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.TOP, R.id.textViewPlantName, ConstraintSet.BOTTOM, dpToPx(2));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dpToPx(12));

            // Optional: Reset overall card padding if it was changed for the list view.
            // Example: holder.plantCardConstraintLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12)); // Default padding
        }

        // Apply all the defined or modified constraints to the item's ConstraintLayout.
        constraintSet.applyTo(holder.plantCardConstraintLayout);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return plantList.size();
    }

    /**
     * Updates the list of plants displayed by the adapter.
     * Clears the existing list and adds all items from the new list, then notifies
     * the adapter that the data set has changed.
     *
     * @param newPlants The new list of {@link Plant} objects to display.
     */
    public void setPlants(List<Plant> newPlants) {
        this.plantList.clear();
        this.plantList.addAll(newPlants);
        notifyDataSetChanged(); // Notifies observers that the underlying data has changed and any View reflecting the data set should refresh itself.
    }

    /**
     * Helper method to convert density-independent pixels (dp) to their equivalent value in pixels (px).
     *
     * @param dp The value in dp to convert.
     * @return The equivalent value in pixels.
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, // The unit type of the value.
                dp,                           // The dp value to convert.
                context.getResources().getDisplayMetrics() // Current display metrics for conversion.
        );
    }

    /**
     * ViewHolder class for plant items.
     * Holds references to the views within each item layout (item_plant_card.xml)
     * to avoid frequent `findViewById` calls.
     */
    static class PlantViewHolder extends RecyclerView.ViewHolder {
        // The root ConstraintLayout of the item card.
        ConstraintLayout plantCardConstraintLayout;
        // ImageView for displaying the plant's image.
        ImageView imageViewPlant;
        // TextView for displaying the plant's name.
        TextView textViewPlantName;
        // TextView for displaying the plant's added date (used in list view).
        TextView textViewPlantDate;

        /**
         * Constructor for the PlantViewHolder.
         *
         * @param itemView The View for a single item in the RecyclerView.
         */
        PlantViewHolder(View itemView) {
            super(itemView);
            // Initialize views by finding them by their ID from the inflated item layout.
            // These IDs must match those defined in R.layout.item_plant_card.
            plantCardConstraintLayout = itemView.findViewById(R.id.plantCardConstraintLayout);
            imageViewPlant = itemView.findViewById(R.id.imageViewPlant);
            textViewPlantName = itemView.findViewById(R.id.textViewPlantName);
            textViewPlantDate = itemView.findViewById(R.id.textViewPlantDate);
        }
    }
}
