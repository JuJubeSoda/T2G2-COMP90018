// Package declaration
package com.example.myapplication;

// Android core imports
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.ActivityMainBinding;

/**
 * MainActivity is the entry point for the app.
 * It sets up the layout, navigation controller, and click listeners
 * for the BottomAppBar ImageButtons.
 */
public class MainActivity extends AppCompatActivity {

    // View Binding for activity_main.xml
    private ActivityMainBinding activityBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using View Binding
        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

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
}
