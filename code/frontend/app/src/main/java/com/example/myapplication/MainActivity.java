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

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MainActivity is the entry point for the app.
 * It sets up the layout, navigation controller, and click listeners
 * for the BottomAppBar ImageButtons.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // View Binding for activity_main.xml
    private ActivityMainBinding activityBinding;

    // Permission launcher for requesting multiple permissions at startup
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                StringBuilder deniedPermissions = new StringBuilder();
                
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    if (!entry.getValue()) {
                        allGranted = false;
                        deniedPermissions.append(entry.getKey()).append("\n");
                    }
                }
                
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

        // Inflate layout using View Binding
        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        // Request necessary permissions at startup
        requestNecessaryPermissions();

        // --- Setup NavController safely ---
        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment)
                fragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found. Check activity_main.xml layout.");
        }

        NavController navController = navHostFragment.getNavController();

        // --- Setup BottomAppBar ImageButton navigation ---

        // Home
        activityBinding.imageButton6.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() != R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
            }
        });

        // Explore / Wiki
        activityBinding.imageButton5.setOnClickListener(v ->
                navController.navigate(R.id.navigation_plant_wiki));

        // Map
        activityBinding.imageButton8.setOnClickListener(v ->
                navController.navigate(R.id.navigation_plant_map));

        // Profile / My Garden
        activityBinding.imageButton9.setOnClickListener(v ->
                navController.navigate(R.id.navigation_my_garden));

        // ✅ AI Chat
        activityBinding.imageButtonAI.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AIChatActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Requests necessary permissions for the app to function properly.
     * Includes camera, location, and storage permissions.
     */
    private void requestNecessaryPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Camera permission (for plant capture)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
            Log.d(TAG, "Camera permission not granted, requesting...");
        }

        // Location permissions (for nearby discoveries and plant location)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            Log.d(TAG, "Fine location permission not granted, requesting...");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            Log.d(TAG, "Coarse location permission not granted, requesting...");
        }

        // Storage permissions (Android version dependent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
                Log.d(TAG, "Read media images permission not granted, requesting...");
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Android 9 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                Log.d(TAG, "Write external storage permission not granted, requesting...");
            }
        }

        // Request all needed permissions at once
        if (!permissionsToRequest.isEmpty()) {
            Log.d(TAG, "Requesting " + permissionsToRequest.size() + " permissions");
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            Log.d(TAG, "All permissions already granted");
        }
    }
}
