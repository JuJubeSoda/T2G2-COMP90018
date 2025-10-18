// /.../app/src/main/java/com/example/myapplication/ui/myplants/UploadFragment.java

package com.example.myapplication.ui.myplants;

// Android framework and utility imports...
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

// AndroidX imports...
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// Application-specific and library imports...
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
    private static final String KEY_IMAGE_URI = "key_image_uri";
    private UploadplantBinding binding;
    private NavController navController;

    private String receivedImageUriString;
    private String receivedScientificName;
    private boolean receivedIsFavouriteFlow;

    // --- MODIFICATION: Add FusedLocationProviderClient and permission launcher ---
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) { // This is the success path
                    Log.d(TAG, "Location permission granted. Proceeding with upload.");
                    getCurrentLocationAndUpload(); // <-- FIX: Retry getting location now
                } else { // This is the denial path
                    Log.w(TAG, "Location permission denied by user.");
                    Toast.makeText(getContext(), "Location denied. Uploading without location data.", Toast.LENGTH_LONG).show();
                    uploadPlantToBackend(null); // <-- FIX: Proceed with the upload but with null location
                }
            });

    public static final String ARG_IMAGE_URI = "imageUri";
    public static final String ARG_SCIENTIFIC_NAME = "scientificName";
    public static final String ARG_IS_FAVOURITE_FLOW = "isFavouriteFlow";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- FIX: Restore from savedInstanceState if it exists ---
        if (savedInstanceState != null) {
            receivedImageUriString = savedInstanceState.getString(KEY_IMAGE_URI);
            Log.d(TAG, "Restored image URI from saved state: " + receivedImageUriString);
        }

        // If not restored, get from arguments (only on first creation)
        if (getArguments() != null && receivedImageUriString == null) {
            receivedImageUriString = getArguments().getString(ARG_IMAGE_URI);
            receivedScientificName = getArguments().getString(ARG_SCIENTIFIC_NAME);
            receivedIsFavouriteFlow = getArguments().getBoolean(ARG_IS_FAVOURITE_FLOW, false);
            Log.d(TAG, "Received image URI from arguments: " + receivedImageUriString);
        }
        // Initialize the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    // --- FIX: Implement onSaveInstanceState to save the URI ---
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (receivedImageUriString != null) {
            outState.putString(KEY_IMAGE_URI, receivedImageUriString);
            Log.d(TAG, "Saving image URI to instance state: " + receivedImageUriString);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UploadplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        if (receivedImageUriString != null) {
            binding.plantImageViewPreview.setImageURI(Uri.parse(receivedImageUriString));
        }

        if (receivedScientificName != null && !receivedScientificName.isEmpty()) {
            binding.editTextScientificName.setText(receivedScientificName);
            binding.editTextScientificName.setEnabled(false);
        }

        binding.backButtonUpload.setOnClickListener(v -> navController.popBackStack());
        // --- MODIFICATION: The upload button now starts the location permission flow ---
        binding.uploadButton.setOnClickListener(v -> triggerUploadProcess());
    }

    /**
     * Starts the upload process by validating fields and then checking for location permissions.
     */
    private void triggerUploadProcess() {
        String scientificName = binding.editTextScientificName.getText().toString().trim();
        if (scientificName.isEmpty()) {
            Toast.makeText(getContext(), "Scientific name must be filled.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.uploadButton.setEnabled(false);
        binding.uploadButton.setText("Uploading...");

        // Check for location permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndUpload();
        } else {
            // Request permission. The result will be handled by the ActivityResultLauncher.
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Fetches the device's last known location and then proceeds to call the backend upload method.
     */
    @SuppressLint("MissingPermission") // We only call this after checking for permission.
    private void getCurrentLocationAndUpload() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        Log.d(TAG, "Location acquired: Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                        uploadPlantToBackend(location);
                    } else {
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
     * --- MODIFICATION: This method now takes a Location object and handles timestamps. ---
     */
    private void uploadPlantToBackend(@Nullable Location location) {
        // --- FIX: Final safety check to prevent crash ---
        if (receivedImageUriString == null) {
            Log.e(TAG, "Cannot upload, image URI string is null.");
            Toast.makeText(getContext(), "Error: Image data was lost. Please try again.", Toast.LENGTH_LONG).show();
            resetUploadButton();
            return;
        }

        if (getContext() == null) {
            resetUploadButton();
            return;
        }

        String base64Image = ImageUtils.convertImageToBase64(getContext(), Uri.parse(receivedImageUriString));
        if (base64Image == null) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            resetUploadButton();
            return;
        }

        // Generate current timestamp in ISO 8601 UTC format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(new Date());

        // Create the request object with all dynamic data
        PlantRequest plantRequest = new PlantRequest(
                binding.editTextScientificName.getText().toString().trim(), // name
                base64Image,
                binding.editTextIntroduction.getText().toString().trim(), // description
                location != null ? location.getLatitude() : null, // latitude
                location != null ? location.getLongitude() : null, // longitude
                binding.editTextScientificName.getText().toString().trim(), // scientificName
                currentTime, // createdAt
                currentTime, // updatedAt
                null, // gardenId
                receivedIsFavouriteFlow
        );

        ApiService apiService = ApiClient.create(getContext());
        Call<ApiResponse> call = apiService.addPlant(plantRequest);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Plant uploaded successfully!", Toast.LENGTH_SHORT).show();
                    showSuccessOverlay();
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

    private void resetUploadButton() {
        if (binding != null) {
            binding.uploadButton.setEnabled(true);
            binding.uploadButton.setText("Upload");
        }
    }

    private void showSuccessOverlay() {
        // This method remains the same, but the hardcoded location field is gone
        // You may want to update UploadCompleteFragment to handle latitude/longitude instead of a location string
        if (getContext() == null || binding == null) return;
        binding.successOverlay.getRoot().setVisibility(View.VISIBLE);
        binding.successOverlay.okButtonSuccess.setOnClickListener(view -> {
            binding.successOverlay.getRoot().setVisibility(View.GONE);
            Bundle args = new Bundle();
            // Pass the data that UploadCompleteFragment actually needs
            args.putString(UploadCompleteFragment.ARG_IMAGE_URI, receivedImageUriString);
            args.putString(UploadCompleteFragment.ARG_SCIENTIFIC_NAME, binding.editTextScientificName.getText().toString().trim());
            args.putBoolean("isFavourite", receivedIsFavouriteFlow);
            navController.navigate(R.id.navigation_upload_complete_preview, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
