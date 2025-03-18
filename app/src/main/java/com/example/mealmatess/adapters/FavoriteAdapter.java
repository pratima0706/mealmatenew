package com.example.mealmatess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.R;
import com.example.mealmatess.models.Meal;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private Context context;
    private List<Meal> favoriteMeals;
    private OnUnfavoriteListener listener;

    public interface OnUnfavoriteListener {
        void onUnfavorite(Meal meal);
    }

    public FavoriteAdapter(Context context, List<Meal> favoriteMeals, OnUnfavoriteListener listener) {
        this.context = context;
        this.favoriteMeals = favoriteMeals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Meal meal = favoriteMeals.get(position);
        holder.favoriteNameTextView.setText(meal.getName());
        holder.favoriteIngredientsTextView.setText(meal.getIngredients());
        if (meal.getImage() != null) {
            holder.favoriteImageView.setImageBitmap(meal.getImage());
        } else {
            holder.favoriteImageView.setImageResource(R.drawable.ic_placeholder);
        }
        holder.unfavoriteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnfavorite(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteMeals.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView favoriteImageView;
        TextView favoriteNameTextView, favoriteIngredientsTextView;
        ImageButton unfavoriteButton;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            favoriteImageView = itemView.findViewById(R.id.favorite_image);
            favoriteNameTextView = itemView.findViewById(R.id.favorite_name);
            favoriteIngredientsTextView = itemView.findViewById(R.id.favorite_ingredients);
            unfavoriteButton = itemView.findViewById(R.id.unfavorite_button);
        }
    }
}