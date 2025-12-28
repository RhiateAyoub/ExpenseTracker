// BudgetDao.java
// Location: app/src/main/java/com/example/expensetracker/data/dao/BudgetDao.java
package com.example.expensetracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensetracker.data.entity.Budget;

import java.util.List;

/**
 * Data Access Object (DAO) for Budget entity.
 * Handles all budget-related database operations.
 */
@Dao
public interface BudgetDao {

    // ==================== INSERT ====================

    /**
     * Insert a new budget
     * If a budget for this user/month already exists, replace it
     *
     * onConflict = REPLACE means:
     * - If budget exists for this user + year + month → update it
     * - Otherwise → insert new budget
     *
     * @param budget - Budget object to insert
     * @return - ID of inserted/updated budget
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Budget budget);

    // ==================== UPDATE ====================

    /**
     * Update an existing budget
     *
     * @param budget - Budget with updated data
     * @return - Number of rows updated
     */
    @Update
    int update(Budget budget);

    // ==================== DELETE ====================

    /**
     * Delete a budget
     *
     * @param budget - Budget to delete
     */
    @Delete
    void delete(Budget budget);

    // ==================== QUERIES ====================

    /**
     * Get budget for a specific user, year, and month
     * Most commonly used query!
     *
     * @param userId - User ID
     * @param year - Year (e.g., 2025)
     * @param month - Month (0-11, Calendar constants)
     * @return - Budget if exists, null otherwise
     *
     * Usage:
     * Calendar cal = Calendar.getInstance();
     * Budget currentBudget = budgetDao.getBudgetForMonth(
     *     userId,
     *     cal.get(Calendar.YEAR),
     *     cal.get(Calendar.MONTH)
     * );
     */
    @Query("SELECT * FROM budgets WHERE user_id = :userId AND year = :year AND month = :month LIMIT 1")
    Budget getBudgetForMonth(int userId, int year, int month);

    /**
     * Get budget for a specific month as LiveData
     * UI automatically updates when budget changes
     *
     * @param userId - User ID
     * @param year - Year
     * @param month - Month
     * @return - LiveData containing budget
     */
    @Query("SELECT * FROM budgets WHERE user_id = :userId AND year = :year AND month = :month LIMIT 1")
    LiveData<Budget> getBudgetForMonthLive(int userId, int year, int month);

    /**
     * Get all budgets for a user
     * Ordered by most recent first
     *
     * @param userId - User ID
     * @return - List of all budgets for this user
     */
    @Query("SELECT * FROM budgets WHERE user_id = :userId ORDER BY year DESC, month DESC")
    List<Budget> getAllBudgetsForUser(int userId);

    /**
     * Get all budgets for a user as LiveData
     * Used for Monthly History screen
     *
     * @param userId - User ID
     * @return - LiveData list of budgets
     */
    @Query("SELECT * FROM budgets WHERE user_id = :userId ORDER BY year DESC, month DESC")
    LiveData<List<Budget>> getAllBudgetsForUserLive(int userId);

    /**
     * Get budgets for a specific year
     *
     * @param userId - User ID
     * @param year - Year to filter by
     * @return - List of budgets for that year
     */
    @Query("SELECT * FROM budgets WHERE user_id = :userId AND year = :year ORDER BY month DESC")
    List<Budget> getBudgetsForYear(int userId, int year);

    @Query("SELECT IFNULL(amount, 0) FROM budgets WHERE user_id = :userId AND month = :monthStart LIMIT 1")
    double getBudgetForMonth(int userId, long monthStart);

    /**
     * Check if budget exists for a specific month
     * Returns 1 if exists, 0 if not
     *
     * @param userId - User ID
     * @param year - Year
     * @param month - Month
     * @return - Count (0 or 1)
     */
    @Query("SELECT COUNT(*) FROM budgets WHERE user_id = :userId AND year = :year AND month = :month")
    int checkBudgetExists(int userId, int year, int month);

    /**
     * Get total budget amount across all months for a user
     *
     * @param userId - User ID
     * @return - Sum of all budget amounts
     */
    @Query("SELECT SUM(amount) FROM budgets WHERE user_id = :userId")
    double getTotalBudgetForUser(int userId);

    /**
     * Delete all budgets for a user
     *
     * @param userId - User ID
     */
    @Query("DELETE FROM budgets WHERE user_id = :userId")
    void deleteAllBudgetsForUser(int userId);

    /**
     * Delete budget for a specific month
     *
     * @param userId - User ID
     * @param year - Year
     * @param month - Month
     */
    @Query("DELETE FROM budgets WHERE user_id = :userId AND year = :year AND month = :month")
    void deleteBudgetForMonth(int userId, int year, int month);

    /**
     * Get count of budgets for a user
     *
     * @param userId - User ID
     * @return - Number of budgets
     */
    @Query("SELECT COUNT(*) FROM budgets WHERE user_id = :userId")
    int getBudgetCountForUser(int userId);
}