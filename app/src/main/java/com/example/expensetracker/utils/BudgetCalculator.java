// BudgetCalculator.java
// Location: app/src/main/java/com/example/expensetracker/utils/BudgetCalculator.java
package com.example.expensetracker.utils;

import java.util.Calendar;

/**
 * Utility class for budget calculations and predictions.
 *
 * KEY FEATURE: Predicts end-of-month balance based on current spending patterns
 *
 * Algorithm:
 * 1. Calculate average daily expense from start of month to today
 * 2. Multiply by total days in month to predict total monthly expenses
 * 3. Compare with budget to predict final balance
 *
 * Example:
 * - Today is Nov 15 (halfway through month)
 * - Spent 500 MAD in 15 days
 * - Average per day: 500 / 15 = 33.33 MAD
 * - Predicted monthly total: 33.33 * 30 = 1000 MAD
 * - Budget: 1000 MAD
 * - Predicted balance: 1000 - 1000 = 0 MAD
 */
public class BudgetCalculator {

    /**
     * Calculate predicted end-of-month balance
     *
     * @param currentExpenses - Total expenses so far this month
     * @param monthlyBudget - Budget for the month
     * @return - Predicted balance at end of month
     *
     * Usage:
     * double predicted = BudgetCalculator.calculatePredictedBalance(725, 1000);
     * // If negative, user will exceed budget
     */
    public static double calculatePredictedBalance(double currentExpenses, double monthlyBudget) {
        double predictedTotal = calculatePredictedMonthlyExpenses(currentExpenses);
        return monthlyBudget - predictedTotal;
    }

    /**
     * Calculate predicted total expenses for the entire month
     * Based on spending pattern so far
     *
     * @param currentExpenses - Total expenses from day 1 to today
     * @return - Predicted total expenses for full month
     *
     * Algorithm:
     * 1. Get current day of month
     * 2. Calculate average daily expense
     * 3. Multiply by total days in month
     *
     * Example:
     * - Today: November 15
     * - Current expenses: 500 MAD
     * - Days elapsed: 15
     * - Average per day: 500 / 15 = 33.33 MAD
     * - Total days in November: 30
     * - Predicted total: 33.33 * 30 = 1000 MAD
     */
    public static double calculatePredictedMonthlyExpenses(double currentExpenses) {
        Calendar cal = Calendar.getInstance();

        // Get current day of month (1-31)
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);

        // Get total days in this month (28-31)
        int totalDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // If it's the first day, no prediction possible yet
        if (currentDay == 1) {
            return currentExpenses; // Just return what's spent today
        }

        // Calculate average daily expense
        double avgDailyExpense = currentExpenses / currentDay;

        // Predict total for full month
        return avgDailyExpense * totalDaysInMonth;
    }

    /**
     * Calculate average daily expense
     *
     * @param currentExpenses - Total expenses so far
     * @return - Average expense per day
     */
    public static double calculateAverageDailyExpense(double currentExpenses) {
        Calendar cal = Calendar.getInstance();
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);

        if (currentDay == 0) {
            return 0;
        }

        return currentExpenses / currentDay;
    }

    /**
     * Calculate budget usage percentage
     *
     * @param currentExpenses - Current expenses
     * @param monthlyBudget - Monthly budget
     * @return - Percentage used (0-100+)
     *
     * Example:
     * - Expenses: 725 MAD
     * - Budget: 1000 MAD
     * - Returns: 72.5%
     */
    public static double calculateBudgetUsagePercentage(double currentExpenses, double monthlyBudget) {
        if (monthlyBudget == 0) {
            return 0;
        }
        return (currentExpenses / monthlyBudget) * 100;
    }

    /**
     * Calculate predicted budget usage percentage at end of month
     *
     * @param currentExpenses - Current expenses
     * @param monthlyBudget - Monthly budget
     * @return - Predicted percentage at month end
     */
    public static double calculatePredictedBudgetUsage(double currentExpenses, double monthlyBudget) {
        double predictedExpenses = calculatePredictedMonthlyExpenses(currentExpenses);
        if (monthlyBudget == 0) {
            return 0;
        }
        return (predictedExpenses / monthlyBudget) * 100;
    }

    /**
     * Check if user is on track to stay within budget
     *
     * @param currentExpenses - Current expenses
     * @param monthlyBudget - Monthly budget
     * @return - true if within budget, false if will exceed
     *
     * Usage:
     * if (BudgetCalculator.isOnTrack(expenses, budget)) {
     *     // Show positive message
     * } else {
     *     // Show warning message
     * }
     */
    public static boolean isOnTrack(double currentExpenses, double monthlyBudget) {
        double predictedBalance = calculatePredictedBalance(currentExpenses, monthlyBudget);
        return predictedBalance >= 0;
    }

    /**
     * Get days remaining in current month
     *
     * @return - Number of days left
     */
    public static int getDaysRemainingInMonth() {
        Calendar cal = Calendar.getInstance();
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return totalDays - currentDay;
    }

    /**
     * Calculate recommended daily spending to stay within budget
     *
     * @param currentExpenses - Expenses so far
     * @param monthlyBudget - Monthly budget
     * @return - Recommended daily limit for rest of month
     *
     * Example:
     * - Today: Nov 15
     * - Spent: 600 MAD
     * - Budget: 1000 MAD
     * - Remaining budget: 400 MAD
     * - Days left: 15
     * - Recommended: 400 / 15 = 26.67 MAD/day
     */
    public static double calculateRecommendedDailySpending(double currentExpenses, double monthlyBudget) {
        int daysRemaining = getDaysRemainingInMonth();

        if (daysRemaining == 0) {
            return 0;
        }

        double remainingBudget = monthlyBudget - currentExpenses;

        if (remainingBudget < 0) {
            return 0; // Already over budget
        }

        return remainingBudget / daysRemaining;
    }

    /**
     * Format currency amount
     *
     * @param amount - Amount to format
     * @return - Formatted string (e.g., "1,234 MAD")
     */
    public static String formatCurrency(double amount) {
        return String.format(java.util.Locale.FRENCH, "%.0f MAD", amount);
    }

    /**
     * Format percentage
     *
     * @param percentage - Percentage to format
     * @return - Formatted string (e.g., "72%")
     */
    public static String formatPercentage(double percentage) {
        return String.format(java.util.Locale.FRENCH, "%.0f%%", percentage);
    }
}