import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a registered system user in the e-banking application.
 * <p>
 * Each {@code User} has login credentials (username and password hash)
 * and may be linked to a corresponding {@link Customer} profile.
 * <br>
 * A new customer profile is created before the user, then linked here.
 * 
 *
 * author Ilinca Rusescu
 * @version 2.1
 */

public class User {

    /** Username used for login. Must be unique across the system. */
    private final String username;

    /** Hashed password stored securely. */
    private String passwordHash;

    /** User role: either "admin" or "user". */
    private final String role;

    /**
     * The unique ID of the associated customer *as stored in file*.
     * This ensures the relationship survives application restarts,
     * even before we link the real {@link Customer} object.
     */
    private final String storedCustomerId;

    /** Associated customer profile. */
    private Customer customer;

    // ----------------------------------------------------
    // CONSTRUCTOR FOR NORMAL USER REGISTRATION
    // ----------------------------------------------------
    /**
     * Creates a regular user linked to a {@link Customer} profile.
     *
     * @param username chosen username (non-blank)
     * @param password chosen password (≥ 4 chars, will be hashed)
     * @param customer existing {@link Customer} to link
     * @throws IllegalArgumentException if validation fails
     */
    public User(String username, String password, Customer customer) {
        if (isBlank(username)) throw new IllegalArgumentException("Username cannot be blank.");
        if (isBlank(password) || password.length() < 4)
            throw new IllegalArgumentException("Password must have at least 4 characters.");
        if (customer == null) throw new IllegalArgumentException("Customer cannot be null.");

        this.username = username.trim();
        this.passwordHash = hash(password.trim());
        this.role = "user";
        this.customer = customer;
        this.storedCustomerId = customer.getCustomerId();
    }

    // ----------------------------------------------------
    // CONSTRUCTOR FOR ADMIN
    // ----------------------------------------------------
    /**
     * Creates an admin account (no associated customer).
     *
     * @param username admin username
     * @param password admin password (plain text)
     * @throws IllegalArgumentException if username/password are invalid
     */
    public User(String username, String password) {
        if (isBlank(username)) throw new IllegalArgumentException("Username cannot be blank.");
        if (isBlank(password) || password.length() < 4)
            throw new IllegalArgumentException("Password must have at least 4 characters.");

        this.username = username.trim();
        this.passwordHash = password.trim();
        this.role = "admin";
        this.customer = null;
        this.storedCustomerId = "N/A";
    }

    // ----------------------------------------------------
    // CONSTRUCTOR FOR LOADING FROM FILE
    // ----------------------------------------------------
    /**
     * Constructor used exclusively when loading user data from persistent storage.
     *
     * @param username         username
     * @param passwordHash     already hashed password
     * @param storedCustomerId ID of linked customer (string)
     * @param role             user role ("admin" / "user")
     * @param ignored          dummy flag to differentiate constructors
     */
    public User(String username, String passwordHash, String storedCustomerId, String role, boolean ignored) {
        this.username = username.trim();
        this.passwordHash = passwordHash.trim();
        this.storedCustomerId = storedCustomerId.trim();
        this.role = role.trim();
        this.customer = null; // will be linked after customers are loaded
    }

    // =============================
    //          GETTERS
    // =============================

    /** @return username */
    public String getUsername() { return username; }

    /** @return stored password (treated as hash) */
    public String getPasswordHash() { return passwordHash; }

    /** @return role of the user ("user" or "admin") */
    public String getRole() { return role; }

    /** @return associated Customer or null if admin */
    public Customer getCustomer() { return customer; }

    /**
     * @return real customer ID if linked, otherwise stored ID from file
     */
    public String getCustomerId() {
        return (customer != null) ? customer.getCustomerId() : storedCustomerId;
    }


    // =============================
    //      CUSTOMER LINKING
    // =============================

    /**
     * Associates this user with a loaded {@link Customer} after data import.
     *
     * @param c the customer object to link
     */
    public void associateCustomer(Customer c) {
        this.customer = c;
    }

    // =============================
    //  AUTHENTICATION & SECURITY
    // =============================

    /**
     * Verifies if an input password matches the stored hash.
     *
     * @param inputPassword plain text password
     * @return {@code true} if password matches, otherwise {@code false}
     */
    public boolean checkPassword(String inputPassword) {
        return passwordHash.equals(hash(inputPassword));
    }

    /**
     * Resets the user's password without requiring the old one.
     * <p>
     * This method is used in "Forgot Password" flow, where the user's identity
     * is confirmed through external information (e.g., phone number).
     * <br><br>
     * The provided new password is validated and then stored securely as a hashed value.
     * 
     *
     * @param newPassword the new password in plain text (must be at least 4 characters)
     *
     * @throws IllegalArgumentException if the password is invalid (null, blank or too short)
     */
    public void resetPassword(String newPassword) {
        if (isBlank(newPassword) || newPassword.length() < 4) {
            throw new IllegalArgumentException("Password must have at least 4 characters.");
        }

        this.passwordHash = hash(newPassword);
    }

    // =============================
    //          UTILITIES
    // =============================

    /**
     * Hashes a given string using SHA-256.
     *
     * @param input plain text to hash
     * @return hexadecimal string representation of the hash
     */
    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : encoded) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failure.", e);
        }
    }

    /** @return {@code true} if string is null, empty or whitespace only */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Returns a formatted string containing user details.
     *
     * @return summary of user information
     */
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                (customer != null ? ", name='" + customer.getFullName() + '\'' : "") +
                '}';
    }

}

