// Specifies the package where this UploadFragment class resides.
package com.example.myapplication.ui.myplants;

// Android framework and utility imports.
import android.net.Uri; // For handling URIs, specifically for the image.
import android.os.Bundle; // For passing data between components (e.g., fragments).
import android.os.Handler; // For scheduling messages and runnables to be executed at some point in the future.
import android.os.Looper; // For managing the message loop of a thread.
import android.util.Log; // For logging messages for debugging.
import android.view.LayoutInflater; // For instantiating layout XML files into their corresponding View objects.
import android.view.View; // Base class for widgets, which are used to create interactive UI components.
import android.view.ViewGroup; // Base class for layouts, which are containers that hold other Views or ViewGroups.
// import android.widget.Button; // Commented out: No longer needed as ViewBinding provides direct access to views.
import android.widget.Toast; // For displaying short messages (toasts) to the user.

// AndroidX (Jetpack) library imports for modern Android development.
import androidx.annotation.NonNull; // Annotation indicating a parameter, field, or method return value can never be null.
import androidx.annotation.Nullable; // Annotation indicating a parameter, field, or method return value can be null.
import androidx.fragment.app.Fragment; // Base class for managing a piece of an application's UI or behavior.
import androidx.navigation.NavController; // For managing app navigation within a NavHost.
import androidx.navigation.Navigation; // Utility class for finding a NavController.

// Application-specific R class for accessing resources.
// Crucially, this must be the R class from the application's package, not android.R.
import com.example.myapplication.R;
// View Binding class generated from the uploadplant.xml layout file.
import com.example.myapplication.databinding.UploadplantBinding;
// Note: If UploadplantsuccessBinding (for an included layout) were in a different package,
// its specific import might be needed, but it's usually generated in the same module.

/**
 * UploadFragment handles the UI and logic for uploading a new plant.
 * It receives an image URI (presumably from a capture screen), allows the user to input
 * details like scientific name, location, and introduction, and then simulates an upload process.
 * After a simulated upload, it displays a success overlay and navigates to a preview screen.
 */
public class UploadFragment extends Fragment {

    // TAG for logging, helps in identifying messages from this fragment.
    private static final String TAG = "UploadFragment_Details";
    // View Binding instance for the uploadplant.xml layout.
    private UploadplantBinding binding;
    // String to store the URI of the image received from the CaptureFragment.
    private String receivedImageUriString;
    // NavController instance for handling navigation actions.
    private NavController navController;

    // Public constant key used by CaptureFragment (or other fragments) to pass the image URI
    // as an argument to this fragment.
    public static final String ARG_IMAGE_URI = "imageUri";

    /**
     * Required empty public constructor for Fragment instantiation by the Android framework.
     */
    public UploadFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     * This is where initial data (arguments) passed to the fragment is processed.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if any arguments were passed to this fragment.
        if (getArguments() != null) {
            // Retrieve the image URI string from the arguments bundle.
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

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is where the layout is inflated using View Binding.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding.
        binding = UploadplantBinding.inflate(inflater, container, false);
        // Return the root view of the inflated layout.
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This is where view initializations, such as setting up listeners, should occur.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize the NavController for this view.
        navController = Navigation.findNavController(view);

        // Check if an image URI was received and is valid.
        if (receivedImageUriString != null && !receivedImageUriString.isEmpty()) {
            // Parse the string URI to an Android Uri object.
            Uri imageUri = Uri.parse(receivedImageUriString);
            // Set the image URI to the ImageView in the layout (assumes an ID 'plantImageViewPreview').
            binding.plantImageViewPreview.setImageURI(imageUri);
            Log.d(TAG, "Image set to ImageView.");
        } else {
            // If no valid image URI, show an error message and log it.
            Toast.makeText(getContext(), "Error: No image to display.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "receivedImageUriString is null or empty, cannot display image.");
        }

        // Set up the click listener for the back button.
        binding.backButtonUpload.setOnClickListener(v -> {
            // If NavController is available, pop the back stack to navigate to the previous screen.
            if (navController != null) {
                navController.popBackStack();
            }
        });

        // Set up the click listener for the upload button.
        binding.uploadButton.setOnClickListener(v -> {
            // Get text from input fields and trim whitespace.
            String scientificName = binding.editTextScientificName.getText().toString().trim();
            String location = binding.editTextLocation.getText().toString().trim();
            String introduction = binding.editTextIntroduction.getText().toString().trim();
            String searchTag = binding.editTextSearchTags.getText().toString().trim();

            // Validate that an image has been provided.
            if (receivedImageUriString == null || receivedImageUriString.isEmpty()) {
                Toast.makeText(getContext(), "No image has been captured to upload.", Toast.LENGTH_SHORT).show();
                return; // Stop further processing.
            }
            // Validate that the scientific name is not empty.
            if (scientificName.isEmpty()) {
                binding.editTextScientificName.setError("Scientific Name is required");
                binding.editTextScientificName.requestFocus(); // Focus on the field.
                return; // Stop further processing.
            }
            // Validate that the location is not empty.
            if (location.isEmpty()) {
                binding.editTextLocation.setError("Location is required");
                binding.editTextLocation.requestFocus();
                return; // Stop further processing.
            }
            // Validate that the introduction is not empty.
            if (introduction.isEmpty()) {
                binding.editTextIntroduction.setError("Introduction is required");
                binding.editTextIntroduction.requestFocus();
                return; // Stop further processing.
            }

            // Log the (simulated) upload request details.
            Log.i(TAG, "UPLOAD REQUESTED (Simulated):");
            Log.i(TAG, "Image URI: " + receivedImageUriString);
            Log.i(TAG, "Scientific Name: " + scientificName);
            Log.i(TAG, "Location: " + location);
            Log.i(TAG, "Introduction: " + introduction);
            Log.i(TAG, "Search Tag: " + searchTag);

            // Show a toast message indicating the start of the simulated upload.
            Toast.makeText(getContext(), "Simulating upload for '" + scientificName + "'...", Toast.LENGTH_LONG).show();

            // Simulate a network delay for the upload process using a Handler.
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // After the delay, show the success overlay.
                showSuccessOverlay();
            }, 1500); // 1.5 second delay.
        });
    }

    /**
     * Displays an overlay indicating that the (simulated) upload was successful.
     * The overlay is assumed to be an included layout in `uploadplant.xml`.
     * It also handles navigation to the `UploadCompleteFragment` upon clicking "OK" on the overlay.
     */
    private void showSuccessOverlay() {
        // Ensure context and view binding are not null before proceeding.
        if (getContext() == null || binding == null) {
            Log.e(TAG, "showSuccessOverlay: Context or binding is null.");
            return;
        }

        // --- Handling included layouts with ViewBinding ---
        // This section assumes that:
        // 1. In `uploadplant.xml`, the `<include>` tag for the success overlay has `android:id="@+id/successOverlay"`.
        // 2. The layout file for the success overlay (e.g., `uploadplantsuccess.xml`) uses ViewBinding
        //    (and its root is not a `<merge>` tag).
        // 3. The `UploadplantBinding` class (generated from `uploadplant.xml`) will have a field
        //    named `successOverlay` of the type corresponding to the included layout's binding class
        //    (e.g., `UploadplantsuccessBinding`).

        // Check if the binding for the success overlay is available.
        if (binding.successOverlay != null) {
            // Make the entire included success overlay layout visible.
            binding.successOverlay.getRoot().setVisibility(View.VISIBLE);

            // Set up a click listener for the "OK" button within the success overlay.
            // This assumes the OK button in `uploadplantsuccess.xml` has `android:id="@+id/okButtonSuccess"`.
            binding.successOverlay.okButtonSuccess.setOnClickListener(view -> {
                // Hide the success overlay.
                binding.successOverlay.getRoot().setVisibility(View.GONE);

                // If NavController is available, prepare and navigate to the UploadCompleteFragment.
                if (navController != null) {
                    // Create a bundle to pass data to the next fragment.
                    Bundle args = new Bundle();
                    String currentImageUri = receivedImageUriString; // Use the URI received from CaptureFragment.
                    // Get current values from the input fields.
                    String scientificName = binding.editTextScientificName.getText().toString().trim();
                    String location = binding.editTextLocation.getText().toString().trim();
                    String introduction = binding.editTextIntroduction.getText().toString().trim();
                    String searchTag = binding.editTextSearchTags.getText().toString().trim();

                    // Put the collected data into the arguments bundle.
                    // Keys should match those expected by UploadCompleteFragment.
                    args.putString(UploadCompleteFragment.ARG_IMAGE_URI, currentImageUri);
                    args.putString(UploadCompleteFragment.ARG_SCIENTIFIC_NAME, scientificName);
                    args.putString(UploadCompleteFragment.ARG_LOCATION, location);
                    args.putString(UploadCompleteFragment.ARG_INTRODUCTION, introduction);
                    args.putString(UploadCompleteFragment.ARG_SEARCH_TAG, searchTag);

                    // Log the data being sent to UploadCompleteFragment for debugging.
                    Log.d("UploadFragment_SendData", "Action: Navigating to UploadCompleteFragment");
                    Log.d("UploadFragment_SendData", "Sending Image URI: " + currentImageUri);
                    Log.d("UploadFragment_SendData", "Sending Scientific Name: " + scientificName);
                    Log.d("UploadFragment_SendData", "Sending Location: " + location);
                    Log.d("UploadFragment_SendData", "Sending Introduction: " + introduction);
                    Log.d("UploadFragment_SendData", "Sending Search Tag: " + searchTag);

                    try {
                        // Navigate to the UploadCompleteFragment using the defined action ID
                        // from the navigation graph (e.g., mobile_navigation.xml) and pass the arguments.
                        // Ensure R.id.navigation_upload_complete_preview is the correct action/destination ID.
                        navController.navigate(R.id.navigation_upload_complete_preview, args);
                    } catch (IllegalArgumentException e) {
                        // Log an error and show a toast if navigation fails (e.g., wrong action ID).
                        Log.e(TAG, "Navigation to UploadCompleteFragment failed. Check action ID ("
                                + "R.id.action_uploadFragment_to_uploadCompleteFragment" // Example old/incorrect ID
                                + ") and arguments.", e);
                        Toast.makeText(getContext(), "Error navigating to preview.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "NavController is null, cannot navigate after success overlay.");
                }
            });
        } else {
            // Log an error if the success overlay binding is null, indicating a potential setup issue.
            Log.e(TAG, "binding.successOverlay is null. Check <include> ID in uploadplant.xml and ViewBinding setup.");
        }
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has been detached from the fragment.
     * The next time the fragment needs to be displayed, a new view will be created.
     * This is where you should clean up resources associated with the view.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify the binding object to prevent memory leaks.
        // This is crucial because the fragment instance might outlive its view.
        binding = null;
        Log.d(TAG, "onDestroyView called, binding set to null.");
    }
}
