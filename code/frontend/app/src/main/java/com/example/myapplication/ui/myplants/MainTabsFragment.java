package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.databinding.PlantwikiMaintabBinding; // Uses its own layout
import com.google.android.material.tabs.TabLayoutMediator;

public class MainTabsFragment extends Fragment {

    private PlantwikiMaintabBinding binding;
    private Plant plant;

    // A static method to create an instance and pass arguments safely
    public static MainTabsFragment newInstance(Plant plant) {
        MainTabsFragment fragment = new MainTabsFragment();
        Bundle args = new Bundle();
        args.putParcelable(PlantDetailFragment.ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = getArguments().getParcelable(PlantDetailFragment.ARG_PLANT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This fragment correctly uses its simple tab layout
        binding = PlantwikiMaintabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (plant == null) return;

        // Use the updated TabsPagerAdapter, passing the plant data
        TabsPagerAdapter pagerAdapter = new TabsPagerAdapter(getChildFragmentManager(), getLifecycle(), plant);
        binding.viewPager.setAdapter(pagerAdapter);

        // Link the TabLayout and ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Overview"); break;
                case 1: tab.setText("Features"); break;
                case 2: tab.setText("Care Guide"); break;
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
