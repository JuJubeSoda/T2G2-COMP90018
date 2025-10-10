
package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantdetailBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlantDetailFragment extends Fragment {

    private static final String TAG = "PlantDetailFragment";
    public static final String ARG_PLANT = "plant_object";

    private PlantdetailBinding binding;
    private Plant currentPlant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantdetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButtonDetail.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        if (getArguments() != null) {
            currentPlant = getArguments().getParcelable(ARG_PLANT);
            if (currentPlant != null) {
                populateUI(currentPlant);
            } else {
                Log.e(TAG, "Plant object received from arguments is null.");
            }
        }
    }

    private void populateUI(Plant plant) {
        Log.d(TAG, "Populating UI for plant: " + plant.getName());

        binding.textViewPageTitle.setText(plant.getName());
        binding.textViewScientificName.setText(plant.getScientificName());
        binding.textViewIntroduction.setText(plant.getIntroduction());
        binding.textViewLocation.setText(plant.getLocation());
        binding.textViewSearchTag.setText(plant.getSearchTag());
        binding.textViewDiscoveredBy.setText(plant.getDiscoveredBy());

        if (plant.getDiscoveredOn() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(plant.getDiscoveredOn()));
            binding.textViewDiscoveredOn.setText(formattedDate);
        } else {
            binding.textViewDiscoveredOn.setText("N/A");
        }

        // --- MODIFICATION: Handle Base64 image string for the detail view --- //
        String base64Image = plant.getImageUrl();
        try {
            if (base64Image != null && !base64Image.isEmpty()) {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Glide.with(this)
                        .load(imageBytes)
                        .placeholder(R.drawable.plantbulb_foreground)
                        .error(R.drawable.plantbulb_foreground)
                        .into(binding.imageViewPlantPreview);
            } else {
                Glide.with(this)
                        .load(R.drawable.plantbulb_foreground)
                        .into(binding.imageViewPlantPreview);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to decode Base64 string for plant detail view: " + plant.getName(), e);
            Glide.with(this)
                    .load(R.drawable.plantbulb_foreground)
                    .into(binding.imageViewPlantPreview);
        }

        // TODO: Favourite button logic remains unchanged.
        /*
        updateFavouriteIcon(plant.isFavourite());
        binding.favouriteButton.setOnClickListener(v -> {
            plant.setFavourite(!plant.isFavourite());
            updateFavouriteIcon(plant.isFavourite());
            Log.d(TAG, "Favourite status changed to: " + plant.isFavourite());
        });
        */
    }

    private void updateFavouriteIcon(boolean isFavourite) {
        /*
        if (isFavourite) {
            binding.favouriteButton.setImageResource(R.drawable.ic_favourite_selected);
        } else {
            binding.favouriteButton.setImageResource(R.drawable.ic_favourite_unselected);
        }
        */
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
