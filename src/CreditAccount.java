import java.time.LocalDate;

/**
 * Represents a credit account that allows negative balances (debt)
 * and applies a monthly penalty interest if the balance is below zero.
 * <p>
 * A {@code CreditAccount} enables the customer to borrow money up to a specified
 * credit limit. Each month, the account applies a penalty interest on the
 * negative balance (debt), increasing the total amount owed to the bank.
 * 
 *
 * <p>
 * Example of monthly interest:
 * <pre>
 * Initial balance = -1000 RON
 * Interest rate = 5%
 * → New balance after update = -1050 RON
 * </pre>
 * 
 *
 * <p>
 * This class extends the abstract {@link Account} class and overrides its
 * {@link #applyMonthlyUpdate()} and {@link #withdraw(double)} methods to allow
 * controlled debt operations.
 * 
 *
 * @author Ilinca Rusescu
 * @version 1.0
 */

public class CreditAccount extends Account {
    /** Monthly penalty interest rate (e.g., 5 means +5% of debt per month). */
    private final double interestRate;

    /** Maximum allowed credit (e.g., -10000 means you can borrow up to 10,000 RON). */
    private final double creditLimit;


    // ------------------------------------------------------------
    // Constructor: Normal Creation (automatic ID)
    // ------------------------------------------------------------

    /**
     * Constructs a new {@code CreditAccount} linked to a given {@link Customer}.
     * @param customer    owner of the account
     * @param iban        optional IBAN (auto-generated if null)
     * @param balance     initial balance (can be ≤ 0)
     * @param currency    currency code (e.g., "RON")
     * @param interestRate monthly interest rate for debts (e.g., 5.0 = +5%)
     * @param creditLimit  maximum negative balance allowed (e.g., -10000)
     * @throws IllegalArgumentException if any parameter is invalid
     */

    public CreditAccount(Customer customer, String iban, double balance, String currency,
                         double interestRate, double creditLimit) {
        super('C', customer, iban, balance, currency);

        if (interestRate < 0)
            throw new IllegalArgumentException("Interest rate must be positive.");
        if (creditLimit >= 0)
            throw new IllegalArgumentException("Credit limit must be negative.");
        if (balance < creditLimit)
            throw new IllegalArgumentException("Initial balance exceeds credit limit.");

        this.interestRate = interestRate;
        this.creditLimit = creditLimit;
    }

    // ------------------------------------------------------------
    // Constructor: Creation from DashboardUI
    // ------------------------------------------------------------

    /**
     * Creates a credit account with a manually provided account ID.
     *
     * @param accountId    custom account ID
     * @param customer     account owner
     * @param iban         optional IBAN (auto-generated if null)
     * @param balance      current balance
     * @param currency     currency code
     * @param interestRate monthly interest rate (≥ 0)
     * @param creditLimit  maximum allowed debt (&lt; 0)
     */
    public CreditAccount(String accountId, Customer customer, String iban, double balance,
                         String currency, double interestRate, double creditLimit) {
        super('C', accountId, customer, iban, balance, currency);
        if (interestRate < 0)
            throw new IllegalArgumentException("Interest rate must be positive.");
        if (creditLimit >= 0)
            throw new IllegalArgumentException("Credit limit must be negative.");
        if (balance < creditLimit)
            throw new IllegalArgumentException("Initial balance exceeds credit limit.");

        this.interestRate = interestRate;
        this.creditLimit = creditLimit;
    }

    // ------------------------------------------------------------
    // Constructor: LOAD FROM FILES
    // ------------------------------------------------------------

    /**
     * Reconstructs a {@code CreditAccount} from stored file data.
     *
     * @param type          account type ('C')
     * @param accountId     internal ID
     * @param customerId    customer identifier
     * @param iban          IBAN
     * @param balance       current balance
     * @param currency      currency code
     * @param country       customer country
     * @param accountHolder full name of the account holder
     * @param creationDate  date of creation
     * @param interestRate  monthly interest rate
     * @param creditLimit   credit limit (negative value)
     */
    public CreditAccount(char type, String accountId, String customerId, String iban,
                         double balance, String currency, String country,
                         String accountHolder, LocalDate creationDate,
                         double interestRate, double creditLimit) {
        super(type, accountId, customerId, iban, balance, currency, country, accountHolder, creationDate);
        this.interestRate = interestRate;
        this.creditLimit = creditLimit;
    }

    // -----------------------------------------------------
    // GETTERS
    // -----------------------------------------------------

    /** @return interest rate as a double (e.g., 5.0 = 5%) */
    public double getInterestRate() { return interestRate; }

    /** @return credit limit as a negative value (e.g., -10000) */
    public double getCreditLimit() { return creditLimit; }


    // -----------------------------------------------------
    // Overridden Methods
    // -----------------------------------------------------

    /**
     * Withdraws a specified amount, allowing balance to go negative
     * but not below {@link #creditLimit}.
     *
     * @param amount amount to withdraw
     * @throws IllegalArgumentException if amount ≤ 0 or exceeds credit limit
     */

    @Override
    public void withdraw(double amount){
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive.");

        double newBallance = balance - amount;
        if (newBallance < creditLimit)
            throw new IllegalArgumentException("Withdrawal exceeds the credit limit.");

        balance = newBallance;
    }

    /**
     * Applies monthly penalty interest when the balance is negative.
     * <p>Formula: {@code balance -= |balance| * (interestRate / 100)}
     */
    @Override
    public void applyMonthlyUpdate() {
        if (balance < 0) {
            double interest = Math.abs(balance) * (interestRate / 100);
            balance -= interest; // increases debt
        }
    }

    /**
     * Returns a concise string summary including interest and credit limit.
     *
     * @return formatted account details
     */
    @Override
    public String toString() {
        return super.toString() +
                " | interestRate=" + interestRate + "%" +
                " | creditLimit=" + creditLimit;
    }
}
