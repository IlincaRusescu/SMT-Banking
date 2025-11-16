import java.util.List;

/**
 * Provides high-level banking operations on existing account data.
 * <p>
 * The service acts as the business logic layer between:
 * <ul>
 *     <li>UI (Console / JavaFX)</li>
 *     <li>Domain model ({@link Customer}, {@link Account}, {@link Transaction})</li>
 * </ul>
 *
 * This class does not store or persist data itself. All data remains in memory
 * until saved by FileManager classes on application exit.
 *
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *     <li>Finding accounts</li>
 *     <li>Creating accounts</li>
 *     <li>Processing transfers (internal &amp; external)</li>
 *     <li>Applying monthly interest updates</li>
 * </ul>
 *
 * <p><b>Important Business Rule:</b><br>
 * Only <b>Debit Accounts</b> can send transfers. Savings and Credit accounts
 * must first transfer money to a Debit Account before external transfers.
 * 
 *
 * @author Ilinca Rusescu
 * @version 2.2
 */
public class BankService {

//    // --------------------------------------------------
//    // ACCOUNT LOOKUP
//    // --------------------------------------------------
//
//    /**
//     * Finds an account by its internal ID (e.g., "A004").
//     *
//     * @param accounts list of all accounts in memory
//     * @param accountId ID to search
//     * @return matching account or {@code null} if not found
//     */
//    public static Account findAccountById(List<Account> accounts, String accountId) {
//        return accounts.stream()
//                .filter(acc -> acc.getAccountId().equals(accountId))
//                .findFirst()
//                .orElse(null);
//    }

    /**
     * Returns all accounts owned by a specific customer.
     *
     * @param accounts list of all accounts
     * @param customerId ID of the owner
     * @return list of accounts owned by that customer
     */

    public static List<Account> findAccountsByCustomer(List<Account> accounts, String customerId) {
        return accounts.stream()
                .filter(acc -> acc.getCustomerId().equals(customerId))
                .toList();
    }

    // --------------------------------------------------
    // ACCOUNT CREATION
    // --------------------------------------------------

    /**
     * Creates a new Debit account with zero initial balance.
     *
     * @param accounts list of existing accounts
     * @param customer owner of the new account
     * @param currency currency (RON / EUR / USD)
     * @return created {@link DebitAccount}
     */

    @Deprecated(forRemoval = true)
    public static DebitAccount createDebitAccount(List<Account> accounts, Customer customer, String currency) {
        String newId = "A" + String.format("%03d", accounts.size() + 1);
        DebitAccount acc = new DebitAccount(customer, null, 0, currency);
        accounts.add(acc);
        return acc;
    }

    // --------------------------------------------------
    // INTERNAL TRANSFER (same customer)
    // --------------------------------------------------

    /**
     * Transfers money between accounts belonging to the same customer.
     * Currency conversion is handled automatically when needed.
     *
     * @param from source account (must be Debit)
     * @param to destination account (same customer)
     * @param amount positive transfer amount
     */
    public static void transferInternal(Account from, Account to, double amount) {
        if (!from.getCustomerId().equals(to.getCustomerId()))
            throw new IllegalArgumentException("Internal transfer allowed only within same customer.");

        if (from.getType() != 'D')
            throw new IllegalArgumentException("Transfers must originate from a Debit account.");

        double converted = ExchangeRate.convert(amount, from.getCurrency(), to.getCurrency());

        from.withdraw(amount);
        to.deposit(converted);
    }

    // --------------------------------------------------
    // EXTERNAL TRANSFER (customer → another customer)
    // --------------------------------------------------

    /**
     * Transfers funds from one customer to another.
     * <p>
     * Business rules:
     * <ul>
     *     <li>Source must be a Debit account</li>
     *     <li>Destination can be any account type</li>
     *     <li>Currency conversion occurs automatically</li>
     *     <li>Transaction history records both sides with names</li>
     * </ul>
     *
     * @param from source account (must be Debit)
     * @param to destination account
     * @param amount amount to transfer (must be positive)
     */

    public static void transferExternal(Account from, Account to, double amount) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Both accounts must exist.");

        if (amount <= 0)
            throw new IllegalArgumentException("Transfer amount must be positive.");

        if (from.getType() != 'D')
            throw new IllegalArgumentException("Only Debit accounts can send external transfers.");

        // Currency conversion
        double convertedAmount = ExchangeRate.convert(amount, from.getCurrency(), to.getCurrency());

        // Withdraw from sender
        from.withdraw(amount);
        from.addTransaction(
                "TRANSFER_SENT",
                -amount,
                "To: " + to.getAccountHolder()
        );


        // Deposit to receiver
        to.deposit(convertedAmount);
        to.addTransaction(
                "TRANSFER_RECEIVED",
                convertedAmount,
                "From: " + from.getAccountHolder()
        );

        System.out.printf("[INFO] External transfer completed: %.2f %s from %s → %.2f %s to %s%n",
                amount,
                from.getCurrency(),
                from.getAccountHolder(),
                convertedAmount,
                to.getCurrency(),
                to.getAccountHolder()
        );
    }

    // --------------------------------------------------
    // MONTHLY PROCESSING
    // --------------------------------------------------

    /**
     * Applies monthly updates:
     * <ul>
     *     <li>Savings → adds interest</li>
     *     <li>Credit → increases owed balance</li>
     *     <li>Debit → no effect</li>
     * </ul>
     *
     * @param accounts all existing accounts
     */
    public static void applyMonthlyProcessing(List<Account> accounts) {
        accounts.forEach(Account::applyMonthlyUpdate);
    }
}
