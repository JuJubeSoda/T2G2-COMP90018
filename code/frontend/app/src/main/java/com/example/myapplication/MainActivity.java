package com.example.myapplication;

import android.os.Bundle;
import android.view.View; // Import View for click listeners if needed later
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI; // For BottomNavigationView setup

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        // --- Now, setup your Bottom Navigation ---
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // If you want to use the BottomAppBar/BottomNavigationView for navigation
        // (Your ImageButtons are currently separate, but if you switched to a BottomNavigationView)
        // Example if you were using a BottomNavigationView with ID 'bottom_nav_view':
        // NavigationUI.setupWithNavController(activityBinding.bottomNavView, navController);

        // If you want to set up an ActionBar (Toolbar) with the NavController (Optional)
        // AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        // R.id.navigation_home, R.id.navigation_plant_wiki, R.id.navigation_plant_map /*, other top-level IDs*/)
        // .build();
        // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


        // --- If your ImageButtons in BottomAppBar are for manual navigation ---
        // You'll need to set click listeners on them here in MainActivity
        // to call navController.navigate(...)

        // Example for one of your ImageButtons in activity_main.xml
        if (activityBinding.imageButton6 != null) { // Assuming imageButton6 is for Home
            activityBinding.imageButton6.setOnClickListener(v -> {
                // Navigate to home if not already there, or handle re-selection
                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() != R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home);
                }
            });
        }
        if (activityBinding.imageButton5 != null) { // Assuming imageButton5 is for Explore (Plant Wiki?)
            activityBinding.imageButton5.setOnClickListener(v -> navController.navigate(R.id.navigation_plant_wiki));
        }
        if (activityBinding.imageButton8 != null) { // Assuming imageButton8 is for Map
            activityBinding.imageButton8.setOnClickListener(v -> navController.navigate(R.id.navigation_plant_map));
        }
        // Add listener for imageButton9 (Profile/My Garden?) if it's for navigation_my_garden
        if (activityBinding.imageButton9 != null) {
            activityBinding.imageButton9.setOnClickListener(v -> navController.navigate(R.id.navigation_plant_wiki)); // Example destination
        }
    }
}
    