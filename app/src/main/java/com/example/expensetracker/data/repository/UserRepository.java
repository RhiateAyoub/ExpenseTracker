// UserRepository.java
// Location: app/src/main/java/com/example/expensetracker/data/repository/UserRepository.java
package com.example.expensetracker.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.expensetracker.data.database.AppDatabase;
import com.example.expensetracker.data.dao.UserDao;
import com.example.expensetracker.data.entity.User;
import com.example.expensetracker.utils.PasswordUtil;

import org.mindrot.jbcrypt.BCrypt;

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
    private FirebaseAuth firebaseAuth;

    /**
     * Constructor
     *
     * @param context - Application context
     */
    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
        firebaseAuth = FirebaseAuth.getInstance();
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
    public void register(String username, String password, String fullName, String email, RegisterCallback callback) {
        executorService.execute(() -> {
            try {
                // 1. Check if username exists
                if (userDao.checkUsernameExists(username) > 0) {
                    callback.onError("Ce nom d'utilisateur existe déjà");
                    return;
                }

                // 2. Check if email exists
                if (userDao.checkEmailExists(email) > 0) {
                    callback.onError("Cet email est déjà utilisé");
                    return;
                }

                // 3. Hash password for local storage
                String hashedPassword = PasswordUtil.hashPassword(password);

                // 4. Create LOCAL user first
                User user = new User(username, hashedPassword, fullName, email, System.currentTimeMillis());
                long userId = userDao.insert(user);

                if (userId > 0) {
                    user.setId((int) userId);

                    // 5. Create Firebase user IN PARALLEL
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                // Firebase account created successfully
                                FirebaseUser firebaseUser = authResult.getUser();
                                if (firebaseUser != null) {
                                    String firebaseUid = firebaseUser.getUid();

                                    // Store Firebase UID in Room database
                                    executorService.execute(() -> {
                                        userDao.setFirebaseUid((int) userId, firebaseUid);
                                        user.setFirebaseUid(firebaseUid);
                                        callback.onSuccess(user);
                                    });
                                } else {
                                    // Firebase succeeded but no user returned (rare)
                                    callback.onSuccess(user);
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Firebase registration failed, but local user exists
                                // Allow user to login locally, sync will fail gracefully
                                callback.onSuccess(user);
                            });
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
     * @param identifier - Username
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
    // AJOUT : On passe le callback en paramètre
    public void login(String identifier, String password, LoginCallback callback) {
        executorService.execute(() -> {
            try {
                // 1. Find user locally (by username or email)
                User user = userDao.findUserForLogin(identifier);

                if (user == null) {
                    callback.onError("Identifiant ou mot de passe incorrect");
                    return;
                }

                // 2. Verify password locally
                if (!BCrypt.checkpw(password, user.getPassword())) {
                    callback.onError("Identifiant ou mot de passe incorrect");
                    return;
                }

                // 3. Local authentication succeeded
                // Now sign in to Firebase
                firebaseAuth.signInWithEmailAndPassword(user.getEmail(), password)
                        .addOnSuccessListener(authResult -> {
                            // Firebase login successful
                            FirebaseUser firebaseUser = authResult.getUser();
                            if (firebaseUser != null) {
                                String firebaseUid = firebaseUser.getUid();

                                // Update Firebase UID if it's not stored yet
                                if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                                    executorService.execute(() -> {
                                        userDao.setFirebaseUid(user.getId(), firebaseUid);
                                        user.setFirebaseUid(firebaseUid);
                                    });
                                }
                            }
                            callback.onSuccess(user);
                        })
                        .addOnFailureListener(e -> {
                            // Firebase login failed (offline or account doesn't exist)
                            // But local login succeeded, so allow offline mode
                            callback.onSuccess(user);
                        });

            } catch (Exception e) {
                callback.onError("Erreur de connexion : " + e.getMessage());
            }
        });
    }
    public void checkIfUserExists(String username, String email, CheckCallback callback) {
        executorService.execute(() -> {
            if (userDao.checkUsernameExists(username) > 0) {
                callback.onResult(true, "Ce nom d'utilisateur est déjà pris.");
            } else if (userDao.checkEmailExists(email) > 0) {
                callback.onResult(true, "Cet email est déjà utilisé.");
            } else {
                callback.onResult(false, ""); // N'existe pas, c'est bon
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
    // AJOUTEZ CETTE MÉTHODE
    public void resetPasswordByEmail(String email, String newPassword, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                // 1. Trouver l'utilisateur par email (besoin d'ajouter cette requête dans DAO si elle manque)
                // Pour l'instant, utilisons une méthode qui cherche par identifier (username ou email)
                User user = userDao.findUserForLogin(email);

                if (user != null) {
                    // 2. Hasher le nouveau mot de passe
                    String hashedPassword = PasswordUtil.hashPassword(newPassword);
                    user.setPassword(hashedPassword);

                    // 3. Mettre à jour
                    userDao.update(user);
                    callback.onSuccess();
                } else {
                    callback.onError("Utilisateur introuvable.");
                }
            } catch (Exception e) {
                callback.onError("Erreur : " + e.getMessage());
            }
        });
    }

    // ==================== CALLBACKS ====================

    /**
     * Callback for registration
     */
    public interface RegisterCallback {
        void onSuccess(User user); // Return the full User object
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
    // Callback for checkIfUserExists
    public interface CheckCallback {
        void onResult(boolean exists, String message);
    }
}
