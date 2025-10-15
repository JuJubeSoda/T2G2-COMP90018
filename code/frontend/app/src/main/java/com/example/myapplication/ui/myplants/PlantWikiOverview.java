package com.example.myapplication.ui.myplants;

// --- FIX: Add required imports ---
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R; // Import R class for layouts
import com.example.myapplication.databinding.PlantwikiOverviewtabBinding;
import com.example.myapplication.ui.myplants.Plant; // Assuming you have this model

import java.io.Serializable;

/**
 * A fragment that displays the overview tab for a specific plant in the Plant Wiki.
 * It shows a brief description and a grid of key care requirements.
 */
// --- FIX: Implement the SensorEventListener interface ---
public class PlantWikiOverview extends Fragment implements SensorEventListener {

    private static final String ARG_PLANT = "plant_object";

    private PlantwikiOverviewtabBinding binding;
    private Plant plant;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor tempSensor;
    private Sensor humiditySensor;
    private AlertDialog sensorDialog;
    private TextView dialogValueView;

    public static PlantWikiOverview newInstance(Plant plant) {
        PlantWikiOverview fragment = new PlantWikiOverview();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLANT, (Serializable) plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // It's safer to check the type before casting
            Serializable serializable = getArguments().getSerializable(ARG_PLANT);
            if (serializable instanceof Plant) {
                plant = (Plant) serializable;
            }
        }

        // Initialize the SensorManager and sensors
        // --- FIX: Use the correct Context class ---
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = PlantwikiOverviewtabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (plant != null) {
            binding.overviewIntroText.setText(plant.getIntroduction());
            binding.lightSubtitle.setText(plant.getLightRequirement());
            binding.waterSubtitle.setText(plant.getWaterRequirement());
            binding.temperatureSubtitle.setText(plant.getTemperatureRequirement());
            binding.humiditySubtitle.setText(plant.getHumidityRequirement());
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.viewFullCareGuideButton.setOnClickListener(v -> switchToCareGuideTab(null));
        binding.waterCard.setOnClickListener(v -> switchToCareGuideTab("water"));

        binding.lightCard.setOnClickListener(v -> {
            if (lightSensor != null) {
                showSensorDialog("Live Light Reading", "lx", lightSensor);
            } else {
                Toast.makeText(getContext(), "Light sensor not available on this device.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.temperatureCard.setOnClickListener(v -> {
            if (tempSensor != null) {
                showSensorDialog("Live Temperature", "°C", tempSensor);
            } else {
                Toast.makeText(getContext(), "Ambient temperature sensor not available.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.humidityCard.setOnClickListener(v -> {
            if (humiditySensor != null) {
                showSensorDialog("Live Humidity", "%", humiditySensor);
            } else {
                Toast.makeText(getContext(), "Humidity sensor not available on this device.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    private void showSensorDialog(String title, String unit, Sensor sensorToRegister) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sensor_reading, null); // Use R class here
        dialogValueView = dialogView.findViewById(R.id.sensor_value_text); // Use R class here
        dialogValueView.setText("Waiting for sensor data...");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title)
                .setView(dialogView)
                .setNegativeButton("Close", (dialog, id) -> {
                    // Dialog will be dismissed, and the listener will handle the cleanup
                });

        sensorDialog = builder.create();

        sensorDialog.setOnDismissListener(dialog -> {
            sensorManager.unregisterListener(this);
            dialogValueView = null;
        });

        sensorManager.registerListener(this, sensorToRegister, SensorManager.SENSOR_DELAY_NORMAL);
        sensorDialog.show();
    }

    private void switchToCareGuideTab(String anchor) {
        if (getParentFragment() instanceof PlantDetailFragment) {
            PlantDetailFragment parent = (PlantDetailFragment) getParentFragment();
            parent.switchToTab(2);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- FIX: Add the two required methods for SensorEventListener ---
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Can be ignored for this use case.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (dialogValueView == null) return;

        float value = event.values[0];
        String unit = "";
        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_LIGHT) {
            unit = " lx";
        } else if (sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            unit = " °C";
        } else if (sensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
            unit = " %";
        }

        dialogValueView.setText(String.format("%.1f%s", value, unit));
    }
}
