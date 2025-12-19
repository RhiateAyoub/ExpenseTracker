package com.example.expensetracker.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.expensetracker.data.dao.BudgetDao;
import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.entity.Budget;
import java.util.Calendar;
import java.util.List; // Import List
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
     */
    public LiveData<Budget> getBudgetForMonthLive(int userId, int year, int month) {
        return budgetDao.getBudgetForMonthLive(userId, year, month);
    }

    /**
     * Gets all budgets for a user, ordered by most recent.
     * This will be used by the MonthlyHistoryViewModel.
     */
    public LiveData<List<Budget>> getAllBudgetsForUserLive(int userId) {
        return budgetDao.getAllBudgetsForUserLive(userId);
    }


    // ==================== SAVE / UPDATE BUDGET ====================

    /**
     * Inserts or updates a budget for a given user, year, and month.
     */
    public void saveBudget(Budget budget) {
        executor.execute(() -> {
            Budget existingBudget = budgetDao.getBudgetForMonth(
                    budget.getUserId(),
                    budget.getYear(),
                    budget.getMonth()
            );

            if (existingBudget == null) {
                budgetDao.insert(budget);
            } else {
                existingBudget.setAmount(budget.getAmount());
                budgetDao.update(existingBudget);
            }
        });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Returns the budget amount for the current month for a given user.
     */
    public double getCurrentMonthBudgetAmount(int userId) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        Budget budget = budgetDao.getBudgetForMonth(userId, year, month);
        return (budget != null) ? budget.getAmount() : 0.0;
    }
}
