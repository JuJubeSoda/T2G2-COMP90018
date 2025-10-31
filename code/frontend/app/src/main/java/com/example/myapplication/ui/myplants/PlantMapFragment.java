package com.example.myapplication.ui.myplants;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantDto;
import com.example.myapplication.network.PlantMapDto;
import com.example.myapplication.map.PlantGardenMapManager;
import com.example.myapplication.map.MapDataManager;
import com.example.myapplication.ui.map.PlantBottomSheetDialogFragment;
import com.example.myapplication.ui.map.GardenBottomSheetDialogFragment;
import androidx.navigation.Navigation;

import java.util.List;

public class PlantMapFragment extends Fragment implements OnMapReadyCallback, com.example.myapplication.ui.map.PlantBottomSheetDialogFragment.OnPlantActionListener, com.example.myapplication.ui.map.GardenBottomSheetDialogFragment.OnGardenActionListener {

    private static final String TAG = "PlantMapFragment";
    private GoogleMap mMap;
    private PlantGardenMapManager plantGardenMapManager;
    
    
    
    // Bottom sheet UI components
    // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
    // 在 onPlantClick 实现中：
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // 在 onGardenClick 实现中：
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    
    // Refresh control buttons
    private Button btnRefreshData;
    private Button btnToggleDataType;
    private Button btnBackToGardens;
    
    // Current coordinates for navigation
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    
    // Current plant for like functionality
    private PlantMapDto currentPlant = null;
    // Local like state (null = unknown, true = liked, false = not liked)
    private Boolean currentLiked = null;
    
    // UI related fields
    private FloatingActionButton fabPlaces;
    

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plant_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Request location permission with an official prompt before using location
        checkAndRequestLocationPermission();


        // Initialize bottom sheet components
        // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
        // 在 onPlantClick 实现中：
        // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
        // 在 onGardenClick 实现中：
        // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
        
        // Initialize refresh control buttons
        initializeRefreshControls(view);
        
        // Initialize places button
        initializePlacesButton(view);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void checkAndRequestLocationPermission() {
        Log.d(TAG, "检查定位权限...");
        int requestCode = com.example.myapplication.map.MapLocationManager.getLocationPermissionRequestCode();
        boolean hasPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "定位权限状态: " + (hasPermission ? "已授予" : "未授予"));
        
        if (!hasPermission) {
            Log.d(TAG, "显示权限请求对话框");
            new AlertDialog.Builder(requireContext())
                .setTitle("位置权限请求")
                .setMessage("我们需要您的位置权限以显示当前位置和附近植物。请允许以获得完整体验。")
                .setPositiveButton("继续", (dialog, which) -> {
                    Log.d(TAG, "用户点击继续，请求权限");
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    Log.d(TAG, "用户取消权限请求");
                })
                .show();
        } else {
            Log.d(TAG, "已有权限，无需请求");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Initialize Plant Garden Map Manager
        plantGardenMapManager = new PlantGardenMapManager(requireContext(), mMap);
        plantGardenMapManager.setOnPlantGardenMapInteractionListener(new PlantGardenMapManager.OnPlantGardenMapInteractionListener() {
            @Override
            public void onPlantClick(PlantMapDto plant) {
                Log.d(TAG, "onPlantClick fired for: " + plant.getName() + " (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
                // 使用BottomSheetDialogFragment展示植物信息
                PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
            }
            
            @Override
            public void onGardenClick(GardenDto garden) {
                Log.d(TAG, "onGardenClick fired for: " + garden.getName());
                // 使用BottomSheetDialogFragment展示花园信息
                GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
            }
            
            @Override
            public void onPlantsFound(List<PlantMapDto> plants) {
                Log.d(TAG, "=== onPlantsFound Callback Debug ===");
                Log.d(TAG, "Received plants: " + (plants == null ? "null" : plants.size()));
                if (plants != null && !plants.isEmpty()) {
                    Log.d(TAG, "First plant: " + plants.get(0).getName());
                    Log.d(TAG, "Coordinates: (" + plants.get(0).getLatitude() + ", " + plants.get(0).getLongitude() + ")");
                }
                Log.d(TAG, "PlantGardenMapManager instance: " + (plantGardenMapManager == null ? "null" : "available"));
                Log.d(TAG, "=== End onPlantsFound Callback Debug ===");
                // 植物数据已由MapDisplayManager自动显示在地图上
            }
            
            @Override
            public void onGardensFound(List<GardenDto> gardens) {
                // 花园数据已由MapDisplayManager自动显示在地图上
            }
            
            @Override
            public void onSearchError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onDataTypeChanged(boolean isShowingPlants) {
                String message = isShowingPlants ? "Switched to Plants view" : "Switched to Gardens view";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onPlantLiked(boolean liked) {
                String message = liked ? "Plant liked! ❤️" : "Plant unliked";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // sync local state with server result
                currentLiked = liked;
                updateLikeButton();
            }
            
            @Override
            public void onPlantLikeError(String message) {
                Toast.makeText(getContext(), "Failed to like plant: " + message, Toast.LENGTH_SHORT).show();
            }
            
            public void onMapRadiusChanged(int newRadius) {
                // 可以在这里显示半径变化信息，或者实现自动重新搜索
                Log.d(TAG, "Map radius changed to: " + newRadius + " meters");
                // 可选：显示半径变化提示
                // Toast.makeText(getContext(), "Search radius: " + newRadius + "m", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Initialize map with location services
        plantGardenMapManager.initializeMap();

        // If a plantId was provided, center and show that plant on the map
        Bundle args = getArguments();
        if (args != null && args.containsKey("plantId")) {
            int plantId = args.getInt("plantId");
            // Ensure in plants mode
            if (!plantGardenMapManager.isShowingPlants()) {
                plantGardenMapManager.toggleDataType();
            }
            MapDataManager dataManager = new MapDataManager(requireContext());
            dataManager.getPlantById(plantId, new MapDataManager.MapDataCallback<com.example.myapplication.network.PlantDto>() {
                @Override
                public void onSuccess(com.example.myapplication.network.PlantDto plantDto) {
                    com.example.myapplication.network.PlantMapDto mapDto = com.example.myapplication.network.PlantMapDto.fromPlantDto(plantDto);
                    java.util.ArrayList<com.example.myapplication.network.PlantMapDto> list = new java.util.ArrayList<>();
                    list.add(mapDto);
                    plantGardenMapManager.refreshPlants(list);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Failed to load plant on map: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    

    /**
     * Initialize the bottom sheet components
     */
    // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
    // 在 onPlantClick 实现中：
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // 在 onGardenClick 实现中：
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    
    /**
     * Initialize floating action button for places
     */
    private void initializePlacesButton(View view) {
        fabPlaces = view.findViewById(R.id.fab_places);
        if (fabPlaces != null) {
            fabPlaces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCurrentPlace();
                }
            });
        }
    }
    
    /**
     * Initialize refresh control buttons
     */
    private void initializeRefreshControls(View view) {
        btnRefreshData = view.findViewById(R.id.btn_refresh_data);
        btnToggleDataType = view.findViewById(R.id.btn_toggle_data_type);
        btnBackToGardens = view.findViewById(R.id.btn_back_to_gardens);
        
        if (btnRefreshData != null) {
            btnRefreshData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshCurrentData();
                }
            });
        }
        
        
        if (btnToggleDataType != null) {
            btnToggleDataType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleDataType();
                    updateDataTypeButtonText();
                }
            });
            updateDataTypeButtonText();
        }

        if (btnBackToGardens != null) {
            btnBackToGardens.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (plantGardenMapManager != null) {
                        plantGardenMapManager.restoreGardensView();
                    }
                }
            });
        }
    }
    
    
    /**
     * Update the data type button text based on current state
     */
    private void updateDataTypeButtonText() {
        if (btnToggleDataType != null && plantGardenMapManager != null) {
            if (plantGardenMapManager.isShowingPlants()) {
                btnToggleDataType.setText("🌱 Plants");
                btnToggleDataType.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light));
            } else {
                btnToggleDataType.setText("🌿 Gardens");
                btnToggleDataType.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light));
            }
        }
    }
    
    /**
     * Toggle between plants and gardens data type
     */
    private void toggleDataType() {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.toggleDataType();
            // 自动触发一次加载，Garden 模式会全量拉取
            plantGardenMapManager.searchNearbyData();
        }
    }


    /**
     * Hide the bottom sheet
     */
    // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
    // 在 onPlantClick 实现中：
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // 在 onGardenClick 实现中：
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");

    /**
     * Check if bottom sheet is visible
     */
    // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
    // 在 onPlantClick 实现中：
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // 在 onGardenClick 实现中：
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");

    /**
     * Open navigation to the current location
     */
    private void openNavigationToLocation() {
        if (currentLat == 0.0 && currentLng == 0.0) {
            Toast.makeText(getContext(), "No location available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create navigation intent
            String navigationUri = String.format("google.navigation:q=%f,%f&mode=driving", 
                currentLat, currentLng);
            
            Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUri));
            navigationIntent.setPackage("com.google.android.apps.maps");
            
            // Check if Google Maps is available
            if (navigationIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(navigationIntent);
                Log.d(TAG, "Opening Google Maps navigation to: " + currentLat + ", " + currentLng);
            } else {
                // Fallback to web-based navigation
                String webNavigationUri = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", 
                    currentLat, currentLng);
                
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webNavigationUri));
                startActivity(webIntent);
                Log.d(TAG, "Opening web-based navigation to: " + currentLat + ", " + currentLng);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to open navigation", e);
            Toast.makeText(getContext(), "Unable to open navigation", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (requestCode == com.example.myapplication.map.MapLocationManager.getLocationPermissionRequestCode()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "已获得位置权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "未授权位置权限，将无法准确显示定位", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    

    /**
     * 刷新当前数据 - 直接调用现有方法
     */
    private void refreshCurrentData() {
        if (plantGardenMapManager == null) {
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // 仅在植物模式检查位置权限；花园模式不需要
        if (plantGardenMapManager.isShowingPlants() && !plantGardenMapManager.hasLocationPermission()) {
            Toast.makeText(getContext(), "Location permission required to refresh plants", Toast.LENGTH_SHORT).show();
            checkAndRequestLocationPermission();
            return;
        }

        // 显示刷新消息
        String refreshMessage = plantGardenMapManager.isShowingPlants() ? "Refreshing plants data..." : "Refreshing gardens data...";
        Toast.makeText(getContext(), refreshMessage, Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Refreshing current data using existing searchNearbyData() method...");
        
        // 直接调用现有的搜索方法
        plantGardenMapManager.searchNearbyData();
    }

    /**
     * 定位到当前位置 - 移动地图到用户位置
     */
    private void showCurrentPlace() {
        if (plantGardenMapManager == null) {
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查位置权限
        if (!plantGardenMapManager.hasLocationPermission()) {
            Toast.makeText(getContext(), "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示定位消息
        Toast.makeText(getContext(), "Moving to your current location...", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Moving map to current location...");
        
        // 移动地图到当前位置
        plantGardenMapManager.getLocationManager().getCurrentLocationAndMove();
    }
    
    
    

    
    /**
     * 获取完整的植物详情并导航到详情页面
     */
    private void getFullPlantDetailsAndNavigate(int plantId) {
        Log.d(TAG, "Getting full plant details for ID: " + plantId);
        
        // Show loading indicator
        Toast.makeText(getContext(), "Loading plant details...", Toast.LENGTH_SHORT).show();
        
        // Use existing plantGardenMapManager's dataManager if available, otherwise create new instance
        if (plantGardenMapManager == null) {
            Log.e(TAG, "PlantGardenMapManager is null, cannot get plant details");
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create a temporary MapDataManager for getting plant details
        // Note: This is independent of the map display flow
        MapDataManager dataManager = new MapDataManager(requireContext());
        
        // Call API to get full plant details
        dataManager.getPlantById(plantId, new MapDataManager.MapDataCallback<PlantDto>() {
            @Override
            public void onSuccess(PlantDto plantDto) {
                Log.d(TAG, "Successfully retrieved plant details: " + plantDto.getName());
                
                // Convert PlantDto to Plant object
                com.example.myapplication.ui.myplants.Plant plant = plantDto.toPlant();
                
                // Navigate to PlantDetailFragment
                navigateToPlantDetail(plant);
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to get plant details: " + message);
                Toast.makeText(getContext(), "Failed to load plant details: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * 导航到植物详情页面
     */
    private void navigateToPlantDetail(com.example.myapplication.ui.myplants.Plant plant) {
        try {
            Log.d(TAG, "Navigating to plant detail for: " + plant.getName());
            
            // Create bundle with plant data
            Bundle args = new Bundle();
            args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
            
            // Navigate to PlantDetailFragment
            Navigation.findNavController(requireView()).navigate(R.id.plantDetailFragment, args);
            
            // Hide bottom sheet after navigation
            // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
            // 在 onPlantClick 实现中：
            // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
            // 在 onGardenClick 实现中：
            // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to navigate to plant detail", e);
            Toast.makeText(getContext(), "Failed to open plant details", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示植物信息底部弹窗
     */
    // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
    // 在 onPlantClick 实现中：
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // 在 onGardenClick 实现中：
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    
    /**
     * 显示花园信息底部弹窗
     */
    // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
    // 在 onPlantClick 实现中：
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // 在 onGardenClick 实现中：
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    
    /**
     * Handle like button click
     */
    // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
    // 在 onPlantClick 实现中：
    // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
    // 在 onGardenClick 实现中：
    // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    
    /**
     * Like a plant
     */
    private void likePlant(int plantId) {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.likePlant(plantId);
        } else {
            Toast.makeText(getContext(), "Map manager not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Unlike a plant
     */
    private void unlikePlant(int plantId) {
        if (plantGardenMapManager != null) {
            plantGardenMapManager.unlikePlant(plantId);
        } else {
            Toast.makeText(getContext(), "Map manager not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Update like button appearance based on current state
     */
    private void updateLikeButton() {
        // 移除 LinearLayout bottomSheetContainer 及其相关变量和逻辑。
        // 在 onPlantClick 实现中：
        // PlantBottomSheetDialogFragment.newInstance(plant).show(getChildFragmentManager(), "plant_sheet");
        // 在 onGardenClick 实现中：
        // GardenBottomSheetDialogFragment.newInstance(garden).show(getChildFragmentManager(), "garden_sheet");
    }

    // ==== BottomSheet actions callbacks ====
    @Override
    public void onMoreInfo(int plantId) {
        getFullPlantDetailsAndNavigate(plantId);
    }

    @Override
    public void onToggleLike(int plantId) {
        boolean liked = currentLiked != null && currentLiked;
        if (liked) {
            unlikePlant(plantId);
            currentLiked = false;
        } else {
            likePlant(plantId);
            currentLiked = true;
        }
    }

    // ==== Garden bottom sheet actions ====
    @Override
    public void onViewPlants(long gardenId) {
        if (plantGardenMapManager == null) {
            Toast.makeText(getContext(), "Map not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "Loading plants in garden...", Toast.LENGTH_SHORT).show();
        plantGardenMapManager.fetchPlantsByGarden(gardenId);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Clean up PlantGardenMapManager
        if (plantGardenMapManager != null) {
            plantGardenMapManager.destroy();
            plantGardenMapManager = null;
        }
    }
}

