package com.example.myapplication.ui.myplants.plantWiki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.databinding.PlantwikiMaintabBinding;
import com.example.myapplication.ui.myplants.share.Plant;
import com.example.myapplication.ui.myplants.myGarden.PlantDetailFragment;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * MainTabsFragment - Simple tabbed container for plant details (legacy/alternative to PlantWikiMainTabFragment).
 * 
 * Purpose:
 * - Provide tabbed interface for plant details
 * - Host three tabs: Overview, Features, Care Guide
 * - Alternative to PlantWikiMainTabFragment (without header)
 * 
 * User Flow:
 * 1. Fragment receives Plant object
 * 2. Displays three tabs for plant information
 * 3. User can swipe or tap to switch tabs
 * 
 * Key Features:
 * - ViewPager2 for smooth tab swiping
 * - TabLayout integration
 * - TabsPagerAdapter for tab content
 * - Simplified layout (no header image)
 * 
 * Note: This fragment uses the same layout as PlantWikiMainTabFragment
 * but without the header section. Consider consolidating if not needed.
 * 
 * Data Source:
 * - Plant object passed via newInstance factory method
 * 
 * Tab Structure:
 * - Tab 0: PlantWikiOverview
 * - Tab 1: PlantWikiFeatures
 * - Tab 2: PlantWikiCareGuide
 */
public class MainTabsFragment extends Fragment {

    /** View binding for plantwiki_maintab.xml layout */
    private PlantwikiMaintabBinding binding;
    
    /** Plant data to display across tabs */
    private Plant plant;

    /**
     * Factory method to create fragment with Plant data.
     * @param plant Plant object to display
     * @return New instance with Plant as Parcelable argument
     */
    public static MainTabsFragment newInstance(Plant plant) {
        MainTabsFragment fragment = new MainTabsFragment();
        Bundle args = new Bundle();
        args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Fragment creation lifecycle method.
     * Retrieves Plant object from arguments.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = getArguments().getParcelable(PlantDetailFragment.ARG_PLANT);
        }
    }

    /** Inflates the layout using View Binding. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PlantwikiMaintabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI after view is created.
     * 
     * Setup:
     * - Creates TabsPagerAdapter with Plant data
     * - Connects ViewPager2 to adapter
     * - Links TabLayout to ViewPager2 with TabLayoutMediator
     * - Sets tab titles: Overview, Features, Care Guide
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (plant == null) return;

        // Create adapter with Plant data for all tabs
        TabsPagerAdapter pagerAdapter = new TabsPagerAdapter(getChildFragmentManager(), getLifecycle(), plant);
        binding.viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout to ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Overview"); break;
                case 1: tab.setText("Features"); break;
                case 2: tab.setText("Care Guide"); break;
            }
        }).attach();
    }

    /** Cleans up view binding to prevent memory leaks. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
