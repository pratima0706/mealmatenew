package com.example.mealmatess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds
    private static final String PREFS_NAME = "MealMatePrefs";
    private static final String KEY_FIRST_INSTALL = "first_install";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstInstall = preferences.getBoolean(KEY_FIRST_INSTALL, true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFirstInstall) {
                // First install, go to LoginActivity
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_FIRST_INSTALL, false);
                editor.apply();
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                // Check "Remember Me" preference
                boolean rememberMe = preferences.getBoolean("rememberMe", false);
                if (rememberMe) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            }
            finish();
        }, SPLASH_DURATION);
    }
}