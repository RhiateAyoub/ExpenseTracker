package com.example.expensetracker.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.repository.ExpenseRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
    }

    public interface CategoryCallback {
        void onResult(List<ExpenseDao.CategoryTotal> result);
    }

    public void loadCategoryTotals(
            int userId,
            int year,
            int month,
            CategoryCallback callback
    ) {
        executor.execute(() -> {
            List<ExpenseDao.CategoryTotal> result =
                    repository.getCategoryTotalsForMonth(userId, year, month);
            callback.onResult(result);
        });
    }

    public double getTotalForMonth(int userId, int year, int month) {
        return repository.getTotalForMonth(userId, year, month);
    }
}
