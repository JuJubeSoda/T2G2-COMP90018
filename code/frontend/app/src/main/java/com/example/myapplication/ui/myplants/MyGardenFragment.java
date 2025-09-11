package com.example.myapplication.ui.myplants; // Ensure this is your correct package

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager; // For RecyclerView
import androidx.recyclerview.widget.RecyclerView;    // For RecyclerView

import com.example.myapplication.R;
// Binding class for mycollectiongrid.xml
import com.example.myapplication.databinding.MygardengridBinding;

// *** ADJUST THESE IMPORTS TO YOUR ACTUAL PROJECT STRUCTURE ***
import com.example.myapplication.ui.myplants.PlantCardAdapter; // Example
import com.example.yourapp.model.Plant;

import java.util.ArrayList;
import java.util.List;

public class MyGardenFragment extends Fragment {

    // Binding for mycollectiongrid.xml which NOW includes the RecyclerView
    private MygardengridBinding binding;

    private PlantCardAdapter plantCardAdapter;
    private ArrayList<Plant> allSamplePlants = new ArrayList<>(); // Stores all generated sample plants
    private boolean currentViewIsFavourites = false; // To track current filter state

    public static MyGardenFragment newInstance() {
        return new MyGardenFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Generate all sample plants once when the fragment is created
        generateAllSamplePlants();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("MyGardenFragment", "onCreateView CALLED - Inflating mycollectiongrid.xml");
        binding = MygardengridBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("MyGardenFragment", "onViewCreated CALLED for mycollectiongrid.xml");

        // --- Setup UI elements from mycollectiongrid.xml ---
        if (binding.pageTitleAddPlant != null) {
            binding.pageTitleAddPlant.setText("My Garden");
        }

        if (binding.imageButton != null) {
            binding.imageButton.setOnClickListener(v -> Toast.makeText(getContext(), "List View Clicked (Not Implemented)", Toast.LENGTH_SHORT).show());
        }
        if (binding.imageButton2 != null) {
            binding.imageButton2.setOnClickListener(v -> Toast.makeText(getContext(), "Grid View Clicked (Not Implemented)", Toast.LENGTH_SHORT).show());
        }

        if (binding.searchScientificNameEditText != null) {
            binding.searchScientificNameEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchQuery = v.getText().toString().trim();
                    Toast.makeText(getContext(), "Searching for: " + searchQuery, Toast.LENGTH_SHORT).show();
                    // TODO: Implement actual search filtering on 'allSamplePlants' and refresh adapter
                    return true;
                }
                return false;
            });
        }

        // --- Setup RecyclerView DIRECTLY in this fragment ---
        setupRecyclerView(); // Call the new setup method

        if (binding.button2 != null) { // "My Collection"
            binding.button2.setOnClickListener(v -> {
                Log.d("MyGardenFragment", "My Collection button clicked.");
                currentViewIsFavourites = false;
                displayPlants(); // Refresh the RecyclerView
            });
        }

        if (binding.button3 != null) { // "My Favourite"
            binding.button3.setOnClickListener(v -> {
                Log.d("MyGardenFragment", "My Favourite button clicked.");
                currentViewIsFavourites = true;
                displayPlants(); // Refresh the RecyclerView
            });
        }

        if (binding.imageButton4 != null) { // "Add Plant"
            binding.imageButton4.setOnClickListener(v -> {
                Log.d("MyGardenFragment", "Add Plant button clicked.");
                Toast.makeText(getContext(), "Add Plant Clicked (Navigate to Add Screen)", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to your "Add Plant" screen/fragment
            });
        }

        // Load the "My Collection" view by default
        if (savedInstanceState == null) {
            Log.i("MyGardenFragment", "Initial load: Displaying All Collection.");
            currentViewIsFavourites = false;
            displayPlants(); // Display initial data
        }
    }

    private void setupRecyclerView() {
        // *** Access RecyclerView directly from binding of mycollectiongrid.xml ***
        if (binding.recyclerViewMyGardenPlants == null) {
            Log.e("MyGardenFragment", "CRITICAL: recyclerViewMyGardenPlants is NULL in binding! Check ID in mycollectiongrid.xml.");
            return;
        }
        binding.recyclerViewMyGardenPlants.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        // Initialize adapter with an empty list; data will be set in displayPlants()
        plantCardAdapter = new PlantCardAdapter(new ArrayList<>(), plant -> {
            Toast.makeText(getContext(), "Clicked on: " + plant.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Handle plant card click (e.g., navigate to detail screen)
        });
        binding.recyclerViewMyGardenPlants.setAdapter(plantCardAdapter);
        Log.d("MyGardenFragment", "RecyclerView setup complete directly in MyGardenFragment.");
    }

    private void generateAllSamplePlants() {
        if (!allSamplePlants.isEmpty()) return; // Generate only once

        Log.d("MyGardenFragment", "Generating all sample plant data.");
        // Parameters: id, name, scientificName, imageUrl, description, dateAdded, isFavourite
        allSamplePlants.add(new Plant("p1", "Monstera Deliciosa", "Monstera deliciosa", "https://via.placeholder.com/150/771796", "Loves humidity", System.currentTimeMillis(), false));
        allSamplePlants.add(new Plant("p2", "Snake Plant", "Sansevieria trifasciata", "https://via.placeholder.com/150/24f355", "Drought tolerant", System.currentTimeMillis(), true));
        allSamplePlants.add(new Plant("p3", "Pothos", "Epipremnum aureum", "https://via.placeholder.com/150/d32776", "Easy to grow", System.currentTimeMillis(), false));
        allSamplePlants.add(new Plant("p4", "ZZ Plant", "Zamioculcas zamiifolia", "https://via.placeholder.com/150/f66b97", "Low light ok", System.currentTimeMillis(), true));
        allSamplePlants.add(new Plant("p5", "Fiddle Leaf Fig", "Ficus lyrata", "https://via.placeholder.com/150/56a8c2", "Needs bright light", System.currentTimeMillis(), false));
        allSamplePlants.add(new Plant("p6", "Spider Plant", "Chlorophytum comosum", "https://via.placeholder.com/150/b0f7cc", "Produces plantlets", System.currentTimeMillis(), true));
    }

    private void displayPlants() {
        if (plantCardAdapter == null) {
            Log.e("MyGardenFragment", "plantCardAdapter is null in displayPlants. Cannot update UI.");
            return;
        }

        ArrayList<Plant> listToDisplay = new ArrayList<>();
        if (currentViewIsFavourites) {
            Log.d("MyGardenFragment", "Filtering for Favourites. Total sample plants: " + allSamplePlants.size());
            for (Plant p : allSamplePlants) {
                if (p.isFavourite()) {
                    listToDisplay.add(p);
                }
            }
        } else {
            Log.d("MyGardenFragment", "Displaying All Collection. Total sample plants: " + allSamplePlants.size());
            listToDisplay.addAll(allSamplePlants);
        }

        plantCardAdapter.setPlants(listToDisplay); // Make sure PlantCardAdapter has this method
        Log.d("MyGardenFragment", "Adapter updated with " + listToDisplay.size() + " plants.");

        updateEmptyViewVisibility(listToDisplay.isEmpty());
    }

    private void updateEmptyViewVisibility(boolean isEmpty) {
        // *** Access views directly from binding of mycollectiongrid.xml ***
        if (binding.textViewMyGardenEmptyMessage == null || binding.recyclerViewMyGardenPlants == null) {
            Log.w("MyGardenFragment", "Cannot update empty view visibility, required views are null in binding. Check IDs in mycollectiongrid.xml.");
            return;
        }

        if (isEmpty) {
            binding.recyclerViewMyGardenPlants.setVisibility(View.GONE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.VISIBLE);
            binding.textViewMyGardenEmptyMessage.setText(currentViewIsFavourites ? "No favourites yet!" : "Your collection is empty.");
        } else {
            binding.recyclerViewMyGardenPlants.setVisibility(View.VISIBLE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("MyGardenFragment", "onDestroyView CALLED for mycollectiongrid.xml");
        // Important to nullify the binding object
        binding.recyclerViewMyGardenPlants.setAdapter(null); // Recommended to clear adapter
        binding = null;
    }
}
