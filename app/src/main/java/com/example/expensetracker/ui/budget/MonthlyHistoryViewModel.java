package com.example.expensetracker.ui.budget;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.repository.BudgetRepository;
import com.example.expensetracker.data.repository.ExpenseRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyHistoryViewModel extends AndroidViewModel {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    private LiveData<List<Budget>> allBudgets;
    private LiveData<List<Expense>> allExpenses;

    // The final combined list for the UI to observe
    public MediatorLiveData<List<MonthSummary>> monthlySummaries = new MediatorLiveData<>();

    public MonthlyHistoryViewModel(@NonNull Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        expenseRepository = new ExpenseRepository(application);
    }

    public void loadHistory(int userId) {
        allBudgets = budgetRepository.getAllBudgetsForUserLive(userId);
        allExpenses = expenseRepository.getAllExpensesForUserLive(userId);

        monthlySummaries.addSource(allBudgets, budgets -> combineData(budgets, allExpenses.getValue()));
        monthlySummaries.addSource(allExpenses, expenses -> combineData(allBudgets.getValue(), expenses));
    }

    private void combineData(List<Budget> budgets, List<Expense> expenses) {
        if (budgets == null) {
            return; // Wait for budgets to load
        }

        // Create a map to store total expenses for each month (e.g., "2025-10" -> 1500.0)
        Map<String, Double> expenseMap = new HashMap<>();
        if (expenses != null) {
            for (Expense expense : expenses) {
                String yearMonthKey = expense.getYear() + "-" + expense.getMonth();
                double currentTotal = expenseMap.getOrDefault(yearMonthKey, 0.0);
                expenseMap.put(yearMonthKey, currentTotal + expense.getAmount());
            }
        }

        // Create the final summary list
        List<MonthSummary> summaries = new ArrayList<>();
        for (Budget budget : budgets) {
            String yearMonthKey = budget.getYear() + "-" + budget.getMonth();
            double totalExpensesForMonth = expenseMap.getOrDefault(yearMonthKey, 0.0);
            summaries.add(new MonthSummary(
                    budget.getYear(),
                    budget.getMonth(),
                    totalExpensesForMonth,
                    budget.getAmount()
            ));
        }
        monthlySummaries.setValue(summaries);
    }
}
