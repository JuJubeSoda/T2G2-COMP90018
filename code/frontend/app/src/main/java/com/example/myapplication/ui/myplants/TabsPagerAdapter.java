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
        // Create an arguments bundle to pass the plant data down to each tab
        Bundle args = new Bundle();
        args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);

        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new PlantWikiOverview();
                break;
            case 1:
                fragment = new PlantWikiFeatures();
                break;
            case 2:
                fragment = new PlantWikiCareGuide();
                break;
            default:
                throw new IllegalStateException("Invalid position: " + position);
        }

        // FIX: SET THE ARGUMENTS ON THE FRAGMENT BEFORE RETURNING IT
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3; // We have 3 tabs
    }
}
