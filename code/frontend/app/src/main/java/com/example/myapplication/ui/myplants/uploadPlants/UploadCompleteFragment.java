package com.example.myapplication.ui.myplants.uploadPlants;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.R;
import com.example.myapplication.databinding.UploadcompletepreviewBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * UploadCompleteFragment - Success screen after plant upload completion.
 * 
 * Purpose:
 * - Display preview of uploaded plant information
 * - Confirm successful upload to user
 * - Provide options for next actions
 * - Show all entered data for verification
 * 
 * User Flow:
 * 1. User completes plant upload in UploadFragment
 * 2. Fragment receives plant data via arguments
 * 3. Displays image, name, location, introduction, date
 * 4. User can "Upload More" (return to camera) or "Gather More Info" (go to wiki)
 * 
 * Key Features:
 * - Image preview from captured photo
 * - Display all entered plant details
 * - Current date stamp
 * - "Discovered by: You" attribution
 * - Navigation to next actions
 * 
 * Arguments (from UploadFragment):
 * - ARG_IMAGE_URI: URI string of captured image
 * - ARG_SCIENTIFIC_NAME: Plant name entered by user
 * - ARG_LOCATION: Formatted location string (optional)
 * - ARG_INTRODUCTION: User's description text (optional)
 * - ARG_SEARCH_TAG: Tags for categorization (optional, currently unused)
 * 
 * Navigation:
 * - From: UploadFragment (after successful upload)
 * - To: CaptureFragment (upload more) or PlantWikiFragment (gather info)
 */
public class UploadCompleteFragment extends Fragment {

    private static final String TAG = "UploadCompleteFragment";

    /** Argument keys for fragment data passing */
    public static final String ARG_IMAGE_URI = "imageUri";
    public static final String ARG_SCIENTIFIC_NAME = "scientificName";
    public static final String ARG_LOCATION = "location";
    public static final String ARG_INTRODUCTION = "introduction";
    public static final String ARG_SEARCH_TAG = "searchTag";

    /** View binding for uploadcompletepreview.xml layout */
    private UploadcompletepreviewBinding binding;
    
    /** Navigation controller for fragment transitions */
    private NavController navController;

    /** Uploaded plant data received from arguments */
    private String imageUriString;
    private String scientificName;
    private String location;
    private String introduction;
    private String searchTag;

    /**
     * Fragment creation lifecycle method.
     * Retrieves and stores plant data from arguments passed by UploadFragment.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        
        Bundle arguments = getArguments();
        if (arguments != null) {
            // Retrieve all plant data from arguments
            imageUriString = arguments.getString(ARG_IMAGE_URI);
            scientificName = arguments.getString(ARG_SCIENTIFIC_NAME);
            location = arguments.getString(ARG_LOCATION);
            introduction = arguments.getString(ARG_INTRODUCTION);
            searchTag = arguments.getString(ARG_SEARCH_TAG);

            // Log received data for debugging
            Log.d(TAG, "Received Image URI: " + imageUriString);
            Log.d(TAG, "Received Scientific Name: " + scientificName);
            Log.d(TAG, "Received Location: " + location);
            Log.d(TAG, "Received Introduction: " + introduction);
            Log.d(TAG, "Received Search Tag: " + searchTag);
        } else {
            // Error handling for missing arguments
            Log.e(TAG, "No arguments bundle received. Data will be missing.");
            Toast.makeText(getContext(), "Error: Could not load plant details.", Toast.LENGTH_LONG).show();
        }
    }

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        binding = UploadcompletepreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI components after view is created.
     * 
     * Initialization Order:
     * 1. Find NavController (with fallback)
     * 2. Populate views with plant data
     * 3. Setup button click listeners
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        
        // Find NavController with fallback strategy
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to find NavController via NavHostFragment", e);
            if (getView() != null) {
                navController = Navigation.findNavController(getView());
            }
        }

        populateViews();
        setupButtonListeners();
    }

    /**
     * Populates UI elements with uploaded plant data.
     * 
     * Displays:
     * - Plant image (from URI or placeholder)
     * - Scientific name
     * - Location (formatted string)
     * - Introduction/description
     * - Current date stamp
     * - "Discovered by: You" attribution
     * 
     * Fallbacks:
     * - Missing image: Shows placeholder drawable
     * - Missing text fields: Shows "N/A"
     */
    private void populateViews() {
        if (binding == null) {
            Log.e(TAG, "populateViews: Binding is null. Cannot set data.");
            return;
        }

        Log.d(TAG, "Populating views with Image URI: " + imageUriString);
        Log.d(TAG, "Populating views with Scientific Name: " + scientificName);

        // Set plant image with error handling
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                binding.imageViewPlantPreview.setImageURI(Uri.parse(imageUriString));
            } catch (Exception e) {
                Log.e(TAG, "Error setting image URI: " + imageUriString, e);
                binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
            }
        } else {
            Log.w(TAG, "Image URI is null or empty, setting placeholder.");
            binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
        }

        // Set text fields with "N/A" fallbacks
        binding.textViewScientificName.setText(scientificName != null && !scientificName.isEmpty() ? scientificName : "N/A");
        binding.textViewLocation.setText(location != null && !location.isEmpty() ? location : "N/A");
        binding.textViewIntroduction.setText(introduction != null && !introduction.isEmpty() ? introduction : "N/A");

        // Set static fields
        binding.textViewDiscoveredBy.setText("Discovered by: You");
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        binding.textViewDate.setText("Date: " + currentDate);
    }

    /**
     * Sets up button click listeners for next actions.
     * 
     * Buttons:
     * - Upload More: Navigate to CaptureFragment to upload another plant
     * - Gather More Info: Navigate to PlantWikiFragment to learn about plants
     * 
     * Error Handling:
     * - Checks for null binding and NavController
     * - Catches navigation exceptions
     * - Shows user-friendly error messages
     */
    private void setupButtonListeners() {
        if (binding == null || navController == null) {
            Log.e(TAG, "setupButtonListeners: Binding or NavController is null.");
            return;
        }

        // Upload More button - restart upload flow
        binding.buttonUploadMore.setOnClickListener(v -> {
            Log.d(TAG, "Upload More button clicked");
            try {
                navController.navigate(R.id.navigation_upload);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to CaptureFragment for 'Upload More'", e);
                Toast.makeText(getContext(), "Error navigating to camera.", Toast.LENGTH_SHORT).show();
            }
        });

        // Gather More Info button - navigate to plant wiki
        binding.buttonGatherMoreInfo.setOnClickListener(v -> {
            Log.d(TAG, "Gather More Information button clicked");
            navController.navigate(R.id.navigation_plant_wiki);
        });
    }

    /**
     * Cleans up view binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called, binding set to null.");
        binding = null;
    }
}
