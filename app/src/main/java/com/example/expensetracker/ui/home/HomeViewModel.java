package com.example.expensetracker.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.data.repository.ExpenseRepository;
import com.example.expensetracker.data.repository.BudgetRepository;

public class HomeViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    private final MutableLiveData<Double> predictedBalance = new MutableLiveData<>();
    private final MutableLiveData<Double> balancePercentage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
        budgetRepository = new BudgetRepository(application);
    }

    public MutableLiveData<Double> getPredictedBalance() {
        return predictedBalance;
    }

    public MutableLiveData<Double> getBalancePercentage() {
        return balancePercentage;
    }

    public void loadHomeData(int userId) {
        new Thread(() -> {
            double monthlyBudget = budgetRepository.getCurrentMonthBudget(userId);
            double expenses = expenseRepository.getCurrentMonthTotal(userId);

            double balance = monthlyBudget - expenses;
            double percentage = monthlyBudget == 0 ? 0 : (expenses / monthlyBudget) * 100;

            predictedBalance.postValue(balance);
            balancePercentage.postValue(percentage);
        }).start();
    }
}
