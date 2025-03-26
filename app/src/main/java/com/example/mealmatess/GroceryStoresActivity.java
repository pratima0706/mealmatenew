package com.example.mealmatess;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.adapters.StoreAdapter;
import com.example.mealmatess.models.Store;
import com.example.mealmatess.utils.DataManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;

public class GroceryStoresActivity extends AppCompatActivity implements OnMapReadyCallback, StoreAdapter.OnStoreClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ImageButton backButton, addStoreButton, clearListButton;
    private EditText storeSearchEditText;
    private RecyclerView groceryStoresRecyclerView;
    private StoreAdapter storeAdapter;
    private DataManager dataManager;
    private GoogleMap googleMap;
    private Marker selectedLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_stores);

        backButton = findViewById(R.id.back_button);
        addStoreButton = findViewById(R.id.add_store_button);
        clearListButton = findViewById(R.id.clear_list_button);
        storeSearchEditText = findViewById(R.id.store_search);
        groceryStoresRecyclerView = findViewById(R.id.grocery_stores_list);
        dataManager = new DataManager(this);

        // Set up RecyclerView
        groceryStoresRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateStoreList();

        // Initialize map with permission check
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initializeMap();
        }

        // Back button to navigate to MainActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroceryStoresActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Add store button
        addStoreButton.setOnClickListener(v -> {
            Toast.makeText(this, "Tap on the map to select a location for the new store", Toast.LENGTH_LONG).show();
        });

        // Clear list button
        clearListButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Clear Store List")
                    .setMessage("Are you sure you want to clear all stores? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dataManager.clearStores();
                        updateStoreList();
                        updateMap();
                        Toast.makeText(GroceryStoresActivity.this, "Store list cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Search functionality
        storeSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = storeSearchEditText.getText().toString().trim().toLowerCase();
            filterStores(query);
            return true;
        });
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map fragment not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        updateMap();

        // Enable location if permission granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Set up map click listener to pick a location
        googleMap.setOnMapClickListener(latLng -> {
            // Remove previous marker if exists
            if (selectedLocationMarker != null) {
                selectedLocationMarker.remove();
            }

            // Add a marker at the selected location
            selectedLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
                    .draggable(true));

            // Show dialog to input store name
            showAddStoreDialog(latLng);
        });
    }

    private void showAddStoreDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Store");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter store name");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String storeName = input.getText().toString().trim();
            if (storeName.isEmpty()) {
                Toast.makeText(GroceryStoresActivity.this, "Please enter a store name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save the store
            Store newStore = new Store(storeName, latLng);
            dataManager.addStore(newStore);
            updateStoreList();
            updateMap();

            // Remove the temporary marker
            if (selectedLocationMarker != null) {
                selectedLocationMarker.remove();
                selectedLocationMarker = null;
            }

            Toast.makeText(GroceryStoresActivity.this, "Store added: " + storeName, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            // Remove the temporary marker if canceled
            if (selectedLocationMarker != null) {
                selectedLocationMarker.remove();
                selectedLocationMarker = null;
            }
        });

        builder.show();
    }

    private void updateStoreList() {
        List<Store> stores = dataManager.getStores();
        storeAdapter = new StoreAdapter(this, stores, this);
        groceryStoresRecyclerView.setAdapter(storeAdapter);
    }

    private void updateMap() {
        if (googleMap != null) {
            googleMap.clear();
            List<Store> stores = dataManager.getStores();
            for (Store store : stores) {
                LatLng location = store.getLocation();
                googleMap.addMarker(new MarkerOptions().position(location).title(store.getName()));
            }
            if (!stores.isEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stores.get(0).getLocation(), 10));
            }
        }
    }

    private void filterStores(String query) {
        List<Store> allStores = dataManager.getStores();
        List<Store> filteredStores = new ArrayList<>();
        for (Store store : allStores) {
            if (store.getName().toLowerCase().contains(query)) {
                filteredStores.add(store);
            }
        }
        storeAdapter = new StoreAdapter(this, filteredStores, this);
        groceryStoresRecyclerView.setAdapter(storeAdapter);
        updateMap();
    }

    @Override
    public void onStoreClick(Store store) {
        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(store.getLocation(), 15));
            Toast.makeText(this, "Viewing: " + store.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStoreDelete(Store store) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Store")
                .setMessage("Are you sure you want to delete " + store.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dataManager.deleteStore(store.getName()); // Assuming DataManager has a deleteStore method
                    updateStoreList();
                    updateMap();
                    Toast.makeText(GroceryStoresActivity.this, "Store deleted: " + store.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Location permission denied. Map functionality limited.", Toast.LENGTH_LONG).show();
            }
        }
    }
}