package com.example.mealmatess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmatess.utils.DataManager;

public class ForgetPasswordActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText emailEditText;
    private Button resetButton;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        backButton = findViewById(R.id.back_button);
        emailEditText = findViewById(R.id.email);
        resetButton = findViewById(R.id.resetButton);
        dataManager = new DataManager(this);

        resetButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (isValidEmail(email)) {
                // Simulate sending reset email
                dataManager.sendResetEmail(email);
                Toast.makeText(this, "Reset email sent to " + email + ". Check your inbox.", Toast.LENGTH_SHORT).show();
                // Navigate to LoginActivity
                Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> {
            // Navigate to MainActivity (home page)
            Intent intent = new Intent(ForgetPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean isValidEmail(String email) {
        // Basic email validation (can be enhanced with regex)
        return email.contains("@") && email.contains(".") && !email.isEmpty();
    }
}