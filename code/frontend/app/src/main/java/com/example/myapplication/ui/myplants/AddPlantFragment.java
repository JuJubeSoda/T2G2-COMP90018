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

public class AddPlantFragment extends Fragment {

    private static final String TAG = "AddPlantFragment";
    private AddplantBinding binding;
    private NavController navController;
    private SearchResultAdapter searchAdapter;

    private final List<String> allPlantCommonNames = new ArrayList<>();
    private boolean isFavouriteFlow = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddplantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        if (getArguments() != null) {
            isFavouriteFlow = getArguments().getBoolean("isFavouriteFlow", false);
        }

        setupRecyclerView();
        setupSearchLogic();
        fetchAllPlantNamesFromServer(); // Moved to be after setup
        setupBackButton();
        setupNextButton();
    }

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
                    allPlantCommonNames.clear();
                    List<String> fetchedNames = response.body().getData().stream()
                            .map(PlantWikiDto::getName)
                            .filter(name -> name != null && !name.isEmpty())
                            .distinct()
                            .collect(Collectors.toList());

                    allPlantCommonNames.addAll(fetchedNames);
                    Log.d(TAG, "Successfully loaded " + allPlantCommonNames.size() + " unique plant names.");

                    // --- FIX 1: Show the full list immediately after fetching ---
                    searchAdapter.updateData(allPlantCommonNames);
                    binding.recyclerViewScientificNames.setVisibility(View.VISIBLE);
                    binding.textViewSearchStatus.setVisibility(View.GONE);

                } else {
                    Log.e(TAG, "Failed to fetch plant names. Code: " + response.code());
                    binding.textViewSearchStatus.setText("Failed to load plant list. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantWikiDto>>> call, @NonNull Throwable t) {
                if (binding == null) return; // View destroyed, do nothing
                binding.progressBarSearch.setVisibility(View.GONE);
                binding.textViewSearchStatus.setText("Network error. Please check connection.");
                Log.e(TAG, "Network error fetching plant names.", t);
            }
        });
    }

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

    private void setupSearchLogic() {
        // Initial state message, will be replaced on data load
        binding.textViewSearchStatus.setText("");
        binding.textViewSearchStatus.setVisibility(View.GONE);
        binding.recyclerViewScientificNames.setVisibility(View.GONE);

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

    private void filterPlantNames(String query) {
        // --- FIX 2: Simplified filtering logic ---
        List<String> filteredList;

        if (query.isEmpty()) {
            // If the query is empty, show the full list
            filteredList = new ArrayList<>(allPlantCommonNames);
        } else {
            // Otherwise, show the filtered list
            filteredList = allPlantCommonNames.stream()
                    .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        searchAdapter.updateData(filteredList);

        // Update visibility based on whether the list is empty
        if (filteredList.isEmpty()) {
            binding.recyclerViewScientificNames.setVisibility(View.GONE);
            binding.textViewSearchStatus.setText("No results found.");
            binding.textViewSearchStatus.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewScientificNames.setVisibility(View.VISIBLE);
            binding.textViewSearchStatus.setVisibility(View.GONE);
        }
    }

    private void setupNextButton() {
        binding.imageView.setOnClickListener(v -> {
            String plantName = binding.searchScientificNameEditText.getText().toString().trim();
            if (plantName.isEmpty()) {
                binding.searchScientificNameEditText.setError("Please enter or select a name");
                return;
            }

            Log.d(TAG, "Navigating to UploadFragment with plant name: " + plantName);
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

    private void setupBackButton() {
        binding.backButtonAddPlant.setOnClickListener(v -> {
            if (navController.getPreviousBackStackEntry() != null) {
                navController.popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
