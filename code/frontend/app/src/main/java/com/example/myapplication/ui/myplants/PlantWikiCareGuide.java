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

import java.io.Serializable;

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
        args.putSerializable(ARG_PLANT, (Serializable) plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 3. Retrieve the Plant object from the fragment's arguments.
        if (getArguments() != null) {
            Serializable serializable = getArguments().getSerializable(ARG_PLANT);
            if (serializable instanceof Plant) {
                plant = (Plant) serializable;
            }
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
            // --- THIS IS THE FIX ---
            // Set the text for each specific care guide section.
            // This assumes you have added corresponding getter methods to your Plant.java class.

            // Set Watering Text
            // NOTE: Using getWaterRequirement() as a proxy. Ideally, you'd have a specific getWateringGuide() method.
            String wateringInfo = plant.getWaterRequirement();
            binding.careWateringText.setText(wateringInfo != null && !wateringInfo.isEmpty() ? wateringInfo : PENDING_TEXT);

            // Set Light Text
            // NOTE: Using getLightRequirement() as a proxy.
            String lightInfo = plant.getLightRequirement();
            binding.careLightText.setText(lightInfo != null && !lightInfo.isEmpty() ? lightInfo : PENDING_TEXT);

            // Set Soil Text (Assuming you add getSoilGuide() to Plant.java)
            String soilInfo = plant.getSoilGuide();
            binding.careSoilText.setText(soilInfo != null && !soilInfo.isEmpty() ? soilInfo : PENDING_TEXT);
            // For now, let's set it to pending as the field doesn't exist yet
//            binding.careSoilText.setText(PENDING_TEXT);


            // Set Fertilizer Text (Assuming you add getFertilizerGuide() to Plant.java)
            String fertilizerInfo = plant.getFertilizerGuide();
            binding.careFertilizerText.setText(fertilizerInfo != null && !fertilizerInfo.isEmpty() ? fertilizerInfo : PENDING_TEXT);
            // For now, let's set it to pending as the field doesn't exist yet
//            binding.careFertilizerText.setText(PENDING_TEXT);

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
