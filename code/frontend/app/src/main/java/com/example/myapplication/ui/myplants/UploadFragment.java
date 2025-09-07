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

import com.example.myapplication.databinding.UploadplantBinding;

public class UploadFragment extends Fragment { // Class name is UploadFragment

    private static final String TAG = "UploadFragment_Details"; // Changed TAG for clarity
    private UploadplantBinding binding;
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
            receivedImageUriString = getArguments().getString(ARG_IMAGE_URI); // Uses the defined constant
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
            binding.plantImageViewPreview.setImageURI(imageUri); // Assumes ID in uploadplant.xml is 'imageView'
            Log.d(TAG, "Image set to ImageView (ID: imageView).");
        } else {
            Toast.makeText(getContext(), "Error: No image to display.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "receivedImageUriString is null or empty, cannot display image.");
        }

        binding.backButtonUpload.setOnClickListener(v -> { // Back button
            if (navController != null) {
                navController.popBackStack();
            }
        });

        binding.uploadButton.setOnClickListener(v -> { // Upload button
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

            Log.i(TAG, "UPLOAD REQUESTED:");
            Log.i(TAG, "Image URI: " + receivedImageUriString);
            Log.i(TAG, "Scientific Name: " + scientificName);
            // ... (log other fields) ...

            Toast.makeText(getContext(), "Simulating upload for '" + scientificName + "'...", Toast.LENGTH_LONG).show();

            if (navController != null) {
                navController.popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called, binding set to null.");
    }
}
