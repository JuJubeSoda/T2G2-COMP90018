package com.example.myapplication.auth;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.BaseResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfileActivity extends AppCompatActivity {

    private ApiService api;
    private TextInputEditText etUsername, etGender, etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_setting2);

        api = ApiClient.create(this);


        ImageButton btnBack = findViewById(R.id.btnBack);
        etUsername = findViewById(R.id.etName);
        etGender = findViewById(R.id.etGender);
        etLocation = findViewById(R.id.etLocation);
        MaterialButton btnNext = findViewById(R.id.btnNext);

        ((android.widget.TextView) findViewById(R.id.tvTitle)).setText("My Profile");
        btnNext.setText("Refresh");


        etUsername.setEnabled(false);
        etGender.setEnabled(false);
        etLocation.setEnabled(false);


        btnBack.setOnClickListener(v -> finish());


        btnNext.setOnClickListener(v -> loadUserInfo());

        loadUserInfo();
    }

    private void loadUserInfo() {

        api.getUserInfo().enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MyProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    return;
                }

                BaseResponse br = response.body();

                if (br.data != null && br.data.isJsonObject()) {

                    JsonObject user = br.data.getAsJsonObject();

                    String username = user.has("username") ? user.get("username").getAsString() : "Yifei Jia";
                    String gender   = user.has("gender")   ? user.get("gender").getAsString()   : "female";
                    String location = user.has("location") ? user.get("location").getAsString() : "Australia";

                    etUsername.setText(username);
                    etGender.setText(gender);
                    etLocation.setText(location);

                } else {
                    Toast.makeText(MyProfileActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Toast.makeText(MyProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
