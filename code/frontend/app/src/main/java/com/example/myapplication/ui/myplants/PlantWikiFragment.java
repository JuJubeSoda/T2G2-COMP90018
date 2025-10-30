package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
import com.example.myapplication.databinding.PlantwikiBinding;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantWikiDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PlantWikiFragment - Searchable encyclopedia of curated plant information.
 * 
 * Purpose:
 * - Display comprehensive plant database from backend
 * - Provide searchable interface for plant discovery
 * - Navigate to detailed plant information
 * - Support users in learning about plants before adding to garden
 * 
 * User Flow:
 * 1. Fragment loads plant wiki from API (first time only)
 * 2. Displays all plants in grid view
 * 3. User can search by name or scientific name (real-time filtering)
 * 4. User clicks plant to see detailed information (overview, features, care guide)
 * 5. Data cached to prevent re-fetching on back navigation
 * 
 * Key Features:
 * - Grid layout (2 columns) for browsing
 * - Real-time search filtering
 * - Loading indicators during data fetch
 * - Data caching for performance
 * - Navigation to PlantWikiMainTabFragment for detailed view
 * - Comprehensive plant information from PlantWikiDto
 * 
 * API Integration:
 * - GET /api/wiki/all - Fetches all curated plant encyclopedia entries
 * - Converts PlantWikiDto to Plant domain model
 * - Includes rich data: description, features, care guide, requirements
 * 
 * Data Management:
 * - masterWikiList: Complete dataset cached in memory
 * - Prevents re-fetching on back navigation
 * - Search filter applied on cached data
 * - No user-specific data (no favourites, no gardens)
 * 
 * Differences from MyGardenFragment:
 * - No grid/list toggle (always grid)
 * - No favourites filter (wiki plants not user-owned)
 * - Richer plant data (features, care guide, growth info)
 * - Navigates to wiki detail view, not user plant detail
 */
public class PlantWikiFragment extends Fragment {

    private static final String TAG = "PlantWikiFragment";
    
    /** View binding for plantwiki.xml layout */
    private PlantwikiBinding binding;
    
    /** RecyclerView adapter for displaying wiki plants in grid */
    private PlantCardAdapter plantCardAdapter;

    /** Master list of all wiki plants (cached to prevent unnecessary API calls) */
    private List<Plant> masterWikiList = new ArrayList<>();

    /**
     * Inflates the layout using View Binding.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantwikiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI components and loads wiki data after view is created.
     * 
     * Initialization Order:
     * 1. Setup RecyclerView with grid layout and click listener
     * 2. Setup search field listeners
     * 3. Check if data cached (prevents re-fetch on back navigation)
     * 4. Fetch from server if cache empty, otherwise display cached data
     * 
     * Smart Data Loading:
     * - Only fetches from server if masterWikiList is empty
     * - On back navigation, uses cached data immediately
     * - Prevents unnecessary loading indicators
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView with grid layout
        setupWikiRecyclerView();
        
        // Setup search field listeners
        setupWikiClickListeners();

        // Smart data loading: only fetch if cache is empty
        if (masterWikiList.isEmpty()) {
            Log.d(TAG, "Wiki list is empty. Fetching from server.");
            fetchWikiPlantsFromServer();
        } else {
            Log.d(TAG, "Wiki data exists. Displaying from master list.");
            displayWikiPlants();
        }
    }

    /**
     * Initializes RecyclerView with grid layout and click listener.
     * 
     * Setup:
     * - Creates PlantCardAdapter with empty initial data
     * - Sets click listener for navigation to PlantWikiMainTabFragment
     * - Uses GridLayoutManager with 2 columns
     * - Passes Plant object as Parcelable argument
     * 
     * Click Behavior:
     * - User clicks wiki plant card
     * - Navigates to PlantWikiMainTabFragment (tabbed detail view)
     * - Passes complete Plant object with wiki-specific data
     * 
     * Error Handling:
     * - Catches navigation exceptions
     * - Shows user-friendly error message
     * - Logs errors for debugging
     */
    private void setupWikiRecyclerView() {
        if (binding.recyclerViewPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewPlants is NULL!");
            return;
        }

        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Log.d(TAG, "Wiki plant clicked: " + plant.getName() + ". Navigating to wiki tab.");
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
            try {
                // Navigate to wiki detail view (tabbed interface)
                Navigation.findNavController(requireView()).navigate(R.id.plantwiki_maintab, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to R.id.plantwiki_maintab failed. Check nav graph for this ID.", e);
                Toast.makeText(getContext(), "Could not open plant details.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerViewPlants.setAdapter(plantCardAdapter);
        binding.recyclerViewPlants.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    /**
     * Sets up search field listeners for real-time filtering.
     * 
     * Listeners:
     * - TextWatcher: Filters plants as user types (real-time)
     * - EditorActionListener: Handles search button press on keyboard
     * 
     * Search Behavior:
     * - Filters on name and scientific name
     * - Case-insensitive matching
     * - Instant results (no delay)
     * - Operates on cached masterWikiList (no API calls)
     */
    private void setupWikiClickListeners() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayFilteredPlants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                displayFilteredPlants(v.getText().toString());
                return true;
            }
            return false;
        });
    }

    /**
     * Displays wiki plants with current search filter applied.
     * Wrapper method that calls displayFilteredPlants with current search text.
     */
    private void displayWikiPlants() {
        if (binding != null) {
            displayFilteredPlants(binding.searchEditText.getText().toString());
        }
    }

    /**
     * Filters and displays plants based on search query.
     * 
     * Filter Logic:
     * - Empty query: Show all plants from masterWikiList
     * - Non-empty query: Filter by name or scientific name (case-insensitive)
     * 
     * Behavior:
     * - Hides RecyclerView if masterWikiList is empty
     * - Shows RecyclerView and updates adapter with filtered results
     * - Uses Java Streams for efficient filtering
     * 
     * Performance:
     * - Operates on cached masterWikiList (no API calls)
     * - Creates new list to avoid modifying master data
     * 
     * @param query Search text from search field
     */
    private void displayFilteredPlants(String query) {
        if (masterWikiList.isEmpty()) {
            binding.recyclerViewPlants.setVisibility(View.GONE);
            return;
        }

        binding.recyclerViewPlants.setVisibility(View.VISIBLE);
        List<Plant> filteredList;
        if (query.trim().isEmpty()) {
            // No search query - show all plants
            filteredList = new ArrayList<>(masterWikiList);
        } else {
            // Filter by name or scientific name (case-insensitive)
            String lowerCaseQuery = query.toLowerCase();
            filteredList = masterWikiList.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerCaseQuery) ||
                            (p.getScientificName() != null && p.getScientificName().toLowerCase().contains(lowerCaseQuery)))
                    .collect(Collectors.toList());
        }
        plantCardAdapter.setPlants(new ArrayList<>(filteredList));
    }

    /**
     * Fetches plant encyclopedia from backend API.
     * 
     * Process:
     * 1. Show loading indicator, hide RecyclerView
     * 2. Make API call to GET /api/wiki/all
     * 3. Convert PlantWikiDto list to Plant list using toPlant()
     * 4. Cache in masterWikiList
     * 5. Display plants with current filter
     * 6. Handle errors with user-friendly messages
     * 
     * Data Conversion:
     * - PlantWikiDto includes rich data: features, care guide, requirements
     * - toPlant() maps DTO fields to Plant domain model
     * - Logs conversion details for debugging
     * 
     * Error Handling:
     * - Checks if view still exists before updating UI
     * - Shows appropriate error messages
     * - Hides loading indicator in all cases
     * 
     * Data Caching:
     * - Stores all wiki plants in masterWikiList
     * - Prevents re-fetching on back navigation
     * - Filters applied on cached data
     */
    private void fetchWikiPlantsFromServer() {
        binding.progressBarWiki.setVisibility(View.VISIBLE);
        binding.recyclerViewPlants.setVisibility(View.GONE);

        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantWikiDto>>> call = apiService.getAllWikis();

        call.enqueue(new Callback<ApiResponse<List<PlantWikiDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantWikiDto>>> call, @NonNull Response<ApiResponse<List<PlantWikiDto>>> response) {
                // Safety check: If view destroyed, abort UI updates
                if (binding == null) return;
                
                // Hide loading indicator
                binding.progressBarWiki.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Success: Convert DTOs to domain models and cache
                    masterWikiList.clear();
                    for (PlantWikiDto dto : response.body().getData()) {
                        Plant plant = dto.toPlant();
                        masterWikiList.add(plant);
                        
                        // Log detailed conversion for debugging
                        Log.d(TAG, "Added wiki plant: " + plant.getName() + " (Scientific: " + plant.getScientificName() + ")");
                        Log.d(TAG, "Plant features: " + dto.getFeatures());
                        Log.d(TAG, "Water needs: " + dto.getWaterNeeds());
                        Log.d(TAG, "Light needs: " + dto.getLightNeeds());
                        Log.d(TAG, "Difficulty: " + dto.getDifficulty());
                        Log.d(TAG, "Growth height: " + dto.getGrowthHeight());
                    }
                    Log.d(TAG, "Successfully fetched " + masterWikiList.size() + " plants for wiki from database.");
                    displayWikiPlants(); // Apply filters and show
                } else {
                    // API error: Show error message
                    Log.e(TAG, "Failed to fetch wiki plants. Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), "Failed to load wiki from database. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantWikiDto>>> call, @NonNull Throwable t) {
                // Network error: Safety check and show error
                if (binding == null) return;
                
                binding.progressBarWiki.setVisibility(View.GONE);
                Log.e(TAG, "Network error fetching wiki plants from database.", t);
                Toast.makeText(getContext(), "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cleans up view binding to prevent memory leaks.
     * Note: masterWikiList is preserved for data caching across navigation.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView: PlantWikiFragment binding cleared.");
    }
}
