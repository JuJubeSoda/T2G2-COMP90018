package com.example.myapplication.ui.myplants; // Your package

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// 1. IMPORTANT: Change this import to match your generated binding class
import com.example.myapplication.databinding.CaptureplantBinding;
import com.example.myapplication.R; // For R.drawable.ic_baseline_image_24 if you use it

public class UploadFragment extends Fragment {

    private static final String TAG = "UploadFragment";
    // 2. Use the correct binding class type
    private CaptureplantBinding binding;
    private Uri selectedImageUri;

    // ActivityResultLauncher for picking an image
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                selectedImageUri = result.getData().getData();
                                if (selectedImageUri != null) {
                                    // 3. Ensure your captureplant.xml has an ImageView with id 'imageViewPreview'
                                    if (binding.imageViewPreview != null) {
                                        binding.imageViewPreview.setImageURI(selectedImageUri);
                                    }
                                    Log.d(TAG, "Image selected: " + selectedImageUri.toString());
                                } else {
                                    Log.e(TAG, "Selected image URI is null");
                                }
                            } else {
                                Log.d(TAG, "Image selection cancelled or failed.");
                            }
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 4. Inflate using the correct binding class
        binding = CaptureplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 5. Ensure your captureplant.xml has these Button IDs
        if (binding.buttonSelectImage != null) {
            binding.buttonSelectImage.setOnClickListener(v -> openImageChooser());
        }

        if (binding.buttonUpload != null) {
            binding.buttonUpload.setOnClickListener(v -> attemptUpload());
        }

        // Add a Log to confirm onViewCreated is called
        Log.d(TAG, "UploadFragment onViewCreated");
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void attemptUpload() {
        // 6. Ensure your captureplant.xml has an EditText with id 'editTextPlantName'
        String plantName = "";
        if (binding.editTextPlantName != null) {
            plantName = binding.editTextPlantName.getText().toString().trim();
        }

        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please select an image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to upload: " + selectedImageUri.toString());
        if (!plantName.isEmpty()) {
            Log.d(TAG, "Plant Name: " + plantName);
        }

        // 7. Ensure your captureplant.xml has these View IDs
        if (binding.progressBarUpload != null) {
            binding.progressBarUpload.setVisibility(View.VISIBLE);
        }
        if (binding.buttonUpload != null) {
            binding.buttonUpload.setEnabled(false);
        }
        if (binding.buttonSelectImage != null) {
            binding.buttonSelectImage.setEnabled(false);
        }

        new android.os.Handler(Looper.getMainLooper()).postDelayed( // Use getMainLooper() for Handler
                () -> {
                    boolean uploadSuccess = true; // Simulate success

                    if (uploadSuccess) {
                        Toast.makeText(getContext(), "Plant uploaded successfully!", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Simulated Upload Success.");
                        // Optional: Navigate back
                        // NavController navController = Navigation.findNavController(requireView());
                        // navController.popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Upload failed. Please try again.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Simulated Upload Failed.");
                    }

                    if (binding.progressBarUpload != null) {
                        binding.progressBarUpload.setVisibility(View.GONE);
                    }
                    if (binding.buttonUpload != null) {
                        binding.buttonUpload.setEnabled(true);
                    }
                    if (binding.buttonSelectImage != null) {
                        binding.buttonSelectImage.setEnabled(true);
                    }
                    if (binding.imageViewPreview != null) {
                        // Assuming you have this placeholder drawable
                        binding.imageViewPreview.setImageResource(R.drawable.ic_baseline_image_24);
                    }
                    selectedImageUri = null;
                    if (binding.editTextPlantName != null) {
                        binding.editTextPlantName.setText("");
                    }
                },
                3000
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
