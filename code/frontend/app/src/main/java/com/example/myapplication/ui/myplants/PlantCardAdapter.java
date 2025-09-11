package com.example.myapplication.ui.myplants; // Adjust your package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
// import com.bumptech.glide.Glide; // Example image loading library
import java.util.List;

import com.example.myapplication.R;
import com.example.yourapp.model.Plant;

public class PlantCardAdapter extends RecyclerView.Adapter<PlantCardAdapter.PlantViewHolder> {

    private List<Plant> plantList;
    private OnPlantClickListener listener;

    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    public PlantCardAdapter(List<Plant> plantList, OnPlantClickListener listener) {
        this.plantList = plantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item_plant_card.xml
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant currentPlant = plantList.get(position);
        holder.bind(currentPlant, listener);
    }

    @Override
    public int getItemCount() {
        return plantList != null ? plantList.size() : 0;
    }

    // Method to update data (e.g., when new plants are added)
    public void setPlants(List<Plant> newPlantList) {
        this.plantList = newPlantList;
        notifyDataSetChanged(); // Or use DiffUtil for better performance
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPlant;
        TextView textViewPlantName;
        TextView textViewPlantScientificName;

        PlantViewHolder(View itemView) {
            super(itemView);
            imageViewPlant = itemView.findViewById(R.id.imageViewPlant);
            textViewPlantName = itemView.findViewById(R.id.textViewPlantName);
            textViewPlantScientificName = itemView.findViewById(R.id.textViewPlantScientificName);
        }

        void bind(final Plant plant, final OnPlantClickListener listener) {
            textViewPlantName.setText(plant.getName());
            textViewPlantScientificName.setText(plant.getScientificName());

            // Load image using a library like Glide or Picasso
            // Glide.with(itemView.getContext())
            //      .load(plant.getImageUrl())
            //      .placeholder(R.drawable.default_plant_placeholder) // Optional placeholder
            //      .error(R.drawable.default_plant_error) // Optional error image
            //      .into(imageViewPlant);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlantClick(plant);
                }
            });
        }
    }
}
   