package com.example.myapplication.ui.myplants.plantWiki;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantwikiMaintabBinding;
import com.example.myapplication.ui.myplants.share.Plant;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * PlantWikiMainTabFragment - Container for Plant Wiki detail tabs with header.
 * 
 * Purpose:
 * - Display plant header (image, name, scientific name)
 * - Host tabbed interface for plant details
 * - Coordinate between three tab fragments
 * - Provide tab switching functionality
 * 
 * User Flow:
 * 1. User clicks plant in PlantWikiFragment
 * 2. Fragment receives Plant object as argument
 * 3. Displays header with plant image and names
 * 4. Shows three tabs: Overview, Features, Care Guide
 * 5. User can swipe or tap to switch tabs
 * 
 * Tab Structure:
 * - Tab 0: PlantWikiOverview - Description, requirements, sensor data
 * - Tab 1: PlantWikiFeatures - Physical characteristics, mature height
 * - Tab 2: PlantWikiCareGuide - Detailed care instructions
 * 
 * Key Features:
 * - Header with large plant image
 * - Common name and scientific name display
 * - ViewPager2 for smooth tab swiping
 * - TabLayout integration with ViewPager2
 * - Base64 image handling with error fallback
 * - Public tab switching method for child fragments
 * 
 * Arguments:
 * - ARG_PLANT: Plant object (Parcelable) from PlantWikiFragment
 * 
 * Navigation:
 * - From: PlantWikiFragment (wiki plant tile click)
 * - Contains: PlantWikiOverview, PlantWikiFeatures, PlantWikiCareGuide
 */
public class PlantWikiMainTabFragment extends Fragment {

    /** Argument key for Plant object */
    public static final String ARG_PLANT = "plant_argument";
    private static final String TAG = "PlantWikiMainTab";

    /** View binding for plantwiki_maintab.xml layout */
    private PlantwikiMaintabBinding binding;
    
    /** Plant data to display across header and tabs */
    private Plant plant;

    /**
     * Fragment creation lifecycle method.
     * Retrieves Plant object from arguments and validates it.
     * Navigates back if Plant is null (error state).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get Plant object from arguments
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }
        
        // Handle missing Plant data
        if (plant == null) {
            Log.e(TAG, "Plant object is null. Cannot display wiki details.");
            Toast.makeText(getContext(), "Error: Wiki data not found.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
        }
    }

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantwikiMaintabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI after view is created.
     * 
     * Initialization Order:
     * 1. Populate header with plant data
     * 2. Setup ViewPager2 with TabsPagerAdapter
     * 3. Connect TabLayout to ViewPager2
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (plant == null) return;

        populateHeaderData();
        setupViewPagerAndTabs();
    }

    /**
     * Populates header section with plant data.
     * 
     * Displays:
     * - Plant common name
     * - Scientific name
     * - Large header image (Base64 decoded)
     * 
     * Image Handling:
     * - Checks for null/empty Base64 string
     * - Decodes Base64 to byte array
     * - Uses Glide for efficient loading
     * - Shows placeholder on error
     */
    private void populateHeaderData() {
        binding.plantNameText.setText(plant.getName());
        binding.plantNicknameText.setText(plant.getScientificName());

        String base64Image = plant.getImageUrl();

        // Handle Base64 image with robust error handling
        if (TextUtils.isEmpty(base64Image)) {
            // No image data - show placeholder
            Log.w(TAG, "Image data is missing for " + plant.getName() + ". Loading default placeholder.");
            Glide.with(this)
                    .load(R.drawable.plantbulb_foreground)
                    .into(binding.landingimage);
        } else {
            // Decode and load Base64 image
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Glide.with(this)
                        .load(imageBytes)
                        .placeholder(R.drawable.plantbulb_foreground)
                        .error(R.drawable.plantbulb_foreground)
                        .into(binding.landingimage);
            } catch (Exception e) {
                // Decoding failed - show placeholder
                Log.e(TAG, "Failed to decode or load Base64 image for " + plant.getName(), e);
                Glide.with(this)
                        .load(R.drawable.plantbulb_foreground)
                        .into(binding.landingimage);
            }
        }
    }

    /**
     * Sets up ViewPager2 with tabs for plant details.
     * 
     * Setup:
     * - Creates TabsPagerAdapter with Plant data
     * - Connects ViewPager2 to adapter
     * - Links TabLayout to ViewPager2 with TabLayoutMediator
     * - Sets tab titles: Overview, Features, Care Guide
     * 
     * Tab Behavior:
     * - User can swipe between tabs
     * - User can tap tab titles to switch
     * - Smooth animations between tabs
     */
    private void setupViewPagerAndTabs() {
        // Create adapter with Plant data for all tabs
        TabsPagerAdapter pagerAdapter = new TabsPagerAdapter(getChildFragmentManager(), getLifecycle(), plant);
        binding.viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout to ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Overview"); break;
                case 1: tab.setText("Features"); break;
                case 2: tab.setText("Care Guide"); break;
            }
        }).attach();
    }

    /**
     * Programmatically switches to specified tab.
     * Can be called by child fragments to navigate between tabs.
     * 
     * @param tabIndex Tab position (0=Overview, 1=Features, 2=Care Guide)
     */
    public void switchToTab(int tabIndex) {
        if (binding != null && binding.viewPager.getAdapter() != null && tabIndex < binding.viewPager.getAdapter().getItemCount()) {
            binding.viewPager.setCurrentItem(tabIndex, true); // true = smooth scroll
        }
    }

    /**
     * Cleans up view binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
