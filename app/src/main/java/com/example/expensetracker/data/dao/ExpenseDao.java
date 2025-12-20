// ExpenseDao.java
// Location: app/src/main/java/com/example/expensetracker/data/dao/ExpenseDao.java
package com.example.expensetracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensetracker.data.entity.Expense;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) for Expense entity.
 * This is the most used DAO in your app!
 */
@Dao
public interface ExpenseDao {

    // ==================== INSERT ====================

    /**
     * Insert a new expense
     *
     * @param expense - Expense object to insert
     * @return - ID of the newly inserted expense
     */
    @Insert
    long insert(Expense expense);

    /**
     * Insert multiple expenses at once
     * Useful for importing data
     *
     * @param expenses - List of expenses to insert
     * @return - Array of inserted IDs
     */
    @Insert
    long[] insertAll(List<Expense> expenses);

    // ==================== UPDATE ====================

    /**
     * Update an existing expense
     *
     * @param expense - Expense with updated data
     * @return - Number of rows updated
     */
    @Update
    int update(Expense expense);

    // ==================== DELETE ====================

    /**
     * Delete an expense
     *
     * @param expense - Expense to delete
     */
    @Delete
    void delete(Expense expense);

    /**
     * Delete expense by ID
     *
     * @param expenseId - ID of expense to delete
     */
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    void deleteById(int expenseId);

    // ==================== GET EXPENSES ====================

    /**
     * Get all expenses for a user
     * Ordered by date (most recent first)
     *
     * @param userId - User ID
     * @return - List of all expenses
     */
    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC")
    List<Expense> getAllExpensesForUser(int userId);

    /**
     * Get all expenses for a user as LiveData
     * UI updates automatically when expenses change
     *
     * @param userId - User ID
     * @return - LiveData list of expenses
     */
    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpensesForUserLive(int userId);

    /**
     * Get expenses for a specific month
     * Most commonly used query for Expenses screen!
     *
     * How it works:
     * - startDate: first millisecond of the month
     * - endDate: last millisecond of the month
     * - Query finds expenses where date is between these times
     *
     * @param userId - User ID
     * @param startDate - Start of month timestamp
     * @param endDate - End of month timestamp
     * @return - List of expenses for that month
     *
     * Usage:
     * Calendar cal = Calendar.getInstance();
     * cal.set(Calendar.DAY_OF_MONTH, 1);
     * cal.set(Calendar.HOUR_OF_DAY, 0);
     * cal.set(Calendar.MINUTE, 0);
     * cal.set(Calendar.SECOND, 0);
     * long startDate = cal.getTimeInMillis();
     *
     * cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
     * cal.set(Calendar.HOUR_OF_DAY, 23);
     * cal.set(Calendar.MINUTE, 59);
     * cal.set(Calendar.SECOND, 59);
     * long endDate = cal.getTimeInMillis();
     *
     * List<Expense> expenses = expenseDao.getExpensesForMonth(userId, startDate, endDate);
     */
    @Query("SELECT * FROM expenses WHERE user_id = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    List<Expense> getExpensesForMonth(int userId, long startDate, long endDate);

    /**
     * Get expenses for a month as LiveData
     *
     * @param userId - User ID
     * @param startDate - Start of month
     * @param endDate - End of month
     * @return - LiveData list of expenses
     */
    @Query("SELECT * FROM expenses WHERE user_id = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesForMonthLive(int userId, long startDate, long endDate);

    /**
     * Get expenses by category
     *
     * @param userId - User ID
     * @param category - Category name
     * @return - List of expenses in that category
     */
    @Query("SELECT * FROM expenses WHERE user_id = :userId AND category = :category ORDER BY date DESC")
    List<Expense> getExpensesByCategory(int userId, String category);

    /**
     * Get expense by ID
     *
     * @param expenseId - Expense ID
     * @return - Expense object
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    LiveData<Expense> getExpenseById(int expenseId);

    // ==================== CALCULATIONS ====================

    /**
     * Get total expenses for a user (all time)
     *
     * @param userId - User ID
     * @return - Sum of all expense amounts
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE user_id = :userId")
    double getTotalExpensesForUser(int userId);

    /**
     * Get total expenses for a specific month
     * Used for Budget screen calculations!
     *
     * @param userId - User ID
     * @param startDate - Start of month
     * @param endDate - End of month
     * @return - Sum of expenses for that month
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE user_id = :userId AND date >= :startDate AND date <= :endDate")
    double getTotalExpensesForMonth(int userId, long startDate, long endDate);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM expenses WHERE user_id = :userId AND date BETWEEN :start AND :end")
    double getTotalExpensesForPeriod(int userId, long start, long end);

    /**
     * Get total expenses by category for a month
     * Used for Statistics screen!
     *
     * Returns a map: {"Transport" -> 617.0, "Restauration" -> 333.0, ...}
     *
     * @param userId - User ID
     * @param startDate - Start of month
     * @param endDate - End of month
     * @return - Map of category names to total amounts
     */

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE user_id = :userId AND date >= :startDate AND date <= :endDate GROUP BY category ORDER BY total DESC")
    List<CategoryTotal> getCategoryTotalsForMonth(int userId, long startDate, long endDate);

    /**
     * Get expense count for a user
     *
     * @param userId - User ID
     * @return - Number of expenses
     */
    @Query("SELECT COUNT(*) FROM expenses WHERE user_id = :userId")
    int getExpenseCountForUser(int userId);

    /**
     * Get expense count for a month
     *
     * @param userId - User ID
     * @param startDate - Start of month
     * @param endDate - End of month
     * @return - Number of expenses in that month
     */
    @Query("SELECT COUNT(*) FROM expenses WHERE user_id = :userId AND date >= :startDate AND date <= :endDate")
    int getExpenseCountForMonth(int userId, long startDate, long endDate);

    /**
     * Get distinct categories used by a user
     *
     * @param userId - User ID
     * @return - List of category names
     */
    @Query("SELECT DISTINCT category FROM expenses WHERE user_id = :userId ORDER BY category ASC")
    List<String> getDistinctCategories(int userId);

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete all expenses for a user
     *
     * @param userId - User ID
     */
    @Query("DELETE FROM expenses WHERE user_id = :userId")
    void deleteAllExpensesForUser(int userId);

    /**
     * Delete expenses for a specific month
     *
     * @param userId - User ID
     * @param startDate - Start of month
     * @param endDate - End of month
     */
    @Query("DELETE FROM expenses WHERE user_id = :userId AND date >= :startDate AND date <= :endDate")
    void deleteExpensesForMonth(int userId, long startDate, long endDate);

    /**
     * Delete expenses by category
     *
     * @param userId - User ID
     * @param category - Category name
     */
    @Query("DELETE FROM expenses WHERE user_id = :userId AND category = :category")
    void deleteExpensesByCategory(int userId, String category);

    // ==================== HELPER CLASS ====================

    /**
     * Helper class for category totals query result
     * Used by getCategoryTotalsForMonth()
     */
    class CategoryTotal {
        public String category;
        public double total;

        public CategoryTotal(String category, double total) {
            this.category = category;
            this.total = total;
        }
    }
}