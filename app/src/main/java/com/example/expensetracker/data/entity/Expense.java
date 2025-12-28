// Expense.java
// Location: app/src/main/java/com/example/expensetracker/data/entity/Expense.java
package com.example.expensetracker.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity class representing the 'expenses' table in the database.
 * Stores all expense records for users.
 *
 * Foreign Key Constraint:
 * - When a user is deleted, all their expenses are also deleted (CASCADE)
 */
@Entity(
        tableName = "expenses",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE  // Delete expenses when user is deleted
        ),
        indices = {
                @Index(value = "user_id"),  // Index for faster user-based queries
                @Index(value = "date"),      // Index for faster date-based queries
                @Index(value = "category")   // Index for faster category-based queries
        }
)
public class Expense {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    /**
     * Foreign key linking to the users table
     * Identifies which user this expense belongs to
     */
    @ColumnInfo(name = "user_id")
    private int userId;

    /**
     * Expense amount in MAD
     * Example: 125.50
     */
    @ColumnInfo(name = "amount")
    private double amount;

    /**
     * Category name
     * Examples: "Transport", "Restauration", "Courses"
     * Should match categories from CategoryHelper
     */
    @ColumnInfo(name = "category")
    private String category;

    /**
     * Date of the expense
     * Stored as timestamp (milliseconds since Unix epoch)
     * Use: Calendar.getInstance().getTimeInMillis()
     */
    @ColumnInfo(name = "date")
    private long date;

    /**
     * Optional note/description
     * Examples: "2x Taxi", "Dinde, Pain, Tomates"
     * Can be null or empty
     */
    @ColumnInfo(name = "note")
    private String note;

    /**
     * When this expense record was created
     * Timestamp in milliseconds
     */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ==================== CONSTRUCTORS ====================

    public Expense() {
    }

    /**
     * Constructor for creating a new expense
     * @param userId - ID of the user who made this expense
     * @param amount - Amount spent
     * @param category - Category name
     * @param date - When the expense occurred
     * @param note - Optional description
     */
    public Expense(int userId, double amount, String category, long date, String note) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.createdAt = System.currentTimeMillis();
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Checks if this expense has a note
     */
    public boolean hasNote() {
        return note != null && !note.trim().isEmpty();
    }

    /**
     * Gets the year of this expense
     */
    public int getYear() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(date);
        return cal.get(java.util.Calendar.YEAR);
    }

    /**
     * Gets the month of this expense (0-11)
     */
    public int getMonth() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(date);
        return cal.get(java.util.Calendar.MONTH);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}