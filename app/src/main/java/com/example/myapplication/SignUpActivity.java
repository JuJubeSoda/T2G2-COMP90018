package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnCreate = findViewById(R.id.btnCreate);
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm  = findViewById(R.id.etConfirm);

        // Right-side teal link: "Already have an account?"
        TextView tvGoSignIn = findViewById(R.id.tvGoSignIn);
        tvGoSignIn.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));

        btnBack.setOnClickListener(v -> onBackPressed());

        btnCreate.setOnClickListener(v -> {
            String email   = val(etEmail);
            String pwd     = val(etPassword);
            String confirm = val(etConfirm);

            if (email.isEmpty())   { etEmail.setError("Email required"); return; }
            if (pwd.isEmpty())     { etPassword.setError("Password required"); return; }
            if (!pwd.equals(confirm)) { etConfirm.setError("Passwords do not match"); return; }

            // TODO: call backend register API here. On success:
            Toast.makeText(this, "Account created: " + email, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignUpActivity.this, SignUpSuccessActivity.class));
            finish(); // prevent going back to Sign Up
        });
    }

    private String val(TextInputEditText v) {
        return v.getText() != null ? v.getText().toString().trim() : "";
    }
}

