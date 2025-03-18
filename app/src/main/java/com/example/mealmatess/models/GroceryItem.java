package com.example.mealmatess.models;

public class GroceryItem {
    private String name;
    private String ingredients;
    private boolean purchased;

    public GroceryItem(String name, String ingredients, boolean purchased) {
        this.name = name;
        this.ingredients = ingredients;
        this.purchased = purchased;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
}