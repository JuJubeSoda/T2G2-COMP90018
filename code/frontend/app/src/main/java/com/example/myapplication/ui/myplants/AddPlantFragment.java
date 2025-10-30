package com.example.myapplication.ui.myplants;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.databinding.AddplantBinding;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantWikiDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AddPlantFragment - Plant name selection interface for adding new plants.
 * 
 * Purpose:
 * - Provide searchable list of plant names from wiki database
 * - Allow users to select or enter plant name before capture
 * - Pre-fill plant information for upload process
 * - Support favourite flow from MyGardenFragment
 * 
 * User Flow:
 * 1. Fragment loads all plant names from wiki API
 * 2. Displays full list of plant names by default
 * 3. User can search/filter by typing (real-time)
 * 4. User selects plant name from list or enters custom name
 * 5. User clicks next button to proceed to camera capture
 * 6. Selected name passed to CaptureFragment → UploadFragment
 * 
 * Key Features:
 * - Real-time search filtering
 * - Show all plants by default (before typing)
 * - Auto-hide keyboard on selection
 * - Loading indicators during data fetch
 * - Empty state handling
 * - Navigation to camera capture with pre-filled data
 * 
 * API Integration:
 * - GET /api/wiki/all - Fetches all plant names from encyclopedia
 * - Extracts common names from PlantWikiDto
 * - Removes duplicates and empty names
 * 
 * Arguments:
 * - isFavouriteFlow: Boolean indicating if adding to favourites (from MyGardenFragment)
 * 
 * Navigation:
 * - From: MyGardenFragment (via add button)
 * - To: CaptureFragment (via next button) with scientificName and isFavouriteFlow
 */
public class AddPlantFragment extends Fragment {

    private static final String TAG = "AddPlantFragment";
    
    /** View binding for addplant.xml layout */
    private AddplantBinding binding;
    
    /** Navigation controller for fragment transitions */
    private NavController navController;
    
    /** RecyclerView adapter for displaying searchable plant names */
    private SearchResultAdapter searchAdapter;

    /** Complete list of plant names from wiki (cached for filtering) */
    private final List<String> allPlantCommonNames = new ArrayList<>();
    
    /** Whether user is adding to favourites (passed from MyGardenFragment) */
    private boolean isFavouriteFlow = false;

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI components and loads plant names after view is created.
     * 
     * Initialization Order:
     * 1. Get navigation controller
     * 2. Retrieve isFavouriteFlow argument from MyGardenFragment
     * 3. Setup RecyclerView with adapter and click listener
     * 4. Setup search field listeners
     * 5. Fetch all plant names from wiki API
     * 6. Setup back and next button listeners
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Retrieve favourite flow flag from arguments
        if (getArguments() != null) {
            isFavouriteFlow = getArguments().getBoolean("isFavouriteFlow", false);
        }

        setupRecyclerView();
        setupSearchLogic();
        fetchAllPlantNamesFromServer();
        setupBackButton();
        setupNextButton();
    }

    /**
     * Fetches all plant names from wiki API for searchable list.
     * 
     * Process:
     * 1. Show loading indicator
     * 2. Make API call to GET /api/wiki/all
     * 3. Extract common names from PlantWikiDto list
     * 4. Remove duplicates and empty names
     * 5. Display full list immediately (default state)
     * 6. Handle errors with user-friendly messages
     * 
     * Default Behavior:
     * - Shows all plant names immediately after loading
     * - User can then filter by typing
     * - Provides better UX than empty initial state
     */
    private void fetchAllPlantNamesFromServer() {
        Log.d(TAG, "Fetching all plant names from server...");

        binding.progressBarSearch.setVisibility(View.VISIBLE);
        binding.textViewSearchStatus.setText("Loading plant names...");
        binding.textViewSearchStatus.setVisibility(View.VISIBLE);
        binding.recyclerViewScientificNames.setVisibility(View.GONE);

        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantWikiDto>>> call = apiService.getAllWikis();

        call.enqueue(new Callback<ApiResponse<List<PlantWikiDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantWikiDto>>> call, @NonNull Response<ApiResponse<List<PlantWikiDto>>> response) {
                if (binding == null) return; // View destroyed, do nothing
                binding.progressBarSearch.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Extract and filter plant names
                    allPlantCommonNames.clear();
                    List<String> fetchedNames = response.body().getData().stream()
                            .map(PlantWikiDto::getName)
                            .filter(name -> name != null && !name.isEmpty())
                            .distinct() // Remove duplicates
                            .collect(Collectors.toList());

                    allPlantCommonNames.addAll(fetchedNames);
                    Log.d(TAG, "Successfully loaded " + allPlantCommonNames.size() + " unique plant names.");

                    // Show full list immediately (default state before user types)
                    searchAdapter.updateData(allPlantCommonNames);
                    binding.recyclerViewScientificNames.setVisibility(View.VISIBLE);
                    binding.textViewSearchStatus.setVisibility(View.GONE);

                } else {
                    // API error
                    Log.e(TAG, "Failed to fetch plant names. Code: " + response.code());
                    binding.textViewSearchStatus.setText("Failed to load plant list. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantWikiDto>>> call, @NonNull Throwable t) {
                // Network error
                if (binding == null) return;
                binding.progressBarSearch.setVisibility(View.GONE);
                binding.textViewSearchStatus.setText("Network error. Please check connection.");
                Log.e(TAG, "Network error fetching plant names.", t);
            }
        });
    }

    /**
     * Initializes RecyclerView with adapter and item click listener.
     * 
     * Click Behavior:
     * - User selects plant name from list
     * - Name fills search field
     * - Cursor moves to end of text
     * - RecyclerView hides
     * - Keyboard hides automatically
     */
    private void setupRecyclerView() {
        searchAdapter = new SearchResultAdapter(selectedPlantName -> {
            Log.d(TAG, "User selected: " + selectedPlantName);
            binding.searchScientificNameEditText.setText(selectedPlantName);
            binding.searchScientificNameEditText.setSelection(selectedPlantName.length());
            binding.recyclerViewScientificNames.setVisibility(View.GONE);

            if (getContext() != null) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.searchScientificNameEditText.getWindowToken(), 0);
            }
        });

        binding.recyclerViewScientificNames.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewScientificNames.setAdapter(searchAdapter);
    }

    /**
     * Sets up search field listener for real-time filtering.
     * 
     * Initial State:
     * - Empty status message
     * - RecyclerView hidden (until data loads)
     * 
     * TextWatcher:
     * - Triggers filterPlantNames() on each text change
     * - Provides instant search results
     */
    private void setupSearchLogic() {
        // Initial state (will be updated when data loads)
        binding.textViewSearchStatus.setText("");
        binding.textViewSearchStatus.setVisibility(View.GONE);
        binding.recyclerViewScientificNames.setVisibility(View.GONE);

        // Real-time search filtering
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
     * Filters plant names based on search query.
     * 
     * Filter Logic:
     * - Empty query: Show all plants (full list)
     * - Non-empty query: Filter by case-insensitive substring match
     * 
     * UI Updates:
     * - Empty results: Hide RecyclerView, show "No results found"
     * - Has results: Show RecyclerView, hide status message
     * 
     * Performance:
     * - Operates on cached allPlantCommonNames (no API calls)
     * - Uses Java Streams for efficient filtering
     * 
     * @param query Search text from search field
     */
    private void filterPlantNames(String query) {
        List<String> filteredList;

        if (query.isEmpty()) {
            // Empty query - show all plants
            filteredList = new ArrayList<>(allPlantCommonNames);
        } else {
            // Filter by case-insensitive substring match
            filteredList = allPlantCommonNames.stream()
                    .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        searchAdapter.updateData(filteredList);

        // Update visibility based on results
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
     * Sets up next button to navigate to camera capture.
     * 
     * Validation:
     * - Ensures plant name is not empty
     * - Shows error on empty field
     * 
     * Navigation:
     * - To: CaptureFragment (R.id.navigation_upload)
     * - Arguments: scientificName (selected/entered name), isFavouriteFlow
     * - Error handling for navigation failures
     */
    private void setupNextButton() {
        binding.imageView.setOnClickListener(v -> {
            String plantName = binding.searchScientificNameEditText.getText().toString().trim();
            if (plantName.isEmpty()) {
                binding.searchScientificNameEditText.setError("Please enter or select a name");
                return;
            }

            // Navigate to camera capture with plant name
            Log.d(TAG, "Navigating to CaptureFragment with plant name: " + plantName);
            Bundle args = new Bundle();
            args.putString("scientificName", plantName);
            args.putBoolean("isFavouriteFlow", isFavouriteFlow);

            try {
                navController.navigate(R.id.navigation_upload, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation failed.", e);
            }
        });
    }

    /**
     * Sets up back button to return to previous screen.
     * Checks for previous back stack entry before popping.
     */
    private void setupBackButton() {
        binding.backButtonAddPlant.setOnClickListener(v -> {
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            }
        });
    }

    /**
     * Cleans up view binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
