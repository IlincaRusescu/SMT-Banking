/**
 * Base class for all custom banking-related exceptions.
 * Allows grouping and handling all banking errors easily.
 *
 * @author Ilinca Rusescu
 * @version 1.0
 */
public abstract class BankException extends RuntimeException {
    public BankException(String message) {
        super(message);
    }
}
