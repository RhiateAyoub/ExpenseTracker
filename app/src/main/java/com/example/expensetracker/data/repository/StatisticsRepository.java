package com.example.expensetracker.data.repository;

import android.app.Application;

import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.dao.ExpenseDao.CategoryTotal;
import com.example.expensetracker.ui.statistics.MonthlyStats;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsRepository {

    private final ExpenseDao expenseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StatisticsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface MonthlyStatsCallback {
        void onStatsLoaded(MonthlyStats stats);
    }

    // ==================== DATA FETCHING ====================

    /**
     * Gets all statistics for a given month in a single operation.
     * This includes category totals, the overall total amount, and the expense count.
     */
    public void getMonthlyStats(
            int userId,
            long startDate,
            long endDate,
            MonthlyStatsCallback callback
    ) {
        executor.execute(() -> {
            // Fetch all data points in one background task
            List<CategoryTotal> categoryTotals = expenseDao.getCategoryTotalsForMonth(userId, startDate, endDate);
            double totalExpenses = expenseDao.getTotalExpensesForMonth(userId, startDate, endDate);
            int expenseCount = expenseDao.getExpenseCountForMonth(userId, startDate, endDate);

            // Create a container object for the results
            MonthlyStats monthlyStats = new MonthlyStats(categoryTotals, totalExpenses, expenseCount);

            // Return the result via the callback
            callback.onStatsLoaded(monthlyStats);
        });
    }
}
