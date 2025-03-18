package com.example.mealmatess;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.adapters.GroceryAdapter;
import com.example.mealmatess.models.GroceryItem;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.utils.DataManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroceryListActivity extends AppCompatActivity implements GroceryAdapter.OnItemClickListener {

    private DrawerLayout drawerLayout;
    private ImageButton backButton, addItemButton, delegateButton;
    private RecyclerView groceryListRecyclerView;
    private GroceryAdapter groceryAdapter;
    private DataManager dataManager;
    private Spinner categorySpinner;
    private List<GroceryItem> groceryItems;
    private Map<String, List<GroceryItem>> categorizedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        drawerLayout = findViewById(R.id.drawer_layout);
        backButton = findViewById(R.id.back_button);
        addItemButton = findViewById(R.id.add_item_button);
        delegateButton = findViewById(R.id.delegate_button);
        groceryListRecyclerView = findViewById(R.id.grocery_list);
        categorySpinner = findViewById(R.id.category_spinner);
        dataManager = new DataManager(this);
        groceryItems = new ArrayList<>();
        categorizedItems = new HashMap<>();

        // Set up category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.grocery_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterByCategory(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up RecyclerView
        groceryListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateGroceryListFromMealPlan();

        // Set up listeners
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroceryListActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroceryListActivity.this, AddActivity.class);
            intent.putExtra("isGrocery", true);
            startActivity(intent);
        });

        delegateButton.setOnClickListener(v -> delegateShoppingList());
    }

    private void updateGroceryListFromMealPlan() {
        groceryItems.clear();
        List<Meal> mealPlan = dataManager.getMealPlan();
        Set<String> uniqueIngredients = new HashSet<>();

        // Extract all ingredients from the meal plan
        for (Meal meal : mealPlan) {
            String[] ingredients = meal.getIngredients().split(",\\s*");
            for (String ingredient : ingredients) {
                if (!uniqueIngredients.contains(ingredient.trim())) {
                    uniqueIngredients.add(ingredient.trim());
                    groceryItems.add(new GroceryItem(ingredient.trim(), categorizeIngredient(ingredient.trim()), false));
                }
            }
        }

        // Categorize and update the adapter
        categorizeItems();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        filterByCategory(selectedCategory);
    }

    private String categorizeIngredient(String ingredient) {
        String lowerIngredient = ingredient.toLowerCase().trim();
        if (lowerIngredient.contains("carrot") || lowerIngredient.contains("broccoli") ||
                lowerIngredient.contains("vegetable") || lowerIngredient.contains("veggie")) {
            return "Vegetables";
        } else if (lowerIngredient.contains("grain") || lowerIngredient.contains("rice") ||
                lowerIngredient.contains("quinoa")) {
            return "Grains";
        } else if (lowerIngredient.contains("chicken") || lowerIngredient.contains("beef") ||
                lowerIngredient.contains("pork") || lowerIngredient.contains("egg") ||
                lowerIngredient.contains("bacon") || lowerIngredient.contains("meat")) {
            return "Proteins";
        } else if (lowerIngredient.contains("parmesan") || lowerIngredient.contains("cheese") ||
                lowerIngredient.contains("milk") || lowerIngredient.contains("dairy")) {
            return "Dairy";
        }
        return "Other"; // Default to "Other" for unmatched ingredients
    }

    private void categorizeItems() {
        categorizedItems.clear();
        for (GroceryItem item : groceryItems) {
            String category = item.getIngredients();
            categorizedItems.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
        }
    }

    private void filterByCategory(String category) {
        List<GroceryItem> filteredItems = categorizedItems.getOrDefault(category, new ArrayList<>());
        if (category.equals("All")) {
            filteredItems = new ArrayList<>(groceryItems);
        }
        groceryAdapter = new GroceryAdapter(this, filteredItems, this);
        groceryListRecyclerView.setAdapter(groceryAdapter);
    }

    private void delegateShoppingList() {
        List<GroceryItem> items = dataManager.getGroceryItems();
        StringBuilder list = new StringBuilder();
        for (GroceryItem item : items) {
            if (!item.isPurchased()) {
                list.append(item.getName()).append(": ").append(item.getIngredients()).append("\n");
            }
        }
        String groceryList = list.toString();

        if (groceryList.isEmpty()) {
            Toast.makeText(this, "No unpurchased items in grocery list", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.putExtra("sms_body", "Grocery List (Unpurchased Items):\n" + groceryList);

        try {
            startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No SMS app available. Redirecting to homepage.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GroceryListActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(GroceryItem item) {
        // Toggle purchased status via CheckBox in the adapter
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGroceryListFromMealPlan();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}