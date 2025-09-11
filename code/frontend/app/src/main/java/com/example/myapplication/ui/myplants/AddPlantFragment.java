package com.example.myapplication.ui.myplants; // Or your preferred package

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.databinding.AddplantBinding; // ViewBinding class for addplant.xml

public class AddPlantFragment extends Fragment {

    private AddplantBinding binding;

    public static AddPlantFragment newInstance() {
        return new AddPlantFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AddplantBinding.inflate(inflater, container, false);
        // TODO: Setup views from addplant.xml using binding.
        // e.g., binding.editTextPlantName, binding.buttonSavePlant
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Set up listeners and logic for your Add Plant screen
        // For example, a save button click:
        // binding.buttonSavePlant.setOnClickListener(v -> {
        //     // Get data from input fields
        //     // Save the new plant
        //     // Navigate back or show success message
        // });

        // Example: Set a toolbar title if you have one in addplant.xml
        // if (getActivity() instanceof AppCompatActivity) {
        //     ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Add New Plant");
        // }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important for ViewBinding with Fragments
    }
}
