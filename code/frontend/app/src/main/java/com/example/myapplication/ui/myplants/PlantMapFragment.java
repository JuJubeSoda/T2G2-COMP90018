package com.example.myapplication.ui.myplants;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.myapplication.model.Garden;
import com.example.myapplication.model.Plant;
import com.example.myapplication.map.PlantGardenMapManager;

import java.util.List;

public class PlantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "PlantMapFragment";
    private GoogleMap mMap;
    private PlantGardenMapManager plantGardenMapManager;
    
    
    
    // Bottom sheet UI components
    private LinearLayout bottomSheetContainer;
    private TextView tvTitle;
    private TextView tvName;
    private TextView tvDescription;
    private TextView tvTime;
    private TextView tvCoordinates;
    private Button btnClose;
    private Button btnMoreInfo;
    private Button btnNavigate;
    private Button btnLike;
    
    // Refresh control buttons
    private Button btnRefreshData;
    private Button btnToggleDataType;
    
    // Current coordinates for navigation
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    
    // Current plant for like functionality
    private Plant currentPlant = null;
    
    // UI related fields
    private FloatingActionButton fabPlaces;
    

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plant_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Initialize bottom sheet components
        initializeBottomSheet(view);
        
        // Initialize refresh control buttons
        initializeRefreshControls(view);
        
        // Initialize places button
        initializePlacesButton(view);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Initialize Plant Garden Map Manager
        plantGardenMapManager = new PlantGardenMapManager(requireContext(), mMap);
        plantGardenMapManager.setOnPlantGardenMapInteractionListener(new PlantGardenMapManager.OnPlantGardenMapInteractionListener() {
            @Override
            public void onPlantClick(Plant plant) {
                showPlantBottomSheet(plant);
            }
            
            @Override
            public void onGardenClick(Garden garden) {
                showGardenBottomSheet(garden);
            }
            
            @Override
            public void onPlantsFound(List<Plant> plants) {
                // 植物数据已由MapDisplayManager自动显示在地图上
            }
            
            @Override
            public void onGardensFound(List<Garden> gardens) {
                // 花园数据已由MapDisplayManager自动显示在地图上
            }
            
            @Override
            public void onSearchError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onDataTypeChanged(boolean isShowingPlants) {
                String message = isShowingPlants ? "Switched to Plants view" : "Switched to Gardens view";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onPlantLiked(boolean liked) {
                String message = liked ? "Plant liked! ❤️" : "Plant unliked";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onPlantLikeError(String message) {
                Toast.makeText(getContext(), "Failed to like plant: " + message, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Initialize map with location services
        plantGardenMapManager.initializeMap();
    }

    

    /**
     * Initialize the bottom sheet components
     */
    private void initializeBottomSheet(View view) {
        bottomSheetContainer = view.findViewById(R.id.bottom_sheet_container);
        tvTitle = view.findViewById(R.id.tv_earthquake_title);
        tvName = view.findViewById(R.id.tv_magnitude);
        tvDescription = view.findViewById(R.id.tv_location);
        tvTime = view.findViewById(R.id.tv_time);
        tvCoordinates = view.findViewById(R.id.tv_coordinates);
        btnClose = view.findViewById(R.id.btn_close);
        btnMoreInfo = view.findViewById(R.id.btn_more_info);
        btnNavigate = view.findViewById(R.id.btn_navigate);
        btnLike = view.findViewById(R.id.btn_like);

        if (btnClose != null) {
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideBottomSheet();
                }
            });
        }

        if (btnMoreInfo != null) {
            btnMoreInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle more info button click
                    Toast.makeText(getContext(), "More info functionality", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNavigationToLocation();
                }
            });
        }

        if (btnLike != null) {
            btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLikeButtonClick();
                }
            });
        }
    }


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
     * Initialize refresh control buttons
     */
    private void initializeRefreshControls(View view) {
        btnRefreshData = view.findViewById(R.id.btn_refresh_data);
        btnToggleDataType = view.findViewById(R.id.btn_toggle_data_type);
        
        if (btnRefreshData != null) {
            btnRefreshData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Refresh current data type
                    if (plantGardenMapManager != null && plantGardenMapManager.hasLocationPermission()) {
                        showCurrentPlace();
                    } else {
                        Toast.makeText(getContext(), "Location permission required to refresh data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
        
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
        }
    }


    /**
     * Hide the bottom sheet
     */
    private void hideBottomSheet() {
        if (bottomSheetContainer != null) {
            bottomSheetContainer.animate()
                    .translationY(bottomSheetContainer.getHeight())
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            bottomSheetContainer.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    /**
     * Check if bottom sheet is visible
     */
    private boolean isBottomSheetVisible() {
        return bottomSheetContainer != null && bottomSheetContainer.getVisibility() == View.VISIBLE;
    }

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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    

    /**
     * Show nearby data using unified map manager
     */
    private void showCurrentPlace() {
        if (plantGardenMapManager == null) {
            return;
        }

        // Show loading message based on current data type
        String loadingMessage = plantGardenMapManager.isShowingPlants() ? "Searching for nearby plants..." : "Searching for nearby gardens...";
        Toast.makeText(getContext(), loadingMessage, Toast.LENGTH_SHORT).show();
        
        // 使用统一的地图管理器搜索附近数据
        plantGardenMapManager.searchNearbyData();
    }
    
    
    

    
    /**
     * 显示植物信息底部弹窗
     */
    private void showPlantBottomSheet(Plant plant) {
        Log.d(TAG, "showPlantBottomSheet called with: " + plant.getName());

        if (bottomSheetContainer != null) {
            Log.d(TAG, "Bottom sheet container found, updating content...");

            // Update the content for plant information
            if (tvTitle != null) {
                tvTitle.setText("Plant Information");
            }
            if (tvName != null) {
                tvName.setText(plant.getName() != null ? plant.getName() : "Unnamed Plant");
            }
            if (tvDescription != null) {
                String description = plant.getDescription() != null ? plant.getDescription() : "No description available";
                if (plant.getScientificName() != null) {
                    description += "\nScientific Name: " + plant.getScientificName();
                }
                tvDescription.setText(description);
            }
            if (tvTime != null) {
                tvTime.setText(plant.getCreatedAt() != null ? plant.getCreatedAt() : "Unknown");
            }
            if (tvCoordinates != null) {
                tvCoordinates.setText(String.format("%.4f°N, %.4f°E", 
                    plant.getLatitude(), plant.getLongitude()));
            }
            
            // Store coordinates for navigation
            currentLat = plant.getLatitude();
            currentLng = plant.getLongitude();
            
            // Store current plant for like functionality
            currentPlant = plant;

            // Show navigation button for plants
            if (btnNavigate != null) {
                btnNavigate.setVisibility(View.VISIBLE);
            }
            
            // Show and update like button for plants
            if (btnLike != null) {
                btnLike.setVisibility(View.VISIBLE);
                updateLikeButton();
            }

            // Show the bottom sheet with animation
            Log.d(TAG, "Showing plant bottom sheet with animation...");
            bottomSheetContainer.setVisibility(View.VISIBLE);
            bottomSheetContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        } else {
            Log.e(TAG, "Bottom sheet container is null!");
        }
    }
    
    /**
     * 显示花园信息底部弹窗
     */
    private void showGardenBottomSheet(Garden garden) {
        Log.d(TAG, "showGardenBottomSheet called with: " + garden.getName());

        if (bottomSheetContainer != null) {
            Log.d(TAG, "Bottom sheet container found, updating content...");

            // Update the content for garden information
            if (tvTitle != null) {
                tvTitle.setText("Garden Information");
            }
            if (tvName != null) {
                tvName.setText(garden.getName() != null ? garden.getName() : "Unnamed Garden");
            }
            if (tvDescription != null) {
                tvDescription.setText(garden.getDescription() != null ? garden.getDescription() : "No description available");
            }
            if (tvTime != null) {
                tvTime.setText(garden.getCreatedAt() != null ? garden.getCreatedAt() : "Unknown");
            }
            if (tvCoordinates != null) {
                tvCoordinates.setText(String.format("%.4f°N, %.4f°E", 
                    garden.getLatitude(), garden.getLongitude()));
            }
            
            // Store coordinates for navigation
            currentLat = garden.getLatitude();
            currentLng = garden.getLongitude();

            // Clear current plant (gardens don't have like functionality)
            currentPlant = null;
            
            // Hide navigation button for gardens (optional)
            if (btnNavigate != null) {
                btnNavigate.setVisibility(View.VISIBLE); // Keep visible for navigation to garden
            }
            
            // Hide like button for gardens
            if (btnLike != null) {
                btnLike.setVisibility(View.GONE);
            }

            // Show the bottom sheet with animation
            Log.d(TAG, "Showing garden bottom sheet with animation...");
            bottomSheetContainer.setVisibility(View.VISIBLE);
            bottomSheetContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        } else {
            Log.e(TAG, "Bottom sheet container is null!");
        }
    }
    
    /**
     * Handle like button click
     */
    private void handleLikeButtonClick() {
        if (currentPlant == null) {
            Toast.makeText(getContext(), "No plant selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentPlant.isFavourite()) {
            // Unlike the plant
            unlikePlant(currentPlant.getPlantId());
        } else {
            // Like the plant
            likePlant(currentPlant.getPlantId());
        }
    }
    
    /**
     * Like a plant
     */
    private void likePlant(Long plantId) {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.likePlant(plantId);
            // Update UI immediately for better UX
            currentPlant.setFavourite(true);
            updateLikeButton();
        } else {
            Toast.makeText(getContext(), "Map manager not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Unlike a plant
     */
    private void unlikePlant(Long plantId) {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.unlikePlant(plantId);
            // Update UI immediately for better UX
            currentPlant.setFavourite(false);
            updateLikeButton();
        } else {
            Toast.makeText(getContext(), "Map manager not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Update like button appearance based on current state
     */
    private void updateLikeButton() {
        if (btnLike != null && currentPlant != null) {
            if (currentPlant.isFavourite()) {
                btnLike.setText("❤️ Liked");
                btnLike.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                btnLike.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                btnLike.setText("❤️ Like");
                btnLike.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnLike.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
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

