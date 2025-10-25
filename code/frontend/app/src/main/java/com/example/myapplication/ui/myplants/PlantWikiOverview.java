// /app/src/main/java/com/example/myapplication/ui/myplants/PlantWikiOverview.java
package com.example.myapplication.ui.myplants;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
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

public class PlantWikiOverview extends Fragment implements SensorEventListener {

    private static final String ARG_PLANT = "plant_object";
    private static final String TAG = "PlantWikiOverview";

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
        args.putParcelable(ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
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
            Log.d(TAG, "Plant object received: " + plant.getName());
            
            // Get description from plant object
            String description = plant.getDescription();
            Log.d(TAG, "Description: " + description);
            binding.overviewIntroText.setText(description != null && !description.isEmpty() ? description : "No description available.");

            // Get light requirement (mapped from lightNeeds in API)
            String lightReq = plant.getLightRequirement();
            Log.d(TAG, "Light Requirement: " + lightReq);
            binding.lightSubtitle.setText(lightReq != null && !lightReq.isEmpty() ? lightReq : "N/A");

            // Get water requirement (mapped from waterNeeds in API)
            String waterReq = plant.getWaterRequirement();
            Log.d(TAG, "Water Requirement: " + waterReq);
            binding.waterSubtitle.setText(waterReq != null && !waterReq.isEmpty() ? waterReq : "N/A");

            // Temperature and humidity are not available in wiki API
            String tempReq = plant.getTemperatureRequirement();
            binding.temperatureSubtitle.setText(tempReq != null && !tempReq.isEmpty() ? tempReq : "N/A");

            String humidityReq = plant.getHumidityRequirement();
            binding.humiditySubtitle.setText(humidityReq != null && !humidityReq.isEmpty() ? humidityReq : "N/A");
        } else {
            Log.e(TAG, "Plant object is null!");
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
        // Water card is no longer clickable - removed click listener

        binding.lightCard.setOnClickListener(v -> {
            if (lightSensor != null) {
                showSensorDialog("Live Light Reading", lightSensor, SensorType.LIGHT);
            } else {
                Toast.makeText(getContext(), "Light sensor not available on this device.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.temperatureCard.setOnClickListener(v -> {
            if (tempSensor != null) {
                showSensorDialog("Live Temperature", tempSensor, SensorType.TEMPERATURE);
            } else {
                Toast.makeText(getContext(), "Ambient temperature sensor not available.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.humidityCard.setOnClickListener(v -> {
            if (humiditySensor != null) {
                showSensorDialog("Live Humidity", humiditySensor, SensorType.HUMIDITY);
            } else {
                Toast.makeText(getContext(), "Humidity sensor not available on this device.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Enum to track sensor types
    private enum SensorType {
        LIGHT, TEMPERATURE, HUMIDITY
    }

    private SensorType currentSensorType;

    @SuppressLint("MissingInflatedId")
    private void showSensorDialog(String title, Sensor sensorToRegister, SensorType sensorType) {
        this.currentSensorType = sensorType;
        
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
            currentSensorType = null;
        });

        sensorManager.registerListener(this, sensorToRegister, SensorManager.SENSOR_DELAY_NORMAL);
        sensorDialog.show();
    }

    /**
     * Converts light sensor value (lux) to human-readable description
     */
    private String getLightDescription(float lux) {
        if (lux > 10000) {
            return "Very Bright";
        } else if (lux > 1000) {
            return "Bright";
        } else if (lux > 100) {
            return "Moderate";
        } else {
            return "Low Light";
        }
    }

    /**
     * Converts temperature sensor value (°C) to human-readable description
     */
    private String getTemperatureDescription(float celsius) {
        if (celsius > 30) {
            return "Very Hot";
        } else if (celsius > 24) {
            return "Warm";
        } else if (celsius > 18) {
            return "Moderate";
        } else if (celsius > 12) {
            return "Cool";
        } else {
            return "Cold";
        }
    }

    /**
     * Converts humidity sensor value (%) to human-readable description
     */
    private String getHumidityDescription(float humidity) {
        if (humidity > 70) {
            return "Very High";
        } else if (humidity > 60) {
            return "High";
        } else if (humidity > 40) {
            return "Moderate";
        } else {
            return "Low";
        }
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
        if (dialogValueView == null || currentSensorType == null) return;

        float value = event.values[0];
        String description;
        String displayText;

        switch (currentSensorType) {
            case LIGHT:
                description = getLightDescription(value);
                displayText = String.format("%s\n(%.1f lx)", description, value);
                break;
            case TEMPERATURE:
                description = getTemperatureDescription(value);
                displayText = String.format("%s\n(%.1f °C)", description, value);
                break;
            case HUMIDITY:
                description = getHumidityDescription(value);
                displayText = String.format("%s\n(%.1f %%)", description, value);
                break;
            default:
                displayText = String.format("%.1f", value);
                break;
        }

        dialogValueView.setText(displayText);
    }
}
