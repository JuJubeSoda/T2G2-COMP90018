package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// 1. Import the binding class for your layout
import com.example.myapplication.databinding.PlantwikiFeaturestabBinding;

import java.io.Serializable;

public class PlantWikiFeatures extends Fragment {

    private static final String ARG_PLANT = "plant_object";
    // 2. Declare a binding variable
    private PlantwikiFeaturestabBinding binding;
    private Plant plant;

    /**
     * Creates a new instance of this fragment and passes the Plant object.
     * This is the recommended way to pass arguments to a fragment.
     */
    public static PlantWikiFeatures newInstance(Plant plant) {
        PlantWikiFeatures fragment = new PlantWikiFeatures();
        Bundle args = new Bundle();
        // The Plant object must implement Serializable to be passed this way
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
        // 4. Inflate the layout using View Binding instead of the constructor
        binding = PlantwikiFeaturestabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 5. Populate the TextViews with data from the Plant object
        populateFeatures();
    }

    private void populateFeatures() {
        final String PENDING_TEXT = "Pending Information";

        if (plant != null) {
            // --- THIS IS THE FIX ---
            // For each feature, check if the data exists. If not, show the pending text.

            // Set Mature Height
            String height = plant.getMatureHeight();
            binding.featureHeightText.setText(height != null && !height.isEmpty() ? height : PENDING_TEXT);

            // Set Leaf Type
            String leafType = plant.getLeafType();
            binding.featureLeafTypeText.setText(leafType != null && !leafType.isEmpty() ? leafType : PENDING_TEXT);

            // Set Toxicity
            String toxicity = plant.getToxicity();
            binding.featureToxicityText.setText(toxicity != null && !toxicity.isEmpty() ? toxicity : PENDING_TEXT);

            // Set Air Purifying ability
            String airPurifying = plant.getAirPurifying();
            binding.featureAirPurifyingText.setText(airPurifying != null && !airPurifying.isEmpty() ? airPurifying : PENDING_TEXT);
        } else {
            // Fallback in case the plant object itself is null
            binding.featureHeightText.setText(PENDING_TEXT);
            binding.featureLeafTypeText.setText(PENDING_TEXT);
            binding.featureToxicityText.setText(PENDING_TEXT);
            binding.featureAirPurifyingText.setText(PENDING_TEXT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set binding to null to avoid memory leaks
        binding = null;
    }
}
