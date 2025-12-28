package com.example.expensetracker.ui.statistics;import com.example.expensetracker.data.dao.ExpenseDao;
import java.util.List;

/**
 * A data class to hold all statistics for a single month.
 * This simplifies passing data from the Repository to the ViewModel.
 */
public class MonthlyStats {
    public final List<ExpenseDao.CategoryTotal> categoryTotals;
    public final double totalExpenses;
    public final int expenseCount;

    public MonthlyStats(List<ExpenseDao.CategoryTotal> categoryTotals, double totalExpenses, int expenseCount) {
        this.categoryTotals = categoryTotals;
        this.totalExpenses = totalExpenses;
        this.expenseCount = expenseCount;
    }
}
