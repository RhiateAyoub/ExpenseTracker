package com.example.expensetracker.ui.budget;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

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
    private final MutableLiveData<Calendar> selectedMonth = new MutableLiveData<>();

    private LiveData<Budget> budgetForMonth;
    private LiveData<List<Expense>> expensesForMonth;

    public MediatorLiveData<Double> totalExpenses = new MediatorLiveData<>();
    public MediatorLiveData<Budget> budget = new MediatorLiveData<>();

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        expenseRepository = new ExpenseRepository(application);
        selectedMonth.setValue(Calendar.getInstance()); // Default to current month
    }

    public LiveData<Calendar> getSelectedMonth() {
        return selectedMonth;
    }

    public void init(int userId) {
        this.userId = userId;
        // Observe changes in selectedMonth to reload data
        selectedMonth.observeForever(this::loadDataForMonth);
        loadDataForMonth(selectedMonth.getValue()); // Initial load
    }

    private void loadDataForMonth(Calendar calendar) {
        if (calendar == null) return;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Remove previous sources to avoid memory leaks and unwanted updates
        if (budgetForMonth != null) {
            budget.removeSource(budgetForMonth);
        }
        if (expensesForMonth != null) {
            totalExpenses.removeSource(expensesForMonth);
        }

        // Get new LiveData sources from repositories for the selected month
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

    public void saveBudget(double amount) {
        Calendar calendar = selectedMonth.getValue();
        if (calendar == null) return;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        Budget newBudget = new Budget(userId, amount, year, month, System.currentTimeMillis());
        budgetRepository.saveBudget(newBudget);
    }

    public void changeMonth(int amount) {
        Calendar current = selectedMonth.getValue();
        if (current != null) {
            Calendar newDate = (Calendar) current.clone();
            newDate.add(Calendar.MONTH, amount);
            selectedMonth.setValue(newDate);
        }
    }

    public void setMonth(int year, int month) {
        Calendar current = selectedMonth.getValue();
        if (current != null) {
            Calendar newDate = (Calendar) current.clone();
            newDate.set(Calendar.YEAR, year);
            newDate.set(Calendar.MONTH, month);
            selectedMonth.setValue(newDate);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        selectedMonth.removeObserver(this::loadDataForMonth);
    }
}
