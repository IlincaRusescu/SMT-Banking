/**
 * Thrown when a financial operation receives an invalid amount
 * such as negative, zero, or mathematically impossible values.
 *
 * @author Ilinca Rusescu
 * @version 1.0
 */

public class InvalidAmountException extends BankException  {
    /**
     * Constructs the exception with a message.
     *
     * @param message error details
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}
