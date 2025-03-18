package com.example.mealmatess;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    private ImageButton backButton;
    private LinearLayout faq1Header;
    private TextView faq1Content;
    private ImageView faq1Arrow;
    private TextView contactEmail;
    private boolean isFaq1Expanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        backButton = findViewById(R.id.back_button);
        faq1Header = findViewById(R.id.faq1_header);
        faq1Content = findViewById(R.id.faq1_content);
        faq1Arrow = findViewById(R.id.faq1_arrow);
        contactEmail = findViewById(R.id.contact_email);

        // Back button to navigate to MainActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(HelpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Expandable FAQ
        faq1Header.setOnClickListener(v -> {
            isFaq1Expanded = !isFaq1Expanded;
            faq1Content.setVisibility(isFaq1Expanded ? View.VISIBLE : View.GONE);
            faq1Arrow.setRotation(isFaq1Expanded ? 180 : 0);
        });

        // Clickable email
        contactEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@mealmate.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help & Support - MealMate");
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            }
        });
    }
}