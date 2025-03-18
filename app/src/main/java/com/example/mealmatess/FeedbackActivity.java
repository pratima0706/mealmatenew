package com.example.mealmatess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText feedbackTitleEditText, feedbackMessageEditText;
    private Button submitFeedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        backButton = findViewById(R.id.back_button);
        feedbackTitleEditText = findViewById(R.id.feedback_title);
        feedbackMessageEditText = findViewById(R.id.feedback_message);
        submitFeedbackButton = findViewById(R.id.submit_feedback_button);

        submitFeedbackButton.setOnClickListener(v -> {
            String title = feedbackTitleEditText.getText().toString().trim();
            String message = feedbackMessageEditText.getText().toString().trim();
            if (!title.isEmpty() && !message.isEmpty()) {
                // Simulate submitting feedback (e.g., toast for now; could save to a database)
                Toast.makeText(this, "Feedback submitted: " + title, Toast.LENGTH_SHORT).show();
                // Clear the input fields
                feedbackTitleEditText.setText("");
                feedbackMessageEditText.setText("");
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> {
            // Navigate to MainActivity (home page)
            Intent intent = new Intent(FeedbackActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}