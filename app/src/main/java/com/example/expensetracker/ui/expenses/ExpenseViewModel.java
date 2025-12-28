package com.example.expensetracker.ui.expenses;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.repository.ExpenseRepository;
import java.util.Calendar;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final MutableLiveData<Calendar> selectedMonth = new MutableLiveData<>();

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        selectedMonth.setValue(Calendar.getInstance()); // Default to current month
    }

    public LiveData<Calendar> getSelectedMonth() {
        return selectedMonth;
    }

    public void changeMonth(int amount) {
        Calendar current = selectedMonth.getValue();
        if (current != null) {
            current.add(Calendar.MONTH, amount);
            selectedMonth.setValue(current);
        }
    }

    public void setMonth(int year, int month) {
        Calendar current = selectedMonth.getValue();
        if (current != null) {
            current.set(Calendar.YEAR, year);
            current.set(Calendar.MONTH, month);
            selectedMonth.setValue(current);
        }
    }

    public LiveData<List<Expense>> getExpensesForMonth(int userId, int year, int month) {
        return repository.getExpensesForMonthLive(userId, year, month);
    }

    public void addExpense(Expense expense) {
        repository.insert(expense);
    }

    public void updateExpense(Expense expense) {
        repository.update(expense);
    }

    public void deleteExpense(Expense expense) {
        repository.delete(expense);
    }

    public LiveData<Expense> getExpenseById(int id) {
        return repository.getExpenseById(id);
    }
}
