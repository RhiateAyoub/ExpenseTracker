package com.example.expensetracker.ui.expenses;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.repository.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
    }

    public LiveData<List<Expense>> getExpensesForMonth(
            int userId,
            int year,
            int month
    ) {
        return repository.getExpensesForMonthLive(userId, year, month);
    }

    public void addExpense(Expense expense) {
        repository.insert(expense);
    }

    public void deleteExpense(Expense expense) {
        repository.delete(expense);
    }
}
