package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.BaseResponse;
import com.example.myapplication.sensor.SensorDataCollector;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import io.noties.markwon.Markwon;


public class AIChatActivity extends AppCompatActivity implements SensorDataCollector.SensorDataCallback {

    private static final String TAG = "AIChatActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1003;
    private static final int CAMERA_REQUEST_CODE = 1004;
    private static final int GALLERY_REQUEST_CODE = 1005;

    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText inputMessage;
    private Button sendButton;
    private ImageButton cameraButton;
    
    // Image preview components
    private RelativeLayout imagePreviewContainer;
    private ImageView imagePreview;
    private ImageButton removeImageButton;
    private Uri pendingImageUri;

    private Markwon markwon;

    private ApiService apiService;
    private Handler handler = new Handler();
    
    // Sensor functionality - Auto-start
    private SensorDataCollector sensorCollector;
    private Map<String, Object> currentSensorData;
    private boolean sensorsInitialized = false;
    
    // Camera functionality
    private Uri currentImageUri;
    
    // Timeout and retry functionality
    private Call<BaseResponse> currentApiCall;
    private Runnable currentTimeoutTask;
    private TextView currentAiThinkingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aichat);

        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        cameraButton = findViewById(R.id.cameraButton);
        
        // Initialize image preview components
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imagePreview = findViewById(R.id.imagePreview);
        removeImageButton = findViewById(R.id.removeImageButton);

        apiService = ApiClient.create(this);
        sensorCollector = new SensorDataCollector(this);

        markwon = Markwon.create(this);

        sendButton.setOnClickListener(v -> sendMessage());
        cameraButton.setOnClickListener(v -> showImageSourceDialog());
        removeImageButton.setOnClickListener(v -> clearImagePreview());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        
        // Add welcome message
        addMessage("🌱 Plant AI Assistant: Hello! I can help you:\n• 📷 Identify plants from photos\n• 🌍 Recommend plants for your area\n• 💬 Answer plant-related questions\n\nAsk me: 'What plants should I grow?' or upload a plant photo!", false);
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        boolean hasImage = pendingImageUri != null;
        
        // Check if there's either a message or an image
        if (message.isEmpty() && !hasImage) {
            Toast.makeText(this, "Please enter a message or attach an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // If there's an image, send it with identification
        if (hasImage) {
            // Display user message with image
            addUserMessageWithImage(message.isEmpty() ? "What plant is this?" : message, pendingImageUri);
            
            // Clear inputs
            inputMessage.setText("");
            Uri imageToSend = pendingImageUri;
            clearImagePreview();
            
            // Send to AI for identification
            identifyPlantFromImage(imageToSend);
        } else {
            // Text-only message
            addMessage("You: " + message, true);
            inputMessage.setText("");

            TextView aiThinking = addMessage("🌱 Plant AI: ...", false);

            // Check if asking for plant recommendations based on location
            if (isRecommendationQuestion(message)) {
                handlePlantRecommendationRequest(message, aiThinking);
            } else {
                // Regular plant question with timeout and retry
                Runnable retryAction = () -> {
                    TextView newAiThinking = addMessage("🌱 Plant AI: ...", false);
                    Call<BaseResponse> call = apiService.askPlantQuestion(message);
                    executeWithTimeout(call, newAiThinking, null);
                };
                
                Call<BaseResponse> call = apiService.askPlantQuestion(message);
                executeWithTimeout(call, aiThinking, retryAction);
            }
        }
    }
    
    private void handlePlantRecommendationRequest(String message, TextView aiThinking) {
        // Check if sensors are already initialized
        if (!sensorsInitialized) {
            aiThinking.setText("🌱 Plant AI: Collecting environmental data... Please wait a moment.");
            initializeSensors();
            // Wait a bit for sensor data to be collected
            new android.os.Handler().postDelayed(() -> {
                if (currentSensorData != null && !currentSensorData.isEmpty()) {
                    String location = sensorCollector.getLocationString();
                    Runnable retryAction = () -> {
                        TextView newAiThinking = addMessage("🌱 Plant AI: ...", false);
                        Call<BaseResponse> call = apiService.getPlantRecommendations(location, currentSensorData);
                        executeWithTimeout(call, newAiThinking, null);
                    };
                    Call<BaseResponse> call = apiService.getPlantRecommendations(location, currentSensorData);
                    executeWithTimeout(call, aiThinking, retryAction);
                } else {
                    aiThinking.setText("🌱 Plant AI: Unable to collect sensor data. Using general recommendations...");
                    // Fallback to general recommendations without sensor data
                    Runnable retryAction = () -> {
                        TextView newAiThinking = addMessage("🌱 Plant AI: ...", false);
                        Call<BaseResponse> call = apiService.getPlantRecommendations("Unknown location", new java.util.HashMap<>());
                        executeWithTimeout(call, newAiThinking, null);
                    };
                    Call<BaseResponse> call = apiService.getPlantRecommendations("Unknown location", new java.util.HashMap<>());
                    executeWithTimeout(call, aiThinking, retryAction);
                }
            }, 3000); // Wait 3 seconds for sensor data
        } else if (currentSensorData != null && !currentSensorData.isEmpty()) {
            // Use existing sensor data
            String location = sensorCollector.getLocationString();
            Runnable retryAction = () -> {
                TextView newAiThinking = addMessage("🌱 Plant AI: ...", false);
                Call<BaseResponse> call = apiService.getPlantRecommendations(location, currentSensorData);
                executeWithTimeout(call, newAiThinking, null);
            };
            Call<BaseResponse> call = apiService.getPlantRecommendations(location, currentSensorData);
            executeWithTimeout(call, aiThinking, retryAction);
        } else {
            // Sensors initialized but no data yet
            aiThinking.setText("🌱 Plant AI: Waiting for sensor data... Please wait a moment.");
            new android.os.Handler().postDelayed(() -> {
                if (currentSensorData != null && !currentSensorData.isEmpty()) {
                    String location = sensorCollector.getLocationString();
                    Runnable retryAction = () -> {
                        TextView newAiThinking = addMessage("🌱 Plant AI: ...", false);
                        Call<BaseResponse> call = apiService.getPlantRecommendations(location, currentSensorData);
                        executeWithTimeout(call, newAiThinking, null);
                    };
                    Call<BaseResponse> call = apiService.getPlantRecommendations(location, currentSensorData);
                    executeWithTimeout(call, aiThinking, retryAction);
                } else {
                    aiThinking.setText("🌱 Plant AI: Using general recommendations...");
                    Runnable retryAction = () -> {
                        TextView newAiThinking = addMessage("🌱 Plant AI: ...", false);
                        Call<BaseResponse> call = apiService.getPlantRecommendations("Unknown location", new java.util.HashMap<>());
                        executeWithTimeout(call, newAiThinking, null);
                    };
                    Call<BaseResponse> call = apiService.getPlantRecommendations("Unknown location", new java.util.HashMap<>());
                    executeWithTimeout(call, aiThinking, retryAction);
                }
            }, 2000);
        }
    }
    
    private void identifyPlantFromImage(Uri imageUri) {
        TextView aiThinking = addMessage("🌱 Plant AI: Analyzing plant image...", false);
        
        try {
            // Convert image to file for upload
            File imageFile = createImageFile(imageUri);
            if (imageFile == null) {
                aiThinking.setText("❌ Failed to process image");
                return;
            }
            
            // Create multipart request
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
            
            String location = sensorCollector.getLocationString();
            
            // Retry action for image identification
            Runnable retryAction = () -> {
                identifyPlantFromImage(imageUri);
            };
            
            // Call plant identification API with timeout and retry
            Call<BaseResponse> call = apiService.identifyPlant(imagePart, location);
            executeWithTimeout(call, aiThinking, retryAction);
            
        } catch (Exception e) {
            aiThinking.setText("❌ Error processing image: " + e.getMessage());
            Log.e(TAG, "Error processing image", e);
        }
    }
    
    private void handleAIResponse(Response<BaseResponse> response, TextView aiThinking) {
        Log.d(TAG, "=== API Response Debug ===");
        Log.d(TAG, "Response successful: " + response.isSuccessful());
        Log.d(TAG, "Response code: " + response.code());
        Log.d(TAG, "Response message: " + response.message());
        Log.d(TAG, "Response body null: " + (response.body() == null));
        
        if (response.isSuccessful() && response.body() != null) {
            BaseResponse base = response.body();
            Log.d(TAG, "API Response: " + base.toString());

            if (base.code != null && base.code == 200 && base.data != null) {
                JsonElement data = base.data;
                Log.d(TAG, "Data type: " + data.getClass().getSimpleName());
                Log.d(TAG, "Data content: " + data.toString());
                
                String reply = "";
                
                if (data.isJsonObject()) {
                    JsonObject dataObj = data.getAsJsonObject();
                    Log.d(TAG, "Data object keys: " + dataObj.keySet().toString());
                    
                    if (dataObj.has("answer")) {
                        reply = dataObj.get("answer").getAsString();
                        Log.d(TAG, "Found answer: " + reply);
                    } else if (dataObj.has("recommendations")) {
                        reply = dataObj.get("recommendations").getAsString();
                        Log.d(TAG, "Found recommendations: " + reply);
                    } else if (dataObj.has("reply")) {
                        reply = dataObj.get("reply").getAsString();
                        Log.d(TAG, "Found reply: " + reply);
                    } else if (dataObj.has("identification")) {
                        reply = dataObj.get("identification").getAsString();
                        Log.d(TAG, "Found identification: " + reply);
                    }
                }
                
                if (!reply.isEmpty()) {
                    typewriterEffect(aiThinking, "🌱 Plant AI: " + reply);
                } else {
                    aiThinking.setText("⚠️ Plant AI: Unexpected data format. Check logs for details.");
                }
            } else {
                Log.d(TAG, "Response code: " + base.code + ", message: " + base.msg);
                aiThinking.setText("⚠️ Plant AI: " + base.msg);
            }
        } else {
            Log.d(TAG, "Response not successful or body is null");
            Log.d(TAG, "Response details - Code: " + response.code() + ", Message: " + response.message());
            if (response.errorBody() != null) {
                try {
                    Log.d(TAG, "Error body: " + response.errorBody().string());
                } catch (Exception e) {
                    Log.d(TAG, "Error reading error body: " + e.getMessage());
                }
            }
            aiThinking.setText("⚠️ Plant AI: Server returned no data. Check logs for details.");
        }
        Log.d(TAG, "=== End API Response Debug ===");
    }
    
    private boolean isRecommendationQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        String[] recommendationKeywords = {
            "recommend", "suggest", "suitable", "grow", "plant", "what should i",
            "best plants", "good for", "appropriate", "ideal", "perfect for"
        };
        
        for (String keyword : recommendationKeywords) {
            if (lowerQuestion.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    // Image handling methods
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Take photo
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            } else {
                // Choose from gallery
                openGallery();
            }
        });
        builder.show();
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        // Show rationale dialog
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("📷 Camera Permission")
                .setMessage("This app needs camera access to take photos of plants for identification.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Deny", null)
                .show();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
        }
    }
    
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED;
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6.0 to 12 (API 23-32)
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        }
        return true; // No permission needed for older versions
    }
    
    private void requestStoragePermission() {
        String permission;
        String message;
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
            message = "This app needs access to your photos to select plant images for identification.";
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            message = "This app needs storage access to select photos from your gallery.";
        }
        
        // Show rationale dialog if needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("📁 Storage Permission")
                .setMessage(message)
                .setPositiveButton("Grant", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        STORAGE_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Deny", null)
                .show();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{permission},
                STORAGE_PERMISSION_REQUEST_CODE);
        }
    }
    
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }
    
    private void openGallery() {
        // Check storage permission first
        if (checkStoragePermission()) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        } else {
            requestStoragePermission();
        }
    }
    
    private File createImageFile(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            File file = new File(getCacheDir(), "plant_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            return null;
        }
    }

    private TextView addMessage(String text, boolean isUser) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setPadding(20, 12, 20, 12);
        textView.setSingleLine(false);
        textView.setMaxLines(Integer.MAX_VALUE);
        textView.setLineSpacing(0, 1.3f);
        textView.setTextIsSelectable(true);
        textView.setEllipsize(null);


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        if (isUser) {
            params.gravity = Gravity.END;
            textView.setTextColor(Color.WHITE);
            textView.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.90));
            textView.setBackgroundResource(R.drawable.bg_user_message);
        } else {
            params.gravity = Gravity.START;
            textView.setTextColor(Color.BLACK);
            textView.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.90));
            textView.setBackgroundResource(R.drawable.bg_ai_message);
        }

        textView.setLayoutParams(params);

        chatContainer.addView(textView);
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        return textView;
    }

    private void typewriterEffect(TextView textView, String fullText) {
        textView.setText("");
        final int[] index = {0};
        final int delay = 5;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index[0] < fullText.length()) {
                    textView.append(String.valueOf(fullText.charAt(index[0])));
                    textView.requestLayout();
                    textView.invalidate();


                    chatScrollView.postDelayed(() ->
                            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN), 50);

                    index[0]++;
                    handler.postDelayed(this, delay);
                } else {

                    handler.postDelayed(() -> {
                        markwon.setMarkdown(textView, fullText);
                        chatScrollView.post(() ->
                                chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    }, 150);
                }
            }
        }, delay);
    }
    
    // Initialize sensors when needed
    private void initializeSensors() {
        // Check if we need to show sensor notice (first time only)
        if (!hasSensorNoticeBeenShown()) {
            showSensorDataNotice();
            return;
        }
        
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }
        
        sensorCollector.startCollecting(this);
        sensorsInitialized = true;
        Log.d(TAG, "Sensors initialized for environmental data collection");
    }
    
    // Check if sensor notice has been shown before
    private boolean hasSensorNoticeBeenShown() {
        android.content.SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        return prefs.getBoolean("sensor_notice_shown", false);
    }
    
    // Mark sensor notice as shown
    private void markSensorNoticeShown() {
        android.content.SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        prefs.edit().putBoolean("sensor_notice_shown", true).apply();
    }
    
    // Show sensor data collection notice (first time only)
    private void showSensorDataNotice() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("🌡️ Environmental Data Collection")
            .setMessage("To provide accurate plant recommendations, this app will collect environmental data from your device:\n\n" +
                    "• 🌡️ Temperature - Ambient temperature\n" +
                    "• 💧 Humidity - Relative humidity level\n" +
                    "• 💡 Light - Light intensity\n" +
                    "• 🌪️ Pressure - Atmospheric pressure\n" +
                    "• 📍 Location - Your geographic location\n\n" +
                    "This data is used only to recommend plants suitable for your local environment. " +
                    "No personal information is collected or stored.\n\n" +
                    "Do you want to continue?")
            .setPositiveButton("Continue", (dialog, which) -> {
                markSensorNoticeShown();
                // Now proceed with location permission
                if (!hasLocationPermission()) {
                    requestLocationPermission();
                } else {
                    sensorCollector.startCollecting(this);
                    sensorsInitialized = true;
                    Log.d(TAG, "Sensors initialized for environmental data collection");
                }
            })
            .setNegativeButton("No Thanks", (dialog, which) -> {
                markSensorNoticeShown();
                addMessage("ℹ️ Environmental data collection disabled. Plant recommendations will be general.", false);
            })
            .setCancelable(false)
            .show();
    }
    
    // SensorDataCallback implementation
    @Override
    public void onSensorDataUpdated(Map<String, Object> sensorData) {
        runOnUiThread(() -> {
            // Only update sensor data in background, don't display automatically
            currentSensorData = sensorData;
            Log.d(TAG, "Sensor data updated: " + sensorData.toString());
        });
    }
    
    @Override
    public void onLocationUpdated(android.location.Location location) {
        runOnUiThread(() -> {
            // Only update location information in background, don't display automatically
            Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            addMessage("❌ Sensor Error: " + error, false);
        });
    }
    
    // Permission handling
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestLocationPermission() {
        // Show rationale dialog if needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("📍 Location Permission")
                .setMessage("To provide accurate plant recommendations, this app needs access to your location to:\n\n" +
                        "• Get your geographic location\n" +
                        "• Collect environmental data (temperature, humidity)\n" +
                        "• Recommend suitable plants for your area\n\n" +
                        "This data will only be used to analyze plants suitable for your location.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Deny", (dialog, which) -> {
                    addMessage("ℹ️ Location access denied. Plant recommendations will be general and may be less accurate.", false);
                })
                .show();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMessage("✅ Location permission granted", false);
                if (!sensorsInitialized) {
                    initializeSensors();
                }
            } else {
                addMessage("❌ Location permission denied. Plant recommendations will be general and may be less accurate.", false);
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMessage("✅ Camera permission granted", false);
                openCamera();
            } else {
                addMessage("❌ Camera permission denied. Cannot take photos.", false);
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMessage("✅ Storage permission granted", false);
                openGallery();
            } else {
                addMessage("❌ Storage permission denied. Cannot access photos from gallery.", false);
            }
        }
    }
    
    // Show image preview without sending immediately
    private void showImagePreview(Uri imageUri) {
        try {
            pendingImageUri = imageUri;
            imagePreview.setImageURI(imageUri);
            imagePreviewContainer.setVisibility(android.view.View.VISIBLE);
            
            // Focus on input field so user can add a message
            inputMessage.requestFocus();
            
            Log.d(TAG, "Image preview shown: " + imageUri.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error showing image preview", e);
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }
    
    // Clear image preview
    private void clearImagePreview() {
        pendingImageUri = null;
        imagePreview.setImageURI(null);
        imagePreviewContainer.setVisibility(android.view.View.GONE);
    }
    
    // Add user message with image to chat
    private void addUserMessageWithImage(String message, Uri imageUri) {
        // Create a container for the message
        LinearLayout messageContainer = new LinearLayout(this);
        messageContainer.setOrientation(LinearLayout.VERTICAL);
        messageContainer.setPadding(20, 12, 20, 12);
        messageContainer.setBackgroundResource(R.drawable.bg_user_message);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.setMargins(50, 8, 8, 8);
        messageContainer.setLayoutParams(params);
        
        // Add the image
        ImageView msgImage = new ImageView(this);
        msgImage.setImageURI(imageUri);
        msgImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        msgImage.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.6));
        msgImage.setAdjustViewBounds(true);
        
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.setMargins(0, 0, 0, 8);
        msgImage.setLayoutParams(imageParams);
        messageContainer.addView(msgImage);
        
        // Add the text message if not empty
        if (!message.isEmpty()) {
            TextView msgText = new TextView(this);
            msgText.setText("You: " + message);
            msgText.setTextSize(16);
            msgText.setTextColor(Color.WHITE);
            messageContainer.addView(msgText);
        }
        
        chatContainer.addView(messageContainer);
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Handle camera result
                if (data != null && data.getExtras() != null) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null) {
                        // Save bitmap to file and show preview
                        Uri imageUri = saveBitmapToFile(photo);
                        if (imageUri != null) {
                            showImagePreview(imageUri);
                        }
                    }
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                // Handle gallery result
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    showImagePreview(imageUri);
                }
            }
        }
    }
    
    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "plant_photo_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap", e);
            return null;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorCollector != null) {
            sensorCollector.stopCollecting();
        }
        // Cancel any pending API calls
        cancelCurrentApiCall();
    }
    
    // Cancel current API call if exists
    private void cancelCurrentApiCall() {
        if (currentApiCall != null && !currentApiCall.isCanceled()) {
            currentApiCall.cancel();
            Log.d(TAG, "API call cancelled");
        }
        if (currentTimeoutTask != null) {
            handler.removeCallbacks(currentTimeoutTask);
            currentTimeoutTask = null;
        }
    }
    
    // Execute API call with timeout and retry functionality
    private void executeWithTimeout(Call<BaseResponse> call, TextView aiThinking, Runnable retryAction) {
        // Cancel any previous call
        cancelCurrentApiCall();
        
        currentApiCall = call;
        currentAiThinkingView = aiThinking;
        
        // Set timeout handler (15 seconds)
        currentTimeoutTask = () -> {
            if (currentApiCall != null && !currentApiCall.isExecuted()) {
                cancelCurrentApiCall();
                showTimeoutMessage(aiThinking, retryAction);
            }
        };
        handler.postDelayed(currentTimeoutTask, 15000); // 15 seconds timeout
        
        // Execute the API call
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                // Cancel timeout since we got a response
                if (currentTimeoutTask != null) {
                    handler.removeCallbacks(currentTimeoutTask);
                    currentTimeoutTask = null;
                }
                handleAIResponse(response, aiThinking);
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                // Cancel timeout
                if (currentTimeoutTask != null) {
                    handler.removeCallbacks(currentTimeoutTask);
                    currentTimeoutTask = null;
                }
                
                if (call.isCanceled()) {
                    Log.d(TAG, "API call was cancelled");
                } else {
                    showErrorWithRetry(aiThinking, "Connection failed: " + t.getMessage(), retryAction);
                }
            }
        });
    }
    
    // Show timeout message with retry button
    private void showTimeoutMessage(TextView aiThinking, Runnable retryAction) {
        runOnUiThread(() -> {
            aiThinking.setText("⏱️ Plant AI: Request timed out (15s). The server is taking too long to respond.");
            addRetryButton(retryAction);
        });
    }
    
    // Show error message with retry button
    private void showErrorWithRetry(TextView aiThinking, String errorMsg, Runnable retryAction) {
        runOnUiThread(() -> {
            aiThinking.setText("❌ Plant AI: " + errorMsg);
            addRetryButton(retryAction);
        });
    }
    
    // Add retry button to the chat
    private void addRetryButton(Runnable retryAction) {
        Button retryButton = new Button(this);
        retryButton.setText("🔄 Retry");
        retryButton.setTextColor(Color.WHITE);
        retryButton.setBackgroundColor(Color.parseColor("#4CAF50"));
        retryButton.setPadding(40, 20, 40, 20);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(0, 20, 0, 20);
        retryButton.setLayoutParams(params);
        
        retryButton.setOnClickListener(v -> {
            // Remove the retry button
            chatContainer.removeView(retryButton);
            // Execute the retry action
            if (retryAction != null) {
                retryAction.run();
            }
        });
        
        chatContainer.addView(retryButton);
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

}

