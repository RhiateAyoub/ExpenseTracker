// PasswordUtil.java
// Location: app/src/main/java/com/example/expensetracker/utils/PasswordUtil.java
package com.example.expensetracker.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification.
 * Uses BCrypt - a secure one-way hashing algorithm.
 *
 * SECURITY NOTE:
 * - Passwords are NEVER stored as plain text
 * - BCrypt automatically adds salt (random data) to prevent rainbow table attacks
 * - Even identical passwords produce different hashes
 *
 * Example:
 * Password: "mypassword123"
 * Hash 1:   "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
 * Hash 2:   "$2a$10$K3pP9wLMickgx2ZMRZoMxf..." (different!)
 */
public class PasswordUtil {

    /**
     * Hash a plain text password
     *
     * @param plainPassword - The password to hash (e.g., "mypassword123")
     * @return - Hashed password (e.g., "$2a$10$N9qo8uLOickgx2ZMRZoMye...")
     *
     * Usage:
     * String hashedPassword = PasswordUtil.hashPassword("mypassword123");
     * // Store hashedPassword in database
     */
    public static String hashPassword(String plainPassword) {
        // BCrypt.gensalt() generates random salt
        // This makes each hash unique even for identical passwords
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verify if a plain password matches a hashed password
     *
     * @param plainPassword - Password entered by user (e.g., "mypassword123")
     * @param hashedPassword - Hashed password from database
     * @return - true if passwords match, false otherwise
     *
     * Usage:
     * String enteredPassword = "mypassword123";
     * String storedHash = user.getPassword(); // From database
     *
     * if (PasswordUtil.verifyPassword(enteredPassword, storedHash)) {
     *     // Login successful!
     * } else {
     *     // Wrong password
     * }
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Handle corrupted hash or invalid format
            return false;
        }
    }

    /**
     * Validate password strength
     *
     * Requirements:
     * - At least 6 characters
     * - Can add more requirements (uppercase, numbers, etc.)
     *
     * @param password - Password to validate
     * @return - true if valid, false otherwise
     *
     * Usage:
     * if (!PasswordUtil.isValidPassword(password)) {
     *     // Show error message
     * }
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Minimum length check
        if (password.length() < 6) {
            return false;
        }

        // Add more requirements here if needed:
        // - Must contain uppercase letter
        // - Must contain number
        // - Must contain special character
        // etc.

        return true;
    }

    /**
     * Get password strength message
     * Provides user feedback on password quality
     *
     * @param password - Password to check
     * @return - Strength message
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est requis";
        }

        if (password.length() < 6) {
            return "Le mot de passe doit contenir au moins 6 caractères";
        }

        if (password.length() < 8) {
            return "Mot de passe faible";
        }

        if (password.length() < 12) {
            return "Mot de passe moyen";
        }

        return "Mot de passe fort";
    }
}