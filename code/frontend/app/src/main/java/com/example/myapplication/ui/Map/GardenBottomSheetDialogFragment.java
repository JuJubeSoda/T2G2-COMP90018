package com.example.myapplication.ui.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.myapplication.network.GardenDto;
import com.example.myapplication.R;

public class GardenBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_GARDEN = "garden_arg";
    private GardenDto garden;
    private OnGardenActionListener actionListener;

    public interface OnGardenActionListener {
        void onViewPlants(long gardenId);
    }

    public static GardenBottomSheetDialogFragment newInstance(GardenDto gardenDto) {
        GardenBottomSheetDialogFragment fragment = new GardenBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_GARDEN, gardenDto);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_garden, container, false);

        if (getArguments() != null) {
            garden = (GardenDto) getArguments().getSerializable(ARG_GARDEN);
        }

        if (garden != null) {
            TextView tvTitle = view.findViewById(R.id.tv_garden_title);
            TextView tvName = view.findViewById(R.id.tv_garden_name);
            TextView tvDesc = view.findViewById(R.id.tv_garden_desc);
            TextView tvCoords = view.findViewById(R.id.tv_garden_coords);
            View btnNavigate = view.findViewById(R.id.btn_garden_navigate);
            View btnViewPlants = view.findViewById(R.id.btn_garden_view_plants);

            tvTitle.setText("Garden Information");
            tvName.setText(garden.getName());
            String description = garden.getDescription();
            tvDesc.setText(description == null || description.isEmpty() ? "No description available" : description);
            if (garden.getLatitude() != null && garden.getLongitude() != null) {
                tvCoords.setText(String.format("Location: %.4f, %.4f", garden.getLatitude(), garden.getLongitude()));
            } else {
                tvCoords.setText("Location: Not available");
            }

            if (btnNavigate != null) {
                btnNavigate.setOnClickListener(v -> {
                    if (garden.getLatitude() != null && garden.getLongitude() != null) {
                        String navigationUri = String.format("google.navigation:q=%f,%f&mode=driving", garden.getLatitude(), garden.getLongitude());
                        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUri));
                        navigationIntent.setPackage("com.google.android.apps.maps");
                        try {
                            startActivity(navigationIntent);
                        } catch (Exception e) {
                            String webUri = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", garden.getLatitude(), garden.getLongitude());
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUri)));
                        }
                    }
                });
            }

            // Removed More and Share buttons and their logic

            if (btnViewPlants != null) {
                btnViewPlants.setOnClickListener(v -> {
                    if (actionListener != null && garden.getGardenId() != null) {
                        actionListener.onViewPlants(garden.getGardenId());
                        dismiss();
                    }
                });
            }
        }

        return view;
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        // Try parent fragment first
        if (getParentFragment() instanceof OnGardenActionListener) {
            actionListener = (OnGardenActionListener) getParentFragment();
        } else if (context instanceof OnGardenActionListener) {
            actionListener = (OnGardenActionListener) context;
        }
    }
}


