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
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.User;
import com.example.myapplication.ui.myplants.share.Plant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

            // Set initial placeholder for "Discovered by" before API call
            binding.textViewDiscoveredBy.setText("User #" + plant.getUserId());
            
            // Fetch username from userId by calling getAllUsers API (async)
            fetchAndDisplayUsername(plant.getUserId());
            
            // Display discovery date as-is from backend
            String createdAt = plant.getCreatedAt();
            if (createdAt == null || createdAt.isEmpty()) {
                binding.textViewDiscoveredOn.setText("Date not available");
            } else {
                binding.textViewDiscoveredOn.setText(createdAt);
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
    
    /**
     * Fetches all users from backend and finds username by userId.
     *
     * WARNING: This endpoint may return large responses with avatar data.
     * If memory issues occur, consider:
     * 1. Backend should provide GET /user/{userId} endpoint
     * 2. Backend should exclude avatarData from list responses
     * 3. Use caching to avoid repeated calls
     *
     * Process:
     * 1. Call GET /user to get all users
     * 2. Find user with matching userId
     * 3. Display username in "Discovered by" field
     * 4. Fallback to "User #[userId]" if API fails or user not found
     *
     * @param userId The userId to look up
     */
    private void fetchAndDisplayUsername(int userId) {
        Log.d(TAG, "fetchAndDisplayUsername called with userId: " + userId);

        // Verify binding is not null
        if (binding == null || binding.textViewDiscoveredBy == null) {
            Log.e(TAG, "Binding or textViewDiscoveredBy is null!");
            return;
        }

        // Set loading state
        binding.textViewDiscoveredBy.setText("Loading...");
        Log.d(TAG, "Set text to 'Loading...'");

        try {
            ApiService apiService = ApiClient.create(getContext());
            Call<ApiResponse<List<User>>> call = apiService.getAllUsers();

            call.enqueue(new Callback<ApiResponse<List<User>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                    if (!isAdded()) return; // Fragment detached

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            List<User> users = response.body().getData();
                            if (users != null) {
                                // Find user with matching userId
                                String username = null;
                                for (User user : users) {
                                    if (user.getUserId() != null && user.getUserId() == userId) {
                                        username = user.getUsername();
                                        break;
                                    }
                                }

                                // Display username or fallback
                                if (username != null && !username.isEmpty()) {
                                    binding.textViewDiscoveredBy.setText(username);
                                    Log.d(TAG, "Found username: " + username + " for userId: " + userId);
                                } else {
                                    binding.textViewDiscoveredBy.setText("User #" + userId);
                                    Log.w(TAG, "Username not found for userId: " + userId);
                                }
                            } else {
                                binding.textViewDiscoveredBy.setText("User #" + userId);
                                Log.w(TAG, "User list is null");
                            }
                        } else {
                            binding.textViewDiscoveredBy.setText("User #" + userId);
                            Log.e(TAG, "Failed to fetch users: " + response.message());
                        }
                    } catch (OutOfMemoryError e) {
                        // Handle OOM from large response
                        binding.textViewDiscoveredBy.setText("User #" + userId);
                        Log.e(TAG, "OutOfMemoryError while parsing user list - response too large", e);
                    } catch (Exception e) {
                        binding.textViewDiscoveredBy.setText("User #" + userId);
                        Log.e(TAG, "Error parsing user response", e);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                    if (!isAdded()) return; // Fragment detached

                    binding.textViewDiscoveredBy.setText("User #" + userId);

                    if (t instanceof OutOfMemoryError) {
                        Log.e(TAG, "OutOfMemoryError fetching users - response too large. Backend should exclude avatarData.", t);
                    } else {
                        Log.e(TAG, "Network error fetching users", t);
                    }
                }
            });
        } catch (OutOfMemoryError e) {
            // Catch OOM even before API call
            binding.textViewDiscoveredBy.setText("User #" + userId);
            Log.e(TAG, "OutOfMemoryError before API call", e);
        } catch (Exception e) {
            binding.textViewDiscoveredBy.setText("User #" + userId);
            Log.e(TAG, "Error setting up API call", e);
        }
    }
}


