package com.example.mealmatess.models;

import android.graphics.Bitmap;

public class User {
    private String email;
    private String password;
    private String name;
    private Bitmap image; // Added to support profile picture

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.image = null; // Initialize to null
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Added getter and setter for image
    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}