package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * TabsPagerAdapter - ViewPager2 adapter for Plant Wiki detail tabs.
 * 
 * Purpose:
 * - Manage three tabs in PlantWikiMainTabFragment
 * - Create and provide fragments for each tab
 * - Pass Plant data to all child fragments
 * 
 * Tabs:
 * 1. Overview (Position 0) - PlantWikiOverview
 * 2. Features (Position 1) - PlantWikiFeatures
 * 3. Care Guide (Position 2) - PlantWikiCareGuide
 * 
 * Usage:
 * - Used in PlantWikiMainTabFragment with ViewPager2
 * - Receives Plant object from PlantWikiFragment
 * - Creates tab fragments with Plant data via factory methods
 * 
 * Data Flow:
 * 1. PlantWikiFragment passes Plant to PlantWikiMainTabFragment
 * 2. PlantWikiMainTabFragment creates TabsPagerAdapter with Plant
 * 3. Adapter creates each tab fragment with Plant via newInstance()
 * 4. Each tab displays different aspects of Plant data
 */
public class TabsPagerAdapter extends FragmentStateAdapter {

    /** Plant data to pass to all tab fragments */
    private final Plant plant;

    /**
     * Constructor with Plant data.
     * 
     * @param fragmentManager Fragment manager for creating child fragments
     * @param lifecycle Lifecycle for managing fragment states
     * @param plant Plant data to display across all tabs
     */
    public TabsPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Plant plant) {
        super(fragmentManager, lifecycle);
        this.plant = plant;
    }

    /**
     * Creates fragment for the specified tab position.
     * Uses factory methods to pass Plant data to each fragment.
     * 
     * @param position Tab position (0=Overview, 1=Features, 2=Care Guide)
     * @return Fragment instance for the tab
     * @throws IllegalStateException if position is invalid
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                // Overview tab - description, water/light needs, sensor data
                fragment = PlantWikiOverview.newInstance(plant);
                break;
            case 1:
                // Features tab - physical characteristics, mature height
                fragment = PlantWikiFeatures.newInstance(plant);
                break;
            case 2:
                // Care Guide tab - detailed care instructions
                fragment = PlantWikiCareGuide.newInstance(plant);
                break;
            default:
                throw new IllegalStateException("Invalid tab position: " + position);
        }

        return fragment;
    }

    /**
     * Returns total number of tabs.
     * @return 3 (Overview, Features, Care Guide)
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}
