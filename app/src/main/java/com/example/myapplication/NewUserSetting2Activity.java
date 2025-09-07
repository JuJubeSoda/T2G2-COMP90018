package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class NewUserSetting2Activity extends AppCompatActivity {

    private TextInputLayout tilName, tilGender, tilLocation;
    private TextInputEditText etName, etGender, etLocation;
    private MaterialButton btnNext;

    private String emailFromFlow; // 从登录页传进来的邮箱

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_setting2);

        // 取邮箱（SignInActivity 跳转时 putExtra("email", email)）
        emailFromFlow = getIntent().getStringExtra("email");

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> onBackPressed());

        tilName = findViewById(R.id.tilName);
        tilGender = findViewById(R.id.tilGender);
        tilLocation = findViewById(R.id.tilLocation);
        etName = findViewById(R.id.etName);
        etGender = findViewById(R.id.etGender);
        etLocation = findViewById(R.id.etLocation);
        btnNext = findViewById(R.id.btnNext);

        attachWatcher(tilName, etName);
        attachWatcher(tilGender, etGender);
        attachWatcher(tilLocation, etLocation);

        btnNext.setEnabled(allValid());

        btnNext.setOnClickListener(v -> {
            if (validateRequired(tilName, etName, "Name is required")) return;
            if (validateRequired(tilGender, etGender, "Gender is required")) return;
            if (validateRequired(tilLocation, etLocation, "Location is required")) return;

            // 1) 保存资料（后端就绪后改为调 API）
            saveProfileLocal(getTrim(etName), getTrim(etGender), getTrim(etLocation));

            // 2) 标记该邮箱已完成首次引导（配合之前 SignInActivity 的本地首次登录判断）
            if (!TextUtils.isEmpty(emailFromFlow)) {
                SignInActivity.markOnboarded(this, emailFromFlow);
            }

            // 3) 跳转主页面（还没有 HomeActivity ，WelcomeActivity 代替）
            startActivity(new Intent(this, WelcomeActivity.class));

            finishAffinity();
        });
    }

    private void attachWatcher(TextInputLayout til, TextInputEditText et) {
        til.setEndIconVisible(false);

        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                til.setError(null);
                boolean ok = !TextUtils.isEmpty(s.toString().trim());
                til.setEndIconVisible(ok);
                btnNext.setEnabled(allValid());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateRequired(TextInputLayout til, TextInputEditText et, String msg) {
        String value = getTrim(et);
        if (TextUtils.isEmpty(value)) {
            til.setError(msg);
            et.requestFocus();
            return true;
        } else {
            til.setError(null);
            return false;
        }
    }

    private boolean allValid() {
        return !TextUtils.isEmpty(getTrim(etName))
                && !TextUtils.isEmpty(getTrim(etGender))
                && !TextUtils.isEmpty(getTrim(etLocation));
    }

    private String getTrim(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    // 本地保存
    private void saveProfileLocal(String name, String gender, String location) {
        SharedPreferences sp = getSharedPreferences("profile_local", MODE_PRIVATE);
        sp.edit()
                .putString("name", name)
                .putString("gender", gender)
                .putString("location", location)
                .apply();
    }
}
