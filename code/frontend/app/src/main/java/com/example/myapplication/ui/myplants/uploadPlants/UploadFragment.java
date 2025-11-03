/**
 * UploadFragment - Handles plant detail input and upload to backend.
 * 
 * User Flow:
 * 1. User arrives from CaptureFragment with captured image URI
 * 2. Pre-fills scientific name if coming from AddPlantFragment
 * 3. User fills in description and other details
 * 4. User toggles "Share with other users" switch (public visibility)
 * 5. User clicks "Upload" button
 * 6. Fragment requests location permission if needed
 * 7. Gets device location (or proceeds without if denied)
 * 8. Converts image URI to Base64 string
 * 9. Creates PlantRequest with all data
 * 10. Uploads to backend via API
 * 11. Shows success overlay and navigates to UploadCompleteFragment
 * 
 * Key Features:
 * - Location permission handling with graceful fallback
 * - Image URI preservation across configuration changes
 * - Base64 image encoding for API transmission
 * - Public/private visibility toggle
 * - ISO 8601 timestamp generation
 * - Error handling and user feedback
 * 
 * Arguments (from CaptureFragment):
 * - ARG_IMAGE_URI: URI string of captured photo
 * - ARG_SCIENTIFIC_NAME: Pre-filled plant name (optional)
 * - ARG_IS_FAVOURITE_FLOW: Whether plant is marked as favourite
 */
package com.example.myapplication.ui.myplants.uploadPlants;

// Android framework and utility imports
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

// AndroidX imports
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// Application-specific and library imports
import com.example.myapplication.R;
import com.example.myapplication.databinding.UploadplantBinding;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantRequest;
import com.example.myapplication.utils.ImageUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class UploadFragment extends Fragment {

    private static final String TAG = "UploadFragment_Details";
    private static final String KEY_IMAGE_URI = "key_image_uri"; // For saving state
    
    /** View binding for uploadplant.xml layout */
    private UploadplantBinding binding;
    
    /** Navigation controller for fragment transitions */
    private NavController navController;

    /** Image URI string received from CaptureFragment (preserved across config changes) */
    private String receivedImageUriString;
    
    /** Pre-filled scientific name from AddPlantFragment (optional) */
    private String receivedScientificName;
    
    /** Whether plant is marked as favourite */
    private boolean receivedIsFavouriteFlow;

    /** Google Play Services location client for getting device GPS coordinates */
    private FusedLocationProviderClient fusedLocationClient;
    
    /**
     * Permission launcher for location access during upload.
     * 
     * Flow:
     * - Granted: Retry getting location and proceed with upload
     * - Denied: Upload without location data (graceful degradation)
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Location permission granted. Proceeding with upload.");
                    getCurrentLocationAndUpload();
                } else {
                    Log.w(TAG, "Location permission denied by user.");
                    Toast.makeText(getContext(), "Location denied. Uploading without location data.", Toast.LENGTH_LONG).show();
                    uploadPlantToBackend(null); // Proceed without location
                }
            });

    /** Argument keys for navigation (passed from CaptureFragment) */
    public static final String ARG_IMAGE_URI = "imageUri";
    public static final String ARG_SCIENTIFIC_NAME = "scientificName";
    public static final String ARG_IS_FAVOURITE_FLOW = "isFavouriteFlow";

    /**
     * Fragment creation lifecycle method.
     * Retrieves arguments from navigation and initializes location client.
     * Handles state restoration after configuration changes (e.g., screen rotation).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore image URI from saved state if available (handles config changes)
        if (savedInstanceState != null) {
            receivedImageUriString = savedInstanceState.getString(KEY_IMAGE_URI);
            Log.d(TAG, "Restored image URI from saved state: " + receivedImageUriString);
        }

        // Get arguments from navigation (only on first creation)
        if (getArguments() != null && receivedImageUriString == null) {
            receivedImageUriString = getArguments().getString(ARG_IMAGE_URI);
            receivedScientificName = getArguments().getString(ARG_SCIENTIFIC_NAME);
            receivedIsFavouriteFlow = getArguments().getBoolean(ARG_IS_FAVOURITE_FLOW, false);
            Log.d(TAG, "Received image URI from arguments: " + receivedImageUriString);
        }
        
        // Initialize Google Play Services location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    /**
     * Saves image URI to instance state to preserve it across configuration changes.
     * Critical for preventing image loss when device is rotated.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (receivedImageUriString != null) {
            outState.putString(KEY_IMAGE_URI, receivedImageUriString);
            Log.d(TAG, "Saving image URI to instance state: " + receivedImageUriString);
        }
    }

    /**
     * Inflates the layout for this fragment using View Binding.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UploadplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI components after view is created.
     * - Displays captured image preview
     * - Pre-fills scientific name if provided
     * - Sets up button click listeners
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Display captured image in preview
        if (receivedImageUriString != null) {
            binding.plantImageViewPreview.setImageURI(Uri.parse(receivedImageUriString));
        }

        // Pre-fill and lock scientific name if provided from AddPlantFragment
        if (receivedScientificName != null && !receivedScientificName.isEmpty()) {
            binding.editTextScientificName.setText(receivedScientificName);
            binding.editTextScientificName.setEnabled(false); // Lock field
        }

        // Back button - return to previous screen
        binding.backButtonUpload.setOnClickListener(v -> navController.popBackStack());
        
        // Upload button - start upload process with location permission check
        binding.uploadButton.setOnClickListener(v -> triggerUploadProcess());
    }

    /**
     * Initiates the plant upload process.
     * 
     * Steps:
     * 1. Validates required fields (scientific name)
     * 2. Disables upload button to prevent double-submission
     * 3. Checks location permission status
     * 4. Either proceeds with upload or requests permission
     * 
     * Location handling:
     * - Permission granted: Get location and upload
     * - Permission not granted: Request it (result handled by launcher)
     */
    private void triggerUploadProcess() {
        // Validate required field
        String scientificName = binding.editTextScientificName.getText().toString().trim();
        if (scientificName.isEmpty()) {
            Toast.makeText(getContext(), "Scientific name must be filled.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-submission
        binding.uploadButton.setEnabled(false);
        binding.uploadButton.setText("Uploading...");

        // Check if location permission is already granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndUpload();
        } else {
            // Request permission (result handled by requestPermissionLauncher)
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Retrieves device's last known GPS location using FusedLocationProviderClient.
     * 
     * Behavior:
     * - Success with location: Proceeds with upload including coordinates
     * - Success but null location: Proceeds without coordinates (graceful fallback)
     * - Failure: Proceeds without coordinates and shows error message
     * 
     * Note: @SuppressLint is safe here as we only call this after permission check
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndUpload() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        Log.d(TAG, "Location acquired: Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                        uploadPlantToBackend(location);
                    } else {
                        // Location services might be off or no cached location available
                        Log.w(TAG, "Could not get location. Uploading without location data.");
                        Toast.makeText(getContext(), "Could not retrieve location. Uploading without it.", Toast.LENGTH_SHORT).show();
                        uploadPlantToBackend(null);
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    Log.e(TAG, "Failed to get location", e);
                    Toast.makeText(getContext(), "Failed to get location. Uploading without it.", Toast.LENGTH_SHORT).show();
                    uploadPlantToBackend(null);
                });
    }

    /**
     * Uploads plant data to backend API.
     * 
     * Process:
     * 1. Validates image URI exists
     * 2. Converts image URI to Base64 string
     * 3. Generates ISO 8601 timestamps
     * 4. Reads public visibility toggle
     * 5. Creates PlantRequest with all data
     * 6. Sends POST request to /api/plants/add
     * 7. Handles response (success → overlay, failure → error message)
     * 
     * @param location GPS location (nullable - may be null if permission denied or unavailable)
     */
    private void uploadPlantToBackend(@Nullable Location location) {
        // Safety check: Ensure image URI wasn't lost
        if (receivedImageUriString == null) {
            Log.e(TAG, "Cannot upload, image URI string is null.");
            Toast.makeText(getContext(), "Error: Image data was lost. Please try again.", Toast.LENGTH_LONG).show();
            resetUploadButton();
            return;
        }

        // Safety check: Ensure fragment is still attached
        if (getContext() == null) {
            resetUploadButton();
            return;
        }

        // Convert image URI to Base64 for API transmission
        String base64Image = ImageUtils.convertImageToBase64(getContext(), Uri.parse(receivedImageUriString));
        if (base64Image == null) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            resetUploadButton();
            return;
        }

        // Generate ISO 8601 UTC timestamp for createdAt and updatedAt
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(new Date());

        // Read public visibility preference from toggle switch
        boolean isPublic = binding.switchShowPublicly.isChecked();
        Log.d(TAG, "Plant visibility set to public: " + isPublic);

        // Build complete plant request object
        PlantRequest plantRequest = new PlantRequest(
                binding.editTextScientificName.getText().toString().trim(), // name
                base64Image, // Base64 encoded image
                binding.editTextIntroduction.getText().toString().trim(), // description
                location != null ? location.getLatitude() : null, // latitude (nullable)
                location != null ? location.getLongitude() : null, // longitude (nullable)
                binding.editTextScientificName.getText().toString().trim(), // scientificName
                currentTime, // createdAt
                currentTime, // updatedAt
                null, // gardenId (null for new plants)
                receivedIsFavouriteFlow, // isFavourite
                isPublic // isPublic - controls nearby discoveries visibility
        );

        // Send API request
        ApiService apiService = ApiClient.create(getContext());
        Call<ApiResponse> call = apiService.addPlant(plantRequest);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Plant uploaded successfully!", Toast.LENGTH_SHORT).show();
                    showSuccessOverlay(location);
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    resetUploadButton();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    resetUploadButton();
                }
            }
        });
    }

    /**
     * Re-enables upload button after failure.
     * Allows user to retry upload.
     */
    private void resetUploadButton() {
        if (binding != null) {
            binding.uploadButton.setEnabled(true);
            binding.uploadButton.setText("Upload");
        }
    }

    /**
     * Displays success overlay and navigates to UploadCompleteFragment.
     * Passes plant data for preview display.
     */
    private void showSuccessOverlay(@Nullable Location location) {
        if (getContext() == null || binding == null) return;
        
        binding.successOverlay.getRoot().setVisibility(View.VISIBLE);
        binding.successOverlay.okButtonSuccess.setOnClickListener(view -> {
            binding.successOverlay.getRoot().setVisibility(View.GONE);
            
            // Prepare data for UploadCompleteFragment
            Bundle args = new Bundle();
            args.putString(UploadCompleteFragment.ARG_IMAGE_URI, receivedImageUriString);
            args.putString(UploadCompleteFragment.ARG_SCIENTIFIC_NAME, binding.editTextScientificName.getText().toString().trim());
            args.putString(UploadCompleteFragment.ARG_INTRODUCTION, binding.editTextIntroduction.getText().toString().trim());
            String locationString;
            if (location != null) {
                // Format the Latitude and Longitude into a readable string
                locationString = String.format(Locale.getDefault(), "(%.4f, %.4f)",
                        location.getLatitude(),
                        location.getLongitude());
            } else {
                // If location is null, set a specific string
                locationString = "Location not available";
            }
            args.putString(UploadCompleteFragment.ARG_LOCATION, locationString);
            args.putBoolean("isFavourite", receivedIsFavouriteFlow);
            
            navController.navigate(R.id.navigation_upload_complete_preview, args);
        });
    }

    /**
     * Cleans up view binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
