import java.util.List;

/**
 * Utility class responsible for generating unique IDs for entities such as customers or accounts.
 * <p>
 * This class provides simple static methods that auto-increment
 * counters and return formatted IDs like {@code C001}, {@code C002}, etc.
 *
 * <p>In a more advanced system, this logic could be replaced with a database sequence.
 *
 * @author Ilinca Rusescu
 * @version 2.1
 */

public class IdGenerator {

    /** Internal counter for generated customer IDs. */
    private static  int customerCounter =1;

    /** Internal counter for generated account IDs (future use). */
    private static int accountCounter =1;

    /**
     * Generates a new unique customer ID in the format {@code C001}, {@code C002}, etc.
     *
     * @return a formatted customer ID string
     */
    public static String nextCustomerId(){
        return String.format("C%03d", customerCounter++);
    }

    /**
     * Updates the internal customer counter to the next available number
     * based on the list of already existing customers.
     * <p>
     * Should be called once after loading customers from file to ensure
     * new IDs continue sequentially.
     * 
     *
     * @param customers list of customers already loaded from file
     */
    public static synchronized void syncCustomerCounter(List<Customer> customers) {
        int max = customers.stream()
                .map(Customer::getCustomerId)
                .filter(id -> id != null && id.matches("C\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(1)))
                .max()
                .orElse(0);
        customerCounter = max + 1;
    }
    /**
     * Generates a new unique account ID in the format {@code A001}, {@code A002}, etc.
     *
     * @return a formatted account ID string
     */
    public static String nextAccountId() {
        return String.format("A%03d", accountCounter++);
    }

    /**
     * Updates the internal account counter to the next available number
     * based on the list of already existing accounts.
     * <p>
     * Should be called once after loading accounts from file to ensure
     * new IDs continue sequentially (no duplicates like A001 for different customers).
     * 
     *
     * @param accounts list of accounts already loaded from file
     */
    public static synchronized void syncAccountCounter(List<Account> accounts) {
        int max = accounts.stream()
                .map(Account::getAccountId)
                .filter(id -> id != null && id.matches("A\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(1)))
                .max()
                .orElse(0);
        accountCounter = max + 1;
    }


    // -----------------------------
    // RESET / SYNC METHODS
    // -----------------------------

    /**
     * Resets both counters to start from 1.
     * (Use only for testing or when resetting data files.)
     */
    public static synchronized void resetAll() {
        customerCounter = 1;
        accountCounter = 1;
    }

    /**
     * Manually sets the customer counter to a specific next value.
     * Usually called after loading existing data to continue from the highest existing ID.
     * @param nextCounter the next number to use for new customers
     */
    public static synchronized void setCustomerCounter(int nextCounter) {
        customerCounter = Math.max(nextCounter, 1);
    }



    // -----------------------------
    // DEBUG / INFO HELPERS
    // -----------------------------

    /**
     * Manually sets the account counter to a specific next value.
     * Usually called after loading existing accounts to continue from the highest existing ID.
     * @param nextCounter the next number to use for new accounts
     */
    public static synchronized void setAccountCounter(int nextCounter) {
        accountCounter = Math.max(nextCounter, 1);
    }

    /** @return current next value for customers (useful for debugging) */
    public static synchronized int getCustomerCounter() {
        return customerCounter;
    }

    /** @return current next value for accounts (useful for debugging) */
    public static synchronized int getAccountCounter() {
        return accountCounter;
    }
}
