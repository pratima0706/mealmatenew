package com.example.mealmatess.models;

public class MealPlanItem {
    private String day;
    private String mealName;

    public MealPlanItem(String day, String mealName) {
        this.day = day;
        this.mealName = mealName;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }
}