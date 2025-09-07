package com.example.myapplication.ui.myplants; // Make sure this is your correct package

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

// CLASS NAME MATCHES YOUR FILE NAME
public class UploadCompleteFragment extends Fragment {

    // Logcat TAG uses the correct class name
    private static final String TAG = "UploadCompleteFragment";

    // Argument keys (must match those defined in mobile_navigation.xml for this fragment)
    public static final String ARG_IMAGE_URI = "imageUri";
    public static final String ARG_SCIENTIFIC_NAME = "scientificName";
    public static final String ARG_LOCATION = "location";
    public static final String ARG_INTRODUCTION = "introduction";
    public static final String ARG_SEARCH_TAG = "searchTag";

    // Binding class name matches the layout file it's supposed to inflate
    private UploadcompletepreviewBinding binding;
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
        if (getArguments() != null) {
            imageUriString = getArguments().getString(ARG_IMAGE_URI);
            scientificName = getArguments().getString(ARG_SCIENTIFIC_NAME);
            location = getArguments().getString(ARG_LOCATION);
            introduction = getArguments().getString(ARG_INTRODUCTION);
            searchTag = getArguments().getString(ARG_SEARCH_TAG);

            Log.d(TAG, "Received image URI: " + imageUriString);
            Log.d(TAG, "Received Scientific Name: " + scientificName);
            // ... log other received arguments
        } else {
            Log.e(TAG, "No arguments bundle received for " + TAG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        // Ensure this binding class name matches your XML file name
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
            Log.e(TAG, "Failed to find NavController", e);
            if (getView() != null) {
                navController = Navigation.findNavController(getView());
            }
        }

        populateViews();
        setupButtonListeners();
    }

    private void populateViews() {
        if (binding == null) {
            Log.e(TAG, "populateViews: Binding is null.");
            return;
        }

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

        binding.textViewScientificName.setText(scientificName != null && !scientificName.isEmpty() ? scientificName : "N/A");
        binding.textViewLocation.setText(location != null && !location.isEmpty() ? location : "N/A");
        binding.textViewIntroduction.setText(introduction != null && !introduction.isEmpty() ? introduction : "N/A");
        binding.textViewSearchTag.setText(searchTag != null && !searchTag.isEmpty() ? "Tags: " + searchTag : "Tags: N/A");
        binding.textViewDiscoveredBy.setText("Discovered by: You");
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        binding.textViewDate.setText("Date: " + currentDate);
    }

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
                }  else if (navController.getGraph().getStartDestinationId() != 0) {
                    navController.navigate(navController.getGraph().getStartDestinationId());
                }else {
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
            // ... (Example browser intent logic from previous answer if needed)
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        binding = null;
    }
}
