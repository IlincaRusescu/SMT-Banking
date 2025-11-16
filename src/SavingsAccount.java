import java.time.LocalDate;

/**
 * Represents a savings account that earns monthly interest.
 * <p>
 * A {@code SavingsAccount} behaves like a standard account but
 * automatically adds interest to its balance each month.
 * 
 *
 * @author Ilinca Rusescu
 * @version 3.0
 */

public class SavingsAccount extends Account {

    /** Monthly interest rate in percent (e.g., 2.5 = 2.5% per month). */
    private final double interestRate;


    // ------------------------------------------------------------
    // Constructor: Normal Creation (automatic ID)
    // ------------------------------------------------------------
    /**
     * Constructs a new {@code SavingsAccount} associated with a given {@link Customer}.

     *
     * @param customer    owner of the account
     * @param iban        optional IBAN (auto-generated if null)
     * @param balance     initial balance (must be ≥ 0)
     * @param currency    currency code (e.g., "RON", "EUR")
     * @param interestRate monthly interest rate (e.g., 2.0 = +2%)
     *
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public SavingsAccount(Customer customer, String iban,
                          double balance, String currency, double interestRate){
        super('S',customer,iban,balance,currency);

        if (interestRate < 0) {
            throw new IllegalArgumentException("Interest rate must be positive.");
        }
        this.interestRate = interestRate;
    }

    // ------------------------------------------------------------
    // Constructor: Creation from DashboardUI
    // ------------------------------------------------------------
    /**
     * Constructs a new {@code SavingsAccount} with a pre-defined {@code accountId}.
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
    public SavingsAccount(String accountId, Customer customer, String iban, double balance, String currency, double interestRate) {
        super('S', accountId, customer, iban, balance, currency);
        if (interestRate < 0)
            throw new IllegalArgumentException("Interest rate must be positive.");
        this.interestRate = interestRate;
    }


    // ------------------------------------------------------------
    // Constructor: LOAD FROM FILES
    // ------------------------------------------------------------

    /**
     * Restores an existing {@code SavingsAccount} from file.
     *
     * @param type account type ('S')
     * @param accountId internal system ID (e.g., "A002")
     * @param customerId customer ID (e.g., "C001")
     * @param iban account IBAN
     * @param balance current balance
     * @param currency account currency (e.g., "RON")
     * @param country country code (e.g., "RO")
     * @param accountHolder account holder full name
     * @param creationDate date of creation
     * @param interestRate monthly interest rate in percent
     */
    public SavingsAccount(char type, String accountId, String customerId, String iban,
                          double balance, String currency, String country,
                          String accountHolder, LocalDate creationDate, double interestRate) {
        super(type, accountId, customerId, iban, balance, currency, country, accountHolder, creationDate);
        this.interestRate = interestRate;
    }

    // =============================
    //           GETTERS
    // =============================

    /**
     * @return the monthly interest rate (percentage)
     */
    public double getInterestRate() {
        return interestRate;
    }

    // =============================
    //       ACCOUNT BEHAVIOR
    // =============================

    /**
     * Applies monthly interest to the current balance.
     * <p>Formula: {@code balance += balance * (interestRate / 100)}
     */
    @Override
    public void applyMonthlyUpdate() {
        double earnedInterest = balance * (interestRate / 100);
        balance += earnedInterest;
    }

    /**
     * Returns a string representation of this savings account including its interest rate.
     *
     * @return formatted account information string
     */
    @Override
    public String toString() {
        return super.toString() + " | interestRate=" + interestRate + "%";
    }
}
