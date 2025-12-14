// MonthSummary.java
package com.example.expensetracker.ui.budget;

public class MonthSummary {
    private int year;
    private int month; // 0-11 (Calendar.JANUARY to Calendar.DECEMBER)
    private double expenses;
    private double budget;
    private double balance;

    public MonthSummary(int year, int month, double expenses, double budget) {
        this.year = year;
        this.month = month;
        this.expenses = expenses;
        this.budget = budget;
        this.balance = budget - expenses;
    }

    // Getters
    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getBudget() {
        return budget;
    }

    public double getBalance() {
        return balance;
    }

    // Setters
    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setExpenses(double expenses) {
        this.expenses = expenses;
        this.balance = this.budget - expenses;
    }

    public void setBudget(double budget) {
        this.budget = budget;
        this.balance = budget - this.expenses;
    }

    // Helper methods
    public boolean isPositive() {
        return balance >= 0;
    }

    public String getMonthName() {
        String[] months = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };
        return months[month];
    }

    public String getMonthYear() {
        return getMonthName() + " " + year;
    }
}