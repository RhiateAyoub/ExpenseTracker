package com.example.expensetracker.ai;

import android.content.Context;
import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.dao.UserDao;
import com.example.expensetracker.data.dao.BudgetDao;
import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.entity.User;
import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.model.ExpenseSummary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

/**
 * Coordinates AI advice generation
 * Fetches data from database and calls Groq API
 *
 * UPDATED: Uses Budget.amount for current month budget limit
 */
public class AIAdvisorService {
    private final ExpenseDao expenseDao;
    private final UserDao userDao;
    private final BudgetDao budgetDao;
    private final GroqApiClient apiClient;
    private final AdviceCache cache;

    public AIAdvisorService(Context context, String groqApiKey) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.expenseDao = db.expenseDao();
        this.userDao = db.userDao();
        this.budgetDao = db.budgetDao();
        this.apiClient = new GroqApiClient(groqApiKey);
        this.cache = new AdviceCache(context);
    }

    public String generateAdvice(int userId) {
        // Check cache first
        String cachedAdvice = cache.getCachedAdvice(userId);
        if (cachedAdvice != null) {
            return cachedAdvice + "\n\n💡 Tip: This advice was generated recently. Your spending may have changed since then.";
        }

        try {
            // 1. Fetch user data (for validation)
            User user = userDao.getUserById(userId);
            if (user == null) {
                return "Unable to fetch user data. Please try again.";
            }

            // 2. Get current month/year
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1; // 1-12 for query
            int currentMonthCalendar = cal.get(Calendar.MONTH); // 0-11 for Budget
            int currentYear = cal.get(Calendar.YEAR);

            // 3. Get expenses for current month
            List<Expense> monthExpenses = expenseDao.getExpensesByMonth(
                    userId, currentMonth, currentYear);

            // 4. Calculate total and category-wise expenses
            double totalExpenses = 0.0;
            Map<String, Double> categoryExpenses = new HashMap<>();

            for (Expense expense : monthExpenses) {
                totalExpenses += expense.getAmount();

                String category = expense.getCategory();
                categoryExpenses.put(category,
                        categoryExpenses.getOrDefault(category, 0.0) + expense.getAmount());
            }

            // Handle case with no expenses
            if (categoryExpenses.isEmpty()) {
                return "You haven't recorded any expenses this month yet. Start tracking your spending to get personalized financial advice!";
            }

            // 5. Get budget limit for current month
            Budget budget = budgetDao.getBudgetForMonth(userId, currentYear, currentMonthCalendar);
            double budgetLimit = (budget != null) ? budget.getAmount() : 0.0;

            // Warn if no budget is set
            if (budgetLimit == 0.0) {
                return "⚠️ You haven't set a budget for this month yet!\n\n" +
                        "Setting a budget helps you stay on track with your finances. " +
                        "Go to the Budget tab to set your monthly budget, then come back here for personalized advice.";
            }

            // 6. Create summary (monthlyIncome = 0.0 since we don't track it)
            ExpenseSummary summary = new ExpenseSummary(
                    "MAD",
                    0.0,  // No income tracking in your Budget entity
                    totalExpenses,
                    budgetLimit,
                    categoryExpenses
            );

            // 7. Call AI
            String advice = apiClient.getFinancialAdvice(summary);

            // 8. Cache the result
            cache.cacheAdvice(userId, advice);

            return advice;

        } catch (Exception e) {
            e.printStackTrace();
            return getFallbackAdvice();
        }
    }

    private String getFallbackAdvice() {
        return "I'm having trouble analyzing your finances right now. " +
                "Here are some general tips:\n\n" +
                "1. Track every expense to understand your spending patterns.\n" +
                "2. Set a monthly budget and try to stay within it.\n" +
                "3. Review your largest expense categories for potential savings.\n\n" +
                "Please try again in a moment.";
    }

    public void clearCache(int userId) {
        cache.clearCache(userId);
    }
}