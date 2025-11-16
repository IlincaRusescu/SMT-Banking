/**
 * Thrown when an account does not have enough balance
 * to complete a withdrawal or debit-to-credit repayment operation.
 *
 * @author Ilinca Rusescu
 * @version 1.0
 */
public class InsufficientFundsException extends BankException  {

    /**
     * Constructs the exception with a message.
     *
     * @param message error details
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
}
