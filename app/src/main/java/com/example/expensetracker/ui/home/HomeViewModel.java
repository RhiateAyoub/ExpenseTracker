package com.example.expensetracker.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.repository.BudgetRepository;
import com.example.expensetracker.data.repository.ExpenseRepository;
import com.example.expensetracker.utils.BudgetCalculator;

import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    // A single LiveData to hold all the necessary data for the home screen
    private final MutableLiveData<HomeData> homeData = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
        budgetRepository = new BudgetRepository(application);
    }

    // Public getter for the fragment to observe
    public LiveData<HomeData> getHomeData() {
        return homeData;
    }

    /**
     * Loads all data required for the home screen in a background thread.
     * @param userId The ID of the logged-in user.
     */
    public void loadHomeData(int userId) {
        new Thread(() -> {
            // 1. Get current month's budget and total expenses
            double monthlyBudget = budgetRepository.getCurrentMonthBudgetAmount(userId);
            double currentMonthExpenses = expenseRepository.getCurrentMonthTotal(userId);

            // 2. Get the most expensive category for the current month
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            List<ExpenseDao.CategoryTotal> categoryTotals = expenseRepository.getCategoryTotalsForMonth(userId, year, month);

            ExpenseDao.CategoryTotal mostExpensiveCategory = null;
            if (categoryTotals != null && !categoryTotals.isEmpty()) {
                mostExpensiveCategory = categoryTotals.get(0); // The list is sorted by total descending
            }

            // 3. Perform predictive calculations using BudgetCalculator
            double predictedBalance = BudgetCalculator.calculatePredictedBalance(currentMonthExpenses, monthlyBudget);

            // 4. Calculate all necessary percentages
            double expensePercentageOfBudget = BudgetCalculator.calculateBudgetUsagePercentage(currentMonthExpenses, monthlyBudget);
            double predictedBalancePercentage = BudgetCalculator.calculateBudgetUsagePercentage(Math.abs(predictedBalance), monthlyBudget);

            double expensiveCategoryPercentage = 0.0;
            if (mostExpensiveCategory != null && currentMonthExpenses > 0) {
                expensiveCategoryPercentage = (mostExpensiveCategory.total / currentMonthExpenses) * 100;
            }

            // 5. Create the data holder object and post it to LiveData
            HomeData data = new HomeData(
                    monthlyBudget,
                    currentMonthExpenses,
                    expensePercentageOfBudget,
                    mostExpensiveCategory,
                    expensiveCategoryPercentage,
                    predictedBalance,
                    predictedBalancePercentage
            );
            homeData.postValue(data);

        }).start();
    }

    /**
     * A simple data class to hold all calculated values for the home screen.
     * This makes the code cleaner and easier to manage.
     */
    public static class HomeData {
        public final double monthlyBudget;
        public final double currentMonthExpenses;
        public final double expensePercentageOfBudget;
        public final ExpenseDao.CategoryTotal mostExpensiveCategory;
        public final double expensiveCategoryPercentage;
        public final double predictedBalance;
        public final double predictedBalancePercentage;
        public final boolean hasBudget;
        public final boolean isOnTrack;

        public HomeData(double monthlyBudget, double currentMonthExpenses, double expensePercentageOfBudget,
                        ExpenseDao.CategoryTotal mostExpensiveCategory, double expensiveCategoryPercentage,
                        double predictedBalance, double predictedBalancePercentage) {
            this.monthlyBudget = monthlyBudget;
            this.currentMonthExpenses = currentMonthExpenses;
            this.expensePercentageOfBudget = expensePercentageOfBudget;
            this.mostExpensiveCategory = mostExpensiveCategory;
            this.expensiveCategoryPercentage = expensiveCategoryPercentage;
            this.predictedBalance = predictedBalance;
            this.predictedBalancePercentage = predictedBalancePercentage;
            this.hasBudget = monthlyBudget > 0;
            this.isOnTrack = predictedBalance >= 0;
        }
    }
}
