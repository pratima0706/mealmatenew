package com.example.mealmatess.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.Toast;

import com.example.mealmatess.R;
import com.example.mealmatess.database.DatabaseHelper;
import com.example.mealmatess.models.FavoriteItem;
import com.example.mealmatess.models.GroceryItem;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.models.MealPlanItem;
import com.example.mealmatess.models.Store;
import com.example.mealmatess.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private static final String SALT = "MealMateFixedSalt123"; // Fixed salt for simplicity
    private final Context context;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private Map<String, Meal> mealsCache;
    private FirebaseAuth mAuth;

    public DataManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE);
        this.dbHelper = new DatabaseHelper(context);
        this.mAuth = FirebaseAuth.getInstance(); // Optional Firebase Auth for Google Sign-In
        this.mealsCache = new HashMap<>();
        loadData();
        addDefaultRecipesIfFirstLaunch();
    }

    private void loadData() {
        mealsCache.clear();
        List<Meal> meals = dbHelper.getAllMeals();
        for (Meal meal : meals) {
            mealsCache.put(meal.getName(), meal);
        }
    }

    private void addDefaultRecipesIfFirstLaunch() {
        SharedPreferences prefs = context.getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("defaultRecipesAdded", false)) return;

        // Default recipes with images
        List<Meal> defaultMeals = new ArrayList<>();
        defaultMeals.add(new Meal("Chicken Stir-Fry", "Chicken, Vegetables, Soy Sauce", "Cook chicken and veggies with soy sauce.",
                BitmapFactory.decodeResource(context.getResources(), R.drawable.chickenstirfry), null, false));
        defaultMeals.add(new Meal("Spaghetti Carbonara", "Spaghetti, Eggs, Bacon, Parmesan", "Cook pasta, mix with egg and bacon.",
                BitmapFactory.decodeResource(context.getResources(), R.drawable.spaghetticarbonara), null, false));
        defaultMeals.add(new Meal("Vegetable Quinoa Bowl", "Quinoa, Broccoli, Carrots", "Cook quinoa and steam veggies.",
                BitmapFactory.decodeResource(context.getResources(), R.drawable.vegquinoa), null, false));

        for (Meal meal : defaultMeals) {
            if (dbHelper.getMeal(meal.getName()) == null) {
                dbHelper.addMeal(meal);
                dbHelper.addGroceryItem(new GroceryItem(meal.getName(), meal.getIngredients(), false));
            }
        }
        prefs.edit().putBoolean("defaultRecipesAdded", true).apply();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = SALT + password;
            byte[] hash = digest.digest(saltedPassword.getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(hash);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (not secure, for testing only)
        }
        return password;
    }

    private boolean verifyPassword(String password, String storedHash) {
        String hashedInput = hashPassword(password);
        return storedHash.equals(hashedInput);
    }

    public boolean checkUser(String email, String password) {
        User user = getUser(email);
        if (user != null) {
            return verifyPassword(password, user.getPassword());
        }
        return false;
    }

    public void registerUser(String name, String email, String password) {
        String hashedPassword = hashPassword(password);
        User user = new User(email, hashedPassword, name);
        dbHelper.addUser(user);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("userName", name);
        editor.apply();
    }

    public Task<Void> sendResetEmail(String email) {
        if (mAuth != null && email != null) {
            return mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Reset email sent to " + email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Offline mode: Password reset not available. Contact support.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void addMeal(String name, String ingredients, String instructions, Bitmap image, String day) {
        Meal meal = new Meal(name, ingredients, instructions, image, day, false);
        long result = dbHelper.addMeal(meal);
        if (result != -1) {
            mealsCache.put(name, meal);
            dbHelper.addGroceryItem(new GroceryItem(name, ingredients, false));
            if (day != null && !day.isEmpty()) {
                addToMealPlan(name, day);
            }
        }
    }

    public void updateMeal(String oldName, String newName, String ingredients, String instructions, Bitmap image, String day) {
        Meal oldMeal = mealsCache.get(oldName);
        if (oldMeal != null) {
            Meal newMeal = new Meal(newName, ingredients, instructions, image, day, oldMeal.isFavorite());
            dbHelper.updateMeal(oldName, newMeal);
            mealsCache.remove(oldName);
            mealsCache.put(newName, newMeal);
            List<GroceryItem> groceryItems = dbHelper.getGroceryItems();
            for (GroceryItem item : groceryItems) {
                if (item.getName().equals(oldName)) {
                    dbHelper.updateGroceryItem(new GroceryItem(newName, ingredients, item.isPurchased()));
                }
            }
            List<MealPlanItem> mealPlanItems = dbHelper.getMealPlan();
            for (MealPlanItem item : mealPlanItems) {
                if (item.getMealName().equals(oldName)) {
                    dbHelper.addMealPlanItem(new MealPlanItem(day, newName));
                }
            }
        }
    }

    public void deleteMeal(String name) {
        dbHelper.deleteMeal(name);
        mealsCache.remove(name);
        dbHelper.deleteGroceryItem(name);
    }

    public Meal getMeal(String name) {
        return dbHelper.getMeal(name);
    }

    public void toggleFavorite(String mealName) {
        Meal meal = mealsCache.get(mealName);
        if (meal != null) {
            meal.setFavorite(!meal.isFavorite());
            dbHelper.updateMeal(mealName, meal);
            if (meal.isFavorite()) {
                dbHelper.addFavorite(mealName);
            } else {
                dbHelper.removeFavorite(mealName);
            }
        }
    }

    public boolean isFavorite(String mealName) {
        Meal meal = mealsCache.get(mealName);
        return meal != null && meal.isFavorite();
    }

    public List<String> getFavorites() {
        List<FavoriteItem> favorites = dbHelper.getFavorites();
        List<String> favoriteNames = new ArrayList<>();
        for (FavoriteItem item : favorites) {
            favoriteNames.add(item.getMealName());
        }
        return favoriteNames;
    }

    public void addToMealPlan(String mealName, String day) {
        dbHelper.addMealPlanItem(new MealPlanItem(day, mealName));
        Meal meal = mealsCache.get(mealName);
        if (meal != null) {
            meal.setDay(day);
            dbHelper.updateMeal(mealName, meal);
        }
    }

    public List<Meal> getMealPlan() {
        List<MealPlanItem> mealPlanItems = dbHelper.getMealPlan();
        List<Meal> mealPlan = new ArrayList<>();
        for (MealPlanItem item : mealPlanItems) {
            Meal meal = dbHelper.getMeal(item.getMealName());
            if (meal != null) {
                meal.setDay(item.getDay());
                mealPlan.add(meal);
            }
        }
        return mealPlan;
    }

    public void removeFromMealPlan(String mealName, String day) {
        dbHelper.removeMealPlanItem(mealName, day);

        // Update the meal's day to null in the meals table
        Meal meal = mealsCache.get(mealName);
        if (meal != null) {
            meal.setDay(null);
            dbHelper.updateMeal(mealName, meal);
            mealsCache.put(mealName, meal);
        }
    }

    public Map<String, Meal> getAllMeals() {
        mealsCache.clear();
        List<Meal> meals = dbHelper.getAllMeals();
        for (Meal meal : meals) {
            mealsCache.put(meal.getName(), meal);
        }
        return new HashMap<>(mealsCache);
    }

    public String getGroceryList() {
        List<GroceryItem> items = dbHelper.getGroceryItems();
        StringBuilder list = new StringBuilder();
        for (GroceryItem item : items) {
            if (!item.isPurchased()) {
                list.append(item.getName()).append(": ").append(item.getIngredients()).append("\n");
            }
        }
        return list.toString();
    }

    public void markAsPurchased() {
        List<GroceryItem> items = dbHelper.getGroceryItems();
        for (GroceryItem item : items) {
            item.setPurchased(true);
            dbHelper.updateGroceryItem(item);
        }
    }

    public List<GroceryItem> getGroceryItems() {
        return dbHelper.getGroceryItems();
    }

    public void updateGroceryItem(GroceryItem item) {
        dbHelper.updateGroceryItem(item);
    }

    public void deleteGroceryItem(String name) {
        dbHelper.deleteGroceryItem(name);
    }

    public List<Store> getStores() {
        return dbHelper.getStores();
    }

    public void addStore(Store store) {
        dbHelper.addStore(store);
    }

    public void clearStores() {
        dbHelper.clearStores();
    }
    public void deleteStore(String name) {
        dbHelper.deleteStore(name);
    }

    public void deleteAccount() {
        String email = sharedPreferences.getString("email", "");
        dbHelper.deleteUser(email);
        dbHelper.clearAllData();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        mealsCache.clear();
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("rememberMe", false);
        editor.apply();
        if (mAuth != null) {
            mAuth.signOut();
        }
    }

    public String getUserName() {
        return sharedPreferences.getString("userName", "Guest");
    }

    public String getUserEmail() {
        return sharedPreferences.getString("email", "");
    }

    public User getUser(String email) {
        return dbHelper.getUser(email);
    }

    public void updateUser(String oldEmail, String newEmail, String password, String name, Bitmap image) {
        User user = new User(newEmail, password, name);
        user.setImage(image);
        dbHelper.updateUser(oldEmail, user);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", newEmail);
        editor.putString("userName", name);
        editor.apply();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}