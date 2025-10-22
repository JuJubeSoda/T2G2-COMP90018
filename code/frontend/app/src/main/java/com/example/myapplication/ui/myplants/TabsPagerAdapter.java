package com.example.myapplication.ui.myplants;

import android.os.Bundle; // <-- ADD THIS IMPORT
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabsPagerAdapter extends FragmentStateAdapter {

    // ADD A FIELD TO HOLD THE PLANT DATA
    private final Plant plant;

    // FIX: MODIFY THE CONSTRUCTOR TO ACCEPT A PLANT OBJECT
    public TabsPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Plant plant) {
        super(fragmentManager, lifecycle);
        this.plant = plant; // Store the plant object
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Use the factory methods to create fragments with the plant object
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = PlantWikiOverview.newInstance(plant);
                break;
            case 1:
                fragment = PlantWikiFeatures.newInstance(plant);
                break;
            case 2:
                fragment = PlantWikiCareGuide.newInstance(plant);
                break;
            default:
                throw new IllegalStateException("Invalid position: " + position);
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3; // We have 3 tabs
    }
}
