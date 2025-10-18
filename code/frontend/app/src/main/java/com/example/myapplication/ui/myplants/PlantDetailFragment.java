package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantdetailBinding;

// FIX: Add required imports for date parsing and streams
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * PlantDetailFragment displays the details of a single plant collected by the user.
 * It is used by the MyGardenFragment and uses the simple plantdetail.xml layout.
 */
public class PlantDetailFragment extends Fragment {

    public static final String ARG_PLANT = "plant_argument";
    private static final String TAG = "PlantDetailFragment";

    private PlantdetailBinding binding;
    private Plant plant;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }

        if (plant == null) {
            Log.e(TAG, "CRITICAL: Plant object is null. Cannot display details.");
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantdetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show progress bar while loading
        binding.progressBar.setVisibility(View.VISIBLE);

        if (plant != null) {
            populateUi();
            // Hide progress bar after populating UI
            binding.progressBar.setVisibility(View.GONE);
        } else {
            // Hide progress bar and show error
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
        }

        binding.backButtonDetail.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    /**
     * Populates the views in the layout with data from the 'plant' object.
     */
    private void populateUi() {
        try {
            // --- Use correct and safe method calls based on the updated Plant model ---
            binding.textViewScientificName.setText(plant.getScientificName() != null ? plant.getScientificName() : "Not available");
            binding.textViewIntroduction.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");

            // Construct a location string from latitude and longitude, with null checks.
            String locationString = "Location not available";
            if (plant.getLatitude() != null && plant.getLongitude() != null) {
                locationString = String.format(Locale.getDefault(), "(%.4f, %.4f)", plant.getLatitude(), plant.getLongitude());
            }
            binding.textViewLocation.setText(locationString);

            // Correctly handle the list of tags
            String tags = "No tags";
            List<String> plantTags = plant.getTags();
            if (plantTags != null && !plantTags.isEmpty()) {
                tags = plantTags.stream().collect(Collectors.joining(", "));
            }
            binding.textViewSearchTag.setText(tags);

            // Set the discovered by user, with a null check.
            String discoveredBy = plant.getDiscoveredBy() != null ? plant.getDiscoveredBy() : "Unknown";
            binding.textViewDiscoveredBy.setText(discoveredBy);
        } catch (Exception e) {
            Log.e(TAG, "Error populating UI with plant data", e);
            Toast.makeText(getContext(), "Error displaying plant information", Toast.LENGTH_SHORT).show();
        }

        // --- FIX: Handle the date string from the backend ---
        try {
            String createdAt = plant.getCreatedAt();
            
            if (createdAt == null || createdAt.isEmpty()) {
                binding.textViewDiscoveredOn.setText("Date not available");
            } else {
                // Try to parse the date string - handle different possible formats
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                String formattedDate;
                
                try {
                    // Try ISO 8601 format first
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = isoFormat.parse(createdAt);
                    formattedDate = displayFormat.format(date);
                } catch (ParseException e1) {
                    try {
                        // Try simpler ISO format
                        SimpleDateFormat simpleIsoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        simpleIsoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = simpleIsoFormat.parse(createdAt);
                        formattedDate = displayFormat.format(date);
                    } catch (ParseException e2) {
                        // If all parsing fails, just display the raw string
                        formattedDate = createdAt;
                    }
                }
                
                binding.textViewDiscoveredOn.setText(formattedDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle discovery date.", e);
            binding.textViewDiscoveredOn.setText("Date not available");
        }


        // Load the plant image using Glide with added null safety.
        try {
            String base64Image = plant.getImageUrl();
            if (base64Image == null || base64Image.isEmpty()) {
                binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
            } else {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Glide.with(this)
                        .load(imageBytes)
                        .placeholder(R.drawable.plantbulb_foreground)
                        .error(R.drawable.plantbulb_foreground)
                        .into(binding.imageViewPlantPreview);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode or load image for plant: " + (plant.getName() != null ? plant.getName() : "Unknown"), e);
            binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
