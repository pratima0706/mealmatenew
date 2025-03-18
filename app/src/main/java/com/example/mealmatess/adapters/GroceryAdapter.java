package com.example.mealmatess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmatess.R;
import com.example.mealmatess.models.GroceryItem;
import com.example.mealmatess.utils.DataManager;
import java.util.List;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder> {

    private Context context;
    private List<GroceryItem> groceryItems;
    private OnItemClickListener listener;
    private DataManager dataManager;

    public interface OnItemClickListener {
        void onItemClick(GroceryItem item);
    }

    public GroceryAdapter(Context context, List<GroceryItem> groceryItems, OnItemClickListener listener) {
        this.context = context;
        this.groceryItems = groceryItems;
        this.listener = listener;
        this.dataManager = new DataManager(context);
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grocery, parent, false);
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        GroceryItem item = groceryItems.get(position);
        holder.groceryIngredientTextView.setText(item.getName());
        holder.groceryCategoryTextView.setText(item.getIngredients());
        holder.purchasedCheckBox.setChecked(item.isPurchased());

        holder.purchasedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setPurchased(isChecked);
            dataManager.updateGroceryItem(item);
            Toast.makeText(context, "Ingredient marked as " + (isChecked ? "purchased" : "unpurchased"), Toast.LENGTH_SHORT).show();
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groceryItems.size();
    }

    public static class GroceryViewHolder extends RecyclerView.ViewHolder {
        TextView groceryIngredientTextView, groceryCategoryTextView;
        CheckBox purchasedCheckBox;

        public GroceryViewHolder(@NonNull View itemView) {
            super(itemView);
            groceryIngredientTextView = itemView.findViewById(R.id.grocery_ingredient);
            groceryCategoryTextView = itemView.findViewById(R.id.grocery_category);
            purchasedCheckBox = itemView.findViewById(R.id.purchased_checkbox);
        }
    }
}