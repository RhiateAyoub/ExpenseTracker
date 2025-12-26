// UserRepository.java
// Location: app/src/main/java/com/example/expensetracker/data/repository/UserRepository.java
package com.example.expensetracker.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

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
    public void register(String username, String password, String fullName, String Email, RegisterCallback callback) {
        executorService.execute(() -> {
            try {
                // 1. Vérifier si le username existe déjà
                if (userDao.checkUsernameExists(username) > 0) {
                    callback.onError("Ce nom d'utilisateur existe déjà");
                    return;
                }

                // 2. AJOUTÉ : Vérifier si l'email existe déjà (Important !)
                if (userDao.checkEmailExists(Email) > 0) {
                    callback.onError("Cet email est déjà utilisé");
                    return;
                }

                // Hash password
                String hashedPassword = PasswordUtil.hashPassword(password);

                // 3. CORRIGÉ : Ajout du paramètre 'Email' dans le constructeur User
                // Assurez-vous que votre User.java a bien ce constructeur !
                User user = new User(username, hashedPassword, fullName, Email, System.currentTimeMillis());

                long userId = userDao.insert(user);

                if (userId > 0) {
                    User newUser = userDao.getUserById((int) userId);
                    callback.onSuccess(newUser);
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
        // AJOUT : On enveloppe tout le code dans l'executorService pour passer en arrière-plan
        executorService.execute(() -> {
            try {
                // Cette ligne causait le crash car elle accédait à la BD sur le main thread
                User user = userDao.findUserForLogin(identifier);

                if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                    // Succès
                    callback.onSuccess(user);
                } else {
                    // Échec (mot de passe ou user incorrect)
                    callback.onError("Identifiant ou mot de passe incorrect");
                }
            } catch (Exception e) {
                // Gestion des erreurs imprévues
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
