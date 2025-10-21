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
    private static final int CAMERA_REQUEST_CODE = 1003;
    private static final int GALLERY_REQUEST_CODE = 1004;

    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText inputMessage;
    private Button sendButton;
    private ImageButton cameraButton;

    private Markwon markwon;

    private ApiService apiService;
    private Handler handler = new Handler();
    
    // Sensor functionality - Auto-start
    private SensorDataCollector sensorCollector;
    private Map<String, Object> currentSensorData;
    private boolean sensorsInitialized = false;
    
    // Camera functionality
    private Uri currentImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aichat);

        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        cameraButton = findViewById(R.id.cameraButton);

        apiService = ApiClient.create(this);
        sensorCollector = new SensorDataCollector(this);

        markwon = Markwon.create(this);

        sendButton.setOnClickListener(v -> sendMessage());
        cameraButton.setOnClickListener(v -> showImageSourceDialog());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        
        // Auto-start sensors for environmental data
        initializeSensors();
        
        // Add welcome message
        addMessage("🌱 Plant AI Assistant: Hello! I can help you:\n• 📷 Identify plants from photos\n• 🌍 Recommend plants for your area\n• 💬 Answer plant-related questions\n\nAsk me: 'What plants should I grow?' or upload a plant photo!", false);
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        addMessage("You: " + message, true);
        inputMessage.setText("");

        TextView aiThinking = addMessage("🌱 Plant AI: ...", false);

        // Check if asking for plant recommendations based on location
        if (isRecommendationQuestion(message)) {
            handlePlantRecommendationRequest(message, aiThinking);
        } else {
            // Regular plant question
            apiService.askPlantQuestion(message).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    handleAIResponse(response, aiThinking);
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    aiThinking.setText("❌ Connection failed: " + t.getMessage());
                }
            });
        }
    }
    
    private void handlePlantRecommendationRequest(String message, TextView aiThinking) {
        // Auto-use sensor data for recommendations
        if (currentSensorData != null && !currentSensorData.isEmpty()) {
            String location = sensorCollector.getLocationString();
            
            // 直接调用API，不显示环境数据
            apiService.getPlantRecommendations(location, currentSensorData).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    handleAIResponse(response, aiThinking);
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    aiThinking.setText("❌ Connection failed: " + t.getMessage());
                }
            });
        } else {
            aiThinking.setText("🌱 Plant AI: I need to collect environmental data first. Please wait a moment...");
            // Try to initialize sensors if not done yet
            if (!sensorsInitialized) {
                initializeSensors();
            }
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
            
            // Call plant identification API
            apiService.identifyPlant(imagePart, location).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    handleAIResponse(response, aiThinking);
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    aiThinking.setText("❌ Plant identification failed: " + t.getMessage());
                }
            });
            
        } catch (Exception e) {
            aiThinking.setText("❌ Error processing image: " + e.getMessage());
            Log.e(TAG, "Error processing image", e);
        }
    }
    
    private void handleAIResponse(Response<BaseResponse> response, TextView aiThinking) {
        if (response.isSuccessful() && response.body() != null) {
            BaseResponse base = response.body();

            if (base.code != null && base.code == 200 && base.data != null) {
                JsonElement data = base.data;
                String reply = "";
                
                if (data.isJsonObject()) {
                    if (data.getAsJsonObject().has("answer")) {
                        reply = data.getAsJsonObject().get("answer").getAsString();
                    } else if (data.getAsJsonObject().has("recommendations")) {
                        reply = data.getAsJsonObject().get("recommendations").getAsString();
                    } else if (data.getAsJsonObject().has("reply")) {
                        reply = data.getAsJsonObject().get("reply").getAsString();
                    }
                }
                
                if (!reply.isEmpty()) {
                    typewriterEffect(aiThinking, "🌱 Plant AI: " + reply);
                } else {
                    aiThinking.setText("⚠️ Plant AI: Unexpected data format.");
                }
            } else {
                aiThinking.setText("⚠️ Plant AI: " + base.msg);
            }
        } else {
            aiThinking.setText("⚠️ Plant AI: Server returned no data.");
        }
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
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.CAMERA},
            CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }
    
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
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
    
    // Auto-initialize sensors
    private void initializeSensors() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }
        
        sensorCollector.startCollecting(this);
        sensorsInitialized = true;
        addMessage("📡 Sensors: Ready to provide personalized plant recommendations!", false);
    }
    
    // SensorDataCallback implementation
    @Override
    public void onSensorDataUpdated(Map<String, Object> sensorData) {
        runOnUiThread(() -> {
            // 只在后台更新传感器数据，不自动显示
            currentSensorData = sensorData;
            Log.d(TAG, "Sensor data updated: " + sensorData.toString());
        });
    }
    
    @Override
    public void onLocationUpdated(android.location.Location location) {
        runOnUiThread(() -> {
            // 只在后台更新位置信息，不自动显示
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
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            LOCATION_PERMISSION_REQUEST_CODE);
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
                addMessage("❌ Location permission denied", false);
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                addMessage("❌ Camera permission denied", false);
            }
        }
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
                        // Save bitmap to file and identify plant
                        Uri imageUri = saveBitmapToFile(photo);
                        if (imageUri != null) {
                            identifyPlantFromImage(imageUri);
                        }
                    }
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                // Handle gallery result
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    identifyPlantFromImage(imageUri);
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
    }

}

