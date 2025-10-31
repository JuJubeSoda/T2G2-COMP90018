package com.example.myapplication.map;

import android.content.Context;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.example.myapplication.network.GardenDto;
import com.example.myapplication.network.PlantMapDto;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.util.LogUtil;

/**
 * 地图显示管理器 - 负责在地图上显示植物和花园数据
 * 统一管理地图显示相关的功能，包括GeoJSON图层和标记
 */
public class MapDisplayManager {
    
    private static final String TAG = "MapDisplayManager";
    
    private final Context context;
    private final GoogleMap googleMap;
    // Clustering for plants
    private ClusterManager<PlantClusterItem> plantClusterManager;
    private DefaultClusterRenderer<PlantClusterItem> plantClusterRenderer;
    private List<PlantClusterItem> currentPlantItems = new ArrayList<>();
    // Clustering for gardens
    private ClusterManager<GardenClusterItem> gardenClusterManager;
    private DefaultClusterRenderer<GardenClusterItem> gardenClusterRenderer;
    private List<GardenClusterItem> currentGardenItems = new ArrayList<>();
    private List<PlantMapDto> currentPlants = new ArrayList<>();
    private List<GardenDto> currentGardens = new ArrayList<>();
    
    // 回调接口
    public interface OnGardenMapClickListener {
        void onGardenClick(GardenDto garden);
    }
    
    public interface OnPlantMapClickListener {
        void onPlantClick(PlantMapDto plant);
    }
    
    private OnGardenMapClickListener gardenClickListener;
    private OnPlantMapClickListener plantClickListener;
    
    public MapDisplayManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        LogUtil.d(TAG, "Initializing MapDisplayManager");
        setupClusterManagerIfNeeded();
        setupGardenClusterManagerIfNeeded();
        // 禁用InfoWindow
        if (googleMap != null) {
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) { return null; }
                @Override
                public View getInfoContents(Marker marker) { return null; }
            });
        }
    }
    
    public void setOnGardenClickListener(OnGardenMapClickListener listener) {
        this.gardenClickListener = listener;
    }
    
    public void setOnPlantClickListener(OnPlantMapClickListener listener) {
        this.plantClickListener = listener;
    }
    
    /**
     * 在地图上显示植物列表 - 使用Marker方式（适合频繁刷新）
     */
    public void displayPlantsOnMap(List<PlantMapDto> plants) {
        LogUtil.d(TAG, "=== Display Plants Debug ===");
        LogUtil.d(TAG, "Received plants list: " + (plants == null ? "null" : "size=" + plants.size()));
        LogUtil.d(TAG, "GoogleMap instance: " + (googleMap == null ? "null" : "available"));
        
        if (plants != null) {
            for (int i = 0; i < plants.size(); i++) {
                PlantMapDto plant = plants.get(i);
                LogUtil.d(TAG, "Plant " + i + ": " + plant.toString());
                LogUtil.d(TAG, "  - Name: " + plant.getName());
                LogUtil.d(TAG, "  - Latitude: " + plant.getLatitude());
                LogUtil.d(TAG, "  - Longitude: " + plant.getLongitude());
                LogUtil.d(TAG, "  - PlantId: " + plant.getPlantId());
            }
        }
        
        LogUtil.d(TAG, "Clearing existing plant items...");
        clearPlantMarkers();
        currentPlants.clear();
        currentPlantItems.clear();
        
        int validPlants = 0;
        if (plants != null) {
            for (PlantMapDto plant : plants) {
                if (plant.getLatitude() != null && plant.getLongitude() != null) {
                    LogUtil.d(TAG, "Adding marker for plant: " + plant.getName() + " at (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
                    try {
                        addPlantClusterItem(plant);
                        currentPlants.add(plant);
                        validPlants++;
                        LogUtil.d(TAG, "Successfully added marker for: " + plant.getName());
                    } catch (Exception e) {
                        LogUtil.e(TAG, "Failed to add marker for plant: " + plant.getName(), e);
                    }
                } else {
                    LogUtil.w(TAG, "Skipping plant with null coordinates: " + plant.getName());
                }
            }
        }
        
        LogUtil.d(TAG, "Final cluster item count: " + currentPlantItems.size());
        LogUtil.d(TAG, "Valid plants: " + validPlants + " out of " + (plants != null ? plants.size() : 0));
        LogUtil.d(TAG, "Current plants list size: " + currentPlants.size());
        
        // 检查地图状态
        if (googleMap != null) {
            LogUtil.d(TAG, "GoogleMap is ready, camera position: " + googleMap.getCameraPosition());
            LogUtil.d(TAG, "GoogleMap is ready, visible region: " + googleMap.getProjection().getVisibleRegion());
        } else {
            LogUtil.e(TAG, "GoogleMap is null! Cannot display markers.");
        }
        
        // UI feedback should be handled at higher layer
        LogUtil.d(TAG, "=== End Display Plants Debug ===");
    }
    
    /**
     * 增量更新植物显示（添加新植物）
     */
    public void addNewPlants(List<PlantMapDto> newPlants) {
        for (PlantMapDto plant : newPlants) {
            if (plant.getLatitude() != null && plant.getLongitude() != null && !currentPlants.contains(plant)) {
                addPlantClusterItem(plant);
                currentPlants.add(plant);
            }
        }
        
        LogUtil.d(TAG, "Added " + newPlants.size() + " new plants to map");
    }
    
    /**
     * 移除植物标记
     */
    public void removePlants(List<PlantMapDto> plantsToRemove) {
        for (PlantMapDto plant : plantsToRemove) {
            removePlantMarker(plant);
            currentPlants.remove(plant);
        }
        
        LogUtil.d(TAG, "Removed " + plantsToRemove.size() + " plants from map");
    }
    
    /**
     * 添加单个植物标记
     */
    private void addPlantMarker(PlantMapDto plant) {
        LogUtil.d(TAG, "=== Add Plant Marker Debug ===");
        LogUtil.d(TAG, "Plant: " + plant.getName());
        LogUtil.d(TAG, "Coordinates: (" + plant.getLatitude() + ", " + plant.getLongitude() + ")");
        LogUtil.d(TAG, "GoogleMap null check: " + (googleMap == null));
        
        if (googleMap == null) {
            LogUtil.e(TAG, "GoogleMap is null! Cannot add marker.");
            return;
        }
        
        LatLng position = new LatLng(plant.getLatitude(), plant.getLongitude());
        LogUtil.d(TAG, "Created LatLng: " + position);
        
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(plant.getName())
                .snippet(plant.getDescription())
                .icon(createPlantIcon());
        
        LogUtil.d(TAG, "MarkerOptions created: " + markerOptions);
        
        Marker marker = googleMap.addMarker(markerOptions);
        LogUtil.d(TAG, "Marker created: " + (marker == null ? "null" : "success"));
        
        if (marker != null) {
            marker.setTag(plant);
            // Legacy path (kept for potential fallback)
            
            // 设置点击监听器
            setupPlantMarkerClickListener(marker);
            LogUtil.d(TAG, "Marker click listener set");
        } else {
            LogUtil.e(TAG, "Failed to create marker for plant: " + plant.getName());
        }
        LogUtil.d(TAG, "=== End Add Plant Marker Debug ===");
    }

    /**
     * 添加Cluster item并刷新聚合
     */
    private void addPlantClusterItem(PlantMapDto plant) {
        if (plantClusterManager == null) {
            setupClusterManagerIfNeeded();
        }
        PlantClusterItem item = new PlantClusterItem(plant);
        currentPlantItems.add(item);
        plantClusterManager.addItem(item);
        plantClusterManager.cluster();
    }
    
    /**
     * 移除单个植物标记
     */
    private void removePlantMarker(PlantMapDto plant) {
        if (plantClusterManager == null || currentPlantItems.isEmpty()) return;
        for (int i = currentPlantItems.size() - 1; i >= 0; i--) {
            PlantClusterItem item = currentPlantItems.get(i);
            if (item.getPlant() != null && java.util.Objects.equals(item.getPlant().getPlantId(), plant.getPlantId())) {
                currentPlantItems.remove(i);
                plantClusterManager.removeItem(item);
                break;
            }
        }
        plantClusterManager.cluster();
    }
    
    /**
     * 设置植物标记点击监听器
     */
    private void setupPlantMarkerClickListener(Marker marker) {
        // 点击监听器通过GoogleMap的setOnMarkerClickListener统一处理
    }
    
    /**
     * 在地图上显示花园列表 - 使用GeoJSON方式（适合静态数据）
     */
    public void displayGardensOnMap(List<GardenDto> gardens) {
        clearGardenMarkers();
        currentGardens.clear();
        if (gardens != null) {
            currentGardens.addAll(gardens);
        }
        // 初次调用时按当前视野渲染，后续由相机事件触发刷新
        if (googleMap != null) {
            LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
            refreshGardensForViewport(bounds, 1000);
        }
    }
    
    /**
     * 清除植物标记
     */
    public void clearPlantMarkers() {
        // Clear clustering items
        if (plantClusterManager != null) {
            plantClusterManager.clearItems();
        }
        currentPlantItems.clear();
        // Legacy markers list is no longer used
    }
    
    /**
     * 清除花园GeoJSON图层
     */
    public void clearGardenMarkers() {
        if (gardenClusterManager != null) {
            gardenClusterManager.clearItems();
        }
        currentGardenItems.clear();
    }
    
    
    /**
     * 将花园列表转换为GeoJSON格式
     */
    public void refreshGardensForViewport(LatLngBounds bounds, int limit) {
        if (bounds == null) return;
        setupGardenClusterManagerIfNeeded();
        clearGardenMarkers();
        int count = 0;
        for (GardenDto g : currentGardens) {
            if (g.getLatitude() == null || g.getLongitude() == null) continue;
            LatLng pos = new LatLng(g.getLatitude(), g.getLongitude());
            if (bounds.contains(pos)) {
                gardenClusterManager.addItem(new GardenClusterItem(g));
                count++;
                if (count >= limit) break;
            }
        }
        gardenClusterManager.cluster();
        LogUtil.d(TAG, "Displayed gardens in viewport: " + count);
    }
    
    
    /**
     * 创建花园GeoJSON特征
     */
    // GeoJSON-related methods removed since we now use clustering for gardens
    
    
    /**
     * 设置花园GeoJSON图层
     */
    // GeoJSON layer setup removed
    
    
    /**
     * 处理花园GeoJSON特征点击事件
     */
    // Feature click handling removed with GeoJSON
    
    
    /**
     * 从GeoJSON特征创建Garden对象
     */
    // Conversion from GeoJSON removed
    
    
    /**
     * 应用花园标记样式
     */
    // Style application for GeoJSON removed
    
    /**
     * 创建植物图标
     */
    private BitmapDescriptor createPlantIcon() {
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.flower);
    }
    
    /**
     * 创建花园图标
     */
    private BitmapDescriptor createGardenIcon() {
        // Ensure garden icon matches plant icon visual size
        try {
            // Decode plant icon to get target dimensions
            Bitmap plantBmp = BitmapFactory.decodeResource(context.getResources(), com.example.myapplication.R.drawable.flower);
            int targetW = plantBmp != null ? plantBmp.getWidth() : 0;
            int targetH = plantBmp != null ? plantBmp.getHeight() : 0;

            Bitmap gardenBmp = BitmapFactory.decodeResource(context.getResources(), com.example.myapplication.R.drawable.gardon);
            if (gardenBmp != null && targetW > 0 && targetH > 0) {
                Bitmap scaled = Bitmap.createScaledBitmap(gardenBmp, targetW, targetH, true);
                return BitmapDescriptorFactory.fromBitmap(scaled);
            }
        } catch (Exception ignored) {}
        // Fallback to raw resource if scaling failed
        return BitmapDescriptorFactory.fromResource(com.example.myapplication.R.drawable.gardon);
    }
    
    /**
     * 清除当前显示
     */
    public void clearCurrentDisplay() {
        // 清除花园聚合
        clearGardenMarkers();
        
        // 清除植物标记
        clearPlantMarkers();
        
        // 清除数据缓存
        currentPlants.clear();
        currentGardens.clear();
    }
    
    /**
     * 处理植物标记点击事件
     */
    public boolean handlePlantMarkerClick(Marker marker) {
        PlantMapDto plant = (PlantMapDto) marker.getTag();
        if (plant != null && plantClickListener != null) {
            plantClickListener.onPlantClick(plant);
            return true;
        }
        return false;
    }
    
    /**
     * 销毁管理器
     */
    public void destroy() {
        clearCurrentDisplay();
    }

    /**
     * 初始化ClusterManager并绑定点击监听
     */
    private void setupClusterManagerIfNeeded() {
        if (googleMap == null || plantClusterManager != null) return;

        plantClusterManager = new ClusterManager<>(context, googleMap);
        plantClusterRenderer = new DefaultClusterRenderer<>(context, googleMap, plantClusterManager) {
            @Override
            protected void onBeforeClusterItemRendered(PlantClusterItem item, MarkerOptions markerOptions) {
                // 不设置 title/snippet，以彻底避免默认 InfoWindow 弹出
                markerOptions.icon(createPlantIcon());
            }

            @Override
            protected void onClusterItemUpdated(PlantClusterItem item, Marker marker) {
                // 确保更新时也不带有 title/snippet，防止默认 InfoWindow 出现
                marker.setTitle(null);
                marker.setSnippet(null);
                marker.setIcon(createPlantIcon());
                super.onClusterItemUpdated(item, marker);
            }

            @Override
            protected void onClusterItemRendered(PlantClusterItem clusterItem, Marker marker) {
                // 渲染完成后再清一次，防止默认渲染器写回
                marker.setTitle(null);
                marker.setSnippet(null);
                super.onClusterItemRendered(clusterItem, marker);
            }
        };
        plantClusterManager.setRenderer(plantClusterRenderer);

        // Marker点击：传递到回调，让上层展示BottomSheet
        plantClusterManager.setOnClusterItemClickListener(clusterItem -> {
            LogUtil.d(TAG, "Plant cluster ITEM clicked: " + clusterItem.getPosition());
            if (plantClickListener != null) {
                plantClickListener.onPlantClick(clusterItem.getPlant());
                return true;
            }
            return false;
        });

        // 仅日志，不改变既有行为
        plantClusterManager.setOnClusterClickListener(cluster -> {
            LogUtil.d(TAG, "Plant CLUSTER clicked, size=" + cluster.getSize());
            return false; // 不拦截，保持默认放大行为
        });

        // 将地图的各种事件委托给ClusterManager
        // Do not set listeners here; coordinator will attach composite listeners
    }

    public ClusterManager<PlantClusterItem> getPlantClusterManager() {
        return plantClusterManager;
    }

    /**
     * 初始化Garden ClusterManager并绑定点击监听
     */
    private void setupGardenClusterManagerIfNeeded() {
        if (googleMap == null || gardenClusterManager != null) return;

        gardenClusterManager = new ClusterManager<>(context, googleMap);
        gardenClusterRenderer = new DefaultClusterRenderer<>(context, googleMap, gardenClusterManager) {
            @Override
            protected void onBeforeClusterItemRendered(GardenClusterItem item, MarkerOptions markerOptions) {
                // 不设置 title/snippet，以彻底避免默认 InfoWindow 弹出
                markerOptions.icon(createGardenIcon());
            }

            @Override
            protected void onClusterItemUpdated(GardenClusterItem item, Marker marker) {
                // 确保更新时也不带有 title/snippet，防止默认 InfoWindow 出现
                marker.setTitle(null);
                marker.setSnippet(null);
                marker.setIcon(createGardenIcon());
                super.onClusterItemUpdated(item, marker);
            }

            @Override
            protected void onClusterItemRendered(GardenClusterItem clusterItem, Marker marker) {
                marker.setTitle(null);
                marker.setSnippet(null);
                super.onClusterItemRendered(clusterItem, marker);
            }
        };
        gardenClusterManager.setRenderer(gardenClusterRenderer);

        gardenClusterManager.setOnClusterItemClickListener(clusterItem -> {
            LogUtil.d(TAG, "Garden cluster ITEM clicked: " + clusterItem.getPosition());
            if (gardenClickListener != null) {
                gardenClickListener.onGardenClick(clusterItem.getGarden());
                return true;
            }
            return false;
        });

        gardenClusterManager.setOnClusterClickListener(cluster -> {
            LogUtil.d(TAG, "Garden CLUSTER clicked, size=" + cluster.getSize());
            return false;
        });
    }

    public ClusterManager<GardenClusterItem> getGardenClusterManager() {
        return gardenClusterManager;
    }

    /**
     * 显示单个植物并聚焦相机
     */
    public void displayAndFocusSinglePlant(PlantMapDto plant, float zoom) {
        if (plant == null || plant.getLatitude() == null || plant.getLongitude() == null || googleMap == null) return;
        // 清理现有植物聚合并仅显示该点
        clearPlantMarkers();
        currentPlants.clear();
        addPlantClusterItem(plant);
        currentPlants.add(plant);
        // 聚焦
        LatLng pos = new LatLng(plant.getLatitude(), plant.getLongitude());
        googleMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(pos, zoom <= 0 ? 17f : zoom));
        // 直接触发点击回调以弹出BottomSheet
        if (plantClickListener != null) {
            plantClickListener.onPlantClick(plant);
        }
    }
}
