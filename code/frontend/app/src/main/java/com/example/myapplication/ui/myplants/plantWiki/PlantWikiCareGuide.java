package com.example.myapplication.ui.myplants.plantWiki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.PlantwikiCareguideBinding;
import com.example.myapplication.ui.myplants.share.Plant;

/**
 * PlantWikiCareGuide - Care Guide tab showing detailed plant care instructions.
 * 
 * Purpose:
 * - Display comprehensive care guide from wiki
 * - Show watering instructions (from PlantWikiDto.waterNeeds)
 * - Show light requirements (from PlantWikiDto.lightNeeds)
 * - Show soil and fertilizer guidance
 * - Handle missing data with "Pending Information" fallback
 * 
 * User Flow:
 * 1. User navigates to Care Guide tab in PlantWikiMainTabFragment
 * 2. Fragment displays detailed care instructions
 * 3. Shows "Pending Information" for unavailable data
 * 4. User can also access via "View Full Care Guide" button in Overview tab
 * 
 * Key Features:
 * - Comprehensive care guide display
 * - Watering schedule and tips
 * - Light requirement details
 * - Soil type recommendations
 * - Fertilizer application guidance
 * - Graceful handling of null/empty fields
 * 
 * Data Priority:
 * 1. If careGuide field exists, use it for all sections
 * 2. Otherwise, use individual requirement fields
 * 3. Show "Pending Information" for missing data
 * 
 * Data Source:
 * - Plant object passed from PlantWikiMainTabFragment
 * - careGuide from PlantWikiDto (comprehensive)
 * - waterNeeds, lightNeeds as fallback
 * - soilGuide, fertilizerGuide if available
 */
public class PlantWikiCareGuide extends Fragment {

    private static final String ARG_PLANT = "plant_object";
    
    /** View binding for plantwiki_careguide.xml layout */
    private PlantwikiCareguideBinding binding;
    
    /** Plant data to display */
    private Plant plant;

    /**
     * Factory method to create fragment with Plant data.
     * @param plant Plant object to display
     * @return New instance with Plant as Parcelable argument
     */
    public static PlantWikiCareGuide newInstance(Plant plant) {
        PlantWikiCareGuide fragment = new PlantWikiCareGuide();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Fragment creation lifecycle method.
     * Retrieves Plant object from arguments.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }
    }

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantwikiCareguideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI after view is created.
     * Populates all care guide fields from Plant object.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateCareGuide();
    }

    /**
     * Populates care guide fields with plant data.
     * 
     * Displays:
     * - Watering instructions (from PlantWikiDto.waterNeeds or careGuide)
     * - Light requirements (from PlantWikiDto.lightNeeds or careGuide)
     * - Soil recommendations (from soilGuide or careGuide)
     * - Fertilizer guidance (from fertilizerGuide or careGuide)
     * 
     * Data Priority:
     * 1. If comprehensive careGuide exists, use it for all sections
     * 2. Otherwise, use individual requirement fields
     * 3. Show "Pending Information" for null/empty fields
     * 
     * Note: Current implementation uses same careGuide text for all sections.
     * Future enhancement: Parse careGuide to extract section-specific content.
     * 
     * Handles null Plant gracefully with fallback text.
     */
    private void populateCareGuide() {
        final String PENDING_TEXT = "Pending Information";

        if (plant != null) {
            // Check if comprehensive care guide exists
            String wateringText = plant.getWaterRequirement();
            String lightText = plant.getLightRequirement();
            String soilText = plant.getSoilGuide();
            String fertilizerText = plant.getFertilizerGuide();
            
            if (wateringText != null && lightText != null && soilText != null && fertilizerText != null) {
                // Use comprehensive care guide for all sections
                // TODO: Parse careGuide to extract section-specific content
                binding.careWateringText.setText(wateringText);
                binding.careLightText.setText(lightText);
                binding.careSoilText.setText(soilText);
                binding.careFertilizerText.setText(fertilizerText);
            } else {
                // Use individual requirement fields as fallback
                
                // Watering instructions from wiki API
                String wateringInfo = plant.getWaterRequirement();
                binding.careWateringText.setText(wateringInfo != null && !wateringInfo.isEmpty() ? wateringInfo : PENDING_TEXT);

                // Light requirements from wiki API
                String lightInfo = plant.getLightRequirement();
                binding.careLightText.setText(lightInfo != null && !lightInfo.isEmpty() ? lightInfo : PENDING_TEXT);

                // Soil recommendations
                String soilInfo = plant.getSoilGuide();
                binding.careSoilText.setText(soilInfo != null && !soilInfo.isEmpty() ? soilInfo : PENDING_TEXT);

                // Fertilizer guidance
                String fertilizerInfo = plant.getFertilizerGuide();
                binding.careFertilizerText.setText(fertilizerInfo != null && !fertilizerInfo.isEmpty() ? fertilizerInfo : PENDING_TEXT);
            }
        } else {
            // Handle null Plant with fallback text
            binding.careWateringText.setText(PENDING_TEXT);
            binding.careLightText.setText(PENDING_TEXT);
            binding.careSoilText.setText(PENDING_TEXT);
            binding.careFertilizerText.setText(PENDING_TEXT);
        }
    }

    /** Cleans up view binding to prevent memory leaks. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
