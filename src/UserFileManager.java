import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for saving and loading {@link User} objects from a text file.
 * <p>
 * Each line in {@code users.txt} contains user data separated by {@code |}:
 * <pre>
 * username|passwordHash|customerId|role
 * </pre>
 * Lines starting with {@code #} are ignored as comments.
 * 
 *
 * author Ilinca Rusescu
 * @version 1.0
 */
public class UserFileManager {

    /** Path to the text file used to store user information. */
    private static final String FILE_PATH = "users.txt";

    // =============================
    //          SAVE USERS
    // =============================

    /**
     * Saves all {@link User} objects to the {@code users.txt} file.
     * <p>
     * The file is overwritten on each save. For clarity, a commented header line
     * is added at the top of the file.
     * 
     *
     * @param users list of users to save (ignored if empty or null)
     */
    public static void saveUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            // --- Header for readability
            writer.write("# username|passwordHash|customerId|role");
            writer.newLine();

            for (User u : users) {
                writer.write(String.join("|",
                        u.getUsername(),
                        u.getPasswordHash(),
                        u.getCustomerId(),
                        u.getRole()
                ));
                writer.newLine();
            }
            System.out.println("[INFO] Users saved successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Error saving users: " + e.getMessage());
        }
    }

    // =============================
    //          LOAD USERS
    // =============================

    /**
     * Loads all users from {@code users.txt}.
     * <p>
     * Each valid line must contain four fields:
     * <pre>
     * username|passwordHash|customerId|role
     * </pre>
     * Invalid or malformed lines are skipped automatically.
     * 
     *
     * @return a list of reconstructed {@link User} objects;
     *         an empty list if the file does not exist
     */
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split("\\|");
                if (p.length != 4) continue; // skip malformed line

                String username = p[0];
                String passwordHash = p[1];
                String customerId = p[2];
                String role = p[3];

                // --- Rebuild user instance
                User user = new User(username, passwordHash, customerId, role, true);
                users.add(user);
            }
            System.out.println("[INFO] Users loaded: " + users.size());
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] No users.txt file found, starting fresh.");
        } catch (IOException e) {
            System.err.println("[ERROR] Error loading users: " + e.getMessage());
        }

        return users;
    }

    // =============================
    //        VALIDATION HELPERS
    // =============================

    public static boolean usernameExists(List<User> users, String username) {
        return users.stream().anyMatch(u -> u.getUsername().equals(username));
    }
}
