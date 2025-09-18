package com.example.myapplication.auth;

import android.content.Intent;
import com.example.myapplication.SignInActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;        // 新增：直接 import 使用
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.BaseResponse;
import com.example.myapplication.auth.model.RegisterRequest;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etEmail, etPassword, etConfirm;

    private ApiService api; // Retrofit API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 初始化 Retrofit
        api = ApiClient.create(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnCreate = findViewById(R.id.btnCreate);

        //
        etUsername = findViewById(R.id.etUsername); // ← 新增：来自你刚加的 Username 输入框
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm  = findViewById(R.id.etConfirm);

        // Already have an account
        TextView tvGoSignIn = findViewById(R.id.tvGoSignIn);
        tvGoSignIn.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));

        btnBack.setOnClickListener(v -> onBackPressed());

        btnCreate.setOnClickListener(v -> {
            // 读取用户输入
            String username = val(etUsername); // 中文：单独的用户名
            String email    = val(etEmail);    // 中文：邮箱
            String pwd      = val(etPassword);
            String confirm  = val(etConfirm);

            // 表单校验（按需可继续增强）
            if (username.isEmpty()) { etUsername.setError("Username required"); return; }
            if (email.isEmpty())    { etEmail.setError("Email required"); return; }
            if (pwd.isEmpty())      { etPassword.setError("Password required"); return; }
            if (!pwd.equals(confirm)) { etConfirm.setError("Passwords do not match"); return; }

            // 组装请求体
            // 中文：后端需要 username、phone、password、email
            // 目前没有 phone 输入框，这里用占位符；后续需要时再加 UI
            RegisterRequest req = new RegisterRequest(username, "0000000000", pwd, email);

            // 调用后端注册 API
            api.register(req).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(SignUpActivity.this, "Register failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 注册成功 → 跳转成功页面
                    Toast.makeText(SignUpActivity.this, "Account created: " + username, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, SignUpSuccessActivity.class));
                    finish();
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // 读取输入框文本
    private String val(TextInputEditText v) {
        return v.getText() != null ? v.getText().toString().trim() : "";
    }
}
