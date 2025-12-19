// UserRepository.java
// Location: app/src/main/java/com/example/expensetracker/data/repository/UserRepository.java
package com.example.expensetracker.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.dao.UserDao;
import com.example.expensetracker.data.entity.User;
import com.example.expensetracker.utils.PasswordUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Repository for User-related operations.
 *
 * Handles:
 * - User registration
 * - User login
 * - Background threading
 * - Password security
 *
 * Why Repository?
 * - ViewModels don't directly access DAOs
 * - Repository handles background threads
 * - Single source of truth for user data
 */
public class UserRepository {

    private UserDao userDao;
    private ExecutorService executorService;

    /**
     * Constructor
     *
     * @param context - Application context
     */
    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Register a new user
     *
     * Process:
     * 1. Check if username already exists
     * 2. Hash password with BCrypt
     * 3. Create user in database
     * 4. Return user ID
     *
     * @param username - Username
     * @param password - Plain password
     * @param fullName - Full name (optional)
     * @param callback - Callback with result
     *
     * Usage:
     * repository.register("ayoub", "password123", "Ayoub", new RegisterCallback() {
     *     @Override
     *     public void onSuccess(int userId) {
     *         // Registration successful!
     *     }
     *
     *     @Override
     *     public void onError(String error) {
     *         // Show error message
     *     }
     * });
     */
    public void register(String username, String password, String fullName, RegisterCallback callback) {
        executorService.execute(() -> {
            try {
                // Check if username already exists
                int count = userDao.checkUsernameExists(username);
                if (count > 0) {
                    callback.onError("Ce nom d'utilisateur existe déjà");
                    return;
                }

                // Hash password
                String hashedPassword = PasswordUtil.hashPassword(password);

                // Create user
                User user = new User(username, hashedPassword, fullName, System.currentTimeMillis());
                long userId = userDao.insert(user);

                if (userId > 0) {
                    callback.onSuccess((int) userId);
                } else {
                    callback.onError("Erreur lors de l'inscription");
                }

            } catch (Exception e) {
                callback.onError("Erreur: " + e.getMessage());
            }
        });
    }

    /**
     * Login user
     *
     * Process:
     * 1. Get user by username
     * 2. Verify password
     * 3. Return user if successful
     *
     * @param username - Username
     * @param password - Plain password
     * @param callback - Callback with result
     *
     * Usage:
     * repository.login("ayoub", "password123", new LoginCallback() {
     *     @Override
     *     public void onSuccess(User user) {
     *         // Login successful!
     *     }
     *
     *     @Override
     *     public void onError(String error) {
     *         // Show error message
     *     }
     * });
     */
    public void login(String username, String password, LoginCallback callback) {
        executorService.execute(() -> {
            try {
                // Get user from database
                User user = userDao.getUserByUsername(username);

                if (user == null) {
                    callback.onError("Nom d'utilisateur ou mot de passe incorrect");
                    return;
                }

                // Verify password
                boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getPassword());

                if (passwordMatches) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("Nom d'utilisateur ou mot de passe incorrect");
                }

            } catch (Exception e) {
                callback.onError("Erreur: " + e.getMessage());
            }
        });
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Get user by ID
     *
     * @param userId - User ID
     * @param callback - Callback with user
     */
    public void getUserById(int userId, UserCallback callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserById(userId);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError("Utilisateur introuvable");
                }
            } catch (Exception e) {
                callback.onError("Erreur: " + e.getMessage());
            }
        });
    }

    /**
     * Update user
     *
     * @param user - Updated user object
     * @param callback - Callback with result
     */
    public void updateUser(User user, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                int rowsUpdated = userDao.update(user);
                if (rowsUpdated > 0) {
                    callback.onSuccess();
                } else {
                    callback.onError("Erreur lors de la mise à jour");
                }
            } catch (Exception e) {
                callback.onError("Erreur: " + e.getMessage());
            }
        });
    }

    /**
     * Delete user
     * WARNING: This also deletes all budgets and expenses!
     *
     * @param user - User to delete
     * @param callback - Callback with result
     */
    public void deleteUser(User user, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                userDao.delete(user);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Erreur: " + e.getMessage());
            }
        });
    }

    /**
     * Change password
     *
     * @param userId - User ID
     * @param oldPassword - Current password
     * @param newPassword - New password
     * @param callback - Callback with result
     */
    public void changePassword(int userId, String oldPassword, String newPassword, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserById(userId);

                if (user == null) {
                    callback.onError("Utilisateur introuvable");
                    return;
                }

                // Verify old password
                if (!PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
                    callback.onError("Mot de passe actuel incorrect");
                    return;
                }

                // Hash new password
                String hashedPassword = PasswordUtil.hashPassword(newPassword);
                user.setPassword(hashedPassword);

                // Update user
                int rowsUpdated = userDao.update(user);
                if (rowsUpdated > 0) {
                    callback.onSuccess();
                } else {
                    callback.onError("Erreur lors de la mise à jour");
                }

            } catch (Exception e) {
                callback.onError("Erreur: " + e.getMessage());
            }
        });
    }

    // ==================== CALLBACKS ====================

    /**
     * Callback for registration
     */
    public interface RegisterCallback {
        void onSuccess(int userId);
        void onError(String error);
    }

    /**
     * Callback for login
     */
    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    /**
     * Callback for getting user
     */
    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    /**
     * Callback for update/delete operations
     */
    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }
}