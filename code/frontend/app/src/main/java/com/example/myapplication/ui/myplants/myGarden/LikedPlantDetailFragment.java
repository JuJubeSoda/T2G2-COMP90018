package com.example.myapplication.ui.myplants.myGarden;

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
import com.example.myapplication.databinding.LikedPlantDetailBinding;
import com.example.myapplication.myPlantsData.MyGardenDataManager;
import com.example.myapplication.ui.myplants.share.Plant;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LikedPlantDetailFragment extends Fragment {

    public static final String ARG_PLANT = "plant_argument";
    private static final String TAG = "LikedPlantDetailFragment";

    private LikedPlantDetailBinding binding;
    private Plant plant;
    private MyGardenDataManager myGardenDataManager;
    private Boolean currentLiked = true; // default for MyFavourite

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }
        if (plant == null) {
            Toast.makeText(getContext(), "Error: Could not load plant data.", Toast.LENGTH_LONG).show();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LikedPlantDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myGardenDataManager = new MyGardenDataManager(requireContext());

        if (plant != null) {
            populateUi();
        }

        binding.backButtonDetail.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Default Unlike (since MyFavourite)
        binding.btnLikeToggle.setText("Unlike");
        binding.btnLikeToggle.setOnClickListener(v -> toggleLike());
        binding.btnShowOnMap.setOnClickListener(v -> navigateToMap());
    }

    private void populateUi() {
        try {
            binding.textViewScientificName.setText(plant.getScientificName() != null ? plant.getScientificName() : "Not available");
            binding.textViewIntroduction.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");
            String locationString = "Location not available";
            if (plant.getLatitude() != null && plant.getLongitude() != null) {
                locationString = String.format(Locale.getDefault(), "(%.4f, %.4f)", plant.getLatitude(), plant.getLongitude());
            }
            binding.textViewLocation.setText(locationString);
            String tags = "No tags";
            List<String> plantTags = plant.getTags();
            if (plantTags != null && !plantTags.isEmpty()) {
                tags = plantTags.stream().collect(Collectors.joining(", "));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error populating UI", e);
        }

        try {
            String base64Image = plant.getImageUrl();
            if (base64Image == null || base64Image.isEmpty()) {
                binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
            } else {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Glide.with(this)
                        .load(imageBytes)
                        .placeholder(R.drawable.plantbulb_foreground)
                        .error(R.drawable.plantbulb_foreground)
                        .into(binding.imageViewPlantPreview);
            }
        } catch (Exception e) {
            binding.imageViewPlantPreview.setImageResource(R.drawable.plantbulb_foreground);
        }
    }

    private void toggleLike() {
        if (plant == null) return;
        boolean liked = currentLiked != null && currentLiked;
        if (liked) {
            binding.btnLikeToggle.setEnabled(false);
            Toast.makeText(getContext(), "Unliking...", Toast.LENGTH_SHORT).show();
            myGardenDataManager.unlikePlant(plant.getPlantId(), new MyGardenDataManager.DataCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    currentLiked = false;
                    binding.btnLikeToggle.setText("Like");
                    binding.btnLikeToggle.setEnabled(true);
                    Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    binding.btnLikeToggle.setEnabled(true);
                    Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            binding.btnLikeToggle.setEnabled(false);
            Toast.makeText(getContext(), "Liking...", Toast.LENGTH_SHORT).show();
            myGardenDataManager.likePlant(plant.getPlantId(), new MyGardenDataManager.DataCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    currentLiked = true;
                    binding.btnLikeToggle.setText("Unlike");
                    binding.btnLikeToggle.setEnabled(true);
                    Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    binding.btnLikeToggle.setEnabled(true);
                    Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToMap() {
        if (plant == null) return;
        try {
            Bundle args = new Bundle();
            args.putInt("plantId", plant.getPlantId());
            Navigation.findNavController(requireView()).navigate(R.id.navigation_plant_map, args);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open map", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


