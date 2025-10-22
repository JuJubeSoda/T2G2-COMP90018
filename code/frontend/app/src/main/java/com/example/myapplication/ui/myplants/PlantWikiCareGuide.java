package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// 1. Import the binding class for your layout
import com.example.myapplication.databinding.PlantwikiCareguideBinding; // Changed from PlantwikiCareguidetabBinding

public class PlantWikiCareGuide extends Fragment {

    private static final String ARG_PLANT = "plant_object";
    // 2. Declare the correct binding variable
    private PlantwikiCareguideBinding binding;
    private Plant plant;

    /**
     * Creates a new instance of this fragment and passes the Plant object.
     */
    public static PlantWikiCareGuide newInstance(Plant plant) {
        PlantWikiCareGuide fragment = new PlantWikiCareGuide();
        Bundle args = new Bundle();
        // The Plant object implements Parcelable
        args.putParcelable(ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 3. Retrieve the Plant object from the fragment's arguments.
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 4. Inflate the layout using the correct View Binding class
        binding = PlantwikiCareguideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 5. Populate the new TextViews with data from the Plant object
        populateCareGuide();
    }

    private void populateCareGuide() {
        final String PENDING_TEXT = "Pending Information";

        if (plant != null) {
            // Check if we have a comprehensive care guide from the wiki API
            String careGuideText = plant.getCareGuide();
            
            if (careGuideText != null && !careGuideText.isEmpty()) {
                // If we have a care guide, use it for all sections
                // For now, we'll set the same care guide text for all sections
                // In a more sophisticated implementation, you could parse the care guide
                binding.careWateringText.setText(careGuideText);
                binding.careLightText.setText(careGuideText);
                binding.careSoilText.setText(careGuideText);
                binding.careFertilizerText.setText(careGuideText);
            } else {
                // Fall back to individual fields
                // Set Watering Text from waterNeeds
                String wateringInfo = plant.getWaterRequirement();
                binding.careWateringText.setText(wateringInfo != null && !wateringInfo.isEmpty() ? wateringInfo : PENDING_TEXT);

                // Set Light Text from lightNeeds
                String lightInfo = plant.getLightRequirement();
                binding.careLightText.setText(lightInfo != null && !lightInfo.isEmpty() ? lightInfo : PENDING_TEXT);

                // Set Soil Text
                String soilInfo = plant.getSoilGuide();
                binding.careSoilText.setText(soilInfo != null && !soilInfo.isEmpty() ? soilInfo : PENDING_TEXT);

                // Set Fertilizer Text
                String fertilizerInfo = plant.getFertilizerGuide();
                binding.careFertilizerText.setText(fertilizerInfo != null && !fertilizerInfo.isEmpty() ? fertilizerInfo : PENDING_TEXT);
            }
        } else {
            // Fallback in case the plant object itself is null
            binding.careWateringText.setText(PENDING_TEXT);
            binding.careLightText.setText(PENDING_TEXT);
            binding.careSoilText.setText(PENDING_TEXT);
            binding.careFertilizerText.setText(PENDING_TEXT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set binding to null to avoid memory leaks
        binding = null;
    }
}
