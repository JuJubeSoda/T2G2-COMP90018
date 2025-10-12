package com.example.myapplication.ui.myplants;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "PlantMapFragment";
    private GoogleMap mMap;
    private GeoJsonLayer geoJsonLayer;
    
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

        // Initialize bottom sheet components
        initializeBottomSheet(view);

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
        
        // Request location permission
        getLocationPermission();
        
        // Update location UI
        updateLocationUI();
        
        // Get device location
        getDeviceLocation();
        
        // Load GeoJSON data
        loadGeoJsonData();
    }

    private void loadGeoJsonData() {
        try {
            geoJsonLayer = new GeoJsonLayer(mMap, R.raw.earthquakes_with_usa, getContext());
            
            // Apply custom styling
            applyStylesToMarkers(geoJsonLayer);
            
            // Add layer to map
            geoJsonLayer.addLayerToMap();
            
            // Set click listener
            geoJsonLayer.setOnFeatureClickListener(new GeoJsonLayer.OnFeatureClickListener() {
                @Override
                public void onFeatureClick(Feature feature) {
                    Log.d(TAG, "GeoJSON feature clicked!");
                    
                    // Extract earthquake information from the feature
                    String magnitude = feature.getProperty("mag");
                    String location = feature.getProperty("place");
                    String time = feature.getProperty("time");
                    String title = feature.getProperty("title");
                    
                    // Get coordinates from the geometry
                    double latitude = 0.0;
                    double longitude = 0.0;
                    
                    if (feature.getGeometry() != null && feature.getGeometry().getGeometryType().equals("Point")) {
                        com.google.maps.android.data.geojson.GeoJsonPoint point =
                            (com.google.maps.android.data.geojson.GeoJsonPoint) feature.getGeometry();
                        latitude = point.getCoordinates().latitude;
                        longitude = point.getCoordinates().longitude;
                    }
                    
                    // Format the time if available
                    String formattedTime = "Unknown";
                    if (time != null) {
                        try {
                            long timestamp = Long.parseLong(time);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            formattedTime = sdf.format(new Date(timestamp));
                        } catch (NumberFormatException e) {
                            formattedTime = time; // Use original time if parsing fails
                        }
                    }
                    
                    // Show bottom sheet with earthquake information
                    showEarthquakeBottomSheet(
                        magnitude != null ? magnitude : "Unknown",
                        location != null ? location : "Unknown location",
                        formattedTime,
                        latitude,
                        longitude
                    );
                    
                    Log.d(TAG, "Earthquake clicked - Magnitude: " + magnitude + ", Location: " + location);
                }
            });
            
        } catch (IOException e) {
            Log.e(TAG, "GeoJSON file could not be read", e);
        } catch (JSONException e) {
            Log.e(TAG, "GeoJSON file could not be converted to a JSONObject", e);
        }
    }

    private void applyStylesToMarkers(GeoJsonLayer geoJsonLayer) {
        // Apply custom icon based on magnitude
        for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            
            // Get magnitude and set icon size accordingly
            String magnitudeStr = feature.getProperty("mag");
            if (magnitudeStr != null) {
                try {
                    double magnitude = Double.parseDouble(magnitudeStr);
                    // Create custom icon based on magnitude
                    BitmapDescriptor icon = createMagnitudeIcon(magnitude);
                    pointStyle.setIcon(icon);
                } catch (NumberFormatException e) {
                    // Use default icon if magnitude parsing fails
                    pointStyle.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            } else {
                pointStyle.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            
            feature.setPointStyle(pointStyle);
        }
    }

    private BitmapDescriptor createMagnitudeIcon(double magnitude) {
        // Use flower icon for earthquakes
        return BitmapDescriptorFactory.fromResource(R.drawable.flower);
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
    }

    /**
     * Show the earthquake information bottom sheet
     */
    private void showEarthquakeBottomSheet(String magnitude, String location, String time, double latitude, double longitude) {
        Log.d(TAG, "showEarthquakeBottomSheet called with: " + magnitude + ", " + location);

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
                
                // Set map padding for location button
                mMap.setPadding(0, 0, 20, 100);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                
                // Move to default location if no permission
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                
                // Reset padding
                mMap.setPadding(0, 0, 0, 0);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
}
