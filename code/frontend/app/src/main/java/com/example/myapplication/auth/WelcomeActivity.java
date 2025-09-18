package com.example.myapplication.auth;

import android.content.Intent;
import com.example.myapplication.SignInActivity;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnCreate);

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, SignInActivity.class)));

        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class)));
    }
}

