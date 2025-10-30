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
import com.example.myapplication.myPlantsData.MyGardenDataManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * PlantDetailFragment - Displays detailed information for user-collected plants.
 * 
 * Purpose:
 * - Show comprehensive details for plants in My Garden
 * - Display plant image, name, scientific name
 * - Show location, discovery date, discovered by
 * - Display tags and description
 * - Handle date parsing from backend
 * 
 * User Flow:
 * 1. User clicks plant tile in MyGardenFragment
 * 2. Fragment displays full plant details
 * 3. User can navigate back via back button
 * 
 * Key Features:
 * - Base64 image decoding and display
 * - Robust date parsing (multiple ISO 8601 formats)
 * - Location coordinate formatting
 * - Tag list display
 * - Progress indicator during loading
 * - Error handling with fallback text
 * 
 * Data Source:
 * - Plant object passed from MyGardenFragment
 * - Data from user uploads via UploadFragment
 * - Stored in backend database
 * 
 * Navigation:
 * - From: MyGardenFragment (plant tile click)
 * - Back: Navigation up arrow
 */
public class PlantDetailFragment extends Fragment {

    public static final String ARG_PLANT = "plant_argument";
    private static final String TAG = "PlantDetailFragment";

    /** View binding for plantdetail.xml layout */
    private PlantdetailBinding binding;
    
    /** Plant data to display */
    private Plant plant;
    private MyGardenDataManager myGardenDataManager;
    private Boolean currentLiked;

    /**
     * Fragment creation lifecycle method.
     * Retrieves Plant object and validates it.
     * Navigates back if Plant is null (error state).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }

        // Handle missing Plant data
        if (plant == null) {
            Log.e(TAG, "CRITICAL: Plant object is null. Cannot display details.");
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantdetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI after view is created.
     * 
     * Process:
     * 1. Shows progress bar
     * 2. Populates UI if Plant exists
     * 3. Hides progress bar
     * 4. Sets up back button navigation
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show loading indicator
        binding.progressBar.setVisibility(View.VISIBLE);

        myGardenDataManager = new MyGardenDataManager(requireContext());
        currentLiked = plant != null ? plant.isFavourite() : null;

        if (plant != null) {
            populateUi();
            binding.progressBar.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
        }

        // Setup back navigation
        binding.backButtonDetail.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Setup like toggle button and show-on-map
        if (binding.btnLikeToggle != null) {
            updateLikeButtonUi();
            binding.btnLikeToggle.setOnClickListener(v -> toggleLike());
        }
        if (binding.btnShowOnMap != null) {
            binding.btnShowOnMap.setOnClickListener(v -> navigateToMap());
        }
    }

    /**
     * Populates all UI fields with plant data.
     * 
     * Displays:
     * - Scientific name
     * - Description/introduction
     * - Location coordinates (latitude, longitude)
     * - Tags (comma-separated list)
     * - Discovered by (username)
     * - Discovery date (formatted from ISO 8601)
     * - Plant image (Base64 decoded)
     * 
     * Error Handling:
     * - Null checks for all fields
     * - Fallback text for missing data
     * - Multiple date format parsing attempts
     * - Image loading with placeholder on error
     * - Try-catch blocks for robust error handling
     */
    private void populateUi() {
        // Populate text fields with null safety
        try {
            binding.textViewScientificName.setText(plant.getScientificName() != null ? plant.getScientificName() : "Not available");
            binding.textViewIntroduction.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");

            // Format location coordinates
            String locationString = "Location not available";
            if (plant.getLatitude() != null && plant.getLongitude() != null) {
                locationString = String.format(Locale.getDefault(), "(%.4f, %.4f)", plant.getLatitude(), plant.getLongitude());
            }
            binding.textViewLocation.setText(locationString);

            // Format tags as comma-separated list
            String tags = "No tags";
            List<String> plantTags = plant.getTags();
            if (plantTags != null && !plantTags.isEmpty()) {
                tags = plantTags.stream().collect(Collectors.joining(", "));
            }

            // Display discovered by username
            String discoveredBy = plant.getDiscoveredBy() != null ? plant.getDiscoveredBy() : "Unknown";
            binding.textViewDiscoveredBy.setText(discoveredBy);
        } catch (Exception e) {
            Log.e(TAG, "Error populating UI with plant data", e);
            Toast.makeText(getContext(), "Error displaying plant information", Toast.LENGTH_SHORT).show();
        }

        // Parse and format discovery date
        try {
            String createdAt = plant.getCreatedAt();
            
            if (createdAt == null || createdAt.isEmpty()) {
                binding.textViewDiscoveredOn.setText("Date not available");
            } else {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                String formattedDate;
                
                try {
                    // Try ISO 8601 with milliseconds
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = isoFormat.parse(createdAt);
                    formattedDate = displayFormat.format(date);
                } catch (ParseException e1) {
                    try {
                        // Try ISO 8601 without milliseconds
                        SimpleDateFormat simpleIsoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        simpleIsoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = simpleIsoFormat.parse(createdAt);
                        formattedDate = displayFormat.format(date);
                    } catch (ParseException e2) {
                        // Fallback to raw string
                        formattedDate = createdAt;
                    }
                }
                
                binding.textViewDiscoveredOn.setText(formattedDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle discovery date.", e);
            binding.textViewDiscoveredOn.setText("Date not available");
        }

        // Load plant image with Base64 decoding
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

    private void updateLikeButtonUi() {
        if (currentLiked != null && currentLiked) {
            binding.btnLikeToggle.setImageResource(R.drawable.ic_favorite_24);
        } else {
            binding.btnLikeToggle.setImageResource(R.drawable.ic_favorite_border_24);
        }
    }

    private void toggleLike() {
        if (plant == null) return;
        boolean liked = currentLiked != null && currentLiked;
        if (liked) {
            // unlike
            myGardenDataManager.unlikePlant(plant.getPlantId(), new MyGardenDataManager.DataCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    currentLiked = false;
                    updateLikeButtonUi();
                    Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            myGardenDataManager.likePlant(plant.getPlantId(), new MyGardenDataManager.DataCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    currentLiked = true;
                    updateLikeButtonUi();
                    Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToMap() {
        if (plant == null) return;
        try {
            Bundle args = new Bundle();
            args.putInt("plantId", plant.getPlantId());
            Navigation.findNavController(requireView()).navigate(R.id.navigation_plant_map, args);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open map", Toast.LENGTH_SHORT).show();
        }
    }

    /** Cleans up view binding to prevent memory leaks. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
