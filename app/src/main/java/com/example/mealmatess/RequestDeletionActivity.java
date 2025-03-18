package com.example.mealmatess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmatess.utils.DataManager;

public class RequestDeletionActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button confirmDeletionButton, cancelDeletionButton;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_deletion);

        backButton = findViewById(R.id.back_button);
        confirmDeletionButton = findViewById(R.id.confirmDeletionButton);
        cancelDeletionButton = findViewById(R.id.cancelDeletionButton);
        dataManager = new DataManager(this);

        confirmDeletionButton.setOnClickListener(v -> {
            dataManager.deleteAccount();
            Toast.makeText(this, "Account deletion requested!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RequestDeletionActivity.this, LoginActivity.class));
            finish();
        });

        cancelDeletionButton.setOnClickListener(v -> finish());

        backButton.setOnClickListener(v -> finish());
    }
}