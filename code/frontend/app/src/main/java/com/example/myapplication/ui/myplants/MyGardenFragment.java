package com.example.myapplication.ui.myplants;

import android.os.Bundle;
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
// Remove import for MycollectiongridBinding if your XML is now mygardenlist.xml
// import com.example.myapplication.databinding.MycollectiongridBinding;
import com.example.myapplication.databinding.MygardenlistBinding; // Assuming this matches your XML file name
import com.example.myapplication.R;
import com.example.myapplication.adapter.PlantCardAdapter; // Your updated adapter
import com.example.myapplication.ui.myplants.Plant;

import java.util.ArrayList;
import java.util.List;

public class MyGardenFragment extends Fragment {

    // Update this binding variable if your XML file is named 'mygardenlist.xml'
    private MygardenlistBinding binding; // Or MycollectiongridBinding if that's still the main file name

    private PlantCardAdapter plantCardAdapter;
    private ArrayList<Plant> allSamplePlants = new ArrayList<>();
    private boolean currentViewIsFavourites = false;
    private boolean isCurrentlyListView = false;

    public static MyGardenFragment newInstance() {
        return new MyGardenFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generateAllSamplePlants();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Ensure this inflate matches your main fragment XML file name (e.g., mygardenlist.xml)
        binding = MygardenlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("MyGardenFragment", "onViewCreated");

        if (binding.pageTitleAddPlant != null) {
            binding.pageTitleAddPlant.setText(getString(R.string.my_garden_title));
        }

        setupRecyclerView();

        // --- View Toggle Buttons ---
        // Assuming IDs imageButton (list) and imageButton2 (grid) are in your fragment's XML
        if (binding.imageButton != null) {
            binding.imageButton.setOnClickListener(v -> {
                if (!isCurrentlyListView) {
                    switchToListView();
                }
            });
        }
        if (binding.imageButton2 != null) {
            binding.imageButton2.setOnClickListener(v -> {
                if (isCurrentlyListView) {
                    switchToGridView(); // Or switchToCardView if that's more accurate
                }
            });
        }
        updateToggleButtonsVisualState();

        // --- Search, Collection/Favourite, Add Plant buttons setup (remains the same if IDs are consistent) ---
        if (binding.searchScientificNameEditText != null) {
            binding.searchScientificNameEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) { /* ... search ... */ return true; }
                return false;
            });
        }
        if (binding.button2 != null) { /* My Collection click */
            binding.button2.setOnClickListener(v -> {
                currentViewIsFavourites = false;
                displayPlants();
            });
        }
        if (binding.button3 != null) { /* My Favourite click */
            binding.button3.setOnClickListener(v -> {
                currentViewIsFavourites = true;
                displayPlants();
            });
        }
        if (binding.imageButton4 != null) { /* Add Plant click */
            binding.imageButton4.setOnClickListener(v -> Toast.makeText(getContext(), "Add Plant Clicked", Toast.LENGTH_SHORT).show());
        }


        if (savedInstanceState == null) {
            currentViewIsFavourites = false;
            switchToGridView(); // Default to grid view
            displayPlants();
        }

        if (binding.imageButton4 != null) { // Assuming imageButton4 is your "Add Plant" button
            binding.imageButton4.setOnClickListener(v -> {
                Log.d("MyGardenFragment", "Add Plant button clicked, attempting to navigate.");
                NavController navController = Navigation.findNavController(v);
                try {
                    // Use the action ID defined in your mobile_navigation.xml
                    navController.navigate(R.id.action_myGardenFragment_to_addPlantFragment);
                } catch (IllegalArgumentException e) {
                    Log.e("MyGardenFragment", "Navigation action ID not found. Check mobile_navigation.xml: " + e.getMessage());
                    Toast.makeText(getContext(), "Error: Could not navigate to Add Plant screen.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setupRecyclerView() {
        // Use the RecyclerView ID from your fragment's XML (e.g., mygardenlist.xml)
        if (binding.recyclerViewMyGardenPlants == null) {
            Log.e("MyGardenFragment", "CRITICAL: recyclerViewMyGardenPlants is NULL!");
            return;
        }
        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Toast.makeText(getContext(), "Clicked: " + plant.getName(), Toast.LENGTH_SHORT).show();
        });
        binding.recyclerViewMyGardenPlants.setAdapter(plantCardAdapter);
        // Initial layout set by switchToGridView()
    }

    private void switchToListView() {
        if (binding.recyclerViewMyGardenPlants == null || plantCardAdapter == null) return;
        binding.recyclerViewMyGardenPlants.setLayoutManager(new LinearLayoutManager(getContext()));
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_LIST_WITH_DATE); // Use the new type
        isCurrentlyListView = true;
        updateToggleButtonsVisualState();
        Log.d("MyGardenFragment", "Switched to List View with Date (Modified Card)");
    }

    private void switchToGridView() { // Renamed from switchToCardView for consistency if using GridLayoutManager
        if (binding.recyclerViewMyGardenPlants == null || plantCardAdapter == null) return;
        binding.recyclerViewMyGardenPlants.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns for grid
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_GRID); // Back to default grid type
        isCurrentlyListView = false;
        updateToggleButtonsVisualState();
        Log.d("MyGardenFragment", "Switched to Grid View");
    }

    private void updateToggleButtonsVisualState() {
        if (binding.imageButton == null || binding.imageButton2 == null) return;
        if (isCurrentlyListView) {
            binding.imageButton.setImageResource(R.drawable.list_select_foreground);
            binding.imageButton2.setImageResource(R.drawable.grid_unselect_foreground);
        } else {
            binding.imageButton.setImageResource(R.drawable.list_unselect_foreground);
            binding.imageButton2.setImageResource(R.drawable.grid_select_foreground);
        }
    }

    private void generateAllSamplePlants() {
        if (!allSamplePlants.isEmpty()) return;
        Log.d("MyGardenFragment", "Generating sample plants.");
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;
        // Ensure your Plant constructor can take these parameters
        allSamplePlants.add(new Plant("p1", "Monstera", "M. deliciosa", "url", "Desc", now - 5 * oneDay, false));
        allSamplePlants.add(new Plant("p2", "Snake Plant", "S. trifasciata", "url", "Desc", now - 2 * oneDay, true));
        // Add more plants
    }

    private void displayPlants() {
        if (plantCardAdapter == null) return;
        ArrayList<Plant> listToDisplay = new ArrayList<>();
        if (currentViewIsFavourites) {
            for (Plant p : allSamplePlants) if (p.isFavourite()) listToDisplay.add(p);
        } else {
            listToDisplay.addAll(allSamplePlants);
        }
        plantCardAdapter.setPlants(listToDisplay);
        updateEmptyViewVisibility(listToDisplay.isEmpty());
    }

    private void updateEmptyViewVisibility(boolean isEmpty) {
        if (binding.textViewMyGardenEmptyMessage == null || binding.recyclerViewMyGardenPlants == null) return;
        if (isEmpty) {
            binding.recyclerViewMyGardenPlants.setVisibility(View.GONE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.VISIBLE);
            binding.textViewMyGardenEmptyMessage.setText(currentViewIsFavourites ? R.string.no_favourites_yet : R.string.collection_is_empty);
        } else {
            binding.recyclerViewMyGardenPlants.setVisibility(View.VISIBLE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null && binding.recyclerViewMyGardenPlants != null) {
            binding.recyclerViewMyGardenPlants.setAdapter(null);
        }
        binding = null;
    }
}
