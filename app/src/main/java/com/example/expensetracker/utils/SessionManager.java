// SessionManager.java
// Location: app/src/main/java/com/example/expensetracker/utils/SessionManager.java
package com.example.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user login session.
 *
 * Uses SharedPreferences to store:
 * - Whether user is logged in
 * - User ID
 * - Username
 *
 * This persists even after app is closed/reopened!
 *
 * Usage:
 * SessionManager session = new SessionManager(context);
 * if (session.isLoggedIn()) {
 *     // User is logged in
 *     int userId = session.getUserId();
 * }
 */
public class SessionManager {

    // SharedPreferences file name
    private static final String PREF_NAME = "ExpenseTrackerSession";

    // Keys for storing values
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_FIREBASE_UID = "firebaseUid";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    /**
     * Constructor
     *
     * @param context - Application or Activity context
     *
     * Usage:
     * SessionManager session = new SessionManager(requireContext());
     */
    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ==================== SESSION MANAGEMENT ====================

    /**
     * Create login session
     * Call this after successful login
     *
     * @param userId - User's database ID
     * @param username - Username
     * @param fullName - User's full name (optional, can be null)
     *
     * Usage:
     * session.createLoginSession(user.getId(), user.getUsername(), user.getFullName());
     */
    public void createLoginSession(int userId, String username, String fullName, String firebaseUid) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_FIREBASE_UID, firebaseUid);
        editor.apply();
    }

    /**
     * Check if user is logged in
     *
     * @return - true if logged in, false otherwise
     *
     * Usage:
     * if (session.isLoggedIn()) {
     *     // Navigate to home screen
     * } else {
     *     // Navigate to login screen
     * }
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Log out user
     * Clears all session data
     *
     * Usage:
     * session.logout();
     * // Then navigate to login screen
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // ==================== GET USER DATA ====================

    /**
     * Get logged-in user's ID
     *
     * @return - User ID, or -1 if not logged in
     *
     * Usage:
     * int userId = session.getUserId();
     * if (userId != -1) {
     *     // Use userId for database queries
     * }
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get logged-in user's username
     *
     * @return - Username, or null if not logged in
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    /**
     * Get logged-in user's full name
     *
     * @return - Full name, or null if not set
     */
    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    /**
     * Get logged-in user's Firebase UID
     *
     * @return - Firebase UID, or null if not set
     */
    public String getFirebaseUid() {
        return prefs.getString(KEY_FIREBASE_UID, null);
    }

    /**
     * Get the first name of the logged-in user.* If full name is "Ayoub El" it returns "Ayoub".
     * If no full name, falls back to username.
     * @return User's first name or username.
     */
    public String getFirstName() {
        String fullName = getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            // Split the full name by space and return the first part
            return fullName.trim().split("\\s+")[0];
        }

        String username = getUsername();
        if (username != null) {
            return username;
        }

        return "Utilisateur"; // Default fallback
    }

    /**
     * Get display name (full name if available, otherwise username)
     * This is what you show in "Bonjour [name]"
     *
     * @return - Display name
     */
    public String getDisplayName() {
        String fullName = getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }

        String username = getUsername();
        if (username != null && !username.isEmpty()) {
            return username;
        }

        return "Utilisateur";
    }

    // ==================== UPDATE SESSION DATA ====================

    /**
     * Update full name in session
     * Call this if user updates their profile
     *
     * @param fullName - New full name
     */
    public void updateFullName(String fullName) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    /**
     * Update username in session
     * Call this if user changes their username
     *
     * @param username - New username
     */
    public void updateUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }
}