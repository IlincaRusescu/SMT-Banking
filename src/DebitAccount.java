import java.time.LocalDate;

/**
 * Represents a standard debit account belonging to a {@link Customer}.
 * <p>
 * A {@code DebitAccount} allows deposits and withdrawals up to the available balance.
 * It does not apply any monthly interest or fees.
 * 
 *
 * <h2>Usage:</h2>
 * <ul>
 *   <li>When creating a new account from the UI - use {@link DebitAccount(Customer, String, double, String)}</li>
 *   <li>When restoring existing accounts from file - use {@link #DebitAccount(char, String, String, String, double, String, String, String, LocalDate)}</li>
 * </ul>
 * @author Ilinca Rusescu
 * @version 3.0
 */

public class DebitAccount extends Account{

    // ------------------------------------------------------------
    // Constructor: Normal Creation (automatic ID)
    // ------------------------------------------------------------
    /**
     * Constructs a new {@code DebitAccount} associated with a given {@link Customer}.
     * Generates a new unique {@code accountId} via {@link IdGenerator#nextAccountId()}.
     *
     * @param customer  the customer who owns this account
     * @param iban      optional IBAN (auto-generated if null)
     * @param balance   initial account balance (must be ≥ 0)
     * @param currency  currency code (e.g., "RON", "EUR")
     *
     * @throws IllegalArgumentException if any validation fails
     */

    public DebitAccount( Customer customer, String iban, double balance, String currency) {
        super('D', customer, iban, balance, currency);
    }

    // ------------------------------------------------------------
    // Constructor: Creation from DashboardUI
    // ------------------------------------------------------------
    /**
     * Constructs a new {@code DebitAccount} with a pre-defined {@code accountId}.
     * <p>
     * Used when creating an implicit account for a new user, ensuring consistent ID tracking.
     * 
     *
     * @param accountId unique internal ID (e.g., "A004")
     * @param customer  owner of the account
     * @param iban      optional IBAN (auto-generated if null)
     * @param balance   initial balance
     * @param currency  currency code
     */
    public DebitAccount(String accountId, Customer customer, String iban, double balance, String currency) {
        super('D', accountId, customer, iban, balance, currency);
    }

    // ------------------------------------------------------------
    // Constructor: LOAD FROM FILES
    // ------------------------------------------------------------

    /**
     * Restores an existing debit account from persistent storage.
     *
     * @param type account type ('D')
     * @param accountId unique internal ID (e.g., "A001")
     * @param customerId associated customer ID
     * @param iban IBAN of the account
     * @param balance current balance
     * @param currency account currency (e.g., "RON")
     * @param country ISO country code (e.g., "RO")
     * @param accountHolder full name of account holder
     * @param creationDate date the account was created
     */
    public DebitAccount(char type, String accountId, String customerId, String iban,
                        double balance, String currency, String country,
                        String accountHolder, LocalDate creationDate) {
        super(type, accountId, customerId, iban, balance, currency, country, accountHolder, creationDate);
    }

    // =============================
    //       ACCOUNT OPERATIONS
    // =============================

    /**
     * Deposits a specified positive amount into the account.
     *
     * @param amount the amount to deposit (must be greater than 0)
     * @throws IllegalArgumentException if {@code amount ≤ 0}
     */
    @Override
    public void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
    }

    /**
     * Withdraws a specified amount if sufficient balance is available.
     *
     * @param amount the amount to withdraw (must be greater than 0)
     * @throws IllegalArgumentException if {@code amount ≤ 0} or exceeds available balance
     */
    @Override
    public void withdraw(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance)
            throw new IllegalArgumentException("Insufficient funds.");
        balance -= amount;
    }

    // =============================
    //       MONTHLY LIFECYCLE
    // =============================

    /**
     * Applies monthly updates specific to debit accounts.
     * <p>No monthly interest or fees are applied.
     */
    @Override
    public void applyMonthlyUpdate() {
        // No monthly changes for debit accounts
    }

    /**
     * Returns a string representation of this debit account.
     *
     * @return string containing account details
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
