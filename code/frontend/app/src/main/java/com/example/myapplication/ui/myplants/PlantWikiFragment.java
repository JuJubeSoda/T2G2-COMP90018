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

public class PlantWikiFragment extends Fragment {

    private static final String TAG = "PlantWikiFragment";
    private PlantwikiBinding binding;
    private PlantCardAdapter plantCardAdapter;

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

        setupWikiRecyclerView();
        setupWikiClickListeners();

        if (masterWikiList.isEmpty()) {
            Log.d(TAG, "Wiki list is empty. Fetching from server.");
            fetchWikiPlantsFromServer();
        } else {
            Log.d(TAG, "Wiki data exists. Displaying from master list.");
            displayWikiPlants();
        }
    }

    private void setupWikiRecyclerView() {
        if (binding.recyclerViewPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewPlants is NULL!");
            return;
        }

        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Log.d(TAG, "Wiki plant clicked: " + plant.getName() + ". Navigating to wiki tab.");
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant); // The key can be the same
            try {
                // --- THIS IS THE FIX ---
                // Provide the correct destination ID for the wiki flow.
                Navigation.findNavController(requireView()).navigate(R.id.plantwiki_maintab, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation to R.id.plantwiki_maintab failed. Check nav graph for this ID.", e);
                Toast.makeText(getContext(), "Could not open plant details.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerViewPlants.setAdapter(plantCardAdapter);
        binding.recyclerViewPlants.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

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

    private void displayWikiPlants() {
        if (binding != null) {
            displayFilteredPlants(binding.searchEditText.getText().toString());
        }
    }

    private void displayFilteredPlants(String query) {
        if (masterWikiList.isEmpty()) {
            binding.recyclerViewPlants.setVisibility(View.GONE);
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
                            (p.getScientificName() != null && p.getScientificName().toLowerCase().contains(lowerCaseQuery)))
                    .collect(Collectors.toList());
        }
        plantCardAdapter.setPlants(new ArrayList<>(filteredList));
    }

    private void fetchWikiPlantsFromServer() {
        binding.progressBarWiki.setVisibility(View.VISIBLE);
        binding.recyclerViewPlants.setVisibility(View.GONE);

        ApiService apiService = ApiClient.create(requireContext());
        // Use the getAllWikis endpoint as specified in the Swagger documentation
        Call<ApiResponse<List<PlantWikiDto>>> call = apiService.getAllWikis();

        call.enqueue(new Callback<ApiResponse<List<PlantWikiDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantWikiDto>>> call, @NonNull Response<ApiResponse<List<PlantWikiDto>>> response) {
                if (binding == null) return;
                binding.progressBarWiki.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    masterWikiList.clear();
                    for (PlantWikiDto dto : response.body().getData()) {
                        Plant plant = dto.toPlant();
                        masterWikiList.add(plant);
                        Log.d(TAG, "Added wiki plant: " + plant.getName() + " (Scientific: " + plant.getScientificName() + ")");
                        Log.d(TAG, "Plant features: " + dto.getFeatures());
                        Log.d(TAG, "Care guide: " + dto.getCareGuide());
                        Log.d(TAG, "Water needs: " + dto.getWaterNeeds());
                        Log.d(TAG, "Light needs: " + dto.getLightNeeds());
                        Log.d(TAG, "Difficulty: " + dto.getDifficulty());
                        Log.d(TAG, "Growth height: " + dto.getGrowthHeight());
                    }
                    Log.d(TAG, "Successfully fetched " + masterWikiList.size() + " plants for wiki from database.");
                    displayWikiPlants();
                } else {
                    Log.e(TAG, "Failed to fetch wiki plants. Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), "Failed to load wiki from database. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantWikiDto>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.progressBarWiki.setVisibility(View.GONE);
                Log.e(TAG, "Network error fetching wiki plants from database.", t);
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
