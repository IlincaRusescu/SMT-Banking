import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that manages persistence of {@link Account} objects.
 * <p>
 * The {@code AccountFileManager} handles saving and loading of all account types
 * to and from a plain text file named {@code accounts.txt}. The file stores
 * one record per account, using a pipe-delimited format.
 * 
 *
 * <p>Supported account types include:
 * <ul>
 *     <li>{@link DebitAccount}</li>
 *     <li>{@link SavingsAccount}</li>
 *     <li>{@link CreditAccount}</li>
 * </ul>
 *
 * <p>Each line in {@code accounts.txt} follows the structure:
 * <pre>
 * type|accountId|customerId|iban|balance|currency|creationDate|interestRate|creditLimit
 * </pre>
 * Example:
 * <pre>
 * D|A001|C002|RO12BANK2345A00123|1530.25|EUR|2024-02-12|-|-
 * </pre>
 *
 * <p>
 * Comment lines beginning with {@code #} are ignored during reading.
 * The class ensures account ID synchronization via {@link IdGenerator}.
 * 
 *
 * @author  Ilinca Rusescu
 * @version 3.0
 */
public class AccountFileManager {

    /** Default file path used for storing account data. */
    private static final String FILE_PATH = "accounts.txt";

    // =============================
    //          SAVE ACCOUNTS
    // =============================

    /**
     * Saves all {@link Account} instances to the text file {@code accounts.txt}.
     * <p>
     * The method overwrites any existing file content and writes each account on a separate line.
     * The first line of the file is a comment header describing the column order.
     * 
     * <p>
     * For {@link SavingsAccount} and {@link CreditAccount}, the {@code interestRate} field is saved.
     * For {@link CreditAccount}, both {@code interestRate} and {@code creditLimit} are included.
     * Missing fields are represented by the dash character "-".
     * 
     *
     * @param accounts list of {@link Account} objects to save; if {@code null}, the operation is skipped
     */
    public static void saveAccounts(List<Account> accounts) {
        if (accounts == null) {
            System.err.println("[WARN] No accounts to save.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_PATH, false), StandardCharsets.UTF_8))) {

            // --- Write file header
            writer.write("# type|accountId|customerId|iban|balance|currency|creationDate|interestRate|creditLimit");
            writer.newLine();

            // --- Write each account
            for (Account a : accounts) {
                String interest = "-";
                String creditLimit = "-";

                if (a instanceof SavingsAccount sa)
                    interest = String.valueOf(sa.getInterestRate());
                else if (a instanceof CreditAccount ca) {
                    interest = String.valueOf(ca.getInterestRate());
                    creditLimit = String.valueOf(ca.getCreditLimit());
                }

                writer.write(String.join("|",
                        String.valueOf(a.getType()),
                        a.getAccountId(),
                        a.getCustomerId(),
                        a.getIban(),
                        String.valueOf(a.getBalance()),
                        a.getCurrency(),
                        a.getCreationDate().toString(),
                        interest,
                        creditLimit
                ));
                writer.newLine();
            }

            // --- Update ID generator counter
            int maxId = accounts.stream()
                    .mapToInt(a -> Integer.parseInt(a.getAccountId().substring(1)))
                    .max()
                    .orElse(0);
            IdGenerator.setAccountCounter(maxId + 1);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =============================
    //          LOAD ACCOUNTS
    // =============================

    /**
     * Loads all accounts from the text file {@code accounts.txt}.
     * <p>
     * This method reconstructs {@link Account} objects (of various subclasses)
     * by reading and parsing each line of the text file.
     * 
     *
     * <p>
     * Each record must contain exactly nine fields, separated by {@code |}.
     * Invalid or incomplete lines are skipped automatically.
     * 
     *
     * <p>
     * The reconstruction process also ensures that each loaded account is linked
     * to an existing {@link Customer} object from the provided {@code customers} list.
     * Accounts referencing unknown customers are ignored.
     * 
     *
     * @param customers list of preloaded {@link Customer} objects; used to link account ownership
     * @return a {@link List} of successfully reconstructed {@link Account} instances
     */
    public static List<Account> loadAccounts(List<Customer> customers) {
        List<Account> accounts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Skip empty or commented lines
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split("\\|");
                if (p.length != 9) continue;

                char type = p[0].charAt(0);
                String accountId = p[1];
                String customerId = p[2];
                String iban = p[3];
                double balance = Double.parseDouble(p[4]);
                String currency = p[5];
                LocalDate creationDate = LocalDate.parse(p[6]);
                String interestStr = p[7];
                String creditLimitStr = p[8];

                // Find the associated customer
                Customer customer  = customers.stream()
                        .filter(c -> c.getCustomerId().equals(customerId))
                        .findFirst().orElse(null);

                if (customer == null) {
                    System.err.println("[WARN] Skipping account: missing customer " + customerId);
                    continue;
                }

                // Recreate the correct account subclass
                Account acc;
                switch (type) {
                    case 'D' -> acc = new DebitAccount(
                            'D', accountId, customerId, iban, balance,
                            currency, customer.getCountry(), customer.getFullName(), creationDate
                    );
                    case 'S' -> acc = new SavingsAccount(
                            'S', accountId, customerId, iban, balance,
                            currency, customer.getCountry(), customer.getFullName(), creationDate,
                            !interestStr.equals("-") ? Double.parseDouble(interestStr) : 0.0
                    );
                    case 'C' -> acc = new CreditAccount(
                            'C', accountId, customerId, iban, balance,
                            currency, customer.getCountry(), customer.getFullName(), creationDate,
                            !interestStr.equals("-") ? Double.parseDouble(interestStr) : 0.0,
                            !creditLimitStr.equals("-") ? Double.parseDouble(creditLimitStr) : -5000.0
                    );
                    default -> {
                        System.err.println("[WARN] Unknown account type: " + type);
                        continue;
                    }

                }
                // Add reconstructed account to list
                accounts.add(acc);
            }

            System.out.println("[INFO] Accounts loaded: " + accounts.size());

        } catch (FileNotFoundException e) {
            System.out.println("[INFO] No accounts.txt found — starting fresh.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load accounts: " + e.getMessage());
        }

        // Synchronize ID generator with loaded accounts
        IdGenerator.syncAccountCounter(accounts);
        return accounts;
    }
}
