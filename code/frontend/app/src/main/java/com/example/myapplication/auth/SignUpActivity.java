package com.example.myapplication.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;        // New: direct import usage
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

        // Initialize Retrofit
        api = ApiClient.create(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnCreate = findViewById(R.id.btnCreate);

        //
        etUsername = findViewById(R.id.etUsername); // ← New: the Username input you just added
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm  = findViewById(R.id.etConfirm);

        // Already have an account
        TextView tvGoSignIn = findViewById(R.id.tvGoSignIn);
        tvGoSignIn.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));

        btnBack.setOnClickListener(v -> onBackPressed());

        btnCreate.setOnClickListener(v -> {
            // Add debug log
            // Toast.makeText(SignUpActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
            
            // Read user input
            String username = val(etUsername); // Username only
            String email    = val(etEmail);    // Email
            String pwd      = val(etPassword);
            String confirm  = val(etConfirm);

            // Form validation (can be enhanced later)
            if (username.isEmpty()) { 
                // Toast.makeText(SignUpActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                etUsername.setError("Username required"); 
                return; 
            }
            if (email.isEmpty()) { 
                // Toast.makeText(SignUpActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                etEmail.setError("Email required"); 
                return; 
            }
            if (pwd.isEmpty()) { 
                // Toast.makeText(SignUpActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                etPassword.setError("Password required"); 
                return; 
            }
            if (!pwd.equals(confirm)) { 
                // Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                etConfirm.setError("Passwords do not match"); 
                return; 
            }

            // Build request body
            // Backend requires username, phone, password, email
            // No phone input for now, use placeholder; add UI later if needed
            RegisterRequest req = new RegisterRequest(username, "0000000000", pwd, email);

            // Add debug log
            Toast.makeText(SignUpActivity.this, "Registering", Toast.LENGTH_SHORT).show();
            
            // Call backend register API
            api.register(req).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(SignUpActivity.this, "Register failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Registration succeeded → navigate to success screen
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

    // Read text from input field
    private String val(TextInputEditText v) {
        return v.getText() != null ? v.getText().toString().trim() : "";
    }
}
