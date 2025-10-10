package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.BaseResponse;
import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import io.noties.markwon.Markwon;


public class AIChatActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText inputMessage;
    private Button sendButton;

    private Markwon markwon;

    private ApiService apiService;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aichat);

        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        apiService = ApiClient.create(this);

        markwon = Markwon.create(this);

        sendButton.setOnClickListener(v -> sendMessage());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        addMessage("You: " + message, true);
        inputMessage.setText("");

        TextView aiThinking = addMessage("AI Assistant: ...", false);

        apiService.askQuestion(message).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse base = response.body();

                    if (base.code != null && base.code == 200 && base.data != null) {
                        JsonElement data = base.data;
                        if (data.isJsonObject() && data.getAsJsonObject().has("reply")) {
                            String reply = data.getAsJsonObject().get("reply").getAsString();


                            typewriterEffect(aiThinking, "\uD83E\uDD16 AI Assistant: " + reply);
                        } else {
                            aiThinking.setText("⚠️ AI Assistant: Unexpected data format.");
                        }
                    } else {
                        aiThinking.setText("⚠️ AI Assistant: " + base.msg);
                    }
                } else {
                    aiThinking.setText("⚠️ AI Assistant: Server returned no data.");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                aiThinking.setText("❌ Connection failed: " + t.getMessage());
            }
        });
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



}

