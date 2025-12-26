package com.example.expensetracker.workers;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.repository.ExpenseRepository; // Votre repo existant
import com.example.expensetracker.data.repository.FirebaseRepository; // Un nouveau repo pour Firebase
import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.repository.BudgetRepository;

import java.util.List;

public class SyncWorker extends Worker {

    private final ExpenseRepository expenseRepo;
    private final BudgetRepository budgetRepo;
    private final FirebaseRepository remoteRepo;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // On initialise les deux repositories
        Application app = (Application) getApplicationContext();
        expenseRepo = new ExpenseRepository(app);
        budgetRepo = new BudgetRepository(app);
        remoteRepo = new FirebaseRepository(); // On va créer ce repo
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            syncExpenses();
            syncBudgets();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void syncExpenses() {
        List<Expense> unsyncedExpenses = expenseRepo.getUnsyncedExpenses();
        if (unsyncedExpenses != null && !unsyncedExpenses.isEmpty()) {
            remoteRepo.syncExpenses(unsyncedExpenses, new FirebaseRepository.SyncCallback() {
                @Override
                public void onSuccess(List<Expense> syncedExpenses) {
                    expenseRepo.markExpensesAsSynced(syncedExpenses);
                }
                @Override
                public void onError(Exception e) { /* WorkManager réessayera */ }
            });
        }
    }

    private void syncBudgets() {
        List<Budget> unsyncedBudgets = budgetRepo.getUnsyncedBudgets();
        if (unsyncedBudgets != null && !unsyncedBudgets.isEmpty()) {
            remoteRepo.syncBudgets(unsyncedBudgets, new FirebaseRepository.SyncBudgetCallback() {
                @Override
                public void onSuccess(List<Budget> syncedBudgets) {
                    budgetRepo.markBudgetsAsSynced(syncedBudgets);
                }
                @Override
                public void onError(Exception e) { /* WorkManager réessayera */ }
            });
        }
    }
}
