package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class NewUserSetting1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_setting1);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            // Go to page 2
            Intent i = new Intent(NewUserSetting1Activity.this, NewUserSetting2Activity.class);
            i.putExtra("progress", 50);
            startActivity(i);
        });
    }
}
