package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignInActivity extends AppCompatActivity {

    private static final String PREFS_ONBOARD = "first_login_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvGoSignUp = findViewById(R.id.tvGoSignUp);
        TextView tvForgotPwd = findViewById(R.id.tvForgotPwd);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        CheckBox cbRemember = findViewById(R.id.cbRemember);

        btnBack.setOnClickListener(v -> finish());
        tvGoSignUp.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
        tvForgotPwd.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show());

        // 点击登录后，判断是否首次登录 -> 跳转
        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String pwd   = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (email.isEmpty()) { etEmail.setError("Email required"); etEmail.requestFocus(); return; }
            if (pwd.isEmpty())   { etPassword.setError("Password required"); etPassword.requestFocus(); return; }

            // TODO: 这里将来调用后端登录接口，拿到 isFirstLogin
            // boolean isFirstLogin = serverResponse.isFirstLogin();
            boolean isFirstLogin = isFirstLoginLocal(email); // 先用本地逻辑模拟

            if (isFirstLogin) {
                // 跳转到首次登录设置页
                Intent it = new Intent(SignInActivity.this, NewUserSetting1Activity.class);
                it.putExtra("email", email);
                startActivity(it);
            } else {
                // 非首次，正常进主界面（先用 Toast 占位）
                Toast.makeText(this, "Welcome back: " + email, Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(this, HomeActivity.class));
            }
        });
    }

    // ===== 本地“是否首次登录”模拟：某邮箱从未标记过就认为是首次 =====
    private boolean isFirstLoginLocal(String email) {
        SharedPreferences sp = getSharedPreferences(PREFS_ONBOARD, MODE_PRIVATE);
        return !sp.getBoolean(email, false);
    }

    // 供设置页完成后调用：标记该邮箱已完成首次设置
    public static void markOnboarded(AppCompatActivity activity, String email) {
        SharedPreferences sp = activity.getSharedPreferences(PREFS_ONBOARD, MODE_PRIVATE);
        sp.edit().putBoolean(email, true).apply();
    }
}

