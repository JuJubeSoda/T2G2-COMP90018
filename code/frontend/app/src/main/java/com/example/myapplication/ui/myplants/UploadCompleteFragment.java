// Specifies the package where this UploadCompleteFragment class resides.
package com.example.myapplication.ui.myplants;

// Android framework and utility imports.
import android.net.Uri; // For handling URIs, specifically for the image.
import android.os.Bundle; // For passing data between components (e.g., fragments).
import android.util.Log; // For logging messages for debugging.
import android.view.LayoutInflater; // For instantiating layout XML files into their corresponding View objects.
import android.view.View; // Base class for widgets, which are used to create interactive UI components.
import android.view.ViewGroup; // Base class for layouts, which are containers that hold other Views or ViewGroups.
import android.widget.Toast; // For displaying short messages (toasts) to the user.

// AndroidX (Jetpack) library imports for modern Android development.
import androidx.annotation.NonNull; // Annotation indicating a parameter, field, or method return value can never be null.
import androidx.annotation.Nullable; // Annotation indicating a parameter, field, or method return value can be null.
import androidx.fragment.app.Fragment; // Base class for managing a piece of an application's UI or behavior.
import androidx.navigation.NavController; // For managing app navigation within a NavHost.
import androidx.navigation.Navigation; // Utility class for finding a NavController.
import androidx.navigation.fragment.NavHostFragment; // Specifically for finding NavController from a Fragment.

// Application-specific R class for accessing resources.
import com.example.myapplication.R;
// View Binding class generated from the uploadcompletepreview.xml layout file.
// The name of this binding class (UploadcompletepreviewBinding) should exactly match the XML file name
// (uploadcompletepreview.xml) converted to PascalCase with "Binding" appended.
import com.example.myapplication.databinding.UploadcompletepreviewBinding;

// Java utility classes for date formatting.
import java.text.SimpleDateFormat; // For formatting and parsing dates in a specific manner.
import java.util.Date; // Represents a specific instant in time.
import java.util.Locale; // Represents a specific geographical, political, or cultural region.

/**
 * UploadCompleteFragment displays a preview of the plant information that was just "uploaded"
 * (or entered in the previous screen). It receives data such as an image URI, scientific name,
 * location, etc., via fragment arguments and populates the UI elements accordingly.
 * It also provides options to "Upload More" (navigate back to the start of the upload flow)
 * or "Gather More Info" (placeholder functionality).
 */
public class UploadCompleteFragment extends Fragment {

    // TAG for logging, matches the class name for easy identification of log messages.
    private static final String TAG = "UploadCompleteFragment";

    // Public constants defining the keys used to pass arguments to this fragment.
    // These act as a "contract" for any fragment sending data to UploadCompleteFragment.
    public static final String ARG_IMAGE_URI = "imageUri";
    public static final String ARG_SCIENTIFIC_NAME = "scientificName";
    public static final String ARG_LOCATION = "location";
    public static final String ARG_INTRODUCTION = "introduction";
    public static final String ARG_SEARCH_TAG = "searchTag";

    // View Binding instance for the uploadcompletepreview.xml layout.
    // Ensure this binding class name matches the XML file name (e.g., uploadcompletepreview.xml -> UploadcompletepreviewBinding).
    private UploadcompletepreviewBinding binding;
    // NavController instance for handling navigation actions from this fragment.
    private NavController navController;

    // Member variables to store the data received from the arguments.
    private String imageUriString;
    private String scientificName;
    private String location;
    private String introduction;
    private String searchTag;

    /**
     * Called when the fragment is first created.
     * This is where initial data (arguments) passed to the fragment is retrieved and processed.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        // Get the arguments bundle passed to this fragment.
        Bundle arguments = getArguments();
        if (arguments != null) {
            // Retrieve data from the bundle using the defined argument keys.
            imageUriString = arguments.getString(ARG_IMAGE_URI);
            scientificName = arguments.getString(ARG_SCIENTIFIC_NAME);
            location = arguments.getString(ARG_LOCATION);
            introduction = arguments.getString(ARG_INTRODUCTION);
            searchTag = arguments.getString(ARG_SEARCH_TAG);

            // Log the received data for debugging purposes.
            Log.d(TAG, "Received Image URI: " + imageUriString);
            Log.d(TAG, "Received Scientific Name: " + scientificName);
            Log.d(TAG, "Received Location: " + location);
            Log.d(TAG, "Received Introduction: " + introduction);
            Log.d(TAG, "Received Search Tag: " + searchTag);
        } else {
            // Log an error and show a toast if no arguments were received, as data will be missing.
            Log.e(TAG, "No arguments bundle received for " + TAG + ". Data will be missing.");
            Toast.makeText(getContext(), "Error: Could not load plant details.", Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "onCreateView called");
        // Inflate the layout for this fragment using View Binding.
        // Make sure UploadcompletepreviewBinding correctly corresponds to your XML file name (e.g., uploadcompletepreview.xml).
        binding = UploadcompletepreviewBinding.inflate(inflater, container, false);
        // Return the root view of the inflated layout.
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This is where view initializations (like finding NavController, populating views,
     * and setting up listeners) should occur.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        // Attempt to find the NavController associated with this fragment.
        // Using NavHostFragment.findNavController(this) is generally preferred.
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            // Fallback to Navigation.findNavController(getView()) if the primary method fails.
            // This might happen in some specific NavHost configurations.
            Log.e(TAG, "Failed to find NavController via NavHostFragment", e);
            if (getView() != null) {
                navController = Navigation.findNavController(getView());
            }
        }

        // Populate the UI elements with the received data.
        populateViews();
        // Set up click listeners for the buttons on this screen.
        setupButtonListeners();
    }

    /**
     * Populates the views in the layout with the data received in {@link #onCreate(Bundle)}.
     * This includes setting the plant image, scientific name, location, introduction, and tags.
     */
    private void populateViews() {
        // Ensure the binding is not null before trying to access views.
        if (binding == null) {
            Log.e(TAG, "populateViews: Binding is null. Cannot set data.");
            return;
        }

        // Log the data just before it's set to the views for debugging.
        Log.d(TAG, "Populating views with Image URI: " + imageUriString);
        Log.d(TAG, "Populating views with Scientific Name: " + scientificName);
        // Consider adding logs for other fields if debugging issues with them.

        // Set the plant image if a valid URI is available.
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                // Ensure 'imageViewPlantPreview' is the correct ID of the ImageView in your uploadcompletepreview.xml.
                binding.imageViewPlantPreview.setImageURI(Uri.parse(imageUriString));
            } catch (Exception e) {
                // Log any error during image setting and set a placeholder image.
                Log.e(TAG, "Error setting image URI in populateViews: " + imageUriString, e);
                // Ensure 'R.drawable.plantbulb_foreground' is a valid placeholder drawable resource.
                binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
            }
        } else {
            // If the image URI is null or empty, log a warning and set a placeholder.
            Log.w(TAG, "Image URI is null or empty in populateViews, setting placeholder.");
            binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
        }

        // Set the text for various TextViews, using "N/A" if data is null or empty.
        // Ensure the IDs used (e.g., textViewScientificName, textViewLocation) match those in uploadcompletepreview.xml.
        binding.textViewScientificName.setText(scientificName != null && !scientificName.isEmpty() ? scientificName : "N/A");
        binding.textViewLocation.setText(location != null && !location.isEmpty() ? location : "N/A");
        binding.textViewIntroduction.setText(introduction != null && !introduction.isEmpty() ? introduction : "N/A");
        // For search tags, format the text to include "Tags: ".
        // Assumes your XML has a TextView with ID 'textViewSearchTag' for the tag value.
        binding.textViewSearchTag.setText(searchTag != null && !searchTag.isEmpty() ? "Tags: " + searchTag : "Tags: N/A");

        // Set static or dynamically generated text for other fields.
        binding.textViewDiscoveredBy.setText("Discovered by: You"); // Example: assuming the current user discovered it.
        // Get the current date and format it.
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        binding.textViewDate.setText("Date: " + currentDate);
    }

    /**
     * Sets up click listeners for the buttons on this screen, such as
     * "Upload More" and "Gather More Information".
     */
    private void setupButtonListeners() {
        // Ensure binding and NavController are not null before setting listeners.
        if (binding == null || navController == null) {
            Log.e(TAG, "setupButtonListeners: Binding or NavController is null.");
            return;
        }

        // Set click listener for the "Upload More" button.
        // This button navigates the user back to the CaptureFragment to start a new upload flow.
        binding.buttonUploadMore.setOnClickListener(v -> {
            Log.d(TAG, "Upload More button clicked");
            try {
                // To restart the upload flow, navigate to the CaptureFragment.
                // We should also clear the previous upload screens from the back stack.
                // The popUpTo attribute in a navigation action is the best way to handle this cleanly.
                // For a programmatic solution, we can pop the stack up to the screen
                // before the capture flow began (e.g., myGardenFragment) and then navigate.
                navController.navigate(R.id.navigation_upload); // Now, navigate to the camera.

            } catch (Exception e) {
                // Log and show an error if navigation fails.
                Log.e(TAG, "Error navigating to CaptureFragment for 'Upload More'", e);
                Toast.makeText(getContext(), "Error navigating to camera.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the "Gather More Information" button.
        // Currently, this is a placeholder functionality.
        binding.buttonGatherMoreInfo.setOnClickListener(v -> {
            Log.d(TAG, "Gather More Information button clicked");
            Toast.makeText(getContext(), "Functionality to gather more info coming soon!", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has been detached from the fragment.
     * The next time the fragment needs to be displayed, a new view will be created.
     * This is where you should clean up resources associated with the view, particularly the ViewBinding instance.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called, binding set to null.");
        // Nullify the binding object to prevent memory leaks.
        // This is important because the fragment instance might outlive its view.
        binding = null;
    }
}
