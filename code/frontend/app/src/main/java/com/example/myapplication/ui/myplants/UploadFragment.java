package com.example.myapplication.ui.myplants; // Ensure this is your correct package

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.Button; // No longer needed if accessing via binding
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// Crucially, make sure this R is your app's R, not android.R
import com.example.myapplication.R; // <<< ADD THIS if missing or incorrect
import com.example.myapplication.databinding.UploadplantBinding;
// If UploadplantsuccessBinding is in a different package, you might need its specific import too,
// but usually it's not necessary if it's generated in the same module.

public class UploadFragment extends Fragment {

    private static final String TAG = "UploadFragment_Details";
    private UploadplantBinding binding; // This is the binding for uploadplant.xml
    private String receivedImageUriString;
    private NavController navController;

    // This is the key CaptureFragment will use to send the image URI
    public static final String ARG_IMAGE_URI = "imageUri";

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receivedImageUriString = getArguments().getString(ARG_IMAGE_URI);
            if (receivedImageUriString != null) {
                Log.d(TAG, "Received image URI: " + receivedImageUriString);
            } else {
                Log.e(TAG, "Argument for image URI (" + ARG_IMAGE_URI + ") is null.");
            }
        } else {
            Log.e(TAG, "No arguments bundle received for UploadFragment.");
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

        if (receivedImageUriString != null && !receivedImageUriString.isEmpty()) {
            Uri imageUri = Uri.parse(receivedImageUriString);
            binding.plantImageViewPreview.setImageURI(imageUri); // Assumes ID in uploadplant.xml
            Log.d(TAG, "Image set to ImageView.");
        } else {
            Toast.makeText(getContext(), "Error: No image to display.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "receivedImageUriString is null or empty, cannot display image.");
        }

        binding.backButtonUpload.setOnClickListener(v -> {
            if (navController != null) {
                navController.popBackStack();
            }
        });

        binding.uploadButton.setOnClickListener(v -> {
            String scientificName = binding.editTextScientificName.getText().toString().trim();
            String location = binding.editTextLocation.getText().toString().trim();
            String introduction = binding.editTextIntroduction.getText().toString().trim();
            String searchTag = binding.editTextSearchTags.getText().toString().trim();

            if (receivedImageUriString == null || receivedImageUriString.isEmpty()) {
                Toast.makeText(getContext(), "No image has been captured to upload.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (scientificName.isEmpty()) {
                binding.editTextScientificName.setError("Scientific Name is required");
                binding.editTextScientificName.requestFocus();
                return;
            }
            if (location.isEmpty()) {
                binding.editTextLocation.setError("Location is required");
                binding.editTextLocation.requestFocus();
                return;
            }
            if (introduction.isEmpty()) {
                binding.editTextIntroduction.setError("Introduction is required");
                binding.editTextIntroduction.requestFocus();
                return;
            }

            Log.i(TAG, "UPLOAD REQUESTED (Simulated):");
            Log.i(TAG, "Image URI: " + receivedImageUriString);
            Log.i(TAG, "Scientific Name: " + scientificName);
            Log.i(TAG, "Location: " + location);
            Log.i(TAG, "Introduction: " + introduction);
            Log.i(TAG, "Search Tag: " + searchTag);


            Toast.makeText(getContext(), "Simulating upload for '" + scientificName + "'...", Toast.LENGTH_LONG).show();

            // Simulate a delay for the upload
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showSuccessOverlay();
            }, 1500); // 1.5 second delay
        });
    }

    private void showSuccessOverlay() {
        if (getContext() == null || binding == null) { // Added null check for binding
            Log.e(TAG, "showSuccessOverlay: Context or binding is null.");
            return;
        }

        // --- Corrected way to use <include> with ViewBinding ---
        // Assumes:
        // 1. In uploadplant.xml, your <include> tag has android:id="@+id/successOverlay"
        // 2. uploadplantsuccess.xml uses ViewBinding (its root is not <merge>)
        // 3. UploadplantBinding generates a field 'successOverlay' of type 'UploadplantsuccessBinding'

        if (binding.successOverlay != null) { // successOverlay should be of type UploadplantsuccessBinding
            // Make the entire included layout visible
            binding.successOverlay.getRoot().setVisibility(View.VISIBLE);

            // Access the OK button directly via its ID in the UploadplantsuccessBinding
            // (assuming the button in uploadplantsuccess.xml has android:id="@+id/okButtonSuccess")
            binding.successOverlay.okButtonSuccess.setOnClickListener(view -> {
                binding.successOverlay.getRoot().setVisibility(View.GONE); // Hide the overlay

                // Navigate back or to another destination
                if (navController != null) {
                    navController.popBackStack(); // Example: Go back after success
                    // Or navigate to a "My Plants" list, etc.
                    // navController.navigate(R.id.action_uploadFragment_to_myPlantsFragment);
                } else {
                    Log.e(TAG, "NavController is null, cannot navigate after success overlay.");
                }
            });
        } else {
            Log.e(TAG, "binding.successOverlay is null. Check <include> ID in uploadplant.xml and ViewBinding setup.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // If you were using the ViewStub alternative, clear its view reference here
        binding = null; // Important to prevent memory leaks
        Log.d(TAG, "onDestroyView called, binding set to null.");
    }
}

