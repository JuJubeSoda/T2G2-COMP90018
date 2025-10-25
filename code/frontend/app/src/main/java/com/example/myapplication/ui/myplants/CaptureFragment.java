package com.example.myapplication.ui.myplants;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.CaptureplantBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CaptureFragment - Camera interface for capturing plant photos.
 * 
 * Purpose:
 * - Provide camera preview and capture functionality
 * - Handle camera permissions
 * - Save captured images to device storage
 * - Pass image URI and plant data to UploadFragment
 * 
 * User Flow:
 * 1. User arrives from AddPlantFragment (with pre-filled plant name) or directly
 * 2. Fragment requests camera permissions if needed
 * 3. Camera preview starts automatically
 * 4. User frames plant and taps capture button
 * 5. Image saved to MediaStore (Pictures/AppName/)
 * 6. Navigates to UploadFragment with image URI and plant data
 * 
 * Key Features:
 * - CameraX integration for modern camera API
 * - Back camera with preview
 * - High-quality JPEG capture (90% quality)
 * - MediaStore integration for proper image storage
 * - Permission handling with fallback for older Android versions
 * - Button state management to prevent double-capture
 * 
 * Permissions Required:
 * - CAMERA: For camera access
 * - WRITE_EXTERNAL_STORAGE: For Android 9 and below
 * 
 * Arguments (from AddPlantFragment):
 * - scientificName: Pre-filled plant name (optional)
 * - isFavouriteFlow: Whether adding to favourites
 * 
 * Navigation:
 * - From: AddPlantFragment or direct navigation
 * - To: UploadFragment with image URI and plant data
 */
public class CaptureFragment extends Fragment {

    private static final String TAG = "CaptureFragment";
    
    /** Filename format for saved images (timestamp-based) */
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    
    /** View binding for captureplant.xml layout */
    private CaptureplantBinding binding;
    
    /** Executor for camera operations (background thread) */
    private ExecutorService cameraExecutor;
    
    /** CameraX ImageCapture use case for taking photos */
    private ImageCapture imageCapture;
    
    /** Navigation controller for fragment transitions */
    private NavController navController;

    /** Pre-filled plant name from AddPlantFragment (optional) */
    private String scientificNameToPass;
    
    /** Whether user is adding to favourites */
    private boolean isFavouriteFlowToPass = false;

    /**
     * Permission launcher for camera and storage access.
     * 
     * Callback Behavior:
     * - All granted: Start camera preview
     * - Any denied: Show error message, camera won't start
     */
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    if (!entry.getValue()) allGranted = false;
                }
                if (allGranted) {
                    Log.d(TAG, "All necessary permissions GRANTED. Starting camera.");
                    startCamera();
                } else {
                    Log.e(TAG, "One or more permissions DENIED.");
                    Toast.makeText(getContext(), "Camera permissions are required.", Toast.LENGTH_LONG).show();
                }
            });

    /**
     * Fragment creation lifecycle method.
     * Retrieves plant data from arguments passed by AddPlantFragment.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            scientificNameToPass = getArguments().getString("scientificName");
            isFavouriteFlowToPass = getArguments().getBoolean("isFavouriteFlow", false);

            Log.d(TAG, "Received scientific name to pass along: " + scientificNameToPass);
            Log.d(TAG, "Received isFavouriteFlow to pass along: " + isFavouriteFlowToPass);
        }
    }

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CaptureplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up camera and UI after view is created.
     * 
     * Initialization Order:
     * 1. Get navigation controller
     * 2. Create camera executor (background thread)
     * 3. Check/request permissions, start camera if granted
     * 4. Setup button click listeners
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check permissions and start camera if granted
        if (checkAndRequestPermissions()) {
            startCamera();
        }

        // Setup button listeners
        binding.backButton.setOnClickListener(v -> navController.popBackStack());
        binding.imageCaptureButton.setOnClickListener(v -> takePhoto());
    }

    /**
     * Checks camera permissions and requests if needed.
     * 
     * Permissions Checked:
     * - CAMERA: Always required
     * - WRITE_EXTERNAL_STORAGE: Only for Android 9 (API 28) and below
     * 
     * @return true if all permissions granted, false if requesting
     */
    private boolean checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        // Check write permission for older Android versions
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
            return false;
        }
        return true;
    }

    /**
     * Initializes and starts camera preview using CameraX.
     * 
     * Setup:
     * - Creates Preview use case for live camera feed
     * - Creates ImageCapture use case with 90% JPEG quality
     * - Uses back camera by default
     * - Binds to fragment lifecycle for automatic cleanup
     * 
     * Error Handling:
     * - Logs errors if camera binding fails
     * - Runs on main thread executor for UI updates
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                // Setup preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
                
                // Setup image capture with high quality
                imageCapture = new ImageCapture.Builder().setJpegQuality(90).build();
                
                // Use back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                
                // Unbind previous use cases and bind new ones
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "startCamera: Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Captures photo and saves to MediaStore.
     * 
     * Process:
     * 1. Disable capture button to prevent double-capture
     * 2. Generate timestamp-based filename
     * 3. Create MediaStore ContentValues
     * 4. Capture image using ImageCapture use case
     * 5. Save to Pictures/AppName/ directory
     * 6. Navigate to UploadFragment with image URI
     * 
     * Button State Management:
     * - Disabled during capture
     * - Re-enabled on error or navigation failure
     * - Not re-enabled on success (navigates away)
     * 
     * Error Handling:
     * - Null URI check
     * - Navigation exception handling
     * - Capture error handling
     * - User feedback via Toast messages
     */
    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "takePhoto: ImageCapture is null.");
            return;
        }

        // Disable button to prevent double-capture
        binding.imageCaptureButton.setEnabled(false);
        Toast.makeText(getContext(), "Capturing...", Toast.LENGTH_SHORT).show();

        // Generate timestamp-based filename
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg";
        
        // Setup MediaStore content values
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name));
        }

        // Create output file options for MediaStore
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                requireContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        // Capture image
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri == null) {
                            Log.e(TAG, "onImageSaved: Saved URI is null.");
                            Toast.makeText(getContext(), "Failed to save image.", Toast.LENGTH_SHORT).show();
                            binding.imageCaptureButton.setEnabled(true); // Re-enable on failure
                            return;
                        }

                        Log.d(TAG, "Photo capture succeeded: " + savedUri);

                        // Prepare arguments for UploadFragment
                        Bundle args = new Bundle();
                        args.putString(UploadFragment.ARG_IMAGE_URI, savedUri.toString());
                        if (scientificNameToPass != null) {
                            args.putString(UploadFragment.ARG_SCIENTIFIC_NAME, scientificNameToPass);
                        }
                        args.putBoolean(UploadFragment.ARG_IS_FAVOURITE_FLOW, isFavouriteFlowToPass);

                        // Navigate to UploadFragment
                        try {
                            navController.navigate(R.id.navigation_upload_plant, args);
                            Log.d(TAG, "Navigating to UploadFragment...");
                        } catch (Exception e) {
                            Log.e(TAG, "Navigation to UploadFragment failed!", e);
                            Toast.makeText(getContext(), "Error: Could not open upload screen.", Toast.LENGTH_LONG).show();
                            binding.imageCaptureButton.setEnabled(true); // Re-enable on navigation failure
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(getContext(), "Photo capture failed.", Toast.LENGTH_SHORT).show();
                        binding.imageCaptureButton.setEnabled(true); // Re-enable on capture error
                    }
                }
        );
    }

    /**
     * Cleans up resources when view is destroyed.
     * Shuts down camera executor and clears binding.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        binding = null;
    }
}
