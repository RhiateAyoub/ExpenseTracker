// CategoryStatistic.java
package com.example.expensetracker.ui.statistics;

public class CategoryStatistic {
    private String categoryName;
    private double amount;
    private float percentage;
    private int color;
    private int iconResId;

    public CategoryStatistic(String categoryName, double amount, float percentage, int color, int iconResId) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
        this.iconResId = iconResId;
    }

    // Getters
    public String getCategoryName() {
        return categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public float getPercentage() {
        return percentage;
    }

    public int getColor() {
        return color;
    }

    public int getIconResId() {
        return iconResId;
    }

    // Setters
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}