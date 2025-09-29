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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.databinding.AddplantBinding;
import com.example.myapplication.ui.myplants.Plant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddPlantFragment extends Fragment {

    private static final String TAG = "AddPlantFragment";
    private AddplantBinding binding;
    private NavController navController;
    private SearchResultAdapter searchAdapter;

    // This will hold all possible plant names to search against.
    private final List<String> allPlantNames = new ArrayList<>();

    // --- MODIFICATION 1: Field to hold the isFavouriteFlow flag ---
    private boolean isFavouriteFlow = false;

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

        // --- MODIFICATION 2: Receive the isFavouriteFlow argument ---
        if (getArguments() != null) {
            isFavouriteFlow = getArguments().getBoolean("isFavouriteFlow", false);
            Log.d(TAG, "Received isFavouriteFlow: " + isFavouriteFlow);
        }

        loadAllPlantNames();
        setupRecyclerView();
        setupSearchLogic();
        setupBackButton();
        setupNextButton();
    }

    /**
     * Loads the master list of all searchable plant names.
     */
    private void loadAllPlantNames() {
        // Placeholder: generating sample data
        List<Plant> allPlants = generateAllSamplePlants();
        allPlantNames.clear();
        allPlantNames.addAll(
                allPlants.stream()
                        .map(Plant::getScientificName) // Search by scientific name now
                        .distinct()
                        .collect(Collectors.toList())
        );
        Log.d(TAG, "Loaded " + allPlantNames.size() + " unique plant names for searching.");
    }

    /**
     * Initializes the RecyclerView and its adapter.
     */
    private void setupRecyclerView() {
        searchAdapter = new SearchResultAdapter(scientificName -> {
            Log.d(TAG, "User selected: " + scientificName);

            binding.searchScientificNameEditText.setText(scientificName);
            binding.searchScientificNameEditText.setSelection(scientificName.length());
            binding.recyclerViewScientificNames.setVisibility(View.GONE);

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
        binding.textViewSearchStatus.setText("Search for a plant by its scientific name.");
        binding.textViewSearchStatus.setVisibility(View.VISIBLE);
        binding.recyclerViewScientificNames.setVisibility(View.GONE);

        binding.searchScientificNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlantNames(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters the master list of plant names based on the user's query.
     */
    private void filterPlantNames(String query) {
        if (query.isEmpty()) {
            binding.recyclerViewScientificNames.setVisibility(View.GONE);
            binding.textViewSearchStatus.setVisibility(View.VISIBLE);
            searchAdapter.updateData(new ArrayList<>());
            return;
        }

        List<String> filteredList = allPlantNames.stream()
                .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        searchAdapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            binding.recyclerViewScientificNames.setVisibility(View.GONE);
            binding.textViewSearchStatus.setText("No results found.");
            binding.textViewSearchStatus.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewScientificNames.setVisibility(View.VISIBLE);
            binding.textViewSearchStatus.setVisibility(View.GONE);
        }
    }

    /**
     * --- MODIFICATION 3: Pass both scientificName and isFavouriteFlow ---
     * This will navigate to the CaptureFragment, passing all necessary data.
     */
    private void setupNextButton() {
        binding.imageView.setOnClickListener(v -> {
            String scientificName = binding.searchScientificNameEditText.getText().toString().trim();

            if (scientificName.isEmpty()) {
                binding.searchScientificNameEditText.setError("Please enter or select a name");
                return;
            }

            Log.d(TAG, "Navigating to CaptureFragment with scientific name: " + scientificName + " and isFavouriteFlow: " + isFavouriteFlow);

            // Create a bundle to pass all necessary data
            Bundle args = new Bundle();
            args.putString("scientificName", scientificName);
            args.putBoolean("isFavouriteFlow", isFavouriteFlow);

            try {
                // Navigate to the CaptureFragment with the arguments.
                // Ensure this action exists in your navigation graph.
                navController.navigate(R.id.navigation_upload, args);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Navigation failed. Check action 'action_addPlantFragment_to_captureFragment' in your nav graph.", e);
                Toast.makeText(getContext(), "Error: Cannot proceed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBackButton() {
        binding.backButtonAddPlant.setOnClickListener(v -> {
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            }
        });
    }

    /**
     * --- MODIFICATION 4: Update Plant constructor to match new fields ---
     * This is a temporary method to provide data. Replace with your actual data source.
     */
    private List<Plant> generateAllSamplePlants() {
        List<Plant> samplePlants = new ArrayList<>();
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;

        // Using the new Plant constructor
        samplePlants.add(new Plant("p1", "Monstera", "Monstera deliciosa", "A popular houseplant with iconic leaves.", "Indoor", "Houseplant, Tropical", "UserA", now - 5 * oneDay, "url_monstera", false));
        samplePlants.add(new Plant("p2", "Snake Plant", "Dracaena trifasciata", "A very hardy plant.", "Indoor", "Succulent, Hardy", "UserB", now - 2 * oneDay, "url_snakeplant", true));
        samplePlants.add(new Plant("p3", "Spider Plant", "Chlorophytum comosum", "Produces small 'spiderettes'.", "Indoor", "Easy Care", "UserA", now - 10 * oneDay, "url_spiderplant", false));
        samplePlants.add(new Plant("p4", "Peace Lily", "Spathiphyllum wallisii", "Features elegant white spathes.", "Indoor", "Flowering, Air Purifying", "UserC", now - 7 * oneDay, "url_peacelily", true));
        samplePlants.add(new Plant("p5", "Fiddle Leaf Fig", "Ficus lyrata", "A popular but tricky indoor tree.", "Indoor", "Tree, Fussy", "UserB", now - 30 * oneDay, "url_fiddle", false));

        return samplePlants;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
