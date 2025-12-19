package com.example.expensetracker.ui.budget;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.repository.BudgetRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new BudgetRepository(application);
    }

    public void saveBudget(Budget budget) {
        repository.saveBudget(budget);
    }

    public interface BudgetCallback {
        void onResult(Budget budget);
    }

    public void loadBudget(int userId, int year, int month, BudgetCallback callback) {
        executor.execute(() -> {
            Budget budget = repository.getBudgetForMonth(userId, year, month);
            callback.onResult(budget);
        });
    }
}
