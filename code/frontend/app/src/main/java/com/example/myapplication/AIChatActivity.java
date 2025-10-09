package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
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

public class AIChatActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText inputMessage;
    private Button sendButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aichat);

        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        apiService = ApiClient.create(this);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // 用户消息
        addMessage("You: " + message, true);
        inputMessage.setText("");

        // 调用后端 AI 接口
        apiService.askQuestion(message).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse base = response.body();

                    // ✅ 判断 code、msg、data
                    if (base.code != null && base.code == 200 && base.data != null) {
                        JsonElement data = base.data;
                        if (data.isJsonObject() && data.getAsJsonObject().has("reply")) {
                            String reply = data.getAsJsonObject().get("reply").getAsString();
                            addMessage("AI Assistant: " + reply, false);
                        } else {
                            addMessage("⚠️ AI Assistant: Unexpected data format.", false);
                        }
                    } else {
                        addMessage("⚠️ AI Assistant: " + base.msg, false);
                    }
                } else {
                    addMessage("⚠️ AI Assistant: Server returned no data.", false);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                addMessage("❌ Connection failed: " + t.getMessage(), false);
            }
        });
    }

    // ✅ 改进的 addMessage（自动换行 + 复制 + 左右对齐）
    private void addMessage(String text, boolean isUser) {
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
            textView.setBackgroundColor(Color.parseColor("#3F51B5")); // 蓝色背景
            textView.setTextColor(Color.WHITE);
        } else {
            params.gravity = Gravity.START;
            textView.setBackgroundColor(Color.parseColor("#EDEDED")); // 灰色背景
            textView.setTextColor(Color.BLACK);
        }

        textView.setLayoutParams(params);
        chatContainer.addView(textView);

        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
