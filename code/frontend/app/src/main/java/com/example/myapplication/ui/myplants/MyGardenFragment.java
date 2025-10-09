
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyGardenFragment extends Fragment {

    private static final String TAG = "MyGardenFragment";
    private MygardenlistBinding binding;
    private PlantCardAdapter plantCardAdapter;

    // This list holds the master data fetched from the backend.
    private List<Plant> masterPlantList = new ArrayList<>();
    private boolean currentViewIsFavourites = false;
    private boolean isCurrentlyListView = false;
    private String currentSearchText = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Data is now fetched in onViewCreated, not here.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MygardenlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created and listeners being set up.");

        setupRecyclerView();
        setupClickListeners();

        if (savedInstanceState == null) {
            currentViewIsFavourites = false;
            switchToGridView();
            fetchPlantsFromServer(); // Fetch data from the server.
        }
        updateToggleButtonsVisualState();
    }

    private void fetchPlantsFromServer() {
        Log.d(TAG, "Fetching plants from server...");
        // TODO: Add a ProgressBar with id 'progressBar' to your mygardenlist.xml layout file.
        // binding.progressBar.setVisibility(View.VISIBLE); // Show progress bar

        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantDto>>> call = apiService.getPlantsByUser();

        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlantDto>>> call, Response<ApiResponse<List<PlantDto>>> response) {
                // TODO: Add a ProgressBar with id 'progressBar' to your mygardenlist.xml layout file.
                // binding.progressBar.setVisibility(View.GONE); // Hide progress bar
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Log.d(TAG, "API call successful. Received " + response.body().getData().size() + " plants.");
                    masterPlantList.clear();
                    // Convert each PlantDto to a Plant object and add to the master list.
                    for (PlantDto dto : response.body().getData()) {
                        masterPlantList.add(dto.toPlant());
                    }
                    displayPlants(); // Refresh the UI with the new data.
                } else {
                    Log.e(TAG, "API call failed or returned empty data. Code: " + response.code());
                    Toast.makeText(getContext(), "Failed to load plants: " + response.message(), Toast.LENGTH_SHORT).show();
                    updateEmptyViewVisibility(true); // Show empty view on failure
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlantDto>>> call, Throwable t) {
                // TODO: Add a ProgressBar with id 'progressBar' to your mygardenlist.xml layout file.
                // binding.progressBar.setVisibility(View.GONE); // Hide progress bar
                Log.e(TAG, "API call failed due to network error.", t);
                Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
                updateEmptyViewVisibility(true); // Show empty view on failure
            }
        });
    }

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
            displayPlants();
        });

        binding.imageButton4.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putBoolean("isFavouriteFlow", currentViewIsFavourites);
            navController.navigate(R.id.action_myGardenFragment_to_addPlantFragment, args);
        });
    }

    private void setupRecyclerView() {
        if (binding.recyclerViewMyGardenPlants == null) {
            Log.e(TAG, "CRITICAL: recyclerViewMyGardenPlants is NULL!");
            return;
        }

        plantCardAdapter = new PlantCardAdapter(requireContext(), new ArrayList<>(), plant -> {
            Log.d(TAG, "Plant clicked: " + plant.getName() + ". Navigating to details.");
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
            Navigation.findNavController(requireView()).navigate(R.id.plantDetailFragment, args);
        });

        binding.recyclerViewMyGardenPlants.setAdapter(plantCardAdapter);
    }

    private void switchToListView() {
        binding.recyclerViewMyGardenPlants.setLayoutManager(new LinearLayoutManager(getContext()));
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_LIST_WITH_DATE);
        isCurrentlyListView = true;
        updateToggleButtonsVisualState();
    }

    private void switchToGridView() {
        binding.recyclerViewMyGardenPlants.setLayoutManager(new GridLayoutManager(getContext(), 2));
        plantCardAdapter.setViewType(PlantCardAdapter.VIEW_TYPE_GRID);
        isCurrentlyListView = false;
        updateToggleButtonsVisualState();
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

    private void displayPlants() {
        if (plantCardAdapter == null) {
            Log.e(TAG, "displayPlants: plantCardAdapter is null.");
            return;
        }

        List<Plant> plantsToShow;

        // Step 1: Filter by Favourites (from the master list)
        if (currentViewIsFavourites) {
            plantsToShow = masterPlantList.stream()
                    .filter(Plant::isFavourite)
                    .collect(Collectors.toList());
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
        binding = null;
    }
}
