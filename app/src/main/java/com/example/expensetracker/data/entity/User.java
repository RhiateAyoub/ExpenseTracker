// User.java
// Location: app/src/main/java/com/example/expensetracker/data/entity/User.java
package com.example.expensetracker.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity class representing the 'users' table in the database.
 * This is where we store user account information.
 *
 * @Entity - Tells Room this class represents a database table
 * @tableName - Names the table "users" in the database
 * @indices - Creates an index on username for faster lookups and ensures uniqueness
 */
@Entity(
        tableName = "users",
        indices = {@Index(value = "username", unique = true)}
)
public class User {

    /**
     * Primary key - unique identifier for each user
     * @PrimaryKey - Marks this field as the primary key
     * autoGenerate = true - Room automatically assigns increasing numbers (1, 2, 3...)
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    /**
     * Username - must be unique (enforced by index above)
     * Used for login
     */
    @ColumnInfo(name = "username")
    private String username;

    /**
     * Password - stored as a HASHED value (never plain text!)
     * We use BCrypt to hash passwords for security
     */
    @ColumnInfo(name = "password")
    private String password;

    /**
     * User's full name - displayed in "Bonjour [name]"
     * Optional field
     */
    @ColumnInfo(name = "full_name")
    private String fullName;

    /**
     * Account creation timestamp
     * Stored as milliseconds since Unix epoch (January 1, 1970)
     * Use: System.currentTimeMillis()
     */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor - required by Room
     */
    public User() {
    }

    /**
     * Constructor for creating a new user
     * ID is auto-generated, so we don't include it here
     */
    public User(String username, String password, String fullName, long createdAt) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    // ==================== GETTERS AND SETTERS ====================
    // Room needs these to read/write data

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Returns the display name for the user
     * If fullName is set, use it; otherwise use username
     */
    public String getDisplayName() {
        return (fullName != null && !fullName.isEmpty()) ? fullName : username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}