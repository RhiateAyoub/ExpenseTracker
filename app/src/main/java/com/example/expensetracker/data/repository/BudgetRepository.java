package com.example.expensetracker.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.expensetracker.data.dao.BudgetDao;
import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.entity.Budget;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {

    private final BudgetDao budgetDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BudgetRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        budgetDao = db.budgetDao();
    }

    // ==================== GET BUDGETS ====================

    /**
     * Gets the budget for a specific month as a LiveData object.
     * This is the primary method for the UI to observe budget changes.
     */
    public LiveData<Budget> getBudgetForMonthLive(int userId, int year, int month) {
        return budgetDao.getBudgetForMonthLive(userId, year, month);
    }

    // ==================== SAVE / UPDATE BUDGET ====================

    /**
     * Inserts or updates a budget for a given user, year, and month.
     * This is the main method for saving changes from the UI.
     */
    public void saveBudget(Budget budget) {
        executor.execute(() -> {
            // Check if a budget for this user and month already exists.
            Budget existingBudget = budgetDao.getBudgetForMonth(
                    budget.getUserId(),
                    budget.getYear(),
                    budget.getMonth()
            );

            if (existingBudget == null) {
                // No budget exists, so insert a new one.
                budgetDao.insert(budget);
            } else {
                // A budget exists, so update its amount.
                existingBudget.setAmount(budget.getAmount());
                budgetDao.update(existingBudget);
            }
        });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Returns the budget amount for the current month for a given user.
     * This is a synchronous call intended for background calculations.
     */
    public double getCurrentMonthBudgetAmount(int userId) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        Budget budget = budgetDao.getBudgetForMonth(userId, year, month);
        return (budget != null) ? budget.getAmount() : 0.0;
    }
}
