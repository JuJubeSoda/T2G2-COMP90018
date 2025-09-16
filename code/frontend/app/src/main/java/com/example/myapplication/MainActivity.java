// Package declaration for the application.
package com.example.myapplication;
// Android core and UI component imports.
import android.os.Bundle;
// Base class for activities that use the support library action bar features.
import androidx.appcompat.app.AppCompatActivity;
// Component for managing app navigation within a NavHost.
import androidx.navigation.NavController;
// Utility class for finding a NavController.
import androidx.navigation.Navigation;
// Import for View Binding, allows easy access to views defined in the layout.
import com.example.myapplication.databinding.ActivityMainBinding;

/**
 * MainActivity is the primary entry point and main container for the application's UI.
 * It sets up the main layout and handles the primary navigation structure,
 * currently using individual ImageButton clicks to navigate between different fragments.
 */
public class MainActivity extends AppCompatActivity {

    // View Binding instance for accessing views in activity_main.xml.
    private ActivityMainBinding activityBinding;

    /**
     * Called when the activity is first created.
     * This is where you should do all of your normal static set up: create views,
     * bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call the superclass's method first.
        super.onCreate(savedInstanceState);

        // Inflate the layout using View Binding.
        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        // Set the content view to the root of the inflated layout.
        setContentView(activityBinding.getRoot());

        // --- Setup Navigation Controller ---
        // Find the NavController associated with the NavHostFragment in the layout.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // --- Manual Navigation Setup for ImageButtons in BottomAppBar ---
        // The current setup uses individual ImageButtons within a BottomAppBar (or similar structure)
        // to trigger navigation. Click listeners are set on each button to navigate to the
        // respective destination defined in the navigation graph.

        // Setup click listener for the 'Home' ImageButton (assumed to be imageButton6).
        if (activityBinding.imageButton6 != null) {
            activityBinding.imageButton6.setOnClickListener(v -> {
                // Navigate to the home destination (R.id.navigation_home) only if
                // the current destination is not already home. This prevents creating
                // multiple instances of the home screen on the back stack unnecessarily.
                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() != R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home);
                }
            });
        }

        // Setup click listener for the 'Plant Wiki' ImageButton (assumed to be imageButton5).
        // Navigates to the plant wiki destination (R.id.navigation_plant_wiki).
        if (activityBinding.imageButton5 != null) {
            activityBinding.imageButton5.setOnClickListener(v -> navController.navigate(R.id.navigation_plant_wiki));
        }

        // Setup click listener for the 'Plant Map' ImageButton (assumed to be imageButton8).
        // Navigates to the plant map destination (R.id.navigation_plant_map).
        if (activityBinding.imageButton8 != null) {
            activityBinding.imageButton8.setOnClickListener(v -> navController.navigate(R.id.navigation_plant_map));
        }

        // Setup click listener for the 'Profile/My Garden' ImageButton (assumed to be imageButton9).
        // Currently navigates to R.id.navigation_plant_wiki as an example.
        if (activityBinding.imageButton9 != null) {
            activityBinding.imageButton9.setOnClickListener(v -> navController.navigate(R.id.navigation_plant_wiki)); // Example destination
        }
    }
}
