package com.example.mealmatess.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.AddActivity;
import com.example.mealmatess.R;
import com.example.mealmatess.RecipeDetailActivity;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.utils.DataManager;
import java.util.ArrayList;
import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.MealViewHolder> {

    private Context context;
    private List<Meal> meals;
    private DataManager dataManager;
    private static final float SWIPE_THRESHOLD = 100;
    private static final float SWIPE_VELOCITY_THRESHOLD = 100;
    private static final float MAX_SWIPE_OFFSET = 200;

    public MealPlanAdapter(Context context, List<Meal> meals) {
        this.context = context;
        this.meals = new ArrayList<>(meals);
        this.dataManager = new DataManager(context);
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_plan, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.mealNameText.setText(meal.getName());
        holder.dayTitle.setText(meal.getDay() != null ? meal.getDay() : "No Day");

        // Load meal image directly from Bitmap
        if (meal.getImage() != null) {
            holder.mealImage.setImageBitmap(meal.getImage());
        } else {
            holder.mealImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Add click listener to navigate to RecipeDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("mealName", meal.getName());
            context.startActivity(intent);
        });

        // Set up swipe gestures
        setupSwipeGesture(holder, meal);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public Meal getMealAt(int position) {
        return meals.get(position);
    }

    private void setupSwipeGesture(MealViewHolder holder, Meal meal) {
        GestureDetectorCompat gestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            private float translationX = 0;
            private boolean isSwiping = false;

            @Override
            public boolean onDown(MotionEvent e) {
                holder.itemView.setTranslationX(0);
                holder.itemView.setBackgroundResource(R.drawable.grid_item_background);
                translationX = 0;
                isSwiping = false;
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                translationX -= distanceX;
                if (translationX > MAX_SWIPE_OFFSET) translationX = MAX_SWIPE_OFFSET;
                if (translationX < -MAX_SWIPE_OFFSET) translationX = -MAX_SWIPE_OFFSET;

                holder.itemView.setTranslationX(translationX);

                if (translationX > 0) {
                    holder.itemView.setBackgroundColor(Color.argb((int) (255 * (translationX / MAX_SWIPE_OFFSET)), 0, 255, 0)); // Green for edit
                } else if (translationX < 0) {
                    holder.itemView.setBackgroundColor(Color.argb((int) (255 * (-translationX / MAX_SWIPE_OFFSET)), 255, 0, 0)); // Red for delete
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.grid_item_background);
                }
                isSwiping = true;
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    int position = holder.getAdapterPosition();
                    if (position >= 0 && position < meals.size()) {
                        if (diffX > 0) {
                            // Swipe right to edit
                            Intent intent = new Intent(context, AddActivity.class);
                            intent.putExtra("mealName", meal.getName());
                            context.startActivity(intent);
                        } else {
                            // Swipe left to delete
                            dataManager.deleteMeal(meal.getName());
                            meals.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Meal deleted: " + meal.getName(), Toast.LENGTH_SHORT).show();
                        }
                        holder.itemView.setTranslationX(0);
                        holder.itemView.setBackgroundResource(R.drawable.grid_item_background);
                        return true;
                    }
                }
                // Reset if not completed
                holder.itemView.setTranslationX(0);
                holder.itemView.setBackgroundResource(R.drawable.grid_item_background);
                return false;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Allow click events to pass through if not swiping
                return !isSwiping;
            }
        });

        holder.itemView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (Math.abs(holder.itemView.getTranslationX()) < SWIPE_THRESHOLD) {
                    holder.itemView.setTranslationX(0);
                    holder.itemView.setBackgroundResource(R.drawable.grid_item_background);
                }
            }
            // Return false to allow click events to pass through
            return false;
        });
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView mealNameText, dayTitle;
        ImageView mealImage;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealNameText = itemView.findViewById(R.id.meal_name);
            dayTitle = itemView.findViewById(R.id.dayTitle);
            mealImage = itemView.findViewById(R.id.meal_plan_image);
        }
    }

    public void updateMeals(List<Meal> newMeals) {
        this.meals.clear();
        this.meals.addAll(newMeals);
        notifyDataSetChanged();
    }
}