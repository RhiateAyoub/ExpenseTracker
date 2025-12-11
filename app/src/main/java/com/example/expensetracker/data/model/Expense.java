package com.example.expensetracker.data.model;

public class Expense {
    private long id;
    private double amount;
    private String category;
    private long date;
    private String note;

    public Expense() {
    }

    public Expense(long id, double amount, String category, long date, String note) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    // Getters
    public long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public long getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setNote(String note) {
        this.note = note;
    }
}