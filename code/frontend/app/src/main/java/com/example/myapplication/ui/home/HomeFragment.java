package com.example.myapplication.ui.home; // Adjust to your project's package structure

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation; // For NavController
// import androidx.recyclerview.widget.LinearLayoutManager; // Only if not set in XML, and only if using 'binding.recyclerViewNearbyDiscoveries.setLayoutManager(...)'

import com.example.myapplication.R; // Your app's R file
import com.example.myapplication.ui.home.DiscoveryAdapter; // Assuming this is your adapter's package
import com.example.myapplication.databinding.HomeBinding; // Generated from fragment_home.xml
import com.example.myapplication.ui.home.DiscoveryItem; // Assuming this is your model's package

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeBinding binding; // ViewBinding instance
    private DiscoveryAdapter discoveryAdapter;
    private List<DiscoveryItem> discoveryItemList;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = HomeBinding.inflate(inflater, container, false);
        // Don't return binding.getRoot() here yet if you need to initialize navController in onViewCreated
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize NavController here, as the view is now created
        navController = Navigation.findNavController(view);

        // Setup UI components
        setupTopCardClickListeners();
        setupSearchView();
        setupNearbyDiscoveriesRecyclerView();
        loadNearbyDiscoveryData();
    }

    private void setupTopCardClickListeners() {
        // Card 1: Upload Plants (leads to a camera/gallery flow eventually)
        binding.card1.setOnClickListener(v -> {
            if (navController != null) {
                // Make sure R.id.action_navigation_home_to_uploadFragment is defined in your nav_graph.xml
                try {
                    navController.navigate(R.id.navigation_upload);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), "Upload navigation action not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Card 2: Plant Map
        if (binding.card2 != null) {
            binding.card2.setOnClickListener(v -> {
                if (navController != null) {
                    // Make sure R.id.action_navigation_home_to_plantMapFragment is defined
                    try {
                        navController.navigate(R.id.navigation_plant_map);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "Plant Map navigation action not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Card Row 2 exists if binding.cardRow2 is not null (assuming it's the LinearLayout ID)
        // Card 3: My Garden
        if (binding.card3 != null) { // Ensure card3 ID exists in your XML
            binding.card3.setOnClickListener(v -> {
                if (navController != null) {
                    // Make sure R.id.action_navigation_home_to_myGardenFragment is defined
                    try {
                        navController.navigate(R.id.navigation_my_garden);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "My Garden navigation action not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Card 4: Plant Wiki
        if (binding.card4 != null) { // Ensure card4 ID exists in your XML
            binding.card4.setOnClickListener(v -> {
                if (navController != null) {
                    // Make sure R.id.action_navigation_home_to_plantWikiFragment is defined
                    try {
                        navController.navigate(R.id.navigation_plant_wiki);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "Plant Wiki navigation action not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setupSearchView() {
        if (binding.searchViewMain != null) {
            binding.searchViewMain.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // User pressed search button on keyboard
                    Toast.makeText(getContext(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
                    // TODO: Implement your actual search logic here (e.g., navigate to a search results fragment)
                    return true; // True if the listener has consumed the event
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Text changed in search view
                    // TODO: Implement filtering of the discovery list or provide live suggestions if needed
                    return false;
                }
            });
        }
    }

    private void setupNearbyDiscoveriesRecyclerView() {
        // The LinearLayoutManager is assumed to be set in the XML layout for recyclerViewNearbyDiscoveries
        // (e.g., app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" and android:orientation="horizontal")

        discoveryItemList = new ArrayList<>();
        // Use requireContext() for a non-null Context once the fragment is attached
        discoveryAdapter = new DiscoveryAdapter(requireContext(), discoveryItemList);
        binding.recyclerViewNearbyDiscoveries.setAdapter(discoveryAdapter);

        // Optional: If you need custom item spacing for the horizontal RecyclerView
        // binding.recyclerViewNearbyDiscoveries.addItemDecoration(new HorizontalSpaceItemDecoration(16)); // Example
    }

    private void loadNearbyDiscoveryData() {
        // This is sample data. In a real app, you'd fetch this from a ViewModel, API, database, etc.
        List<DiscoveryItem> sampleData = new ArrayList<>();
        int placeholderImage = R.drawable.map_foreground; // Ensure ic_placeholder.xml exists in res/drawable

        // Add some sample items
        sampleData.add(new DiscoveryItem("Mystic Fern", "0.2 km away", placeholderImage, "A beautiful fern found nearby."));
        sampleData.add(new DiscoveryItem("Sunny Sunflower", "1.5 km away", placeholderImage, "Bright and cheerful."));
        sampleData.add(new DiscoveryItem("Royal Orchid", "0.8 km away", placeholderImage, "An elegant orchid specimen."));
        sampleData.add(new DiscoveryItem("Desert Cactus", "2.1 km away", placeholderImage, "Resilient and striking."));
        sampleData.add(new DiscoveryItem("Forest Pine", "3.0 km away", placeholderImage, "A tall and sturdy pine tree."));

        // Update the adapter with the new data
        if (discoveryAdapter != null) {
            discoveryAdapter.updateData(sampleData); // Make sure your adapter has an updateData method
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to avoid memory leaks
        if (binding != null && binding.recyclerViewNearbyDiscoveries != null) {
            binding.recyclerViewNearbyDiscoveries.setAdapter(null); // Good practice
        }
        binding = null;
        navController = null; // Also clear NavController reference if it's tied to the view
    }

    // Example ItemDecoration for horizontal spacing (if needed)
    /*
    public static class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int horizontalSpaceWidth;

        public HorizontalSpaceItemDecoration(int horizontalSpaceWidth) {
            // Convert dp to pixels if needed, or assume this is already in pixels
            this.horizontalSpaceWidth = horizontalSpaceWidth;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // Add right margin to all items except the last one
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.right = horizontalSpaceWidth;
            }
            // Add left margin to the first item if you have paddingStart="0dp" on RecyclerView
            if (parent.getChildAdapterPosition(view) == 0) {
                 // outRect.left = horizontalSpaceWidth; // If RecyclerView has no paddingStart
            }
        }
    }
    */
}
