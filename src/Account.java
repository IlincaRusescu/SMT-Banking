import java.time.LocalDate;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Abstract base class representing a general-purpose bank account entity.
 * <p>
 * Each {@code Account} instance is uniquely identified by an account ID, associated with a {@link Customer},
 * and characterized by specific financial attributes such as IBAN, balance, currency, and creation date.
 * 
 *
 * <p>
 * The {@code Account} class provides shared functionality and structure for all specialized account types:
 * {@link DebitAccount}, {@link SavingsAccount}, and {@link CreditAccount}. Each of these subclasses
 * implements its own version of the {@link #applyMonthlyUpdate()} method to reflect the
 * unique financial behavior of that account type — for example, adding interest for savings accounts,
 * charging interest for credit accounts, or leaving balances unchanged for debit accounts.
 * 
 *
 * <p><b>IBAN format (Western European, simplified):</b>
 * <br>{@code CCkkBBBBSSSSXXXXXXXXXXXXXX}
 * <br>where:
 * <ul>
 *   <li><b>CC</b> = country code (e.g., RO, DE)</li>
 *   <li><b>kk</b> = checksum digits</li>
 *   <li><b>BBBBSSSS</b> = bank identifier and branch code</li>
 *   <li><b>X...</b> = account number</li>
 * </ul>
 * 
 *
 * @author Ilinca Rusescu
 * @version 2.1
 */
public abstract class Account {

    /** Account type: 'D' = Debit, 'S' = Savings, 'C' = Credit. */
    protected final char type;

    /** Internal system account ID (e.g., "A001"). */
    protected final String accountId;

    /** The ID of the customer who owns this account. */
    protected final String customerId;

    /** The unique IBAN of the account (automatically generated if not provided). */
    protected final String iban;

    /** Extracted part of the IBAN representing the bank account number. */
    protected final String bankAccount;

    /** Extracted part of the IBAN representing the bank key or branch code. */
    protected final String bankKey;

    /** Full name of the account holder (taken from {@link Customer}). */
    protected final String accountHolder;

    /** Country code used in IBAN generation (from {@link Customer}). */
    protected final String country;

    /** Current balance in the account. */
    protected double balance;

    /** Account currency ("RON", "EUR", "USD"). */
    protected final String currency;

    /** Date when the account was created. */
    protected final LocalDate creationDate;

    /** List of all recorded {@link Transaction} objects linked to this account. */
    protected final List<Transaction> transactions = new ArrayList<>();



    // -------------------------------
    // CONSTRUCTORS
    // -------------------------------


    /**
     * Constructs a new {@code Account} for a given {@link Customer}, generating or validating
     * all required account attributes. Automatically assigns an account ID and IBAN
     * if not provided and validates input data before object creation.
     *
     * @param type          account type (debit, savings, credit)
     * @param customer      the {@link Customer} who owns this account
     * @param iban          unique IBAN; if null, one is auto-generated
     * @param balance       initial balance
     * @param currency      currency code ("RON", "EUR", "USD")
     * @throws IllegalArgumentException if validation fails for any input parameter
     */

    public Account(char type, Customer customer, String iban, double balance, String currency) {

        if (customer == null)
            throw new IllegalArgumentException("Customer cannot be null.");
        if (isBlank(currency))
            throw new IllegalArgumentException("Currency cannot be blank.");
        if (balance < 0 && type != 'C')
            throw new IllegalArgumentException("Negative balance allowed only for Credit accounts.");
        if (type != 'D' && type != 'S' && type != 'C')
            throw new IllegalArgumentException("Invalid account type. Must be 'D', 'S', or 'C'.");

        this.type = type;
        this.accountId = IdGenerator.nextAccountId();
        this.customerId = customer.getCustomerId();
        this.accountHolder = customer.getFullName();
        this.country = customer.getCountry().toUpperCase();
        this.balance = balance;
        this.currency = currency.trim().toUpperCase();
        this.creationDate = LocalDate.now();

        // Generate IBAN if not provided
        this.iban = (iban == null || iban.isBlank()) ? generateIban(accountId, country) : iban.trim();

        // Extract details from IBAN
        this.bankKey = this.iban.substring(4, 12);      // BBBBSSSS
        this.bankAccount = this.iban.substring(12);     // remaining portion
    }

    /**
     * Constructs a fully specified {@code Account} using all known attributes.
     * <p>
     * This constructor is typically used when loading account data from persistent storage (e.g., a file or database).
     * 
     *
     * @param type           character representing account type ('D', 'S', 'C')
     * @param accountId      internal system account ID
     * @param customerId     unique customer identifier associated with this account
     * @param iban           the full IBAN of the account
     * @param balance        the current or stored balance of the account
     * @param currency       ISO currency code (e.g., "RON", "EUR")
     * @param country        two-letter ISO country code
     * @param accountHolder  full name of the account owner
     * @param creationDate   date when the account was originally created
     */
    public Account(char type, String accountId, String customerId, String iban,
                   double balance, String currency, String country,
                   String accountHolder, LocalDate creationDate) {

        this.type = type;
        this.accountId = accountId;
        this.customerId = customerId;
        this.iban = iban;
        this.balance = balance;
        this.currency = currency;
        this.country = country;
        this.accountHolder = accountHolder;
        this.creationDate = creationDate;

        this.bankKey = iban.substring(4, 12);
        this.bankAccount = iban.substring(12);
    }

    /**
     * Constructs an {@code Account} using a manually provided account ID.
     * <p>
     * This version of the constructor is used when account IDs are externally controlled
     * (e.g., during data migration or synchronization with another system).
     * 
     *
     * @param type         character representing account type ('D', 'S', or 'C')
     * @param accountId    manually provided account ID (must not be blank)
     * @param customer     the {@link Customer} who owns this account
     * @param iban         predefined IBAN (optional); generated automatically if {@code null} or blank
     * @param balance      initial or restored balance
     * @param currency     ISO currency code (e.g., "RON", "EUR")
     * @throws IllegalArgumentException if validation fails for type, accountId, or currency
     */
    public Account(char type, String accountId, Customer customer, String iban,
                   double balance, String currency) {

        if (customer == null)
            throw new IllegalArgumentException("Customer cannot be null.");
        if (isBlank(accountId))
            throw new IllegalArgumentException("Account ID cannot be blank.");
        if (isBlank(currency))
            throw new IllegalArgumentException("Currency cannot be blank.");
        if (balance < 0 && type != 'C')
            throw new IllegalArgumentException("Negative balance allowed only for Credit accounts.");
        if (type != 'D' && type != 'S' && type != 'C')
            throw new IllegalArgumentException("Invalid account type.");

        this.type = type;
        this.accountId = accountId;
        this.customerId = customer.getCustomerId();
        this.accountHolder = customer.getFullName();
        this.country = customer.getCountry().toUpperCase();
        this.balance = balance;
        this.currency = currency.trim().toUpperCase();
        this.creationDate = java.time.LocalDate.now();

        this.iban = (iban == null || iban.isBlank())
                ? generateIban(accountId, country)
                : iban.trim();

        this.bankKey = this.iban.substring(4, 12);
        this.bankAccount = this.iban.substring(12);
    }


    // -------------------------------
    // GETTERS
    // -------------------------------

    /** @return account type ('D', 'S', or 'C') */
    public char getType() { return type; }

    /** @return internal account ID (e.g., "A001") */
    public String getAccountId() { return accountId; }

    /** @return the ID of the customer who owns this account */
    public String getCustomerId() { return customerId; }

    /** @return full IBAN of this account */
    public String getIban() { return iban; }

    /** @return extracted internal bank account portion of the IBAN */
    public String getBankAccount() { return bankAccount; }

    /** @return bank key (branch/sort code) extracted from IBAN */
    public String getBankKey() { return bankKey; }

    /** @return full name of the account holder */
    public String getAccountHolder() { return accountHolder; }

    /** @return ISO country code (e.g., "RO", "DE") */
    public String getCountry() { return country; }

    /** @return current account balance */
    public double getBalance() { return balance; }

    /** @return currency of the account */
    public String getCurrency() { return currency; }

    /** @return creation date of the account */
    public LocalDate getCreationDate() { return creationDate; }

    /** @return unmodifiable list of account transactions */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // -------------------------------
    // TRANSACTION HANDLING
    // -------------------------------
    /**
     * Adds a new transaction to the account's transaction list.
     * <p>
     * Used to maintain a complete history of all debit, credit, and transfer operations
     * performed on this account.
     * 
     *
     * @param type        transaction type descriptor (e.g., "Deposit", "Withdrawal")
     * @param amount      transaction amount in account currency
     * @param description optional text describing the transaction details
     */
    public void addTransaction(String type, double amount, String description) {
        transactions.add(new Transaction(this.customerId, this.accountId, type, amount, description));
    }

    // -------------------------------
    // ACCOUNT OPERATIONS
    // -------------------------------

    /**
     * Deposits a specified positive amount into the account balance.
     *
     * @param amount the amount to deposit; must be greater than zero
     * @throws IllegalArgumentException if {@code amount} ≤ 0
     */

    public void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive.");

        balance += amount;
    }

    /**
     * Withdraws a specified amount from the account, ensuring sufficient funds exist.
     * <p>
     * The operation will fail if the balance is insufficient or if the amount is invalid.
     * 
     *
     * @param amount the amount to withdraw
     * @throws InvalidAmountException     if the specified amount is zero or negative
     * @throws InsufficientFundsException if withdrawal exceeds available balance
     */

    public void withdraw(double amount) {
        if (amount <= 0)
            throw new InvalidAmountException("Withdrawal amount must be positive.");
        if (amount > balance)
            throw new InsufficientFundsException("Insufficient funds.");

        balance -= amount;
    }

    /**
     * Abstract method representing the monthly update logic for the account.
     * <p>
     * Each subclass defines how the account evolves monthly:
     * <ul>
     *     <li>{@link DebitAccount} → no changes are applied</li>
     *     <li>{@link SavingsAccount} → interest is added to the balance</li>
     *     <li>{@link CreditAccount} → debt interest is applied</li>
     * </ul>
     * 
     */
    public abstract void applyMonthlyUpdate();



    // -------------------------------
    // UTILITY METHODS
    // -------------------------------

    /**
     * Generates a random IBAN string for demonstration or testing purposes.
     * The method uses the given {@code country} and {@code accountId} to build
     * a pseudo-realistic IBAN following a Western European structure.
     *
     * @param accountId internal system account ID (used as suffix)
     * @param country   two-letter ISO country code
     * @return formatted pseudo-random IBAN
     */

    protected static String generateIban(String accountId,String country){
        Random random = new Random();

        int checkDigits = 10 + random.nextInt(90); //10-99
        int branchCode = 1000 + random.nextInt(9000); //1000 - 9999
        int randomSuffix = 10 + random.nextInt(90); // 2 digits

        return String.format("%s%dSMTB%d%s%d", country.toUpperCase(), checkDigits,
                branchCode, accountId, randomSuffix);
    }

    /**
     * Utility method to determine if a string is null, empty, or contains only whitespace.
     *
     * @param s the string to evaluate
     * @return {@code true} if the string is blank, otherwise {@code false}
     */
    protected static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Returns a human-readable representation of this account, displaying its
     * key identifying information and financial attributes.
     *
     * @return formatted string summarizing account details
     */
    @Override
    public String toString() {
        return "Account{" +
                "type=" + type +
                ", accountId='" + accountId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", iban='" + iban + '\'' +
                ", bankKey='" + bankKey + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                ", holder='" + accountHolder + '\'' +
                ", country='" + country + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", created=" + creationDate +
                '}';
    }
}
