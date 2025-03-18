package com.example.mealmatess.models;

public class FavoriteItem {
    private String mealName;

    public FavoriteItem(String mealName) {
        this.mealName = mealName;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }
}