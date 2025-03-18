package com.example.mealmatess.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.mealmatess.models.FavoriteItem;
import com.example.mealmatess.models.GroceryItem;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.models.MealPlanItem;
import com.example.mealmatess.models.Store;
import com.example.mealmatess.models.User;
import com.google.android.gms.maps.model.LatLng;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MealMate.db";
    private static final int DATABASE_VERSION = 4; // Incremented for image column in users table

    private static final String TABLE_MEALS = "meals";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_GROCERY = "grocery";
    private static final String TABLE_MEAL_PLAN = "meal_plan";
    private static final String TABLE_STORES = "stores";
    private static final String TABLE_USERS = "users";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_INGREDIENTS = "ingredients";
    private static final String COLUMN_INSTRUCTIONS = "instructions";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_FAVORITE = "isFavorite";
    private static final String COLUMN_MEAL_NAME = "meal_name";
    private static final String COLUMN_PURCHASED = "purchased";
    private static final String COLUMN_DAY_NAME = "day";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEALS_TABLE = "CREATE TABLE " + TABLE_MEALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT UNIQUE,"
                + COLUMN_INGREDIENTS + " TEXT,"
                + COLUMN_INSTRUCTIONS + " TEXT,"
                + COLUMN_IMAGE + " BLOB,"
                + COLUMN_DAY + " TEXT,"
                + COLUMN_FAVORITE + " INTEGER)";
        db.execSQL(CREATE_MEALS_TABLE);

        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEAL_NAME + " TEXT)";
        db.execSQL(CREATE_FAVORITES_TABLE);

        String CREATE_GROCERY_TABLE = "CREATE TABLE " + TABLE_GROCERY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_INGREDIENTS + " TEXT,"
                + COLUMN_PURCHASED + " INTEGER)";
        db.execSQL(CREATE_GROCERY_TABLE);

        String CREATE_MEAL_PLAN_TABLE = "CREATE TABLE " + TABLE_MEAL_PLAN + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DAY_NAME + " TEXT,"
                + COLUMN_MEAL_NAME + " TEXT)";
        db.execSQL(CREATE_MEAL_PLAN_TABLE);

        String CREATE_STORES_TABLE = "CREATE TABLE " + TABLE_STORES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(CREATE_STORES_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_IMAGE + " BLOB)"; // Added image column
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_PLAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Meals CRUD
    public long addMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_INGREDIENTS, meal.getIngredients());
        values.put(COLUMN_INSTRUCTIONS, meal.getInstructions());
        if (meal.getImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            meal.getImage().compress(Bitmap.CompressFormat.JPEG, 30, stream);
            values.put(COLUMN_IMAGE, stream.toByteArray());
        }
        values.put(COLUMN_DAY, meal.getDay());
        values.put(COLUMN_FAVORITE, meal.isFavorite() ? 1 : 0);
        long newRowId = db.insert(TABLE_MEALS, null, values);
        db.close();
        return newRowId;
    }

    public List<Meal> getAllMeals() {
        List<Meal> meals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_NAME, COLUMN_INGREDIENTS, COLUMN_INSTRUCTIONS, COLUMN_IMAGE, COLUMN_DAY, COLUMN_FAVORITE};
        Cursor cursor = db.query(TABLE_MEALS, columns, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS));
                byte[] imageBytes = cursor.isNull(cursor.getColumnIndex(COLUMN_IMAGE)) ? null : cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
                Bitmap image = imageBytes != null ? BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length) : null;
                String day = cursor.isNull(cursor.getColumnIndex(COLUMN_DAY)) ? null : cursor.getString(cursor.getColumnIndex(COLUMN_DAY));
                boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1;
                meals.add(new Meal(name, ingredients, instructions, image, day, isFavorite));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return meals;
    }

    public Meal getMeal(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_NAME, COLUMN_INGREDIENTS, COLUMN_INSTRUCTIONS, COLUMN_IMAGE, COLUMN_DAY, COLUMN_FAVORITE};
        Cursor cursor = db.query(TABLE_MEALS, columns, COLUMN_NAME + "=?", new String[]{name}, null, null, null);
        Meal meal = null;
        if (cursor != null && cursor.moveToFirst()) {
            String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
            String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS));
            byte[] imageBytes = cursor.isNull(cursor.getColumnIndex(COLUMN_IMAGE)) ? null : cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
            Bitmap image = imageBytes != null ? BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length) : null;
            String day = cursor.isNull(cursor.getColumnIndex(COLUMN_DAY)) ? null : cursor.getString(cursor.getColumnIndex(COLUMN_DAY));
            boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1;
            meal = new Meal(name, ingredients, instructions, image, day, isFavorite);
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return meal;
    }

    public void updateMeal(String oldName, Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_INGREDIENTS, meal.getIngredients());
        values.put(COLUMN_INSTRUCTIONS, meal.getInstructions());
        if (meal.getImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            meal.getImage().compress(Bitmap.CompressFormat.JPEG, 30, stream);
            values.put(COLUMN_IMAGE, stream.toByteArray());
        }
        values.put(COLUMN_DAY, meal.getDay());
        values.put(COLUMN_FAVORITE, meal.isFavorite() ? 1 : 0);
        db.update(TABLE_MEALS, values, COLUMN_NAME + "=?", new String[]{oldName});
        db.close();
    }

    public void deleteMeal(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEALS, COLUMN_NAME + "=?", new String[]{name});
        db.delete(TABLE_FAVORITES, COLUMN_MEAL_NAME + "=?", new String[]{name});
        db.delete(TABLE_MEAL_PLAN, COLUMN_MEAL_NAME + "=?", new String[]{name});
        db.delete(TABLE_GROCERY, COLUMN_NAME + "=?", new String[]{name});
        db.close();
    }

    // Favorites CRUD
    public void addFavorite(String mealName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEAL_NAME, mealName);
        db.insert(TABLE_FAVORITES, null, values);
        db.close();
    }

    public List<FavoriteItem> getFavorites() {
        List<FavoriteItem> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_MEAL_NAME}, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String mealName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_NAME));
                favorites.add(new FavoriteItem(mealName));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return favorites;
    }

    public void removeFavorite(String mealName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_MEAL_NAME + "=?", new String[]{mealName});
        db.close();
    }

    // Grocery CRUD
    public void addGroceryItem(GroceryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_INGREDIENTS, item.getIngredients());
        values.put(COLUMN_PURCHASED, item.isPurchased() ? 1 : 0);
        db.insert(TABLE_GROCERY, null, values);
        db.close();
    }

    public List<GroceryItem> getGroceryItems() {
        List<GroceryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROCERY, new String[]{COLUMN_NAME, COLUMN_INGREDIENTS, COLUMN_PURCHASED}, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
                boolean purchased = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PURCHASED)) == 1;
                items.add(new GroceryItem(name, ingredients, purchased));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return items;
    }

    public void updateGroceryItem(GroceryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_INGREDIENTS, item.getIngredients());
        values.put(COLUMN_PURCHASED, item.isPurchased() ? 1 : 0);
        db.update(TABLE_GROCERY, values, COLUMN_NAME + "=?", new String[]{item.getName()});
        db.close();
    }

    public void deleteGroceryItem(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROCERY, COLUMN_NAME + "=?", new String[]{name});
        db.close();
    }

    // Meal Plan CRUD
    public void addMealPlanItem(MealPlanItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEAL_PLAN, COLUMN_DAY_NAME + "=?", new String[]{item.getDay()});
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY_NAME, item.getDay());
        values.put(COLUMN_MEAL_NAME, item.getMealName());
        db.insert(TABLE_MEAL_PLAN, null, values);
        db.close();
    }

    public List<MealPlanItem> getMealPlan() {
        List<MealPlanItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEAL_PLAN, new String[]{COLUMN_DAY_NAME, COLUMN_MEAL_NAME}, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_NAME));
                String mealName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_NAME));
                items.add(new MealPlanItem(day, mealName));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return items;
    }

    public void removeMealPlanItem(String mealName, String day) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEAL_PLAN, COLUMN_DAY_NAME + "=? AND " + COLUMN_MEAL_NAME + "=?", new String[]{day, mealName});
        db.close();
    }

    // Stores CRUD
    public void addStore(Store store) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, store.getName());
        values.put(COLUMN_LATITUDE, store.getLocation().latitude);
        values.put(COLUMN_LONGITUDE, store.getLocation().longitude);
        db.insert(TABLE_STORES, null, values);
        db.close();
    }

    public List<Store> getStores() {
        List<Store> stores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STORES, new String[]{COLUMN_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE}, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE));
                stores.add(new Store(name, new LatLng(latitude, longitude)));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return stores;
    }

    public void clearStores() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_STORES);
        db.close();
    }

    public void deleteStore(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STORES, COLUMN_NAME + "=?", new String[]{name});
        db.close();
    }

    // Users CRUD
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_NAME, user.getName());
        if (user.getImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            user.getImage().compress(Bitmap.CompressFormat.JPEG, 30, stream);
            values.put(COLUMN_IMAGE, stream.toByteArray());
        }
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_NAME, COLUMN_IMAGE}, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            byte[] imageBytes = cursor.isNull(cursor.getColumnIndex(COLUMN_IMAGE)) ? null : cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
            Bitmap image = imageBytes != null ? BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length) : null;
            user = new User(email, password, name);
            user.setImage(image);
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return user;
    }

    public void deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_EMAIL + "=?", new String[]{email});
        db.close();
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MEALS);
        db.execSQL("DELETE FROM " + TABLE_FAVORITES);
        db.execSQL("DELETE FROM " + TABLE_GROCERY);
        db.execSQL("DELETE FROM " + TABLE_MEAL_PLAN);
        db.execSQL("DELETE FROM " + TABLE_STORES);
        db.execSQL("DELETE FROM " + TABLE_USERS);
        db.close();
    }

    public void updateUser(String oldEmail, User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_NAME, user.getName());
        if (user.getImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            user.getImage().compress(Bitmap.CompressFormat.JPEG, 30, stream);
            values.put(COLUMN_IMAGE, stream.toByteArray());
        } else {
            values.putNull(COLUMN_IMAGE);
        }
        db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{oldEmail});
        db.close();
    }
}