package com.example.mealmatess;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.adapters.MealPlanAdapter;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.utils.DataManager;
import java.util.ArrayList;
import java.util.List;

public class DayDetailActivity extends AppCompatActivity {

    private static final String TAG = "DayDetailActivity";
    private TextView dayTitle;
    private RecyclerView mealDetailsRecyclerView;
    private ImageButton backButton;
    private DataManager dataManager;
    private String selectedDay;
    private List<Meal> mealsForDay;
    private MealPlanAdapter mealPlanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Log.d(TAG, "onCreate started");
            setContentView(R.layout.activity_day_detail);

            // Initialize UI components
            dayTitle = findViewById(R.id.day_name);
            mealDetailsRecyclerView = findViewById(R.id.meal_plan_list);
            backButton = findViewById(R.id.back_button);

            // Get the selected day from the intent
            selectedDay = getIntent().getStringExtra("day");
            Log.d(TAG, "Selected day: " + selectedDay);
            if (selectedDay != null) {
                dayTitle.setText(selectedDay);
            } else {
                Log.w(TAG, "No day provided in intent, using default");
                dayTitle.setText("Meal Plan");
                selectedDay = "";
            }

            // Initialize DataManager
            dataManager = new DataManager(this);
            mealsForDay = new ArrayList<>();

            // Set up RecyclerView
            mealDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mealPlanAdapter = new MealPlanAdapter(this, mealsForDay);
            mealDetailsRecyclerView.setAdapter(mealPlanAdapter);

            // Add swipe-to-delete functionality with background icons (no edit)
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                private final Drawable deleteIcon = ContextCompat.getDrawable(DayDetailActivity.this, android.R.drawable.ic_menu_delete);
                private final ColorDrawable deleteBackground = new ColorDrawable(Color.RED);

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    Meal meal = mealPlanAdapter.getMealAt(position);
                    if (direction == ItemTouchHelper.LEFT) {
                        // Remove from weekly plan without deleting the meal
                        dataManager.removeFromMealPlan(meal.getName(), selectedDay);
                        mealsForDay.remove(position);
                        mealPlanAdapter.updateMeals(mealsForDay);
                        Toast.makeText(DayDetailActivity.this, meal.getName() + " removed from " + selectedDay, Toast.LENGTH_SHORT).show();
                        loadMealsForDay();
                    }
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                        float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                    View itemView = viewHolder.itemView;
                    int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                    if (dX < 0) { // Swiping to the left (delete)
                        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                        deleteBackground.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        deleteBackground.draw(c);
                        deleteIcon.draw(c);
                    }
                }
            };
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mealDetailsRecyclerView);

            // Set up listeners
            backButton.setOnClickListener(v -> finish());

            loadMealsForDay();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading meal plan: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadMealsForDay() {
        mealsForDay.clear();
        List<Meal> allMeals = new ArrayList<>(dataManager.getAllMeals().values());
        for (Meal meal : allMeals) {
            if (selectedDay != null && selectedDay.equals(meal.getDay())) {
                mealsForDay.add(meal);
            }
        }
        Log.d(TAG, "Meals for " + selectedDay + ": " + mealsForDay.size());
        updateMealDetails();
    }

    private void updateMealDetails() {
        if (mealsForDay.isEmpty()) {
            Toast.makeText(this, "No meals planned for " + (selectedDay.isEmpty() ? "this day" : selectedDay), Toast.LENGTH_SHORT).show();
        }
        mealPlanAdapter.updateMeals(mealsForDay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMealsForDay();
    }
}