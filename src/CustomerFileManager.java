import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
/**
 * Utility class for saving and loading {@link Customer} objects from a text file.
 * <p>
 * Each line in the file represents one customer, with fields separated by {@code |}..
 * <p><b>File format:</b>:
 * <pre>
 * customerId|firstName|lastName|age|gender|email|phone|cnp|addressLine1|addressLine2|city|postalCode|country
 * </pre>
 * Lines starting with '#' are treated as comments and ignored.
 * @author Ilinca Rusescu
 * @version 1.0
 */
public class CustomerFileManager {

    /** Path to the text file used for storing customer data. */
    private static final String FILE_PATH = "customers.txt";

    // =============================
    //         SAVE CUSTOMERS
    // =============================

    /**
     * Saves all {@link Customer} objects to {@code customers.txt}.
     * <p>
     * The method overwrites existing data and writes a header for readability.
     * Each field is separated by {@code |}, and null address lines are replaced with empty values.
     * 
     *
     * @param customers list of customers to save; ignored if empty or null
     */
    public static void saveCustomers(List<Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {

            // --- Header line (for clarity)
            writer.write("# customerId|firstName|lastName|age|gender|email|phone|cnp|addressLine1|addressLine2|city|postalCode|country");
            writer.newLine();

            for (Customer c : customers) {
                writer.write(String.join("|",
                        c.getCustomerId(),
                        c.getFirstName(),
                        c.getLastName(),
                        String.valueOf(c.getAge()),
                        c.getGender(),
                        c.getEmail(),
                        c.getPhoneNumber(),
                        c.getCnp(),
                        c.getAddressLine1(),
                        c.getAddressLine2() == null ? "" : c.getAddressLine2(),
                        c.getCity(),
                        c.getPostalCode(),
                        c.getCountry()
                ));
                writer.newLine();
            }
            System.out.println("[INFO] Customers saved successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save customers: " + e.getMessage());
        }
    }

    // =============================
    //         LOAD CUSTOMERS
    // =============================

    /**
     * Loads all {@link Customer} records from {@code customers.txt}.
     * <p>
     * Each line is split into 13 fields. Malformed lines are skipped with a warning.
     * Empty address lines are restored as {@code null}.
     * 
     * <p>
     * After loading, the original {@code customerId} values are restored using reflection,
     * and the {@link IdGenerator} is synchronized with the last ID.
     * 
     *
     * @return list of reconstructed {@link Customer} objects; empty if no file exists
     */
    public static List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1); // -1 to keep empty fields (address 2)
                if (parts.length != 13) {
                    System.err.println("[WARN] Skipping malformed line (expected 13 fields): " + line);
                    continue;
                }

                String customerId = parts[0].trim();
                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                int age;
                try {
                    age = Integer.parseInt(parts[3].trim());
                } catch (NumberFormatException nfe) {
                    System.err.println("[WARN] Invalid age for customerId=" + customerId + " -> defaulting to 18");
                    age = 18;
                }
                String gender = parts[4].trim();
                String email = parts[5].trim();
                String phone = parts[6].trim();
                String cnp = parts[7].trim();
                String addressLine1 = parts[8].trim();
                String addressLine2 = parts[9].trim().isEmpty() ? null : parts[9].trim();
                String city = parts[10].trim();
                String postalCode = parts[11].trim();
                String country = parts[12].trim().toUpperCase();


                // --- Rebuild the customer
                Customer c = new Customer(
                        firstName,
                        lastName,
                        age,
                        gender,
                        email,
                        phone,
                        cnp,
                        addressLine1,
                        addressLine2,
                        city,
                        postalCode,
                        country
                );

                // --- Restore original ID using reflection
                Field idField = Customer.class.getDeclaredField("customerId");
                idField.setAccessible(true);
                idField.set(c, customerId);


                customers.add(c);
            }
            System.out.println("[INFO] Customers loaded: " + customers.size());
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] No existing customers file found, starting fresh.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load customers: " + e.getMessage());
        }
        IdGenerator.syncCustomerCounter(customers);
        return customers;
    }

    // =============================
    //         VALIDATION HELPERS
    // =============================

    /**
     * Checks whether a CNP already exists among the given customers.
     *
     * @param customers list of existing customers
     * @param cnp       CNP to check
     * @return {@code true} if the CNP is already registered
     */
    public static boolean cnpExists(List<Customer> customers, String cnp) {
        return customers.stream().anyMatch(c -> c.getCnp().equals(cnp));
    }

    /**
     * Checks whether a phone number already exists among the given customers.
     *
     * @param customers list of existing customers
     * @param phone     phone number to check
     * @return {@code true} if the phone number is already registered
     */
    public static boolean phoneExists(List<Customer> customers, String phone) {
        return customers.stream().anyMatch(c -> c.getPhoneNumber().equals(phone));
    }
}
