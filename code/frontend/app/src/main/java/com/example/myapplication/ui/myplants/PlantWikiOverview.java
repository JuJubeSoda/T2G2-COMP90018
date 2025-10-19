// /app/src/main/java/com/example/myapplication/ui/myplants/PlantWikiOverview.java
package com.example.myapplication.ui.myplants;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.databinding.PlantwikiOverviewtabBinding;

import java.io.Serializable;

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
            Serializable serializable = getArguments().getSerializable(ARG_PLANT);
            if (serializable instanceof Plant) {
                plant = (Plant) serializable;
            }
        }

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
            // --- THIS IS THE FIX ---
            // Use a helper function or ternary operator to set "N/A" for null or empty strings.

            String description = plant.getDescription();
            binding.overviewIntroText.setText(description != null && !description.isEmpty() ? description : "No description available.");

            String lightReq = plant.getLightRequirement();
            binding.lightSubtitle.setText(lightReq != null && !lightReq.isEmpty() ? lightReq : "N/A");

            String waterReq = plant.getWaterRequirement();
            binding.waterSubtitle.setText(waterReq != null && !waterReq.isEmpty() ? waterReq : "N/A");

            String tempReq = plant.getTemperatureRequirement();
            binding.temperatureSubtitle.setText(tempReq != null && !tempReq.isEmpty() ? tempReq : "N/A");

            String humidityReq = plant.getHumidityRequirement();
            binding.humiditySubtitle.setText(humidityReq != null && !humidityReq.isEmpty() ? humidityReq : "N/A");
        } else {
            // Handle case where the entire plant object is null
            binding.overviewIntroText.setText("Plant data to be updated!");
            binding.lightSubtitle.setText("N/A");
            binding.waterSubtitle.setText("N/A");
            binding.temperatureSubtitle.setText("N/A");
            binding.humiditySubtitle.setText("N/A");
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.viewFullCareGuideButton.setOnClickListener(v -> switchToCareGuideTab());
        binding.waterCard.setOnClickListener(v -> switchToCareGuideTab());

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
        View dialogView = inflater.inflate(R.layout.dialog_sensor_reading, null);
        dialogValueView = dialogView.findViewById(R.id.sensor_value_text);
        dialogValueView.setText("Waiting for sensor data...");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title)
                .setView(dialogView)
                .setNegativeButton("Close", (dialog, id) -> { /* Listener handles cleanup */ });

        sensorDialog = builder.create();
        sensorDialog.setOnDismissListener(dialog -> {
            sensorManager.unregisterListener(this);
            dialogValueView = null;
        });

        sensorManager.registerListener(this, sensorToRegister, SensorManager.SENSOR_DELAY_NORMAL);
        sensorDialog.show();
    }

    private void switchToCareGuideTab() {
        Fragment parent = getParentFragment();
        if (parent instanceof PlantWikiMainTabFragment) {
            ((PlantWikiMainTabFragment) parent).switchToTab(2); // 2 is the index for "Care Guide"
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Can be ignored.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (dialogValueView == null) return;

        float value = event.values[0];
        String unit = "";
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                unit = " lx";
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                unit = " °C";
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                unit = " %";
                break;
        }

        dialogValueView.setText(String.format("%.1f%s", value, unit));
    }
}
