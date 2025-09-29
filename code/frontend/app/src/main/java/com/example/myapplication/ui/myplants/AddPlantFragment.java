package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.databinding.AddplantBinding;
import com.example.myapplication.ui.myplants.Plant; // Assuming you have a Plant entity

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddPlantFragment extends Fragment {

    private static final String TAG = "AddPlantFragment";
    private AddplantBinding binding;
    private NavController navController;
    private SearchResultAdapter searchAdapter;

    // This will hold all possible plant names to search against.
    // In a real app, this should be fetched from your backend.
    private final List<String> allPlantNames = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Load the data that the user will be searching through
        loadAllPlantNames();

        setupRecyclerView();
        setupSearchLogic();
        setupBackButton();
    }

    /**
     * Loads the master list of all searchable plant names.
     * TODO: Replace this with a real API call to your backend.
     */
    private void loadAllPlantNames() {
        // Placeholder: generating sample data
        List<Plant> allPlants = generateAllSamplePlants();
        allPlantNames.clear();
        allPlantNames.addAll(
                allPlants.stream()
                        .map(Plant::getName) // Assuming Plant has a getName() method
                        .distinct() // Ensure no duplicate names
                        .collect(Collectors.toList())
        );
        Log.d(TAG, "Loaded " + allPlantNames.size() + " unique plant names for searching.");
    }

    /**
     * Initializes the RecyclerView and its adapter.
     */
    private void setupRecyclerView() {
        // This listener defines what happens when a user clicks on a search result
        searchAdapter = new SearchResultAdapter(scientificName -> {
            Log.d(TAG, "User selected: " + scientificName);

            // Set the text in the search box to the selected item
            binding.searchScientificNameEditText.setText(scientificName);
            // Move cursor to the end
            binding.searchScientificNameEditText.setSelection(scientificName.length());
            // Hide the RecyclerView
            binding.recyclerViewScientificNames.setVisibility(View.GONE);

            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.searchScientificNameEditText.getWindowToken(), 0);
        });

        binding.recyclerViewScientificNames.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewScientificNames.setAdapter(searchAdapter);
    }

    /**
     * Sets up the TextWatcher to listen for user input and trigger filtering.
     */
    private void setupSearchLogic() {
        // Initially, show the prompt and hide the RecyclerView
        binding.textViewSearchStatus.setVisibility(View.VISIBLE);
        binding.recyclerViewScientificNames.setVisibility(View.GONE);

        binding.searchScientificNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // As the user types, filter the list
                filterPlantNames(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters the master list of plant names based on the user's query and updates the UI.
     * @param query The text entered by the user.
     */
    private void filterPlantNames(String query) {
        if (query.isEmpty()) {
            binding.recyclerViewScientificNames.setVisibility(View.GONE);
            binding.textViewSearchStatus.setVisibility(View.VISIBLE);
            searchAdapter.updateData(new ArrayList<>()); // Clear previous results
            return;
        }

        // Filter the master list
        List<String> filteredList = allPlantNames.stream()
                .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        // Update the adapter with the filtered results
        searchAdapter.updateData(filteredList);

        // Update UI visibility based on whether results were found
        if (filteredList.isEmpty()) {
            binding.recyclerViewScientificNames.setVisibility(View.GONE);
            binding.textViewSearchStatus.setText("No results found.");
            binding.textViewSearchStatus.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewScientificNames.setVisibility(View.VISIBLE);
            binding.textViewSearchStatus.setVisibility(View.GONE);
        }
    }

    private void setupBackButton() {
        binding.backButtonAddPlant.setOnClickListener(v -> {
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            }
        });
    }

    // This is a temporary method to provide data. Replace with your actual data source.
    private List<Plant> generateAllSamplePlants() {
        List<Plant> samplePlants = new ArrayList<>();
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;
        samplePlants.add(new Plant("p1", "Monstera Deliciosa", "M. deliciosa", "url_monstera", "A popular houseplant...", now - 5 * oneDay, false));
        samplePlants.add(new Plant("p2", "Snake Plant", "Sansevieria trifasciata", "url_snakeplant", "Hardy plant...", now - 2 * oneDay, true));
        samplePlants.add(new Plant("p3", "Spider Plant", "Chlorophytum comosum", "url_spiderplant", "Easy to grow...", now - 10 * oneDay, false));
        samplePlants.add(new Plant("p4", "Peace Lily", "Spathiphyllum spp.", "url_peacelily", "Features elegant white spathes.", now - 7 * oneDay, true));
        samplePlants.add(new Plant("p5", "Fiddle Leaf Fig", "Ficus lyrata", "url_fiddle", "A popular indoor tree.", now - 30 * oneDay, false));
        return samplePlants;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
