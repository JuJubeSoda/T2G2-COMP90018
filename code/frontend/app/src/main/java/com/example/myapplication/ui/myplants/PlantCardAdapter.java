package com.example.myapplication.adapter;

import android.content.Context;
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
import com.example.myapplication.ui.myplants.Plant; // Your Plant model

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantCardAdapter extends RecyclerView.Adapter<PlantCardAdapter.PlantViewHolder> {

    public static final int VIEW_TYPE_GRID = 0; // Represents your default card/grid look
    public static final int VIEW_TYPE_LIST_WITH_DATE = 1; // Represents the list look using the same XML

    private List<Plant> plantList;
    private final OnPlantClickListener onPlantClickListener;
    private int currentViewType = VIEW_TYPE_GRID; // Default to grid
    private final Context context;

    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    public PlantCardAdapter(Context context, ArrayList<Plant> plantList, OnPlantClickListener onPlantClickListener) {
        this.context = context.getApplicationContext();
        this.plantList = plantList;
        this.onPlantClickListener = onPlantClickListener;
    }

    public void setViewType(int viewType) {
        if (this.currentViewType != viewType) {
            this.currentViewType = viewType;
            notifyDataSetChanged(); // Rebind all visible items
        }
    }

    // getItemViewType can be useful if you had different ViewHolders,
    // but here we use one. Still, good practice to return currentViewType.
    @Override
    public int getItemViewType(int position) {
        return currentViewType;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Always inflate item_plant_card.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        holder.textViewPlantName.setText(plant.getName());

        Glide.with(holder.itemView.getContext())
                .load(plant.getImageUrl())
                .placeholder(R.drawable.plantbulb_foreground)
                .error(R.drawable.plantbulb_foreground)
                .into(holder.imageViewPlant);

        holder.itemView.setOnClickListener(v -> onPlantClickListener.onPlantClick(plant));

        // --- Adjust layout based on currentViewType ---
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.plantCardConstraintLayout); // Clone from the ConstraintLayout

        if (currentViewType == VIEW_TYPE_LIST_WITH_DATE) {
            // --- LIST MODE ---
            holder.textViewPlantDate.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(plant.getDateAdded())); // Assumes Plant has getDateAdded() -> long
            holder.textViewPlantDate.setText(context.getString(R.string.plant_added_on_date_format, formattedDate));

            // Modify ImageView for list: smaller, on the left
            constraintSet.constrainWidth(R.id.imageViewPlant, dpToPx(60));
            constraintSet.constrainHeight(R.id.imageViewPlant, dpToPx(60));
            constraintSet.setDimensionRatio(R.id.imageViewPlant, null); // Remove ratio if any
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(0)); // Align to start of card padding
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, dpToPx(0));
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dpToPx(0));
            constraintSet.clear(R.id.imageViewPlant, ConstraintSet.END); // Remove end constraint

            // Modify textViewPlantName for list: to the right of the image
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.START, R.id.imageViewPlant, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.TOP, R.id.imageViewPlant, ConstraintSet.TOP, dpToPx(0)); // Align top with image
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.clear(R.id.textViewPlantName, ConstraintSet.BOTTOM); // Allow wrapping

            // Modify textViewPlantDate for list: below plant name, to the right of image
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.START, R.id.textViewPlantName, ConstraintSet.START); // Align start with name
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.TOP, R.id.textViewPlantName, ConstraintSet.BOTTOM, dpToPx(2));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.BOTTOM, R.id.imageViewPlant, ConstraintSet.BOTTOM); // Align bottom with image


            // Adjust overall card padding or content padding for list if needed
            // holder.plantCardConstraintLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));


        } else { // VIEW_TYPE_GRID (default card appearance)
            // --- GRID MODE ---
            holder.textViewPlantDate.setVisibility(View.GONE); // Hide date in grid, or show it if you prefer

            // Reset ImageView for grid
            constraintSet.constrainWidth(R.id.imageViewPlant, 0); // 0dp for match_constraint
            constraintSet.constrainHeight(R.id.imageViewPlant, dpToPx(160)); // Your defined grid image height
            // constraintSet.setDimensionRatio(R.id.imageViewPlant, "1:1"); // If you want square images in grid
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
            constraintSet.connect(R.id.imageViewPlant, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
            constraintSet.clear(R.id.imageViewPlant, ConstraintSet.BOTTOM);

            // Reset textViewPlantName for grid
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantName, ConstraintSet.TOP, R.id.imageViewPlant, ConstraintSet.BOTTOM, dpToPx(8));

            // Reset textViewPlantDate (it's GONE, but clear constraints if it were visible)
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dpToPx(12));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.TOP, R.id.textViewPlantName, ConstraintSet.BOTTOM, dpToPx(2));
            constraintSet.connect(R.id.textViewPlantDate, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dpToPx(12));

            // Reset overall card padding or content padding if changed for list view
            // holder.plantCardConstraintLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12)); // Default padding
        }

        constraintSet.applyTo(holder.plantCardConstraintLayout); // Apply all changes
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public void setPlants(List<Plant> newPlants) {
        this.plantList.clear();
        this.plantList.addAll(newPlants);
        notifyDataSetChanged();
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    // Single ViewHolder class referencing views from your item_plant_card.xml
    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout plantCardConstraintLayout; // The root ConstraintLayout
        ImageView imageViewPlant;
        TextView textViewPlantName;
        TextView textViewPlantDate; // Your date TextView

        PlantViewHolder(View itemView) {
            super(itemView);
            plantCardConstraintLayout = itemView.findViewById(R.id.plantCardConstraintLayout); // ID from your XML
            imageViewPlant = itemView.findViewById(R.id.imageViewPlant);
            textViewPlantName = itemView.findViewById(R.id.textViewPlantName);
            textViewPlantDate = itemView.findViewById(R.id.textViewPlantDate);
        }
    }
}
