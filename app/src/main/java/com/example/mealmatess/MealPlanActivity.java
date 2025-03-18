package com.example.mealmatess;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

public class MealPlanActivity extends AppCompatActivity {

    private ImageButton backButton, addMealButton;
    private RecyclerView mealPlanRecyclerView;
    private MealPlanAdapter mealPlanAdapter;
    private DataManager dataManager;
    private List<Meal> allMeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        backButton = findViewById(R.id.back_button);
        addMealButton = findViewById(R.id.add_meal_button);
        mealPlanRecyclerView = findViewById(R.id.meal_plan_list);
        dataManager = new DataManager(this);
        allMeals = new ArrayList<>(dataManager.getAllMeals().values());

        // Set up RecyclerView
        mealPlanRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealPlanAdapter = new MealPlanAdapter(this, allMeals);
        mealPlanRecyclerView.setAdapter(mealPlanAdapter);

        // Add swipe-to-delete/edit functionality with background icons
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(MealPlanActivity.this, android.R.drawable.ic_menu_delete);
            private final Drawable editIcon = ContextCompat.getDrawable(MealPlanActivity.this, android.R.drawable.ic_menu_edit);
            private final ColorDrawable deleteBackground = new ColorDrawable(Color.RED);
            private final ColorDrawable editBackground = new ColorDrawable(Color.GREEN);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Meal meal = mealPlanAdapter.getMealAt(position);
                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe left to delete
                    dataManager.deleteMeal(meal.getName());
                    allMeals.remove(position);
                    mealPlanAdapter.updateMeals(allMeals);
                    Toast.makeText(MealPlanActivity.this, "Meal deleted: " + meal.getName(), Toast.LENGTH_SHORT).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe right to edit
                    Intent intent = new Intent(MealPlanActivity.this, AddActivity.class);
                    intent.putExtra("mealName", meal.getName());
                    startActivity(intent);
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

                if (dX > 0) { // Swiping to the right (edit)
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = itemView.getLeft() + iconMargin + editIcon.getIntrinsicWidth();
                    editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    editBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
                    editBackground.draw(c);
                    editIcon.draw(c);
                } else if (dX < 0) { // Swiping to the left (delete)
                    int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    deleteBackground.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteBackground.draw(c);
                    deleteIcon.draw(c);
                }
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mealPlanRecyclerView);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MealPlanActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        addMealButton.setOnClickListener(v -> {
            Intent intent = new Intent(MealPlanActivity.this, AddActivity.class);
            startActivity(intent);
        });

        updateMealList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMealList();
    }

    private void updateMealList() {
        allMeals = new ArrayList<>(dataManager.getAllMeals().values());
        mealPlanAdapter.updateMeals(allMeals);
    }
}