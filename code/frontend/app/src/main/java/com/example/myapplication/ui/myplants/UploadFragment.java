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

import androidx.activity.result.ActivityResultCallback;
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
import androidx.lifecycle.LifecycleOwner; // Important

import com.example.myapplication.databinding.CaptureplantBinding; // Ensure this matches your layout file name
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadFragment extends Fragment {

    private static final String TAG = "UploadFragment";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private CaptureplantBinding binding; // Make sure this matches your layout file name

    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private Uri capturedImageUri; // To store the URI of the captured image

    // Permission Launcher
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                    // You might also request WRITE_EXTERNAL_STORAGE here if needed for older APIs
                    // Boolean storageGranted = result.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);

                    if (cameraGranted != null && cameraGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(getContext(), "Camera permission is required to use this feature.", Toast.LENGTH_LONG).show();
                        // Handle permission denial (e.g., navigate back or disable camera features)
                    }
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CaptureplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionsLauncher.launch(new String[]{Manifest.permission.CAMERA});
            // Add Manifest.permission.WRITE_EXTERNAL_STORAGE if targeting API <= 28 and saving to public storage
        }

        binding.imageButton.setOnClickListener(v -> takePhoto());

        // You'll need an upload button and logic for the capturedImageUri
        // binding.buttonUpload.setOnClickListener(v -> attemptUploadCapturedImage());
        Log.d(TAG, "UploadFragment onViewCreated");
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        // Add || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        // if targeting API <= 28 and saving to public storage
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview Use Case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());

                // ImageCapture Use Case
                imageCapture = new ImageCapture.Builder()
                        // .setTargetRotation(binding.cameraPreviewView.getDisplay().getRotation()) // Optional: handle rotation
                        .build();

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner)this, // Bind to fragment's lifecycle
                        cameraSelector,
                        preview,
                        imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
                Toast.makeText(getContext(), "Error starting camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture use case is null. Cannot take photo.");
            return;
        }

        // Create time-stamped name and MediaStore entry.
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) { // For API 29+
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyPlantsApp"); // Saves to Pictures/MyPlantsApp
        }

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(requireContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Save to gallery
                contentValues)
                .build();

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()), // Executes on main thread
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        capturedImageUri = outputFileResults.getSavedUri();
                        String msg = "Photo capture succeeded: " + capturedImageUri;
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);

//                        // Show the captured image in the small ImageView (optional)
//                        if (binding.imageViewCapturedPreview != null && capturedImageUri != null) {
//                            binding.imageViewCapturedPreview.setImageURI(capturedImageUri);
//                            binding.imageViewCapturedPreview.setVisibility(View.VISIBLE);
//                        }
                        // Now you can enable an "Upload" button or proceed to an upload step
                        // binding.buttonUpload.setEnabled(true);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(getContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // You would then have a method like this:
    // private void attemptUploadCapturedImage() {
    //     if (capturedImageUri != null) {
    //         Log.d(TAG, "Uploading captured image: " + capturedImageUri);
    //         // ... your upload logic using capturedImageUri ...
    //     } else {
    //         Toast.makeText(getContext(), "No image captured to upload.", Toast.LENGTH_SHORT).show();
    //     }
    // }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        binding = null;
    }
}
