import java.time.LocalDateTime;

/**
 * Represents a financial transaction performed on a bank account.
 * <p>
 * Each transaction stores:
 * <ul>
 *     <li>Time when it occurred</li>
 *     <li>Type of operation (Deposit, Withdraw, Credit Taken, etc.)</li>
 *     <li>Amount involved</li>
 *     <li>Optional description for record clarity</li>
 * </ul>
 * 
 *
 * <p>Examples of transaction types:
 * <pre>
 * "DEPOSIT"
 * "WITHDRAW"
 * "CREDIT_TAKEN"
 * "CREDIT_REPAY"
 * "INTEREST_APPLIED"
 * </pre>
 *
 * This class is immutable — once created, transactions cannot be altered.
 *
 * @author Ilinca Rusescu
 * @version 2.0
 */

public class Transaction {

    /** ID of the customer involved in the transaction. */
    private final String customerId;
    /** ID of the account involved in the transaction. */
    private final String accountId;

    /** Date and time when the transaction occurred. */
    private final LocalDateTime timestamp;

    /** Type of transaction (e.g., "Deposit", "Withdraw", etc.). */
    private final String type;

    /** Amount of money involved in the transaction. */
    private final double amount;

    /** Additional descriptive info about the transaction. */
    private final String description;

    /**
     * Constructs a new {@code Transaction}.
     *
     * @param customerId ID of the customer
     * @param accountId   ID of the account
     * @param type        transaction type (cannot be blank)
     * @param amount      transaction amount
     * @param description optional details
     */
    public Transaction(String customerId, String accountId, LocalDateTime timestamp, String type, double amount, String description) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    /**
     * Constructor used when creating a new transaction normally (timestamp = now).
     *
     * @param type        transaction type
     * @param amount      transaction amount
     * @param description optional descriptive text
     */
    public Transaction(String customerId, String accountId, String type, double amount, String description) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.timestamp = LocalDateTime.now(); // automatic timestamp
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    // =============================
    //           GETTERS
    // =============================

    /** @return the ID of the customer this transaction belongs to */
    public String getCustomerId() { return customerId; }

    /** @return the ID of the account this transaction belongs to */
    public String getAccountId() { return accountId; }

    /** @return when the transaction took place */
    public LocalDateTime getTimestamp() { return timestamp; }

    /** @return transaction type */
    public String getType() { return type; }

    /** @return transaction amount */
    public double getAmount() { return amount; }

    /** @return transaction description */
    public String getDescription() { return description; }

    // =============================
    //           UTILITIES
    // =============================
    /**
     * Returns a concise string summarizing the transaction details.
     *
     * @return formatted string containing timestamp, customer, type, amount, and description
     */
    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %.2f | %s",
                timestamp, customerId, type, amount, description);
    }
}
