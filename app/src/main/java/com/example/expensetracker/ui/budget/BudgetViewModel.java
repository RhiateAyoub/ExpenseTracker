package com.example.expensetracker.ui.budget;

import android.app.Application;import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.repository.BudgetRepository;
import com.example.expensetracker.data.repository.ExpenseRepository;
import java.util.Calendar;
import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    private int userId;
    private Calendar currentMonth;

    private LiveData<Budget> budgetForMonth;
    private LiveData<List<Expense>> expensesForMonth;

    // LiveData exposed to the Fragment
    public MediatorLiveData<Double> totalExpenses = new MediatorLiveData<>();
    public MediatorLiveData<Budget> budget = new MediatorLiveData<>();

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        expenseRepository = new ExpenseRepository(application);
        currentMonth = Calendar.getInstance();
    }

    /**
     * Initializes the ViewModel with the user ID and loads the data for the current month.
     */
    public void loadData(int userId) {
        this.userId = userId;
        int year = currentMonth.get(Calendar.YEAR);
        int month = currentMonth.get(Calendar.MONTH);

        // Get LiveData sources from repositories
        budgetForMonth = budgetRepository.getBudgetForMonthLive(userId, year, month);
        expensesForMonth = expenseRepository.getExpensesForMonthLive(userId, year, month);

        // Observe budget changes
        budget.addSource(budgetForMonth, b -> budget.setValue(b));

        // Observe expense changes and calculate the total
        totalExpenses.addSource(expensesForMonth, expenses -> {
            double sum = 0;
            if (expenses != null) {
                for (Expense expense : expenses) {
                    sum += expense.getAmount();
                }
            }
            totalExpenses.setValue(sum);
        });
    }

    /**
     * Saves or updates the budget for the current month.
     */
    public void saveBudget(double amount) {
        int year = currentMonth.get(Calendar.YEAR);
        int month = currentMonth.get(Calendar.MONTH);
        // We create a new budget object to pass to the repository
        Budget newBudget = new Budget(userId, amount, year, month, System.currentTimeMillis());
        budgetRepository.saveBudget(newBudget);
    }
}
