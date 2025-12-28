// Budget.java
// Location: app/src/main/java/com/example/expensetracker/data/entity/Budget.java
package com.example.expensetracker.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity class representing the 'budgets' table in the database.
 * Stores monthly budget limits for each user.
 *
 * Foreign Key Constraint:
 * - When a user is deleted, all their budgets are also deleted (CASCADE)
 *
 * Unique Constraint:
 * - One budget per user per month (enforced by unique index)
 */
@Entity(
        tableName = "budgets",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE  // Delete budgets when user is deleted
        ),
        indices = {
                @Index(value = "user_id"),  // Index for faster queries
                @Index(value = {"user_id", "year", "month"}, unique = true)  // One budget per user per month
        }
)
public class Budget {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    /**
     * Foreign key linking to the users table
     * Identifies which user this budget belongs to
     */
    @ColumnInfo(name = "user_id")
    private int userId;

    /**
     * Budget amount in MAD
     * Example: 1000.0
     */
    @ColumnInfo(name = "amount")
    private double amount;

    /**
     * Year of the budget
     * Example: 2025
     */
    @ColumnInfo(name = "year")
    private int year;

    /**
     * Month of the budget (0-11)
     * Uses Calendar constants:
     * - Calendar.JANUARY = 0
     * - Calendar.FEBRUARY = 1
     * - ...
     * - Calendar.DECEMBER = 11
     */
    @ColumnInfo(name = "month")
    private int month;

    /**
     * When this budget was created
     * Timestamp in milliseconds
     */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ==================== CONSTRUCTORS ====================

    public Budget() {
    }

    public Budget(int userId, double amount, int year, int month, long createdAt) {
        this.userId = userId;
        this.amount = amount;
        this.year = year;
        this.month = month;
        this.createdAt = createdAt;
    }

    // ==================== GETTERS AND SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Returns the month name in French
     */
    public String getMonthName() {
        String[] months = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };
        return months[month];
    }

    /**
     * Returns formatted string like "Novembre 2025"
     */
    public String getMonthYear() {
        return getMonthName() + " " + year;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", year=" + year +
                ", month=" + month +
                " (" + getMonthName() + ")" +
                ", createdAt=" + createdAt +
                '}';
    }
}