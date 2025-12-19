package com.example.expensetracker.data.repository;

import android.app.Application;

import com.example.expensetracker.data.dao.BudgetDao;
import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.utils.DateUtils;

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

    // ==================== INSERT / UPDATE ====================

    public void saveBudget(Budget budget) {
        executor.execute(() -> {
            Budget existing = budgetDao.getBudgetForMonth(
                    budget.getUserId(),
                    budget.getYear(),
                    budget.getMonth()
            );

            if (existing == null) {
                budgetDao.insert(budget);
            } else {
                budget.setId(existing.getId());
                budgetDao.update(budget);
            }
        });
    }

    // ==================== GET ====================

    public Budget getBudgetForMonth(int userId, int year, int month) {
        return budgetDao.getBudgetForMonth(userId, year, month);
    }

    public double getCurrentMonthBudget(int userId) {
        long startOfMonth = DateUtils.getStartOfCurrentMonth();
        return budgetDao.getBudgetForMonth(userId, startOfMonth);
    }

}
