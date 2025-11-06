package com.example.myapplication.ui.myplants.myGarden;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantdetailBinding;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.User;
import com.example.myapplication.ui.myplants.share.Plant;
// Removed interactive dependencies; display-only fragment

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PlantDetailFragment - Displays detailed information for user-collected plants.
 * 
 * Purpose:
 * - Show comprehensive details for plants in My Garden
 * - Display plant image, name, scientific name
 * - Show location, discovery date, discovered by
 * - Display tags and description
 * - Handle date parsing from backend
 * 
 * User Flow:
 * 1. User clicks plant tile in MyGardenFragment
 * 2. Fragment displays full plant details
 * 3. User can navigate back via back button
 * 
 * Key Features:
 * - Base64 image decoding and display
 * - Robust date parsing (multiple ISO 8601 formats)
 * - Location coordinate formatting
 * - Tag list display
 * - Progress indicator during loading
 * - Error handling with fallback text
 * 
 * Data Source:
 * - Plant object passed from MyGardenFragment
 * - Data from user uploads via UploadFragment
 * - Stored in backend database
 * 
 * Navigation:
 * - From: MyGardenFragment (plant tile click)
 * - Back: Navigation up arrow
 */
public class PlantDetailFragment extends Fragment {

    public static final String ARG_PLANT = "plant_argument";
    private static final String TAG = "PlantDetailFragment";

    /** View binding for plantdetail.xml layout */
    private PlantdetailBinding binding;
    
    /** Plant data to display */
    private Plant plant;
    // Display-only: no like/unlike state

    /**
     * Fragment creation lifecycle method.
     * Retrieves Plant object and validates it.
     * Navigates back if Plant is null (error state).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }

        // Handle missing Plant data
        if (plant == null) {
            Log.e(TAG, "CRITICAL: Plant object is null. Cannot display details.");
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantdetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI after view is created.
     * 
     * Process:
     * 1. Shows progress bar
     * 2. Populates UI if Plant exists
     * 3. Hides progress bar
     * 4. Sets up back button navigation
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show loading indicator
        binding.progressBar.setVisibility(View.VISIBLE);

        // No interactive manager/state

        if (plant != null) {
            populateUi();
            binding.progressBar.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
        }

        // Setup back navigation
        binding.backButtonDetail.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // No like/unlike or navigation buttons in display-only fragment
    }

    /**
     * Populates all UI fields with plant data.
     * 
     * Displays:
     * - Scientific name
     * - Description/introduction
     * - Location coordinates (latitude, longitude)
     * - Tags (comma-separated list)
     * - Discovered by (username)
     * - Discovery date (formatted from ISO 8601)
     * - Plant image (Base64 decoded)
     * 
     * Error Handling:
     * - Null checks for all fields
     * - Fallback text for missing data
     * - Multiple date format parsing attempts
     * - Image loading with placeholder on error
     * - Try-catch blocks for robust error handling
     */
    private void populateUi() {
        // Populate text fields with null safety
        try {
            binding.textViewScientificName.setText(plant.getScientificName() != null ? plant.getScientificName() : "Not available");
            binding.textViewIntroduction.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");

            // Format location coordinates
            String locationString = "Location not available";
            if (plant.getLatitude() != null && plant.getLongitude() != null) {
                locationString = String.format(Locale.getDefault(), "(%.4f, %.4f)", plant.getLatitude(), plant.getLongitude());
            }
            binding.textViewLocation.setText(locationString);

            // Format tags as comma-separated list
            String tags = "No tags";
            List<String> plantTags = plant.getTags();
            if (plantTags != null && !plantTags.isEmpty()) {
                tags = plantTags.stream().collect(Collectors.joining(", "));
            }

            // Set initial placeholder for "Discovered by" before API call
            binding.textViewDiscoveredBy.setText("User #" + plant.getUserId());
            
            // Fetch username from userId by calling getAllUsers API (async)
            fetchAndDisplayUsername(plant.getUserId());
        } catch (Exception e) {
            Log.e(TAG, "Error populating UI with plant data", e);
            Toast.makeText(getContext(), "Error displaying plant information", Toast.LENGTH_SHORT).show();
        }

        // Display discovery date as-is from backend
        try {
            String createdAt = plant.getCreatedAt();
            
            if (createdAt == null || createdAt.isEmpty()) {
                binding.textViewDiscoveredOn.setText("Date not available");
            } else {
                binding.textViewDiscoveredOn.setText(createdAt);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle discovery date.", e);
            binding.textViewDiscoveredOn.setText("Date not available");
        }

        // Load plant image with Base64 decoding
        try {
            String base64Image = plant.getImageUrl();
            if (base64Image == null || base64Image.isEmpty()) {
                binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
            } else {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Glide.with(this)
                        .load(imageBytes)
                        .placeholder(R.drawable.plantbulb_foreground)
                        .error(R.drawable.plantbulb_foreground)
                        .into(binding.imageViewPlantPreview);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode or load image for plant: " + (plant.getName() != null ? plant.getName() : "Unknown"), e);
            binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
        }
    }

    /**
     * Fetches all users from backend and finds username by userId.
     * 
     * WARNING: This endpoint may return large responses with avatar data.
     * If memory issues occur, consider:
     * 1. Backend should provide GET /user/{userId} endpoint
     * 2. Backend should exclude avatarData from list responses
     * 3. Use caching to avoid repeated calls
     * 
     * Process:
     * 1. Call GET /user to get all users
     * 2. Find user with matching userId
     * 3. Display username in "Discovered by" field
     * 4. Fallback to "User #[userId]" if API fails or user not found
     * 
     * @param userId The userId to look up
     */
    private void fetchAndDisplayUsername(int userId) {
        Log.d(TAG, "fetchAndDisplayUsername called with userId: " + userId);
        
        // Verify binding is not null
        if (binding == null || binding.textViewDiscoveredBy == null) {
            Log.e(TAG, "Binding or textViewDiscoveredBy is null!");
            return;
        }
        
        // Set loading state
        binding.textViewDiscoveredBy.setText("Loading...");
        Log.d(TAG, "Set text to 'Loading...'");
        
        try {
            ApiService apiService = ApiClient.create(getContext());
            Call<ApiResponse<List<User>>> call = apiService.getAllUsers();
            
            call.enqueue(new Callback<ApiResponse<List<User>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                    if (!isAdded()) return; // Fragment detached
                    
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            List<User> users = response.body().getData();
                            if (users != null) {
                                // Find user with matching userId
                                String username = null;
                                for (User user : users) {
                                    if (user.getUserId() != null && user.getUserId() == userId) {
                                        username = user.getUsername();
                                        break;
                                    }
                                }
                                
                                // Display username or fallback
                                if (username != null && !username.isEmpty()) {
                                    binding.textViewDiscoveredBy.setText(username);
                                    Log.d(TAG, "Found username: " + username + " for userId: " + userId);
                                } else {
                                    binding.textViewDiscoveredBy.setText("User #" + userId);
                                    Log.w(TAG, "Username not found for userId: " + userId);
                                }
                            } else {
                                binding.textViewDiscoveredBy.setText("User #" + userId);
                                Log.w(TAG, "User list is null");
                            }
                        } else {
                            binding.textViewDiscoveredBy.setText("User #" + userId);
                            Log.e(TAG, "Failed to fetch users: " + response.message());
                        }
                    } catch (OutOfMemoryError e) {
                        // Handle OOM from large response
                        binding.textViewDiscoveredBy.setText("User #" + userId);
                        Log.e(TAG, "OutOfMemoryError while parsing user list - response too large", e);
                    } catch (Exception e) {
                        binding.textViewDiscoveredBy.setText("User #" + userId);
                        Log.e(TAG, "Error parsing user response", e);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                    if (!isAdded()) return; // Fragment detached
                    
                    binding.textViewDiscoveredBy.setText("User #" + userId);
                    
                    if (t instanceof OutOfMemoryError) {
                        Log.e(TAG, "OutOfMemoryError fetching users - response too large. Backend should exclude avatarData.", t);
                    } else {
                        Log.e(TAG, "Network error fetching users", t);
                    }
                }
            });
        } catch (OutOfMemoryError e) {
            // Catch OOM even before API call
            binding.textViewDiscoveredBy.setText("User #" + userId);
            Log.e(TAG, "OutOfMemoryError before API call", e);
        } catch (Exception e) {
            binding.textViewDiscoveredBy.setText("User #" + userId);
            Log.e(TAG, "Error setting up API call", e);
        }
    }

    // No interactive methods

    /** Cleans up view binding to prevent memory leaks. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
