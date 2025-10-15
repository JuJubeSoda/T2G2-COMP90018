package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.databinding.PlantwikiOverviewtabBinding; // IMPORTANT: This class is auto-generated
import com.example.myapplication.ui.myplants.Plant; // Assuming your Plant model is here

import java.io.Serializable;

/**
 * A fragment that displays the overview tab for a specific plant in the Plant Wiki.
 * It shows a brief description and a grid of key care requirements.
 */
public class PlantWikiOverview extends Fragment {

    private static final String ARG_PLANT = "plant_object";

    private PlantwikiOverviewtabBinding binding; // ViewBinding object
    private Plant plant;

    /**
     * Factory method to create a new instance of this fragment
     * and pass the Plant object as an argument.
     *
     * @param plant The Plant object to display.
     * @return A new instance of fragment PlantWikiOverview.
     */
    public static PlantWikiOverview newInstance(Plant plant) {
        PlantWikiOverview fragment = new PlantWikiOverview();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLANT, (Serializable) plant); // Use Serializable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = (Plant) getArguments().getSerializable(ARG_PLANT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = PlantwikiOverviewtabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Populate the views with data from the Plant object if it exists
        if (plant != null) {
            // Set introduction text
            binding.overviewIntroText.setText(plant.getIntroduction());

            // --- Populate the 2x2 Grid using direct view binding ---
            binding.lightSubtitle.setText(plant.getLightRequirement());
            binding.waterSubtitle.setText(plant.getWaterRequirement());
            binding.temperatureSubtitle.setText(plant.getTemperatureRequirement());
            binding.humiditySubtitle.setText(plant.getHumidityRequirement());
        }

        // --- Set up Click Listeners for the cards and button ---
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Main button to switch to the care guide tab
        binding.viewFullCareGuideButton.setOnClickListener(v -> {
            // Switch to the 3rd tab (index 2)
            switchToCareGuideTab(null);
        });

        // Listeners for each card in the grid
        binding.lightCard.setOnClickListener(v -> {
            // Switch to care tab and indicate which section to show
            switchToCareGuideTab("light");
        });

        binding.waterCard.setOnClickListener(v -> {
            switchToCareGuideTab("water");
        });

        binding.temperatureCard.setOnClickListener(v -> {
            switchToCareGuideTab("temperature");
        });

        binding.humidityCard.setOnClickListener(v -> {
            switchToCareGuideTab("humidity");
        });
    }

    /**
     * Helper method to switch to the Care Guide tab and pass an optional anchor.
     * @param anchor A string key for the section to scroll to (e.g., "light"), or null.
     */
    private void switchToCareGuideTab(String anchor) {
        // Check if the parent is the correct fragment
        if (getParentFragment() instanceof PlantDetailFragment) {
            PlantDetailFragment parent = (PlantDetailFragment) getParentFragment();
            parent.switchToTab(2); // Assuming Care Guide is the 3rd tab (index 2)

            // Optional: If you want to scroll to a specific part of the care guide.
            // You would need to implement `scrollToSection(anchor)` in `PlantWikiCareGuide.java`
            // parent.getCareGuideFragment().scrollToSection(anchor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set binding to null to avoid memory leaks
        binding = null;
    }
}
