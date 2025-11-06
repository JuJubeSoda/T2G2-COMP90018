/**
 * HomeFragment - Main dashboard and landing screen for the application.
 * 
 * Features:
 * 1. Quick Action Cards:
 *    - Upload Plants: Navigate to plant upload flow
 *    - Add to My Garden: Navigate to add plant screen
 *    - AI Chat: Launch AI assistant activity
 * 
 * 2. Nearby Discoveries Carousel:
 *    - Displays public plants from nearby users
 *    - Fetches from /api/plants/nearby endpoint
 *    - Shows distance, images, and descriptions
 *    - Handles empty states with helpful messages
 * 
 * API Integration:
 * - GET /api/plants/nearby: Fetches plants with isPublic=true near user's location
 * - Converts PlantDto to DiscoveryItem for display
 * - Handles Base64 encoded images from backend
 * 
 * Error Handling:
 * - Network failures: Shows placeholder with helpful message
 * - Empty results: Explains how to contribute plants
 * - Permission issues: Guides user to enable location
 * - Detailed logging for debugging
 * 
 * User Experience:
 * - Horizontal scrolling carousel for discoveries
 * - Smooth navigation to other app sections
 * - Toast messages for user feedback
 * - Graceful degradation on errors
 */
package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.ui.home.DiscoveryAdapter;
import com.example.myapplication.databinding.HomeBinding;
import com.example.myapplication.ui.home.DiscoveryItem;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantMapDto;
import com.example.myapplication.map.MapDataManager;
import com.example.myapplication.ui.myplants.share.Plant;
import com.example.myapplication.ui.myplants.myGarden.PlantDetailFragment;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.map.MapLocationManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.location.Location;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    
    // Search radius (5 kilometers, suitable for Home page fixed range)
    private static final int SEARCH_RADIUS_METERS = 5000;
    
    /** View binding for home.xml layout */
    private HomeBinding binding;
    
    /** Adapter for nearby discoveries horizontal RecyclerView */
    private DiscoveryAdapter discoveryAdapter;
    
    /** List of discovery items to display in carousel */
    private List<DiscoveryItem> discoveryItemList;
    
    /** Navigation controller for fragment transitions */
    private NavController navController;
    
    /** Location manager for getting user's GPS location */
    private MapLocationManager locationManager;
    
    /** Current user location (for distance calculation) */
    private Location userLocation;

    /**
     * Inflates the layout using View Binding.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = HomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes UI components after view is created.
     * Sets up navigation, action cards, and nearby discoveries carousel.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize navigation controller for fragment transitions
        navController = Navigation.findNavController(view);

        // Initialize location manager (no GoogleMap needed for HomeFragment)
        locationManager = new MapLocationManager(requireContext(), null);
        locationManager.requestLocationPermission();

        // Setup all UI components
        setupTopCardClickListeners();
        setupNearbyDiscoveriesRecyclerView();
        
        // Request location and load nearby discoveries
        requestLocationAndLoadData();
    }

    /**
     * Sets up click listeners for the three quick action cards on home screen.
     * 
     * Cards:
     * 1. Upload Plants - Navigate to plant upload flow
     * 2. Add to My Garden - Navigate to add plant screen
     * 3. AI Chat - Launch AI assistant activity
     */
    private void setupTopCardClickListeners() {
        // Card 1: Upload Plants - navigate to upload flow
        binding.card1.setOnClickListener(v -> {
            Log.d(TAG, "Upload Plants card clicked");
            if (navController != null) {
                try {
                    navController.navigate(R.id.navigation_upload);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), "Upload navigation action not found", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Navigation failed", e);
                }
            }
        });

        // Card 2: Plant Map
        if (binding.card2 != null) {
            binding.card2.setOnClickListener(v -> {
                if (navController != null) {
                    // Make sure R.id.action_navigation_home_to_plantMapFragment is defined
                    try {
                        navController.navigate(R.id.navigation_plant_map);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "Plant Map navigation action not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Card Row 2 exists if binding.cardRow2 is not null (assuming it's the LinearLayout ID)
        // Card 3: My Garden
        if (binding.card3 != null) { // Ensure card3 ID exists in your XML
            binding.card3.setOnClickListener(v -> {
                if (navController != null) {
                    // Make sure R.id.action_navigation_home_to_myGardenFragment is defined
                    try {
                        navController.navigate(R.id.navigation_my_garden);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "My Garden navigation action not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Card 4: Plant Wiki
        if (binding.card4 != null) { // Ensure card4 ID exists in your XML
            binding.card4.setOnClickListener(v -> {
                if (navController != null) {
                    // Make sure R.id.action_navigation_home_to_plantWikiFragment is defined
                    try {
                        navController.navigate(R.id.navigation_plant_wiki);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "Plant Wiki navigation action not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setupNearbyDiscoveriesRecyclerView() {
        // The LinearLayoutManager is assumed to be set in the XML layout for recyclerViewNearbyDiscoveries
        // (e.g., app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" and android:orientation="horizontal")

        discoveryItemList = new ArrayList<>();
        // Use requireContext() for a non-null Context once the fragment is attached
        discoveryAdapter = new DiscoveryAdapter(requireContext(), discoveryItemList, plantId -> {
            // On discovery card click: fetch full plant details then navigate
            fetchPlantAndNavigate((int) plantId);
        });
        binding.recyclerViewNearbyDiscoveries.setAdapter(discoveryAdapter);

        // Optional: If you need custom item spacing for the horizontal RecyclerView
        // binding.recyclerViewNearbyDiscoveries.addItemDecoration(new HorizontalSpaceItemDecoration(16)); // Example
    }

    /**
     * Request user location, then load nearby discovery data
     */
    private void requestLocationAndLoadData() {
        // Check permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Have permission, get location
            locationManager.getLocation(new MapLocationManager.OnLocationResultCallback() {
                @Override
                public void onLocationSuccess(Location location) {
                    userLocation = location;
                    Log.d(TAG, "User location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                    loadNearbyDiscoveryData(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onLocationError(String error) {
                    Log.w(TAG, "Failed to get user location: " + error);
                    // Use default location (Sydney) as fallback
                    userLocation = null;
                    loadNearbyDiscoveryData(-33.8523341, 151.2106085);
                }
            });
        } else {
            // No permission, use default location
            Log.w(TAG, "Location permission not granted, using default location");
            userLocation = null;
            loadNearbyDiscoveryData(-33.8523341, 151.2106085);
        }
    }

    /**
     * Fetches nearby public plants from backend API and displays them in carousel.
     * 
     * API Call: GET /api/plants/nearby
     * - Returns plants with isPublic=true near user's location
     * - Includes Base64 encoded images
     * - Ordered by distance (backend logic)
     * 
     * Process:
     * 1. Make API call via Retrofit
     * 2. Log detailed response information for debugging
     * 3. Handle various response scenarios:
     *    - Success with data: Convert to DiscoveryItems and display
     *    - Success but empty: Show helpful message about contributing
     *    - API error: Log error details and show placeholder
     *    - Network error: Show connection error message
     * 4. Update RecyclerView adapter with results
     * 
     * Error Handling:
     * - Checks if fragment is still attached before UI updates
     * - Logs all response codes and error bodies
     * - Provides user-friendly error messages
     * - Falls back to placeholder data on any failure
     * 
     * @param latitude User's latitude coordinate
     * @param longitude User's longitude coordinate
     */
    private void loadNearbyDiscoveryData(double latitude, double longitude) {
        Log.d(TAG, "Fetching nearby plants from server...");
        Log.d(TAG, "Search center: (" + latitude + ", " + longitude + "), radius: " + SEARCH_RADIUS_METERS + "m");
        
        // Create API service and make request
        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantMapDto>>> call = apiService.getNearbyPlants(latitude, longitude, SEARCH_RADIUS_METERS);
        
        call.enqueue(new Callback<ApiResponse<List<PlantMapDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantMapDto>>> call, 
                                 @NonNull Response<ApiResponse<List<PlantMapDto>>> response) {
                // Safety check: Ensure fragment is still attached
                if (binding == null) return;
                
                // Log response details for debugging
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<PlantMapDto>> apiResponse = response.body();
                    Log.d(TAG, "API Response code: " + apiResponse.getCode());
                    Log.d(TAG, "API Response message: " + apiResponse.getMessage());
                    
                    if (apiResponse.getData() != null) {
                        List<PlantMapDto> nearbyPlants = apiResponse.getData();
                        Log.d(TAG, "Successfully fetched " + nearbyPlants.size() + " nearby plants.");
                        
                        // Handle empty results with helpful message
                        if (nearbyPlants.isEmpty()) {
                            Log.w(TAG, "Nearby plants list is empty");
                            Toast.makeText(getContext(), 
                                "No nearby plants discovered yet. Upload plants to share with others!", 
                                Toast.LENGTH_LONG).show();
                            loadPlaceholderData();
                            return;
                        }
                        
                        // Convert PlantDto objects to DiscoveryItem objects for display
                        List<DiscoveryItem> discoveryItems = new ArrayList<>();
                        for (PlantMapDto plant : nearbyPlants) {
                            Log.d(TAG, "Processing plant: " + plant.getName());
                            
                            // Calculate distance (placeholder for now, TODO: use actual GPS calculation)
                            String distance = calculateDistance(plant);
                            
                            // Use description or default text
                            String description = plant.getDescription() != null ? 
                                plant.getDescription() : "Discovered nearby";
                            
                            // Create discovery item with Base64 image string
                            discoveryItems.add(new DiscoveryItem(
                                plant.getPlantId(),
                                plant.getName(),
                                distance,
                                R.drawable.map_foreground, // Placeholder resource ID
                                description,
                                null // no image in PlantMapDto; adapter should handle null
                            ));
                        }
                        
                        // Update RecyclerView adapter with new data
                        if (discoveryAdapter != null) {
                            discoveryAdapter.updateData(discoveryItems);
                            Log.d(TAG, "Updated adapter with " + discoveryItems.size() + " items");
                        }
                    } else {
                        // API returned success but data is null
                        Log.e(TAG, "API response data is null");
                        Toast.makeText(getContext(), "No nearby plants available", Toast.LENGTH_SHORT).show();
                        loadPlaceholderData();
                    }
                } else {
                    // API returned error response
                    Log.e(TAG, "Failed to fetch nearby plants. Code: " + response.code());
                    
                    // Try to log error body for debugging
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error body", e);
                        }
                    }
                    
                    Toast.makeText(getContext(), 
                        "Unable to load nearby plants. Please check your location permissions.", 
                        Toast.LENGTH_LONG).show();
                    loadPlaceholderData();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantMapDto>>> call, @NonNull Throwable t) {
                // Network error (no response from server)
                if (binding == null) return;
                
                Log.e(TAG, "Network error fetching nearby plants", t);
                Toast.makeText(getContext(), 
                    "Unable to connect to server. Please check your internet connection.", 
                    Toast.LENGTH_LONG).show();
                
                loadPlaceholderData();
            }
        });
    }
    
    /**
     * Calculates a display-friendly distance string from plant location data.
     * Uses Android's Location.distanceBetween() to calculate real distance.
     * 
     * @param plant Plant with latitude/longitude coordinates
     * @return Formatted distance string (e.g., "250 m away" or "1.2 km away")
     */
    private String calculateDistance(PlantMapDto plant) {
        if (plant.getLatitude() == null || plant.getLongitude() == null) {
            return "Unknown distance";
        }
        
        // If user location is not available, cannot calculate distance
        if (userLocation == null) {
            return "Distance unknown";
        }
        
        try {
            float[] results = new float[1];
            Location.distanceBetween(
                userLocation.getLatitude(), userLocation.getLongitude(),
                plant.getLatitude(), plant.getLongitude(),
                results
            );
            
            float distanceInMeters = results[0];
            
            // Format distance: use meters for < 1000m, kilometers for >= 1000m
            if (distanceInMeters < 1000) {
                return String.format("%.0f m away", distanceInMeters);
            } else {
                return String.format("%.1f km away", distanceInMeters / 1000.0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate distance", e);
            return "Distance unknown";
        }
    }
    
    /**
     * Loads placeholder data if the API call fails or returns empty results.
     */
    private void loadPlaceholderData() {
        List<DiscoveryItem> placeholderData = new ArrayList<>();
        int placeholderImage = R.drawable.map_foreground;
        
        placeholderData.add(new DiscoveryItem(
            null,
            "No Nearby Plants Yet", 
            "Be the first!", 
            placeholderImage, 
            "Upload a plant with 'Share with other users' enabled to appear in nearby discoveries."
        ));
        
        if (discoveryAdapter != null) {
            discoveryAdapter.updateData(placeholderData);
            Log.d(TAG, "Loaded placeholder data");
        }
    }

    /**
     * Fetches full plant details by ID and navigates to PlantDetailFragment.
     */
    private void fetchPlantAndNavigate(int plantId) {
        MapDataManager dataManager = new MapDataManager(requireContext());
        dataManager.getPlantById(plantId, new MapDataManager.MapDataCallback<PlantDto>() {
            @Override
            public void onSuccess(PlantDto plantDto) {
                try {
                    Plant plant = plantDto.toPlant();
                    Bundle args = new Bundle();
                    args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
                    if (navController != null) {
                        navController.navigate(R.id.plantDetailFragment, args);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to convert PlantDto to Plant", e);
                    Toast.makeText(getContext(), "Failed to open details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to get plant details: " + message);
                Toast.makeText(getContext(), "Failed to load plant details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to avoid memory leaks
        if (binding != null && binding.recyclerViewNearbyDiscoveries != null) {
            binding.recyclerViewNearbyDiscoveries.setAdapter(null); // Good practice
        }
        binding = null;
        navController = null; // Also clear NavController reference if it's tied to the view
    }

    // Example ItemDecoration for horizontal spacing (if needed)
    /*
    public static class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int horizontalSpaceWidth;

        public HorizontalSpaceItemDecoration(int horizontalSpaceWidth) {
            // Convert dp to pixels if needed, or assume this is already in pixels
            this.horizontalSpaceWidth = horizontalSpaceWidth;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // Add right margin to all items except the last one
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.right = horizontalSpaceWidth;
            }
            // Add left margin to the first item if you have paddingStart="0dp" on RecyclerView
            if (parent.getChildAdapterPosition(view) == 0) {
                 // outRect.left = horizontalSpaceWidth; // If RecyclerView has no paddingStart
            }
        }
    }
    */
}
