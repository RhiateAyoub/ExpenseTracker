package com.example.expensetracker.data.repository;

import com.example.expensetracker.data.entity.Budget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.example.expensetracker.data.entity.Expense;
import java.util.List;

public class FirebaseRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public interface SyncCallback {
        void onSuccess(List<Expense> syncedExpenses);
        void onError(Exception e);
    }

    public void syncExpenses(List<Expense> expenses, SyncCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || expenses == null || expenses.isEmpty()) {
            if (user == null) callback.onError(new Exception("Utilisateur non connecté"));
            return;
        }

        // Utiliser un "batch write" pour envoyer toutes les modifications en une seule fois
        WriteBatch batch = db.batch();
        String userId = user.getUid();

        for (Expense expense : expenses) {
            // Crée une nouvelle référence de document dans la sous-collection "expenses" de l'utilisateur
            batch.set(db.collection("users").document(userId).collection("expenses").document(), expense);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(expenses))
                .addOnFailureListener(e -> callback.onError(e));
    }

    // Interface pour le callback des budgets
    public interface SyncBudgetCallback {
        void onSuccess(List<Budget> syncedBudgets);
        void onError(Exception e);
    }

    // NOUVELLE MÉTHODE POUR SYNCHRONISER LES BUDGETS
    public void syncBudgets(List<Budget> budgets, SyncBudgetCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || budgets == null || budgets.isEmpty()) {
            if (user == null) callback.onError(new Exception("Utilisateur non connecté"));
            return;
        }

        com.google.firebase.firestore.WriteBatch batch = db.batch();
        String userId = user.getUid();

        for (Budget budget : budgets) {
            // Le document ID sera "année-mois", ex: "2025-10" pour assurer l'unicité
            String documentId = budget.getYear() + "-" + budget.getMonth();
            batch.set(db.collection("users").document(userId).collection("budgets").document(documentId), budget);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(budgets))
                .addOnFailureListener(e -> callback.onError(e));
    }
}
