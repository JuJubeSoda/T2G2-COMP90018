package com.example.myapplication.ui.myplants;

import android.os.Bundle;
// FIX: Use correct Retrofit imports
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
// FIX: Add necessary imports from MyGardenFragment
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.PlantDto;


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
    private PlantwikiBinding binding;
    private PlantCardAdapter plantCardAdapter;

    // This list holds all plants fetched from the server.
    private List<Plant> masterWikiList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantwikiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // FIX: Use correct method names
        setupWikiRecyclerView();
        setupWikiClickListeners();

        // Check if data already exists before fetching
        if (masterWikiList.isEmpty()) {
            Log.d(TAG, "Wiki list is empty. Fetching from server.");
            fetchWikiPlantsFromServer();
        } else {
            Log.d(TAG, "Wiki data exists. Displaying from master list.");
            displayWikiPlants();
        }
    }

    /**
     * Initializes the RecyclerView, its adapter, and the click listener for navigation.
     */
    // FIX: Renamed from setupRecyclerView to match the call in onViewCreated
    private void setupWikiRecyclerView() {
        if (binding.recyclerViewPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewPlants is NULL!");
            return;
        }

        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Log.d(TAG, "Wiki plant clicked: " + plant.getName() + ". Navigating to details.");
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
            try {
                // Navigate to the detail screen, assuming the action is defined in the nav graph
                Navigation.findNavController(requireView()).navigate(R.id.plantDetailFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to PlantDetailFragment from Wiki failed.", e);
                Toast.makeText(getContext(), "Could not open plant details.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerViewPlants.setAdapter(plantCardAdapter);
        // Default to a grid view with 2 columns
        binding.recyclerViewPlants.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    /**
     * Sets up the search logic and other click listeners.
     */
    // FIX: Renamed from setupSearch to match the call in onViewCreated
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

        // TODO: Add listeners for your list/grid view toggle buttons if they exist in plantwiki.xml
    }

    /**
     * FIX: This method was missing. It's responsible for displaying the current state of the data.
     * It simply calls displayFilteredPlants with the current search query.
     */
    private void displayWikiPlants() {
        if (binding != null) {
            displayFilteredPlants(binding.searchEditText.getText().toString());
        }
    }


    /**
     * Filters the master plant list based on a query and updates the RecyclerView.
     * @param query The search text entered by the user.
     */
    private void displayFilteredPlants(String query) {
        if (masterWikiList.isEmpty()) {
            binding.recyclerViewPlants.setVisibility(View.GONE);
            // TODO: Show an empty message TextView if you have one
            return;
        }

        binding.recyclerViewPlants.setVisibility(View.VISIBLE);
        List<Plant> filteredList;
        if (query.trim().isEmpty()) {
            filteredList = new ArrayList<>(masterWikiList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            filteredList = masterWikiList.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerCaseQuery) ||
                            p.getScientificName().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }
        plantCardAdapter.setPlants(new ArrayList<>(filteredList));
    }

    private void fetchWikiPlantsFromServer() {
        Log.d(TAG, "Fetching wiki plants from server...");
        // Show progress bar and hide the list
        binding.progressBarWiki.setVisibility(View.VISIBLE);
        // FIX: Use the correct RecyclerView ID from your XML
        binding.recyclerViewPlants.setVisibility(View.GONE);

        // FIX: Use the correct ApiService from your network package
        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantDto>>> call = apiService.getAllPlants();

        // FIX: Use the correct Retrofit Callback and Response classes
        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Response<ApiResponse<List<PlantDto>>> response) {
                if (binding == null) return; // Fragment was destroyed
                binding.progressBarWiki.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    masterWikiList.clear();
                    for (PlantDto dto : response.body().getData()) {
                        masterWikiList.add(dto.toPlant());
                    }
                    Log.d(TAG, "Successfully fetched " + masterWikiList.size() + " plants for wiki.");
                    displayWikiPlants();
                } else {
                    Log.e(TAG, "Failed to fetch wiki plants. Code: " + response.code());
                    Toast.makeText(getContext(), "Failed to load wiki.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Throwable t) {
                if (binding == null) return; // Fragment was destroyed
                binding.progressBarWiki.setVisibility(View.GONE);
                Log.e(TAG, "Network error fetching wiki plants.", t);
                Toast.makeText(getContext(), "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView: PlantWikiFragment binding pulled.");
    }
}
