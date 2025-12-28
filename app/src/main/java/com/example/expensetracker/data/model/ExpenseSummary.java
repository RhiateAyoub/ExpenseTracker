package com.example.expensetracker.data.model;

import java.util.Map;

/**
 * Data Transfer Object for sending expense data to AI
 * NOTE: monthlyIncome will be 0.0 since Budget doesn't track income
 */
public class ExpenseSummary {
    private final String currency;
    private final double monthlyIncome;
    private final double monthlyExpenses;
    private final double budgetLimit;
    private final Map<String, Double> categoryExpenses;

    public ExpenseSummary(String currency,
                          double monthlyIncome,
                          double monthlyExpenses,
                          double budgetLimit,
                          Map<String, Double> categoryExpenses) {
        this.currency = currency;
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpenses = monthlyExpenses;
        this.budgetLimit = budgetLimit;
        this.categoryExpenses = categoryExpenses;
    }

    public String getCurrency() {
        return currency;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public Map<String, Double> getCategoryExpenses() {
        return categoryExpenses;
    }
}