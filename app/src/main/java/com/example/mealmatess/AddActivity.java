package com.example.mealmatess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mealmatess.R;
import com.example.mealmatess.utils.DataManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ImageButton pickImageButton;
    private ImageView recipeImageView;
    private MaterialButton saveRecipeButton;
    private TextInputEditText recipeNameInput;
    private TextInputEditText ingredientsInput;
    private TextInputEditText instructionsInput;
    private Spinner daySpinner;
    private Bitmap mealImage;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private String preSelectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        backButton = findViewById(R.id.back_button);
        pickImageButton = findViewById(R.id.pick_image_button);
        recipeImageView = findViewById(R.id.recipe_image);
        saveRecipeButton = findViewById(R.id.save_recipe_button);
        daySpinner = findViewById(R.id.day_spinner);
        recipeNameInput = findViewById(R.id.recipe_name_input);
        ingredientsInput = findViewById(R.id.ingredients_input);
        instructionsInput = findViewById(R.id.instructions_input);

        // Get the pre-selected day from the intent (if any)
        preSelectedDay = getIntent().getStringExtra("day");

        // Set up the day spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        // Pre-select the day if provided
        if (preSelectedDay != null) {
            String[] days = getResources().getStringArray(R.array.days_of_week);
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(preSelectedDay)) {
                    daySpinner.setSelection(i);
                    break;
                }
            }
        }

        // Initialize pickImageLauncher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            mealImage = Bitmap.createScaledBitmap(originalBitmap, 300, 300, true);
                            recipeImageView.setImageBitmap(mealImage);
                            recipeImageView.setVisibility(View.VISIBLE);
                            pickImageButton.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        pickImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        saveRecipeButton.setOnClickListener(v -> {
            String name = recipeNameInput.getText().toString().trim();
            String ingredients = ingredientsInput.getText().toString().trim();
            String instructions = instructionsInput.getText().toString().trim();
            String day = daySpinner.getSelectedItem() != null ? daySpinner.getSelectedItem().toString() : "";
            if (!name.isEmpty() && !ingredients.isEmpty() && !instructions.isEmpty()) {
                new SaveMealTask(name, ingredients, instructions, mealImage, day).execute();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private class SaveMealTask extends AsyncTask<Void, Void, Boolean> {
        private final String name;
        private final String ingredients;
        private final String instructions;
        private final Bitmap image;
        private final String day;

        SaveMealTask(String name, String ingredients, String instructions, Bitmap image, String day) {
            this.name = name;
            this.ingredients = ingredients;
            this.instructions = instructions;
            this.image = image;
            this.day = day;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                DataManager dataManager = new DataManager(AddActivity.this);
                dataManager.addMeal(name, ingredients, instructions, image, day);
                // Add the meal to the meal plan for the selected day
                dataManager.addToMealPlan(name, day);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AddActivity.this, "Meal added successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Return to the previous activity (DayDetailActivity or MainActivity)
            } else {
                Toast.makeText(AddActivity.this, "Failed to add meal", Toast.LENGTH_SHORT).show();
            }
        }
    }
}