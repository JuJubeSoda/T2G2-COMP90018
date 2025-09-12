// Inside UploadCompleteFragment.java (this is your file from the context)

package com.example.myapplication.ui.myplants;

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
// This should be UploadcompletepreviewBinding if your XML is uploadcompletepreview.xml
import com.example.myapplication.databinding.UploadcompletepreviewBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadCompleteFragment extends Fragment {

    private static final String TAG = "UploadCompleteFragment"; // Matches class name

    // Argument keys - these are the "contracts" for data passing
    public static final String ARG_IMAGE_URI = "imageUri";
    public static final String ARG_SCIENTIFIC_NAME = "scientificName";
    public static final String ARG_LOCATION = "location";
    public static final String ARG_INTRODUCTION = "introduction";
    public static final String ARG_SEARCH_TAG = "searchTag";

    private UploadcompletepreviewBinding binding; // Ensure this matches your XML file name
    private NavController navController;

    private String imageUriString;
    private String scientificName;
    private String location;
    private String introduction;
    private String searchTag;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        Bundle arguments = getArguments();
        if (arguments != null) {
            imageUriString = arguments.getString(ARG_IMAGE_URI); // Use the defined constant
            scientificName = arguments.getString(ARG_SCIENTIFIC_NAME);
            location = arguments.getString(ARG_LOCATION);
            introduction = arguments.getString(ARG_INTRODUCTION);
            searchTag = arguments.getString(ARG_SEARCH_TAG);

            // *** Log what's being RECEIVED ***
            Log.d(TAG, "Received Image URI: " + imageUriString);
            Log.d(TAG, "Received Scientific Name: " + scientificName);
            Log.d(TAG, "Received Location: " + location);
            Log.d(TAG, "Received Introduction: " + introduction);
            Log.d(TAG, "Received Search Tag: " + searchTag);
        } else {
            Log.e(TAG, "No arguments bundle received for " + TAG + ". Data will be missing.");
            Toast.makeText(getContext(), "Error: Could not load plant details.", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        // Make sure UploadcompletepreviewBinding matches your XML file name (e.g., uploadcompletepreview.xml)
        binding = UploadcompletepreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to find NavController via NavHostFragment", e);
            if (getView() != null) { // Fallback
                navController = Navigation.findNavController(getView());
            }
        }

        populateViews(); // Call to set data to views
        setupButtonListeners(); // Your existing method
    }

    private void populateViews() {
        if (binding == null) {
            Log.e(TAG, "populateViews: Binding is null. Cannot set data.");
            return;
        }

        // *** Log data just BEFORE setting it to views ***
        Log.d(TAG, "Populating views with Image URI: " + imageUriString);
        Log.d(TAG, "Populating views with Scientific Name: " + scientificName);
        // Add logs for other fields if needed

        // Set the image
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                // Ensure imageViewPlantPreview is the correct ID from uploadcompletepreview.xml
                binding.imageViewPlantPreview.setImageURI(Uri.parse(imageUriString));
            } catch (Exception e) {
                Log.e(TAG, "Error setting image URI in populateViews: " + imageUriString, e);
                // Ensure you have a placeholder drawable
                binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
            }
        } else {
            Log.w(TAG, "Image URI is null or empty in populateViews, setting placeholder.");
            binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
        }

        // Set text fields
        // Ensure these IDs from binding match your uploadcompletepreview.xml
        binding.textViewScientificName.setText(scientificName != null && !scientificName.isEmpty() ? scientificName : "N/A");
        binding.textViewLocation.setText(location != null && !location.isEmpty() ? location : "N/A");
        binding.textViewIntroduction.setText(introduction != null && !introduction.isEmpty() ? introduction : "N/A");
        // Your XML uses "textViewSearchTag" for the value and "labelSearchTag" for the label.
        // The value here is "Tags: N/A" or "Tags: actualTag"
        binding.textViewSearchTag.setText(searchTag != null && !searchTag.isEmpty() ? "Tags: " + searchTag : "Tags: N/A");

        binding.textViewDiscoveredBy.setText("Discovered by: You"); // Assuming static for now
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        binding.textViewDate.setText("Date: " + currentDate);
    }

    // Your setupButtonListeners() and onDestroyView() methods remain the same
    private void setupButtonListeners() {
        if (binding == null || navController == null) {
            Log.e(TAG, "setupButtonListeners: Binding or NavController is null.");
            return;
        }

        binding.buttonUploadMore.setOnClickListener(v -> {
            Log.d(TAG, "Upload More button clicked");
            try {
                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() != navController.getGraph().getStartDestinationId()) {
                    navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                } else if (navController.getGraph().getStartDestinationId() != 0) {
                    navController.navigate(navController.getGraph().getStartDestinationId());
                } else {
                    Toast.makeText(getContext(), "Cannot determine start destination.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to start destination for 'Upload More'", e);
                Toast.makeText(getContext(), "Error navigating.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonGatherMoreInfo.setOnClickListener(v -> {
            Log.d(TAG, "Gather More Information button clicked");
            Toast.makeText(getContext(), "Functionality to gather more info coming soon!", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called, binding set to null.");
        binding = null;
    }
}
