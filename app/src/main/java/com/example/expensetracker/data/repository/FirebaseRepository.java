package com.example.expensetracker.data.repository;

import android.util.Log;

import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.entity.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // ==================== EXPENSE SYNC ====================

    public interface SyncCallback {
        void onSuccess(List<Expense> syncedExpenses);
        void onError(Exception e);
    }

    public void syncExpenses(List<Expense> expenses, SyncCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            callback.onError(new Exception("Utilisateur non connecté"));
            return;
        }

        if (expenses == null || expenses.isEmpty()) {
            callback.onSuccess(expenses);
            return;
        }

        String firebaseUid = user.getUid();
        WriteBatch batch = db.batch();

        for (Expense expense : expenses) {
            // Create a map to store expense data
            Map<String, Object> expenseData = new HashMap<>();
            expenseData.put("amount", expense.getAmount());
            expenseData.put("category", expense.getCategory());
            expenseData.put("date", expense.getDate());
            expenseData.put("note", expense.getNote());
            expenseData.put("createdAt", expense.getCreatedAt());
            expenseData.put("userId", expense.getUserId());

            // Store in: users/{firebaseUid}/expenses/{expenseId}
            batch.set(
                    db.collection("users")
                            .document(firebaseUid)
                            .collection("expenses")
                            .document(String.valueOf(expense.getId())),
                    expenseData
            );
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Expenses synced successfully");
                    callback.onSuccess(expenses);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to sync expenses: " + e.getMessage());
                    callback.onError(e);
                });
    }

    // ==================== BUDGET SYNC ====================

    public interface SyncBudgetCallback {
        void onSuccess(List<Budget> syncedBudgets);
        void onError(Exception e);
    }

    public void syncBudgets(List<Budget> budgets, SyncBudgetCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            callback.onError(new Exception("Utilisateur non connecté"));
            return;
        }

        if (budgets == null || budgets.isEmpty()) {
            callback.onSuccess(budgets);
            return;
        }

        String firebaseUid = user.getUid();
        WriteBatch batch = db.batch();

        for (Budget budget : budgets) {
            // Create a map to store budget data
            Map<String, Object> budgetData = new HashMap<>();
            budgetData.put("amount", budget.getAmount());
            budgetData.put("year", budget.getYear());
            budgetData.put("month", budget.getMonth());
            budgetData.put("createdAt", budget.getCreatedAt());
            budgetData.put("userId", budget.getUserId());

            // Use "year-month" as document ID for uniqueness
            String documentId = budget.getYear() + "-" + budget.getMonth();

            // Store in: users/{firebaseUid}/budgets/{year-month}
            batch.set(
                    db.collection("users")
                            .document(firebaseUid)
                            .collection("budgets")
                            .document(documentId),
                    budgetData
            );
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Budgets synced successfully");
                    callback.onSuccess(budgets);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to sync budgets: " + e.getMessage());
                    callback.onError(e);
                });
    }
}