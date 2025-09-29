// Specifies the package where this MyGardenFragment class resides.
// Ensure this matches your project's structure.
package com.example.myapplication.ui.myplants;

// Android framework and utility imports.
import android.os.Bundle; // For passing data between components and saving instance state.
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // For logging messages for debugging.
import android.view.LayoutInflater; // For instantiating layout XML files into View objects.
import android.view.View; // Base class for widgets, used to create interactive UI components.
import android.view.ViewGroup; // Base class for layouts, containers that hold other Views or ViewGroups.
import android.view.inputmethod.EditorInfo; // For handling IME (Input Method Editor) actions like search.
import android.widget.Toast; // For displaying short messages (toasts) to the user.

// AndroidX (Jetpack) library imports for modern Android development.
import androidx.annotation.NonNull; // Annotation indicating a parameter, field, or method return value can never be null.
import androidx.annotation.Nullable; // Annotation indicating a parameter, field, or method return value can be null.
import androidx.fragment.app.Fragment; // Base class for managing a piece of an application's UI or behavior.
import androidx.navigation.NavController; // For managing app navigation within a NavHost.
import androidx.navigation.Navigation; // Utility class for finding a NavController.
import androidx.recyclerview.widget.GridLayoutManager; // Layout manager for displaying items in a grid.
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager for displaying items in a linear list.

// View Binding class generated from the layout file (e.g., mygardenlist.xml).
// Ensure the import matches the actual name of your layout file.
// import com.example.myapplication.databinding.MycollectiongridBinding; // Example of an old or alternative binding class.
import com.example.myapplication.databinding.MygardenlistBinding;

// Application-specific R class for accessing resources.
import com.example.myapplication.R;
// Adapter for the RecyclerView that displays plant cards.
import com.example.myapplication.ui.myplants.PlantCardAdapter;
// Data model class representing a plant.
import com.example.myapplication.ui.myplants.Plant;

// Java utility classes.
import java.util.ArrayList; // Resizable-array implementation of the List interface.
import java.util.List; // Ordered collection (also known as a sequence).

/**
 * MyGardenFragment displays the user's collection of plants.
 * It allows users to view their plants in either a grid or a list format,
 * filter by favourites, search for plants (placeholder), and navigate to add new plants.
 * This fragment uses a RecyclerView with a {@link PlantCardAdapter} to display the plant items.
 */
public class MyGardenFragment extends Fragment {

    // TAG for logging, helps in identifying messages from this fragment.
    private static final String TAG = "MyGardenFragment";

    // View Binding instance for the fragment's layout (e.g., mygardenlist.xml).
    // The name of this variable and its type (MygardenlistBinding) should correspond to the XML layout file.
    private MygardenlistBinding binding;

    // Adapter for the RecyclerView to display plant cards.
    private PlantCardAdapter plantCardAdapter;
    // List to hold all sample plants (in a real app, this would come from a database or API).
    private ArrayList<Plant> allSamplePlants = new ArrayList<>();
    private ArrayList<Plant> plants = new ArrayList<>();
    // Boolean flag to track if the current view is showing only favourite plants.
    private boolean currentViewIsFavourites = false;
    // Boolean flag to track if the current RecyclerView layout is a list (true) or grid (false).
    private boolean isCurrentlyListView = false;
    private String currentSearchText = "";

    /**
     * Factory method to create a new instance of this fragment.
     * Use this method to instantiate the fragment, especially if you need to pass arguments in the future.
     *
     * @return A new instance of fragment MyGardenFragment.
     */
    public static MyGardenFragment newInstance() {
        return new MyGardenFragment();
    }

    /**
     * Called when the fragment is first created.
     * This is where you should do initial setup that doesn't involve the view hierarchy,
     * such as initializing data sources or setting up non-UI components.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Generate or load the initial list of plants.
        // In a real application, this data might be fetched from a local database or a remote server.
        generateAllSamplePlants();
        Log.d(TAG, "onCreate: Sample plants generated.");
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is where the layout is inflated using View Binding.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding.
        // Ensure that 'MygardenlistBinding' matches the name of your XML layout file (e.g., mygardenlist.xml).
        binding = MygardenlistBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: Layout inflated.");
        // Return the root view of the inflated layout.
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This is where you should initialize the view hierarchy, set up listeners,
     * and make any other UI-related configurations.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created and listeners being set up.");

        // Set the page title if the corresponding TextView exists in the layout.
        // Assumes binding.pageTitleAddPlant is the ID of the title TextView.
        if (binding.pageTitleAddPlant != null) {
            binding.pageTitleAddPlant.setText(getString(R.string.my_garden_title));
        }

        // Initialize the RecyclerView and its adapter.
        setupRecyclerView();

        // --- Setup View Toggle Buttons (List/Grid) ---
        // Assumes IDs 'imageButton' (for list view) and 'imageButton2' (for grid view) exist in the layout.
        if (binding.imageButton != null) {
            binding.imageButton.setOnClickListener(v -> {
                // Switch to list view only if not already in list view.
                if (!isCurrentlyListView) {
                    switchToListView();
                }
            });
        }
        if (binding.imageButton2 != null) {
            binding.imageButton2.setOnClickListener(v -> {
                // Switch to grid view only if currently in list view.
                if (isCurrentlyListView) {
                    switchToGridView();
                }
            });
        }
        // Update the visual state (e.g., selected/unselected icons) of the toggle buttons.
        updateToggleButtonsVisualState();

        // --- Setup Search, Collection/Favourite Filters, and Add Plant Button ---
        // Setup listener for the search input field's editor actions (e.g., when search key is pressed).
        // Assumes ID 'searchScientificNameEditText' for the search input.
        if (binding.searchScientificNameEditText != null) {
            binding.searchScientificNameEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    currentSearchText = v.getText().toString().trim();
                    displayPlants(); // Trigger the filtering
                    Log.d(TAG, "Search triggered for: " + currentSearchText);

                    return true; // Return true to indicate the action was handled.
                }
                return false; // Return false if the action was not handled.
            });
        }

        // Clear text after searching
        binding.searchScientificNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If the text is cleared, reset the search and refresh the list
                if (s.toString().isEmpty()) {
                    currentSearchText = "";
                    displayPlants();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Setup listener for the "My Collection" button
        if (binding.button2 != null) {
            binding.button2.setOnClickListener(v -> {
                currentViewIsFavourites = false;
                binding.searchScientificNameEditText.setText(""); // Clear search text
                currentSearchText = ""; // Clear search state
                displayPlants();
                Log.d(TAG, "Switched to 'My Collection' view.");
            });
        }

        // Setup listener for the "My Favourite" button
        if (binding.button3 != null) {
            binding.button3.setOnClickListener(v -> {
                currentViewIsFavourites = true;
                binding.searchScientificNameEditText.setText(""); // Clear search text
                currentSearchText = ""; // Clear search state
                displayPlants();
                Log.d(TAG, "Switched to 'My Favourite' view.");
            });
        }

        // Setup listener for the "Add Plant" button.
        // Assumes ID 'imageButton4' for this button.
        // This listener now navigates to the AddPlantFragment.
        if (binding.imageButton4 != null) {
            binding.imageButton4.setOnClickListener(v -> {
                Log.d(TAG, "Add Plant button clicked, attempting to navigate to AddPlantFragment.");
                NavController navController = Navigation.findNavController(v);
                try {
                    // Use the action ID defined in your mobile_navigation.xml to navigate.
                    // Ensure 'action_myGardenFragment_to_addPlantFragment' is correctly defined.
                    navController.navigate(R.id.action_myGardenFragment_to_addPlantFragment);
                } catch (IllegalArgumentException e) {
                    // Log an error and show a toast if navigation fails (e.g., action ID not found).
                    Log.e(TAG, "Navigation action ID (action_myGardenFragment_to_addPlantFragment) not found. " +
                            "Check mobile_navigation.xml: " + e.getMessage());
                    Toast.makeText(getContext(), "Error: Could not navigate to Add Plant screen.", Toast.LENGTH_LONG).show();
                }
            });
        }

        // --- Initial Display Setup ---
        // If this is the first time the fragment is created (not restored from a saved state).
        if (savedInstanceState == null) {
            currentViewIsFavourites = false; // Default to showing all plants.
            switchToGridView();              // Default to grid view layout.
            displayPlants();                 // Display the initial set of plants.
            Log.d(TAG, "Initial setup: Grid view, showing all plants.");
        }
    }

    /**
     * Sets up the RecyclerView, including its adapter and initial layout manager.
     */
    private void setupRecyclerView() {
        // Check if the RecyclerView view exists in the binding.
        // Assumes ID 'recyclerViewMyGardenPlants' for the RecyclerView.
        if (binding.recyclerViewMyGardenPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewMyGardenPlants is NULL! Cannot setup RecyclerView.");
            return;
        }
        // Create a new PlantCardAdapter with an empty list initially.
        // The click listener shows a toast with the plant's name.
        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            // TODO: Implement more meaningful action on plant click (e.g., navigate to plant details screen).
            Toast.makeText(getContext(), "Clicked: " + plant.getName(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Plant clicked: " + plant.getName());
        });
        // Set the adapter for the RecyclerView.
        binding.recyclerViewMyGardenPlants.setAdapter(plantCardAdapter);
        // The initial layout manager (grid or list) will be set by switchToGridView() or switchToListView()
        // during onViewCreated or when the view type is toggled.
        Log.d(TAG, "RecyclerView setup complete.");
    }

    /**
     * Switches the RecyclerView's layout to a linear list view and updates the adapter's view type.
     */
    private void switchToListView() {
        if (binding.recyclerViewMyGardenPlants == null || plantCardAdapter == null) {
            Log.w(TAG, "switchToListView: RecyclerView or Adapter is null, cannot switch.");
            return;
        }
        // Set a LinearLayoutManager for a vertical list.
        binding.recyclerViewMyGardenPlants.setLayoutManager(new LinearLayoutManager(getContext()));
        // Tell the adapter to use the list view type (which includes dates).
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_LIST_WITH_DATE);
        isCurrentlyListView = true; // Update the state flag.
        updateToggleButtonsVisualState(); // Update toggle button icons.
        Log.d(TAG, "Switched to List View with Date.");
    }

    /**
     * Switches the RecyclerView's layout to a grid view and updates the adapter's view type.
     */
    private void switchToGridView() {
        if (binding.recyclerViewMyGardenPlants == null || plantCardAdapter == null) {
            Log.w(TAG, "switchToGridView: RecyclerView or Adapter is null, cannot switch.");
            return;
        }
        // Set a GridLayoutManager with 2 columns.
        binding.recyclerViewMyGardenPlants.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // Tell the adapter to use the grid view type.
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_GRID);
        isCurrentlyListView = false; // Update the state flag.
        updateToggleButtonsVisualState(); // Update toggle button icons.
        Log.d(TAG, "Switched to Grid View.");
    }

    /**
     * Updates the visual state (e.g., icons) of the list/grid view toggle buttons
     * based on whether the current view is a list or grid.
     */
    private void updateToggleButtonsVisualState() {
        // Ensure both toggle buttons exist in the binding.
        // Assumes IDs 'imageButton' (list) and 'imageButton2' (grid).
        if (binding.imageButton == null || binding.imageButton2 == null) {
            Log.w(TAG, "updateToggleButtonsVisualState: One or both toggle buttons are null.");
            return;
        }
        if (isCurrentlyListView) {
            // Set list button to 'selected' state and grid button to 'unselected' state.
            binding.imageButton.setImageResource(R.drawable.list_select_foreground);
            binding.imageButton2.setImageResource(R.drawable.grid_unselect_foreground);
        } else {
            // Set list button to 'unselected' state and grid button to 'selected' state.
            binding.imageButton.setImageResource(R.drawable.list_unselect_foreground);
            binding.imageButton2.setImageResource(R.drawable.grid_select_foreground);
        }
        Log.d(TAG, "Toggle buttons visual state updated. isCurrentlyListView: " + isCurrentlyListView);
    }

    /**
     * Generates a list of sample {@link Plant} objects for demonstration purposes.
     * In a real application, this data would typically be fetched from a persistent data source.
     * This method ensures that sample plants are generated only once.
     */
    private void generateAllSamplePlants() {
        // Only generate if the list is currently empty.
        if (!allSamplePlants.isEmpty()) {
            Log.d(TAG, "generateAllSamplePlants: Sample plants already exist.");
            return;
        }
        Log.d(TAG, "generateAllSamplePlants: Generating new sample plants.");
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000; // Milliseconds in one day.

        // Add sample Plant objects. Ensure the Plant constructor matches these parameters.
        allSamplePlants.add(new Plant("p1", "Monstera Deliciosa", "M. deliciosa", "url_monstera", "A popular houseplant with large, glossy, fenestrated leaves.", now - 5 * oneDay, false));
        allSamplePlants.add(new Plant("p2", "Snake Plant", "Sansevieria trifasciata", "url_snakeplant", "Hardy plant known for its air-purifying qualities.", now - 2 * oneDay, true));
        allSamplePlants.add(new Plant("p3", "Spider Plant", "Chlorophytum comosum", "url_spiderplant", "Easy to grow, produces plantlets.", now - 10 * oneDay, false));
        allSamplePlants.add(new Plant("p4", "Peace Lily", "Spathiphyllum spp.", "url_peacelily", "Features elegant white spathes.", now - 7 * oneDay, true));
        // TODO: Add more sample plants or replace with actual data fetching logic.
    }

    /**
     * Filters the `allSamplePlants` list based on the `currentViewIsFavourites` flag
     * and the `currentSearchText`, then updates the adapter.
     * Also updates the visibility of an "empty view" message if the list is empty.
     */
    private void displayPlants() {
        if (plantCardAdapter == null) {
            Log.e(TAG, "displayPlants: plantCardAdapter is null. Cannot display plants.");
            return;
        }

        // Step 1: Filter by Favourites first
        ArrayList<Plant> filteredByFavourite = new ArrayList<>();
        if (currentViewIsFavourites) {
            for (Plant p : allSamplePlants) {
                if (p.isFavourite()) {
                    filteredByFavourite.add(p);
                }
            }
        } else {
            filteredByFavourite.addAll(allSamplePlants);
        }

        // --- THIS IS THE NEW PART ---
        // Step 2: Filter the result of Step 1 by the search text
        ArrayList<Plant> listToDisplay = new ArrayList<>();
        if (currentSearchText.isEmpty()) {
            // If no search text, display the result from the favourite filter
            listToDisplay.addAll(filteredByFavourite);
            Log.d(TAG, "Displaying " + listToDisplay.size() + " plants (no search filter).");
        } else {
            // If there is search text, filter further
            for (Plant p : filteredByFavourite) {
                // Check if the plant's name contains the search text (case-insensitive)
                if (p.getName().toLowerCase().contains(currentSearchText.toLowerCase())) {
                    listToDisplay.add(p);
                }
            }
            Log.d(TAG, "Displaying " + listToDisplay.size() + " plants matching search: '" + currentSearchText + "'");
        }

        // Set the final filtered list to the adapter.
        plantCardAdapter.setPlants(listToDisplay);
        // Update the visibility of the empty message TextView.
        updateEmptyViewVisibility(listToDisplay.isEmpty());
    }

    /**
     * Updates the visibility of the RecyclerView and an "empty message" TextView
     * based on whether the list of plants to display is empty.
     *
     * @param isEmpty True if the list of plants to display is empty, false otherwise.
     */
    private void updateEmptyViewVisibility(boolean isEmpty) {
        // Ensure the necessary views exist in the binding.
        // Assumes ID 'textViewMyGardenEmptyMessage' for the empty message TextView.
        if (binding.textViewMyGardenEmptyMessage == null || binding.recyclerViewMyGardenPlants == null) {
            Log.w(TAG, "updateEmptyViewVisibility: Empty message TextView or RecyclerView is null.");
            return;
        }
        if (isEmpty) {
            // If the list is empty, hide the RecyclerView and show the empty message.
            binding.recyclerViewMyGardenPlants.setVisibility(View.GONE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.VISIBLE);
            // Set the appropriate empty message text based on whether viewing favourites or all plants.
            binding.textViewMyGardenEmptyMessage.setText(currentViewIsFavourites ?
                    R.string.no_favourites_yet : R.string.collection_is_empty);
            Log.d(TAG, "Plant list is empty. Showing empty message: " + binding.textViewMyGardenEmptyMessage.getText());
        } else {
            // If the list is not empty, show the RecyclerView and hide the empty message.
            binding.recyclerViewMyGardenPlants.setVisibility(View.VISIBLE);
            binding.textViewMyGardenEmptyMessage.setVisibility(View.GONE);
            Log.d(TAG, "Plant list is not empty. Showing RecyclerView.");
        }
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has been detached from the fragment.
     * The next time the fragment needs to be displayed, a new view will be created.
     * This is where you should clean up resources associated with the view,
     * particularly by releasing references to the ViewBinding instance and RecyclerView adapter.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear the adapter from the RecyclerView to prevent memory leaks if the RecyclerView
        // is part of the binding that will be nulled out.
        if (binding != null && binding.recyclerViewMyGardenPlants != null) {
            binding.recyclerViewMyGardenPlants.setAdapter(null);
        }
        // Nullify the ViewBinding instance to release its reference to the view hierarchy
        // and prevent memory leaks.
        binding = null;
        Log.d(TAG, "onDestroyView: Binding set to null and RecyclerView adapter cleared.");
    }
}
