package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.myapplication.R; // Make sure this is imported for drawable resources
import com.example.myapplication.databinding.PlantdetailBinding; // The binding class for plantdetail.xml

import java.text.SimpleDateFormat; // For date formatting
import java.util.Date;            // For date object
import java.util.Locale;          // For locale

public class PlantDetailFragment extends Fragment {

    private static final String TAG = "PlantDetailFragment";
    public static final String ARG_PLANT = "plant_object";

    // This class is generated from your plantdetail.xml file
    private PlantdetailBinding binding;
    private Plant currentPlant; // To hold the current plant object

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantdetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the back button listener
        binding.backButtonDetail.setOnClickListener(v -> {
            // Navigate back to the previous screen
            Navigation.findNavController(v).popBackStack();
        });

        // Retrieve the Plant object passed from MyGardenFragment
        if (getArguments() != null) {
            currentPlant = getArguments().getParcelable(ARG_PLANT);
            if (currentPlant != null) {
                // Populate all the UI elements with the plant's data
                populateUI(currentPlant);
            } else {
                Log.e(TAG, "Plant object received from arguments is null.");
            }
        }
    }

    /**
     * Populates all the views in the layout with data from the Plant object.
     * This now uses the exact IDs from your provided plantdetail.xml.
     *
     * @param plant The Plant object containing the details to display.
     */
    private void populateUI(Plant plant) {
        Log.d(TAG, "Populating UI for plant: " + plant.getName());

        // --- Set text for all fields using the correct IDs ---
        // Note: The XML uses textViewPageTitle for the main name. We'll set it here.
        binding.textViewPageTitle.setText(plant.getName());
        binding.textViewScientificName.setText(plant.getScientificName());
        binding.textViewIntroduction.setText(plant.getIntroduction());
        binding.textViewLocation.setText(plant.getLocation());
        binding.textViewSearchTag.setText(plant.getSearchTag());
        binding.textViewDiscoveredBy.setText(plant.getDiscoveredBy());

        // Format the timestamp (long) into a readable date string (e.g., "31 Dec 2024")
        if (plant.getDiscoveredOn() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(plant.getDiscoveredOn()));
            binding.textViewDiscoveredOn.setText(formattedDate);
        } else {
            binding.textViewDiscoveredOn.setText("N/A");
        }

        // TODO: The favorite icon is missing from the provided XML.
        // If you add an ImageButton with id 'favouriteButton', the following code will work.
        /*
        updateFavouriteIcon(plant.isFavourite());
        binding.favouriteButton.setOnClickListener(v -> {
            plant.setFavourite(!plant.isFavourite());
            updateFavouriteIcon(plant.isFavourite());
            // In a real app, save this change to your database here.
            Log.d(TAG, "Favourite status changed to: " + plant.isFavourite());
        });
        */

        // Here you would use a library like Glide or Picasso to load the image
        // The ID for the image is imageViewPlantPreview
        // For example:
        // Glide.with(this).load(plant.getImageUrl()).into(binding.imageViewPlantPreview);
        Log.d(TAG, "Image URL to load into imageViewPlantPreview: " + plant.getImageUrl());
    }

    /**
     * Helper method to update the favorite icon.
     * This will work once you add an ImageButton for the favorite status to your XML.
     *
     * @param isFavourite True if the plant is a favourite, false otherwise.
     */
    private void updateFavouriteIcon(boolean isFavourite) {
        // Example: Assumes you have an ImageButton with id 'favouriteButton'
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
        binding = null; // Clean up binding to prevent memory leaks
    }
}
