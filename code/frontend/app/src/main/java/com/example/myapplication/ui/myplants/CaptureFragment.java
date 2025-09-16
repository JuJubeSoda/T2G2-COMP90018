// Specifies the package where this CaptureFragment class resides.
package com.example.myapplication.ui.myplants;

// Android framework and utility imports.
import android.Manifest; // For accessing manifest permissions.
import android.content.ContentValues; // For storing sets of values that content resolver can process.
import android.content.pm.PackageManager; // For checking permissions.
import android.net.Uri; // For handling URIs.
import android.os.Build; // For accessing build version information.
import android.os.Bundle; // For passing data between components and saving instance state.
import android.provider.MediaStore; // For interacting with the media store.
import android.util.Log; // For logging messages for debugging.
import android.view.LayoutInflater; // For instantiating layout XML files into View objects.
import android.view.View; // Base class for widgets.
import android.view.ViewGroup; // Base class for layouts.
import android.widget.Toast; // For displaying short messages to the user.

// AndroidX (Jetpack) Activity Result APIs for handling permission requests and other activity results.
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
// AndroidX annotations.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
// AndroidX CameraX core libraries for camera functionalities.
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
// AndroidX core utilities.
import androidx.core.content.ContextCompat;
// AndroidX Fragment library.
import androidx.fragment.app.Fragment;
// AndroidX Lifecycle library.
import androidx.lifecycle.LifecycleOwner;
// AndroidX Navigation library for navigating between fragments.
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// Application-specific R class for accessing resources.
import com.example.myapplication.R;
// View Binding class generated from the captureplant.xml layout file.
import com.example.myapplication.databinding.CaptureplantBinding;
// Guava utility for ListenableFuture, used by CameraX.
import com.google.common.util.concurrent.ListenableFuture;

// Java utility classes.
import java.text.SimpleDateFormat; // For formatting dates.
import java.util.ArrayList; // Resizable-array implementation of the List interface.
import java.util.List; // Ordered collection.
import java.util.Locale; // Represents a specific geographical, political, or cultural region.
import java.util.Map; // Interface for key-value mappings.
import java.util.concurrent.ExecutionException; // Exception thrown when attempting to retrieve the result of a task that aborted.
import java.util.concurrent.ExecutorService; // Framework for asynchronous task execution.
import java.util.concurrent.Executors; // Factory methods for ExecutorService.

/**
 * CaptureFragment is responsible for handling camera operations to capture an image.
 * It uses CameraX for camera preview and image capture. It requests necessary permissions
 * (camera and storage for older Android versions), starts the camera, allows the user to
 * take a photo, saves the photo to the MediaStore, and then navigates to the
 * UploadFragment, passing the URI of the saved image.
 */
public class CaptureFragment extends Fragment {

    // TAG for logging, helps in identifying messages from this fragment.
    private static final String TAG = "CaptureFragment";
    // Format for generating unique filenames for captured images based on the current timestamp.
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    // View Binding instance for the captureplant.xml layout.
    private CaptureplantBinding binding;

    // Executor service to run camera operations on a background thread.
    private ExecutorService cameraExecutor;
    // CameraX Usecase for capturing images.
    private ImageCapture imageCapture;
    // NavController instance for handling navigation actions from this fragment.
    private NavController navController;

    /**
     * ActivityResultLauncher for requesting multiple permissions (Camera and potentially Write External Storage).
     * This launcher handles the result of the permission request.
     */
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                // Iterate through the permission results.
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    Log.d(TAG, "Permission: " + entry.getKey() + " Granted: " + entry.getValue());
                    if (!entry.getValue()) {
                        allGranted = false; // If any permission is denied, set allGranted to false.
                    }
                }

                if (allGranted) {
                    // If all necessary permissions are granted by the user.
                    Log.d(TAG, "All necessary permissions GRANTED by user. Starting camera.");
                    startCamera(); // Proceed to start the camera.
                } else {
                    // If one or more permissions were denied by the user.
                    Log.e(TAG, "One or more permissions DENIED by user.");
                    Toast.makeText(getContext(), "Camera and/or Storage permissions are required to use this feature.", Toast.LENGTH_LONG).show();
                    // Optional: Implement logic to handle denied permissions, like navigating back or disabling features.
                    // For example: if (navController != null) navController.popBackStack();
                }
            });

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout using ViewBinding.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding.
        binding = CaptureplantBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: Layout inflated.");
        // Return the root view of the inflated layout.
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned,
     * but before any saved state has been restored in to the view.
     * Initializes NavController, camera executor, checks for permissions, and sets up the capture button.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize the NavController for this view.
        navController = Navigation.findNavController(view);
        // Create a single-thread executor for camera operations.
        cameraExecutor = Executors.newSingleThreadExecutor();
        Log.d(TAG, "onViewCreated: NavController and CameraExecutor initialized.");

        // Check if permissions are already granted. If so, start the camera.
        // If not, checkAndRequestPermissions() will launch the permission request.
        if (checkAndRequestPermissions()) {
            Log.d(TAG, "Permissions already granted or not needed at this API level for startup. Starting camera.");
            startCamera();
        }

        // Set up the click listener for the image capture button.
        // Ensure that 'imageCaptureButton' is the correct ID of your capture button in captureplant.xml.
        if (binding.imageCaptureButton != null) {
            binding.imageCaptureButton.setOnClickListener(v -> takePhoto());
            Log.d(TAG, "Capture button (ID: imageCaptureButton) listener set up.");
        } else {
            // Log an error and show a toast if the capture button is not found in the binding.
            Log.e(TAG, "Capture button (e.g., imageCaptureButton) NOT FOUND in binding. Check captureplant.xml ID.");
            Toast.makeText(getContext(), "Capture button not found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if necessary permissions (Camera and Write External Storage for older APIs) are granted.
     * If not, it requests them using {@link #requestPermissionsLauncher}.
     *
     * @return True if all necessary permissions are already granted, false otherwise (permissions are being requested).
     */
    private boolean checkAndRequestPermissions() {
        Log.d(TAG, "checkAndRequestPermissions called.");
        List<String> permissionsToCheck = new ArrayList<>();

        // Always add CAMERA permission to the list to check.
        permissionsToCheck.add(Manifest.permission.CAMERA);

        // For Android 9 (API 28) and below, WRITE_EXTERNAL_STORAGE is required if saving
        // images to MediaStore.Images.Media.EXTERNAL_CONTENT_URI directly without RELATIVE_PATH.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionsToCheck.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission will be checked for API <= P.");
        }

        // Filter out permissions that are already granted to find those that need to be requested.
        List<String> permissionsToRequest = new ArrayList<>();
        for (String perm : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(perm);
            }
        }

        // If there are any permissions in permissionsToRequest, launch the request.
        if (!permissionsToRequest.isEmpty()) {
            Log.d(TAG, "Requesting permissions: " + permissionsToRequest);
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
            return false; // Permissions are being requested, not all granted yet.
        } else {
            Log.d(TAG, "All necessary permissions are already granted.");
            return true; // All necessary permissions are already granted.
        }
    }


    /**
     * Initializes and starts the camera preview and image capture use cases.
     * It gets an instance of ProcessCameraProvider, configures Preview and ImageCapture,
     * selects the back camera, and binds these use cases to the fragment's lifecycle.
     */
    private void startCamera() {
        Log.d(TAG, "startCamera() called");
        // Get an instance of ProcessCameraProvider. This is an asynchronous operation.
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                // CameraProvider is now ready.
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Build the Preview use case.
                Preview preview = new Preview.Builder().build();

                // Set the SurfaceProvider for the PreviewView from the layout.
                // Ensure 'cameraPreview' is the correct ID of your PreviewView in captureplant.xml.
                if (binding.cameraPreview != null) {
                    preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
                } else {
                    Log.e(TAG, "PreviewView (cameraPreview) NOT FOUND in binding. Cannot start camera preview.");
                    Toast.makeText(getContext(), "Camera preview cannot be initialized.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Build the ImageCapture use case.
                imageCapture = new ImageCapture.Builder()
                        .setJpegQuality(90) // Optional: Set JPEG quality (0-100).
                        .build();

                // Select the default back camera.
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind all use cases before rebinding to ensure a clean state.
                cameraProvider.unbindAll();

                // Bind the use cases (preview and image capture) to the camera and the fragment's lifecycle.
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
                Log.d(TAG, "Camera started successfully and bound to lifecycle.");

            } catch (ExecutionException | InterruptedException e) {
                // Handle exceptions during camera provider retrieval or use case binding.
                Log.e(TAG, "startCamera: Use case binding failed", e);
                Toast.makeText(getContext(), "Error starting camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e) {
                // Handle cases where binding to lifecycle fails due to state issues.
                Log.e(TAG, "startCamera: IllegalStateException: " + e.getMessage(), e);
                Toast.makeText(getContext(), "Could not start camera. " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext())); // Run the listener on the main thread.
    }

    /**
     * Captures a photo using the ImageCapture use case and saves it to the MediaStore.
     * It creates a unique filename, sets up ContentValues for MediaStore,
     * and handles the image saved callback or any errors during capture.
     * After successful capture, it navigates to UploadFragment with the image URI.
     */
    private void takePhoto() {
        Log.d(TAG, "takePhoto() called. API Level: " + Build.VERSION.SDK_INT);
        // Ensure ImageCapture use case is initialized.
        if (imageCapture == null) {
            Log.e(TAG, "takePhoto: ImageCapture use case is null. Camera may not have started correctly.");
            Toast.makeText(getContext(), "Camera not ready or error during startup.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a filename for the image using the defined format and current time. Ensure .jpg extension.
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg";

        // Prepare ContentValues for saving the image to MediaStore.
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        Uri outputCollectionUri; // The collection URI where the image will be saved.

        // Handle storage differently based on Android API level due to Scoped Storage changes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (API 29) and above.
            // For API 29+, use MediaStore.Images.Media.RELATIVE_PATH to specify a subdirectory
            // within the standard "Pictures" directory.
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name));
            outputCollectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // Standard collection for images.
            Log.d(TAG, "takePhoto (API " + Build.VERSION.SDK_INT + "): Using RELATIVE_PATH: " +
                    contentValues.getAsString(MediaStore.Images.Media.RELATIVE_PATH));
        } else { // Android 9 (API 28) and below.
            // For older APIs, save directly to MediaStore.Images.Media.EXTERNAL_CONTENT_URI.
            // This requires WRITE_EXTERNAL_STORAGE permission.
            // The system typically places it in the root of the "Pictures" directory.
            outputCollectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Log.d(TAG, "takePhoto (API " + Build.VERSION.SDK_INT + "): Using EXTERNAL_CONTENT_URI. " +
                    "WRITE_EXTERNAL_STORAGE should be granted.");
        }

        Log.d(TAG, "takePhoto: ContentValues: " + contentValues.toString());
        Log.d(TAG, "takePhoto: Output Collection URI: " + outputCollectionUri.toString());

        // Create OutputFileOptions to specify where and how to save the image.
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        requireContext().getContentResolver(), // ContentResolver to interact with MediaStore.
                        outputCollectionUri,                  // The collection URI.
                        contentValues                         // Metadata for the new image.
                ).build();

        // Take the picture. This is an asynchronous operation.
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()), // Executor for the callback.
                new ImageCapture.OnImageSavedCallback() { // Callback for when the image is saved or an error occurs.
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri(); // Get the URI of the saved image.
                        if (savedUri != null) {
                            String msg = "Photo capture succeeded: " + savedUri.toString();
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, msg);

                            // Prepare arguments to pass the image URI to the UploadFragment.
                            Bundle args = new Bundle();
                            // Ensure UploadFragment.ARG_IMAGE_URI is the correct key defined in UploadFragment.
                            args.putString(UploadFragment.ARG_IMAGE_URI, savedUri.toString());

                            try {
                                if (navController != null) {
                                    // Navigate to the UploadFragment.
                                    // Ensure R.id.navigation_upload_plant is the correct action ID in your navigation graph
                                    // that leads from CaptureFragment to UploadFragment.
                                    navController.navigate(R.id.navigation_upload_plant, args);
                                    Log.d(TAG, "Navigating to UploadFragment with URI: " + savedUri.toString());
                                } else {
                                    Log.e(TAG, "onImageSaved: NavController is null. Cannot navigate.");
                                }
                            } catch (IllegalArgumentException e) {
                                // Handle navigation errors (e.g., incorrect action ID).
                                Log.e(TAG, "onImageSaved: Navigation to UploadFragment failed. " +
                                        "Check action ID (R.id.navigation_upload_plant): " + R.id.navigation_upload_plant, e);
                                Toast.makeText(getContext(), "Error navigating after photo capture.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // This case is unlikely if the primary error is a failure to write to MediaStore,
                            // as that often means the initial URI for insertion wasn't obtained.
                            // However, it's a defensive check.
                            Log.e(TAG, "onImageSaved: output Uri is NULL, though onImageSaved was called. This is unexpected.");
                            Toast.makeText(getContext(), "Photo saved, but the URI is missing.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Handle errors during image capture.
                        Log.e(TAG, "takePhoto: Photo capture FAILED: " + exception.getMessage(), exception);
                        Toast.makeText(getContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has been detached from the fragment.
     * Shuts down the cameraExecutor and nullifies the binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Shut down the camera executor to release its resources.
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        // Nullify the ViewBinding instance to release its reference to the view hierarchy.
        binding = null;
        Log.d(TAG, "onDestroyView: cameraExecutor shut down, binding set to null.");
    }
}
