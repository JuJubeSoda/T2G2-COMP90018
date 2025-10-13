package com.example.myapplication.ui.myplants;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.myapplication.model.Garden;
import com.example.myapplication.model.Plant;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.utils.GardenMapManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;

public class PlantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "PlantMapFragment";
    private GoogleMap mMap;
    private GardenMapManager gardenMapManager;
    
    
    // Location-related fields
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted = false;
    private Location lastKnownLocation;
    
    // Default location and zoom
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    
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
    
    // Garden API related fields
    private FloatingActionButton fabPlaces;
    private ApiService apiService;
    
    // Data type toggle
    private boolean isShowingPlants = true; // true = plants, false = gardens
    
    // Used for selecting nearby gardens
    private static final int M_MAX_ENTRIES = 5; // Default max results
    private static final int DEFAULT_SEARCH_RADIUS = 1000; // meters
    private Garden[] nearbyGardens;
    private String[] gardenNames;
    private String[] gardenDescriptions;
    private LatLng[] gardenLatLngs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plant_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        // Initialize Garden API
        initializeGardenAPI();

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
        
        // Initialize Garden Map Manager
        gardenMapManager = new GardenMapManager(requireContext(), mMap);
        gardenMapManager.setOnGardenClickListener(new GardenMapManager.OnGardenClickListener() {
            @Override
            public void onGardenClick(Garden garden) {
                showGardenBottomSheet(garden);
            }
        });
        gardenMapManager.setOnPlantClickListener(new GardenMapManager.OnPlantClickListener() {
            @Override
            public void onPlantClick(Plant plant) {
                showPlantBottomSheet(plant);
            }
        });
        
        // Request location permission
        getLocationPermission();
        
        // Update location UI
        updateLocationUI();
        
        // Get device location
        getDeviceLocation();
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
     * Initialize API Service
     */
    private void initializeGardenAPI() {
        apiService = ApiClient.create(requireContext());
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
                    if (locationPermissionGranted && lastKnownLocation != null) {
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
        if (btnToggleDataType != null) {
            if (isShowingPlants) {
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
        isShowingPlants = !isShowingPlants;
        
        // Clear current display
        if (gardenMapManager != null) {
            gardenMapManager.clearCurrentDisplay();
        }
        
        // Show toast message
        String message = isShowingPlants ? "Switched to Plants view" : "Switched to Gardens view";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                
                // Enable zoom controls
                mMap.getUiSettings().setZoomControlsEnabled(true);
                
                // Disable Google Maps built-in navigation features
                mMap.getUiSettings().setMapToolbarEnabled(false);  // Hide navigation toolbar
                
                // Set map padding for location button
                mMap.setPadding(0, 0, 20, 100);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                
                // Disable Google Maps built-in navigation features
                mMap.getUiSettings().setMapToolbarEnabled(false);  // Hide navigation toolbar
                
                // Move to default location if no permission
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                
                // Reset padding
                mMap.setPadding(0, 0, 0, 0);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    

    /**
     * Show nearby gardens using backend API
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (locationPermissionGranted && lastKnownLocation != null) {
            // Show loading message based on current data type
            String loadingMessage = isShowingPlants ? "Searching for nearby plants..." : "Searching for nearby gardens...";
            Toast.makeText(getContext(), loadingMessage, Toast.LENGTH_SHORT).show();
            
            if (isShowingPlants) {
                // Call backend API to get nearby plants
                Call<ApiResponse<List<Plant>>> call = apiService.getNearbyPlants(
                    lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude(),
                    DEFAULT_SEARCH_RADIUS
                );
                call.enqueue(new Callback<ApiResponse<List<Plant>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Plant>>> call, Response<ApiResponse<List<Plant>>> response) {
                        handlePlantResponse(response);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Plant>>> call, Throwable t) {
                        Log.e(TAG, "Network call failed", t);
                        Toast.makeText(getContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Call backend API to get nearby gardens
                Call<ApiResponse<List<Garden>>> call = apiService.getNearbyGardens(
                    lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude(),
                    DEFAULT_SEARCH_RADIUS
                );
                call.enqueue(new Callback<ApiResponse<List<Garden>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Garden>>> call, Response<ApiResponse<List<Garden>>> response) {
                        handleGardenResponse(response);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Garden>>> call, Throwable t) {
                        Log.e(TAG, "Network call failed", t);
                        Toast.makeText(getContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(getContext(), "Location permission required to search nearby data", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle plant API response
     */
    private void handlePlantResponse(Response<ApiResponse<List<Plant>>> response) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<List<Plant>> apiResponse = response.body();
            
            if (apiResponse.isSuccessful() && apiResponse.getData() != null) {
                List<Plant> plants = apiResponse.getData();
                
                if (plants.isEmpty()) {
                    Toast.makeText(getContext(), "No plants found nearby", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Display plants on map
                gardenMapManager.displayPlantsAsGeoJson(plants);
            } else {
                Log.e(TAG, "API call failed: " + apiResponse.getMessage());
                Toast.makeText(getContext(), "Failed to load nearby plants", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "HTTP request failed: " + response.code() + " " + response.message());
            Toast.makeText(getContext(), "Network error occurred", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle garden API response
     */
    private void handleGardenResponse(Response<ApiResponse<List<Garden>>> response) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<List<Garden>> apiResponse = response.body();
            
            if (apiResponse.isSuccessful() && apiResponse.getData() != null) {
                List<Garden> gardens = apiResponse.getData();
                
                if (gardens.isEmpty()) {
                    Toast.makeText(getContext(), "No gardens found nearby", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Display gardens on map
                gardenMapManager.displayGardensAsGeoJson(gardens);
            } else {
                Log.e(TAG, "API call failed: " + apiResponse.getMessage());
                Toast.makeText(getContext(), "Failed to load nearby gardens", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "HTTP request failed: " + response.code() + " " + response.message());
            Toast.makeText(getContext(), "Network error occurred", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Legacy method for backward compatibility - now redirects to new logic
     */
    private void showCurrentPlaceLegacy() {
        if (locationPermissionGranted && lastKnownLocation != null) {
            // Show loading message
            Toast.makeText(getContext(), "Searching for nearby gardens...", Toast.LENGTH_SHORT).show();
            
            // Call backend API to get nearby gardens
            Call<ApiResponse<List<Garden>>> call = apiService.getNearbyGardens(
                lastKnownLocation.getLatitude(),
                lastKnownLocation.getLongitude(),
                DEFAULT_SEARCH_RADIUS
            );
            
            call.enqueue(new Callback<ApiResponse<List<Garden>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Garden>>> call, Response<ApiResponse<List<Garden>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<Garden>> apiResponse = response.body();
                        
                        if (apiResponse.isSuccessful() && apiResponse.getData() != null) {
                            List<Garden> gardens = apiResponse.getData();
                            
                            if (gardens.isEmpty()) {
                                Toast.makeText(getContext(), "No gardens found nearby", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // 方法1：直接在地图上显示花园（推荐）
                            // 使用GeoJSON图层显示，支持点击事件
                            gardenMapManager.displayGardensAsGeoJson(gardens);
                            
                            // 方法2：使用标记显示（备选方案）
                            // gardenMapManager.displayGardensAsMarkers(gardens);
                            
                            // 方法3：显示对话框让用户选择（保留原有功能）
                            // 如果需要保留对话框选择功能，可以取消注释下面的代码
                            // prepareGardenDialogData(gardens);
                            // openGardensDialog();
                        } else {
                            Log.e(TAG, "API call failed: " + apiResponse.getMessage());
                            Toast.makeText(getContext(), "Failed to load nearby gardens", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "HTTP request failed: " + response.code() + " " + response.message());
                        Toast.makeText(getContext(), "Network error occurred", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Garden>>> call, Throwable t) {
                    Log.e(TAG, "Network call failed", t);
                    Toast.makeText(getContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Location permission required to search nearby gardens", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open gardens dialog to let user select a garden
     */
    private void openGardensDialog() {
        // Create a dialog to show the nearby gardens
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Select Nearby Gardens");

        if (gardenNames != null && gardenNames.length > 0) {
            builder.setItems(gardenNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Add marker for selected garden
                    if (gardenLatLngs[which] != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(gardenLatLngs[which])
                                .title(gardenNames[which])
                                .snippet(gardenDescriptions[which]));
                        
                        // Move camera to the selected garden
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gardenLatLngs[which], 15));
                        
                        Toast.makeText(getContext(), "Added garden marker: " + gardenNames[which], Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            builder.setMessage("No gardens found nearby");
        }

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * 准备花园对话框数据（保留原有对话框功能）
     */
    private void prepareGardenDialogData(List<Garden> gardens) {
        int count = Math.min(gardens.size(), M_MAX_ENTRIES);
        
        nearbyGardens = new Garden[count];
        gardenNames = new String[count];
        gardenDescriptions = new String[count];
        gardenLatLngs = new LatLng[count];

        for (int i = 0; i < count; i++) {
            Garden garden = gardens.get(i);
            nearbyGardens[i] = garden;
            gardenNames[i] = garden.getName() != null ? garden.getName() : "Unnamed Garden";
            gardenDescriptions[i] = garden.getDescription() != null ? garden.getDescription() : "No description available";
            gardenLatLngs[i] = new LatLng(garden.getLatitude(), garden.getLongitude());
        }
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
        if (apiService == null) {
            Toast.makeText(getContext(), "API service not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Call<ApiResponse<String>> call = apiService.likePlant(plantId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        // Update UI
                        currentPlant.setFavourite(true);
                        updateLikeButton();
                        Toast.makeText(getContext(), "Plant liked! ❤️", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to like plant: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to like plant", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Like plant failed", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Unlike a plant
     */
    private void unlikePlant(Long plantId) {
        if (apiService == null) {
            Toast.makeText(getContext(), "API service not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Call<ApiResponse<String>> call = apiService.unlikePlant(plantId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        // Update UI
                        currentPlant.setFavourite(false);
                        updateLikeButton();
                        Toast.makeText(getContext(), "Plant unliked", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to unlike plant: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to unlike plant", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Unlike plant failed", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        
        // Clean up GardenMapManager
        if (gardenMapManager != null) {
            gardenMapManager.destroy();
            gardenMapManager = null;
        }
    }
}

