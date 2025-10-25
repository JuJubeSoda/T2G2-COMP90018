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

/**
 * PlantWikiOverview - Overview tab showing plant description and live sensor readings.
 * 
 * Purpose:
 * - Display plant description from wiki
 * - Show plant care requirements (water, light, temperature, humidity)
 * - Provide live sensor readings via device sensors
 * - Convert raw sensor values to human-readable descriptions
 * - Navigate to Care Guide tab
 * 
 * User Flow:
 * 1. User views plant in PlantWikiMainTabFragment
 * 2. Overview tab shows by default
 * 3. Displays description and requirements
 * 4. User can tap sensor cards to see live readings
 * 5. Dialog shows real-time sensor data with descriptions
 * 6. User can tap "View Full Care Guide" to switch tabs
 * 
 * Key Features:
 * - Plant description and requirements display
 * - Live sensor integration (light, temperature, humidity)
 * - Human-readable sensor value descriptions
 * - Sensor reading legend for interpretation
 * - Real-time sensor updates in dialog
 * - Tab switching to Care Guide
 * - Graceful handling of unavailable sensors
 * 
 * Sensor Descriptions:
 * - Light: Very Bright (>10000 lx), Bright (>1000 lx), Moderate (>100 lx), Low Light
 * - Temperature: Very Hot (>30°C), Warm (>24°C), Moderate (>18°C), Cool (>12°C), Cold
 * - Humidity: Very High (>70%), High (>60%), Moderate (>40%), Low
 * 
 * Data Source:
 * - Plant object passed from PlantWikiMainTabFragment
 * - Description, waterNeeds, lightNeeds from PlantWikiDto
 * - Live sensor data from device hardware
 */
public class PlantWikiOverview extends Fragment implements SensorEventListener {

    private static final String ARG_PLANT = "plant_object";
    private static final String TAG = "PlantWikiOverview";

    /** View binding for plantwiki_overviewtab.xml layout */
    private PlantwikiOverviewtabBinding binding;
    
    /** Plant data to display */
    private Plant plant;
    
    /** System sensor manager for accessing device sensors */
    private SensorManager sensorManager;
    
    /** Device sensors for live readings */
    private Sensor lightSensor;
    private Sensor tempSensor;
    private Sensor humiditySensor;
    
    /** Dialog for displaying live sensor readings */
    private AlertDialog sensorDialog;
    
    /** TextView in dialog showing sensor value */
    private TextView dialogValueView;

    /**
     * Factory method to create fragment with Plant data.
     * @param plant Plant object to display
     * @return New instance with Plant as Parcelable argument
     */
    public static PlantWikiOverview newInstance(Plant plant) {
        PlantWikiOverview fragment = new PlantWikiOverview();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Fragment creation lifecycle method.
     * Retrieves Plant object and initializes device sensors.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = getArguments().getParcelable(ARG_PLANT);
        }

        // Initialize device sensors for live readings
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    }

    /** Inflates the layout using View Binding. */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = PlantwikiOverviewtabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up UI after view is created.
     * 
     * Populates:
     * - Plant description
     * - Water requirement (from PlantWikiDto.waterNeeds)
     * - Light requirement (from PlantWikiDto.lightNeeds)
     * - Temperature requirement (N/A if not in wiki)
     * - Humidity requirement (N/A if not in wiki)
     * 
     * Handles null Plant gracefully with fallback text.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (plant != null) {
            Log.d(TAG, "Plant object received: " + plant.getName());
            
            // Display plant description
            String description = plant.getDescription();
            Log.d(TAG, "Description: " + description);
            binding.overviewIntroText.setText(description != null && !description.isEmpty() ? description : "No description available.");

            // Display light requirement (from wiki API)
            String lightReq = plant.getLightRequirement();
            Log.d(TAG, "Light Requirement: " + lightReq);
            binding.lightSubtitle.setText(lightReq != null && !lightReq.isEmpty() ? lightReq : "N/A");

            // Display water requirement (from wiki API)
            String waterReq = plant.getWaterRequirement();
            Log.d(TAG, "Water Requirement: " + waterReq);
            binding.waterSubtitle.setText(waterReq != null && !waterReq.isEmpty() ? waterReq : "N/A");

            // Temperature and humidity not available in wiki API
            String tempReq = plant.getTemperatureRequirement();
            binding.temperatureSubtitle.setText(tempReq != null && !tempReq.isEmpty() ? tempReq : "N/A");

            String humidityReq = plant.getHumidityRequirement();
            binding.humiditySubtitle.setText(humidityReq != null && !humidityReq.isEmpty() ? humidityReq : "N/A");
        } else {
            // Handle null Plant with fallback text
            Log.e(TAG, "Plant object is null!");
            binding.overviewIntroText.setText("Plant data to be updated!");
            binding.lightSubtitle.setText("N/A");
            binding.waterSubtitle.setText("N/A");
            binding.temperatureSubtitle.setText("N/A");
            binding.humiditySubtitle.setText("N/A");
        }

        setupClickListeners();
    }

    /**
     * Sets up click listeners for interactive elements.
     * 
     * Interactions:
     * - "View Full Care Guide" button -> switches to Care Guide tab
     * - Light card -> shows live light sensor dialog
     * - Temperature card -> shows live temperature sensor dialog
     * - Humidity card -> shows live humidity sensor dialog
     * - Water card -> non-clickable (display only)
     * 
     * Shows toast if sensor is unavailable on device.
     */
    private void setupClickListeners() {
        // Navigate to Care Guide tab
        binding.viewFullCareGuideButton.setOnClickListener(v -> switchToCareGuideTab());

        // Light sensor card
        binding.lightCard.setOnClickListener(v -> {
            if (lightSensor != null) {
                showSensorDialog("Live Light Reading", lightSensor, SensorType.LIGHT);
            } else {
                Toast.makeText(getContext(), "Light sensor not available on this device.", Toast.LENGTH_SHORT).show();
            }
        });

        // Temperature sensor card
        binding.temperatureCard.setOnClickListener(v -> {
            if (tempSensor != null) {
                showSensorDialog("Live Temperature", tempSensor, SensorType.TEMPERATURE);
            } else {
                Toast.makeText(getContext(), "Ambient temperature sensor not available.", Toast.LENGTH_SHORT).show();
            }
        });

        // Humidity sensor card
        binding.humidityCard.setOnClickListener(v -> {
            if (humiditySensor != null) {
                showSensorDialog("Live Humidity", humiditySensor, SensorType.HUMIDITY);
            } else {
                Toast.makeText(getContext(), "Humidity sensor not available on this device.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Enum to track which sensor is currently active in dialog */
    private enum SensorType {
        LIGHT, TEMPERATURE, HUMIDITY
    }

    /** Currently active sensor type for description conversion */
    private SensorType currentSensorType;

    /**
     * Shows dialog with live sensor readings.
     * 
     * Process:
     * 1. Inflates dialog layout
     * 2. Registers sensor listener
     * 3. Shows dialog with "Waiting for sensor data..."
     * 4. Updates dialog text as sensor data arrives
     * 5. Unregisters listener when dialog is dismissed
     * 
     * @param title Dialog title (e.g., "Live Light Reading")
     * @param sensorToRegister Device sensor to monitor
     * @param sensorType Type for description conversion
     */
    @SuppressLint("MissingInflatedId")
    private void showSensorDialog(String title, Sensor sensorToRegister, SensorType sensorType) {
        this.currentSensorType = sensorType;
        
        // Inflate dialog layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sensor_reading, null);
        dialogValueView = dialogView.findViewById(R.id.sensor_value_text);
        dialogValueView.setText("Waiting for sensor data...");

        // Build and configure dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title)
                .setView(dialogView)
                .setNegativeButton("Close", (dialog, id) -> { /* Listener handles cleanup */ });

        sensorDialog = builder.create();
        sensorDialog.setOnDismissListener(dialog -> {
            // Cleanup when dialog is dismissed
            sensorManager.unregisterListener(this);
            dialogValueView = null;
            currentSensorType = null;
        });

        // Register sensor listener and show dialog
        sensorManager.registerListener(this, sensorToRegister, SensorManager.SENSOR_DELAY_NORMAL);
        sensorDialog.show();
    }

    /**
     * Converts light sensor value (lux) to human-readable description.
     * @param lux Light intensity in lux
     * @return Description: "Very Bright", "Bright", "Moderate", or "Low Light"
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
     * Converts temperature sensor value (°C) to human-readable description.
     * @param celsius Temperature in Celsius
     * @return Description: "Very Hot", "Warm", "Moderate", "Cool", or "Cold"
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
     * Converts humidity sensor value (%) to human-readable description.
     * @param humidity Relative humidity percentage
     * @return Description: "Very High", "High", "Moderate", or "Low"
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

    /**
     * Switches to Care Guide tab (tab index 2).
     * Calls parent PlantWikiMainTabFragment's switchToTab method.
     */
    private void switchToCareGuideTab() {
        Fragment parent = getParentFragment();
        if (parent instanceof PlantWikiMainTabFragment) {
            ((PlantWikiMainTabFragment) parent).switchToTab(2);
        }
    }

    /** Cleans up view binding to prevent memory leaks. */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /** SensorEventListener callback - not used. */
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Can be ignored
    }

    /**
     * SensorEventListener callback - receives live sensor data.
     * 
     * Process:
     * 1. Gets raw sensor value
     * 2. Converts to human-readable description based on sensor type
     * 3. Formats display text with description and raw value
     * 4. Updates dialog TextView
     * 
     * Display Format:
     * - Light: "Very Bright\n(12345.6 lx)"
     * - Temperature: "Warm\n(25.3 °C)"
     * - Humidity: "Moderate\n(55.0 %)"
     */
    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (dialogValueView == null || currentSensorType == null) return;

        float value = event.values[0];
        String description;
        String displayText;

        // Convert raw value to human-readable description
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
