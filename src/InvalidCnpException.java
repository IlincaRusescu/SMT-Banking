/**
 * Thrown when a provided CNP (national identification number)
 * does not match the required format (length, digits, etc.)
 * or already exists in the system.
 *
 * @author Ilinca Rusescu
 * @version 1.0
 */
public class InvalidCnpException extends BankException  {

    /**
     * Constructs the exception with a message.
     *
     * @param message error details
     */
    public InvalidCnpException(String message) {
        super(message);
    }
}
