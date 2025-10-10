package com.example.myapplication.ui.myplants; // Assuming it resides here, adjust if needed.

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantwikiBinding; // Correct binding class for plantwiki.xml

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PlantWikiFragment displays a searchable encyclopedia of all known plants.
 * It allows users to browse plants in a grid or list format and search for specific ones.
 * Clicking on a plant will navigate to its detail page.
 */
public class PlantWikiFragment extends Fragment {

    private static final String TAG = "PlantWikiFragment";
    private PlantwikiBinding binding; // Binding class generated from plantwiki.xml
    private PlantCardAdapter plantCardAdapter;

    // This list holds all plants available in the wiki.
    // In a real app, this data would come from a remote database/API.
    private final List<Plant> allWikiPlants = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = PlantwikiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load the master data for the wiki
        generateAllWikiPlants();

        // Setup the UI components
        setupRecyclerView();
        setupSearch();
        // setupViewToggles(); // This is commented out as the toggles are not in the final XML

        // Initial display of all plants
        displayFilteredPlants("");
    }

    /**
     * Initializes the RecyclerView, its adapter, and the click listener for navigation.
     */
    private void setupRecyclerView() {
        if (binding.recyclerViewPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewPlants is NULL!");
            return;
        }

        // The adapter's click listener will navigate to the PlantDetailFragment
        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Log.d(TAG, "Wiki plant clicked: " + plant.getName() + ". Navigating to details.");

            // Create a bundle to pass the selected Plant object
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);

            try {
                // Navigate to the detail screen.
                // NOTE: This assumes a global action or an action from the wiki fragment exists.
                // You might need to add <action android:id="@+id/action_plantWikiFragment_to_plantDetailFragment" ... />
                // to your mobile_navigation.xml inside the wiki fragment definition.
                // TODO: Replace this with Wiki View
                Navigation.findNavController(requireView()).navigate(R.id.plantDetailFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to PlantDetailFragment from Wiki failed.", e);
                Toast.makeText(getContext(), "Could not open plant details.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the adapter and default layout manager (the XML already defines a GridLayoutManager)
        binding.recyclerViewPlants.setAdapter(plantCardAdapter);
        binding.recyclerViewPlants.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Default to grid view
    }

    /**
     * Sets up the search logic for the EditText field.
     */
    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the list in real-time as the user types
                displayFilteredPlants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle the "Search" button on the keyboard
        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                displayFilteredPlants(v.getText().toString());
                return true;
            }
            return false;
        });
    }

    /**
     * Filters the master plant list based on a query and updates the RecyclerView.
     * @param query The search text entered by the user.
     */
    private void displayFilteredPlants(String query) {
        List<Plant> filteredList;

        if (query.trim().isEmpty()) {
            // If query is empty, show all plants
            filteredList = new ArrayList<>(allWikiPlants);
        } else {
            // Otherwise, filter by name or scientific name (case-insensitive)
            String lowerCaseQuery = query.toLowerCase();
            filteredList = allWikiPlants.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerCaseQuery) ||
                            p.getScientificName().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        // Update the adapter with the new list
        plantCardAdapter.setPlants(new ArrayList<>(filteredList));
    }

    /**
     * Generates a sample list of plants for the wiki.
     * TODO: Replace this with a real API call to fetch all plants from your backend.
     */
    private void generateAllWikiPlants() {
        if (!allWikiPlants.isEmpty()) return; // Generate only once

        Log.d(TAG, "Generating new sample plants for the wiki.");
        long now = System.currentTimeMillis();

        // Create a more extensive list for the wiki
        allWikiPlants.add(new Plant("p1", "Monstera deliciosa", "Monstera", "A popular houseplant with iconic fenestrated leaves.", "Tropical Rainforests", "Houseplant, Tropical", "Botanist", now, "url_monstera", false));
        allWikiPlants.add(new Plant("p2", "Dracaena trifasciata", "Snake Plant", "A very hardy plant known for its air-purifying qualities.", "West Africa", "Succulent, Hardy, Air-purifying", "Botanist", now, "url_snakeplant", false));
        allWikiPlants.add(new Plant("p3", "Chlorophytum comosum", "Spider Plant", "Easy to grow and known for producing 'spiderettes' or plantlets.", "South Africa", "Easy Care, Hanging Plant", "Botanist", now, "url_spiderplant", false));
        allWikiPlants.add(new Plant("p4", "Spathiphyllum wallisii", "Peace Lily",  "Features elegant white spathes and thrives in low light.", "South America", "Flowering, Air Purifying, Low Light", "Botanist", now, "url_peacelily", false));
        allWikiPlants.add(new Plant("p5", "Ficus lyrata", "Fiddle Leaf Fig", "A popular indoor tree with large, violin-shaped leaves.", "West Africa", "Tree, Fussy", "Botanist", now, "url_fiddle", false));
        allWikiPlants.add(new Plant("p6", "Rosa", "Rose", "A woody perennial flowering plant of the genus Rosa, in the family Rosaceae.", "Global", "Flower, Shrub, Garden", "Botanist", now, "url_rose", false));
        allWikiPlants.add(new Plant("p7", "Quercus","Oak Tree", "A tree or shrub in the genus Quercus of the beech family, Fagaceae.", "Northern Hemisphere", "Tree, Deciduous, Hardwood", "Botanist", now, "url_oak", false));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify the binding object to avoid memory leaks
        binding = null;
        Log.d(TAG, "onDestroyView: PlantWikiFragment binding nulled.");
    }
}
