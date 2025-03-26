package com.example.mealmatess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.utils.DataManager;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = "EditActivity";
    private ImageButton backButton;
    private ImageButton pickImageButton;
    private ImageView editImage;
    private EditText editName;
    private EditText editDetail;
    private Bitmap mealImage;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private String originalName;
    private DataManager dataManager;
    private Meal originalMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        backButton = findViewById(R.id.back_button);
        pickImageButton = findViewById(R.id.pick_image_button);
        editImage = findViewById(R.id.edit_image);
        editName = findViewById(R.id.edit_name);
        editDetail = findViewById(R.id.edit_detail);
        dataManager = new DataManager(this);

        // Get the original meal name from the intent
        originalName = getIntent().getStringExtra("mealName");
        if (originalName != null) {
            originalMeal = dataManager.getMeal(originalName);
            if (originalMeal != null) {
                editName.setText(originalMeal.getName());
                editDetail.setText(originalMeal.getIngredients()); // Assuming edit_detail is for ingredients
                mealImage = originalMeal.getImage();
                if (mealImage != null) {
                    editImage.setImageBitmap(mealImage);
                    editImage.setVisibility(View.VISIBLE);
                    pickImageButton.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "Meal not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No meal selected to edit", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize pickImageLauncher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            mealImage = Bitmap.createScaledBitmap(originalBitmap, 100, 100, true); // Match ImageView size
                            editImage.setImageBitmap(mealImage);
                            editImage.setVisibility(View.VISIBLE);
                            pickImageButton.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to load image: " + e.getMessage(), e);
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        pickImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        findViewById(R.id.save_button).setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newDetail = editDetail.getText().toString().trim(); // Assuming this is ingredients
            if (!newName.isEmpty() && !newDetail.isEmpty()) {
                if (originalMeal != null) {
                    dataManager.updateMeal(
                            originalName,
                            newName,
                            newDetail, // Ingredients
                            originalMeal.getInstructions(), // Retain original instructions
                            mealImage != null ? mealImage : originalMeal.getImage(), // Use new or original image
                            originalMeal.getDay() != null ? originalMeal.getDay() : "" // Retain original day
                    );
                    Toast.makeText(this, "Meal updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class); // Navigate back to FavoriteActivity
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Original meal data not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }
}