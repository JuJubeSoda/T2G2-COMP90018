package com.example.myapplication.ui.map;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.example.myapplication.network.PlantMapDto;
import com.example.myapplication.R;

public class PlantBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_PLANT = "plant_arg";
    private static final String ARG_INITIAL_LIKED = "initial_liked";
    private PlantMapDto plant;
    private boolean initialLiked = false;

    public static PlantBottomSheetDialogFragment newInstance(PlantMapDto plantDto) {
        PlantBottomSheetDialogFragment fragment = new PlantBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLANT, plantDto);
        fragment.setArguments(args);
        return fragment;
    }

    public static PlantBottomSheetDialogFragment newInstance(PlantMapDto plantDto, boolean initialLiked) {
        PlantBottomSheetDialogFragment fragment = new PlantBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLANT, plantDto);
        args.putBoolean(ARG_INITIAL_LIKED, initialLiked);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_plant, container, false);

        if (getArguments() != null) {
            plant = (PlantMapDto) getArguments().getSerializable(ARG_PLANT);
            initialLiked = getArguments().getBoolean(ARG_INITIAL_LIKED, false);
        }

        if (plant != null) {
            TextView tvTitle = view.findViewById(R.id.tv_plant_title);
            TextView tvName = view.findViewById(R.id.tv_plant_name);
            TextView tvDesc = view.findViewById(R.id.tv_plant_desc);
            TextView tvSci = view.findViewById(R.id.tv_plant_scientific_name);
            View btnNavigate = view.findViewById(R.id.btn_plant_navigate);
            View btnMore = view.findViewById(R.id.btn_plant_more);
            android.widget.Button btnLike = view.findViewById(R.id.btn_plant_like);
            View btnShare = view.findViewById(R.id.btn_plant_share);

            tvTitle.setText("Plant Information");
            tvName.setText(plant.getName());
            String description = plant.getDescription();
            tvDesc.setText(description == null || description.isEmpty() ? "No description available" : description);
            // Scientific name: display directly from PlantMapDto (no lazy load)
            String sci = plant.getScientificName();
            tvSci.setText(sci != null && !sci.isEmpty() ? ("Scientific Name: " + sci) : "Scientific Name: N/A");

            if (btnNavigate != null) {
                btnNavigate.setOnClickListener(v -> {
                    if (plant.getLatitude() != null && plant.getLongitude() != null) {
                        String navigationUri = String.format("google.navigation:q=%f,%f&mode=driving", plant.getLatitude(), plant.getLongitude());
                        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUri));
                        navigationIntent.setPackage("com.google.android.apps.maps");
                        try {
                            startActivity(navigationIntent);
                        } catch (Exception e) {
                            String webUri = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", plant.getLatitude(), plant.getLongitude());
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUri)));
                        }
                    }
                });
            }

            if (btnMore != null) {
                btnMore.setOnClickListener(v -> {
                    if (getParentFragment() instanceof OnPlantActionListener) {
                        ((OnPlantActionListener) getParentFragment()).onMoreInfo(plant.getPlantId() != null ? plant.getPlantId().intValue() : 0);
                    }
                });
            }

            if (btnLike != null) {
                // Default state: if from "My Likes" list then show "Unlike"
                btnLike.setText(initialLiked ? "Unlike" : "Like");
                btnLike.setOnClickListener(v -> {
                    if (getParentFragment() instanceof OnPlantActionListener) {
                        ((OnPlantActionListener) getParentFragment()).onToggleLike(plant.getPlantId() != null ? plant.getPlantId().intValue() : 0);
                    }
                });
            }

            if (btnShare != null) {
                btnShare.setOnClickListener(v -> {
                    ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        ClipData data = ClipData.newPlainText("plantId", String.valueOf(plant.getPlantId()));
                        cm.setPrimaryClip(data);
                    }
                });
            }
            // No lazy loading here by requirement
        }

        return view;
    }

    public interface OnPlantActionListener {
        void onMoreInfo(int plantId);
        void onToggleLike(int plantId);
    }

    public void setLikedState(boolean liked) {
        View root = getView();
        if (root == null) return;
        View btnLike = root.findViewById(R.id.btn_plant_like);
        if (btnLike instanceof android.widget.Button) {
            ((android.widget.Button) btnLike).setText(liked ? "Unlike" : "Like");
        }
    }

    public void setLikeButtonEnabled(boolean enabled) {
        View root = getView();
        if (root == null) return;
        View btnLike = root.findViewById(R.id.btn_plant_like);
        if (btnLike != null) btnLike.setEnabled(enabled);
    }
}


