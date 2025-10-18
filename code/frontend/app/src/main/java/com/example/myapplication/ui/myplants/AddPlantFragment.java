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
import com.example.myapplication.network.PlantDto;

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

    // This will now hold the common names for the dropdown
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

        fetchAllPlantNamesFromServer();
        setupRecyclerView();
        setupSearchLogic();
        setupBackButton();
        setupNextButton();
    }

    private void fetchAllPlantNamesFromServer() {
        Log.d(TAG, "Fetching all plant names from server...");
        ApiService apiService = ApiClient.create(requireContext());
        Call<ApiResponse<List<PlantDto>>> call = apiService.getAllWikis();

        call.enqueue(new Callback<ApiResponse<List<PlantDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Response<ApiResponse<List<PlantDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allPlantCommonNames.clear();
                    List<String> fetchedNames = response.body().getData().stream()
                            .map(PlantDto::getScientificName)
                            .distinct()
                            .collect(Collectors.toList());

                    allPlantCommonNames.addAll(fetchedNames);
                    Log.d(TAG, "Successfully loaded " + allPlantCommonNames.size() + " unique plant names for searching.");
                } else {
                    Log.e(TAG, "Failed to fetch plant names. Code: " + response.code());
                    if(getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load plant list.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PlantDto>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error fetching plant names.", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupRecyclerView() {
        searchAdapter = new SearchResultAdapter(selectedPlantName -> {
            Log.d(TAG, "User selected: " + selectedPlantName);
            binding.searchScientificNameEditText.setText(selectedPlantName);
            binding.searchScientificNameEditText.setSelection(selectedPlantName.length());
            binding.recyclerViewScientificNames.setVisibility(View.GONE);

            // Hide the keyboard
            if (getContext() != null) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.searchScientificNameEditText.getWindowToken(), 0);
            }
        });

        binding.recyclerViewScientificNames.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewScientificNames.setAdapter(searchAdapter);
    }

    private void setupSearchLogic() {
        binding.textViewSearchStatus.setText("Search for a plant by its name.");
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
            binding.textViewSearchStatus.setText("Search for a plant by its name.");
            searchAdapter.updateData(new ArrayList<>());
            return;
        }

        List<String> filteredList = allPlantCommonNames.stream()
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
            // The value is now a common name, but the key is still "scientificName" for the next fragment.
            // This is acceptable if the next fragment is prepared to handle a common name.
            String plantName = binding.searchScientificNameEditText.getText().toString().trim();

            if (plantName.isEmpty()) {
                binding.searchScientificNameEditText.setError("Please enter or select a name");
                return;
            }

            Log.d(TAG, "Navigating to UploadFragment with plant name: " + plantName + " and isFavouriteFlow: " + isFavouriteFlow);
            Bundle args = new Bundle();
            args.putString("scientificName", plantName); // The key remains "scientificName"
            args.putBoolean("isFavouriteFlow", isFavouriteFlow);

            try {
                // The navigation action ID seems incorrect based on the class name.
                // Assuming you want to navigate from AddPlantFragment to UploadFragment.
                // Please verify this ID in your mobile_navigation.xml
                navController.navigate(R.id.navigation_upload_plant, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation failed. Check the action ID in your navigation graph.", e);
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
