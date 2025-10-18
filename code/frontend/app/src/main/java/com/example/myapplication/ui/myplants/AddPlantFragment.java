package com.example.myapplication.ui.myplants;

import android.content.Context;
import android.os.Bundle;
// import android.telecom.Call; // --- FIX: Removed incorrect import ---
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
import com.example.myapplication.network.PlantDto;

// --- FIX: Add correct Retrofit 2 imports ---
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddPlantFragment extends Fragment {

    private static final String TAG = "AddPlantFragment";
    private AddplantBinding binding;
    private NavController navController;
    private SearchResultAdapter searchAdapter;

    private final List<String> allPlantScientificNames = new ArrayList<>();
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
            Log.d(TAG, "Received isFavouriteFlow: " + isFavouriteFlow);
        }

        fetchAllPlantNamesFromServer();
        setupRecyclerView();
        setupSearchLogic();
        setupBackButton();
        setupNextButton();
    }

    private void fetchAllPlantNamesFromServer() {
        Log.d(TAG, "Fetching all plant names from server...");
        // You could show a progress bar here
        // binding.progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantDto>>> call = apiService.getAllPlants();

        // --- FIX: Use the correct retrofit2.Callback and retrofit2.Response ---
        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Response<ApiResponse<List<PlantDto>>> response) {
                // if (binding != null) binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allPlantScientificNames.clear();
                    List<String> fetchedNames = response.body().getData().stream()
                            // --- THIS IS THE FIX ---
                            // Changed from PlantDto::scientificName to PlantDto::getScientificName
                            .map(PlantDto::getScientificName)
                            .distinct()
                            .collect(Collectors.toList());
                    allPlantScientificNames.addAll(fetchedNames);
                    Log.d(TAG, "Successfully loaded " + allPlantScientificNames.size() + " unique plant names for searching.");
                } else {
                    Log.e(TAG, "Failed to fetch plant names. Code: " + response.code());
                    if(getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load plant list.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Throwable t) {
                // ... (rest of the method is correct)
            }
        });
    }

    private void setupRecyclerView() {
        searchAdapter = new SearchResultAdapter(scientificName -> {
            Log.d(TAG, "User selected: " + scientificName);
            binding.searchScientificNameEditText.setText(scientificName);
            binding.searchScientificNameEditText.setSelection(scientificName.length());
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
        binding.textViewSearchStatus.setText("Search for a plant by its scientific name.");
        binding.textViewSearchStatus.setVisibility(View.VISIBLE);
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
        if (query.isEmpty()) {
            binding.recyclerViewScientificNames.setVisibility(View.GONE);
            binding.textViewSearchStatus.setVisibility(View.VISIBLE);
            binding.textViewSearchStatus.setText("Search for a plant by its scientific name.");
            searchAdapter.updateData(new ArrayList<>());
            return;
        }

        List<String> filteredList = allPlantScientificNames.stream()
                .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        searchAdapter.updateData(filteredList);

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
            String scientificName = binding.searchScientificNameEditText.getText().toString().trim();

            if (scientificName.isEmpty()) {
                binding.searchScientificNameEditText.setError("Please enter or select a name");
                return;
            }

            Log.d(TAG, "Navigating to CaptureFragment with scientific name: " + scientificName + " and isFavouriteFlow: " + isFavouriteFlow);
            Bundle args = new Bundle();
            args.putString("scientificName", scientificName);
            args.putBoolean("isFavouriteFlow", isFavouriteFlow);

            try {
                // Ensure this action ID is correct in your `mobile_navigation.xml`
                navController.navigate(R.id.action_navigation_home_to_uploadFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation failed. Check ID 'action_addPlantFragment_to_captureFragment' in nav graph.", e);
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Error: Cannot proceed.", Toast.LENGTH_SHORT).show();
                }
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
