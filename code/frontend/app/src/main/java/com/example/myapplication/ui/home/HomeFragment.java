package com.example.myapplication.ui.home; // Adjust to your project's package structure

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
import androidx.navigation.Navigation; // For NavController

import com.example.myapplication.R; // Your app's R file
import com.example.myapplication.ui.home.DiscoveryAdapter; // Assuming this is your adapter's package
import com.example.myapplication.databinding.HomeBinding; // Generated from fragment_home.xml
import com.example.myapplication.ui.home.DiscoveryItem; // Assuming this is your model's package
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private HomeBinding binding; // ViewBinding instance
    private DiscoveryAdapter discoveryAdapter;
    private List<DiscoveryItem> discoveryItemList;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = HomeBinding.inflate(inflater, container, false);
        // Don't return binding.getRoot() here yet if you need to initialize navController in onViewCreated
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize NavController here, as the view is now created
        navController = Navigation.findNavController(view);

        Log.d("HomeFragment", "Check1");
        // Setup UI components
        setupTopCardClickListeners();
        setupNearbyDiscoveriesRecyclerView();
        loadNearbyDiscoveryData();
        Log.d("HomeFragment", "Check2");
    }

    private void setupTopCardClickListeners() {
        // Card 1: Upload Plants (leads to a camera/gallery flow eventually)
        binding.card1.setOnClickListener(v -> {
            Log.d("HomeFragment", "Upload card clicked");
            if (navController != null) {
                // Make sure R.id.action_navigation_home_to_uploadFragment is defined in your nav_graph.xml
                try {
                    Log.d("HomeFragment", "try navigate to upload plants.");
                    navController.navigate(R.id.navigation_upload);
                    Log.d("HomeFragment", "After executing upload plants.");
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), "Upload navigation action not found", Toast.LENGTH_SHORT).show();
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
        discoveryAdapter = new DiscoveryAdapter(requireContext(), discoveryItemList);
        binding.recyclerViewNearbyDiscoveries.setAdapter(discoveryAdapter);

        // Optional: If you need custom item spacing for the horizontal RecyclerView
        // binding.recyclerViewNearbyDiscoveries.addItemDecoration(new HorizontalSpaceItemDecoration(16)); // Example
    }

    private void loadNearbyDiscoveryData() {
        Log.d(TAG, "Fetching nearby plants from server...");
        
        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantDto>>> call = apiService.getNearbyPlants();
        
        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantDto>>> call, 
                                 @NonNull Response<ApiResponse<List<PlantDto>>> response) {
                if (binding == null) return;
                
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<PlantDto> nearbyPlants = response.body().getData();
                    Log.d(TAG, "Successfully fetched " + nearbyPlants.size() + " nearby plants.");
                    
                    // Convert PlantDto to DiscoveryItem
                    List<DiscoveryItem> discoveryItems = new ArrayList<>();
                    for (PlantDto plant : nearbyPlants) {
                        String distance = calculateDistance(plant);
                        String description = plant.getDescription() != null ? plant.getDescription() : "Discovered nearby";
                        
                        // Use placeholder image for now, as Base64 decoding is handled by the adapter
                        discoveryItems.add(new DiscoveryItem(
                            plant.getName(),
                            distance,
                            R.drawable.map_foreground, // Placeholder, actual image will be loaded from plant.getImage()
                            description,
                            plant.getImage() // Pass the Base64 image string
                        ));
                    }
                    
                    // Update the adapter with the new data
                    if (discoveryAdapter != null) {
                        discoveryAdapter.updateData(discoveryItems);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch nearby plants. Code: " + response.code());
                    Toast.makeText(getContext(), "No nearby plants found", Toast.LENGTH_SHORT).show();
                    
                    // Show placeholder data if API fails
                    loadPlaceholderData();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e(TAG, "Network error fetching nearby plants", t);
                Toast.makeText(getContext(), "Unable to load nearby plants", Toast.LENGTH_SHORT).show();
                
                // Show placeholder data if network fails
                loadPlaceholderData();
            }
        });
    }
    
    /**
     * Calculates a display-friendly distance string from plant location data.
     * If latitude/longitude are null, returns "Unknown distance".
     */
    private String calculateDistance(PlantDto plant) {
        if (plant.getLatitude() != null && plant.getLongitude() != null) {
            // TODO: Calculate actual distance from user's location
            // For now, return a placeholder
            return String.format("%.1f km away", Math.random() * 5);
        }
        return "Unknown distance";
    }
    
    /**
     * Loads placeholder data if the API call fails.
     */
    private void loadPlaceholderData() {
        List<DiscoveryItem> placeholderData = new ArrayList<>();
        int placeholderImage = R.drawable.map_foreground;
        
        placeholderData.add(new DiscoveryItem("Nearby Plant", "Loading...", placeholderImage, "Discovering plants near you..."));
        
        if (discoveryAdapter != null) {
            discoveryAdapter.updateData(placeholderData);
        }
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
