package com.example.mealmatess;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.adapters.FavoriteAdapter;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.utils.DataManager;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnUnfavoriteListener {

    private ImageButton backButton;
    private RecyclerView favoritesRecyclerView;
    private FavoriteAdapter favoriteAdapter;
    private DataManager dataManager;
    private List<Meal> favoriteMeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        backButton = findViewById(R.id.back_button);
        favoritesRecyclerView = findViewById(R.id.favorites_list);
        dataManager = new DataManager(this);
        favoriteMeals = new ArrayList<>();

        // Set up RecyclerView
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteAdapter = new FavoriteAdapter(this, favoriteMeals, this);
        favoritesRecyclerView.setAdapter(favoriteAdapter);

        backButton.setOnClickListener(v -> finish());

        updateFavoritesList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFavoritesList();
    }

    private void updateFavoritesList() {
        favoriteMeals.clear();
        List<String> favoriteNames = dataManager.getFavorites();
        for (String name : favoriteNames) {
            Meal meal = dataManager.getMeal(name);
            if (meal != null) {
                favoriteMeals.add(meal);
            }
        }
        favoriteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUnfavorite(Meal meal) {
        dataManager.toggleFavorite(meal.getName());
        Toast.makeText(this, "Meal unfavorited: " + meal.getName(), Toast.LENGTH_SHORT).show();
        updateFavoritesList();
        Intent intent = new Intent(this, FavoriteActivity.class); // Navigate back to FavoriteActivity
        startActivity(intent);
        finish(); // Close and reopen to refresh
    }
}