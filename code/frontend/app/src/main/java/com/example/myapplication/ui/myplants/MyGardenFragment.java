package com.example.myapplication.ui.myplants;

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
import com.example.myapplication.databinding.MygardenlistBinding;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.myPlantsData.MyGardenDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MyGardenFragment - User's personal plant collection display and management.
 * 
 * Purpose:
 * - Display all plants in user's garden
 * - Support grid and list view toggle
 * - Filter by favourites or full collection
 * - Search plants by name or scientific name
 * - Navigate to plant details
 * - Add new plants to garden
 * 
 * User Flow:
 * 1. Fragment loads user's plants from API (first time only)
 * 2. Displays in grid view by default
 * 3. User can toggle between grid/list view
 * 4. User can switch between "My Collection" and "My Favourite"
 * 5. User can search plants in real-time
 * 6. User clicks plant to see detailed information
 * 7. User can add new plants via floating action button
 * 
 * Key Features:
 * - Grid/List view toggle with visual state persistence
 * - Collection/Favourites filter
 * - Real-time search filtering
 * - Loading indicators during data fetch
 * - Empty state handling with contextual messages
 * - Data caching to prevent unnecessary re-fetching
 * - Navigation to PlantDetailFragment
 * - Navigation to AddPlantFragment
 * 
 * API Integration:
 * - GET /api/plants/user - Fetches user's plant collection
 * - Converts PlantDto to Plant domain model
 * 
 * View Management:
 * - masterPlantList: Complete dataset cached in memory
 * - Prevents re-fetching on back navigation
 * - Applies filters (favourites, search) on cached data
 * - Handles view switching without data loss
 * 
 * State Preservation:
 * - View type (grid/list) persists across navigation
 * - Filter state (collection/favourites) persists
 * - Search text persists during session
 * - Data cached until fragment destroyed
 */
public class MyGardenFragment extends Fragment {

    private static final String TAG = "MyGardenFragment";
    
    /** View binding for mygardenlist.xml layout */
    private MygardenlistBinding binding;
    
    /** RecyclerView adapter for displaying plants in grid or list */
    private PlantCardAdapter plantCardAdapter;

    /** Master list of all user's plants (cached to prevent unnecessary API calls) */
    private List<Plant> masterPlantList = new ArrayList<>();
    /** Cached list for user's liked plants (from dedicated API) */
    private List<Plant> likedPlantList = new ArrayList<>();
    
    /** Current filter state: true = show favourites only, false = show all */
    private boolean currentViewIsFavourites = false;
    
    /** Current view mode: true = list view, false = grid view */
    private boolean isCurrentlyListView = false;
    
    /** Current search query text */
    private String currentSearchText = "";

    /** Data manager for My Garden domain (liked plants, etc.) */
    private MyGardenDataManager myGardenDataManager;

    /**
     * Fragment creation lifecycle method.
     * Data fetching moved to onViewCreated for better lifecycle management.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Data is now fetched in onViewCreated, not here.
    }

    /** Loads liked plants from API and displays them in the list */
    private void loadLikedPlantsAndDisplay() {
        if (binding == null) return;
        try {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.recyclerViewMyGardenPlants.setVisibility(View.GONE);
        } catch (Exception ignored) {}

        myGardenDataManager.fetchLikedPlants(new MyGardenDataManager.DataCallback<List<PlantDto>>() {
            @Override
            public void onSuccess(List<PlantDto> data) {
                if (binding == null) return;
                likedPlantList.clear();
                if (data != null) {
                    likedPlantList = data.stream().map(PlantDto::toPlant).collect(Collectors.toList());
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerViewMyGardenPlants.setVisibility(View.VISIBLE);
                displayPlants();
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load liked plants: " + message, Toast.LENGTH_SHORT).show();
                // Fallback to filter from master list
                displayPlants();
            }
        });
    }

    /**
     * Inflates the layout using View Binding.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MygardenlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI components and loads data after view is created.
     * 
     * Initialization Order:
     * 1. Setup RecyclerView with adapter
     * 2. Setup all click listeners
     * 3. Check if data cached (prevents re-fetch on back navigation)
     * 4. Fetch from server if cache empty, otherwise display cached data
     * 5. Restore UI state (grid/list view, toggle buttons)
     * 
     * Smart Data Loading:
     * - Only fetches from server if masterPlantList is empty
     * - On back navigation, uses cached data immediately
     * - Prevents unnecessary loading indicators
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created and listeners being set up.");

        // Initialize RecyclerView and adapter
        setupRecyclerView();
        // Initialize MyGarden data manager
        myGardenDataManager = new MyGardenDataManager(requireContext());
        
        // Setup all button and search listeners
        setupClickListeners();

        // Smart data loading: only fetch if cache is empty
        if (masterPlantList.isEmpty()) {
            // First time or data was cleared - fetch from server
            Log.d(TAG, "Master list is empty. Fetching from server.");
            fetchPlantsFromServer();
        } else {
            // Data already cached - display immediately (no loading indicator)
            Log.d(TAG, "Data already exists. Displaying from master list.");
            displayPlants();
        }

        // Restore UI state from previous session
        if (isCurrentlyListView) {
            switchToListView();
        } else {
            switchToGridView();
        }
        updateToggleButtonsVisualState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Auto refresh when returning to My Favourite tab
        if (currentViewIsFavourites) {
            loadLikedPlantsAndDisplay();
        }
    }

    /**
     * Fetches user's plant collection from backend API.
     * 
     * Process:
     * 1. Show loading indicator, hide RecyclerView
     * 2. Make API call to GET /api/plants/user
     * 3. Convert PlantDto list to Plant list
     * 4. Cache in masterPlantList
     * 5. Display plants with current filters
     * 6. Handle errors with user-friendly messages
     * 
     * Error Handling:
     * - Checks if view still exists before updating UI
     * - Shows appropriate error messages
     * - Displays empty state on failure
     * - Hides loading indicator in all cases
     * 
     * Data Caching:
     * - Stores all plants in masterPlantList
     * - Prevents re-fetching on back navigation
     * - Filters applied on cached data
     */
    private void fetchPlantsFromServer() {
        Log.d(TAG, "Fetching plants from server...");
        // Make the ProgressBar visible and hide the RecyclerView
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewMyGardenPlants.setVisibility(View.GONE);

        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantDto>>> call = apiService.getPlantsByUser();

                call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Response<ApiResponse<List<PlantDto>>> response) {
                // Safety check: If view destroyed, abort UI updates
                if (binding == null) {
                    Log.w(TAG, "onResponse called after view was destroyed. Aborting update.");
                    return;
                }

                // Hide loading indicator
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Success: Convert DTOs to domain models and cache
                    Log.d(TAG, "API call successful. Received " + response.body().getData().size() + " plants.");
                    masterPlantList.clear();
                    masterPlantList = response.body().getData().stream()
                            .map(PlantDto::toPlant) // Convert DTO to Plant
                            .collect(Collectors.toList());
                    displayPlants(); // Apply filters and show
                } else {
                    // API error: Show error message and empty state
                    Log.e(TAG, "API call failed or returned empty data. Code: " + response.code());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load plants: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                    updateEmptyViewVisibility(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Throwable t) {
                // Network error: Safety check and show error
                if (binding == null) {
                    Log.w(TAG, "onFailure called after view was destroyed. Aborting update.");
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API call failed due to network error.", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
                }
                updateEmptyViewVisibility(true);
            }
        });
    }

    /**
     * Sets up all click listeners for UI interactions.
     * 
     * Listeners:
     * - Grid/List toggle buttons
     * - Search field (real-time and submit)
     * - Collection/Favourites toggle buttons
     * - Add plant floating action button
     * 
     * Navigation:
     * - To PlantDetailFragment: Via adapter click (setup in setupRecyclerView)
     * - To AddPlantFragment: Via floating action button with favourite flag
     */
    private void setupClickListeners() {
        binding.pageTitleAddPlant.setText(getString(R.string.my_garden_title));

        binding.imageButton.setOnClickListener(v -> {
            if (!isCurrentlyListView) switchToListView();
        });
        binding.imageButton2.setOnClickListener(v -> {
            if (isCurrentlyListView) switchToGridView();
        });

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
                displayPlants();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.button2.setOnClickListener(v -> { // "My Collection"
            currentViewIsFavourites = false;
            displayPlants();
        });

        binding.button3.setOnClickListener(v -> { // "My Favourite"
            currentViewIsFavourites = true;
            loadLikedPlantsAndDisplay();
        });

        // Add plant button - navigate to AddPlantFragment with favourite flag
        binding.imageButton4.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putBoolean("isFavouriteFlow", currentViewIsFavourites);
            navController.navigate(R.id.action_myGardenFragment_to_addPlantFragment, args);
        });
    }

    /**
     * Initializes RecyclerView with adapter and click listener.
     * 
     * Setup:
     * - Creates PlantCardAdapter with empty initial data
     * - Sets click listener for navigation to PlantDetailFragment
     * - Passes Plant object as Parcelable argument
     * 
     * Click Behavior:
     * - User clicks plant card
     * - Navigates to PlantDetailFragment
     * - Passes complete Plant object for display
     */
    private void setupRecyclerView() {
        if (binding.recyclerViewMyGardenPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewMyGardenPlants is NULL!");
            return;
        }

        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Log.d(TAG, "Plant clicked: " + plant.getName() + ". Navigating to details.");
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
            if (currentViewIsFavourites) {
                Navigation.findNavController(requireView()).navigate(R.id.likedPlantDetailFragment, args);
            } else {
                Navigation.findNavController(requireView()).navigate(R.id.plantDetailFragment, args);
            }
        });

        binding.recyclerViewMyGardenPlants.setAdapter(plantCardAdapter);
    }

    /**
     * Switches RecyclerView to list layout with dates.
     * 
     * Changes:
     * - LayoutManager: LinearLayoutManager (vertical list)
     * - View Type: LIST_WITH_DATE (shows creation dates)
     * - Updates toggle button visual state
     * 
     * Error Handling:
     * - Checks for null RecyclerView and adapter
     * - Catches and logs exceptions
     * - Shows user-friendly error message
     */
    private void switchToListView() {
        try {
            if (binding.recyclerViewMyGardenPlants == null) {
                Log.e(TAG, "RecyclerView is null, cannot switch to list view");
                return;
            }
            if (plantCardAdapter == null) {
                Log.e(TAG, "PlantCardAdapter is null, cannot switch to list view");
                return;
            }
            
            binding.recyclerViewMyGardenPlants.setLayoutManager(new LinearLayoutManager(getContext()));
            plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_LIST_WITH_DATE);
            isCurrentlyListView = true;
            updateToggleButtonsVisualState();
            Log.d(TAG, "Successfully switched to list view");
        } catch (Exception e) {
            Log.e(TAG, "Error switching to list view", e);
            Toast.makeText(getContext(), "Error switching to list view", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Switches RecyclerView to grid layout (2 columns).
     * 
     * Changes:
     * - LayoutManager: GridLayoutManager with 2 columns
     * - View Type: GRID (compact card view)
     * - Updates toggle button visual state
     * 
     * Error Handling:
     * - Checks for null RecyclerView and adapter
     * - Catches and logs exceptions
     * - Shows user-friendly error message
     */
    private void switchToGridView() {
        try {
            if (binding.recyclerViewMyGardenPlants == null) {
                Log.e(TAG, "RecyclerView is null, cannot switch to grid view");
                return;
            }
            if (plantCardAdapter == null) {
                Log.e(TAG, "PlantCardAdapter is null, cannot switch to grid view");
                return;
            }
            
            binding.recyclerViewMyGardenPlants.setLayoutManager(new GridLayoutManager(getContext(), 2));
            plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_GRID);
            isCurrentlyListView = false;
            updateToggleButtonsVisualState();
            Log.d(TAG, "Successfully switched to grid view");
        } catch (Exception e) {
            Log.e(TAG, "Error switching to grid view", e);
            Toast.makeText(getContext(), "Error switching to grid view", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates toggle button icons based on current view mode.
     * 
     * Visual States:
     * - List view active: List button highlighted, Grid button normal
     * - Grid view active: Grid button highlighted, List button normal
     * 
     * Error Handling:
     * - Catches exceptions to prevent crashes
     * - Logs errors for debugging
     */
    private void updateToggleButtonsVisualState() {
        try {
            if (isCurrentlyListView) {
                binding.imageButton.setImageResource(R.drawable.list_select_foreground);
                binding.imageButton2.setImageResource(R.drawable.grid_unselect_foreground);
            } else {
                binding.imageButton.setImageResource(R.drawable.list_unselect_foreground);
                binding.imageButton2.setImageResource(R.drawable.grid_select_foreground);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating toggle buttons visual state", e);
        }
    }

    /**
     * Applies filters and displays plants in RecyclerView.
     * 
     * Filter Pipeline:
     * 1. Start with masterPlantList (all user's plants)
     * 2. Filter by favourites if currentViewIsFavourites is true
     * 3. Filter by search text (name or scientific name)
     * 4. Update adapter with filtered list
     * 5. Show/hide empty state based on results
     * 
     * Filters Applied:
     * - Favourites: Shows only plants where isFavourite() is true
     * - Search: Case-insensitive match on name or scientificName
     * 
     * Performance:
     * - Uses Java Streams for efficient filtering
     * - Operates on cached masterPlantList (no API calls)
     * - Creates new list to avoid modifying master data
     */
    private void displayPlants() {
        if (plantCardAdapter == null) {
            Log.e(TAG, "displayPlants: plantCardAdapter is null.");
            return;
        }

        List<Plant> plantsToShow;

        // Step 1: Source data based on current tab
        if (currentViewIsFavourites) {
            // Prefer server-liked list if available; fallback to filtering master list
            if (!likedPlantList.isEmpty()) {
                plantsToShow = new ArrayList<>(likedPlantList);
            } else {
                plantsToShow = masterPlantList.stream()
                        .filter(Plant::isFavourite)
                        .collect(Collectors.toList());
            }
        } else {
            plantsToShow = new ArrayList<>(masterPlantList);
        }

        // Step 2: Filter by Search Text
        if (!currentSearchText.isEmpty()) {
            plantsToShow = plantsToShow.stream()
                    .filter(p -> p.getName().toLowerCase().contains(currentSearchText.toLowerCase()) ||
                            p.getScientificName().toLowerCase().contains(currentSearchText.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Update adapter and show/hide empty state
        plantCardAdapter.setPlants(new ArrayList<>(plantsToShow));
        updateEmptyViewVisibility(plantsToShow.isEmpty());
        Log.d(TAG, "Displaying " + plantsToShow.size() + " plants.");
    }

    /**
     * Shows or hides empty state message based on filtered results.
     * 
     * Empty State Messages:
     * - Favourites view: "No favourites yet"
     * - Collection view: "Collection is empty"
     * 
     * Behavior:
     * - isEmpty = true: Hide RecyclerView, show contextual empty message
     * - isEmpty = false: Show RecyclerView, hide empty message
     * 
     * Safety:
     * - Checks if binding exists before updating UI
     */
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

    /**
     * Cleans up view binding to prevent memory leaks.
     * Note: masterPlantList is preserved for data caching across navigation.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
