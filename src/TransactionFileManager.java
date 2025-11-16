import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for saving and loading {@link Transaction} history.
 * <p>
 * All transactions are stored in a single text file, where each line follows:
 * <pre>
 * customerId|accountId|timestamp|type|amount|description
 * </pre>
 * Lines starting with {@code #} are treated as comments.
 * 
 * @author Ilinca Rusescu
 * @version 1.0
 */

public class TransactionFileManager {

    /** Default file path for transaction storage. */
    private static final String FILE_PATH = "transactions.txt";

    // =============================
    //        SAVE TRANSACTIONS
    // =============================

    /**
     * Saves all transactions from every account into {@code transactions.txt}.
     * <p>
     * Transactions are merged, sorted chronologically, and written to file
     * in the format:
     * <pre>
     * customerId|accountId|timestamp|type|amount|description
     * </pre>
     * The file is overwritten on each save.
     * 
     *
     * @param accounts list of {@link Account} objects containing transactions
     */
    public static void saveTransactions(List<Account> accounts) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {

            // --- Header line for readability
            writer.write("# customerId|accountId|timestamp|type|amount|description");
            writer.newLine();

            // --- Collect and sort all transactions
            List<Transaction> allTransactions = new ArrayList<>();
            for (Account acc : accounts) {
                allTransactions.addAll(acc.getTransactions());
            }

            allTransactions.sort(Comparator.comparing(Transaction::getTimestamp));

            // --- Write to file
            for (Transaction t : allTransactions) {
                writer.write(String.join("|",
                        t.getCustomerId(),
                        t.getAccountId(),
                        t.getTimestamp().toString(),
                        t.getType(),
                        String.valueOf(t.getAmount()),
                        t.getDescription().replace("|", "/") // avoid format conflict
                ));
                writer.newLine();
            }

            System.out.println("[INFO] Transactions saved successfully.");

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save transactions: " + e.getMessage());
        }
    }

    // =============================
    //        LOAD TRANSACTIONS
    // =============================

    /**
     * Loads all transactions from {@code transactions.txt} and attaches them
     * to their corresponding {@link Account} objects.
     * <p>
     * Each transaction line must contain exactly six fields:
     * <pre>
     * customerId|accountId|timestamp|type|amount|description
     * </pre>
     * Lines that are empty, commented, or malformed are ignored.
     * 
     *
     * @param accounts list of already loaded accounts (to attach transactions to)
     */
    public static void loadTransactions(List<Account> accounts) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] p = line.split("\\|", -1);
                if (p.length != 6) continue; // skip malformed lines

                String customerId = p[0];
                String accountId = p[1];
                LocalDateTime timestamp = LocalDateTime.parse(p[2]);
                String type = p[3];
                double amount = Double.parseDouble(p[4]);
                String description = p[5];

                Transaction t = new Transaction(customerId, accountId, timestamp, type, amount, description);

                // --- Attach transaction to its account
                accounts.stream()
                        .filter(a -> a.getAccountId().equals(accountId))
                        .findFirst()
                        .ifPresent(a -> a.transactions.add(t));
            }

            System.out.println("[INFO] Transactions loaded successfully.");

        } catch (FileNotFoundException e) {
            System.out.println("[INFO] No transactions file found. Starting with empty history.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load transactions: " + e.getMessage());
        }
    }
}
