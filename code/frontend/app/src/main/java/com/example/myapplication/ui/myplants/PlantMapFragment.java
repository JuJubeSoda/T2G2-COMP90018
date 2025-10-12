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
import com.example.myapplication.utils.GeoJsonManager;

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

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class PlantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "PlantMapFragment";
    private GoogleMap mMap;
    private GeoJsonManager geoJsonManager;
    
    // Auto-refresh related fields
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 300000; // 5 minutes in milliseconds
    private boolean isAutoRefreshEnabled = true;
    
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
    private TextView tvEarthquakeTitle;
    private TextView tvMagnitude;
    private TextView tvLocation;
    private TextView tvTime;
    private TextView tvCoordinates;
    private Button btnClose;
    private Button btnMoreInfo;
    private Button btnNavigate;
    
    // Refresh control buttons
    private Button btnRefreshData;
    private Button btnToggleAutoRefresh;
    
    // Current earthquake coordinates for navigation
    private double currentEarthquakeLat = 0.0;
    private double currentEarthquakeLng = 0.0;
    
    // Places API related fields
    private PlacesClient placesClient;
    private FloatingActionButton fabPlaces;
    
    // Used for selecting the current place
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

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
        
        // Initialize Places API
        initializePlacesAPI();

        // Initialize bottom sheet components
        initializeBottomSheet(view);
        
        // Initialize refresh control buttons
        initializeRefreshControls(view);
        
        // Initialize places button
        initializePlacesButton(view);

        // Initialize refresh handler
        initializeRefreshHandler();

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
        
        // Initialize GeoJSON manager
        geoJsonManager = new GeoJsonManager(requireContext(), mMap);
        geoJsonManager.setOnFeatureClickListener(new GeoJsonManager.OnFeatureClickListener() {
            @Override
            public void onFeatureClick(String magnitude, String location, String time, 
                                     double latitude, double longitude) {
                showEarthquakeBottomSheet(magnitude, location, time, latitude, longitude);
            }
        });
        
        // Request location permission
        getLocationPermission();
        
        // Update location UI
        updateLocationUI();
        
        // Get device location
        getDeviceLocation();
        
        // Load GeoJSON data (try remote first, fallback to local)
        loadGeoJsonData();
        
        // Start auto-refresh if enabled
        startAutoRefresh();
    }

    private void loadGeoJsonData() {
        // Use GeoJsonManager to load data with caching and fallback
        String remoteUrl = getString(R.string.geojson_url);
        int localResourceId = R.raw.earthquakes_with_usa;
        geoJsonManager.loadGeoJsonData(remoteUrl, localResourceId);
    }
    

    /**
     * Initialize the bottom sheet components
     */
    private void initializeBottomSheet(View view) {
        bottomSheetContainer = view.findViewById(R.id.bottom_sheet_container);
        tvEarthquakeTitle = view.findViewById(R.id.tv_earthquake_title);
        tvMagnitude = view.findViewById(R.id.tv_magnitude);
        tvLocation = view.findViewById(R.id.tv_location);
        tvTime = view.findViewById(R.id.tv_time);
        tvCoordinates = view.findViewById(R.id.tv_coordinates);
        btnClose = view.findViewById(R.id.btn_close);
        btnMoreInfo = view.findViewById(R.id.btn_more_info);
        btnNavigate = view.findViewById(R.id.btn_navigate);

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
                    openNavigationToEarthquake();
                }
            });
        }
    }

    /**
     * Initialize Places API
     */
    private void initializePlacesAPI() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), com.example.myapplication.BuildConfig.PLACES_API_KEY);
        }
        placesClient = Places.createClient(requireContext());
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
        btnToggleAutoRefresh = view.findViewById(R.id.btn_toggle_auto_refresh);
        
        if (btnRefreshData != null) {
            btnRefreshData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Clear cache to force fresh data load
                    if (geoJsonManager != null) {
                        geoJsonManager.clearCache();
                        refreshGeoJsonData();
                    }
                }
            });
        }
        
        if (btnToggleAutoRefresh != null) {
            btnToggleAutoRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleAutoRefresh();
                    updateAutoRefreshButtonText();
                }
            });
            updateAutoRefreshButtonText();
        }
    }
    
    /**
     * Update the auto-refresh button text based on current state
     */
    private void updateAutoRefreshButtonText() {
        if (btnToggleAutoRefresh != null) {
            if (isAutoRefreshEnabled) {
                btnToggleAutoRefresh.setText("Auto: ON");
                btnToggleAutoRefresh.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light));
            } else {
                btnToggleAutoRefresh.setText("Auto: OFF");
                btnToggleAutoRefresh.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light));
            }
        }
    }

    /**
     * Show the earthquake information bottom sheet
     */
    private void showEarthquakeBottomSheet(String magnitude, String location, String time, double latitude, double longitude) {
        Log.d(TAG, "showEarthquakeBottomSheet called with: " + magnitude + ", " + location);

        // Store coordinates for navigation
        currentEarthquakeLat = latitude;
        currentEarthquakeLng = longitude;

        if (bottomSheetContainer != null) {
            Log.d(TAG, "Bottom sheet container found, updating content...");

            // Update the content
            if (tvMagnitude != null) {
                tvMagnitude.setText(magnitude);
                Log.d(TAG, "Magnitude set to: " + magnitude);
            }
            if (tvLocation != null) {
                tvLocation.setText(location);
                Log.d(TAG, "Location set to: " + location);
            }
            if (tvTime != null) {
                tvTime.setText(time);
                Log.d(TAG, "Time set to: " + time);
            }
            if (tvCoordinates != null) {
                tvCoordinates.setText(String.format("%.4f°N, %.4f°W", latitude, longitude));
                Log.d(TAG, "Coordinates set to: " + String.format("%.4f°N, %.4f°W", latitude, longitude));
            }

            // Show the bottom sheet with animation
            Log.d(TAG, "Showing bottom sheet with animation...");
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
     * Open navigation to the current earthquake location
     */
    private void openNavigationToEarthquake() {
        if (currentEarthquakeLat == 0.0 && currentEarthquakeLng == 0.0) {
            Toast.makeText(getContext(), "No earthquake location available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create navigation intent
            String navigationUri = String.format("google.navigation:q=%f,%f&mode=driving", 
                currentEarthquakeLat, currentEarthquakeLng);
            
            Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUri));
            navigationIntent.setPackage("com.google.android.apps.maps");
            
            // Check if Google Maps is available
            if (navigationIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(navigationIntent);
                Log.d(TAG, "Opening Google Maps navigation to: " + currentEarthquakeLat + ", " + currentEarthquakeLng);
            } else {
                // Fallback to web-based navigation
                String webNavigationUri = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", 
                    currentEarthquakeLat, currentEarthquakeLng);
                
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webNavigationUri));
                startActivity(webIntent);
                Log.d(TAG, "Opening web-based navigation to: " + currentEarthquakeLat + ", " + currentEarthquakeLng);
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
                mMap.getUiSettings().setMapToolbarEnabled(false);  // 隐藏导航工具栏
                
                // Set map padding for location button
                mMap.setPadding(0, 0, 20, 100);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                
                // Disable Google Maps built-in navigation features
                mMap.getUiSettings().setMapToolbarEnabled(false);  // 隐藏导航工具栏
                
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
     * Initialize the refresh handler for auto-refresh functionality
     */
    private void initializeRefreshHandler() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoRefreshEnabled && mMap != null) {
                    Log.d(TAG, "Auto-refreshing GeoJSON data...");
                    refreshGeoJsonData();
                    // Schedule next refresh
                    refreshHandler.postDelayed(this, REFRESH_INTERVAL);
                }
            }
        };
    }
    
    /**
     * Start the auto-refresh mechanism
     */
    private void startAutoRefresh() {
        if (isAutoRefreshEnabled && refreshHandler != null && refreshRunnable != null) {
            Log.d(TAG, "Starting auto-refresh with interval: " + REFRESH_INTERVAL + "ms");
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        }
    }
    
    /**
     * Stop the auto-refresh mechanism
     */
    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            Log.d(TAG, "Auto-refresh stopped");
        }
    }
    
    /**
     * Refresh GeoJSON data from remote source
     */
    public void refreshGeoJsonData() {
        Log.d(TAG, "Manually refreshing GeoJSON data...");
        if (geoJsonManager != null) {
            String remoteUrl = getString(R.string.geojson_url);
            int localResourceId = R.raw.earthquakes_with_usa;
            geoJsonManager.loadFromRemoteUrl(remoteUrl, localResourceId);
        }
    }
    
    /**
     * Toggle auto-refresh on/off
     */
    public void toggleAutoRefresh() {
        isAutoRefreshEnabled = !isAutoRefreshEnabled;
        if (isAutoRefreshEnabled) {
            startAutoRefresh();
            Toast.makeText(getContext(), "Auto-refresh enabled", Toast.LENGTH_SHORT).show();
        } else {
            stopAutoRefresh();
            Toast.makeText(getContext(), "Auto-refresh disabled", Toast.LENGTH_SHORT).show();
        }
        updateAutoRefreshButtonText();
    }

    /**
     * Show current place using Places API
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        openPlacesDialog();
                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                        Toast.makeText(getContext(), "无法获取附近地点信息", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "需要位置权限才能获取附近地点", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open places dialog to let user select a place
     */
    private void openPlacesDialog() {
        // Create a simple dialog to show the places
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("选择附近地点");

        if (likelyPlaceNames != null && likelyPlaceNames.length > 0) {
            builder.setItems(likelyPlaceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Add marker for selected place
                    if (likelyPlaceLatLngs[which] != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(likelyPlaceLatLngs[which])
                                .title(likelyPlaceNames[which])
                                .snippet(likelyPlaceAddresses[which]));
                        
                        // Move camera to the selected place
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(likelyPlaceLatLngs[which], 15));
                        
                        Toast.makeText(getContext(), "已添加地点标记: " + likelyPlaceNames[which], Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            builder.setMessage("未找到附近地点");
        }

        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop auto-refresh when fragment is destroyed
        stopAutoRefresh();
        
        // Clean up GeoJsonManager
        if (geoJsonManager != null) {
            geoJsonManager.removeCurrentLayer();
            geoJsonManager = null;
        }
    }
}
