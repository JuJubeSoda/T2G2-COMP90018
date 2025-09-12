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

import com.example.myapplication.R; // Ensure this is your app's R class
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

public class CaptureFragment extends Fragment {

    private static final String TAG = "CaptureFragment";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private CaptureplantBinding binding;

    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private NavController navController;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    Log.d(TAG, "Permission: " + entry.getKey() + " Granted: " + entry.getValue());
                    if (!entry.getValue()) {
                        allGranted = false;
                    }
                }

                if (allGranted) {
                    Log.d(TAG, "All necessary permissions GRANTED by user. Starting camera.");
                    startCamera();
                } else {
                    Log.e(TAG, "One or more permissions DENIED by user.");
                    Toast.makeText(getContext(), "Camera and/or Storage permissions are required to use this feature.", Toast.LENGTH_LONG).show();
                    // Optional: Navigate back or disable features
                    // if (navController != null) navController.popBackStack();
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
        navController = Navigation.findNavController(view);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (checkAndRequestPermissions()) {
            Log.d(TAG, "Permissions already granted. Starting camera.");
            startCamera();
        }
        // If permissions are not granted, checkAndRequestPermissions() will launch the request.

        // Ensure this ID matches your capture button in captureplant.xml
        // (Assuming it was renamed to imageCaptureButton as per previous layout suggestions)
        if (binding.imageCaptureButton != null) {
            binding.imageCaptureButton.setOnClickListener(v -> takePhoto());
            Log.d(TAG, "Capture button (ID: imageCaptureButton) listener set up.");
        } else {
            Log.e(TAG, "Capture button (e.g., imageCaptureButton) NOT FOUND in binding. Check captureplant.xml ID.");
            Toast.makeText(getContext(), "Capture button not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        permissionsToRequest.add(Manifest.permission.CAMERA);

        // For Android 9 (API 28) and below, WRITE_EXTERNAL_STORAGE is needed for MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // Check if camera permission is already granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Already added to list if not granted
        }


        // If the list of permissions to request is not empty, request them.
        // Otherwise, all necessary permissions are already granted.
        if (!permissionsToRequest.contains(Manifest.permission.CAMERA) && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionsToRequest.add(Manifest.permission.CAMERA); // ensure camera is always in the list if not granted
        }


        List<String> finalPermissionsToRequest = new ArrayList<>();
        for (String perm : permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                finalPermissionsToRequest.add(perm);
            }
        }


        if (!finalPermissionsToRequest.isEmpty()) {
            Log.d(TAG, "Requesting permissions: " + finalPermissionsToRequest);
            requestPermissionsLauncher.launch(finalPermissionsToRequest.toArray(new String[0]));
            return false; // Permissions are being requested, not granted yet
        } else {
            return true; // All necessary permissions are already granted
        }
    }


    private void startCamera() {
        Log.d(TAG, "startCamera() called");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                if (binding.cameraPreview != null) { // Ensure ID matches your PreviewView
                    preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
                } else {
                    Log.e(TAG, "PreviewView (cameraPreview) NOT FOUND in binding. Check captureplant.xml ID.");
                    Toast.makeText(getContext(), "Camera preview cannot be initialized.", Toast.LENGTH_SHORT).show();
                    return;
                }

                imageCapture = new ImageCapture.Builder()
                        .setJpegQuality(90) // Optional: set image quality
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
                Log.d(TAG, "Camera started and bound to lifecycle.");
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "startCamera: Use case binding failed", e);
                Toast.makeText(getContext(), "Error starting camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e) {
                Log.e(TAG, "startCamera: IllegalStateException: " + e.getMessage(), e);
                Toast.makeText(getContext(), "Could not start camera. " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        Log.d(TAG, "takePhoto() called. API Level: " + Build.VERSION.SDK_INT);
        if (imageCapture == null) {
            Log.e(TAG, "takePhoto: ImageCapture use case is null. Camera may not have started correctly.");
            Toast.makeText(getContext(), "Camera not ready or error during startup.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a name for the image file. Ensure .jpg extension.
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        Uri outputCollectionUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (API 29) and above
            // For API 29+, specify RELATIVE_PATH. MediaStore.Images.Media.EXTERNAL_CONTENT_URI is a suitable collection.
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name)); // e.g., Pictures/MyAwesomeApp
            outputCollectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Log.d(TAG, "takePhoto (API " + Build.VERSION.SDK_INT + "): Using RELATIVE_PATH: " + contentValues.getAsString(MediaStore.Images.Media.RELATIVE_PATH));
        } else { // Android 9 (API 28) and below
            // For older APIs, saving to EXTERNAL_CONTENT_URI requires WRITE_EXTERNAL_STORAGE.
            // The system will typically place it in the root of the "Pictures" directory.
            // RELATIVE_PATH is not used or recognized here.
            outputCollectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Log.d(TAG, "takePhoto (API " + Build.VERSION.SDK_INT + "): Using EXTERNAL_CONTENT_URI. WRITE_EXTERNAL_STORAGE should be granted.");
        }

        Log.d(TAG, "takePhoto: ContentValues: " + contentValues.toString());
        Log.d(TAG, "takePhoto: Output Collection URI: " + outputCollectionUri.toString());

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        requireContext().getContentResolver(),
                        outputCollectionUri,
                        contentValues
                ).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri != null) {
                            String msg = "Photo capture succeeded: " + savedUri.toString();
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, msg);

                            Bundle args = new Bundle();
                            // Ensure UploadFragment.ARG_IMAGE_URI is the correct key
                            args.putString(UploadFragment.ARG_IMAGE_URI, savedUri.toString());

                            try {
                                if (navController != null) {
                                    // Ensure this action ID is correct in your navigation graph
                                    navController.navigate(R.id.navigation_upload_plant, args);
                                } else {
                                    Log.e(TAG, "onImageSaved: NavController is null.");
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "onImageSaved: Navigation failed. Check action ID: " + R.id.navigation_upload_plant, e);
                                Toast.makeText(getContext(), "Error navigating.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // This specific case is unlikely if the primary error is "failed to write to mediastore URI: null",
                            // as that error implies the insert operation to get a URI failed in the first place.
                            Log.e(TAG, "onImageSaved: output Uri is NULL, though onImageSaved was called. This is unexpected.");
                            Toast.makeText(getContext(), "Photo saved, but URI is missing.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "takePhoto: Photo capture FAILED: " + exception.getMessage(), exception);
                        Toast.makeText(getContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        binding = null;
        Log.d(TAG, "onDestroyView: cameraExecutor shut down, binding set to null.");
    }
}
