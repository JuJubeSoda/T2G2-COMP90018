package com.example.myapplication.ui.myplants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// FIX: Use the binding for "plantwiki_maintab.xml"
import com.example.myapplication.databinding.PlantwikiMaintabBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

// You'll need an image loading library like Glide or Picasso
// import com.bumptech.glide.Glide;

public class PlantDetailFragment extends Fragment {

    public static final String ARG_PLANT = "plant_argument";

    // FIX: Use the correct binding class
    private PlantwikiMaintabBinding binding;
    private Plant plant;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // FIX: Inflate your existing, modified layout
        binding = PlantwikiMaintabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (plant == null) return;

        // This code will now work because all the views exist in the inflated layout
        populateHeaderData();
        setupViewPagerAndTabs();
    }

    private void populateHeaderData() {
        // This code populates the views we added to the top of the XML
        binding.plantNameText.setText(plant.getName());

        // FIX: DO NOT call getScientificName() as it is null.
        // Let's use an empty string or some other placeholder for now.
        binding.plantNicknameText.setText(""); // Or you could use plant.getName() again

        // TODO: Load image into binding.plantImage using Glide or Picasso
        // Glide.with(this).load(plant.getImageUrl()).into(binding.plantImage);
    }

    private void setupViewPagerAndTabs() {
        // FIX: Use requireActivity().getSupportFragmentManager() instead of getChildFragmentManager()
        TabsPagerAdapter pagerAdapter = new TabsPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle(), plant);
        binding.viewPager.setAdapter(pagerAdapter);

        // Link the TabLayout and ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Overview");
                    break;
                case 1:
                    tab.setText("Features");
                    break;
                case 2:
                    tab.setText("Care Guide");
                    break;
            }
        }).attach();
    }

    /**
     * Switches the ViewPager2 to a specific tab index.
     *
     * @param tabIndex The index of the tab to switch to (0-based).
     */
    public void switchToTab(int tabIndex) {
        if (binding != null && tabIndex < Objects.requireNonNull(binding.viewPager.getAdapter()).getItemCount()) {
            binding.viewPager.setCurrentItem(tabIndex, true); // true for smooth scroll
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
