package com.example.mealmatess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.R;
import com.example.mealmatess.models.Store;
import com.example.mealmatess.utils.DataManager;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private Context context;
    private List<Store> stores;
    private OnStoreClickListener listener;
    private DataManager dataManager;

    public interface OnStoreClickListener {
        void onStoreClick(Store store);
        void onStoreDelete(Store store); // Added for delete callback
    }

    public StoreAdapter(Context context, List<Store> stores, OnStoreClickListener listener) {
        this.context = context;
        this.stores = stores;
        this.listener = listener;
        this.dataManager = new DataManager(context);
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);
        holder.storeNameTextView.setText(store.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoreClick(store);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoreDelete(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView storeNameTextView;
        ImageButton deleteButton;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeNameTextView = itemView.findViewById(R.id.store_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}