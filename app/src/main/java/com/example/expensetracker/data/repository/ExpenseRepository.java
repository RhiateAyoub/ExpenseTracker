package com.example.expensetracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.utils.DateUtils;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private final ExpenseDao expenseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
    }

    // ==================== INSERT ====================

    public void insert(Expense expense) {
        executor.execute(() -> expenseDao.insert(expense));
    }

    public void update(Expense expense) {
        executor.execute(() -> expenseDao.update(expense));
    }

    public void delete(Expense expense) {
        executor.execute(() -> expenseDao.delete(expense));
    }

    public LiveData<Expense> getExpenseById(int id) {
        return expenseDao.getExpenseById(id);
    }

    // ==================== GET EXPENSES ====================

    public LiveData<List<Expense>> getAllExpensesForUserLive(int userId) {
        return expenseDao.getAllExpensesForUserLive(userId);
    }

    public LiveData<List<Expense>> getExpensesForMonthLive(
            int userId,
            int year,
            int month
    ) {
        long[] range = getMonthRange(year, month);
        return expenseDao.getExpensesForMonthLive(userId, range[0], range[1]);
    }

    public List<ExpenseDao.CategoryTotal> getCategoryTotalsForMonth(
            int userId,
            int year,
            int month
    ) {
        long[] range = getMonthRange(year, month);
        return expenseDao.getCategoryTotalsForMonth(userId, range[0], range[1]);
    }

    public double getTotalForMonth(int userId, int year, int month) {
        long[] range = getMonthRange(year, month);
        return expenseDao.getTotalExpensesForMonth(userId, range[0], range[1]);
    }

    public double getCurrentMonthTotal(int userId) {
        long startOfMonth = DateUtils.getStartOfCurrentMonth();
        long endOfMonth = DateUtils.getEndOfCurrentMonth();
        return expenseDao.getTotalExpensesForPeriod(userId, startOfMonth, endOfMonth);
    }


    // ==================== DATE UTILS ====================

    private long[] getMonthRange(int year, int month) {
        Calendar cal = Calendar.getInstance();

        cal.set(year, month, 1, 0, 0, 0);
        long start = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long end = cal.getTimeInMillis();

        return new long[]{start, end};
    }
}
