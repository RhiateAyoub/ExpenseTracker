// UserDao.java
// Location: app/src/main/java/com/example/expensetracker/data/dao/UserDao.java
package com.example.expensetracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensetracker.data.entity.User;

import java.util.List;

/**
 * Data Access Object (DAO) for User entity.
 *
 * Room automatically implements all these methods!
 * You just define what you want, and Room writes the SQL code.
 *
 * @Dao - Tells Room this interface defines database operations
 */
@Dao
public interface UserDao {

    // ==================== INSERT ====================

    /**
     * Insert a new user into the database
     *
     * @param user - User object to insert
     * @return - The ID of the newly inserted user (useful for knowing user's ID after registration)
     *
     * Usage:
     * User newUser = new User("ayoub", hashedPassword, "Ayoub", System.currentTimeMillis());
     * long userId = userDao.insert(newUser);
     */
    @Insert
    long insert(User user);

    // ==================== UPDATE ====================

    /**
     * Update an existing user
     * Finds the user by ID and updates all fields
     *
     * @param user - User object with updated data
     * @return - Number of rows updated (should be 1 if successful)
     *
     * Usage:
     * user.setFullName("Ayoub Updated");
     * userDao.update(user);
     */
    @Update
    int update(User user);

    // ==================== DELETE ====================

    /**
     * Delete a user from the database
     * WARNING: This will also delete all budgets and expenses for this user (CASCADE)
     *
     * @param user - User object to delete
     *
     * Usage:
     * userDao.delete(user);
     */
    @Delete
    void delete(User user);

    // ==================== QUERIES ====================

    /**
     * Get a user by their username
     * Used for login - check if username exists and get password hash
     *
     * @param username - Username to search for
     * @return - User object if found, null if not found
     *
     * Usage:
     * User user = userDao.getUserByUsername("ayoub");
     * if (user != null) {
     *     // User exists, check password
     * }
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    /**
     * Get a user by their ID
     *
     * @param userId - User ID to search for
     * @return - User object if found, null if not found
     *
     * Usage:
     * User user = userDao.getUserById(1);
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    /**
     * Get all users in the database
     * Useful for admin features or debugging
     *
     * @return - List of all users
     *
     * Usage:
     * List<User> allUsers = userDao.getAllUsers();
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<User> getAllUsers();

    /**
     * Get all users as LiveData (automatically updates UI)
     * When any user is added/updated/deleted, observers are notified
     *
     * @return - LiveData containing list of users
     *
     * Usage in Fragment:
     * userDao.getAllUsersLive().observe(this, users -> {
     *     // This runs automatically when users change!
     *     updateUI(users);
     * });
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    LiveData<List<User>> getAllUsersLive();

    /**
     * Check if a username already exists
     * Useful for registration validation
     *
     * @param username - Username to check
     * @return - Number of users with this username (should be 0 or 1)
     *
     * Usage:
     * int count = userDao.checkUsernameExists("ayoub");
     * if (count > 0) {
     *     // Username is already taken
     * }
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int checkUsernameExists(String username);

    /**
     * Get total number of users
     *
     * @return - Total user count
     */
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    /**
     * Delete all users (useful for testing or reset functionality)
     * WARNING: This will also delete ALL budgets and expenses!
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();

    // ==================== ADVANCED QUERIES ====================

    /**
     * Search users by username (partial match)
     *
     * @param searchQuery - Search term
     * @return - List of matching users
     *
     * Usage:
     * List<User> results = userDao.searchUsers("%ayo%");
     * // Finds "ayoub", "ayoubi", etc.
     */
    @Query("SELECT * FROM users WHERE username LIKE :searchQuery ORDER BY username ASC")
    List<User> searchUsers(String searchQuery);
    //// :identifier sera ce que l'utilisateur a tapé (ex: "Souhail"ou "Souhail@gmail.com")
    @Query("SELECT * FROM users WHERE username = :identifier OR email = :identifier LIMIT 1")
    User findUserForLogin(String identifier);
    // Ajoutez une vérification pour l'inscription (pour éviter les doublons d'email)
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);
}