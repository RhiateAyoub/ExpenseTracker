// AppDatabase.java
// Location: app/src/main/java/com/example/expensetracker/data/database/AppDatabase.java
package com.example.expensetracker.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.expensetracker.data.dao.BudgetDao;
import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.data.dao.UserDao;
import com.example.expensetracker.data.entity.Budget;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.data.entity.User;

/**
 * Main database class for the Expense Tracker app.
 *
 * This is the central access point to your entire database.
 * It follows the Singleton pattern - only ONE instance exists in the entire app.
 *
 * @Database annotation tells Room:
 * - Which entities (tables) to create
 * - Database version number (increment this when you change schema)
 * - exportSchema = false means don't save schema history (fine for development)
 */

@Database(
        entities = {User.class, Budget.class, Expense.class},
        version = 2, // version 2 ICI
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // ==================== DAO ACCESS METHODS ====================
    // Room automatically implements these methods

    /**
     * Get UserDao to perform user-related operations
     */
    public abstract UserDao userDao();

    /**
     * Get BudgetDao to perform budget-related operations
     */
    public abstract BudgetDao budgetDao();

    /**
     * Get ExpenseDao to perform expense-related operations
     */
    public abstract ExpenseDao expenseDao();

    // ==================== SINGLETON PATTERN ====================

    /**
     * Single instance of the database
     * volatile = ensures thread-safety (important for multi-threaded apps)
     */
    private static volatile AppDatabase INSTANCE;

    /**
     * Database name (the actual file on the device)
     */
    private static final String DATABASE_NAME = "expense_tracker.db";

    /**
     * Get the database instance
     * If it doesn't exist, create it
     * If it exists, return the existing instance
     *
     * synchronized = only one thread can create the database at a time
     *
     * Usage:
     * AppDatabase db = AppDatabase.getInstance(context);
     * UserDao userDao = db.userDao();
     *
     * @param context - Application context
     * @return - Database instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),  // Use app context to prevent memory leaks
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            // IMPORTANT: Remove these before production!
                            // .fallbackToDestructiveMigration()  // Deletes old database if schema changes
                            // .allowMainThreadQueries()          // Allows queries on main thread (BAD for production!)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Close the database
     * Usually not needed, but useful for testing
     */
    public static void destroyInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }

    // ==================== MIGRATION (for future use) ====================

    /**
     * When you need to change the database schema (add columns, tables, etc.),
     * you'll need to create migrations.
     *
     * Example migration from version 1 to 2:
     *
     * static final Migration MIGRATION_1_2 = new Migration(1, 2) {
     *     @Override
     *     public void migrate(SupportSQLiteDatabase database) {
     *         // Add a new column to users table
     *         database.execSQL("ALTER TABLE users ADD COLUMN email TEXT");
     *     }
     * };
     *
     * Then in getInstance(), add:
     * .addMigrations(MIGRATION_1_2)
     */

    // ==================== HELPER METHODS ====================

    /**
     * Clear all data from the database
     * Useful for testing or "Sign Out All Devices" feature
     *
     * WARNING: This deletes EVERYTHING!
     *
     * Usage:
     * AppDatabase.getInstance(context).clearAllTables();
     */
    public void clearAllData() {
        // Run on background thread!
        new Thread(() -> {
            userDao().deleteAllUsers();
            // No need to delete budgets/expenses - CASCADE will handle it
        }).start();
    }
}