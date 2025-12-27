package com.example.expensetracker.workers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.repository.BudgetRepository;
import com.example.expensetracker.data.repository.ExpenseRepository;
import com.example.expensetracker.data.repository.FirebaseRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private final ExpenseRepository expenseRepo;
    private final BudgetRepository budgetRepo;
    private final FirebaseRepository firebaseRepo;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Application app = (Application) getApplicationContext();
        expenseRepo = new ExpenseRepository(app);
        budgetRepo = new BudgetRepository(app);
        firebaseRepo = new FirebaseRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Check if user is authenticated with Firebase
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Log.w(TAG, "No Firebase user signed in, skipping sync");
            return Result.success(); // Not a failure, just skip
        }

        try {
            syncExpenses();
            syncBudgets();
            Log.d(TAG, "Sync completed successfully");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Sync failed: " + e.getMessage());
            return Result.retry();
        }
    }

    private void syncExpenses() {
        List<Expense> unsyncedExpenses = expenseRepo.getUnsyncedExpenses();

        if (unsyncedExpenses != null && !unsyncedExpenses.isEmpty()) {
            Log.d(TAG, "Syncing " + unsyncedExpenses.size() + " expenses");

            firebaseRepo.syncExpenses(unsyncedExpenses, new FirebaseRepository.SyncCallback() {
                @Override
                public void onSuccess(List<Expense> syncedExpenses) {
                    expenseRepo.markExpensesAsSynced(syncedExpenses);
                    Log.d(TAG, "Expenses synced successfully");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to sync expenses: " + e.getMessage());
                }
            });
        } else {
            Log.d(TAG, "No unsynced expenses");
        }
    }

    private void syncBudgets() {
        List<Budget> unsyncedBudgets = budgetRepo.getUnsyncedBudgets();

        if (unsyncedBudgets != null && !unsyncedBudgets.isEmpty()) {
            Log.d(TAG, "Syncing " + unsyncedBudgets.size() + " budgets");

            firebaseRepo.syncBudgets(unsyncedBudgets, new FirebaseRepository.SyncBudgetCallback() {
                @Override
                public void onSuccess(List<Budget> syncedBudgets) {
                    budgetRepo.markBudgetsAsSynced(syncedBudgets);
                    Log.d(TAG, "Budgets synced successfully");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to sync budgets: " + e.getMessage());
                }
            });
        } else {
            Log.d(TAG, "No unsynced budgets");
        }
    }
}