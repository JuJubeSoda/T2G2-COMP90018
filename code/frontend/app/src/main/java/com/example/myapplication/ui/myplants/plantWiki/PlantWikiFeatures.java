package com.example.myapplication.ui.myplants.plantWiki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.PlantwikiFeaturestabBinding;
import com.example.myapplication.ui.myplants.share.Plant;

/**
 * PlantWikiFeatures - Features tab showing physical plant characteristics.
 * 
 * Purpose:
 * - Display plant physical features from wiki
 * - Show mature height (from PlantWikiDto.growthHeight)
 * - Show leaf type, toxicity, air purifying ability
 * - Handle missing data with "Pending Information" fallback
 * 
 * User Flow:
 * 1. User navigates to Features tab in PlantWikiMainTabFragment
 * 2. Fragment displays plant physical characteristics
 * 3. Shows "Pending Information" for unavailable data
 * 
 * Key Features:
 * - Mature height display from wiki API
 * - Leaf type classification
 * - Toxicity information for safety
 * - Air purifying capability indicator
 * - Graceful handling of null/empty fields
 * 
 * Data Source:
 * - Plant object passed from PlantWikiMainTabFragment
 * - growthHeight from PlantWikiDto
 * - features field (if available)
 * - Individual feature fields as fallback
 */
public class PlantWikiFeatures extends Fragment {

    private static final String ARG_PLANT = "plant_object";
    
    /** View binding for plantwiki_featurestab.xml layout */
    private PlantwikiFeaturestabBinding binding;
    
    /** Plant data to display */
    private Plant plant;

    /**
     * Factory method to create fragment with Plant data.
     * @param plant Plant object to display
     * @return New instance with Plant as Parcelable argument
     */
    public static PlantWikiFeatures newInstance(Plant plant) {
        PlantWikiFeatures fragment = new PlantWikiFeatures();
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
        binding = PlantwikiFeaturestabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI after view is created.
     * Populates all feature fields from Plant object.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateFeatures();
    }

    /**
     * Populates feature fields with plant data.
     * 
     * Displays:
     * - Mature height (from PlantWikiDto.growthHeight)
     * - Leaf type (if available)
     * - Toxicity information (if available)
     * - Air purifying ability (if available)
     * 
     * Data Priority:
     * 1. If features field exists, show "Pending Information" for individual fields
     * 2. Otherwise, use individual feature fields
     * 3. Show "Pending Information" for null/empty fields
     * 
     * Handles null Plant gracefully with fallback text.
     */
    private void populateFeatures() {
        final String PENDING_TEXT = "Pending Information";

        if (plant != null) {

            // Check if comprehensive features field exists
            String featuresText = plant.getFeatures();
            if (featuresText != null && !featuresText.isEmpty()) {
                // Features field exists but not parsed yet - show pending
                binding.featureLeafTypeText.setText(PENDING_TEXT);
                binding.featureToxicityText.setText(PENDING_TEXT);
                binding.featureAirPurifyingText.setText(PENDING_TEXT);
            } else {
                // Use individual feature fields
                String leafType = plant.getLeafType();
                binding.featureLeafTypeText.setText(leafType != null && !leafType.isEmpty() ? leafType : PENDING_TEXT);

                String toxicity = plant.getToxicity();
                binding.featureToxicityText.setText(toxicity != null && !toxicity.isEmpty() ? toxicity : PENDING_TEXT);

                String airPurifying = plant.getAirPurifying();
                binding.featureAirPurifyingText.setText(airPurifying != null && !airPurifying.isEmpty() ? airPurifying : PENDING_TEXT);
            }
        } else {
            // Handle null Plant with fallback text
            binding.featureLeafTypeText.setText(PENDING_TEXT);
            binding.featureToxicityText.setText(PENDING_TEXT);
            binding.featureAirPurifyingText.setText(PENDING_TEXT);
        }
    }

    /** Cleans up view binding to prevent memory leaks. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
