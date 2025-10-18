// /app/src/main/java/com/example/myapplication/ui/myplants/PlantWikiMainTabFragment.java
package com.example.myapplication.ui.myplants;

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

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantwikiMaintabBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class PlantWikiMainTabFragment extends Fragment {

    // Argument key should be consistent
    public static final String ARG_PLANT = "plant_argument";
    private static final String TAG = "PlantWikiMainTab";

    private PlantwikiMaintabBinding binding;
    private Plant plant;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the Plant object passed from PlantWikiFragment
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }
        if (plant == null) {
            Log.e(TAG, "Plant object is null. Cannot display wiki details.");
            Toast.makeText(getContext(), "Error: Wiki data not found.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the correct tabbed layout
        binding = PlantwikiMaintabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (plant == null) return;

        populateHeaderData();
        setupViewPagerAndTabs();
    }

    private void populateHeaderData() {
        binding.plantNameText.setText(plant.getName());
        binding.plantNicknameText.setText(plant.getScientificName());

        String base64Image = plant.getImageUrl();
        try {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Glide.with(this)
                    .load(imageBytes)
                    .placeholder(R.drawable.plantbulb_foreground)
                    .error(R.drawable.plantbulb_foreground)
                    // --- FIX: Use the correct ID from your plantwiki_maintab.xml file. ---
                    // Replace 'imageViewPlantPreview' with the actual ID if it is different.
                    .into(binding.landingimage);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image for " + plant.getName(), e);
        }
    }

    private void setupViewPagerAndTabs() {
        // This assumes you have a TabsPagerAdapter class to handle the fragments for each tab
        TabsPagerAdapter pagerAdapter = new TabsPagerAdapter(getChildFragmentManager(), getLifecycle(), plant);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Overview"); break;
                case 1: tab.setText("Features"); break;
                case 2: tab.setText("Care Guide"); break;
            }
        }).attach();
    }

    /**
     * Public method that allows child fragments to request a tab switch.
     * This method belongs in the fragment that OWNS the ViewPager.
     *
     * @param tabIndex The index of the tab to switch to.
     */
    public void switchToTab(int tabIndex) {
        if (binding != null && binding.viewPager.getAdapter() != null && tabIndex < binding.viewPager.getAdapter().getItemCount()) {
            // This code will now work because `binding` is a PlantwikiMaintabBinding
            // which contains the `viewPager`
            binding.viewPager.setCurrentItem(tabIndex, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
