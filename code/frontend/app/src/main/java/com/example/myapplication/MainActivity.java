// Package declaration
package com.example.myapplication;

// Android core imports
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.auth.MyProfileActivity;
import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MainActivity - Main entry point and navigation hub for the application.
 * 
 * Responsibilities:
 * - Request essential permissions (camera, location, storage) at startup
 * - Initialize navigation controller for fragment navigation
 * - Handle bottom navigation bar interactions
 * - Launch external activities (AI Chat)
 * 
 * Permission Flow:
 * 1. App starts → requestNecessaryPermissions() is called
 * 2. Checks which permissions are missing
 * 3. Requests all missing permissions at once
 * 4. Shows user feedback based on results
 * 
 * Navigation Structure:
 * - Home (imageButton6)
 * - Plant Wiki (imageButton5)
 * - Map (imageButton8)
 * - My Garden (imageButton9)
 * - AI Chat (imageButtonAI) - separate activity
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    
    /** View binding for activity_main.xml layout */
    private ActivityMainBinding activityBinding;

    /**
     * Permission launcher for requesting multiple permissions at startup.
     * Handles camera, location, and storage permissions needed for core features.
     * 
     * Callback behavior:
     * - All granted: Shows success message
     * - Some denied: Shows warning with list of denied permissions
     */
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                StringBuilder deniedPermissions = new StringBuilder();
                
                // Check which permissions were denied
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    if (!entry.getValue()) {
                        allGranted = false;
                        deniedPermissions.append(entry.getKey()).append("\n");
                    }
                }
                
                // Provide user feedback based on permission results
                if (allGranted) {
                    Log.d(TAG, "All permissions granted");
                    Toast.makeText(this, "Permissions granted! You can now use all features.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "Some permissions denied: " + deniedPermissions.toString());
                    Toast.makeText(this, "Some features may not work without permissions:\n" + deniedPermissions.toString(), 
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using View Binding for type-safe view access
        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        // Request necessary permissions at startup (camera, location, storage)
        requestNecessaryPermissions();

        // Setup NavController for fragment navigation
        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment)
                fragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main);

        // Ensure NavHostFragment exists in layout
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found. Check activity_main.xml layout.");
        }

        NavController navController = navHostFragment.getNavController();

        // Setup bottom navigation bar click listeners
        
        // Home button - navigate to home screen (with duplicate navigation check)
        activityBinding.imageButton6.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() != R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
            }
        });

        // Explore button - navigate to plant wiki
        activityBinding.imageButton5.setOnClickListener(v ->
                navController.navigate(R.id.navigation_plant_wiki));

        // Map button - navigate to map view
        activityBinding.imageButton8.setOnClickListener(v ->
                navController.navigate(R.id.navigation_plant_map));

        // Profile button - navigate to my garden
        activityBinding.imageButton9.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyProfileActivity.class);
            startActivity(intent);
        });


        // AI Chat button - launch separate activity (not a fragment)
        activityBinding.imageButtonAI.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AIChatActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Requests necessary permissions for the app to function properly.
     * 
     * Permissions requested:
     * - CAMERA: For capturing plant photos
     * - ACCESS_FINE_LOCATION: For tagging plant locations and nearby discoveries
     * - ACCESS_COARSE_LOCATION: Fallback location permission
     * - Storage permissions (version-dependent):
     *   - Android 13+: READ_MEDIA_IMAGES
     *   - Android 9-: WRITE_EXTERNAL_STORAGE
     * 
     * Flow:
     * 1. Check each permission individually
     * 2. Add missing permissions to request list
     * 3. Request all at once via requestPermissionsLauncher
     * 4. User sees system permission dialog
     * 5. Results handled by launcher callback
     */
    private void requestNecessaryPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Check camera permission (required for plant capture feature)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
            Log.d(TAG, "Camera permission not granted, requesting...");
        }

        // Check fine location permission (required for nearby discoveries and plant tagging)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            Log.d(TAG, "Fine location permission not granted, requesting...");
        }

        // Check coarse location permission (fallback for less precise location)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            Log.d(TAG, "Coarse location permission not granted, requesting...");
        }

        // Check storage permissions (varies by Android version)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { 
            // Android 13+ uses scoped storage with READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
                Log.d(TAG, "Read media images permission not granted, requesting...");
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { 
            // Android 9 and below uses WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                Log.d(TAG, "Write external storage permission not granted, requesting...");
            }
        }
        // Note: Android 10-12 uses MediaStore API without explicit storage permissions

        // Request all missing permissions at once
        if (!permissionsToRequest.isEmpty()) {
            Log.d(TAG, "Requesting " + permissionsToRequest.size() + " permissions");
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            Log.d(TAG, "All permissions already granted");
        }
    }
}
