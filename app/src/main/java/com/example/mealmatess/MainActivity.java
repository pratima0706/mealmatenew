package com.example.mealmatess;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.adapters.MealAdapter;
import com.example.mealmatess.models.Meal;
import com.example.mealmatess.models.GroceryItem;
import com.example.mealmatess.receiver.ReminderReceiver;
import com.example.mealmatess.utils.DataManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MealAdapter.OnMealClickListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ImageButton menuIcon;
    private EditText searchEditText;
    private TextView mealSunday, mealMonday, mealTuesday, mealWednesday, mealThursday, mealFriday, mealSaturday, reminderText;
    private RecyclerView mealRecyclerView;
    private MealAdapter mealAdapter;
    private DataManager dataManager;
    private List<Meal> allMeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        menuIcon = findViewById(R.id.menu_icon);
        searchEditText = findViewById(R.id.searchEditText);
        mealSunday = findViewById(R.id.mealSunday);
        mealMonday = findViewById(R.id.mealMonday);
        mealTuesday = findViewById(R.id.mealTuesday);
        mealWednesday = findViewById(R.id.mealWednesday);
        mealThursday = findViewById(R.id.mealThursday);
        mealFriday = findViewById(R.id.mealFriday);
        mealSaturday = findViewById(R.id.mealSaturday);
        reminderText = findViewById(R.id.reminder_text);
        mealRecyclerView = findViewById(R.id.mealGrid);

        // Initialize data manager and load meals
        dataManager = new DataManager(this);
        allMeals = new ArrayList<>(dataManager.getAllMeals().values());

        // Set up RecyclerView for meals
        mealRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mealAdapter = new MealAdapter(this, allMeals, this);
        mealRecyclerView.setAdapter(mealAdapter);

        // Add swipe-to-delete functionality
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Meal meal = allMeals.get(position);
                dataManager.deleteMeal(meal.getName());
                allMeals.remove(position);
                mealAdapter.notifyItemRemoved(position);
                Toast.makeText(MainActivity.this, "Meal deleted: " + meal.getName(), Toast.LENGTH_SHORT).show();
                updateWeeklyMealPlan();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(mealRecyclerView);

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMeals(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Update weekly meal plan
        updateWeeklyMealPlan();

        // Drawer navigation setup
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Bottom navigation setup
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            else if (itemId == R.id.nav_meal_plan) {
                startActivity(new Intent(this, MealPlanActivity.class));
                return true;
            } else if (itemId == R.id.nav_location) {
                startActivity(new Intent(this, GroceryStoresActivity.class));
                return true;
            } else if (itemId == R.id.nav_grocery_list) {
                startActivity(new Intent(this, GroceryListActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorite) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            }
            return false;
        });

        // Menu icon to open drawer
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Weekly meal plan click listeners
        findViewById(R.id.daySunday).setOnClickListener(v -> openDayDetail("Sunday"));
        findViewById(R.id.dayMonday).setOnClickListener(v -> openDayDetail("Monday"));
        findViewById(R.id.dayTuesday).setOnClickListener(v -> openDayDetail("Tuesday"));
        findViewById(R.id.dayWednesday).setOnClickListener(v -> openDayDetail("Wednesday"));
        findViewById(R.id.dayThursday).setOnClickListener(v -> openDayDetail("Thursday"));
        findViewById(R.id.dayFriday).setOnClickListener(v -> openDayDetail("Friday"));
        findViewById(R.id.daySaturday).setOnClickListener(v -> openDayDetail("Saturday"));

        // Quick action buttons
        findViewById(R.id.action_add_recipe).setOnClickListener(v -> startActivity(new Intent(this, AddActivity.class)));
        findViewById(R.id.action_track_price).setOnClickListener(v -> Toast.makeText(this, "Track Price - Upcoming Feature!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.action_delegate_share).setOnClickListener(v -> delegateShoppingList());
        findViewById(R.id.action_delegate_share).setOnLongClickListener(v -> {
            importMeals();
            return true;
        });

        // Set up reminder
        setupReminder();
    }

    private void setupReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

        reminderText.setText("Next Meal Prep: " + calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + " at 12:00 PM");
    }

    private void filterMeals(String query) {
        List<Meal> filteredMeals = new ArrayList<>();
        for (Meal meal : dataManager.getAllMeals().values()) {
            if (meal.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredMeals.add(meal);
            }
        }
        mealAdapter.updateMeals(filteredMeals);
        allMeals = filteredMeals;
    }

    private void updateWeeklyMealPlan() {
        List<Meal> mealPlan = dataManager.getMealPlan();
        if (mealPlan == null) mealPlan = new ArrayList<>();
        mealSunday.setText(mealPlan.stream().filter(m -> "Sunday".equals(m.getDay())).findFirst().map(Meal::getName).orElse("-"));
        mealMonday.setText(mealPlan.stream().filter(m -> "Monday".equals(m.getDay())).findFirst().map(Meal::getName).orElse("-"));
        mealTuesday.setText(mealPlan.stream().filter(m -> "Tuesday".equals(m.getDay())).findFirst().map(Meal::getName).orElse("-"));
        mealWednesday.setText(mealPlan.stream().filter(m -> "Wednesday".equals(m.getDay())).findFirst().map(Meal::getName).orElse("-"));
        mealThursday.setText(mealPlan.stream().filter(m -> "Thursday".equals(m.getDay())).findFirst().map(Meal::getName).orElse("-"));
        mealFriday.setText(mealPlan.stream().filter(m -> "Friday".equals(m.getDay())).findFirst().map(Meal::getName).orElse("-"));
        mealSaturday.setText(mealPlan.stream().filter(m -> "Saturday".equals(m.getDay())).findFirst().map(Meal::getName).orElse("-"));
    }

    private void openDayDetail(String day) {
        Intent intent = new Intent(this, DayDetailActivity.class);
        intent.putExtra("day", day);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_profile) startActivity(new Intent(this, ProfileActivity.class));
        else if (itemId == R.id.menu_grocery_stores) startActivity(new Intent(this, GroceryStoresActivity.class));
        else if (itemId == R.id.menu_settings) startActivity(new Intent(this, SettingsActivity.class));
        else if (itemId == R.id.menu_help) startActivity(new Intent(this, HelpActivity.class));
        else if (itemId == R.id.menu_feedback) startActivity(new Intent(this, FeedbackActivity.class));
        else if (itemId == R.id.menu_request_account_deletion) startActivity(new Intent(this, RequestDeletionActivity.class));
        else if (itemId == R.id.menu_logout) {
            dataManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataManager = new DataManager(this);
        allMeals = new ArrayList<>(dataManager.getAllMeals().values());
        mealAdapter.updateMeals(allMeals);
        updateWeeklyMealPlan();
    }

    @Override
    public void onMealClick(Meal meal) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("mealName", meal.getName());
        startActivity(intent);
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
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void importMeals() {
        List<Meal> importedMeals = new ArrayList<>();
        importedMeals.add(new Meal("Imported Pasta", "Pasta, Tomato Sauce", "Cook pasta, add sauce", null, null, false));
        importedMeals.add(new Meal("Imported Salad", "Lettuce, Dressing", "Mix ingredients", null, null, false));

        for (Meal meal : importedMeals) {
            dataManager.addMeal(meal.getName(), meal.getIngredients(), meal.getInstructions(), meal.getImage(), meal.getDay());
        }
        allMeals = new ArrayList<>(dataManager.getAllMeals().values());
        mealAdapter.updateMeals(allMeals);
        updateWeeklyMealPlan();
        Toast.makeText(this, "Imported 2 meals!", Toast.LENGTH_SHORT).show();
    }
}