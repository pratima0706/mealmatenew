package com.example.mealmatess.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.R;
import com.example.mealmatess.RecipeDetailActivity;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.utils.DataManager;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private Context context;
    private List<Meal> meals;
    private OnMealClickListener listener;
    private DataManager dataManager;

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
    }

    public MealAdapter(Context context, List<Meal> meals, OnMealClickListener listener) {
        this.context = context;
        this.meals = meals;
        this.listener = listener;
        this.dataManager = new DataManager(context);
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.mealNameTextView.setText(meal.getName());
        if (meal.getImage() != null) {
            holder.mealImageView.setImageBitmap(meal.getImage());
        } else {
            holder.mealImageView.setImageResource(R.drawable.ic_placeholder);
        }

        // Set the favorite button icon based on the meal's favorite status
        holder.favoriteButton.setImageResource(meal.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

        // Handle favorite button click
        holder.favoriteButton.setOnClickListener(v -> {
            boolean wasFavorited = meal.isFavorite();
            dataManager.toggleFavorite(meal.getName());
            boolean isFavorited = dataManager.isFavorite(meal.getName());
            holder.favoriteButton.setImageResource(isFavorited ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            // Show toast based on the action taken
            if (!wasFavorited && isFavorited) {
                Toast.makeText(context, "Meal Favorited", Toast.LENGTH_SHORT).show();
            } else if (wasFavorited && !isFavorited) {
                Toast.makeText(context, "Meal Unfavorited", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public void updateMeals(List<Meal> newMeals) {
        this.meals = newMeals;
        notifyDataSetChanged();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView mealImageView;
        TextView mealNameTextView;
        ImageButton favoriteButton;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealImageView = itemView.findViewById(R.id.meal_image);
            mealNameTextView = itemView.findViewById(R.id.meal_name);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
        }
    }
}