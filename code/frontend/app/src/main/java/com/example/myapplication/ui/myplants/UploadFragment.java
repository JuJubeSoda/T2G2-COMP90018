// /.../app/src/main/java/com/example/myapplication/ui/myplants/UploadFragment.java

package com.example.myapplication.ui.myplants;

// Android framework and utility imports...
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

// AndroidX imports...
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadFragment extends Fragment {

    private static final String TAG = "UploadFragment_Details";
    private UploadplantBinding binding;
    private NavController navController;

    // --- MODIFICATION 1: Fields to store all received data ---
    private String receivedImageUriString;
    private String receivedScientificName;
    private boolean receivedIsFavouriteFlow;

    // --- MODIFICATION 2: Define public constant keys for all arguments ---
    public static final String ARG_IMAGE_URI = "imageUri";
    public static final String ARG_SCIENTIFIC_NAME = "scientificName";
    public static final String ARG_IS_FAVOURITE_FLOW = "isFavouriteFlow";

    public UploadFragment() {
        // Required empty public constructor
    }

    /**
     * --- MODIFICATION 3: Receive all arguments when the fragment is created. ---
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receivedImageUriString = getArguments().getString(ARG_IMAGE_URI);
            receivedScientificName = getArguments().getString(ARG_SCIENTIFIC_NAME);
            receivedIsFavouriteFlow = getArguments().getBoolean(ARG_IS_FAVOURITE_FLOW, false);

            Log.d(TAG, "Received image URI: " + receivedImageUriString);
            Log.d(TAG, "Received Scientific Name: " + receivedScientificName);
            Log.d(TAG, "Received isFavouriteFlow: " + receivedIsFavouriteFlow);
        } else {
            Log.e(TAG, "No arguments bundle received.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UploadplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * --- MODIFICATION 4: Pre-fill the scientific name field. ---
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Set the captured image
        if (receivedImageUriString != null && !receivedImageUriString.isEmpty()) {
            binding.plantImageViewPreview.setImageURI(Uri.parse(receivedImageUriString));
        }

        // Pre-fill and disable the scientific name field if a name was passed
        if (receivedScientificName != null && !receivedScientificName.isEmpty()) {
            binding.editTextScientificName.setText(receivedScientificName);
            // Make the field read-only
            binding.editTextScientificName.setEnabled(false);
            binding.editTextScientificName.setFocusable(false);
            binding.editTextScientificName.setClickable(false);
            Log.d(TAG, "Prefilled and disabled scientific name field.");
        }

        binding.backButtonUpload.setOnClickListener(v -> navController.popBackStack());

        binding.uploadButton.setOnClickListener(v -> {
            // (Validation logic is correct and requires no changes)
            String scientificName = binding.editTextScientificName.getText().toString().trim();
            String location = binding.editTextLocation.getText().toString().trim();
            String introduction = binding.editTextIntroduction.getText().toString().trim();
            // ... field validation checks ...
            if (scientificName.isEmpty() || location.isEmpty() || introduction.isEmpty()) {
                // Show errors if needed
                Toast.makeText(getContext(), "All fields must be filled.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i(TAG, "UPLOAD REQUESTED:");
            Log.i(TAG, "Image URI: " + receivedImageUriString);
            Log.i(TAG, "Scientific Name: " + scientificName);
            Log.i(TAG, "Location: " + location);
            Log.i(TAG, "Is Favourite on Upload: " + receivedIsFavouriteFlow);

            // Show loading state
            binding.uploadButton.setEnabled(false);
            binding.uploadButton.setText("Uploading...");
            Toast.makeText(getContext(), "Uploading plant data...", Toast.LENGTH_SHORT).show();
            
            // Upload to backend
            uploadPlantToBackend(scientificName, location, introduction);
        });
    }

    /**
     * Uploads plant data to the backend API.
     */
    private void uploadPlantToBackend(String scientificName, String location, String introduction) {
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot upload");
            resetUploadButton();
            return;
        }
        
        // Convert image to base64
        String base64Image = null;
        if (receivedImageUriString != null && !receivedImageUriString.isEmpty()) {
            base64Image = ImageUtils.convertImageToBase64(getContext(), Uri.parse(receivedImageUriString));
            if (base64Image == null) {
                Log.e(TAG, "Failed to convert image to base64");
                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                resetUploadButton();
                return;
            }
        }
        
        // Create plant request
        PlantRequest plantRequest = new PlantRequest(
            scientificName, // Use scientific name as the plant name
            base64Image,
            introduction,
            null, // latitude - could be added later with location services
            null, // longitude - could be added later with location services
            scientificName,
            null, // gardenId - could be added later
            receivedIsFavouriteFlow
        );
        
        // Make API call
        ApiService apiService = ApiClient.create(getContext());
        Call<ApiResponse> call = apiService.addPlant(plantRequest);
        
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "Plant uploaded successfully: " + response.body().getMessage());
                    Toast.makeText(getContext(), "Plant uploaded successfully!", Toast.LENGTH_SHORT).show();
                    showSuccessOverlay();
                } else {
                    Log.e(TAG, "Upload failed: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    resetUploadButton();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Upload failed with exception", t);
                Toast.makeText(getContext(), "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetUploadButton();
            }
        });
    }
    
    /**
     * Resets the upload button to its original state.
     */
    private void resetUploadButton() {
        if (binding != null) {
            binding.uploadButton.setEnabled(true);
            binding.uploadButton.setText("Upload");
        }
    }

    /**
     * --- MODIFICATION 5: Pass the 'isFavourite' value to the final preview screen ---
     */
    private void showSuccessOverlay() {
        if (getContext() == null || binding == null || binding.successOverlay == null) {
            return;
        }

        binding.successOverlay.getRoot().setVisibility(View.VISIBLE);
        binding.successOverlay.okButtonSuccess.setOnClickListener(view -> {
            binding.successOverlay.getRoot().setVisibility(View.GONE);

            // Create a bundle to pass all data to the final preview fragment
            Bundle args = new Bundle();
            args.putString(UploadCompleteFragment.ARG_IMAGE_URI, receivedImageUriString);
            args.putString(UploadCompleteFragment.ARG_SCIENTIFIC_NAME, binding.editTextScientificName.getText().toString().trim());
            args.putString(UploadCompleteFragment.ARG_LOCATION, binding.editTextLocation.getText().toString().trim());
            args.putString(UploadCompleteFragment.ARG_INTRODUCTION, binding.editTextIntroduction.getText().toString().trim());
            args.putString(UploadCompleteFragment.ARG_SEARCH_TAG, binding.editTextSearchTags.getText().toString().trim());

            // Pass the favourite flag along to the final preview screen
            // You will need to add ARG_IS_FAVOURITE to UploadCompleteFragment as well
            args.putBoolean("isFavourite", receivedIsFavouriteFlow);
            Log.d(TAG, "Navigating to UploadCompleteFragment with isFavourite: " + receivedIsFavouriteFlow);

            try {
                navController.navigate(R.id.navigation_upload_complete_preview, args);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Navigation to UploadCompleteFragment failed.", e);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
