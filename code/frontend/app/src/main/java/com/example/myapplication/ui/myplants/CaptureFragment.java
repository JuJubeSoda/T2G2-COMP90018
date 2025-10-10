// /.../app/src/main/java/com/example/myapplication/ui/myplants/CaptureFragment.java

package com.example.myapplication.ui.myplants;

// Android framework and utility imports...
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

// AndroidX imports...
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

// Application-specific and library imports...
import com.example.myapplication.R;
import com.example.myapplication.databinding.CaptureplantBinding;
import com.google.common.util.concurrent.ListenableFuture;

// Java utility imports...
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

    // --- MODIFICATION 1: Fields to hold the data passed from previous fragments ---
    private String scientificNameToPass;
    private boolean isFavouriteFlowToPass = false;

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
     * --- MODIFICATION 2: Receive arguments when the fragment is created. ---
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve the scientific name
            scientificNameToPass = getArguments().getString("scientificName");
            // Retrieve the favourite status, default to false if not provided
            isFavouriteFlowToPass = getArguments().getBoolean("isFavouriteFlow", false);

            Log.d(TAG, "Received scientific name to pass along: " + scientificNameToPass);
            Log.d(TAG, "Received isFavouriteFlow to pass along: " + isFavouriteFlowToPass);
        }
    }

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
            startCamera();
        }

        binding.backButton.setOnClickListener(v -> navController.popBackStack());
        binding.imageCaptureButton.setOnClickListener(v -> takePhoto());
    }

    private boolean checkAndRequestPermissions() {
        // (This method's implementation is correct and requires no changes)
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        if (!permissionsToRequest.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
            return false;
        }
        return true;
    }

    private void startCamera() {
        // (This method's implementation is correct and requires no changes)
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setJpegQuality(90).build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "startCamera: Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "takePhoto: ImageCapture is null.");
            return;
        }

        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name));
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                requireContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri == null) {
                            Log.e(TAG, "onImageSaved: Saved URI is null.");
                            return;
                        }

                        Log.d(TAG, "Photo capture succeeded: " + savedUri);

                        // --- MODIFICATION 3: Add all data to the bundle for UploadFragment ---
                        Bundle args = new Bundle();
                        args.putString(UploadFragment.ARG_IMAGE_URI, savedUri.toString());

                        // Add the scientific name if it was passed
                        if (scientificNameToPass != null) {
                            args.putString(UploadFragment.ARG_SCIENTIFIC_NAME, scientificNameToPass);
                        }
                        // Add the isFavouriteFlow boolean
                        args.putBoolean(UploadFragment.ARG_IS_FAVOURITE_FLOW, isFavouriteFlowToPass);

                        try {
                            // Use a valid action ID from your navigation graph that goes from Capture to Upload
                            // Assuming this action is named `action_captureFragment_to_uploadFragment`
                            navController.navigate(R.id.navigation_upload_plant, args);
                            Log.d(TAG, "Navigating to UploadFragment with URI, name, and favorite status.");
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Navigation failed. Check action ID.", e);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        binding = null;
    }
}
