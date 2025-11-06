package com.example.myapplication.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.NewUserSetting1Activity;
import com.example.myapplication.R;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.BaseResponse;
import com.example.myapplication.auth.model.LoginRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private static final String PREFS_ONBOARD = "first_login_prefs"; // First-login flag
    private static final String PREFS_AUTH = "auth_prefs";           // Store token
    private static final String KEY_TOKEN = "jwt_token";             // Token storage key

    private ApiService api; // Retrofit API interface

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Retrofit
        api = ApiClient.create(this);


        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvGoSignUp = findViewById(R.id.tvGoSignUp);
        TextView tvForgotPwd = findViewById(R.id.tvForgotPwd);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        CheckBox cbRemember = findViewById(R.id.cbRemember);

        // Back button → close current page
        btnBack.setOnClickListener(v -> finish());

        // Click to navigate to Sign Up page
        tvGoSignUp.setOnClickListener(v ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));

        // Forgot password button (temporary hint only)
        tvForgotPwd.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show());

        // Sign-in button click
        btnSignIn.setOnClickListener(v -> {
            String username = textOf(etUsername);
            String pwd      = textOf(etPassword);

            // Input validation
            if (TextUtils.isEmpty(username)) { etUsername.setError("Username required"); etUsername.requestFocus(); return; }
            if (TextUtils.isEmpty(pwd))      { etPassword.setError("Password required"); etPassword.requestFocus(); return; }

            btnSignIn.setEnabled(false);

            // Call backend /user/login endpoint
            api.login(new LoginRequest(username, pwd)).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    btnSignIn.setEnabled(true);
                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(SignInActivity.this, "Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BaseResponse br = response.body();
                    
                    // Check whether login succeeded
                    if (br.code != null && br.code != 200) {
                        // Login failed, show specific error
                        String errorMessage = getLoginErrorMessage(br.code, br.msg);
                        Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Extract token from backend response
                    String token = null;
                    JsonElement d = br.data;
                    if (d != null) {
                        if (d.isJsonPrimitive()) {
                            token = d.getAsString(); // Case 1: data is a string
                        } else if (d.isJsonObject()) {
                            JsonObject o = d.getAsJsonObject();
                            if (o.has("token")) {
                                token = o.get("token").getAsString(); // Case 2: data is an object
                            }
                        }
                    }

                    // If token not received
                    if (token == null || token.isEmpty()) {
                        Toast.makeText(SignInActivity.this, "No token returned", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save token to SharedPreferences
                    saveToken(token);

                    // Remember-me feature (optional)
                    if (cbRemember.isChecked()) {
                        // TODO: save username locally if needed
                    }

                    // ===== First-login flow (local simulation) =====
                    // Use "username" as the key to flag first login
                    boolean isFirstLogin = isFirstLoginLocal(username);

                    if (isFirstLogin) {
                        // Navigate to initial settings and pass username
                        Intent it = new Intent(SignInActivity.this, NewUserSetting1Activity.class);
                        it.putExtra("username", username);
                        startActivity(it);
                    } else {
                        // Not first time, go to main page
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

    // ======= First-login logic (local simulation) =======
    // Use username as key: if SharedPreferences has no flag for this username → treat as first login
    private boolean isFirstLoginLocal(String username) {
        SharedPreferences sp = getSharedPreferences(PREFS_ONBOARD, MODE_PRIVATE);
        return !sp.getBoolean(username, false);
    }

    // Called after settings complete: mark this username as onboarded
    public static void markOnboarded(AppCompatActivity activity, String username) {
        SharedPreferences sp = activity.getSharedPreferences(PREFS_ONBOARD, MODE_PRIVATE);
        sp.edit().putBoolean(username, true).apply();
    }

    // ======= Token save/load =======
    // Save token
    private void saveToken(String token) {
        SharedPreferences sp = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    // Read token
    private String readToken() {
        SharedPreferences sp = getSharedPreferences(PREFS_AUTH, MODE_PRIVATE);
        return sp.getString(KEY_TOKEN, "");
    }
    
    // Return user-friendly error message based on error code
    private String getLoginErrorMessage(Integer code, String message) {
        if (code == null) {
            return "Login failed. Please try again.";
        }
        
        switch (code) {
            case 205: // FAIL_USER_NOT_FOUND
                return "User not found. Please check your username or register first.";
            case 206: // FAIL_WRONG_PASSWORD
                return "Incorrect password. Please try again.";
            case 202: // FAIL_LOGIN_ERROR
                return "Login failed. Please check your credentials.";
            default:
                return message != null ? message : "Login failed. Please try again.";
        }
    }
}
