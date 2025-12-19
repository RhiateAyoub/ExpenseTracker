package com.example.expensetracker.data.repository;

import android.app.Application;

import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.dao.ExpenseDao.CategoryTotal;

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

    // ==================== CATEGORY TOTALS ====================

    public interface CategoryTotalsCallback {
        void onResult(List<CategoryTotal> totals);
    }

    /**
     * Get total expenses grouped by category for a given month
     */
    public void getCategoryTotalsForMonth(
            int userId,
            long startDate,
            long endDate,
            CategoryTotalsCallback callback
    ) {
        executor.execute(() -> {
            List<CategoryTotal> result =
                    expenseDao.getCategoryTotalsForMonth(userId, startDate, endDate);
            callback.onResult(result);
        });
    }

    // ==================== TOTAL AMOUNT ====================

    public interface TotalAmountCallback {
        void onResult(double total);
    }

    /**
     * Get total expenses for a month
     */
    public void getTotalExpensesForMonth(
            int userId,
            long startDate,
            long endDate,
            TotalAmountCallback callback
    ) {
        executor.execute(() -> {
            double total =
                    expenseDao.getTotalExpensesForMonth(userId, startDate, endDate);
            callback.onResult(total);
        });
    }

    // ==================== COUNT ====================

    public interface ExpenseCountCallback {
        void onResult(int count);
    }

    /**
     * Get expense count for a month
     */
    public void getExpenseCountForMonth(
            int userId,
            long startDate,
            long endDate,
            ExpenseCountCallback callback
    ) {
        executor.execute(() -> {
            int count =
                    expenseDao.getExpenseCountForMonth(userId, startDate, endDate);
            callback.onResult(count);
        });
    }
}
