
package com.example.myapplication.ui.myplants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantCardAdapter extends RecyclerView.Adapter<PlantCardAdapter.PlantViewHolder> {

    private static final String TAG = "PlantCardAdapter";
    public static final int VIEW_TYPE_GRID = 0;
    public static final int VIEW_TYPE_LIST_WITH_DATE = 1;

    private List<Plant> plantList;
    private final OnPlantClickListener onPlantClickListener;
    private int currentViewType = VIEW_TYPE_GRID;
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
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return currentViewType;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        holder.textViewPlantName.setText(plant.getName());

        // --- MODIFICATION: Handle Base64 image string --- //
        String base64Image = plant.getImageUrl(); // In our case, getImageUrl() returns the Base64 string.

        try {
            if (base64Image != null && !base64Image.isEmpty()) {
                // Decode the Base64 string into a byte array.
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);

                // Load the byte array into the ImageView using Glide.
                Glide.with(holder.itemView.getContext())
                        .load(imageBytes)
                        .placeholder(R.drawable.plantbulb_foreground) // Displayed while loading.
                        .error(R.drawable.plantbulb_foreground)       // Displayed if loading or decoding fails.
                        .into(holder.imageViewPlant);
            } else {
                // If the Base64 string is null or empty, load the default placeholder.
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.plantbulb_foreground)
                        .into(holder.imageViewPlant);
            }
        } catch (IllegalArgumentException e) {
            // This catch block handles potential errors from Base64.decode, e.g., if the string is malformed.
            Log.e(TAG, "Failed to decode Base64 string for plant: " + plant.getName(), e);
            // Load the error drawable if decoding fails.
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.plantbulb_foreground)
                    .into(holder.imageViewPlant);
        }

        holder.itemView.setOnClickListener(v -> onPlantClickListener.onPlantClick(plant));

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.plantCardConstraintLayout);

        if (currentViewType == VIEW_TYPE_LIST_WITH_DATE) {
            holder.textViewPlantDate.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(plant.getDiscoveredOn()));
            holder.textViewPlantDate.setText(context.getString(R.string.plant_added_on_date_format, formattedDate));

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

        } else { // VIEW_TYPE_GRID
            holder.textViewPlantDate.setVisibility(View.GONE);

            constraintSet.constrainWidth(R.id.imageViewPlant, 0);
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

        constraintSet.applyTo(holder.plantCardConstraintLayout);
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

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

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
