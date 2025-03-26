package com.example.mealmatess.models;

import android.graphics.Bitmap;

public class Meal {
    private String name;
    private String ingredients;
    private String instructions;
    private Bitmap image;
    private String day;
    private boolean isFavorite;

    public Meal(String name, String ingredients, String instructions, Bitmap image, String day, boolean isFavorite) {
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.image = image;
        this.day = day;
        this.isFavorite = isFavorite;
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }


}