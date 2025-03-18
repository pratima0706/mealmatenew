package com.example.mealmatess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.utils.DataManager;
import com.google.android.material.button.MaterialButton;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView mealNameTextView, mealDayTextView, ingredientsTextView, instructionsTextView;
    private ImageView mealImageView;
    private MaterialButton editButton, deleteButton, favoriteButton, addToMealPlanButton;
    private DataManager dataManager;
    private String mealName;
    private Meal meal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        backButton = findViewById(R.id.back_button);
        mealNameTextView = findViewById(R.id.mealNameDetail);
        mealDayTextView = findViewById(R.id.meal_day);
        ingredientsTextView = findViewById(R.id.ingredientsDetail);
        instructionsTextView = findViewById(R.id.instructionsDetail);
        mealImageView = findViewById(R.id.mealImageDetail);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        addToMealPlanButton = findViewById(R.id.addToMealPlanButton);
        dataManager = new DataManager(this);

        mealName = getIntent().getStringExtra("mealName");
        meal = dataManager.getMeal(mealName);
        if (meal != null) {
            mealNameTextView.setText(meal.getName());
            mealDayTextView.setText("Day: " + (meal.getDay() != null ? meal.getDay() : "Not Assigned"));
            ingredientsTextView.setText(meal.getIngredients());
            instructionsTextView.setText(meal.getInstructions());
            if (meal.getImage() != null) {
                mealImageView.setImageBitmap(meal.getImage());
            }
            favoriteButton.setText(meal.isFavorite() ? "Unfavorite" : "Favorite");
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("mealName", mealName);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> {
            dataManager.deleteMeal(mealName);
            Toast.makeText(this, "Meal deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RecipeDetailActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        favoriteButton.setOnClickListener(v -> {
            if (meal != null && dataManager != null) {
                boolean wasFavorited = meal.isFavorite(); // Check the state before toggling
                dataManager.toggleFavorite(mealName);
                boolean isFavorited = dataManager.isFavorite(mealName);
                favoriteButton.setText(isFavorited ? "Unfavorite" : "Favorite");

                // Show toast based on the action taken
                if (!wasFavorited && isFavorited) {
                    Toast.makeText(this, "Favorite added", Toast.LENGTH_SHORT).show();
                } else if (wasFavorited && !isFavorited) {
                    Toast.makeText(this, "Favorite removed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error: Unable to toggle favorite", Toast.LENGTH_SHORT).show();
            }
        });

        addToMealPlanButton.setOnClickListener(v -> showDaySelectionDialog());
    }

    private void showDaySelectionDialog() {
        final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Day to Add to Plan");

        ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, days);
        listView.setAdapter(adapter);

        builder.setView(listView);

        final AlertDialog dialog = builder.create();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDay = days[position];
            dataManager.addToMealPlan(mealName, selectedDay);
            Toast.makeText(RecipeDetailActivity.this, "Added to " + selectedDay, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            mealDayTextView.setText("Day: " + selectedDay);
        });

        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        meal = dataManager.getMeal(mealName);
        if (meal != null) {
            mealDayTextView.setText("Day: " + (meal.getDay() != null ? meal.getDay() : "Not Assigned"));
        }
    }
}