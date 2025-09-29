// Specifies the package where this MyGardenFragment class resides.
package com.example.myapplication.ui.myplants;

// Android framework and utility imports.
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

// AndroidX (Jetpack) library imports.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

// View Binding and Application-specific imports.
import com.example.myapplication.R;
import com.example.myapplication.databinding.MygardenlistBinding;

// Java utility classes.
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyGardenFragment extends Fragment {

    private static final String TAG = "MyGardenFragment";
    private MygardenlistBinding binding;
    private PlantCardAdapter plantCardAdapter;

    private ArrayList<Plant> allSamplePlants = new ArrayList<>();
    private boolean currentViewIsFavourites = false;
    private boolean isCurrentlyListView = false;
    private String currentSearchText = "";

    // Unused 'plants' variable removed for cleanliness.

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Generate sample data when the fragment is created.
        generateAllSamplePlants();
        Log.d(TAG, "onCreate: Sample plants generated.");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MygardenlistBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: Layout inflated.");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created and listeners being set up.");

        // Initialize UI components and listeners.
        setupRecyclerView();
        setupClickListeners();

        // Set initial view state if it's the first time creating the view.
        if (savedInstanceState == null) {
            currentViewIsFavourites = false;
            switchToGridView();
            displayPlants();
            Log.d(TAG, "Initial setup: Grid view, showing all plants.");
        }
        updateToggleButtonsVisualState();
    }

    private void setupClickListeners() {
        if (binding.pageTitleAddPlant != null) {
            binding.pageTitleAddPlant.setText(getString(R.string.my_garden_title));
        }

        // List/Grid view toggle listeners
        binding.imageButton.setOnClickListener(v -> {
            if (!isCurrentlyListView) switchToListView();
        });
        binding.imageButton2.setOnClickListener(v -> {
            if (isCurrentlyListView) switchToGridView();
        });

        // Search listener
        binding.searchScientificNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchText = v.getText().toString().trim();
                displayPlants();
                return true;
            }
            return false;
        });

        binding.searchScientificNameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().trim();
                displayPlants(); // Filter as the user types.
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter buttons
        binding.button2.setOnClickListener(v -> { // "My Collection"
            currentViewIsFavourites = false;
            binding.searchScientificNameEditText.setText("");
            displayPlants();
            Log.d(TAG, "Switched to 'My Collection' view.");
        });

        binding.button3.setOnClickListener(v -> { // "My Favourite"
            currentViewIsFavourites = true;
            binding.searchScientificNameEditText.setText("");
            displayPlants();
            Log.d(TAG, "Switched to 'My Favourite' view.");
        });

        // --- MODIFICATION 1: Update Add Plant button to pass 'isFavouriteFlow' ---
        binding.imageButton4.setOnClickListener(v -> {
            Log.d(TAG, "Add Plant button clicked, navigating with isFavouriteFlow = " + currentViewIsFavourites);
            NavController navController = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putBoolean("isFavouriteFlow", currentViewIsFavourites); // Pass the context

            try {
                // Navigate with the correct action and arguments.
                navController.navigate(R.id.action_myGardenFragment_to_addPlantFragment, args);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Navigation failed. Check 'action_myGardenFragment_to_addPlantFragment' in mobile_navigation.xml", e);
                Toast.makeText(getContext(), "Error: Navigation to Add Plant screen failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        if (binding.recyclerViewMyGardenPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewMyGardenPlants is NULL!");
            return;
        }

        // --- MODIFICATION 2: Use correct navigation action ID ---
        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Log.d(TAG, "Plant clicked: " + plant.getName() + ". Navigating to details.");
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);

            try {
                // You must use the ACTION ID, not the destination ID.
                Navigation.findNavController(requireView()).navigate(R.id.plantDetailFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to PlantDetailFragment failed.", e);
                Toast.makeText(getContext(), "Error: Could not open plant details.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerViewMyGardenPlants.setAdapter(plantCardAdapter);
        Log.d(TAG, "RecyclerView setup complete.");
    }

    private void switchToListView() {
        binding.recyclerViewMyGardenPlants.setLayoutManager(new LinearLayoutManager(getContext()));
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_LIST_WITH_DATE);
        isCurrentlyListView = true;
        updateToggleButtonsVisualState();
        Log.d(TAG, "Switched to List View.");
    }

    private void switchToGridView() {
        binding.recyclerViewMyGardenPlants.setLayoutManager(new GridLayoutManager(getContext(), 2));
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_GRID);
        isCurrentlyListView = false;
        updateToggleButtonsVisualState();
        Log.d(TAG, "Switched to Grid View.");
    }

    private void updateToggleButtonsVisualState() {
        if (isCurrentlyListView) {
            binding.imageButton.setImageResource(R.drawable.list_select_foreground);
            binding.imageButton2.setImageResource(R.drawable.grid_unselect_foreground);
        } else {
            binding.imageButton.setImageResource(R.drawable.list_unselect_foreground);
            binding.imageButton2.setImageResource(R.drawable.grid_select_foreground);
        }
    }

    // --- MODIFICATION 3: Update `generateAllSamplePlants` to use the new Plant constructor ---
    private void generateAllSamplePlants() {
        if (!allSamplePlants.isEmpty()) return;

        Log.d(TAG, "Generating new sample plants.");
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;

        // Using the new Plant constructor with all required fields
        allSamplePlants.add(new Plant("p1", "Monstera deliciosa", "Monstera", "A popular houseplant with iconic leaves.", "Indoor", "Houseplant, Tropical", "UserA", now - 5 * oneDay, "\"Fiddle Leaf Fig\",", false));
        allSamplePlants.add(new Plant("p2", "Dracaena trifasciata", "Snake Plant", "A very hardy plant, great for beginners.", "Indoor", "Succulent, Hardy, Air-purifying", "UserB", now - 2 * oneDay, "\"Fiddle Leaf Fig\",", true));
        allSamplePlants.add(new Plant("p3",  "Chlorophytum comosum", "Spider Plant", "Easy to grow and produces 'spiderettes'.", "Indoor", "Easy Care, Hanging Plant", "UserA", now - 10 * oneDay, "\"Fiddle Leaf Fig\",", false));
        allSamplePlants.add(new Plant("p4", "Spathiphyllum wallisii", "Spider Plant","Features elegant white spathes and purifies the air.", "Indoor", "Flowering, Air Purifying", "UserC", now - 7 * oneDay, "\"Fiddle Leaf Fig\",", true));
        allSamplePlants.add(new Plant("p5", "Ficus lyrata", "Fiddle Leaf Fig","A popular but sometimes tricky indoor tree.", "Indoor", "Tree, Fussy", "UserB", now - 30 * oneDay, "\"Fiddle Leaf Fig\",", false));
    }

    private void displayPlants() {
        if (plantCardAdapter == null) {
            Log.e(TAG, "displayPlants: plantCardAdapter is null.");
            return;
        }

        List<Plant> plantsToShow;

        // Step 1: Filter by Favourites
        if (currentViewIsFavourites) {
            plantsToShow = allSamplePlants.stream()
                    .filter(Plant::isFavourite)
                    .collect(Collectors.toList());
        } else {
            plantsToShow = new ArrayList<>(allSamplePlants);
        }

        // Step 2: Filter by Search Text
        if (!currentSearchText.isEmpty()) {
            plantsToShow = plantsToShow.stream()
                    .filter(p -> p.getName().toLowerCase().contains(currentSearchText.toLowerCase()) ||
                            p.getScientificName().toLowerCase().contains(currentSearchText.toLowerCase()))
                    .collect(Collectors.toList());
        }

        plantCardAdapter.setPlants(new ArrayList<>(plantsToShow));
        updateEmptyViewVisibility(plantsToShow.isEmpty());
        Log.d(TAG, "Displaying " + plantsToShow.size() + " plants.");
    }

    private void updateEmptyViewVisibility(boolean isEmpty) {
        if (binding == null) return;
        if (isEmpty) {
            binding.recyclerViewMyGardenPlants.setVisibility(View.GONE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.VISIBLE);
            binding.textViewMyGardenEmptyMessage.setText(currentViewIsFavourites ?
                    R.string.no_favourites_yet : R.string.collection_is_empty);
        } else {
            binding.recyclerViewMyGardenPlants.setVisibility(View.VISIBLE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify the binding to prevent memory leaks.
        binding = null;
        Log.d(TAG, "onDestroyView: Binding set to null.");
    }
}
