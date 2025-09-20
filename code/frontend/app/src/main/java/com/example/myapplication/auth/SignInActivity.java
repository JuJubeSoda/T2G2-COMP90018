package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.BaseResponse;
import com.example.myapplication.auth.model.LoginRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.example.myapplication.auth.SignUpActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private static final String PREFS_ONBOARD = "first_login_prefs"; // 首次登录标记用
    private static final String PREFS_AUTH = "auth_prefs";           // 保存 token 用
    private static final String KEY_TOKEN = "jwt_token";             // token 存储的 key

    private ApiService api; // Retrofit 的接口对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // 初始化 Retrofit
        api = ApiClient.create(this);


        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvGoSignUp = findViewById(R.id.tvGoSignUp);
        TextView tvForgotPwd = findViewById(R.id.tvForgotPwd);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        CheckBox cbRemember = findViewById(R.id.cbRemember);

        // 返回按钮 → 关闭当前页
        btnBack.setOnClickListener(v -> finish());

        // 点击跳转到注册页
        tvGoSignUp.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));

        // 忘记密码按钮（暂时只做提示）
        tvForgotPwd.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show());

        // 登录按钮点击事件
        btnSignIn.setOnClickListener(v -> {
            String username = textOf(etUsername);
            String pwd      = textOf(etPassword);

            // 输入校验
            if (TextUtils.isEmpty(username)) { etUsername.setError("Username required"); etUsername.requestFocus(); return; }
            if (TextUtils.isEmpty(pwd))      { etPassword.setError("Password required"); etPassword.requestFocus(); return; }

            btnSignIn.setEnabled(false);

            // 调用后端 /user/login 接口
            api.login(new LoginRequest(username, pwd)).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    btnSignIn.setEnabled(true);
                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(SignInActivity.this, "Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BaseResponse br = response.body();

                    // 提取后端返回的token
                    String token = null;
                    JsonElement d = br.data;
                    if (d != null) {
                        if (d.isJsonPrimitive()) {
                            token = d.getAsString(); // 情况1：data 是字符串
                        } else if (d.isJsonObject()) {
                            JsonObject o = d.getAsJsonObject();
                            if (o.has("token")) {
                                token = o.get("token").getAsString(); // 情况2：data 是对象
                            }
                        }
                    }

                    // 如果没拿到 token
                    if (token == null || token.isEmpty()) {
                        Toast.makeText(SignInActivity.this, "No token returned", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 保存 token 到 SharedPreferences
                    saveToken(token);

                    // 记住我功能（选做）
                    if (cbRemember.isChecked()) {
                        // TODO: save username locally if needed
                    }

                    // ===== 首次登录逻辑（本地模拟）=====
                    // 用"username"作为 key 进行首次登录标记
                    boolean isFirstLogin = isFirstLoginLocal(username);

                    if (isFirstLogin) {
                        // 跳转到首次登录设置页，并传递 username
                        Intent it = new Intent(SignInActivity.this, NewUserSetting1Activity.class);
                        it.putExtra("username", username);
                        startActivity(it);
                    } else {
                        // 非首次，进入主页面
                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    btnSignIn.setEnabled(true);
                    Toast.makeText(SignInActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String textOf(TextInputEditText v) {
        return v.getText() != null ? v.getText().toString().trim() : "";
    }

    // ======= 首次登录逻辑（本地模拟）=======
    // 用 username 作为 key：如果 SharedPreferences 里没有标记过这个 username → 视为首次登录
    private boolean isFirstLoginLocal(String username) {
        SharedPreferences sp = getSharedPreferences(PREFS_ONBOARD, MODE_PRIVATE);
        return !sp.getBoolean(username, false);
    }

    // 设置页完成后调用：标记该 username 已完成首次设置
    public static void markOnboarded(AppCompatActivity activity, String username) {
        SharedPreferences sp = activity.getSharedPreferences(PREFS_ONBOARD, MODE_PRIVATE);
        sp.edit().putBoolean(username, true).apply();
    }

    // ======= token 保存/读取 =======
    // 保存 token
    private void saveToken(String token) {
        SharedPreferences sp = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    // 读取 token
    private String readToken() {
        SharedPreferences sp = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        return sp.getString(KEY_TOKEN, "");
    }
}
