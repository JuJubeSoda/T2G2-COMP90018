package com.example.myapplication.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;

import com.example.myapplication.ui.myplants.myGarden.PlantDetailFragment;
import com.example.myapplication.ui.myplants.share.Plant;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.PlantMapDto;
import com.example.myapplication.map.PlantGardenMapManager;
import com.example.myapplication.map.MapDataManager;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;

import java.util.List;

public class PlantMapFragment extends Fragment implements OnMapReadyCallback, PlantBottomSheetDialogFragment.OnPlantActionListener, GardenBottomSheetDialogFragment.OnGardenActionListener {

    private static final String TAG = "PlantMapFragment";
    private GoogleMap mMap;
    private PlantGardenMapManager plantGardenMapManager;
    
    
    
    // Bottom sheet UI components
    // Removed LinearLayout bottomSheetContainer and related variables/logic.
    // In onPlantClick implementation:
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // In onGardenClick implementation:
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    
    // Refresh control buttons
    private Button btnToggleDataType;
    private Button btnBackToGardens;
    
    // Current coordinates for navigation
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    
    // Current plant for like functionality
    private PlantMapDto currentPlant = null;
    // Local like state (null = unknown, true = liked, false = not liked)
    private Boolean currentLiked = null;
    
    // UI related fields
    private FloatingActionButton fabPlaces;
    private PlantBottomSheetDialogFragment currentPlantSheet;
    

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plant_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Request location permission with an official prompt before using location
        checkAndRequestLocationPermission();


        // Initialize bottom sheet components
        // Removed LinearLayout bottomSheetContainer and related variables/logic.
        // In onPlantClick implementation:
        // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
        // In onGardenClick implementation:
        // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
        
        // Initialize refresh control buttons
        initializeRefreshControls(view);
        
        // Initialize places button
        initializePlacesButton(view);
        // Initialize plant id search bar
        initializePlantIdSearch(view);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void checkAndRequestLocationPermission() {
        Log.d(TAG, "检查定位权限...");
        int requestCode = com.example.myapplication.map.MapLocationManager.getLocationPermissionRequestCode();
        boolean hasPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "定位权限状态: " + (hasPermission ? "已授予" : "未授予"));
        
        if (!hasPermission) {
            Log.d(TAG, "显示权限请求对话框");
            new AlertDialog.Builder(requireContext())
                .setTitle("位置权限请求")
                .setMessage("我们需要您的位置权限以显示当前位置和附近植物。请允许以获得完整体验。")
                .setPositiveButton("继续", (dialog, which) -> {
                    Log.d(TAG, "用户点击继续，请求权限");
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    Log.d(TAG, "用户取消权限请求");
                })
                .show();
        } else {
            Log.d(TAG, "已有权限，无需请求");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Initialize Plant Garden Map Manager
        plantGardenMapManager = new PlantGardenMapManager(requireContext(), mMap);
        plantGardenMapManager.setOnPlantGardenMapInteractionListener(new PlantGardenMapManager.OnPlantGardenMapInteractionListener() {
            @Override
            public void onPlantClick(PlantMapDto plant) {
                Log.d(TAG, "onPlantClick fired for: " + plant.getName() + " (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
                // Show plant info with BottomSheetDialogFragment (default unliked; MyFavourite page handles separately)
                PlantBottomSheetDialogFragment sheet = PlantBottomSheetDialogFragment.newInstance(plant, false);
                sheet.show(getChildFragmentManager(), "plant_sheet");
                currentPlantSheet = sheet;
            }
            
            @Override
            public void onGardenClick(GardenDto garden) {
                Log.d(TAG, "onGardenClick fired for: " + garden.getName());
                // Show garden info with BottomSheetDialogFragment
                GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
            }
            
            @Override
            public void onPlantsFound(List<PlantMapDto> plants) {
                Log.d(TAG, "=== onPlantsFound Callback Debug ===");
                Log.d(TAG, "Received plants: " + (plants == null ? "null" : plants.size()));
                if (plants != null && !plants.isEmpty()) {
                    Log.d(TAG, "First plant: " + plants.get(0).getName());
                    Log.d(TAG, "Coordinates: (" + plants.get(0).getLatitude() + ", " + plants.get(0).getLongitude() + ")");
                }
                Log.d(TAG, "PlantGardenMapManager instance: " + (plantGardenMapManager == null ? "null" : "available"));
                Log.d(TAG, "=== End onPlantsFound Callback Debug ===");
                // Plant data is already displayed on the map by MapDisplayManager
                if (plants == null || plants.isEmpty()) {
                    Toast.makeText(getContext(), "No nearby plants in this area", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Found " + plants.size() + " plants", Toast.LENGTH_SHORT).show();
                }
                // Update BackToGardens & search bar visibility
                updateLockButtonVisibility();
                updateSearchBarVisibility();
            }
            
            @Override
            public void onGardensFound(List<GardenDto> gardens) {
                // Garden data is already displayed on the map by MapDisplayManager
            }
            
            @Override
            public void onSearchError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onDataTypeChanged(boolean isShowingPlants) {
                String message = isShowingPlants ? "Switched to Plants view" : "Switched to Gardens view";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                updateLockButtonVisibility();
                updateSearchBarVisibility();
            }
            
            @Override
            public void onPlantLiked(boolean liked) {
                String message = liked ? "Plant liked! ❤️" : "Plant unliked";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // sync local state with server result
                currentLiked = liked;
                if (currentPlantSheet != null) {
                    currentPlantSheet.setLikedState(liked);
                    currentPlantSheet.setLikeButtonEnabled(true);
                }
                // If unliked, notify MyGarden (MyFavourite) to update list
                if (!liked) {
                    try {
                        Bundle result = new Bundle();
                        result.putInt("plantId", currentPlant != null && currentPlant.getPlantId() != null ? currentPlant.getPlantId().intValue() : -1);
                        result.putString("action", "unliked");
                        getParentFragmentManager().setFragmentResult("favourite_change", result);
                    } catch (Exception ignored) {}
                }
            }
            
            @Override
            public void onPlantLikeError(String message) {
                Toast.makeText(getContext(), "Failed to like plant: " + message, Toast.LENGTH_SHORT).show();
                if (currentPlantSheet != null) currentPlantSheet.setLikeButtonEnabled(true);
            }
            
            public void onMapRadiusChanged(int newRadius) {
                // Optionally display radius change info or auto re-search
                Log.d(TAG, "Map radius changed to: " + newRadius + " meters");
                // Optional: show radius change hint
                // Toast.makeText(getContext(), "Search radius: " + newRadius + "m", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Initialize map with location services
        plantGardenMapManager.initializeMap();
        // Initially set button visibility based on current mode and lock state
        updateLockButtonVisibility();

        // If a plantId was provided, center and show that plant on the map (focus and open sheet)
        Bundle args = getArguments();
        if (args != null && args.containsKey("plantId")) {
            int plantId = args.getInt("plantId");
            // Ensure in plants mode
            if (!plantGardenMapManager.isShowingPlants()) {
                plantGardenMapManager.toggleDataType();
            }
            plantGardenMapManager.searchAndShowPlantById(plantId);
        }
    }

    

    /**
     * Initialize the bottom sheet components
     */
    // Removed LinearLayout bottomSheetContainer and related variables/logic.
    // In onPlantClick implementation:
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // In onGardenClick implementation:
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    
    /**
     * Initialize floating action button for places
     */
    private void initializePlacesButton(View view) {
        fabPlaces = view.findViewById(R.id.fab_places);
        if (fabPlaces != null) {
            fabPlaces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCurrentPlace();
                }
            });
        }
    }

    /**
     * Initialize PlantId search bar
     */
    private void initializePlantIdSearch(View view) {
        View searchBar = view.findViewById(R.id.plant_id_search_bar);
        android.widget.EditText et = view.findViewById(R.id.et_plant_id);
        View btnSearch = view.findViewById(R.id.btn_search_id);

        if (btnSearch != null && et != null) {
            btnSearch.setOnClickListener(v -> {
                String input = et.getText() != null ? et.getText().toString().trim() : "";
                if (input.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Plant ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int plantId = Integer.parseInt(input);
                    if (plantGardenMapManager != null) {
                        plantGardenMapManager.searchAndShowPlantById(plantId);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid Plant ID", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Initial visibility: only visible when Plants mode is unlocked
        if (searchBar != null && plantGardenMapManager != null) {
            boolean show = plantGardenMapManager.isShowingPlants() && !plantGardenMapManager.isPlantViewLocked();
            searchBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * Initialize refresh control buttons
     */
    private void initializeRefreshControls(View view) {
        btnToggleDataType = view.findViewById(R.id.btn_toggle_data_type);
        btnBackToGardens = view.findViewById(R.id.btn_back_to_gardens);
        
        
        if (btnToggleDataType != null) {
            btnToggleDataType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleDataType();
                    updateDataTypeButtonText();
                }
            });
            updateDataTypeButtonText();
        }

        if (btnBackToGardens != null) {
            btnBackToGardens.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (plantGardenMapManager != null) {
                        plantGardenMapManager.restoreGardensView();
                        updateLockButtonVisibility();
                    }
                }
            });
            // Initialize once
            updateLockButtonVisibility();
        }
    }
    
    
    /**
     * Update the data type button text based on current state
     */
    private void updateDataTypeButtonText() {
        if (btnToggleDataType != null && plantGardenMapManager != null) {
            if (plantGardenMapManager.isShowingPlants()) {
                btnToggleDataType.setText("🌱 Plants");
                btnToggleDataType.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light));
            } else {
                btnToggleDataType.setText("🌿 Gardens");
                btnToggleDataType.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light));
            }
        }
    }
    
    /**
     * Toggle between plants and gardens data type
     */
    private void toggleDataType() {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.toggleDataType();
            // Auto-trigger a load; Garden mode fetches all
            plantGardenMapManager.searchNearbyData();
            updateLockButtonVisibility();
            updateSearchBarVisibility();
        }
    }


    /**
     * Hide the bottom sheet
     */
    // Removed LinearLayout bottomSheetContainer and related variables/logic.
    // In onPlantClick implementation:
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // In onGardenClick implementation:
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");

    /**
     * Check if bottom sheet is visible
     */
    // Removed LinearLayout bottomSheetContainer and related variables/logic.
    // In onPlantClick implementation:
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // In onGardenClick implementation:
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");

    /**
     * Open navigation to the current location
     */
    private void openNavigationToLocation() {
        if (currentLat == 0.0 && currentLng == 0.0) {
            Toast.makeText(getContext(), "No location available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create navigation intent
            String navigationUri = String.format("google.navigation:q=%f,%f&mode=driving", 
                currentLat, currentLng);
            
            Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUri));
            navigationIntent.setPackage("com.google.android.apps.maps");
            
            // Check if Google Maps is available
            if (navigationIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(navigationIntent);
                Log.d(TAG, "Opening Google Maps navigation to: " + currentLat + ", " + currentLng);
            } else {
                // Fallback to web-based navigation
                String webNavigationUri = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", 
                    currentLat, currentLng);
                
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webNavigationUri));
                startActivity(webIntent);
                Log.d(TAG, "Opening web-based navigation to: " + currentLat + ", " + currentLng);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to open navigation", e);
            Toast.makeText(getContext(), "Unable to open navigation", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (requestCode == com.example.myapplication.map.MapLocationManager.getLocationPermissionRequestCode()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "已获得位置权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "未授权位置权限，将无法准确显示定位", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    

    /**
     * Refresh current data - directly call existing methods
     */
    private void refreshCurrentData() {
        if (plantGardenMapManager == null) {
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // Only check location permission in plants mode; gardens mode not needed
        if (plantGardenMapManager.isShowingPlants() && !plantGardenMapManager.hasLocationPermission()) {
            Toast.makeText(getContext(), "Location permission required to refresh plants", Toast.LENGTH_SHORT).show();
            checkAndRequestLocationPermission();
            return;
        }

        // Show refresh message
        String refreshMessage = plantGardenMapManager.isShowingPlants() ? "Refreshing plants data..." : "Refreshing gardens data...";
        Toast.makeText(getContext(), refreshMessage, Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Refreshing current data using existing searchNearbyData() method...");
        
        // Directly call existing search method
        plantGardenMapManager.searchNearbyData();
        updateLockButtonVisibility();
        updateSearchBarVisibility();
    }

    // Control "Back to Gardens" button visibility based on mode and lock state
    private void updateLockButtonVisibility() {
        if (btnBackToGardens == null || plantGardenMapManager == null) return;
        boolean show = plantGardenMapManager.isShowingPlants() && plantGardenMapManager.isPlantViewLocked();
        Log.d(TAG, "updateLockButtonVisibility: show=" + show + ", isPlants=" + plantGardenMapManager.isShowingPlants() + ", locked=" + plantGardenMapManager.isPlantViewLocked());
        btnBackToGardens.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Move to current location - move map to user position
     */
    private void showCurrentPlace() {
        if (plantGardenMapManager == null) {
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check location permission
        if (!plantGardenMapManager.hasLocationPermission()) {
            Toast.makeText(getContext(), "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show moving message
        Toast.makeText(getContext(), "Moving to your current location...", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Moving map to current location...");
        
        // Move map to current location
        plantGardenMapManager.getLocationManager().getCurrentLocationAndMove();
    }
    
    private void updateSearchBarVisibility() {
        View root = getView();
        if (root == null || plantGardenMapManager == null) return;
        View searchBar = root.findViewById(R.id.plant_id_search_bar);
        if (searchBar == null) return;
        boolean show = plantGardenMapManager.isShowingPlants() && !plantGardenMapManager.isPlantViewLocked();
        searchBar.setVisibility(show ? View.VISIBLE : View.GONE);
        Log.d(TAG, "updateSearchBarVisibility: show=" + show);
    }
    
    
    
    
    /**
     * Get full plant details and navigate to detail page
     */
    private void getFullPlantDetailsAndNavigate(int plantId) {
        Log.d(TAG, "Getting full plant details for ID: " + plantId);
        
        // Show loading indicator
        Toast.makeText(getContext(), "Loading plant details...", Toast.LENGTH_SHORT).show();
        
        // Use existing plantGardenMapManager's dataManager if available, otherwise create new instance
        if (plantGardenMapManager == null) {
            Log.e(TAG, "PlantGardenMapManager is null, cannot get plant details");
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create a temporary MapDataManager for getting plant details
        // Note: This is independent of the map display flow
        MapDataManager dataManager = new MapDataManager(requireContext());
        
        // Call API to get full plant details
        dataManager.getPlantById(plantId, new MapDataManager.MapDataCallback<PlantDto>() {
            @Override
            public void onSuccess(PlantDto plantDto) {
                Log.d(TAG, "Successfully retrieved plant details: " + plantDto.getName());
                
                // Convert PlantDto to Plant object
                Plant plant = plantDto.toPlant();
                
                // Navigate to PlantDetailFragment
                navigateToPlantDetail(plant);
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to get plant details: " + message);
                Toast.makeText(getContext(), "Failed to load plant details: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Navigate to plant detail page
     */
    private void navigateToPlantDetail(Plant plant) {
        try {
            Log.d(TAG, "Navigating to plant detail for: " + plant.getName());
            
            // Check if fragment is still attached and view is available
            if (!isAdded() || getView() == null) {
                Log.e(TAG, "Fragment not attached or view is null, cannot navigate");
                Toast.makeText(getContext(), "Please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create bundle with plant data
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
            
            // Use action to navigate (safer than direct fragment ID)
            // Ensure navigation happens on main thread
            View view = getView();
            if (view != null) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    try {
                        NavController navController = Navigation.findNavController(view);
                        // Check if action exists before navigating
                        if (navController.getCurrentDestination() != null) {
                            navController.navigate(R.id.action_plantMapFragment_to_plantDetailFragment, args);
                            Log.d(TAG, "Navigation to plant detail successful");
                        } else {
                            Log.e(TAG, "NavController destination is null, cannot navigate");
                            Toast.makeText(getContext(), "Navigation unavailable, please try again", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to navigate to plant detail", e);
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to open plant details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to navigate to plant detail", e);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Failed to open plant details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // ==== BottomSheet actions callbacks ====
    @Override
    public void onMoreInfo(int plantId) {
        getFullPlantDetailsAndNavigate(plantId);
    }

    @Override
    public void onToggleLike(int plantId) {
        boolean liked = currentLiked != null && currentLiked;
        if (liked) {
            if (currentPlantSheet != null) currentPlantSheet.setLikeButtonEnabled(false);
            Toast.makeText(getContext(), "Unliking...", Toast.LENGTH_SHORT).show();
            if (plantGardenMapManager != null) {
                plantGardenMapManager.unlikePlant(plantId);
            }
            currentLiked = false;
        } else {
            if (currentPlantSheet != null) currentPlantSheet.setLikeButtonEnabled(false);
            Toast.makeText(getContext(), "Liking...", Toast.LENGTH_SHORT).show();
            if (plantGardenMapManager != null) {
                plantGardenMapManager.likePlant(plantId);
            }
            currentLiked = true;
        }
    }

    // ==== Garden bottom sheet actions ====
    @Override
    public void onViewPlants(long gardenId) {
        if (plantGardenMapManager == null) {
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "Loading plants in garden...", Toast.LENGTH_SHORT).show();
        plantGardenMapManager.fetchPlantsByGarden(gardenId);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Clean up PlantGardenMapManager
        if (plantGardenMapManager != null) {
            plantGardenMapManager.destroy();
            plantGardenMapManager = null;
        }
    }
}
