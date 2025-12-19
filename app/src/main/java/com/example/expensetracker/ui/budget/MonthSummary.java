package com.example.expensetracker.ui.budget;

import java.util.Locale;

/**
 * A data class to hold the summary for a single month, combining
 * the budget and the total expenses for that month.
 */
public class MonthSummary {
    private final int year;
    private final int month;
    private final double expenses;
    private final double budget;

    public MonthSummary(int year, int month, double expenses, double budget) {
        this.year = year;
        this.month = month;
        this.expenses = expenses;
        this.budget = budget;
    }

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
        return budget - expenses;
    }

    public String getMonthName() {
        String[] months = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };
        return months[month];
    }

    public String getMonthYear() {
        return String.format(Locale.FRENCH, "%s %d", getMonthName(), year);
    }
}
