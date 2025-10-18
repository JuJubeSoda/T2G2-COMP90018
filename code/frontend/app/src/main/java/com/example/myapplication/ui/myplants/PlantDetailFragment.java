package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantdetailBinding; // This will be generated from plantdetail.xml

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * PlantDetailFragment displays the details of a single plant collected by the user.
 * It is used by the MyGardenFragment and uses the simple plantdetail.xml layout.
 */
public class PlantDetailFragment extends Fragment {

    // A unique key to pass the Plant object in a Bundle.
    public static final String ARG_PLANT = "plant_argument";
    private static final String TAG = "PlantDetailFragment";

    // The binding class is generated from your plantdetail.xml layout.
    private PlantdetailBinding binding;
    private Plant plant;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Safely retrieve the Plant object passed from MyGardenFragment.
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }

        // If no plant data is received, log an error and prevent the fragment from loading.
        if (plant == null) {
            Log.e(TAG, "CRITICAL: Plant object is null. Cannot display details.");
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
            // Navigate back to the previous screen.
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the correct layout (plantdetail.xml) using ViewBinding.
        binding = PlantdetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the plant object is valid, populate the UI elements.
        if (plant != null) {
            populateUi();
        }

        // Set up the back button listener.
        binding.backButtonDetail.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    /**
     * Populates the views in the layout with data from the 'plant' object.
     */
    private void populateUi() {
        // Set text fields from the Plant object.
        binding.textViewScientificName.setText(plant.getScientificName());
        binding.textViewIntroduction.setText(plant.getIntroduction()); // Assuming description maps to introduction
        binding.textViewLocation.setText(plant.getLocation()); // Assuming you have a getLocation() method
        binding.textViewSearchTag.setText(String.join(", ", plant.getSearchTag())); // Assuming getTags() returns a List<String>
        binding.textViewDiscoveredBy.setText(plant.getDiscoveredBy()); // Assuming you have a getDiscoveredBy()

        // Format and set the date.
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(plant.getDiscoveredOn()));
            binding.textViewDiscoveredOn.setText(formattedDate);
        } catch (Exception e) {
            Log.e(TAG, "Failed to format discovery date.", e);
            binding.textViewDiscoveredOn.setText("N/A");
        }


        // Load the plant image using Glide.
        String base64Image = plant.getImageUrl();
        try {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Glide.with(this)
                    .load(imageBytes)
                    .placeholder(R.drawable.plantbulb_foreground) // A default drawable
                    .error(R.drawable.plantbulb_foreground)       // A drawable for if the load fails
                    .into(binding.imageViewPlantPreview);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode or load image for plant: " + plant.getName(), e);
            // Set a default error image if Glide fails.
            binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set the binding to null to prevent memory leaks.
        binding = null;
    }
}

